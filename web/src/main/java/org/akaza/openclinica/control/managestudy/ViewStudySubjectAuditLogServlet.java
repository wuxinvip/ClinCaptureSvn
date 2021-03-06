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
 * copyright 2003-2007 Akaza Research
 */

package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clinovo.model.AuditLogRandomization;
import com.clinovo.service.AuditLogRandomizationService;
import com.clinovo.service.AuditLogService;
import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.SpringServlet;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ViewStudySubjectAuditLogServlet class.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class ViewStudySubjectAuditLogServlet extends SpringServlet {

	@Autowired
	private AuditLogRandomizationService auditLogRandomizationService;

	public static final int AUDIT_EVENT_TYPE_3 = 3;

	/**
	 * Checks whether the user has the right permission to proceed function.
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws InsufficientPermissionException
	 *             the InsufficientPermissionException
	 */
	@Override
	public void mayProceed(HttpServletRequest request, HttpServletResponse response)
			throws InsufficientPermissionException {
		UserAccountBean ub = getUserAccountBean(request);
		StudyUserRoleBean currentRole = getCurrentRole(request);

		if (ub.isSysAdmin()) {
			return;
		}

		if (mayViewData(ub, currentRole)) {
			return;
		}

		addPageMessage(
				getResPage().getString("no_have_correct_privilege_current_study") + " "
						+ getResPage().getString("change_study_contact_sysadmin"), request);
		throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS,
				getResException().getString("not_study_director"), "1");

	}

	@Override
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		StudyBean currentStudy = getCurrentStudy(request);

		StudySubjectDAO subdao = getStudySubjectDAO();
		SubjectDAO sdao = getSubjectDAO();
		AuditDAO adao = getAuditDAO();

		FormProcessor fp = new FormProcessor(request);

		StudyEventDAO sedao = getStudyEventDAO();
		EventCRFDAO ecdao = getEventCRFDAO();
		StudyDAO studydao = getStudyDAO();
		CRFDAO cdao = getCRFDAO();
		CRFVersionDAO cvdao = getCRFVersionDAO();

		ArrayList studySubjectAudits = new ArrayList();
		ArrayList eventCRFAudits = new ArrayList();
		ArrayList studyEventAudits = new ArrayList();
		ArrayList allDeletedEventCRFs = new ArrayList();
		String attachedFilePath = Utils.getAttachedFilePath(getParentStudy());

		int studySubId = fp.getInt("id", true);
		request.setAttribute("id", studySubId);

		if (studySubId == 0) {
			addPageMessage(getResPage().getString("please_choose_a_subject_to_view"), request);
			forwardPage(Page.LIST_STUDY_SUBJECTS, request, response);
		} else {
			StudySubjectBean studySubject = (StudySubjectBean) subdao.findByPK(studySubId);
			StudyBean study = (StudyBean) studydao.findByPK(studySubject.getStudyId());
			getStudyConfigService().setParametersForStudy(study);

			// Check if this StudySubject would be accessed from the Current Study
			if (studySubject.getStudyId() != currentStudy.getId()) {
				if (currentStudy.getParentStudyId() > 0) {
					addPageMessage(
							getResPage().getString("no_have_correct_privilege_current_study") + " "
									+ getResPage().getString("change_active_study_or_contact"), request);
					forwardPage(Page.MENU_SERVLET, request, response);
					return;
				} else {
					// The SubjectStudy is not belong to currentstudy and current study is not a site.
					Collection sites = studydao.findOlnySiteIdsByStudy(currentStudy);
					if (!sites.contains(study.getId())) {
						addPageMessage(
								getResPage().getString("no_have_correct_privilege_current_study") + " "
										+ getResPage().getString("change_active_study_or_contact"), request);
						forwardPage(Page.MENU_SERVLET, request, response);
						return;
					}
				}
			}

			request.setAttribute("studySub", studySubject);
			SubjectBean subject = (SubjectBean) sdao.findByPK(studySubject.getSubjectId());
			request.setAttribute("subject", subject);

			request.setAttribute("subjectStudy", study);

			/* Show both study subject and subject audit events together */
			// Study subject value changed
			Collection studySubjectAuditEvents = adao.findStudySubjectAuditEvents(studySubject.getId());
			// Text values will be shown on the page for the corresponding
			// integer values.
			for (Object studySubjectAuditEvent : studySubjectAuditEvents) {
				AuditBean auditBean = (AuditBean) studySubjectAuditEvent;
				if (auditBean.getAuditEventTypeId() == AUDIT_EVENT_TYPE_3) {
					auditBean.setOldValue(Status.get(Integer.parseInt(auditBean.getOldValue())).getName());
					auditBean.setNewValue(Status.get(Integer.parseInt(auditBean.getNewValue())).getName());
				}
			}

			// Global subject value changed
			studySubjectAudits.addAll(adao.findSubjectAuditEvents(subject.getId()));
			studySubjectAudits.addAll(studySubjectAuditEvents);
			studySubjectAudits.addAll(adao.findStudySubjectGroupAssignmentAuditEvents(studySubject.getId()));
			request.setAttribute("studySubjectAudits", studySubjectAudits);

			// Get the list of events
			List<StudyEventBean> events = sedao.findAllByStudySubject(studySubject);
			getAuditLogService(getServletContext()).addDeletedStudyEvents(studySubject, events);
			for (StudyEventBean studyEvent : events) {
				// Link event CRFs
				studyEvent.setEventCRFs(ecdao.findAllByStudyEvent(studyEvent));
				getAuditLogService(getServletContext()).addDeletedEventCRFs(studySubject, studyEvent);

				// Find deleted Event CRFs
				List deletedEventCRFs = adao.findDeletedEventCRFsFromAuditEvent(studyEvent.getId());
				allDeletedEventCRFs.addAll(deletedEventCRFs);
				logger.info("deletedEventCRFs size[" + deletedEventCRFs.size() + "]");
			}

			for (Object event : events) {
				StudyEventBean studyEvent = (StudyEventBean) event;
				studyEventAudits.addAll(adao.findStudyEventAuditEvents(studyEvent.getId()));

				ArrayList eventCRFs = studyEvent.getEventCRFs();
				for (Object eventCRF1 : eventCRFs) {
					// Link CRF and CRF Versions
					EventCRFBean eventCRF = (EventCRFBean) eventCRF1;
					CRFVersionBean crfVersionBean = (CRFVersionBean) cvdao.findByPK(eventCRF.getCRFVersionId());
					if (crfVersionBean.getId() > 0) {
						eventCRF.setCrfVersion(crfVersionBean);
					}
					CRFBean crfBean = cdao.findByVersionId(eventCRF.getCRFVersionId());
					if (crfBean.getId() > 0) {
						eventCRF.setCrf(crfBean);
					}
					// Get the event crf audits
					eventCRFAudits.addAll(adao.findEventCRFAuditEventsWithItemDataType(eventCRF.getId(), eventCRF.getCRFVersionId()));
					logger.info("eventCRFAudits size [" + eventCRFAudits.size() + "] eventCRF id [" + eventCRF.getId()
							+ "]");
				}
			}

			List<AuditLogRandomization> randomizationLogs = auditLogRandomizationService.findAllByStudySubjectId(studySubId);

			request.setAttribute("events", events);
			request.setAttribute("eventCRFAudits", eventCRFAudits);
			request.setAttribute("studyEventAudits", studyEventAudits);
			request.setAttribute("allDeletedEventCRFs", allDeletedEventCRFs);
			request.setAttribute("attachedFilePath", attachedFilePath);
			request.setAttribute("randomizationAudits", randomizationLogs);

			forwardPage(Page.VIEW_STUDY_SUBJECT_AUDIT, request, response);

		}
	}

	@Override
	protected String getAdminServlet(HttpServletRequest request) {
		UserAccountBean ub = getUserAccountBean(request);
		if (ub.isSysAdmin()) {
			return SpringServlet.ADMIN_SERVLET_CODE;
		} else {
			return "";
		}
	}

	private AuditLogService getAuditLogService(ServletContext context) {
		return (AuditLogService) SpringServletAccess.getApplicationContext(context).getBean("auditLogService");
	}
}
