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

package org.akaza.openclinica.web.crfdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.sql.DataSource;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.rule.XmlSchemaValidationHelper;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.job.CrfBusinessLogicHelper;
import org.akaza.openclinica.web.job.TriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 
 * @author thickerson, daniel
 * 
 */
@SuppressWarnings({"unchecked"})
public class DataImportService {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	XmlSchemaValidationHelper schemaValidator = new XmlSchemaValidationHelper();
	ResourceBundle respage;
	Locale locale;

	private WSImportCRFDataService dataService;

	public List<String> validateData(ODMContainer odmContainer, DataSource dataSource, CoreResources resources,
			StudyBean studyBean, UserAccountBean userBean, List<DisplayItemBeanWrapper> displayItemBeanWrappers) {
		respage = ResourceBundleProvider.getPageMessagesBundle();
		TriggerService triggerService = new TriggerService();

		StringBuffer auditMsg = new StringBuffer();
		List<String> errors = new ArrayList<String>();

		logger.debug("passing an odm container and study bean id: " + studyBean.getId());
		errors = getImportCRFDataService(dataSource).validateStudyMetadata(odmContainer, studyBean.getId());
		// this needs to be replaced with the study name from the job, since
		// the user could be in any study ...
		if (errors != null && errors.size() > 0) {
			return errors;
		}

		auditMsg.append(respage.getString("passed_study_check") + " ");
		auditMsg.append(respage.getString("passed_oid_metadata_check") + " ");

		// validation errors, the same as in the ImportCRFDataServlet. DRY?
		List<EventCRFBean> eventCRFBeans = getImportCRFDataService(dataSource).fetchEventCRFBeans(odmContainer,
				userBean);

		ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
		logger.debug("found a list of eventCRFBeans: " + eventCRFBeans.toString());

		// -- does the event already exist? if not, fail
		if (eventCRFBeans.isEmpty()) {
			errors.add(respage.getString("no_event_crfs_matching_the_xml_metadata"));
			return errors;
		}
		for (EventCRFBean eventCRFBean : eventCRFBeans) {
			DataEntryStage dataEntryStage = eventCRFBean.getStage();
			Status eventCRFStatus = eventCRFBean.getStatus();

			logger.debug("Event CRF Bean: id " + eventCRFBean.getId() + ", data entry stage "
					+ dataEntryStage.getName() + ", status " + eventCRFStatus.getName());
			if (eventCRFStatus.equals(Status.AVAILABLE) || dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
					|| dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
					|| dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
					|| dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
				permittedEventCRFIds.add(new Integer(eventCRFBean.getId()));
			} else {
				errors.add(respage.getString("your_listed_crf_in_the_file") + " " + eventCRFBean.getEventName());
				continue;
			}
		}

		if (eventCRFBeans.size() >= permittedEventCRFIds.size()) {
			auditMsg.append(respage.getString("passed_event_crf_status_check") + " ");
		} else {
			auditMsg.append(respage.getString("the_event_crf_not_correct_status") + " ");
		}

		HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
		HashMap<String, String> hardValidationErrors = new HashMap<String, String>();

		try {
			List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
			MockHttpServletRequest request = new MockHttpServletRequest();
			request.addPreferredLocale(locale);

			tempDisplayItemBeanWrappers = getImportCRFDataService(dataSource).lookupValidationErrors(request,
					odmContainer, userBean, totalValidationErrors, hardValidationErrors, permittedEventCRFIds);
			displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
			logger.debug("size of total validation errors: "
					+ (totalValidationErrors.size() + hardValidationErrors.size()));
			ArrayList<SubjectDataBean> subjectData = odmContainer.getCrfDataPostImportContainer().getSubjectData();
			if (!hardValidationErrors.isEmpty()) {
				// check here where to get group repeat key
				errors.add(triggerService.generateHardValidationErrorMessage(subjectData, hardValidationErrors, "1"));
			}
			if (!totalValidationErrors.isEmpty()) {
				errors.add(triggerService.generateHardValidationErrorMessage(subjectData, totalValidationErrors, "1"));
			}

		} catch (NullPointerException npe1) {
			// what if you have 2 event crfs but the third is a fake?
			npe1.printStackTrace();
			errors.add(respage.getString("an_error_was_thrown_while_validation_errors"));
			logger.debug("=== threw the null pointer, import === " + npe1.getMessage());
		} catch (OpenClinicaException oce1) {
			errors.add(oce1.getOpenClinicaMessage());
			logger.debug("=== threw the openclinica message, import === " + oce1.getOpenClinicaMessage());
		}

		auditMsg.append(respage.getString("passing_crf_edit_checks") + " ");

		return errors;

	}

	public ArrayList<String> submitData(ODMContainer odmContainer, DataSource dataSource, StudyBean studyBean,
			UserAccountBean userBean, List<DisplayItemBeanWrapper> displayItemBeanWrappers) throws Exception {

		boolean discNotesGenerated = false;

		ItemDataDAO itemDataDao = new ItemDataDAO(dataSource);
		EventCRFDAO eventCrfDao = new EventCRFDAO(dataSource);

		StringBuffer auditMsg = new StringBuffer();
		int eventCrfBeanId = -1;
		EventCRFBean eventCrfBean;
		ArrayList<Integer> eventCrfInts;
		ItemDataBean itemDataBean;

		CrfBusinessLogicHelper crfBusinessLogicHelper = new CrfBusinessLogicHelper(dataSource);
		for (DisplayItemBeanWrapper wrapper : displayItemBeanWrappers) {
			logger.debug("right before we check to make sure it is savable: " + wrapper.isSavable());
			if (wrapper.isSavable()) {
				eventCrfInts = new ArrayList<Integer>();
				logger.debug("wrapper problems found : " + wrapper.getValidationErrors().toString());
				if (wrapper.getDisplayItemBeans() != null && wrapper.getDisplayItemBeans().size() == 0) {
					return getReturnList("fail", "", "No items to submit. Please check your XML.");
				}
				for (DisplayItemBean displayItemBean : wrapper.getDisplayItemBeans()) {
					eventCrfBeanId = displayItemBean.getData().getEventCRFId();
					eventCrfBean = (EventCRFBean) eventCrfDao.findByPK(eventCrfBeanId);
					logger.debug("found value here: " + displayItemBean.getData().getValue());
					logger.debug("found status here: " + eventCrfBean.getStatus().getName());
					itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem().getId(),
							eventCrfBean.getId(), displayItemBean.getData().getOrdinal());
					if (wrapper.isOverwrite() && itemDataBean.getStatus() != null) {
						logger.debug("just tried to find item data bean on item name "
								+ displayItemBean.getItem().getName());
						itemDataBean.setUpdatedDate(new Date());
						itemDataBean.setUpdater(userBean);
						itemDataBean.setValue(displayItemBean.getData().getValue());
						// set status?
						itemDataDao.update(itemDataBean);
						logger.debug("updated: " + itemDataBean.getItemId());
						// need to set pk here in order to create dn
						displayItemBean.getData().setId(itemDataBean.getId());
					} else {
						itemDataDao.create(displayItemBean.getData());
						logger.debug("created: " + displayItemBean.getData().getItemId());
						itemDataBean = itemDataDao.findByItemIdAndEventCRFIdAndOrdinal(displayItemBean.getItem()
								.getId(), eventCrfBean.getId(), displayItemBean.getData().getOrdinal());
						displayItemBean.getData().setId(itemDataBean.getId());
					}
					ItemDAO idao = new ItemDAO(dataSource);
					ItemBean ibean = (ItemBean) idao.findByPK(displayItemBean.getData().getItemId());
					String itemOid = displayItemBean.getItem().getOid() + "_" + wrapper.getStudyEventRepeatKey() + "_"
							+ displayItemBean.getData().getOrdinal() + "_" + wrapper.getStudySubjectOid();
					if (wrapper.getValidationErrors().containsKey(itemOid)) {
						ArrayList<String> messageList = (ArrayList<String>) wrapper.getValidationErrors().get(itemOid);
						for (String message : messageList) {
							DiscrepancyNoteBean parentDn = createDiscrepancyNote(ibean, message, eventCrfBean,
									displayItemBean, null, userBean, dataSource, studyBean);
							createDiscrepancyNote(ibean, message, eventCrfBean, displayItemBean, parentDn.getId(),
									userBean, dataSource, studyBean);
							discNotesGenerated = true;
							logger.debug("*** created disc note with message: " + message);
							auditMsg.append(wrapper.getStudySubjectOid() + ": " + ibean.getOid() + ": " + message
									+ "---");
						}
					}
					if (!eventCrfInts.contains(new Integer(eventCrfBean.getId()))) {
						crfBusinessLogicHelper.markCRFComplete(eventCrfBean, userBean);
						eventCrfInts.add(new Integer(eventCrfBean.getId()));
					}
				}
			}
		}
		if (!discNotesGenerated) {
			return getReturnList("success", "", auditMsg.toString());
		} else {
			return getReturnList("warn", "", auditMsg.toString());
		}
	}

	public DiscrepancyNoteBean createDiscrepancyNote(ItemBean itemBean, String message, EventCRFBean eventCrfBean,
			DisplayItemBean displayItemBean, Integer parentId, UserAccountBean uab, DataSource ds, StudyBean study) {

		DiscrepancyNoteBean note = new DiscrepancyNoteBean();
		StudySubjectDAO ssdao = new StudySubjectDAO(ds);
		note.setDescription(message);
		note.setDetailedNotes("Failed Validation Check");
		note.setOwner(uab);
		note.setCreatedDate(new Date());
		note.setResolutionStatusId(ResolutionStatus.OPEN.getId());
		note.setDiscrepancyNoteTypeId(DiscrepancyNoteType.FAILEDVAL.getId());
		if (parentId != null) {
			note.setParentDnId(parentId);
		}

		note.setField(itemBean.getName());
		note.setStudyId(study.getId());
		note.setEntityName(itemBean.getName());
		note.setEntityType("ItemData");
		note.setEntityValue(displayItemBean.getData().getValue());

		note.setEventName(eventCrfBean.getName());
		note.setEventStart(eventCrfBean.getCreatedDate());
		note.setCrfName(displayItemBean.getEventDefinitionCRF().getCrfName());

		StudySubjectBean ss = (StudySubjectBean) ssdao.findByPK(eventCrfBean.getStudySubjectId());
		note.setSubjectName(ss.getName());

		note.setEntityId(displayItemBean.getData().getId());
		note.setColumn("value");

		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(ds);
		note = (DiscrepancyNoteBean) dndao.create(note);
		// so that the below method works, need to set the entity above
		logger.debug("trying to create mapping with " + note.getId() + " " + note.getEntityId() + " "
				+ note.getColumn() + " " + note.getEntityType());
		dndao.createMapping(note);
		logger.debug("just created mapping");
		return note;
	}

	private WSImportCRFDataService getImportCRFDataService(DataSource dataSource) {
		if (locale == null) {
			locale = new Locale("en-US");
		}
		dataService = (this.dataService != null) ? dataService : new WSImportCRFDataService(dataSource, locale);
		return dataService;
	}

	private ArrayList<String> getReturnList(String status, String msg, String auditMsg) {
		ArrayList<String> retList = new ArrayList<String>(3);
		retList.add(status);
		retList.add(msg.toString());
		retList.add(auditMsg.toString());
		return retList;
	}
}
