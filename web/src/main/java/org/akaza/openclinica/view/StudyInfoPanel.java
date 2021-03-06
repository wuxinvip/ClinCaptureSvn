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
package org.akaza.openclinica.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.clinovo.util.DateUtil;
import com.clinovo.util.EventCRFUtil;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.extract.ExtractBean;
import org.akaza.openclinica.bean.extract.FilterBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DisplayEventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.DisplayStudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.control.core.SpringServlet;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;

import com.clinovo.i18n.LocaleResolver;

/**
 * To create a flexible panel of information that will change while the user manages his or her session.
 *
 * @author thickerson
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" }) public class StudyInfoPanel {

	private TreeMap data = new TreeMap();

	/**
	 * An array of StudyInfoPanelLine objects. This is only used if orderedData is flipped on.
	 */
	private ArrayList userOrderedData = new ArrayList();

	private String datePattern = "MM/dd/yyyy";

	private SimpleDateFormat englishSdf = new SimpleDateFormat(datePattern);

	private boolean studyInfoShown = true;

	private boolean orderedData = false;

	private boolean submitDataModule = false; // if it is submit data module,
	// need
	// to show tree panel

	private boolean extractData = false;

	private boolean createDataset = false;

	private boolean iconInfoShown = true; // added for the side icons

	private boolean manageSubject = false; // added to control the group of

	// side icons

	/**
	 * @return Returns the manageSubject.
	 */
	public boolean isManageSubject() {
		return manageSubject;
	}

	/**
	 * @param manageSubject
	 *            The manageSubject to set.
	 */
	public void setManageSubject(boolean manageSubject) {
		this.manageSubject = manageSubject;
	}

	/**
	 * @return Returns the iconInfoShown.
	 */
	public boolean isIconInfoShown() {
		return iconInfoShown;
	}

	/**
	 * @param iconInfoShown
	 *            The iconInfoShown to set.
	 */
	public void setIconInfoShown(boolean iconInfoShown) {
		this.iconInfoShown = iconInfoShown;
	}

	/**
	 * @return Returns the extractData.
	 */
	public boolean isExtractData() {
		return extractData;
	}

	/**
	 * @param extractData
	 *            The extractData to set.
	 */
	public void setExtractData(boolean extractData) {
		this.extractData = extractData;
	}

	/**
	 * @return Returns the submitDataModule.
	 */
	public boolean isSubmitDataModule() {
		return submitDataModule;
	}

	/**
	 * @param submitDataModule
	 *            The submitDataModule to set.
	 */
	public void setSubmitDataModule(boolean submitDataModule) {
		this.submitDataModule = submitDataModule;
	}

	/**
	 * Constructor.
	 */
	public StudyInfoPanel() {
		// blank generator
	}

	/**
	 * Set data method.
	 *
	 * @param key
	 *            String
	 * @param value
	 *            String
	 */
	public void setData(String key, String value) {
		data.put(key, value);
	}

	/**
	 * Remove data method.
	 *
	 * @param key
	 *            String
	 */
	public void removeData(String key) {
		data.remove(key);
	}

	/**
	 * Reset method.
	 */
	public void reset() {
		data = new TreeMap();
		userOrderedData = new ArrayList();
	}

	/**
	 * @return Returns the data.
	 */
	public TreeMap getData() {
		return data;
	}

	/**
	 * @param data
	 *            The data to set.
	 */
	public void setData(TreeMap data) {
		this.data = data;
	}

	/**
	 * The external function which creates data for the panel to reflect.
	 *
	 * @param page
	 *            Page
	 * @param session
	 *            HttpSession
	 * @param request
	 *            HttpServletRequest
	 * @param crfVersionDAO
	 *            CRFVersionDAO
	 * @param eventDefCRFDAO
	 *            EventDefinitionCRFDAO
	 */
	public void setData(Page page, HttpSession session, HttpServletRequest request, CRFVersionDAO crfVersionDAO,
			EventDefinitionCRFDAO eventDefCRFDAO) {

		Locale locale = LocaleResolver.getLocale(request);
		ResourceBundle resword = ResourceBundleProvider.getWordsBundle(locale);
		UserAccountBean currentUser = (UserAccountBean) session.getAttribute(SpringServlet.USER_BEAN_NAME);

		try {
			// defaults, can be reset by mistake by running through one page,
			this.setStudyInfoShown(true);
			this.setOrderedData(false);
			if (page.equals(Page.CREATE_DATASET_1)) {
				this.reset();
			} else if (page.equals(Page.CREATE_DATASET_2) || page.equals(Page.CREATE_DATASET_EVENT_ATTR)
					|| page.equals(Page.CREATE_DATASET_SUB_ATTR) || page.equals(Page.CREATE_DATASET_CRF_ATTR)
					|| page.equals(Page.CREATE_DATASET_GROUP_ATTR) || page.equals(Page.CREATE_DATASET_VIEW_SELECTED)) {
				HashMap eventlist = (HashMap) request.getAttribute("eventlist");
				ArrayList displayData = generateEventTree(eventlist, true);

				this.reset();
				this.setUserOrderedData(displayData);
				this.setStudyInfoShown(false);
				this.setOrderedData(true);
				this.setCreateDataset(true);
				this.setSubmitDataModule(false);
				this.setExtractData(false);

			} else if (page.equals(Page.CREATE_DATASET_3)) {
				this.reset();
				this.setStudyInfoShown(false);
				this.setOrderedData(false);
				this.setCreateDataset(true);
				this.setSubmitDataModule(false);
				this.setExtractData(false);

				DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
				int evCount = dsb.getItemIds().size();

				this.setData(resword.getString("items_selected"), Integer.toString(evCount));

			} else if (page.equals(Page.CREATE_DATASET_4)) {
				this.reset();
				this.setStudyInfoShown(false);
				this.setOrderedData(false);
				this.setCreateDataset(true);
				this.setSubmitDataModule(false);
				this.setExtractData(false);
				this.removeData(resword.getString("beginning_date"));
				this.removeData(resword.getString("ending_date"));
				DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
				int evCount = dsb.getItemIds().size();
				this.setData(resword.getString("items_selected"), Integer.toString(evCount));

				if ("01/01/1900".equals(englishSdf.format(dsb.getDateStart()))) {
					this.setData(resword.getString("beginning_date"), resword.getString("not_specified"));
				} else {
					String dateStart = DateUtil.printDate(dsb.getDateStart(), currentUser.getUserTimeZoneId(),
							DateUtil.DatePattern.DATE, LocaleResolver.getLocale(request));
					this.setData(resword.getString("beginning_date"), dateStart);
				}
				if ("12/31/2100".equals(englishSdf.format(dsb.getDateEnd()))) {
					this.setData(resword.getString("ending_date"), resword.getString("not_specified"));
				} else {
					String dateEnd = DateUtil.printDate(dsb.getDateEnd(), currentUser.getUserTimeZoneId(),
							DateUtil.DatePattern.DATE, LocaleResolver.getLocale(request));
					this.setData(resword.getString("ending_date"), dateEnd);
				}
				FilterBean fb = (FilterBean) session.getAttribute("newFilter");
				if (fb != null) {
					this.setData("Added Filter", fb.getName());
				}

			} else if (page.equals(Page.CONFIRM_DATASET)) {
				this.reset();
				this.setStudyInfoShown(false);
				this.setOrderedData(false);
				this.setCreateDataset(true);
				this.setSubmitDataModule(false);
				this.setExtractData(false);
				DatasetBean dsb = (DatasetBean) session.getAttribute("newDataset");
				this.setData(resword.getString("dataset_name"), dsb.getName());
				this.setData(resword.getString("dataset_description"), dsb.getDescription());
				int evCount = dsb.getItemIds().size();
				this.setData(resword.getString("items_selected"), Integer.toString(evCount));

				if ("01/01/1900".equals(englishSdf.format(dsb.getDateStart()))) {
					this.setData(resword.getString("beginning_date"), resword.getString("not_specified"));
				} else {
					String dateStart = DateUtil.printDate(dsb.getDateStart(), currentUser.getUserTimeZoneId(),
							DateUtil.DatePattern.DATE, LocaleResolver.getLocale(request));
					this.setData(resword.getString("beginning_date"), dateStart);
				}
				if ("12/31/2100".equals(englishSdf.format(dsb.getDateEnd()))) {
					this.setData(resword.getString("ending_date"), resword.getString("not_specified"));
				} else {
					String dateEnd = DateUtil.printDate(dsb.getDateEnd(), currentUser.getUserTimeZoneId(),
							DateUtil.DatePattern.DATE, LocaleResolver.getLocale(request));
					this.setData(resword.getString("ending_date"), dateEnd);
				}
				FilterBean fb = (FilterBean) session.getAttribute("newFilter");
				if (fb != null) {
					this.setData(resword.getString("added_filter"), fb.getName());
				}

			} else if (page.equals(Page.VIEW_STUDY_SUBJECT)) {
				this.reset();
				this.setStudyInfoShown(true);
				this.setOrderedData(true);
				this.setExtractData(false);
				this.setSubmitDataModule(false);
				this.setCreateDataset(false);
				this.setIconInfoShown(false);
				this.setManageSubject(true);
				request.setAttribute("showDDEIcon", Boolean.TRUE);

			} else if (page.equals(Page.ENTER_DATA_FOR_STUDY_EVENT)
					|| page.equals(Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET)) {

				ArrayList beans = (ArrayList) request.getAttribute("beans");
				EventCRFBean ecb = (EventCRFBean) request.getAttribute("eventCRF");
				this.reset();
				this.setUserOrderedData(generateTreeFromBeans(beans, ecb, resword, crfVersionDAO, eventDefCRFDAO));
				this.setStudyInfoShown(false);
				this.setOrderedData(true);
				this.setSubmitDataModule(true);
				this.setExtractData(false);
				this.setCreateDataset(false);
				this.setIconInfoShown(false);

			} else if (page.equals(Page.EDIT_DATASET)) {
				this.reset();

				HashMap eventlist = (LinkedHashMap) session.getAttribute("eventsForCreateDataset");
				ArrayList displayData = generateEventTree(eventlist, true);

				this.setCreateDataset(true);
				this.setOrderedData(true);
				this.setUserOrderedData(displayData);
				this.setStudyInfoShown(true);
				this.setSubmitDataModule(false);
				this.setExtractData(false);

				DatasetBean dsb = (DatasetBean) request.getAttribute("dataset");
				this.setData(resword.getString("dataset_name"), dsb.getName());
				String createdDate = DateUtil.printDate(dsb.getCreatedDate(), currentUser.getUserTimeZoneId(),
						DateUtil.DatePattern.DATE, LocaleResolver.getLocale(request));
				this.setData(resword.getString("date_created"), createdDate);
				this.setData(resword.getString("dataset_owner"), dsb.getOwner().getName());
				String dateLastRun = DateUtil.printDate(dsb.getDateLastRun(), currentUser.getUserTimeZoneId(),
						DateUtil.DatePattern.DATE, LocaleResolver.getLocale(request));
				this.setData(resword.getString("date_last_run"), dateLastRun);

			} else if (page.equals(Page.EXPORT_DATASETS)) {

				this.setCreateDataset(false);

			} else if (page.equals(Page.GENERATE_DATASET_HTML)) {
				DatasetBean db = (DatasetBean) request.getAttribute("dataset");
				ExtractBean exbean = (ExtractBean) request.getAttribute("extractBean");
				this.reset();
				ArrayList displayData;

				displayData = generateDatasetTree(exbean, db);
				this.setUserOrderedData(displayData);
				this.setStudyInfoShown(false);
				this.setOrderedData(true);
				this.setExtractData(true);
				this.setSubmitDataModule(false);
				this.setCreateDataset(false);

			} else if (page.equals(Page.LIST_STUDY_SUBJECTS)) {
				this.reset();
				this.setStudyInfoShown(true);
				this.setOrderedData(true);
				this.setExtractData(false);
				this.setSubmitDataModule(false);
				this.setCreateDataset(false);
				this.setIconInfoShown(false);
				this.setManageSubject(true);
				// don't want to show DDE icon key for subject matrix page
				request.setAttribute("showDDEIcon", Boolean.FALSE);

			} else if (page.equals(Page.VIEW_SECTION_DATA_ENTRY) || page.equals(Page.VIEW_SECTION_DATA_ENTRY_SERVLET)) {

				this.reset();
				this.setStudyInfoShown(true);
				this.setOrderedData(true);
				this.setExtractData(false);
				this.setSubmitDataModule(false);
				this.setCreateDataset(false);
				this.setIconInfoShown(true);
				this.setManageSubject(false);
			} else if (page.equals(Page.CREATE_SUBJECT_GROUP_CLASS)
					|| page.equals(Page.CREATE_SUBJECT_GROUP_CLASS_CONFIRM)
					|| page.equals(Page.UPDATE_SUBJECT_GROUP_CLASS)
					|| page.equals(Page.UPDATE_SUBJECT_GROUP_CLASS_CONFIRM)) {

				this.reset();
				this.setStudyInfoShown(true);
				this.setOrderedData(true);
				this.setExtractData(false);
				this.setSubmitDataModule(false);
				this.setCreateDataset(false);
				this.setIconInfoShown(true);
				this.setManageSubject(false);
			} else if (page.equals(Page.VIEW_RULE_SETS2)) {
				HashMap eventlist = (HashMap) request.getAttribute("eventlist");
				ArrayList displayData = generateEventTree(eventlist, false);

				this.reset();
				this.setUserOrderedData(displayData);
				this.setStudyInfoShown(true);
				this.setOrderedData(true);
				this.setCreateDataset(true);
				this.setSubmitDataModule(false);
				this.setExtractData(false);

			} else {
				// automatically reset if we don't know what's happening
				this.reset();
				this.setStudyInfoShown(true);
				this.setOrderedData(true);
				this.setExtractData(false);
				this.setSubmitDataModule(false);
				this.setCreateDataset(false);
				this.setIconInfoShown(true);
				this.setManageSubject(false);
			}
		} catch (Exception e) {
			this.reset();
		}
	}

	/**
	 * @return Returns the studyInfoShown.
	 */
	public boolean isStudyInfoShown() {
		return studyInfoShown;
	}

	/**
	 * @param studyInfoShown
	 *            The studyInfoShown to set.
	 */
	public void setStudyInfoShown(boolean studyInfoShown) {
		this.studyInfoShown = studyInfoShown;
	}

	/**
	 * @return Returns the orderedData.
	 */
	public boolean isOrderedData() {
		return orderedData;
	}

	/**
	 * @param orderedData
	 *            The orderedData to set.
	 */
	public void setOrderedData(boolean orderedData) {
		this.orderedData = orderedData;
	}

	/**
	 * @return Returns the userOrderedData.
	 */
	public ArrayList getUserOrderedData() {
		return userOrderedData;
	}

	/**
	 * @param userOrderedData
	 *            The userOrderedData to set.
	 */
	public void setUserOrderedData(ArrayList userOrderedData) {
		this.userOrderedData = userOrderedData;
	}

	private String getStageImageText(StudySubjectBean studySubject, StudyEventBean studyEvent, DisplayEventCRFBean dec,
			ResourceBundle resBundle, CRFVersionDAO crfVersionDAO, EventDefinitionCRFDAO eventDefCRFDAO) {

		Status eventCRFStatus = EventCRFUtil.getEventCRFCurrentStatus(studySubject, studyEvent,
				dec.getEventDefinitionCRF(), dec.getEventCRF(), crfVersionDAO, eventDefCRFDAO);
		return "<img src='" + EventCRFUtil.getEventCRFStatusIconPath(eventCRFStatus) + "' alt='"
				+ resBundle.getString(EventCRFUtil.getStatusIconHintHandle(eventCRFStatus, dec.getEventDefinitionCRF()))
				+ "'>";
	}

	/**
	 * Generates a tree view in side info panel for submitting data page.
	 *
	 * @param rows
	 *            ArrayList
	 * @param ecb
	 *            EventCRFBean
	 * @param resBundle
	 *            ResourceBundle
	 * @return ArrayList
	 */
	private ArrayList generateTreeFromBeans(ArrayList rows, EventCRFBean ecb, ResourceBundle resBundle,
			CRFVersionDAO crfVersionDAO, EventDefinitionCRFDAO eventDefCRFDAO) {

		ArrayList displayData = new ArrayList();
		for (Object row : rows) {
			DisplayStudyEventBean dseBean = (DisplayStudyEventBean) row;
			StudyEventBean seBean = dseBean.getStudyEvent();
			// checks whether the event is the current one
			if (ecb != null && ecb.getStudyEventId() == seBean.getId()) {
				displayData.add(new StudyInfoPanelLine("Study Event", seBean.getStudyEventDefinition().getName(), true,
						false, true));

			} else {
				displayData.add(new StudyInfoPanelLine("Study Event", seBean.getStudyEventDefinition().getName(), true,
						false, false));
			}

			displayData.add(new StudyInfoPanelLine("<b>Status: </b>", "<a href='EnterDataForStudyEvent?eventId="
					+ seBean.getId() + "'>" + seBean.getSubjectEventStatus().getName() + "</a>", false, false, false));
			ArrayList displayCRFs = dseBean.getDisplayEventCRFs();
			int count = 0;
			for (Object displayCRF : displayCRFs) {
				DisplayEventCRFBean dec = (DisplayEventCRFBean) displayCRF;
				boolean lastCRF = (count == displayCRFs.size() - 1) && (dseBean.getUncompletedCRFs().size() == 0);
				String eventCRFStatusImgString = getStageImageText(dseBean.getStudySubject(), seBean, dec, resBundle,
						crfVersionDAO, eventDefCRFDAO);
				if (ecb != null && ecb.getId() == dec.getEventCRF().getId()) {
					displayData.add(new StudyInfoPanelLine(eventCRFStatusImgString,
							"<span class='alert'>" + dec.getEventCRF().getCrf().getName() + " "
									+ dec.getEventCRF().getCrfVersion().getName() + "</span>", false, lastCRF, true));
				} else {
					displayData.add(new StudyInfoPanelLine(eventCRFStatusImgString,
							" " + dec.getEventCRF().getCrf().getName() + " "
									+ dec.getEventCRF().getCrfVersion().getName() + "</a>", false, lastCRF, false));
				}
				count++;
			}
			count = 0;
			ArrayList uncompleted = dseBean.getUncompletedCRFs();
			for (Object anUncompleted : uncompleted) {
				DisplayEventDefinitionCRFBean dedc = (DisplayEventDefinitionCRFBean) anUncompleted;
				if (count == uncompleted.size() - 1) {
					if (ecb != null && ecb.getId() == dedc.getEventCRF().getId()
							&& ecb.getCrf().getId() == dedc.getEventCRF().getCrf().getId()) {
						displayData
								.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>",
										"<span class='alert'>" + dedc.getEdc().getCrf().getName() + "</span>", false,
										true, true));
					} else {
						displayData
								.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>",
										"<span class='alert'>" + dedc.getEdc().getCrf().getName() + "</a>", false, true,
										false));
					}
				} else {
					if (ecb != null && ecb.getId() == dedc.getEventCRF().getId()) {
						displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>",
										"<span class='alert'>" + dedc.getEdc().getCrf().getName() + "</span>", false,
										false, true));
					} else {
						displayData.add(new StudyInfoPanelLine("<img src='images/icon_NotStarted.gif' alt='Not Started'/>",
												"<span class='alert'>" + dedc.getEdc().getCrf().getName() + "</a>",
												false, false, false));
					}
				}
				count++;
			}
		}

		return displayData;
	}

	private ArrayList generateDatasetTree(ExtractBean eb, DatasetBean db) {
		ArrayList displayData = new ArrayList();

		ArrayList seds = eb.getStudyEvents();

		for (int i = 0; i < seds.size(); i++) {
			// second, iterate through seds
			StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seds.get(i);
			String repeating = "";
			if (sed.isRepeating()) {
				repeating = " (Repeating) ";
			}
			displayData.add(new StudyInfoPanelLine("Study Event Definition", sed.getName() + repeating, true, false));

			ArrayList crfs = sed.getCrfs();
			for (int j = 0; j < crfs.size(); j++) {
				CRFBean cb = (CRFBean) crfs.get(j);

				if (j < crfs.size() - 1 && crfs.size() > 1) {
					displayData.add(new StudyInfoPanelLine("CRF", cb.getName() + " <b>"
							+ ExtractBean.getSEDCRFCode(i + 1, j + 1) + "</b>", false, false));

				} else {
					// last crf
					displayData.add(new StudyInfoPanelLine("CRF", cb.getName() + " <b>"
							+ ExtractBean.getSEDCRFCode(i + 1, j + 1) + "</b>", false, true));
				}
			}
		}
		return displayData;
	}

	private ArrayList generateEventTree(HashMap eventlist, Boolean isExtractData) {
		ArrayList displayData = new ArrayList();
		for (Object o : eventlist.keySet()) {
			StudyEventDefinitionBean sed = (StudyEventDefinitionBean) o;
			displayData.add(new StudyInfoPanelLine("Definition", sed.getName(), true, false));
			ArrayList crfs = (ArrayList) eventlist.get(sed);
			int ordinalCrf = 1;
			for (int i = 0; i < crfs.size(); i++) {
				CRFBean crf = (CRFBean) crfs.get(i);
				if (ordinalCrf < crfs.size()) {
					if (isExtractData) {
						displayData.add(new StudyInfoPanelLine("CRF", "<a href='SelectItems?crfId=" + crf.getId()
								+ "&defId=" + sed.getId() + "'>" + crf.getName() + "</a>", false, false));
					} else {
						displayData.add(new StudyInfoPanelLine("CRF",
								"<a href='ViewRuleAssignment?ruleAssignments_f_crfName=" + crf.getName() + "'>"
										+ crf.getName() + "</a>", false, false));
					}
				} else {
					if (isExtractData) {
						displayData.add(new StudyInfoPanelLine("CRF", "<a href='SelectItems?crfId=" + crf.getId()
								+ "&defId=" + sed.getId() + "'>" + crf.getName() + "</a>", false, true));
					} else {
						displayData.add(new StudyInfoPanelLine("CRF",
								"<a href='ViewRuleAssignment?ruleAssignments_f_studyEventDefinitionName="
										+ sed.getName() + "&ruleAssignments_f_crfName=" + crf.getName() + "'>"
										+ crf.getName() + "</a>", false, true));
					}

				}
				ordinalCrf++;
			}
		}
		return displayData;

	}

	/**
	 * @return Returns the createDataset.
	 */
	public boolean isCreateDataset() {
		return createDataset;
	}

	/**
	 * @param createDataset
	 *            The createDataset to set.
	 */
	public void setCreateDataset(boolean createDataset) {
		this.createDataset = createDataset;
	}

}
