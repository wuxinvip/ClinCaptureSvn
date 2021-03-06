/*******************************************************************************
 * Copyright (C) 2009-2013 Clinovo Inc.
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

package org.akaza.openclinica.job;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractPropertyBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.service.extract.ExtractUtils;
import org.akaza.openclinica.service.extract.XsltTriggerService;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Converts all old jobs created under DEFAULT group, to make it use new XSLT transformation code.
 *
 */
public class LegacyJobConverterJob extends QuartzJobBean {
	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	public static final String USER_ID = "user_id";
	public static final String DATASET_ID = "dsId";
	public static final String EMAIL = "contactEmail";
	public static final String JOB_NAME = "jobName";
	public static final String JOB_DESC = "jobDesc";
	public static final String PERIOD = "periodToRun";

	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try {
			// Pulling all the trigger under the DEFAULT group
			Scheduler scheduler = context.getScheduler();
			String triggerGroup = "DEFAULT";
			Set<TriggerKey> legacyTriggers = scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(triggerGroup));
			ExtractUtils extractUtils = new ExtractUtils();
			if (legacyTriggers == null || legacyTriggers.size() == 0) {
				logger.info("No legacy jobs to convert");
				return;
			}

			DataSource dataSource = (DataSource) getApplicationContext(context).getBean("dataSource");
			CoreResources coreResources = (CoreResources) getApplicationContext(context).getBean("coreResources");

			for (TriggerKey triggerKey : legacyTriggers) {
				Trigger trigger = scheduler.getTrigger(triggerKey);
				JobDataMap dataMap = trigger.getJobDataMap();

				DatasetDAO datasetDao = new DatasetDAO(dataSource);
				int datasetId = dataMap.getInt(DATASET_ID);
				int userId = dataMap.getInt(USER_ID);
				String period = dataMap.getString(PERIOD);
				String email = dataMap.getString(EMAIL);
				String jobName = dataMap.getString(JOB_NAME);
				if (jobName == null || jobName.length() == 0) {
					jobName = "LegacyJob";
				}
				String jobDesc = dataMap.getString(JOB_DESC);

				UserAccountBean userBean = new UserAccountBean();
				userBean.setId(userId);
				Map<String, Integer> exportingFormats = getExportingformats(dataMap);

				for (String exportFormat : exportingFormats.keySet()) {
					Integer exportFormatId = exportingFormats.get(exportFormat);
					ExtractPropertyBean epBean = coreResources.findExtractPropertyBeanById(exportFormatId, ""
							+ datasetId);
					DatasetBean dsBean = (DatasetBean) datasetDao.findByPK(new Integer(datasetId).intValue());
					String[] files = epBean.getFileName();

					String exportFileName;
					int cnt = 0;
					dsBean.setName(dsBean.getName().replaceAll(" ", "_"));
					String[] exportFiles = epBean.getExportFileName();
					String pattern = "yyyy" + File.separator + "MM" + File.separator + "dd" + File.separator
							+ "HHmmssSSS" + File.separator;
					SimpleDateFormat sdfDir = new SimpleDateFormat(pattern);
					int i = 0;
					String[] temp = new String[exportFiles.length];
					String datasetFilePath = getFilePath(context) + "datasets";

					while (i < exportFiles.length) {
						temp[i] = extractUtils.resolveVars(exportFiles[i], dsBean, sdfDir, datasetFilePath);
						i++;
					}
					epBean.setDoNotDelFiles(temp);
					epBean.setExportFileName(temp);

					exportFileName = epBean.getExportFileName()[cnt];

					String xsltPath = getFilePath(context) + "xslt" + File.separator + files[0];
					String endFilePath = epBean.getFileLocation();
					endFilePath = extractUtils.getEndFilePath(endFilePath, dsBean, sdfDir, datasetFilePath);

					if (epBean.getPostProcExportName() != null) {
						String preProcExportPathName = extractUtils.resolveVars(epBean.getPostProcExportName(), dsBean,
								sdfDir, datasetFilePath);
						epBean.setPostProcExportName(preProcExportPathName);
					}
					if (epBean.getPostProcLocation() != null) {
						String prePocLoc = extractUtils.getEndFilePath(epBean.getPostProcLocation(), dsBean, sdfDir,
								datasetFilePath);
						epBean.setPostProcLocation(prePocLoc);
					}

					StudyBean studyBean = (StudyBean) new StudyDAO(dataSource).findByPK(userBean.getActiveStudyId());
					if (studyBean.getParentStudyId() > 0) {
						studyBean = (StudyBean) new StudyDAO(dataSource).findByPK(studyBean.getParentStudyId());
					}
					extractUtils.setAllProps(epBean, dsBean, sdfDir, datasetFilePath);
					XsltTriggerService xsltService = new XsltTriggerService();
					SimpleTriggerImpl newTrigger;
					newTrigger = xsltService.generateXsltTrigger(xsltPath, studyBean, endFilePath + File.separator,
							exportFileName, dsBean.getId(), epBean, userBean, Locale.US.toString(), cnt,
							getFilePath(context) + "xslt", XsltTriggerService.TRIGGER_GROUP_NAME);
					// Updating the original trigger with user given inputs
					newTrigger.setRepeatCount(64000);
					newTrigger.setRepeatInterval(XsltTriggerService.getIntervalTime(period));
					newTrigger.setDescription(jobDesc);
					// set just the start date
					newTrigger.setName(jobName + "-" + exportFormat);
					newTrigger
							.setMisfireInstruction(SimpleTriggerImpl.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT);
					newTrigger.getJobDataMap().put(XsltTriggerService.EMAIL, email);
					newTrigger.getJobDataMap().put(XsltTriggerService.PERIOD, period);
					newTrigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT, epBean.getFiledescription());
					newTrigger.getJobDataMap().put(XsltTriggerService.EXPORT_FORMAT_ID, exportFormatId);
					newTrigger.getJobDataMap().put(XsltTriggerService.JOB_NAME, jobName);

					JobDetailImpl jobDetailBean = new JobDetailImpl();
					jobDetailBean.setGroup(XsltTriggerService.TRIGGER_GROUP_NAME);
					jobDetailBean.setName(newTrigger.getName());
					jobDetailBean.setJobClass(XsltStatefulJob.class);
					jobDetailBean.setJobDataMap(newTrigger.getJobDataMap());
					jobDetailBean.setDurability(true); // need durability?
					// jobDetailBean.setVolatility(false);

					scheduler.deleteJob(trigger.getJobKey());
					Date dataStart = scheduler.scheduleJob(jobDetailBean, newTrigger);
					logger.info("Old job[" + trigger.getKey().getName() + "] has been converted to ["
							+ newTrigger.getName() + "] with a dataStart of " + dataStart.toString());
				}
			}

		} catch (SchedulerException e) {
			logger.error("Error converting legacy triggers");
			e.printStackTrace();
		}
	}

	private Map<String, Integer> getExportingformats(JobDataMap dataMap) {
		Map<String, Integer> formats = new HashMap<String, Integer>();
		String spss = dataMap.getString("spss");
		if (spss != null && spss.length() > 0) {
			formats.put("spss", CoreResources.SPSS_ID);
		}
		String tab = dataMap.getString("tab");
		if (tab != null && tab.length() > 0) {
			formats.put("tab", CoreResources.TAB_ID);
		}
		String cdisc = dataMap.getString("cdisc");
		if (cdisc != null && cdisc.length() > 0) {
			formats.put("cdisc", CoreResources.CDISC_ODM_1_2_ID);
		}
		String cdisc12 = dataMap.getString("cdisc12");
		if (cdisc12 != null && cdisc12.length() > 0) {
			formats.put("cdisc12", CoreResources.CDISC_ODM_1_2_EXTENSION_ID);
		}
		String cdisc13 = dataMap.getString("cdisc13");
		if (cdisc13 != null && cdisc13.length() > 0) {
			formats.put("cdisc13", CoreResources.CDISC_ODM_1_3_ID);
		}
		String cdisc13oc = dataMap.getString("cdisc13oc");
		if (cdisc13oc != null && cdisc13oc.length() > 0) {
			formats.put("cdisc13oc", CoreResources.CDISC_ODM_1_3_EXTENSION_ID);
		}

		return formats;
	}

	private ApplicationContext getApplicationContext(JobExecutionContext context) throws SchedulerException {
		return (ApplicationContext) context.getScheduler().getContext().get("applicationContext");
	}

	private String getFilePath(JobExecutionContext context) throws SchedulerException {
		Properties params = (Properties) getApplicationContext(context).getBean("dataInfo");
		String filePath = params.getProperty("filePath");
		if (filePath != null) {
			filePath = filePath.trim();
		}

		return filePath == null ? "" : filePath;
	}
}
