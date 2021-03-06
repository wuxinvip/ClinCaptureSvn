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

package org.akaza.openclinica.control.admin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clinovo.util.DateUtil;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SpringServlet;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.service.extract.ExtractUtils;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.stereotype.Component;

import com.clinovo.i18n.LocaleResolver;
import com.clinovo.util.ValidatorHelper;

/**
 * 
 * @author thickerson
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class CreateJobExportServlet extends SpringServlet {

	public static final String PERIOD = "periodToRun";
	public static final String FORMAT_ID = "formatId";
	public static final String DATASET_ID = "dsId";
	public static final String DATE_START_JOB = "job";
	public static final String EMAIL = "contactEmail";
	public static final String JOB_NAME = "jobName";
	public static final String JOB_DESC = "jobDesc";
	public static final String DATASETS = "datasets";

	@Override
	protected void mayProceed(HttpServletRequest request, HttpServletResponse response)
			throws InsufficientPermissionException {
		UserAccountBean ub = getUserAccountBean(request);

		if (ub.isSysAdmin() || ub.isTechAdmin()) {
			return;
		}

		addPageMessage(
				getResPage().getString("no_have_correct_privilege_current_study")
						+ getResPage().getString("change_study_contact_sysadmin"), request);
		throw new InsufficientPermissionException(Page.MENU_SERVLET,
				getResException().getString("not_allowed_access_extract_data_servlet"), "1");// TODO
		// above copied from create dataset servlet, needs to be changed to
		// allow only admin-level users

	}

	private void setUpServlet(HttpServletRequest request) {
		// TODO find all the form items and re-populate them if necessary
		FormProcessor fp2 = new FormProcessor(request);
		DatasetDAO dsdao = getDatasetDAO();
		Collection dsList = dsdao.findAllOrderByStudyIdAndName();
		// TODO will have to dress this up to allow for sites then datasets
		request.setAttribute(DATASETS, dsList);
		request.setAttribute(JOB_NAME, fp2.getString(JOB_NAME));
		request.setAttribute(JOB_DESC, fp2.getString(JOB_DESC));
		request.setAttribute("extractProperties", CoreResources.getExtractProperties());
		request.setAttribute(EMAIL, fp2.getString(EMAIL));
		request.setAttribute(FORMAT_ID, fp2.getInt(FORMAT_ID));
		request.setAttribute(PERIOD, fp2.getString(PERIOD));
		request.setAttribute(DATASET_ID, fp2.getInt(DATASET_ID));
		// to back local time use expression fp2.getDateTime(DATE_START_JOB)
		DateTimeZone userTimeZone = DateTimeZone.forID(getUserAccountBean().getUserTimeZoneId());
		DateTime serverJobDate = new DateTime().plusMinutes(15);
		DateTime localJobDate = serverJobDate.withZone(userTimeZone);
		HashMap presetValues = new HashMap();
		presetValues.put(DATE_START_JOB + "Hour", localJobDate.getHourOfDay());
		presetValues.put(DATE_START_JOB + "Minute", localJobDate.getMinuteOfHour());
		presetValues.put(DATE_START_JOB + "Date", DateUtil.printDate(serverJobDate.toDate(), userTimeZone.getID(),
				DateUtil.DatePattern.DATE, getLocale()));
		fp2.setPresetValues(presetValues);
		setPresetValues(fp2.getPresetValues(), request);
	}

	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// TODO multi stage servlet which will create export jobs
		// will accept, create, and return the ViewJob servlet
		FormProcessor fp = new FormProcessor(request);

		String action = fp.getString("action");
		ExtractUtils extractUtils = new ExtractUtils();
		if (StringUtil.isBlank(action)) {
			// set up list of data sets
			// select by ... active study
			setUpServlet(request);
			forwardPage(Page.CREATE_JOB_EXPORT, request, response);
		} else if ("confirmall".equalsIgnoreCase(action)) {
			// collect form information
			HashMap errors = validateForm(fp, request,
					getStdScheduler().getTriggerKeys(GroupMatcher.triggerGroupEquals("DEFAULT")), "");

			if (!errors.isEmpty()) {
				// set errors to request
				request.setAttribute("formMessages", errors);
				logger.info("errors found: " + errors.toString());
				setUpServlet(request);
				forwardPage(Page.CREATE_JOB_EXPORT, request, response);
			} else {
				logger.info("found no validation errors, continuing");

				DatasetDAO datasetDao = getDatasetDAO();

				UserAccountBean userBean = (UserAccountBean) request.getSession().getAttribute("userBean");
				CoreResources cr = new CoreResources();
				int datasetId = fp.getInt(DATASET_ID);
				if (datasetId == 0) {
					addPageMessage(getResException().getString("please_choose_a_dataset"), request);
					forwardPage(Page.CREATE_JOB_EXPORT, request, response);
					return;
				}
				String period = fp.getString(PERIOD);
				String email = fp.getString(EMAIL);
				String jobName = fp.getString(JOB_NAME);
				String jobDesc = fp.getString(JOB_DESC);
				Date startDateTime = fp.getDateTimeInput(DATE_START_JOB);
				Integer exportFormatId = fp.getInt(FORMAT_ID);

				ExtractPropertyBean epBean = cr.findExtractPropertyBeanById(exportFormatId, "" + datasetId);
				DatasetBean dsBean = (DatasetBean) datasetDao.findByPK(new Integer(datasetId).intValue());

				// set the job in motion
				String[] files = epBean.getFileName();
				String exportFileName;
				int cnt = 0;
				dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
				String[] exportFiles = epBean.getExportFileName();
				String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator + "HHmmssSSS"
						+ File.separator;
				SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
				int i = 0;
				String[] temp = new String[exportFiles.length];
				// JN: The following logic is for comma separated variables, to
				// avoid the second file be treated as a
				// old file and deleted.
				String datasetFilePath = SQLInitServlet.getField("filePath") + DATASETS;

				while (i < exportFiles.length) {
					temp[i] = extractUtils.resolveVars(exportFiles[i], dsBean, sdfDir, datasetFilePath);
					i++;
				}
				epBean.setDoNotDelFiles(temp);
				epBean.setExportFileName(temp);

				XsltTriggerService xsltService = new XsltTriggerService();

				exportFileName = epBean.getExportFileName()[cnt];

				// need to set the dataset path here, tbh
				// next, can already run jobs, translations, and then add a
				// message to be notified later
				// JN all the properties need to have the variables...
				String xsltPath = SQLInitServlet.getField("filePath") + "xslt" + File.separator + files[cnt];
				String endFilePath = epBean.getFileLocation();
				endFilePath = extractUtils.getEndFilePath(endFilePath, dsBean, sdfDir, datasetFilePath);
				// exportFileName = resolveVars(exportFileName,dsBean,sdfDir);
				if (epBean.getPostProcExportName() != null) {
					// String preProcExportPathName =
					// getEndFilePath(epBean.getPostProcExportName(),dsBean,sdfDir);
					String preProcExportPathName = extractUtils.resolveVars(epBean.getPostProcExportName(), dsBean,
							sdfDir, datasetFilePath);
					epBean.setPostProcExportName(preProcExportPathName);
				}
				if (epBean.getPostProcLocation() != null) {
					String prePocLoc = extractUtils.getEndFilePath(epBean.getPostProcLocation(), dsBean, sdfDir,
							datasetFilePath);
					epBean.setPostProcLocation(prePocLoc);
				}
				extractUtils.setAllProps(epBean, dsBean, sdfDir, datasetFilePath);
				SimpleTriggerImpl trigger;

				trigger = xsltService.generateXsltTrigger(xsltPath, getParentStudy(), endFilePath + File.separator,
						exportFileName, dsBean.getId(), epBean, userBean, LocaleResolver.getLocale(request).toString(),
						cnt, SQLInitServlet.getField("filePath") + "xslt", XsltTriggerService.TRIGGER_GROUP_NAME);

				trigger.setName(jobName);
				trigger.setJobName(jobName);
				// Updating the original trigger with user given inputs
				trigger.setRepeatCount(64000);
				trigger.setRepeatInterval(XsltTriggerService.getIntervalTime(period));
				trigger.setDescription(jobDesc);
				// set just the start date

				trigger.setStartTime(startDateTime);
				trigger.setMisfireInstruction(SimpleTriggerImpl.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT);
				trigger.getJobDataMap().put(XsltTriggerService.EMAIL, email);
				trigger.getJobDataMap().put(XsltTriggerService.PERIOD, period);
				trigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT, epBean.getFiledescription());
				trigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT_ID, exportFormatId);
				trigger.getJobDataMap().put(XsltTriggerService.JOB_NAME, jobName);
				trigger.getJobDataMap().put("job_type", "exportJob");

				JobDetailImpl jobDetailBean = new JobDetailImpl();
				jobDetailBean.setGroup(XsltTriggerService.TRIGGER_GROUP_NAME);
				jobDetailBean.setName(trigger.getName());
				jobDetailBean.setJobClass(org.akaza.openclinica.job.XsltStatefulJob.class);
				jobDetailBean.setJobDataMap(trigger.getJobDataMap());
				jobDetailBean.setDurability(true); // need durability?
				// jobDetailBean.setVolatility(false);

				// set to the scheduler
				try {
					Date dateStart = getStdScheduler().scheduleJob(jobDetailBean, trigger);
					logger.info("== found job date: " + dateStart.toString());
					// set a success message here
				} catch (SchedulerException se) {
					se.printStackTrace();
					setUpServlet(request);
					addPageMessage(getResPage().getString("error_creating_job"), request);
					forwardPage(Page.VIEW_JOB_SERVLET, request, response);
					return;
				}
				setUpServlet(request);
				addPageMessage(getResPage().getString("you_have_successfully_created_a_new_job") + " " + jobName + " "
						+ getResPage().getString("which_is_now_set_to_run"), request);
				forwardPage(Page.VIEW_JOB_SERVLET, request, response);
			}
		} else {
			forwardPage(Page.ADMIN_SYSTEM, request, response);
			// forward to form
			// should we even get to this part?
		}
	}

	public HashMap validateForm(FormProcessor fp, HttpServletRequest request, Set<TriggerKey> triggerKeys,
			String properName) {
		Validator v = new Validator(new ValidatorHelper(request, getConfigurationDao()));
		v.addValidation(DATASET_ID, Validator.NO_BLANKS_SET);
		v.addValidation(JOB_NAME, Validator.NO_BLANKS);

		// need to be unique too
		v.addValidation(JOB_DESC, Validator.NO_BLANKS);
		v.addValidation(EMAIL, Validator.IS_A_EMAIL);
		v.addValidation(PERIOD, Validator.NO_BLANKS);
		v.addValidation(DATE_START_JOB + "Date", Validator.IS_A_DATE);

		int formatId = fp.getInt(FORMAT_ID);
		HashMap errors = v.validate();
		if (formatId == 0) {
			// throw an error here, at least one should work
			// errors.put(TAB, "Error Message - Pick one of the below");
			Validator.addError(errors, FORMAT_ID, getResPage().getString("please_pick_at_least_one"));
		}
		for (TriggerKey triggerKey : triggerKeys) {
			if (triggerKey.getName().equals(fp.getString(JOB_NAME)) && (!triggerKey.getName().equals(properName))) {
				Validator.addError(errors, JOB_NAME,
						getResPage().getString("a_job_with_that_name_already_exist_please_pick"));
			}
		}
		try {
			Date jobDate = fp.getDateTimeInput(DATE_START_JOB);
			if (jobDate.before(new Date())) {
				Validator.addError(errors, DATE_START_JOB + "Date", getResPage().getString("this_date_needs_to_be_later"));
			}
		} catch (IllegalArgumentException ex) {
			//
		}
		String jobDesc = fp.getString(JOB_DESC);
		if ((null != jobDesc) && (!jobDesc.equals(""))) {
			if (jobDesc.length() > 250) {
				Validator.addError(errors, JOB_DESC, getResPage().getString("a_job_description_cannot_be_more_than"));
			}
		}
		Matcher matcher = Pattern.compile("[^\\w_\\d ]").matcher(fp.getString(JOB_NAME));
		boolean isContainSpecialSymbol = matcher.find();
		if (isContainSpecialSymbol) {
			Validator.addError(errors, JOB_NAME, getResException().getString("dataset_should_not_contain_any_special"));
		}
		return errors;
	}
}
