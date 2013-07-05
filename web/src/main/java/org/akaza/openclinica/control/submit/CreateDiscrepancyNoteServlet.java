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
package org.akaza.openclinica.control.submit;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.DnDescription;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.*;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.EmailEngine;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.discrepancy.DnDescriptionDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.*;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.*;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

/**
 * Create a discrepancy note for a data entity
 * 
 * @author jxu
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked", "serial"})
public class CreateDiscrepancyNoteServlet extends SecureController {

	Locale locale;

	public static final String DIS_TYPES = "discrepancyTypes";

	public static final String RES_STATUSES = "resolutionStatuses";

	public static final String ENTITY_ID = "id";

	public static final String SUBJECT_ID = "subjectId";

	public static final String ITEM_ID = "itemId";

	public static final String IS_GROUP_ITEM = "isGroup";

	public static final String PARENT_ID = "parentId";// parent note id

	public static final String ENTITY_TYPE = "name";

	public static final String ENTITY_COLUMN = "column";

	public static final String ENTITY_FIELD = "field";

	public static final String FORM_DISCREPANCY_NOTES_NAME = "fdnotes";

	public static final String DIS_NOTE = "discrepancyNote";

	public static final String WRITE_TO_DB = "writeToDB";

	public static final String IS_REASON_FOR_CHANGE = "isRfc";

	public static final String PRESET_RES_STATUS = "strResStatus";

	public static final String CAN_MONITOR = "canMonitor";

	public static final String NEW_NOTE = "new";

	public static final String RES_STATUS_ID = "resStatusId";

	public static final String ERROR_FLAG = "errorFlag";// use to determine

	public static final String USER_ACCOUNTS = "userAccounts"; // use to provide

	public static final String USER_ACCOUNT_ID = "strUserAccountId"; // use to

	public static final String SUBMITTED_USER_ACCOUNT_ID = "userAccountId";

	public static final String PRESET_USER_ACCOUNT_ID = "preUserAccountId";

	public static final String EMAIL_USER_ACCOUNT = "sendEmail";

	public static final String WHICH_RES_STATUSES = "whichResStatus";

	public static final String EVENT_CRF_ID = "eventCRFId";
	public static final String PARENT_ROW_COUNT = "rowCount";

	public String exceptionName = resexception.getString("no_permission_to_create_discrepancy_note");
	public String noAccessMessage = respage.getString("you_may_not_create_discrepancy_note")
			+ respage.getString("change_study_contact_sysadmin");

	private List<ResolutionStatus> actualStatusesList;

	@Override
	protected void mayProceed() throws InsufficientPermissionException {
		checkStudyLocked(Page.MENU_SERVLET, respage.getString("current_study_locked"));
		locale = request.getLocale();

		if (SubmitDataServlet.mayViewData(ub, currentRole)) {
			return;
		}

		addPageMessage(noAccessMessage);
		throw new InsufficientPermissionException(Page.MENU, exceptionName, "1");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void processRequest() throws Exception {
		FormProcessor fp = new FormProcessor(request);
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
		ArrayList types = DiscrepancyNoteType.toArrayList();

		request.setAttribute(DIS_TYPES, types);
		request.setAttribute(RES_STATUSES, ResolutionStatus.toArrayList());

		boolean writeToDB = fp.getBoolean(WRITE_TO_DB, true); // this should be set based on a new property of
																// DisplayItemBean
		boolean isReasonForChange = fp.getBoolean(IS_REASON_FOR_CHANGE);
		request.setAttribute(IS_REASON_FOR_CHANGE, isReasonForChange ? "1" : "0");
		int entityId = fp.getInt(ENTITY_ID);
		// subjectId has to be added to the database when disc notes area saved
		// as entity_type 'subject'
		int subjectId = fp.getInt(SUBJECT_ID);
		int itemId = fp.getInt(ITEM_ID);
		String entityType = fp.getString(ENTITY_TYPE);

		String field = fp.getString(ENTITY_FIELD);
		String column = fp.getString(ENTITY_COLUMN);
		int parentId = fp.getInt(PARENT_ID);

		int isGroup = fp.getInt(IS_GROUP_ITEM);
		int eventCRFId = fp.getInt(EVENT_CRF_ID);
		request.setAttribute(EVENT_CRF_ID, new Integer(eventCRFId));
		int rowCount = fp.getInt(PARENT_ROW_COUNT);
		// run only once: try to recalculate writeToDB
		if (!StringUtil.isBlank(entityType) && "itemData".equalsIgnoreCase(entityType) && isGroup != 0
				&& eventCRFId != 0) {
			int ordinal_for_repeating_group_field = calculateOrdinal(isGroup, field, eventCRFId, rowCount);
			int writeToDBStatus = isWriteToDB(isGroup, field, entityId, itemId, ordinal_for_repeating_group_field,
					eventCRFId);
			writeToDB = (writeToDBStatus == -1) ? false : ((writeToDBStatus == 1) ? true : writeToDB);
		}
		boolean isInError = fp.getBoolean(ERROR_FLAG);
		request.setAttribute(ERROR_FLAG, isInError ? "1" : "0");

		boolean isNew = fp.getBoolean(NEW_NOTE);
		request.setAttribute(NEW_NOTE, isNew ? "1" : "0");

		String strResStatus = fp.getString(PRESET_RES_STATUS);
		if (!strResStatus.equals("")) {
			request.setAttribute(PRESET_RES_STATUS, strResStatus);
		}

		String monitor = fp.getString("monitor");
		String enterData = fp.getString("enterData");
		request.setAttribute("enterData", enterData);

		boolean enteringData = false;
		if (enterData != null && "1".equalsIgnoreCase(enterData)) {
			// variables are not set in JSP, so not from viewing data and from
			// entering data
			request.setAttribute(CAN_MONITOR, "1");
			request.setAttribute("monitor", monitor);

			enteringData = true;
		} else if ("1".equalsIgnoreCase(monitor)) {// change to allow user to
			// enter note for all items,
			// not just blank items

			request.setAttribute(CAN_MONITOR, "1");
			request.setAttribute("monitor", monitor);

		} else {
			request.setAttribute(CAN_MONITOR, "0");

		}

		if ("itemData".equalsIgnoreCase(entityType) && enteringData) {
			request.setAttribute("enterItemData", "yes");
		}

		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
		int preUserId = 0;
		if (!StringUtil.isBlank(entityType)) {
			if ("itemData".equalsIgnoreCase(entityType) || "itemdata".equalsIgnoreCase(entityType)) {
				ItemBean item = (ItemBean) new ItemDAO(sm.getDataSource()).findByPK(itemId);
				ItemDataBean itemData = (ItemDataBean) new ItemDataDAO(sm.getDataSource()).findByPK(entityId);
				request.setAttribute("entityValue", itemData.getValue());
				request.setAttribute("entityName", item.getName());
				EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
				EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
				/*
				 * ClinCapture #1 add event crf owner id
				 */
				request.setAttribute("eventCrfOwnerId", ec.getOwnerId());
				preUserId = ec.getOwnerId();
			} else if ("studySub".equalsIgnoreCase(entityType)) {
				StudySubjectBean ssub = (StudySubjectBean) new StudySubjectDAO(sm.getDataSource()).findByPK(entityId);
				SubjectBean sub = (SubjectBean) new SubjectDAO(sm.getDataSource()).findByPK(ssub.getSubjectId());
				preUserId = ssub.getOwnerId();
				if (!StringUtil.isBlank(column)) {
					if ("enrollment_date".equalsIgnoreCase(column)) {
						if (ssub.getEnrollmentDate() != null) {
							request.setAttribute("entityValue", dateFormatter.format(ssub.getEnrollmentDate()));
						} else {
							request.setAttribute("entityValue", resword.getString("N/A"));
						}
						request.setAttribute("entityName", resword.getString("enrollment_date"));
					} else if ("gender".equalsIgnoreCase(column)) {
						request.setAttribute("entityValue", sub.getGender() + "");
						request.setAttribute("entityName", resword.getString("gender"));
					} else if ("date_of_birth".equalsIgnoreCase(column)) {
						if (sub.getDateOfBirth() != null) {
							request.setAttribute("entityValue", dateFormatter.format(sub.getDateOfBirth()));
						} else {
							request.setAttribute("entityValue", resword.getString("N/A"));
						}
						request.setAttribute("entityName", resword.getString("date_of_birth"));
					} else if ("unique_identifier".equalsIgnoreCase(column)) {
						if (sub.getUniqueIdentifier() != null) {
							request.setAttribute("entityValue", sub.getUniqueIdentifier());
						}
						request.setAttribute("entityName", resword.getString("unique_identifier"));
					}
				}
			} else if ("subject".equalsIgnoreCase(entityType)) {
				SubjectBean sub = (SubjectBean) new SubjectDAO(sm.getDataSource()).findByPK(entityId);
				preUserId = sub.getOwnerId();
				if (!StringUtil.isBlank(column)) {
					if ("gender".equalsIgnoreCase(column)) {
						request.setAttribute("entityValue", sub.getGender() + "");
						request.setAttribute("entityName", resword.getString("gender"));
					} else if ("date_of_birth".equalsIgnoreCase(column)) {
						if (sub.getDateOfBirth() != null) {
							request.setAttribute("entityValue", dateFormatter.format(sub.getDateOfBirth()));
						}
						request.setAttribute("entityName", resword.getString("date_of_birth"));
					} else if ("unique_identifier".equalsIgnoreCase(column)) {
						request.setAttribute("entityValue", sub.getUniqueIdentifier());
						request.setAttribute("entityName", resword.getString("unique_identifier"));
					}
				}
			} else if ("studyEvent".equalsIgnoreCase(entityType)) {
				StudyEventBean se = (StudyEventBean) new StudyEventDAO(sm.getDataSource()).findByPK(entityId);
				preUserId = se.getOwnerId();
				if (!StringUtil.isBlank(column)) {
					if ("location".equalsIgnoreCase(column)) {
						request.setAttribute("entityValue",
								(se.getLocation().equals("") || se.getLocation() == null) ? resword.getString("N/A")
										: se.getLocation());
						request.setAttribute("entityName", resword.getString("location"));
					} else if ("start_date".equalsIgnoreCase(column)) {
						if (se.getDateStarted() != null) {
							request.setAttribute("entityValue", dateFormatter.format(se.getDateStarted()));
						} else {
							request.setAttribute("entityValue", resword.getString("N/A"));
						}
						request.setAttribute("entityName", resword.getString("start_date"));
					} else if ("end_date".equalsIgnoreCase(column)) {
						if (se.getDateEnded() != null) {
							request.setAttribute("entityValue", dateFormatter.format(se.getDateEnded()));
						} else {
							request.setAttribute("entityValue", resword.getString("N/A"));
						}
						request.setAttribute("entityName", resword.getString("end_date"));
					}
				}
			} else if ("eventCrf".equalsIgnoreCase(entityType)) {
				EventCRFBean ec = (EventCRFBean) new EventCRFDAO(sm.getDataSource()).findByPK(entityId);
				preUserId = ec.getOwnerId();
				if (!StringUtil.isBlank(column)) {
					if ("date_interviewed".equals(column)) {
						if (ec.getDateInterviewed() != null) {
							request.setAttribute("entityValue", dateFormatter.format(ec.getDateInterviewed()));
						} else {
							request.setAttribute("entityValue", resword.getString("N/A"));
						}
						request.setAttribute("entityName", resword.getString("date_interviewed"));
					} else if ("interviewer_name".equals(column)) {
						request.setAttribute("entityValue", ec.getInterviewerName());
						request.setAttribute("entityName", resword.getString("interviewer_name"));
					}
				}
			}

		}

		// finds all the related notes
		ArrayList notes = (ArrayList) dndao.findAllByEntityAndColumn(entityType, entityId, column);

		DiscrepancyNoteBean parent = new DiscrepancyNoteBean();
		if (parentId > 0) {
			dndao.setFetchMapping(true);
			parent = (DiscrepancyNoteBean) dndao.findByPK(parentId);
			if (parent.isActive()) {
				request.setAttribute("parent", parent);
			}
			dndao.setFetchMapping(false);
		}
		FormDiscrepancyNotes newNotes = (FormDiscrepancyNotes) session.getAttribute(FORM_DISCREPANCY_NOTES_NAME);

		if (newNotes == null) {
			newNotes = new FormDiscrepancyNotes();
		}
		boolean isNotesExistInSession = (!newNotes.getNotes(field).isEmpty()) ? true : (!newNotes.getNotes(field)
				.isEmpty()) ? true : false;
		if (!notes.isEmpty() || isNotesExistInSession) {
			request.setAttribute("hasNotes", "yes");
		} else {
			request.setAttribute("hasNotes", "no");
			logger.debug("has notes:" + "no");
		}

		// only for adding a new thread
		if (currentRole.getRole().equals(Role.CLINICAL_RESEARCH_COORDINATOR) || currentRole.getRole().equals(Role.INVESTIGATOR)) {
			ArrayList<ResolutionStatus> resStatuses = new ArrayList<ResolutionStatus>();
			resStatuses.add(ResolutionStatus.OPEN);
			resStatuses.add(ResolutionStatus.RESOLVED);
			request.setAttribute(RES_STATUSES, resStatuses);
			ArrayList types2 = DiscrepancyNoteType.toArrayList();
			types2.remove(DiscrepancyNoteType.QUERY);
			request.setAttribute(DIS_TYPES, types2);
			request.setAttribute(WHICH_RES_STATUSES, "22");
		} else if (currentRole.getRole().equals(Role.STUDY_MONITOR)) {
			ArrayList<ResolutionStatus> resStatuses = new ArrayList();
			resStatuses.add(ResolutionStatus.OPEN);
			resStatuses.add(ResolutionStatus.UPDATED);
			resStatuses.add(ResolutionStatus.CLOSED);
			request.setAttribute(RES_STATUSES, resStatuses);
			request.setAttribute(WHICH_RES_STATUSES, "1");
			ArrayList<DiscrepancyNoteType> types2 = new ArrayList<DiscrepancyNoteType>();
			types2.add(DiscrepancyNoteType.QUERY);
			request.setAttribute(DIS_TYPES, types2);
		} else {
			ArrayList<ResolutionStatus> resStatuses = ResolutionStatus.toArrayList();
			resStatuses.remove(ResolutionStatus.NOT_APPLICABLE);
			request.setAttribute(RES_STATUSES, resStatuses);
			;
			request.setAttribute(WHICH_RES_STATUSES, "2");
		}

		if (currentRole.getRole().equals(Role.CLINICAL_RESEARCH_COORDINATOR) || currentRole.getRole().equals(Role.INVESTIGATOR)) {

			request.setAttribute("showStatus", false);
			request.setAttribute(RES_STATUSES, Arrays.asList(ResolutionStatus.NOT_APPLICABLE));
			request.setAttribute(DIS_TYPES, Arrays.asList(DiscrepancyNoteType.ANNOTATION));
			actualStatusesList = Arrays.asList(ResolutionStatus.NOT_APPLICABLE);
			request.setAttribute(WHICH_RES_STATUSES, "2");
		} else {
			request.setAttribute("showStatus", true);
			request.setAttribute(RES_STATUSES, ResolutionStatus.simpleList);
			request.setAttribute(DIS_TYPES, DiscrepancyNoteType.simpleList);
			actualStatusesList = ResolutionStatus.simpleList;
			request.setAttribute(WHICH_RES_STATUSES, "1");
		}

		if (!fp.isSubmitted()) {
			DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
			ArrayList<DnDescription> dnDescriptions = new ArrayList<DnDescription>();
			
			if (subjectId > 0) {
				// This doesn't seem correct, because the SubjectId should
				// be the id for
				// the SubjectBean, different from StudySubjectBean
				StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
				StudySubjectBean ssub = (StudySubjectBean) ssdao.findByPK(subjectId);
				dnb.setSubjectName(ssub.getName());
				dnb.setSubjectId(ssub.getId());
				dnb.setStudySub(ssub);
				StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
				int parentStudyForSubject = 0;
				StudyBean studyBeanSub = (StudyBean) studyDAO.findByPK(ssub.getStudyId());
				if (null != studyBeanSub) {
					parentStudyForSubject = studyBeanSub.getParentStudyId();
				}
				if (ssub.getStudyId() != currentStudy.getId() && currentStudy.getId() != parentStudyForSubject) {
					addPageMessage(noAccessMessage);
					throw new InsufficientPermissionException(Page.MENU_SERVLET, exceptionName, "1");
				}

			}
			if (itemId > 0) {
				ItemBean item = (ItemBean) new ItemDAO(sm.getDataSource()).findByPK(itemId);
				dnb.setEntityName(item.getName());
				request.setAttribute("item", item);
			}
			dnb.setEntityType(entityType);
			dnb.setColumn(column);
			dnb.setEntityId(entityId);
			dnb.setField(field);
			dnb.setParentDnId(parent.getId());
			dnb.setCreatedDate(new Date());

			if (parent.getId() == 0 || isNew) {// no parent, new note thread
				if (enteringData) {
					if (isInError) {
						dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.ANNOTATION.getId()); // ClinCapture #42

						dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
					} else {
						dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.ANNOTATION.getId());
						dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
					}
					if (isReasonForChange) {
						ArrayList<DnDescription> siteVisibleDescs = new ArrayList<DnDescription>();
						ArrayList<DnDescription> studyVisibleDescs = new ArrayList<DnDescription>();
						DnDescriptionDao descriptionDao = new DnDescriptionDao(sm.getDataSource());
						int parentStudyId = currentStudy.getParentStudyId() > 0 ? currentStudy.getParentStudyId() : currentStudy.getId();
						ArrayList<DnDescription> rfcDescriptions = (ArrayList<DnDescription>) descriptionDao.findAllByStudyId(parentStudyId);
						for (DnDescription rfcTerm : rfcDescriptions) {
							if (rfcTerm.isSiteVisible()) {
								siteVisibleDescs.add(rfcTerm);
							} else {
								studyVisibleDescs.add(rfcTerm);
							}
						}
						
						if (currentStudy.getParentStudyId() > 0) {
							dnDescriptions = siteVisibleDescs;
						} else {
							dnDescriptions = studyVisibleDescs;
						}

						dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.ANNOTATION.getId()); // ClinCapture #42

						dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
					}
					request.setAttribute("autoView", "0");
					// above set to automatically open up the user panel
				} else {
					dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.QUERY.getId());
					if (currentRole.getRole().equals(Role.CLINICAL_RESEARCH_COORDINATOR)
							|| currentRole.getRole().equals(Role.INVESTIGATOR)) {
						request.setAttribute("autoView", "0");
					} else {
						request.setAttribute("autoView", "1");
						dnb.setAssignedUserId(preUserId);
					}
				}

			}

			else if (parent.getDiscrepancyNoteTypeId() > 0) {
				dnb.setDiscrepancyNoteTypeId(parent.getDiscrepancyNoteTypeId());

				// if it is a CRC then we should automatically propose a
				// solution, tbh

				if (currentRole.getRole().equals(Role.CLINICAL_RESEARCH_COORDINATOR)
						&& currentStudy.getId() != currentStudy.getParentStudyId()) {
					dnb.setResolutionStatusId(ResolutionStatus.RESOLVED.getId());
					request.setAttribute("autoView", "0");
					// hide the panel, tbh
				} else {
					dnb.setResolutionStatusId(ResolutionStatus.UPDATED.getId());
				}

			}

			// ClinCapture #42
			if (actualStatusesList.size() == 1 && actualStatusesList.get(0).equals(ResolutionStatus.NOT_APPLICABLE)) {
				dnb.setDiscrepancyNoteTypeId(DiscrepancyNoteType.ANNOTATION.getId());
				dnb.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
			}
			dnb.setOwnerId(parent.getOwnerId());
			String detailedDes = fp.getString("strErrMsg");
			if (detailedDes != null) {
				dnb.setDetailedNotes(detailedDes);
				logger.debug("found strErrMsg: " + fp.getString("strErrMsg"));
			}

			// If the data entry form has not been saved yet, collecting info from parent page.
			dnb = getNoteInfo(dnb);// populate note infos
			if (dnb.getEventName() == null || dnb.getEventName().equals("")) {
				dnb.setEventName(fp.getString("eventName"));
			}
			if (dnb.getEventStart() == null) {
				dnb.setEventStart(fp.getDate("eventDate"));
			}
			if (dnb.getCrfName() == null || dnb.getCrfName().equals("")) {
				dnb.setCrfName(fp.getString("crfName"));
			}
			// // #4346 TBH 10/2009
			request.setAttribute(DIS_NOTE, dnb);
			request.setAttribute("unlock", "0");
			request.setAttribute(WRITE_TO_DB, writeToDB ? "1" : "0");// this should go from UI & here
			ArrayList userAccounts = this.generateUserAccounts(ub.getActiveStudyId(), subjectId);
			request.setAttribute(USER_ACCOUNTS, userAccounts);
			request.setAttribute("dnDescriptions", dnDescriptions);

			// ideally should be only two cases
			if (currentRole.getRole().equals(Role.CLINICAL_RESEARCH_COORDINATOR)
					&& currentStudy.getId() != currentStudy.getParentStudyId()) {
				// assigning back to OP, tbh
				request.setAttribute(USER_ACCOUNT_ID, Integer.valueOf(parent.getOwnerId()).toString());
				logger.debug("assigned owner id: " + parent.getOwnerId());
			} else if (dnb.getEventCRFId() > 0) {
				logger.debug("found a event crf id: " + dnb.getEventCRFId());
				EventCRFDAO eventCrfDAO = new EventCRFDAO(sm.getDataSource());
				EventCRFBean eventCrfBean = new EventCRFBean();
				eventCrfBean = (EventCRFBean) eventCrfDAO.findByPK(dnb.getEventCRFId());
				request.setAttribute(USER_ACCOUNT_ID, Integer.valueOf(eventCrfBean.getOwnerId()).toString());
				logger.debug("assigned owner id: " + eventCrfBean.getOwnerId());
			} else {
				// the end case

			}

			session.setAttribute("cdn_eventCRFId", fp.getString("eventCRFId"));
			session.setAttribute("cdn_groupOid", fp.getString("groupOid"));
			session.setAttribute("cdn_itemId", itemId);
			session.setAttribute("cdn_order", fp.getString("order"));

			// set the user account id for the user who completed data entry
			forwardPage(Page.ADD_DISCREPANCY_NOTE);

		} else {
			FormDiscrepancyNotes noteTree = (FormDiscrepancyNotes) session.getAttribute(FORM_DISCREPANCY_NOTES_NAME);

			if (noteTree == null) {
				noteTree = new FormDiscrepancyNotes();
				logger.debug("No note tree initailized in session");
			}

			Validator v = new Validator(request);
			String description = fp.getString("description");
			int typeId = fp.getInt("typeId");
			int assignedUserAccountId = fp.getInt(SUBMITTED_USER_ACCOUNT_ID);
			int resStatusId = fp.getInt(RES_STATUS_ID);
			String detailedDes = fp.getString("detailedDes");
			int sectionId = fp.getInt("sectionId");
			DiscrepancyNoteBean note = new DiscrepancyNoteBean();
			v.addValidation("description", Validator.NO_BLANKS);
			v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON,
					NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
			v.addValidation("detailedDes", Validator.LENGTH_NUMERIC_COMPARISON,
					NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);

			v.addValidation("typeId", Validator.NO_BLANKS);

			HashMap errors = v.validate();
			note.setDescription(description);
			note.setDetailedNotes(detailedDes);
			note.setOwner(ub);
			note.setOwnerId(ub.getId());
			note.setCreatedDate(new Date());
			note.setResolutionStatusId(resStatusId);
			note.setDiscrepancyNoteTypeId(typeId);

			// Annotations associated with an edit check to a Failed Validation (FV) note
			// Annotation associated with a field changed under Administrative Editing
			// (after a CRF is marked complete) to a Reason For Change note.
			isReasonForChange = fp.getBoolean(IS_REASON_FOR_CHANGE);
			if (DiscrepancyNoteType.get(typeId) == DiscrepancyNoteType.ANNOTATION) {
				note.setAssignedUserId(preUserId);
				if ("itemdata".equalsIgnoreCase(entityType)) {
					if (isReasonForChange) {
						typeId = DiscrepancyNoteType.REASON_FOR_CHANGE.getId();
						note.setDisType(DiscrepancyNoteType.REASON_FOR_CHANGE);
						note.setDiscrepancyNoteTypeId(typeId);

						resStatusId = ResolutionStatus.NOT_APPLICABLE.getId();
						note.setResStatus(ResolutionStatus.NOT_APPLICABLE);
						note.setResolutionStatusId(resStatusId);
					} else if (isInError) {
						typeId = DiscrepancyNoteType.FAILEDVAL.getId();
						note.setDisType(DiscrepancyNoteType.FAILEDVAL);
						note.setDiscrepancyNoteTypeId(typeId);

						resStatusId = ResolutionStatus.NOT_APPLICABLE.getId();
						note.setResStatus(ResolutionStatus.NOT_APPLICABLE);
						note.setResolutionStatusId(resStatusId);
					}
				}
			}

			note.setParentDnId(parent.getId());
			if (typeId == 1) { // <- failed validation check
				note.setAssignedUser(ub);
				note.setAssignedUserId(ub.getId());
			}

			if (typeId != DiscrepancyNoteType.ANNOTATION.getId() && typeId != DiscrepancyNoteType.FAILEDVAL.getId()
					&& typeId != DiscrepancyNoteType.REASON_FOR_CHANGE.getId()) {
				if (assignedUserAccountId > 0) {
					note.setAssignedUserId(assignedUserAccountId);
					logger.debug("^^^ found assigned user id: " + assignedUserAccountId);

				} else {
					// a little bit of a workaround, should ideally be always from
					// the form
					note.setAssignedUserId(parent.getOwnerId());
					logger.debug("found user assigned id, in the PARENT OWNER ID: " + parent.getOwnerId()
							+ " note that user assgined id did not work: " + assignedUserAccountId);
				}
			}

			note.setField(field);
			if (DiscrepancyNoteType.ANNOTATION.getId() == note.getDiscrepancyNoteTypeId()) {
				updateStudyEvent(entityType, entityId);
				updateStudySubjectStatus(entityType, entityId);
			}
			if (DiscrepancyNoteType.ANNOTATION.getId() == note.getDiscrepancyNoteTypeId()
					|| DiscrepancyNoteType.REASON_FOR_CHANGE.getId() == note.getDiscrepancyNoteTypeId()
					|| DiscrepancyNoteType.FAILEDVAL.getId() == note.getDiscrepancyNoteTypeId()) {

				note.setResStatus(ResolutionStatus.NOT_APPLICABLE);
				note.setResolutionStatusId(ResolutionStatus.NOT_APPLICABLE.getId());
			}
			if (DiscrepancyNoteType.QUERY.getId() == note.getDiscrepancyNoteTypeId()) {
				if (ResolutionStatus.NOT_APPLICABLE.getId() == note.getResolutionStatusId()) {
					Validator.addError(errors, RES_STATUS_ID, restext.getString("not_valid_res_status"));
				}
			}

			if (!parent.isActive()) {
				note.setEntityId(entityId);
				note.setEntityType(entityType);
				note.setColumn(column);
			} else {
				note.setEntityId(parent.getEntityId());
				note.setEntityType(parent.getEntityType());
				if (!StringUtil.isBlank(parent.getColumn())) {
					note.setColumn(parent.getColumn());
				} else {
					note.setColumn(column);
				}
				note.setParentDnId(parent.getId());
			}

			StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
			StudySubjectBean ssub = (StudySubjectBean) ssdao.findByPK(subjectId);
			note.setStudyId(ssub.getStudyId());

			note = getNoteInfo(note);// populate note infos

			request.setAttribute(DIS_NOTE, note);
			request.setAttribute(WRITE_TO_DB, writeToDB ? "1" : "0");// this should go from UI & here
			ArrayList userAccounts = this.generateUserAccounts(ub.getActiveStudyId(), subjectId);

			request.setAttribute(USER_ACCOUNT_ID, Integer.valueOf(note.getAssignedUserId()).toString());
			// formality more than anything else, we should go to say the note
			// is done

			Role r = currentRole.getRole();
			if (r.equals(Role.STUDY_MONITOR) || r.equals(Role.INVESTIGATOR) || r.equals(Role.CLINICAL_RESEARCH_COORDINATOR)
					|| r.equals(Role.STUDY_ADMINISTRATOR)) { // investigator
				request.setAttribute("unlock", "1");
				logger.debug("set UNLOCK to ONE");
			} else {
				request.setAttribute("unlock", "0");
				logger.debug("set UNLOCK to ZERO");
			}

			request.setAttribute(USER_ACCOUNTS, userAccounts);

			if (errors.isEmpty()) {
				if (!isWritingIntoDBAllowed(writeToDB, session, note)) {
					noteTree.addNote(field, note);
					noteTree.addIdNote(note.getEntityId(), field);
					session.setAttribute(FORM_DISCREPANCY_NOTES_NAME, noteTree);
					//
					/*
					 * Setting a marker to check later while saving administrative edited data. This is needed to make
					 * sure the system flags error while changing data for items which already has a DiscrepanyNote
					 */
					manageReasonForChangeState(session, field);
					forwardPage(Page.ADD_DISCREPANCY_NOTE_DONE);
				} else {
					// if not creating a new thread(note), update exsiting notes
					// if necessary
					// if ("itemData".equalsIgnoreCase(entityType) && !isNew) {
					int pdnId = note != null ? note.getParentDnId() : 0;
					if (pdnId > 0) {
						logger.debug("Create:find parent note for item data:" + note.getEntityId());

						DiscrepancyNoteBean pNote = (DiscrepancyNoteBean) dndao.findByPK(pdnId);

						logger.debug("setting DN owner id: " + pNote.getOwnerId());

						note.setOwnerId(pNote.getOwnerId());

						if (note.getDiscrepancyNoteTypeId() == pNote.getDiscrepancyNoteTypeId()) {

							if (note.getResolutionStatusId() != pNote.getResolutionStatusId()) {
								pNote.setResolutionStatusId(note.getResolutionStatusId());
								dndao.update(pNote);
							}

							if (note.getAssignedUserId() != pNote.getAssignedUserId()) {
								pNote.setAssignedUserId(note.getAssignedUserId());
								if (pNote.getAssignedUserId() > 0) {
									dndao.updateAssignedUser(pNote);
								} else {
									dndao.updateAssignedUserToNull(pNote);
								}
							}

						}

					}

					int dnTypeId = note.getDiscrepancyNoteTypeId();
					if ("itemData".equalsIgnoreCase(entityType)
							&& (DiscrepancyNoteType.FAILEDVAL.getId() == dnTypeId || DiscrepancyNoteType.QUERY.getId() == dnTypeId)
							&& note.getAssignedUserId() == 0) {

						DataSource dataSource = sm.getDataSource();
						ItemDataBean itemData = (ItemDataBean) new ItemDataDAO(dataSource).findByPK(entityId);
						EventCRFBean ec = (EventCRFBean) new EventCRFDAO(sm.getDataSource()).findByPK(itemData
								.getEventCRFId());
						int userToAssignId = ec.getUpdaterId() == 0 ? ec.getOwnerId() : ec.getUpdaterId();
						note.setAssignedUserId(userToAssignId);
					}

					note = (DiscrepancyNoteBean) dndao.create(note);

					dndao.createMapping(note);

					request.setAttribute(DIS_NOTE, note);

					if (note.getParentDnId() == 0) {
						note.setParentDnId(note.getId());
						note = (DiscrepancyNoteBean) dndao.create(note);
						dndao.createMapping(note);
					}

					manageReasonForChangeState(session, field);

					logger.debug("found resolution status: " + note.getResolutionStatusId());

					String email = fp.getString(EMAIL_USER_ACCOUNT);

					logger.debug("found email: " + email);
					if (note.getAssignedUserId() > 0 && "1".equals(email.trim())
							&& DiscrepancyNoteType.QUERY.getId() == note.getDiscrepancyNoteTypeId()) {

						logger.debug("++++++ found our way here: " + note.getDiscrepancyNoteTypeId()
								+ " id number and " + note.getDisType().getName());
						// generate email for user here
						StringBuffer message = new StringBuffer();

						// generate message here
						UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
						ItemDAO itemDAO = new ItemDAO(sm.getDataSource());
						ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
						ItemBean item = new ItemBean();
						ItemDataBean itemData = new ItemDataBean();
						SectionBean section = new SectionBean();

						StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
						UserAccountBean assignedUser = (UserAccountBean) userAccountDAO.findByPK(note
								.getAssignedUserId());
						String alertEmail = assignedUser.getEmail();
						message.append(MessageFormat.format(respage.getString("mailDNHeader"),
								assignedUser.getFirstName(), assignedUser.getLastName()));
						message.append("<A HREF='" + SQLInitServlet.getSystemURL()
								+ "ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user="
								+ assignedUser.getName() + "&listNotes_f_entityName=" + note.getEntityName() + "'>"
								+ SQLInitServlet.getField("sysURL") + "</A><BR/>");
						message.append(respage.getString("you_received_this_from"));
						StudyBean study = (StudyBean) studyDAO.findByPK(note.getStudyId());
						SectionDAO sectionDAO = new SectionDAO(sm.getDataSource());

						if ("itemData".equalsIgnoreCase(entityType)) {
							itemData = (ItemDataBean) iddao.findByPK(note.getEntityId());
							item = (ItemBean) itemDAO.findByPK(itemData.getItemId());
							if (sectionId > 0) {
								section = (SectionBean) sectionDAO.findByPK(sectionId);
							} 
						}

						message.append(respage.getString("email_body_separator"));
						message.append(respage.getString("disc_note_info"));
						message.append(respage.getString("email_body_separator"));
						message.append(MessageFormat.format(respage.getString("mailDNParameters1"),
								note.getDescription(), note.getDetailedNotes(), ub.getName()));
						message.append(respage.getString("email_body_separator"));
						message.append(respage.getString("entity_information"));
						message.append(respage.getString("email_body_separator"));
						message.append(MessageFormat.format(respage.getString("mailDNParameters2"), study.getName(),
								note.getSubjectName()));

						if (!("studySub".equalsIgnoreCase(entityType) || "subject".equalsIgnoreCase(entityType))) {
							message.append(MessageFormat.format(respage.getString("mailDNParameters3"),
									note.getEventName()));
							if (!"studyEvent".equalsIgnoreCase(note.getEntityType())) {
								message.append(MessageFormat.format(respage.getString("mailDNParameters4"),
										note.getCrfName()));
								if (!"eventCrf".equalsIgnoreCase(note.getEntityType())) {
									if (sectionId > 0) {
										message.append(MessageFormat.format(respage.getString("mailDNParameters5"),
												section.getName()));
									}
									message.append(MessageFormat.format(respage.getString("mailDNParameters6"),
											item.getName()));
								}
							}
						}

						message.append(respage.getString("email_body_separator"));
						message.append(MessageFormat.format(respage.getString("mailDNThanks"), study.getName()));
						message.append(respage.getString("email_body_separator"));
						message.append(respage.getString("disclaimer"));
						message.append(respage.getString("email_body_separator"));
						message.append(respage.getString("email_footer"));

						String emailBodyString = message.toString();
						sendEmail(
								alertEmail.trim(),
								EmailEngine.getAdminEmail(),
								MessageFormat.format(respage.getString("mailDNSubject"), study.getName(),
										note.getEntityName()), emailBodyString, true, null, null, true);

					} else {
						logger.debug("did not send email, but did save DN");
					}
					addPageMessage(respage.getString("note_saved_into_db"));
					addPageMessage(respage.getString("page_close_automatically"));
					forwardPage(Page.ADD_DISCREPANCY_NOTE_SAVE_DONE);
				}

			} else {
				if (parentId > 0) {
					if (note.getResolutionStatusId() == ResolutionStatus.NOT_APPLICABLE.getId()) {
						request.setAttribute("autoView", "0");
					}
				} else {
					if (note.getDiscrepancyNoteTypeId() == DiscrepancyNoteType.QUERY.getId()) {
						request.setAttribute("autoView", "1");
					} else {
						request.setAttribute("autoView", "0");
					}
				}
				setInputMessages(errors);
				forwardPage(Page.ADD_DISCREPANCY_NOTE);
			}

		}

	}

	private boolean isWritingIntoDBAllowed(boolean writeToDB, HttpSession session, DiscrepancyNoteBean note) {
		boolean result = writeToDB;
		try {
			String groupOid = (String) session.getAttribute("cdn_groupOid");
			if (groupOid != null && !groupOid.trim().isEmpty()) {
				Integer eventCRFId = Integer.parseInt((String) session.getAttribute("cdn_eventCRFId"));
				Integer order = Integer.parseInt((String) session.getAttribute("cdn_order"));
				Integer itemId = (Integer) session.getAttribute("cdn_itemId");
				ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
				note.setEventCRFId(eventCRFId);
				ItemDataBean itemDataBean = iddao.findByItemIdAndEventCRFIdAndOrdinal(itemId, eventCRFId, ++order);
				result = !(itemDataBean == null || itemDataBean.getId() == 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Constructs a url for creating new note on 'view note list' page
	 * 
	 * @param note
	 * @param preset
	 * @return
	 */
	public static String getAddChildURL(DiscrepancyNoteBean note, ResolutionStatus preset, boolean toView) {
		ArrayList<String> arguments = new ArrayList<String>();

		arguments.add(ENTITY_TYPE + "=" + note.getEntityType());
		arguments.add(ENTITY_ID + "=" + note.getEntityId());
		arguments.add(WRITE_TO_DB + "=" + "1");
		arguments.add("monitor" + "=" + 1);// of course, when resolving a note,
		// we have monitor privilege

		if (preset.isActive()) {
			arguments.add(PRESET_RES_STATUS + "=" + String.valueOf(preset.getId()));
		}

		if (toView) {
			String columnValue = "".equalsIgnoreCase(note.getColumn()) ? "value" : note.getColumn();
			arguments.add(ENTITY_COLUMN + "=" + columnValue);
			arguments.add(SUBJECT_ID + "=" + note.getSubjectId());
			arguments.add(ITEM_ID + "=" + note.getItemId());
			String queryString = StringUtil.join("&", arguments);
			return "ViewDiscrepancyNote?" + queryString;
		} else {
			arguments.add(PARENT_ID + "=" + note.getId());
			String queryString = StringUtil.join("&", arguments);
			return "CreateDiscrepancyNote?" + queryString;
		}
	}

	/**
	 * Pulls the note related information from database according to note type
	 */

	private void updateStudySubjectStatus(String entityType, int entityId) {
		if ("itemData".equalsIgnoreCase(entityType)) {
			int itemDataId = entityId;
			ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
			ItemDataBean itemData = (ItemDataBean) iddao.findByPK(itemDataId);
			EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
			StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());
			StudySubjectDAO studySubjectDAO = new StudySubjectDAO(sm.getDataSource());
			EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
			StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());
			StudySubjectBean studySubject = (StudySubjectBean) studySubjectDAO.findByPK(event.getStudySubjectId());
			if (studySubject.getStatus() != null && studySubject.getStatus().equals(Status.SIGNED)) {
				studySubject.setStatus(Status.AVAILABLE);
				studySubject.setUpdater(ub);
				studySubject.setUpdatedDate(new Date());
				studySubjectDAO.update(studySubject);
			}
			if (ec.isSdvStatus()) {
				studySubject.setStatus(Status.AVAILABLE);
				studySubject.setUpdater(ub);
				studySubject.setUpdatedDate(new Date());
				studySubjectDAO.update(studySubject);
				ec.setSdvStatus(false);
				ec.setSdvUpdateId(ub.getId());
				ecdao.update(ec);
			}

		}
	}

	private ArrayList generateUserAccounts(int studyId, int subjectId) {
		UserAccountDAO userAccountDAO = new UserAccountDAO(sm.getDataSource());
		StudyDAO studyDAO = new StudyDAO(sm.getDataSource());
		StudyBean subjectStudy = studyDAO.findByStudySubjectId(subjectId);
		ArrayList userAccounts = new ArrayList();
		if (currentStudy.getParentStudyId() > 0) {
			userAccounts = userAccountDAO
					.findAllUsersByStudyOrSite(studyId, currentStudy.getParentStudyId(), subjectId);
		} else if (subjectStudy.getParentStudyId() > 0) {
			userAccounts = userAccountDAO.findAllUsersByStudyOrSite(subjectStudy.getId(),
					subjectStudy.getParentStudyId(), subjectId);
		} else {
			userAccounts = userAccountDAO.findAllUsersByStudyOrSite(studyId, 0, subjectId);
		}
		return userAccounts;
	}

	private void updateStudyEvent(String entityType, int entityId) {
		if ("itemData".equalsIgnoreCase(entityType)) {
			int itemDataId = entityId;
			ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource());
			ItemDataBean itemData = (ItemDataBean) iddao.findByPK(itemDataId);
			EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
			StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());
			EventCRFBean ec = (EventCRFBean) ecdao.findByPK(itemData.getEventCRFId());
			StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());
			if (event.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)) {
				event.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
				event.setUpdater(ub);
				event.setUpdatedDate(new Date());
				svdao.update(event);
			}
		} else if ("eventCrf".equalsIgnoreCase(entityType)) {
			int eventCRFId = entityId;
			EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
			StudyEventDAO svdao = new StudyEventDAO(sm.getDataSource());

			EventCRFBean ec = (EventCRFBean) ecdao.findByPK(eventCRFId);
			StudyEventBean event = (StudyEventBean) svdao.findByPK(ec.getStudyEventId());
			if (event.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)) {
				event.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
				event.setUpdater(ub);
				event.setUpdatedDate(new Date());
				svdao.update(event);
			}
		}
	}

	private void manageReasonForChangeState(HttpSession session, String itemDataBeanId) {
		HashMap<String, Boolean> noteSubmitted = (HashMap<String, Boolean>) session
				.getAttribute(DataEntryServlet.NOTE_SUBMITTED);
		if (noteSubmitted == null) {
			noteSubmitted = new HashMap<String, Boolean>();
		}
		noteSubmitted.put(itemDataBeanId, Boolean.TRUE);
		session.setAttribute(DataEntryServlet.NOTE_SUBMITTED, noteSubmitted);
	}

	private int isWriteToDB(int isGroup, String field, int item_data_id, int item_id,
			int ordinal_for_repeating_group_field, int event_crf_id) {
		if (item_data_id > 0 && isGroup == -1) {// non repeating group; coming from showItemInput.jsp
			return 1;
		} else if (item_data_id < 0 && isGroup == -1) {// non repeating group; coming from showItemInput.jsp
			return -1;
		} else if (isGroup == 1) {// repeating group;
			// initial data entry or if template cell is empty (last row)
			if (item_data_id < 0) {
				return -1;
			}
			if (item_data_id > 0) {
				if (field.contains("_0input") || field.contains("manual")) {
					// get ordinal
					ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource(), locale);

					boolean isExistInDB = iddao.isItemExists(item_id, ordinal_for_repeating_group_field, event_crf_id);
					return (isExistInDB) ? 1 : -1;
				} else if (field.contains("input")) {
					return -1;
				}
			}
		}
		return 0;
	}

	public int calculateOrdinal(int isGroup, String field_name, int event_crf_id, int rowCount) {
		int ordinal = 0;
		int start = -1;
		int end = -1;
		if (isGroup == -1) {
			return 1;
		}
		if (field_name.contains("_0input")) {
			return 1;
		}
		try {
			if (field_name.contains("manual")) {
				start = field_name.indexOf("manual") + 5;
				end = field_name.indexOf("input");
				if (start == 4 || end == -1) {
					return 0;
				}
				ordinal = Integer.valueOf(field_name.substring(start + 1, end));
				return ordinal + 1;

			} else {
				// get max ordinal from DB
				ItemDataDAO iddao = new ItemDataDAO(sm.getDataSource(), locale);
				String[] field_name_items = field_name.split("_");

				String group_oid = field_name.substring(0,
						field_name.indexOf(field_name_items[field_name_items.length - 1]) - 1);
				int maxOrdinal = iddao.getMaxOrdinalForGroupByGroupOID(group_oid, event_crf_id);

				// get ordinal from field
				end = field_name.indexOf("input");
				start = field_name.lastIndexOf("_");
				if (end == -1 || start == -1) {
					return 0;
				}
				ordinal = Integer.valueOf(field_name.substring(start + 1, end));
				return ordinal + maxOrdinal + rowCount;
			}
		} catch (NumberFormatException e) {
			// DO NOTHING
		}

		return ordinal;

	}
}
