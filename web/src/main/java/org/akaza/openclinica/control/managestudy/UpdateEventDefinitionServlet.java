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
package org.akaza.openclinica.control.managestudy;

import com.clinovo.util.ValidatorHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.control.core.Controller;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.util.DAOWrapper;
import org.akaza.openclinica.util.SignStateRestorer;
import org.akaza.openclinica.util.SubjectEventStatusUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.stereotype.Component;

@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
@Component
public class UpdateEventDefinitionServlet extends Controller {

	@Override
	public void mayProceed(HttpServletRequest request, HttpServletResponse response) throws InsufficientPermissionException {
		UserAccountBean ub = getUserAccountBean(request);
		StudyUserRoleBean currentRole = getCurrentRole(request);

		checkStudyLocked(Page.LIST_DEFINITION_SERVLET, respage.getString("current_study_locked"), request, response);
		if (ub.isSysAdmin() || currentRole.getRole().equals(Role.STUDY_ADMINISTRATOR)) {
			return;
		}
		addPageMessage(respage.getString("no_have_permission_to_update_study_event_definition") + "<br>"
						+ respage.getString("change_study_contact_sysadmin"), request);
		throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET,
				resexception.getString("not_study_director"), "1");
	}

	@Override
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		checkReferenceVisit(request);
		String action = request.getParameter("action");
		if (StringUtil.isBlank(action)) {
			forwardPage(Page.UPDATE_EVENT_DEFINITION1, request, response);
		} else {
			if ("confirm".equalsIgnoreCase(action)) {
				confirmDefinition(request, response);

			} else if ("submit".equalsIgnoreCase(action)) {
				submitDefinition(request, response);

			} else if ("addCrfs".equalsIgnoreCase(action)) {
				FormProcessor fp = new FormProcessor(request);
				StudyEventDefinitionBean sed = (StudyEventDefinitionBean) request.getSession().getAttribute("definition");
				saveEventDefinitionToSession(sed, fp);
				saveEventDefinitionCRFsToSession(fp);
				response.sendRedirect(request.getContextPath() + "/AddCRFToDefinition");
			} else {
				addPageMessage(respage.getString("updating_ED_is_cancelled"), request);
				clearSession(request.getSession());
				forwardPage(Page.LIST_DEFINITION_SERVLET, request, response);
			}
		}

	}

	private void confirmDefinition(HttpServletRequest request, HttpServletResponse response) throws Exception {
		Validator v = new Validator(new ValidatorHelper(request, getConfigurationDao()));
		FormProcessor fp = new FormProcessor(request);
		HashMap errors = v.validate();

		StudyEventDefinitionBean sed = (StudyEventDefinitionBean) request.getSession().getAttribute("definition");
		v.addValidation("name", Validator.NO_BLANKS);
		v.addValidation("name", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
		v.addValidation("description", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);
		v.addValidation("category", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 2000);

		String calendaredVisitType = fp.getString("type");
		if ("calendared_visit".equalsIgnoreCase(calendaredVisitType)) {
			v.addValidation("maxDay", Validator.IS_REQUIRED);
			v.addValidation("maxDay", Validator.IS_A_FLOAT, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 3);
			v.addValidation("minDay", Validator.IS_REQUIRED);
			v.addValidation("minDay", Validator.IS_A_FLOAT, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 3);
			v.addValidation("schDay", Validator.IS_REQUIRED);
			v.addValidation("schDay", Validator.IS_A_FLOAT, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 3);
			if ("".equalsIgnoreCase(fp.getString("isReference"))) {
				v.addValidation("emailUser", Validator.NO_BLANKS);
			}
			v.addValidation("emailDay", Validator.IS_REQUIRED);
			v.addValidation("emailDay", Validator.IS_A_FLOAT, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 3);
			String showCalendarBox = fp.getString("type");
			if ("calendared_visit".equalsIgnoreCase(showCalendarBox)) {
				request.getSession().setAttribute("showCalendaredVisitBox", true);
			}
			String changedReference = fp.getString("isReference");
			if ("true".equalsIgnoreCase(changedReference)) {
				request.getSession().setAttribute("changedReference", true);
			} else {
				request.getSession().setAttribute("changedReference", false);
			}
		}

		int minDay = fp.getInt("minDay");
		int maxDay = fp.getInt("maxDay");
		int schDay = fp.getInt("schDay");
		int emailDay = fp.getInt("emailDay");
		String emailUser = fp.getString("emailUser");

		if (!(maxDay >= schDay)) {
			Validator.addError(errors, "maxDay", resexception.getString("daymax_greate_or_equal_dayschedule"));
		}
		if (!(minDay <= schDay)) {
			Validator.addError(errors, "minDay", resexception.getString("daymin_less_or_equal_dayschedule"));
		}
		if (!(minDay <= maxDay)) {
			Validator.addError(errors, "minDay", resexception.getString("daymin_less_or_equal_daymax"));
		}
		if (!(emailDay <= schDay)) {
			Validator.addError(errors, "emailDay", resexception.getString("dayemail_less_or_equal_dayschedule"));
		}
		if (!checkUserName(request, emailUser) && "calendared_visit".equalsIgnoreCase(calendaredVisitType)
				&& "".equalsIgnoreCase(fp.getString("isReference"))) {
			Validator.addError(errors, "emailUser", resexception.getString("this_user_name_does_not_exist"));
		}

		if (!errors.isEmpty()) {
			StudyEventDefinitionBean sedForErrors = new StudyEventDefinitionBean();
			sedForErrors.setName(fp.getString("name"));
			sedForErrors.setDescription(fp.getString("description"));
			sedForErrors.setCategory(fp.getString("category"));
			sedForErrors.setType(fp.getString("type"));
			sedForErrors.setRepeating(fp.getBoolean("repeating"));
			sedForErrors.setMaxDay(fp.getInt("maxDay"));
			sedForErrors.setMinDay(fp.getInt("minDay"));
			sedForErrors.setScheduleDay(fp.getInt("schDay"));
			String referenceVisitValue = fp.getString("isReference");
			if ("true".equalsIgnoreCase(referenceVisitValue)) {
				sedForErrors.setReferenceVisit(true);
			} else {
				sedForErrors.setReferenceVisit(false);
			}
			request.setAttribute("userNameInsteadEmail", fp.getString("emailUser"));
			request.setAttribute("definition", sedForErrors);
			logger.info("has errors");
			request.setAttribute("formMessages", errors);
			forwardPage(Page.UPDATE_EVENT_DEFINITION1, request, response);

		} else {
			logger.info("no errors");
			saveEventDefinitionToSession(sed, fp);
		}

		saveEventDefinitionCRFsToSession(fp);

		forwardPage(Page.UPDATE_EVENT_DEFINITION_CONFIRM, request, response);
	}

	private void submitDefinition(HttpServletRequest request, HttpServletResponse response) {
		UserAccountBean ub = getUserAccountBean(request);
		StudyBean currentStudy = getCurrentStudy(request);

		ArrayList edcs = (ArrayList) request.getSession().getAttribute("eventDefinitionCRFs");
		ArrayList childEdcs = (ArrayList) request.getSession().getAttribute("childEventDefCRFs");

		SignStateRestorer signStateRestorer = (SignStateRestorer) request.getSession().getAttribute("signStateRestorer");

		StudyEventDefinitionBean sed = (StudyEventDefinitionBean) request.getSession().getAttribute("definition");
		logger.info("Definition bean to be updated:" + sed.getName() + sed.getCategory());

		StudyDAO sdao = new StudyDAO(getDataSource());
		StudyEventDAO sedao = new StudyEventDAO(getDataSource());
		EventCRFDAO eventCRFDAO = new EventCRFDAO(getDataSource());
		StudyEventDefinitionDAO edao = getStudyEventDefinitionDAO();
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(getDataSource());
		EventDefinitionCRFDAO eventDefinitionCrfDAO = new EventDefinitionCRFDAO(getDataSource());
		DAOWrapper daoWrapper = new DAOWrapper(sdao, sedao, studySubjectDAO, eventCRFDAO, eventDefinitionCrfDAO, dndao);

		StudyBean study = (StudyBean) sdao.findByPK(sed.getStudyId());

		sed.setUpdater(ub);
		sed.setUpdatedDate(new Date());
		sed.setStatus(Status.AVAILABLE);
		edao.update(sed);

		EventDefinitionCRFDAO cdao = getEventDefinitionCRFDAO();

		for (Object edc1 : edcs) {
			EventDefinitionCRFBean edc = (EventDefinitionCRFBean) edc1;
			if (edc.getId() > 0) {
				edc.setUpdater(ub);
				edc.setUpdatedDate(new Date());
				logger.info("Status:" + edc.getStatus().getName());
				logger.info("version:" + edc.getDefaultVersionId());
				logger.info("Electronic Signature [" + edc.isElectronicSignature() + "]");
				cdao.update(edc);

				if (edc.getStatus().equals(Status.DELETED) || edc.getStatus().equals(Status.AUTO_DELETED)) {
					removeAllEventsItems(currentStudy, ub, edc, sed);
				}
				if (edc.getOldStatus() != null && edc.getOldStatus().equals(Status.DELETED)) {
					restoreAllEventsItems(request, edc, sed);
				}

			} else {
				edc.setOwner(ub);
				edc.setCreatedDate(new Date());
				edc.setStatus(Status.AVAILABLE);
				cdao.create(edc);

			}
		}

		updateChildEdcs(request, childEdcs, ub, cdao, currentStudy, sed);

		ArrayList<StudyEventBean> studyEventList = (ArrayList<StudyEventBean>) sedao
				.findAllByStudyAndEventDefinitionIdExceptLockedSkippedStoppedRemoved(study, sed.getId());
		SubjectEventStatusUtil.determineSubjectEventStates(studyEventList, study, daoWrapper, signStateRestorer);

		clearSession(request.getSession());

		addPageMessage(respage.getString("the_ED_has_been_updated_succesfully"), request);
		forwardPage(Page.LIST_DEFINITION_SERVLET, request, response);
	}

	private void updateChildEdcs(HttpServletRequest request, ArrayList childEdcs, UserAccountBean ub, EventDefinitionCRFDAO cdao,
			StudyBean currentStudy, StudyEventDefinitionBean sed) {
		for (Object childEdc1 : childEdcs) {
			EventDefinitionCRFBean childEdc = (EventDefinitionCRFBean) childEdc1;
			if (childEdc.getId() > 0) {
				childEdc.setUpdater(ub);
				childEdc.setUpdatedDate(new Date());
				cdao.update(childEdc);

				if (childEdc.getStatus().equals(Status.DELETED) || childEdc.getStatus().equals(Status.AUTO_DELETED)) {
					removeAllEventsItems(currentStudy, ub, childEdc, sed);
				}
				if (childEdc.getOldStatus() != null && childEdc.getOldStatus().equals(Status.DELETED)) {
					restoreAllEventsItems(request, childEdc, sed);
				}
			}
		}
	}

	public void removeAllEventsItems(StudyBean currentStudy, UserAccountBean ub, EventDefinitionCRFBean edc, StudyEventDefinitionBean sed) {
		StudyEventDAO seDao = new StudyEventDAO(getDataSource());
		EventCRFDAO ecrfDao = new EventCRFDAO(getDataSource());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());

		ArrayList seList = seDao.findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), edc.getCrf().getOid());
		for (Object aSeList : seList) {
			StudyEventBean seBean = (StudyEventBean) aSeList;

			ArrayList ecrfList = ecrfDao.findAllByStudyEventAndCrfOrCrfVersionOid(seBean, edc.getCrf().getOid());
			for (Object anEcrfList : ecrfList) {
				EventCRFBean ecrfBean = (EventCRFBean) anEcrfList;
				ecrfBean.setOldStatus(ecrfBean.getStatus());
				ecrfBean.setStatus(Status.AUTO_DELETED);
				ecrfBean.setUpdater(ub);
				ecrfBean.setUpdatedDate(new Date());
				ecrfDao.update(ecrfBean);

				ArrayList itemData = iddao.findAllByEventCRFId(ecrfBean.getId());
				// remove all the item data
				for (Object anItemData : itemData) {
					ItemDataBean item = (ItemDataBean) anItemData;
					if (!item.getStatus().equals(Status.DELETED)) {
						item.setOldStatus(item.getStatus());
						item.setStatus(Status.AUTO_DELETED);
						item.setUpdater(ub);
						item.setUpdatedDate(new Date());
						iddao.update(item);
						DiscrepancyNoteDAO dnDao = getDiscrepancyNoteDAO();
						List dnNotesOfRemovedItem = dnDao.findExistingNotesForItemData(item.getId());
						if (!dnNotesOfRemovedItem.isEmpty()) {
							DiscrepancyNoteBean itemParentNote = null;
							for (Object obj : dnNotesOfRemovedItem) {
								if (((DiscrepancyNoteBean) obj).getParentDnId() == 0) {
									itemParentNote = (DiscrepancyNoteBean) obj;
								}
							}
							DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
							if (itemParentNote != null) {
								dnb.setParentDnId(itemParentNote.getId());
								dnb.setDiscrepancyNoteTypeId(itemParentNote.getDiscrepancyNoteTypeId());
							}
							dnb.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
							dnb.setStudyId(currentStudy.getId());
							dnb.setAssignedUserId(ub.getId());
							dnb.setOwner(ub);
							dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
							dnb.setEntityId(item.getId());
							dnb.setColumn("value");
							dnb.setCreatedDate(new Date());
							dnb.setDescription("The item has been removed, this Discrepancy Note has been Closed.");
							dnDao.create(dnb);
							dnDao.createMapping(dnb);
							if (itemParentNote != null) {
								itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
							}
							dnDao.update(itemParentNote);
						}
					}
				}
			}
		}
	}

	public void restoreAllEventsItems(HttpServletRequest request, EventDefinitionCRFBean edc,
			StudyEventDefinitionBean sed) {
		UserAccountBean ub = getUserAccountBean(request);

		StudyEventDAO seDao = getStudyEventDAO();
		EventCRFDAO ecrfDao = getEventCRFDAO();
		ItemDataDAO iddao = getItemDataDAO();

		ArrayList seList = seDao.findAllByStudyEventDefinitionAndCrfOids(sed.getOid(), edc.getCrf().getOid());
		for (Object aSeList : seList) {
			StudyEventBean seBean = (StudyEventBean) aSeList;

			ArrayList ecrfList = ecrfDao.findAllByStudyEventAndCrfOrCrfVersionOid(seBean, edc.getCrf().getOid());
			for (Object anEcrfList : ecrfList) {
				EventCRFBean ecrfBean = (EventCRFBean) anEcrfList;
				ecrfBean.setStatus(ecrfBean.getOldStatus());
				ecrfBean.setUpdater(ub);
				ecrfBean.setUpdatedDate(new Date());
				ecrfDao.update(ecrfBean);

				ArrayList itemData = iddao.findAllByEventCRFId(ecrfBean.getId());

				for (Object anItemData : itemData) {
					ItemDataBean item = (ItemDataBean) anItemData;
					if (item.getStatus().equals(Status.DELETED) || item.getStatus().equals(Status.AUTO_DELETED)) {
						item.setStatus(item.getOldStatus());
						item.setUpdater(ub);
						item.setUpdatedDate(new Date());
						iddao.update(item);
					}
				}
			}
		}
	}

	private void checkReferenceVisit(HttpServletRequest request) {
		StudyEventDefinitionDAO seddao = getStudyEventDefinitionDAO();
		ArrayList<StudyEventDefinitionBean> definitions = seddao.findReferenceVisitBeans();
		for (StudyEventDefinitionBean studyEventDefinition : definitions) {
			if (studyEventDefinition.getReferenceVisit()) {
				logger.trace("Reference visit already exist");
				request.getSession().setAttribute("referenceVisitAlreadyExist", true);
				break;
			}
		}
	}

	private boolean checkUserName(HttpServletRequest request, String emailUser) {
		StudyBean currentStudy = getCurrentStudy(request);
		boolean isValid = false;
		UserAccountDAO uadao = getUserAccountDAO();
		ArrayList<StudyUserRoleBean> userBean = uadao.findAllByStudyId(currentStudy.getId());
		for (StudyUserRoleBean userAccountBean : userBean) {
			if (emailUser.equals(userAccountBean.getUserName())) {
				isValid = true;
				break;
			}
		}
		return isValid;
	}

	private int getIdByUserName(String userName) {
		UserAccountDAO uadao = getUserAccountDAO();
		UserAccountBean userBean = (UserAccountBean) uadao.findByUserName(userName);
		return userBean.getId();
	}

	private void saveEventDefinitionCRFsToSession(FormProcessor fp) {
		ArrayList edcs = (ArrayList) fp.getRequest().getSession().getAttribute("eventDefinitionCRFs");
		CRFVersionDAO cvdao = new CRFVersionDAO(getDataSource());
		for (int i = 0; i < edcs.size(); i++) {
			EventDefinitionCRFBean edcBean = (EventDefinitionCRFBean) edcs.get(i);
			if (!edcBean.getStatus().equals(Status.DELETED) && !edcBean.getStatus().equals(Status.AUTO_DELETED)) {
				int defaultVersionId = fp.getInt("defaultVersionId" + i);
				edcBean.setDefaultVersionId(defaultVersionId);
				CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
				edcBean.setDefaultVersionName(defaultVersion.getName());

				String requiredCRF = fp.getString("requiredCRF" + i);
				String doubleEntry = fp.getString("doubleEntry" + i);
				String decisionCondition = fp.getString("decisionCondition" + i);
				String electronicSignature = fp.getString("electronicSignature" + i);
				String hideCRF = fp.getString("hideCRF" + i);
				int sdvId = fp.getInt("sdvOption" + i);
				String emailStep = fp.getString("emailOnStep" + i);
				String emailTo = fp.getString("mailTo" + i);
				String evaluatedCrf = fp.getString("evaluatedCRF" + i);

				if (!StringUtil.isBlank(hideCRF) && "yes".equalsIgnoreCase(hideCRF.trim())) {
					edcBean.setHideCrf(true);
				} else {
					edcBean.setHideCrf(false);
				}
				if (!StringUtil.isBlank(requiredCRF) && "yes".equalsIgnoreCase(requiredCRF.trim())) {
					edcBean.setRequiredCRF(true);
				} else {
					edcBean.setRequiredCRF(false);
				}

				if (!StringUtil.isBlank(doubleEntry) && "yes".equalsIgnoreCase(doubleEntry.trim())) {
					edcBean.setDoubleEntry(true);
				} else {
					edcBean.setDoubleEntry(false);
				}

				if (!StringUtil.isBlank(electronicSignature) && "yes".equalsIgnoreCase(electronicSignature.trim())) {
					edcBean.setElectronicSignature(true);
				} else {
					edcBean.setElectronicSignature(false);
				}

				if (!StringUtil.isBlank(decisionCondition) && "yes".equalsIgnoreCase(decisionCondition.trim())) {
					edcBean.setDecisionCondition(true);
				} else {
					edcBean.setDecisionCondition(false);
				}

				if (!StringUtil.isBlank(evaluatedCrf) && "yes".equalsIgnoreCase(evaluatedCrf.trim())) {
					edcBean.setEvaluatedCRF(true);
				} else {
					edcBean.setEvaluatedCRF(false);
				}

				String nullString = "";
				// process null values
				ArrayList nulls = NullValue.toArrayList();
				for (Object aNull : nulls) {
					NullValue n = (NullValue) aNull;
					String myNull = fp.getString(n.getName().toLowerCase() + i);
					if (!StringUtil.isBlank(myNull) && "yes".equalsIgnoreCase(myNull.trim())) {
						nullString = nullString + n.getName().toUpperCase() + ",";
					}

				}
				nullString = (nullString != "") ? nullString.substring(0, nullString.length() - 1) : "";

				if (sdvId > 0
						&& (edcBean.getSourceDataVerification() == null || sdvId != edcBean.getSourceDataVerification()
								.getCode())) {
					edcBean.setSourceDataVerification(SourceDataVerification.getByCode(sdvId));
				}

				if (!StringUtil.isBlank(emailTo)) {
					edcBean.setEmailTo(emailTo);
				} else {
					edcBean.setEmailTo("");
				}

				if (!StringUtil.isBlank(emailStep)) {
					edcBean.setEmailStep(emailStep);
				} else {
					edcBean.setEmailStep("");
				}

				edcBean.setNullValues(nullString);
				logger.info("found null values: " + nullString);
			}

		}
		fp.getRequest().getSession().setAttribute("eventDefinitionCRFs", edcs);
	}

	private void saveEventDefinitionToSession(StudyEventDefinitionBean sed, FormProcessor fp) {
		sed.setName(fp.getString("name"));
		sed.setRepeating(fp.getBoolean("repeating"));
		sed.setCategory(fp.getString("category"));
		sed.setDescription(fp.getString("description"));
		sed.setType(fp.getString("type"));
		sed.setMaxDay(fp.getInt("maxDay"));
		sed.setMinDay(fp.getInt("minDay"));
		sed.setScheduleDay(fp.getInt("schDay"));
		int userId = getIdByUserName(fp.getString("emailUser"));
		if (userId != 0) {
			sed.setUserEmailId(userId);
		} else {
			sed.setUserEmailId(1);
		}
		sed.setEmailDay(fp.getInt("emailDay"));
		String referenceVisitValue = fp.getString("isReference");
		if ("true".equalsIgnoreCase(referenceVisitValue)) {
			sed.setReferenceVisit(true);
		} else {
			sed.setReferenceVisit(false);
		}
		fp.getRequest().getSession().setAttribute("definition", sed);
	}

	public static void clearSession(HttpSession session) {
		session.removeAttribute("definition");
		session.removeAttribute("eventDefinitionCRFs");
		session.removeAttribute("childEventDefCRFs");
		session.removeAttribute("tmpCRFIdMap");
		session.removeAttribute("crfsWithVersion");
		session.removeAttribute("eventDefinitionCRFs");
		session.removeAttribute("showCalendaredVisitBox");
		session.removeAttribute("changedReference");
		session.removeAttribute("userNameInsteadEmail");
		session.removeAttribute("sdvOptions");
	}

}
