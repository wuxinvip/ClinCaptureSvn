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
package org.akaza.openclinica.bean.extract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DatasetItemStatus;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DatasetBean.
 */
@SuppressWarnings({"rawtypes", "serial"})
public class DatasetBean extends AuditableEntityBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasetBean.class);
	public static final int FIRST_YEAR_DEFAULT_VALUE = 1900;
	public static final int LAST_YEAR_DEFAULT_VALUE = 2100;

	private int studyId;
	private StudyBean studyBean;
	private String description;
	private String sqlStatement;
	private int numRuns = 0;
	private int runTime = 0;
	private java.util.Date dateLastRun;
	private java.util.Date dateStart;
	private java.util.Date dateEnd;
	private int approverId = 0;
	private ArrayList eventIds = new ArrayList();
	private ArrayList itemIds = new ArrayList();
	private ArrayList subjectGroupIds = new ArrayList();
	private HashMap itemMap = new HashMap();

	private boolean showEventLocation = false;
	private boolean showEventStart = false;
	private boolean showEventEnd = false;
	private boolean showSubjectDob = false;
	private boolean showSubjectGender = false;
	private boolean showSubjectStatus = false;
	private boolean showSubjectUniqueIdentifier = false;
	private boolean showSubjectAgeAtEvent = false;
	private boolean showSubjectSecondaryId = false;

	private boolean showEventStatus = false;
	// tbh
	private boolean showEventStartTime = false;
	private boolean showEventEndTime = false;
	// how is this different than Start/End? not adding the two above for now,
	// tbh

	private boolean showCRFstatus = false;
	private boolean showCRFversion = false;
	private boolean showCRFinterviewerName = false;
	private boolean showCRFinterviewerDate = false;
	private boolean showCRFcompletionDate = false;
	// again, how is it different from Start/End?
	private boolean showSubjectGroupInformation = false;
	private boolean containsMaskedCRFs = false;

	private ArrayList itemDefCrf = new ArrayList();
	// map items with definition and CRF

	private static final String VIEW_NAME = "extract_data_table";
	// put up here since we know it's going to be changed, tbh

	private String odmMetaDataVersionName;
	private String odmMetaDataVersionOid;
	private String odmPriorStudyOid;
	private String odmPriorMetaDataVersionOid;
	private DatasetItemStatus datasetItemStatus;

	private int firstMonth = 0;
	private int firstYear = FIRST_YEAR_DEFAULT_VALUE;
	private int lastMonth = 0;
	private int lastYear = LAST_YEAR_DEFAULT_VALUE;

	private ArrayList allSelectedGroups;

	private String excludeItems;
	private String sedIdAndCRFIdPairs;

	/**
	 * Constructor.
	 */
	public DatasetBean() {
	}

	/**
	 * @return Returns the dateLastRun.
	 */
	public java.util.Date getDateLastRun() {
		return dateLastRun;
	}

	/**
	 * @param dateLastRun
	 *            The dateLastRun to set.
	 */
	public void setDateLastRun(java.util.Date dateLastRun) {
		this.dateLastRun = dateLastRun;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the numRuns.
	 */
	public int getNumRuns() {
		return numRuns;
	}

	/**
	 * @param numRuns
	 *            The numRuns to set.
	 */
	public void setNumRuns(int numRuns) {
		this.numRuns = numRuns;
	}

	/**
	 * @return Returns the runTime.
	 */
	public int getRunTime() {
		return runTime;
	}

	/**
	 * @param runTime
	 *            The runTime to set.
	 */
	public void setRunTime(int runTime) {
		this.runTime = runTime;
	}

	/**
	 * @return Returns the sQLStatement.
	 */
	public String getSQLStatement() {
		return sqlStatement;
	}

	/**
	 * @param statement
	 *            The sQLStatement to set.
	 */
	public void setSQLStatement(String statement) {
		sqlStatement = statement;
	}

	/**
	 * @return Returns the studyId.
	 */
	public int getStudyId() {
		return studyId;
	}

	/**
	 * @param studyId
	 *            The studyId to set.
	 */
	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return Returns the approverId.
	 */
	public int getApproverId() {
		return approverId;
	}

	/**
	 * @param approverId
	 *            The approverId to set.
	 */
	public void setApproverId(int approverId) {
		this.approverId = approverId;
	}

	/**
	 * @return Returns the dateEnd.
	 */
	public java.util.Date getDateEnd() {
		return dateEnd;
	}

	/**
	 * @param dateEnd
	 *            The dateEnd to set.
	 */
	public void setDateEnd(java.util.Date dateEnd) {
		this.dateEnd = dateEnd;
	}

	/**
	 * @return Returns the dateStart.
	 */
	public java.util.Date getDateStart() {
		return dateStart;
	}

	/**
	 * @param dateStart
	 *            The dateStart to set.
	 */
	public void setDateStart(java.util.Date dateStart) {
		this.dateStart = dateStart;
	}

	/**
	 * takes the dataset bean information and generates a query; this will changes if the database changes. This will
	 * also change when we apply filters.
	 * 
	 * @return string in SQL, to elicit information.
	 */
	public String generateQuery() {
		StringBuffer sb = new StringBuffer();
		sb.append("select distinct * from " + VIEW_NAME + " where ");

		if (!this.getEventIds().isEmpty()) {
			String idList = this.getEventIds().toString();
			sb.append("study_event_definition_id in (" + idList + ") and ");
		}
		if (!this.getItemIds().isEmpty()) {
			String idList = this.getItemIds().toString();
			sb.append("item_id in (" + idList + ") and ");
		}

		String pattern = "yyyy-MM-dd";
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String beginDate = "1900-01-01";
		if (dateStart != null) {
			beginDate = sdf.format(this.dateStart);
		}
		String stopDate = "2100-01-01";
		if (dateEnd != null) {
			stopDate = sdf.format(this.dateEnd);
		}
		sb.append("(date(date_created) >= date('" + beginDate + "')) and (date(date_created) <= date('" + stopDate
				+ "'))");

		String returnMe = sb.toString().replaceAll("\\[|\\]", "");
		returnMe = returnMe + " order by date_start asc";
		return returnMe;
	}

	/**
	 * generateOracleQuery, generates the Oracle syntax for the query (this may have to be changed to reflect different
	 * syntaxes in the future).
	 * 
	 * @return the Oracle SQL syntax to capture datasets.
	 */
	public String generateOracleQuery() {
		StringBuffer sb = new StringBuffer();
		sb.append("select distinct * from " + VIEW_NAME + " where ");
		if (!this.getEventIds().isEmpty()) {
			String idList = this.getEventIds().toString();
			sb.append("study_event_definition_id in (" + idList + ") and ");
		}

		if (!this.getItemIds().isEmpty()) {
			String idList = this.getItemIds().toString();
			sb.append("item_id in (" + idList + ") and ");
		}
		String pattern = "dd-MMM-yyyy"; // changed by bads issue 2152
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		String beginDate = sdf.format(this.dateStart);
		String stopDate = sdf.format(this.dateEnd);

		sb.append("(date_created >= '" + beginDate + "') and (date_created <= '" + stopDate + "')");
		// perform regexp here that pulls out [] square brackets

		LOGGER.info("-----------------------------");
		LOGGER.info(sb.toString());
		LOGGER.info("-----------------------------");
		String returnMe = sb.toString().replaceAll("\\[|\\]", "");
		returnMe = returnMe + " order by date_start";
		return returnMe;
	}

	/**
	 * @return Returns the itemIds.
	 */
	public ArrayList getItemIds() {
		return itemIds;
	}

	/**
	 * @param itemIds
	 *            The itemIds to set.
	 */
	public void setItemIds(ArrayList itemIds) {
		this.itemIds = itemIds;
	}

	/**
	 * @return Returns the itemMap.
	 */
	public HashMap getItemMap() {
		return itemMap;
	}

	/**
	 * @param itemMap
	 *            The itemMap to set.
	 */
	public void setItemMap(HashMap itemMap) {
		this.itemMap = itemMap;
	}

	/**
	 * @return Returns the eventIds.
	 */
	public ArrayList getEventIds() {
		return eventIds;
	}

	/**
	 * @param eventIds
	 *            The eventIds to set.
	 */
	public void setEventIds(ArrayList eventIds) {
		this.eventIds = eventIds;
	}

	/**
	 * @return Returns the showEventEnd.
	 */
	public boolean isShowEventEnd() {
		return showEventEnd;
	}

	/**
	 * @param showEventEnd
	 *            The showEventEnd to set.
	 */
	public void setShowEventEnd(boolean showEventEnd) {
		this.showEventEnd = showEventEnd;
	}

	/**
	 * @return Returns the showEventLocation.
	 */
	public boolean isShowEventLocation() {
		return showEventLocation;
	}

	/**
	 * @param showEventLocation
	 *            The showEventLocation to set.
	 */
	public void setShowEventLocation(boolean showEventLocation) {
		this.showEventLocation = showEventLocation;
	}

	/**
	 * @return Returns the showEventStart.
	 */
	public boolean isShowEventStart() {
		return showEventStart;
	}

	/**
	 * @param showEventStart
	 *            The showEventStart to set.
	 */
	public void setShowEventStart(boolean showEventStart) {
		this.showEventStart = showEventStart;
	}

	/**
	 * @return Returns the showSubjectDob.
	 */
	public boolean isShowSubjectDob() {
		return showSubjectDob;
	}

	/**
	 * @param showSubjectDob
	 *            The showSubjectDob to set.
	 */
	public void setShowSubjectDob(boolean showSubjectDob) {
		this.showSubjectDob = showSubjectDob;
	}

	/**
	 * @return Returns the showSubjectGender.
	 */
	public boolean isShowSubjectGender() {
		return showSubjectGender;
	}

	/**
	 * @param showSubjectGender
	 *            The showSubjectGender to set.
	 */
	public void setShowSubjectGender(boolean showSubjectGender) {
		this.showSubjectGender = showSubjectGender;
	}

	/**
	 * @return Returns the itemDefCrf.
	 */
	public ArrayList getItemDefCrf() {
		return itemDefCrf;
	}

	/**
	 * @param itemDefCrf
	 *            The itemDefCrf to set.
	 */
	public void setItemDefCrf(ArrayList itemDefCrf) {
		this.itemDefCrf = itemDefCrf;
	}

	public boolean isShowCRFcompletionDate() {
		return showCRFcompletionDate;
	}

	public void setShowCRFcompletionDate(boolean showCRFcompletionDate) {
		this.showCRFcompletionDate = showCRFcompletionDate;
	}

	public boolean isShowCRFinterviewerDate() {
		return showCRFinterviewerDate;
	}

	public void setShowCRFinterviewerDate(boolean showCRFinterviewerDate) {
		this.showCRFinterviewerDate = showCRFinterviewerDate;
	}

	public boolean isShowCRFinterviewerName() {
		return showCRFinterviewerName;
	}

	public void setShowCRFinterviewerName(boolean showCRFinteviewerName) {
		this.showCRFinterviewerName = showCRFinteviewerName;
	}

	public boolean isShowCRFstatus() {
		return showCRFstatus;
	}

	public void setShowCRFstatus(boolean showCRFstatus) {
		this.showCRFstatus = showCRFstatus;
	}

	public boolean isShowCRFversion() {
		return showCRFversion;
	}

	public void setShowCRFversion(boolean showCRFversion) {
		this.showCRFversion = showCRFversion;
	}

	public boolean isShowEventEndTime() {
		return showEventEndTime;
	}

	public void setShowEventEndTime(boolean showEventEndTime) {
		this.showEventEndTime = showEventEndTime;
	}

	public boolean isShowEventStartTime() {
		return showEventStartTime;
	}

	public void setShowEventStartTime(boolean showEventStartTime) {
		this.showEventStartTime = showEventStartTime;
	}

	public boolean isShowEventStatus() {
		return showEventStatus;
	}

	public void setShowEventStatus(boolean showEventStatus) {
		this.showEventStatus = showEventStatus;
	}

	public boolean isShowSubjectAgeAtEvent() {
		return showSubjectAgeAtEvent;
	}

	public void setShowSubjectAgeAtEvent(boolean showSubjectAgeAtEvent) {
		this.showSubjectAgeAtEvent = showSubjectAgeAtEvent;
	}

	public boolean isShowSubjectSecondaryId() {
		return showSubjectSecondaryId;
	}

	public void setShowSubjectSecondaryId(boolean showSubjectSecondaryId) {
		this.showSubjectSecondaryId = showSubjectSecondaryId;
	}

	public boolean isShowSubjectStatus() {
		return showSubjectStatus;
	}

	public void setShowSubjectStatus(boolean showSubjectStatus) {
		this.showSubjectStatus = showSubjectStatus;
	}

	public boolean isShowSubjectUniqueIdentifier() {
		return showSubjectUniqueIdentifier;
	}

	public void setShowSubjectUniqueIdentifier(boolean showUniqueIdentifier) {
		this.showSubjectUniqueIdentifier = showUniqueIdentifier;
	}

	public boolean isShowSubjectGroupInformation() {
		return showSubjectGroupInformation;
	}

	public void setShowSubjectGroupInformation(boolean showSubjectGroupInformation) {
		this.showSubjectGroupInformation = showSubjectGroupInformation;
	}

	public ArrayList getSubjectGroupIds() {
		return subjectGroupIds;
	}

	public void setSubjectGroupIds(ArrayList subjectGroupIds) {
		this.subjectGroupIds = subjectGroupIds;
	}

	public DatasetItemStatus getDatasetItemStatus() {
		return datasetItemStatus;
	}

	public void setDatasetItemStatus(DatasetItemStatus datasetItemStatus) {
		this.datasetItemStatus = datasetItemStatus;
	}

	/**
	 * Returns sql with uniqe item ids.
	 * 
	 * @param itemIdStr
	 *            String
	 * @return String
	 */
	public String sqlWithUniqeItemIds(String itemIdStr) {
		String sql = "";
		String[] s1 = this.sqlStatement.split("item_id in");
		sql += s1[0] + itemIdStr + s1[1].substring(s1[1].indexOf(")"));
		return sql;
	}

	public int getFirstMonth() {
		return firstMonth;
	}

	public void setFirstMonth(int firstMonth) {
		this.firstMonth = firstMonth;
	}

	public int getFirstYear() {
		return firstYear;
	}

	public void setFirstYear(int firstYear) {
		this.firstYear = firstYear;
	}

	public int getLastMonth() {
		return lastMonth;
	}

	public void setLastMonth(int lastMonth) {
		this.lastMonth = lastMonth;
	}

	public int getLastYear() {
		return lastYear;
	}

	public void setLastYear(int lastYear) {
		this.lastYear = lastYear;
	}

	public String getOdmMetaDataVersionOid() {
		return odmMetaDataVersionOid;
	}

	public void setOdmMetaDataVersionOid(String odmMetaDataVersionOid) {
		this.odmMetaDataVersionOid = odmMetaDataVersionOid;
	}

	public String getOdmMetaDataVersionName() {
		return odmMetaDataVersionName;
	}

	public void setOdmMetaDataVersionName(String odmMetaDataVersionName) {
		this.odmMetaDataVersionName = odmMetaDataVersionName;
	}

	public String getOdmPriorStudyOid() {
		return odmPriorStudyOid;
	}

	public void setOdmPriorStudyOid(String odmPriorStudyOid) {
		this.odmPriorStudyOid = odmPriorStudyOid;
	}

	public String getOdmPriorMetaDataVersionOid() {
		return odmPriorMetaDataVersionOid;
	}

	public void setOdmPriorMetaDataVersionOid(String odmPriorMetaDataVersionOid) {
		this.odmPriorMetaDataVersionOid = odmPriorMetaDataVersionOid;
	}

	public ArrayList getAllSelectedGroups() {
		return allSelectedGroups;
	}

	public void setAllSelectedGroups(ArrayList allSelectedGroups) {
		this.allSelectedGroups = allSelectedGroups;
	}

	public String getExcludeItems() {
		return excludeItems;
	}

	public void setExcludeItems(String excludeItems) {
		this.excludeItems = excludeItems;
	}

	public String getSedIdAndCRFIdPairs() {
		return sedIdAndCRFIdPairs;
	}

	public void setSedIdAndCRFIdPairs(String sedIdAndCRFIdPairs) {
		this.sedIdAndCRFIdPairs = sedIdAndCRFIdPairs;
	}

	public boolean isContainsMaskedCRFs() {
		return containsMaskedCRFs;
	}

	public void setContainsMaskedCRFs(boolean containsMaskedCRFs) {
		this.containsMaskedCRFs = containsMaskedCRFs;
	}

	public StudyBean getStudyBean() {
		return studyBean;
	}

	public void setStudyBean(StudyBean studyBean) {
		this.studyBean = studyBean;
	}
}
