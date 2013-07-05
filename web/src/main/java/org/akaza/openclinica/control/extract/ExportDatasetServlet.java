/*******************************************************************************
 * ClinCapture, Copyright (C) 2009-2013 Clinovo Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the Lesser GNU General Public License 
 * as published by the Free Software Foundation, either version 2.1 of the License, or(at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Lesser GNU General Public License for more details.
 * 
 * You should have received a copy of the Lesser GNU General Public License along with this program.  
 \* If not, see <http://www.gnu.org/licenses/>. Modified by Clinovo Inc 01/29/2013.
 ******************************************************************************/

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.control.extract;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.*;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.extract.GenerateExtractFileService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.bean.ArchivedDatasetFileRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.job.XalanTriggerService;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.triggers.SimpleTriggerImpl;

/**
 * Take a dataset and show it in different formats,<BR/>
 * Detect whether or not files exist in the system or database,<BR/>
 * Give the user the option of showing a stored dataset, or refresh the current one. </P>
 * 
 * TODO eventually allow for a thread to be split off, so that exporting can run seperately from the servlet and be
 * retrieved at a later time.
 * 
 * @author thickerson
 * 
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class ExportDatasetServlet extends SecureController {

	public static String getLink(int dsId) {
		return "ExportDataset?datasetId=" + dsId;
	}

	private StdScheduler scheduler;

	private static String SCHEDULER = "schedulerFactoryBean";
	private static final String DATASET_DIR = SQLInitServlet.getField("filePath") + "datasets" + File.separator;

	// may not use the above, security issue
	public File SASFile;
	public String SASFilePath;
	public File SPSSFile;
	public String SPSSFilePath;
	public File TXTFile;
	public String TXTFilePath;
	public File CSVFile;
	public String CSVFilePath;
	public ArrayList fileList;

	@Override
	public void processRequest() throws Exception {
		DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
		ArchivedDatasetFileDAO asdfdao = new ArchivedDatasetFileDAO(sm.getDataSource());
		FormProcessor fp = new FormProcessor(request);

		GenerateExtractFileService generateFileService = new GenerateExtractFileService(sm.getDataSource(), request,
				sm.getUserBean(), (CoreResources) SpringServletAccess.getApplicationContext(context).getBean(
						"coreResources"), (RuleSetRuleDao) SpringServletAccess.getApplicationContext(context).getBean(
						"ruleSetRuleDao"));
		String action = fp.getString("action");
		int datasetId = fp.getInt("datasetId");
		int adfId = fp.getInt("adfId");
		if (datasetId == 0) {
			try {
				DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
				datasetId = dsb.getId();
				logger.info("dataset id was zero, trying session: " + datasetId);
			} catch (NullPointerException e) {

				e.printStackTrace();
				logger.info("tripped over null pointer exception");
			}
		}
		DatasetBean db = (DatasetBean) dsdao.findByPK(datasetId);
		StudyDAO sdao = new StudyDAO(sm.getDataSource());
		StudyBean study = (StudyBean) sdao.findByPK(db.getStudyId());
		checkRoleByUserAndStudy(ub, study.getParentStudyId(), study.getId());

		// Checks if the study is current study or child of current study
		if (study.getId() != currentStudy.getId() && study.getParentStudyId() != currentStudy.getId()) {
			addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " "
					+ respage.getString("change_active_study_or_contact"));
			forwardPage(Page.MENU_SERVLET);
			return;
		}

		int currentstudyid = currentStudy.getId();
		int parentstudy = currentstudyid;

		StudyBean parentStudy = new StudyBean();
		if (currentStudy.getParentStudyId() > 0) {
			parentStudy = (StudyBean) sdao.findByPK(currentStudy.getParentStudyId());
		}

		ExtractBean eb = generateFileService.generateExtractBean(db, currentStudy, parentStudy);

		if (StringUtil.isBlank(action)) {
			loadList(db, asdfdao, datasetId, fp, eb);
			forwardPage(Page.EXPORT_DATASETS);
		} else if ("delete".equalsIgnoreCase(action) && adfId > 0) {
			boolean success = false;
			ArchivedDatasetFileBean adfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(adfId);
			File file = new File(adfBean.getFileReference());
			if (!file.canWrite()) {
				addPageMessage(respage.getString("write_protected"));
			} else {
				success = file.delete();
				if (success) {
					asdfdao.deleteArchiveDataset(adfBean);
					addPageMessage(respage.getString("file_removed"));
				} else {
					addPageMessage(respage.getString("error_removing_file"));
				}
			}
			loadList(db, asdfdao, datasetId, fp, eb);
			forwardPage(Page.EXPORT_DATASETS);
		} else {
			logger.info("**** found action ****: " + action);
			String generateReport = "";
			String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS"
					+ File.separator;
			SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
			String generalFileDir = DATASET_DIR + db.getId() + File.separator + sdfDir.format(new java.util.Date());
			db.setName(db.getName().replaceAll(" ", "_"));
			Page finalTarget = Page.GENERATE_DATASET;
			finalTarget = Page.EXPORT_DATA_CUSTOM;

			// now display report according to format specified

			// TODO revise final target to set to fileReference????
			long sysTimeBegin = System.currentTimeMillis();
			int fId = 0;
			if ("sas".equalsIgnoreCase(action)) {
				long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
				String SASFileName = db.getName() + "_sas.sas";
				generateFileService.createFile(SASFileName, generalFileDir, generateReport, db, sysTimeEnd,
						ExportFormatBean.TXTFILE, true);
				logger.info("created sas file");
				request.setAttribute("generate", generalFileDir + SASFileName);
				finalTarget.setFileName(generalFileDir + SASFileName);
			} else if ("odm".equalsIgnoreCase(action)) {
				String odmVersion = fp.getString("odmVersion");
				String ODMXMLFileName = "";
				HashMap answerMap = generateFileService.createODMFile(odmVersion, sysTimeBegin, generalFileDir, db,
						this.currentStudy, "", eb, currentStudy.getId(), currentStudy.getParentStudyId(), "99", true,
						true, true, null);

				for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
					java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
					Object key = entry.getKey();
					Object value = entry.getValue();
					ODMXMLFileName = (String) key;
					Integer fileID = (Integer) value;
					fId = fileID.intValue();
				}
				request.setAttribute("generate", generalFileDir + ODMXMLFileName);
				System.out.println("+++ set the following: " + generalFileDir + ODMXMLFileName);
				// Working group
				// put an extra flag here, where we generate the XML, and then
				// find the XSL, run a job and
				// send a link with the SQL file? put the generated SQL file
				// with the dataset?
				if (fp.getString("xalan") != null) {
					XalanTriggerService xts = new XalanTriggerService();

					String propertiesPath = SQLInitServlet.getField("filePath");

					// the trick there, we need to open up the zipped file and
					// get at the XML
					openZipFile(generalFileDir + ODMXMLFileName + ".zip");
					// need to find out how to copy this xml file from /bin to
					// the generalFileDir
					SimpleTriggerImpl simpleTrigger = xts.generateXalanTrigger(propertiesPath + File.separator
							+ "ODMReportStylesheet.xsl", ODMXMLFileName, generalFileDir + "output.sql", db.getId());
					scheduler = getScheduler();

					JobDetailImpl jobDetailBean = new JobDetailImpl();
					jobDetailBean.setGroup(XalanTriggerService.TRIGGER_GROUP_NAME);
					jobDetailBean.setName(simpleTrigger.getName());
					jobDetailBean.setJobClass(org.akaza.openclinica.web.job.XalanStatefulJob.class);
					jobDetailBean.setJobDataMap(simpleTrigger.getJobDataMap());
					jobDetailBean.setDurability(true); // need durability?
					// jobDetailBean.setVolatility(false);

					try {
						Date dateStart = scheduler.scheduleJob(jobDetailBean, simpleTrigger);
						logger.info("== found job date: " + dateStart.toString());
					} catch (SchedulerException se) {
						se.printStackTrace();
					}
				}
			} else if ("txt".equalsIgnoreCase(action)) {
				String TXTFileName = "";
				HashMap answerMap = generateFileService.createTabFile(eb, sysTimeBegin, generalFileDir, db,
						currentstudyid, parentstudy, "");
				// the above gets us the best of both worlds - the file name,
				// together with the file id which we can then
				// push out to the browser. Shame that it is a long hack,
				// though. need to pare it down later, tbh
				// and of course DRY
				for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
					java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
					Object key = entry.getKey();
					Object value = entry.getValue();
					TXTFileName = (String) key;
					Integer fileID = (Integer) value;
					fId = fileID.intValue();
				}
				request.setAttribute("generate", generalFileDir + TXTFileName);
				System.out.println("+++ set the following: " + generalFileDir + TXTFileName);
			} else if ("html".equalsIgnoreCase(action)) {
				// html based dataset browser
				TabReportBean answer = new TabReportBean();

				eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);
				eb.getMetadata();
				eb.computeReport(answer);
				request.setAttribute("dataset", db);
				request.setAttribute("extractBean", eb);
				finalTarget = Page.GENERATE_DATASET_HTML;

			} else if ("spss".equalsIgnoreCase(action)) {
				SPSSReportBean answer = new SPSSReportBean();

				eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);

				eb.getMetadata();

				eb.computeReport(answer);

				String DDLFileName = "";
				HashMap answerMap = generateFileService.createSPSSFile(db, eb, currentStudy, parentStudy, sysTimeBegin,
						generalFileDir, answer, "");

				for (Iterator it = answerMap.entrySet().iterator(); it.hasNext();) {
					java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
					Object key = entry.getKey();
					Object value = entry.getValue();
					DDLFileName = (String) key;
					Integer fileID = (Integer) value;
					fId = fileID.intValue();
				}
				request.setAttribute("generate", generalFileDir + DDLFileName);
				System.out.println("+++ set the following: " + generalFileDir + DDLFileName);
			} else if ("csv".equalsIgnoreCase(action)) {
				CommaReportBean answer = new CommaReportBean();
				eb = dsdao.getDatasetData(eb, currentstudyid, parentstudy);
				eb.getMetadata();
				eb.computeReport(answer);
				long sysTimeEnd = System.currentTimeMillis() - sysTimeBegin;
				String CSVFileName = db.getName() + "_comma.txt";
				fId = generateFileService.createFile(CSVFileName, generalFileDir, answer.toString(), db, sysTimeEnd,
						ExportFormatBean.CSVFILE, true);
				logger.info("just created csv file");
				request.setAttribute("generate", generalFileDir + CSVFileName);
			} else if ("excel".equalsIgnoreCase(action)) {
				String excelFileName = db.getName() + "_excel.xls";

				finalTarget = Page.GENERATE_EXCEL_DATASET;

				response.setHeader("Content-Disposition", "attachment; filename=" + db.getName() + "_excel.xls");
				request.setAttribute("generate", generalFileDir + excelFileName);
				logger.info("set 'generate' to :" + generalFileDir + excelFileName);
			}

			if (!finalTarget.equals(Page.GENERATE_EXCEL_DATASET) && !finalTarget.equals(Page.GENERATE_DATASET_HTML)) {

				finalTarget.setFileName("" + "/WEB-INF/jsp/extract/generateMetadataCore.jsp");
				// also set up table here???
				asdfdao = new ArchivedDatasetFileDAO(sm.getDataSource());

				ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) asdfdao.findByPK(fId);
				ArrayList newFileList = new ArrayList();
				newFileList.add(asdfBean);

				ArrayList filterRows = ArchivedDatasetFileRow.generateRowsFromBeans(newFileList);
				EntityBeanTable table = fp.getEntityBeanTable();
				table.setSortingIfNotExplicitlySet(3, false);// sort by date
				String[] columns = { resword.getString("file_name"), resword.getString("run_time"),
						resword.getString("file_size"), resword.getString("created_date"),
						resword.getString("created_by") };

				table.setColumns(new ArrayList(Arrays.asList(columns)));
				table.hideColumnLink(0);
				table.hideColumnLink(1);
				table.hideColumnLink(2);
				table.hideColumnLink(3);
				table.hideColumnLink(4);

				request.setAttribute("dataset", db);
				request.setAttribute("file", asdfBean);
				table.setRows(filterRows);

				table.computeDisplay();

				request.setAttribute("table", table);
			}
			logger.info("set first part of 'generate' to :" + generalFileDir);
			logger.info("found file name: " + finalTarget.getFileName());

			forwardPage(finalTarget);
		}
	}

	@Override
	public void mayProceed() throws InsufficientPermissionException {
		if (ub.isSysAdmin()) {
			return;
		}
		if (currentRole.getRole().equals(Role.STUDY_DIRECTOR) || currentRole.getRole().equals(Role.STUDY_ADMINISTRATOR)
				|| currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.STUDY_MONITOR)) {
			return;
		}

		addPageMessage(respage.getString("no_have_correct_privilege_current_study")
				+ respage.getString("change_study_contact_sysadmin"));
		throw new InsufficientPermissionException(Page.MENU,
				resexception.getString("not_allowed_access_extract_data_servlet"), "1");

	}

	public ArchivedDatasetFileBean generateFileBean(File datasetFile, String relativePath, int formatId) {
		ArchivedDatasetFileBean adfb = new ArchivedDatasetFileBean();
		adfb.setName(datasetFile.getName());
		if (datasetFile.canRead()) {
			logger.info("File can be read");
		} else {
			logger.info("File CANNOT be read");
		}
		logger.info("Found file length: " + datasetFile.length());
		logger.info("Last Modified: " + datasetFile.lastModified());
		adfb.setFileSize(new Long(datasetFile.length()).intValue());
		adfb.setExportFormatId(formatId);
		adfb.setWebPath(relativePath);
		adfb.setDateCreated(new java.util.Date(datasetFile.lastModified()));
		return adfb;
	}

	private void openZipFile(String fileName) {
		try {
			ZipFile zipFile = new ZipFile(fileName);

			java.util.Enumeration entries = zipFile.entries();

			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					// Assume directories are stored parents first then
					// children.
					System.out.println("Extracting directory: " + entry.getName());
					// This is not robust, just for demonstration purposes.
					(new File(entry.getName())).mkdir();
					// no dirs necessary?
					continue;
				}

				System.out.println("Extracting file: " + entry.getName());
				copyInputStream(zipFile.getInputStream(entry), new java.io.BufferedOutputStream(
						new java.io.FileOutputStream(entry.getName())));
			}

			zipFile.close();
		} catch (java.io.IOException ioe) {
			System.err.println("Unhandled exception:");
			ioe.printStackTrace();
			return;
		}
	}

	public void loadList(DatasetBean db, ArchivedDatasetFileDAO asdfdao, int datasetId, FormProcessor fp, ExtractBean eb) {
		logger.info("action is blank");
		request.setAttribute("dataset", db);
		logger.info("just set dataset to request");
		request.setAttribute("extractProperties", CoreResources.getExtractProperties());
		ArrayList fileListRaw = new ArrayList();
		fileListRaw = asdfdao.findByDatasetId(datasetId);
		fileList = new ArrayList();
		Iterator fileIterator = fileListRaw.iterator();
		while (fileIterator.hasNext()) {

			ArchivedDatasetFileBean asdfBean = (ArchivedDatasetFileBean) fileIterator.next();
			asdfBean.setWebPath(asdfBean.getFileReference());

			if (new File(asdfBean.getFileReference()).isFile()) {
				fileList.add(asdfBean);
			} else {
				logger.warn(asdfBean.getFileReference() + " is NOT a file!");
			}
		}

		logger.warn("");
		logger.warn("file list length: " + fileList.size());
		request.setAttribute("filelist", fileList);

		ArrayList filterRows = ArchivedDatasetFileRow.generateRowsFromBeans(fileList);
		EntityBeanTable table = fp.getEntityBeanTable();
		table.setSortingIfNotExplicitlySet(3, false);// sort by date
		String[] columns = { resword.getString("file_name"), resword.getString("run_time"),
				resword.getString("file_size"), resword.getString("created_date"), resword.getString("created_by"),
				resword.getString("action") };
		table.setColumns(new ArrayList(Arrays.asList(columns)));
		table.hideColumnLink(0);
		table.hideColumnLink(1);
		table.hideColumnLink(2);
		table.hideColumnLink(3);
		table.hideColumnLink(4);
		table.hideColumnLink(5);

		table.setQuery("ExportDataset?datasetId=" + db.getId(), new HashMap());
		// trying to continue...
		session.setAttribute("newDataset", db);
		table.setRows(filterRows);
		table.computeDisplay();

		request.setAttribute("table", table);
		resetPanel();
		panel.setStudyInfoShown(false);
		setToPanel(resword.getString("study_name"), eb.getStudy().getName());
		setToPanel(resword.getString("protocol_ID"), eb.getStudy().getIdentifier());
		setToPanel(resword.getString("dataset_name"), db.getName());
		setToPanel(resword.getString("created_date"), local_df.format(db.getCreatedDate()));
		setToPanel(resword.getString("dataset_owner"), db.getOwner().getName());
		try {
			// do we not set this or is it null b/c we come to the page with no
			// session?
			setToPanel(resword.getString("date_last_run"), local_df.format(db.getDateLastRun()));
		} catch (NullPointerException npe) {
			System.out.println("exception: " + npe.getMessage());
		}

		logger.warn("just set file list to request, sending to page");

	}

	private StdScheduler getScheduler() {
		scheduler = this.scheduler != null ? scheduler : (StdScheduler) SpringServletAccess.getApplicationContext(
				context).getBean(SCHEDULER);
		return scheduler;
	}

	private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;

		while ((len = in.read(buffer)) > 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}
}
