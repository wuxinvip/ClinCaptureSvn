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
 *
 * Created on Sep 22, 2005
 */
package org.akaza.openclinica.control.managestudy;

import com.clinovo.util.CrfShortcutsAnalyzer;
import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayEventCRFBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.control.core.SpringServlet;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.CreateDiscrepancyNoteServlet;
import org.akaza.openclinica.control.submit.DataEntryServlet;
import org.akaza.openclinica.control.submit.EnterDataForStudyEventServlet;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ResolveDiscrepancyServlet that handles request when user click on "view within record"-button on "Notes &
 * Discrepancies"-page.
 */
@SuppressWarnings("rawtypes")
@Component
public class ResolveDiscrepancyServlet extends SpringServlet {

	private static final String INPUT_NOTE_ID = "noteId";
	private static final String EVENT_CRF_ID = "ecId";
	private static final String STUDY_SUB_ID = "studySubjectId";
	public static final String TAB_ID = "tabId";
	public static final String SECTION_ID = "sectionId";
	public static final String FIELD = "field";
	public static final String FROM_RESOLVE_DISCREPANCY_ATTR = "fromResolveDiscrepancy";

	/**
	 * Returns Page for forwarding.
	 * 
	 * @param request
	 *            The HttpServletRequest.
	 * @param note
	 *            The DiscrepancyNoteBean for what we are looking for page for forwarding.
	 * @return A Page.
	 */
	public Page getPageForForwarding(HttpServletRequest request, DiscrepancyNoteBean note) {
		UserAccountBean ub = getUserAccountBean(request);
		StudyBean currentStudy = getCurrentStudy(request);
		StudyUserRoleBean currentRole = getCurrentRole(request);

		String entityType = note.getEntityType().toLowerCase();
		request.setAttribute("fromResolvingNotes", "yes");

		if ("subject".equalsIgnoreCase(entityType)) {
			if (ub.isSysAdmin() || ub.isTechAdmin()) {
				return Page.UPDATE_SUBJECT_SERVLET;
			} else {
				return Page.VIEW_STUDY_SUBJECT_SERVLET;
			}
		} else if ("studysub".equalsIgnoreCase(entityType)) {
			if (ub.isSysAdmin() || ub.isTechAdmin()) {
				return Page.UPDATE_STUDY_SUBJECT_SERVLET;
			} else {
				return Page.VIEW_STUDY_SUBJECT_SERVLET;
			}
		} else if ("studyevent".equalsIgnoreCase(entityType)) {
			if (ub.isSysAdmin() || ub.isTechAdmin()) {
				return Page.UPDATE_STUDY_EVENT_SERVLET;
			} else {
				return Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET;
			}
		} else if ("itemdata".equalsIgnoreCase(entityType) || "eventcrf".equalsIgnoreCase(entityType)) {

			int eventCRFID;
			if ("itemdata".equalsIgnoreCase(entityType)) {
				ItemDataBean idb = (ItemDataBean) getItemDataDAO().findByPK(note.getEntityId());
				eventCRFID = idb.getEventCRFId();
			} else {
				eventCRFID = note.getEntityId();
			}
			EventCRFBean ecb = (EventCRFBean) getEventCRFDAO().findByPK(eventCRFID);
			StudySubjectBean ssb = (StudySubjectBean) getStudySubjectDAO().findByPK(ecb.getStudySubjectId());
			CRFVersionBean crfvb = (CRFVersionBean) getCRFVersionDAO().findByPK(ecb.getCRFVersionId());
			StudyEventBean seb = (StudyEventBean) getStudyEventDAO().findByPK(ecb.getStudyEventId());
			EventDefinitionCRFBean edcb = getEventDefinitionCRFDAO().findByStudyEventDefinitionIdAndCRFId(
					seb.getStudyEventDefinitionId(), crfvb.getCrfId());
			DisplayEventCRFBean dec = new DisplayEventCRFBean();
			dec.setEventDefinitionCRF(edcb);
			dec.setFlags(ecb, ub, currentRole, edcb);
			request.setAttribute(EVENT_CRF_ID, Integer.toString(ecb.getId()));
			Role activeStudyRole = StudyUserRoleBean.determineRoleInCurrentStudy(ub, currentStudy);
			if (!ssb.getStatus().equals(Status.LOCKED)
					&& currentStudy.getStatus().equals(Status.AVAILABLE)
					&& (ecb.getStatus().equals(Status.AVAILABLE) || ecb.getStatus().equals(Status.UNAVAILABLE) || ecb
							.getStatus().equals(Status.PENDING))
					&& (activeStudyRole.equals(Role.SYSTEM_ADMINISTRATOR)
							|| activeStudyRole.equals(Role.CLINICAL_RESEARCH_COORDINATOR)
							|| activeStudyRole.equals(Role.INVESTIGATOR)
							|| activeStudyRole.equals(Role.STUDY_ADMINISTRATOR) || activeStudyRole
								.equals(Role.STUDY_EVALUATOR))) {
				request.getSession().setAttribute(FROM_RESOLVE_DISCREPANCY_ATTR, "true");
				if (dec.isContinueInitialDataEntryPermitted()) {
					return Page.INITIAL_DATA_ENTRY_SERVLET;
				} else if (dec.isStartDoubleDataEntryPermitted()) {
					return Page.DOUBLE_DATA_ENTRY_SERVLET;
				} else if (dec.isContinueDoubleDataEntryPermitted()) {
					return Page.DOUBLE_DATA_ENTRY_SERVLET;
				} else if (dec.isPerformAdministrativeEditingPermitted()) {
					return Page.ADMIN_EDIT_SERVLET;
				}
			}
			return Page.VIEW_SECTION_DATA_ENTRY_SERVLET;
		}
		return null;
	}

	/**
	 * Get needed data from DB and save it to the request.
	 * 
	 * @param request
	 *            The HttpServletRequest.
	 * @param ds
	 *            The DataSource.
	 * @param currentStudy
	 *            The StudyBean.
	 * @param note
	 *            The DiscrepancyNoteBean.
	 * @param isCompleted
	 *            The boolean.
	 * @return A boolean.
	 */
	@SuppressWarnings("unchecked")
	public boolean prepareRequestForResolution(HttpServletRequest request, DataSource ds, StudyBean currentStudy,
			DiscrepancyNoteBean note, boolean isCompleted) {
		StudyUserRoleBean currentRole = getCurrentRole(request);
		String entityType = note.getEntityType().toLowerCase();
		int id = note.getEntityId();
		if ("subject".equalsIgnoreCase(entityType)) {
			StudySubjectDAO ssdao = getStudySubjectDAO();
			StudySubjectBean ssb = ssdao.findBySubjectIdAndStudy(id, currentStudy);

			request.setAttribute("action", "show");
			request.setAttribute("id", String.valueOf(note.getEntityId()));
			request.setAttribute("studySubId", String.valueOf(ssb.getId()));
		} else if ("studysub".equalsIgnoreCase(entityType)) {
			request.setAttribute("action", "show");
			request.setAttribute("id", String.valueOf(note.getEntityId()));
		} else if ("eventcrf".equalsIgnoreCase(entityType)) {
			request.setAttribute("editInterview", "1");

			EventCRFDAO ecdao = getEventCRFDAO();
			EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(id);
			request.setAttribute("eventCRF", ecb);
			// If the request is passed along to ViewSectionDataEntryServlet,
			// that code needs
			// an event crf id; the (ecb.getId()+"") is necessary because
			// FormProcessor throws
			// a ClassCastException without the casting to a String
			request.setAttribute(ViewSectionDataEntryServlet.EVENT_CRF_ID, ecb.getId() + "");
		} else if ("studyevent".equalsIgnoreCase(entityType)) {
			StudyEventDAO sedao = getStudyEventDAO();
			StudyEventBean seb = (StudyEventBean) sedao.findByPK(id);
			request.setAttribute(EnterDataForStudyEventServlet.INPUT_EVENT_ID, String.valueOf(id));
			request.setAttribute(UpdateStudyEventServlet.EVENT_ID, String.valueOf(id));
			request.setAttribute(UpdateStudyEventServlet.STUDY_SUBJECT_ID, String.valueOf(seb.getStudySubjectId()));
			request.getSession().setAttribute(CreateDiscrepancyNoteServlet.SUBJECT_ID,
					String.valueOf(seb.getStudySubjectId()));
		} else if ("itemdata".equalsIgnoreCase(entityType)) {
			// this is for item data
			SectionDAO sdao = new SectionDAO(ds);
			ItemDataDAO iddao = new ItemDataDAO(ds);
			ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(ds);

			ItemDataBean idb = (ItemDataBean) iddao.findByPK(id);

			EventCRFDAO ecdao = new EventCRFDAO(ds);

			EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(idb.getEventCRFId());

			ItemFormMetadataBean ifmb = ifmdao.findByItemIdAndCRFVersionId(idb.getItemId(), ecb.getCRFVersionId());
			List<SectionBean> allSections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
			int tabNum = CrfShortcutsAnalyzer.getTabNum(allSections, ifmb.getSectionId());
			request.setAttribute(TAB_ID, "" + tabNum);
			request.setAttribute(SECTION_ID, "" + ifmb.getSectionId());

			if (Role.isMonitor(currentRole.getRole()) || !isCompleted) {
				StudyEventDAO sedao = new StudyEventDAO(ds);
				StudyEventBean seb = (StudyEventBean) sedao.findByPK(id);
				request.setAttribute(EVENT_CRF_ID, String.valueOf(idb.getEventCRFId()));
				request.setAttribute(ViewSectionDataEntryServlet.EVENT_CRF_ID, String.valueOf(idb.getEventCRFId()));
				request.setAttribute(STUDY_SUB_ID, String.valueOf(seb.getStudySubjectId()));

			} else {
				request.setAttribute(DataEntryServlet.INPUT_EVENT_CRF_ID, String.valueOf(idb.getEventCRFId()));
				request.setAttribute(DataEntryServlet.INPUT_SECTION_ID, String.valueOf(ifmb.getSectionId()));
			}
		}

		return true;
	}

	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		StudyBean currentStudy = getCurrentStudy(request);
		StudyUserRoleBean currentRole = getCurrentRole(request);
		FormProcessor fp = new FormProcessor(request);
		int noteId = fp.getInt(INPUT_NOTE_ID);
		String module = (String) request.getSession().getAttribute("module");

		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		dndao.setFetchMapping(true);

		// check that the note exists
		DiscrepancyNoteBean discrepancyNoteBean = (DiscrepancyNoteBean) dndao.findByPK(noteId);

		if (!discrepancyNoteBean.isActive()) {
			throw new InconsistentStateException(Page.MENU_SERVLET,
					getResException().getString("you_are_trying_resolve_discrepancy_not_exist"));
		}

		// check that the note has not already been closed
		ArrayList children = dndao.findAllByParent(discrepancyNoteBean);
		discrepancyNoteBean.setChildren(children);

		// all clear, send the user to the resolved screen
		String entityType = discrepancyNoteBean.getEntityType().toLowerCase();
		discrepancyNoteBean.setResStatus(ResolutionStatus.get(discrepancyNoteBean.getResolutionStatusId()));
		discrepancyNoteBean.setDisType(DiscrepancyNoteType.get(discrepancyNoteBean.getDiscrepancyNoteTypeId()));

		boolean isCompleted = false;
		if ("itemdata".equalsIgnoreCase(entityType)) {
			ItemDataDAO iddao = new ItemDataDAO(getDataSource());
			ItemDataBean idb = (ItemDataBean) iddao.findByPK(discrepancyNoteBean.getEntityId());
			ItemBean ib = (ItemBean) getItemDAO().findByPK(idb.getItemId());
			EventCRFBean ecb = (EventCRFBean) getEventCRFDAO().findByPK(idb.getEventCRFId());
			CRFVersionBean crfvb = (CRFVersionBean) getCRFVersionDAO().findByPK(ecb.getCRFVersionId());
			ItemGroupBean ig = getItemGroupDAO().findByItemAndCRFVersion(ib, crfvb);
			ItemGroupMetadataBean igmb = (ItemGroupMetadataBean) getItemGroupMetadataDAO().findByItemAndCrfVersion(
					ib.getId(), crfvb.getId());

			discrepancyNoteBean.setSubjectId(ecb.getStudySubjectId());
			discrepancyNoteBean.setItemId(idb.getItemId());
			if (igmb.isRepeatingGroup()) {
				discrepancyNoteBean.setField(ig.getOid() + "_" + (idb.getOrdinal() - 1) + "input" + ib.getId());
			} else {
				discrepancyNoteBean.setField("input" + ib.getId());
			}
			if (ecb.getStatus().equals(Status.UNAVAILABLE)) {
				isCompleted = true;
			}
		} else if ("studySub".equalsIgnoreCase(entityType)) {
			discrepancyNoteBean.setSubjectId(discrepancyNoteBean.getEntityId());
		} else if ("subject".equalsIgnoreCase(entityType)) {
			discrepancyNoteBean.setSubjectId(discrepancyNoteBean.getEntityId());
		} else if ("studyevent".equalsIgnoreCase(entityType)) {
			StudyEventDAO sedao = new StudyEventDAO(getDataSource());
			StudyEventBean seb = (StudyEventBean) sedao.findByPK(discrepancyNoteBean.getEntityId());
			discrepancyNoteBean.setSubjectId(seb.getStudySubjectId());
		} else if ("eventCrf".equalsIgnoreCase(entityType)) {
			EventCRFDAO ecdao = new EventCRFDAO(getDataSource());
			EventCRFBean ecb = (EventCRFBean) ecdao.findByPK(discrepancyNoteBean.getEntityId());
			discrepancyNoteBean.setSubjectId(ecb.getStudySubjectId());
		}

		// If it's not an ItemData type note, redirect
		// Monitors to View Subject or
		// View Study Events <<
		if (Role.isMonitor(currentRole.getRole()) && !"itemdata".equalsIgnoreCase(entityType)
				&& !"eventcrf".equalsIgnoreCase(entityType)) {
			redirectMonitor(request, response, module, discrepancyNoteBean);
			return;
		}
		// If Study is Frozen or Locked
		if (currentStudy.getStatus().isFrozen() && !"itemdata".equalsIgnoreCase(entityType)
				&& !"eventcrf".equalsIgnoreCase(entityType)) {
			redirectMonitor(request, response, module, discrepancyNoteBean);
			return;
		}

		boolean goNext = prepareRequestForResolution(request, getDataSource(), currentStudy, discrepancyNoteBean,
				isCompleted);

		Page p = getPageForForwarding(request, discrepancyNoteBean);

		if (p == null) {
			throw new InconsistentStateException(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET,
					getResException().getString("the_discrepancy_note_triying_resolve_has_invalid_type"));
		} else {
			String createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(discrepancyNoteBean,
					ResolutionStatus.CLOSED, true);
			setPopUpURL(request, createNoteURL);
		}

		if (p.equals(Page.INITIAL_DATA_ENTRY_SERVLET) || p.equals(Page.DOUBLE_DATA_ENTRY_SERVLET)
				|| p.equals(Page.ADMIN_EDIT_SERVLET)) {
			request.getSession().setAttribute(POP_UP_URL, request.getAttribute(POP_UP_URL));
			response.sendRedirect(request.getContextPath() + p.getFileName()
					+ (p.getFileName().contains("?") ? "&" : "?") + "eventCRFId=" + request.getAttribute(EVENT_CRF_ID)
					+ "&sectionId=" + request.getAttribute(SECTION_ID) + "&tabId=" + request.getAttribute(TAB_ID));
			return;
		}

		if (!goNext) {
			setPopUpURL(request, "");
			addPageMessage(getResPage().getString("you_may_not_perform_admin_edit_on_CRF_not_completed_by_user"), request);
			p = Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_SERVLET;
		}

		forwardPage(p, request, response);
	}

	@Override
	protected void mayProceed(HttpServletRequest request, HttpServletResponse response)
			throws InsufficientPermissionException {
		StudyUserRoleBean currentRole = getCurrentRole(request);

		String module = (String) request.getSession().getAttribute("module");
		if (module != null) {
			request.getSession().removeAttribute("module");
		}

		if (currentRole.getRole().equals(Role.STUDY_EVALUATOR) || currentRole.getRole().equals(Role.STUDY_CODER)
				|| currentRole.getRole().equals(Role.INVESTIGATOR) || currentRole.getRole().equals(Role.STUDY_DIRECTOR)
				|| currentRole.getRole().equals(Role.STUDY_ADMINISTRATOR)
				|| currentRole.getRole().equals(Role.SYSTEM_ADMINISTRATOR)
				|| currentRole.getRole().equals(Role.CLINICAL_RESEARCH_COORDINATOR)
				|| Role.isMonitor(currentRole.getRole())) {

			return;
		}

		addPageMessage(
				getResPage().getString("no_have_permission_to_resolve_discrepancy")
						+ getResPage().getString("change_study_contact_sysadmin"), request);
		throw new InsufficientPermissionException(Page.MENU_SERVLET,
				getResException().getString("not_study_director_or_study_coordinator"), "1");
	}

	/**
	 * Redirect the request to another page if the user is a Monitor type and the discrepancy note is a type other than
	 * item data or event crf.
	 * 
	 * @param module
	 *            String A String like "managestudy" or "admin"
	 * @param discrepancyNoteBean
	 *            DiscrepancyNoteBean
	 */
	private void redirectMonitor(HttpServletRequest request, HttpServletResponse response, String module,
			DiscrepancyNoteBean discrepancyNoteBean) {
		StudyBean currentStudy = getCurrentStudy(request);

		if (discrepancyNoteBean != null) {

			String createNoteURL;
			// This String will determine whether the type is other than
			// itemdata.
			String entityType = discrepancyNoteBean.getEntityType().toLowerCase();
			// The id of the subject, study subject, or study event
			int entityId = discrepancyNoteBean.getEntityId();
			RequestDispatcher dispatcher = null;
			DiscrepancyNoteUtil discNoteUtil = new DiscrepancyNoteUtil();

			if (!"".equalsIgnoreCase(entityType) && !"itemdata".equalsIgnoreCase(entityType)
					&& !"eventcrf".equalsIgnoreCase(entityType)) {
				if ("studySub".equalsIgnoreCase(entityType)) {
					dispatcher = request.getRequestDispatcher("/ViewStudySubject?id=" + entityId + "&module=" + module);
					discrepancyNoteBean.setSubjectId(entityId);
				} else if ("subject".equalsIgnoreCase(entityType)) {

					int studySubId = discNoteUtil.getStudySubjectIdForDiscNote(discrepancyNoteBean, getDataSource(),
							currentStudy.getId());

					dispatcher = request.getRequestDispatcher("/ViewStudySubject?id=" + studySubId + "&module="
							+ module);
					discrepancyNoteBean.setSubjectId(studySubId);
				} else if ("studyevent".equalsIgnoreCase(entityType)) {
					dispatcher = request.getRequestDispatcher("/EnterDataForStudyEvent?eventId=" + entityId);
				}

				// This code creates the URL for a popup window, which the
				// processing Servlet will initiate.
				// 'true' parameter means that ViewDiscrepancyNote is the
				// handling Servlet.
				createNoteURL = CreateDiscrepancyNoteServlet.getAddChildURL(discrepancyNoteBean,
						ResolutionStatus.CLOSED, true);
				request.setAttribute(POP_UP_URL, createNoteURL);

				try {
					if (dispatcher != null) {
						dispatcher.forward(request, response);
					}
				} catch (ServletException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
