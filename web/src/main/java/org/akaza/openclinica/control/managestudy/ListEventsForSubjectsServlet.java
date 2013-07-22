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

import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.control.RememberLastPage;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({ "rawtypes" })
public class ListEventsForSubjectsServlet extends RememberLastPage {

    private static final long serialVersionUID = 1L;
    public static final String SAVED_LIST_EVENTS_FOR_SUBJECTS_URL = "savedListEventsForSubjectsUrl";
    private StudyEventDefinitionDAO studyEventDefinitionDAO;
	private SubjectDAO subjectDAO;
	private StudySubjectDAO studySubjectDAO;
	private StudyEventDAO studyEventDAO;
	private StudyGroupClassDAO studyGroupClassDAO;
	private SubjectGroupMapDAO subjectGroupMapDAO;
	private StudyDAO studyDAO;
	private StudyGroupDAO studyGroupDAO;
	private EventCRFDAO eventCRFDAO;
	private EventDefinitionCRFDAO eventDefintionCRFDAO;
	private CRFDAO crfDAO;
	Locale locale;
	private boolean showMoreLink;

	@Override
	protected void mayProceed() throws InsufficientPermissionException {

		locale = request.getLocale();

		if (ub.isSysAdmin()) {
			return;
		}

		if (SubmitDataServlet.mayViewData(ub, currentRole)) {
			return;
		}

		addPageMessage(respage.getString("no_have_correct_privilege_current_study")
				+ respage.getString("change_study_contact_sysadmin"));
		throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
	}

	@Override
	public void processRequest() throws Exception {
        analyzeUrl();
		FormProcessor fp = new FormProcessor(request);
		if (fp.getString("showMoreLink").equals("")) {
			showMoreLink = true;
		} else {
			showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
		}
		String idSetting = currentStudy.getStudyParameterConfig().getSubjectIdGeneration();
		// set up auto study subject id
		if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
			String nextLabel = getStudySubjectDAO().findNextLabel(currentStudy.getIdentifier());
			request.setAttribute("label", nextLabel);
		}

		// checks which module the requests are from
		String module = fp.getString(MODULE);
		request.setAttribute(MODULE, module);

		int definitionId = fp.getInt("defId");
		if (definitionId <= 0) {
			addPageMessage(respage.getString("please_choose_an_ED_ta_to_vies_details"));
			forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET);
			return;
		}

		ListEventsForSubjectTableFactory factory = new ListEventsForSubjectTableFactory(showMoreLink);
		factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
		factory.setSubjectDAO(getSubjectDAO());
		factory.setStudySubjectDAO(getStudySubjectDAO());
		factory.setStudyEventDAO(getStudyEventDAO());
		factory.setStudyBean(currentStudy);
		factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
		factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
		factory.setStudyDAO(getStudyDAO());
		factory.setStudyGroupDAO(getStudyGroupDAO());
		factory.setCurrentRole(currentRole);
		factory.setCurrentUser(ub);
		factory.setEventCRFDAO(getEventCRFDAO());
		factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
		factory.setCrfDAO(getCrfDAO());
		factory.setSelectedStudyEventDefinition((StudyEventDefinitionBean) getStudyEventDefinitionDao().findByPK(
				definitionId));
		String listEventsForSubjectsHtml = factory.createTable(request, response).render();
		request.setAttribute("listEventsForSubjectsHtml", listEventsForSubjectsHtml);
		request.setAttribute("defId", definitionId);
		// For event definitions and group class list in the add subject popup
		request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
		request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
		FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
		session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
		//
        analyzeForward(Page.LIST_EVENTS_FOR_SUBJECTS);

	}

	public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
		studyEventDefinitionDAO = studyEventDefinitionDAO == null ? new StudyEventDefinitionDAO(sm.getDataSource())
				: studyEventDefinitionDAO;
		return studyEventDefinitionDAO;
	}

	public SubjectDAO getSubjectDAO() {
		subjectDAO = this.subjectDAO == null ? new SubjectDAO(sm.getDataSource()) : subjectDAO;
		return subjectDAO;
	}

	public StudySubjectDAO getStudySubjectDAO() {
		studySubjectDAO = this.studySubjectDAO == null ? new StudySubjectDAO(sm.getDataSource()) : studySubjectDAO;
		return studySubjectDAO;
	}

	public StudyGroupClassDAO getStudyGroupClassDAO() {
		studyGroupClassDAO = this.studyGroupClassDAO == null ? new StudyGroupClassDAO(sm.getDataSource())
				: studyGroupClassDAO;
		return studyGroupClassDAO;
	}

	public SubjectGroupMapDAO getSubjectGroupMapDAO() {
		subjectGroupMapDAO = this.subjectGroupMapDAO == null ? new SubjectGroupMapDAO(sm.getDataSource())
				: subjectGroupMapDAO;
		return subjectGroupMapDAO;
	}

	public StudyEventDAO getStudyEventDAO() {
		studyEventDAO = this.studyEventDAO == null ? new StudyEventDAO(sm.getDataSource()) : studyEventDAO;
		return studyEventDAO;
	}

	public StudyDAO getStudyDAO() {
		studyDAO = this.studyDAO == null ? new StudyDAO(sm.getDataSource()) : studyDAO;
		return studyDAO;
	}

	public EventCRFDAO getEventCRFDAO() {
		eventCRFDAO = this.eventCRFDAO == null ? new EventCRFDAO(sm.getDataSource()) : eventCRFDAO;
		return eventCRFDAO;
	}

	public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
		eventDefintionCRFDAO = this.eventDefintionCRFDAO == null ? new EventDefinitionCRFDAO(sm.getDataSource())
				: eventDefintionCRFDAO;
		return eventDefintionCRFDAO;
	}

	public CRFDAO getCrfDAO() {
		crfDAO = this.crfDAO == null ? new CRFDAO(sm.getDataSource()) : crfDAO;
		return crfDAO;
	}

	public StudyGroupDAO getStudyGroupDAO() {
		studyGroupDAO = this.studyGroupDAO == null ? new StudyGroupDAO(sm.getDataSource()) : studyGroupDAO;
		return studyGroupDAO;
	}

	private String parseDefId(String currentDefId, String savedUrl) {
		String pattern = ".*defId=(\\d*).*";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(savedUrl);
		return m.find() ? m.group(m.groupCount()) : currentDefId;
	}

	@Override
	protected String getUrlKey() {
		return SAVED_LIST_EVENTS_FOR_SUBJECTS_URL;
	}

	@Override
	protected String getDefaultUrl() {
		FormProcessor fp = new FormProcessor(request);
		if (fp.getString("showMoreLink").equals("")) {
			showMoreLink = true;
		} else {
			showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
		}
		String currentDefId = fp.getString("defId");
		String savedUrl = (String) request.getSession().getAttribute(SAVED_LIST_EVENTS_FOR_SUBJECTS_URL);
		if (savedUrl != null && !currentDefId.equals(parseDefId(currentDefId, savedUrl))) {
			savedUrl = null;
			request.getSession().removeAttribute(SAVED_LIST_EVENTS_FOR_SUBJECTS_URL);
		}
		savedUrl = savedUrl != null ? savedUrl.replaceAll(".*" + request.getContextPath() + "/ListStudySubjects", "")
				: savedUrl;
		return request.getMethod().equalsIgnoreCase("POST") && savedUrl != null ? savedUrl : "?module="
				+ fp.getString("module") + "&defId=" + fp.getString("defId") + "&maxRows=15&showMoreLink="
				+ showMoreLink + "&listEventsForSubject_tr_=true&listEventsForSubject_p_=1&listEventsForSubject_mr_=15";
	}

	@Override
	protected boolean userDoesNotUseJmesaTableForNavigation() {
		return request.getQueryString() == null || !request.getQueryString().contains("&listEventsForSubject_p_=");
	}
}
