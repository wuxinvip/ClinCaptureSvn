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
package org.akaza.openclinica.control;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.admin.EventStatusStatisticsTableFactory;
import org.akaza.openclinica.control.admin.SiteStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudyStatisticsTableFactory;
import org.akaza.openclinica.control.admin.StudySubjectStatusStatisticsTableFactory;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.submit.ListStudySubjectTableFactory;
import org.akaza.openclinica.dao.dynamicevent.DynamicEventDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.akaza.openclinica.web.table.sdv.SDVUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * The main controller servlet for all the work behind study sites for OpenClinica.
 * 
 * @author jxu
 * 
 */
@SuppressWarnings({ "rawtypes", "serial" })
public class MainMenuServlet extends SecureController {

	Locale locale;
	private StudyEventDefinitionDAO studyEventDefinitionDAO;
	private SubjectDAO subjectDAO;
	private StudySubjectDAO studySubjectDAO;
	private StudyEventDAO studyEventDAO;
	private StudyGroupClassDAO studyGroupClassDAO;
	private SubjectGroupMapDAO subjectGroupMapDAO;
	private StudyDAO studyDAO;
	private EventCRFDAO eventCRFDAO;
	private EventDefinitionCRFDAO eventDefintionCRFDAO;
	private StudyGroupDAO studyGroupDAO;
	private DiscrepancyNoteDAO discrepancyNoteDAO;
	private DynamicEventDao dynamicEventDao;

	@Override
	public void mayProceed() throws InsufficientPermissionException {
		locale = request.getLocale();
	}

	@Override
	public void processRequest() throws Exception {
		ub.incNumVisitsToMainMenu();
		session.setAttribute(USER_BEAN_NAME, ub);
		request.setAttribute("iconInfoShown", true);
		request.setAttribute("closeInfoShowIcons", false);

		if (ub == null || ub.getId() == 0) {// in case database connection is
			// broken
			forwardPage(Page.MENU, false);
			return;
		}
		StudyDAO sdao = new StudyDAO(sm.getDataSource());
		ArrayList studies = null;

		long pwdExpireDay = new Long(SQLInitServlet.getField("passwd_expiration_time")).longValue();
		Date lastPwdChangeDate = ub.getPasswdTimestamp();

		// a flag tells whether users are required to change pwd upon the first
		// time log in or pwd expired
		int pwdChangeRequired = new Integer(SQLInitServlet.getField("change_passwd_required")).intValue();
		// update last visit date to current date
		UserAccountDAO udao = new UserAccountDAO(sm.getDataSource());
		UserAccountBean ub1 = (UserAccountBean) udao.findByPK(ub.getId());
		ub1.setLastVisitDate(new Date(System.currentTimeMillis()));
		// have to actually set the above to a timestamp? tbh
		ub1.setOwner(ub1);
		ub1.setUpdater(ub1);
		udao.update(ub1);

		// Use study Id in JSPs
		request.setAttribute("studyId", currentStudy.getId());
		// Event Definition list and Group Class list for add suybject window.
		request.setAttribute("allDefsArray", super.getEventDefinitionsByCurrentStudy());
		request.setAttribute("studyGroupClasses", super.getStudyGroupClassesByCurrentStudy());
		if (lastPwdChangeDate != null || pwdChangeRequired == 0) {// not a new user

			if (lastPwdChangeDate == null && pwdChangeRequired == 0) {
				lastPwdChangeDate = new Date();
			}
			Calendar cal = Calendar.getInstance();
			// compute difference between current date and lastPwdChangeDate
			long difference = Math.abs(cal.getTime().getTime() - lastPwdChangeDate.getTime());
			long days = difference / (1000 * 60 * 60 * 24);
			session.setAttribute("passwordExpired", "no");

			if (pwdExpireDay != 0 && days >= pwdExpireDay) {// password expired, need to be changed
				studies = (ArrayList) sdao.findAllByUser(ub.getName());
				request.setAttribute("studies", studies);
				session.setAttribute("userBean1", ub);
				addPageMessage(respage.getString("password_expired"));
				// Add the feature that if password is expired,
				// have to go through /ResetPassword page
				session.setAttribute("passwordExpired", "yes");
				if (pwdChangeRequired == 1) {
					request.setAttribute("mustChangePass", "yes");
					addPageMessage(respage.getString("your_password_has_expired_must_change"));
				} else {
					request.setAttribute("mustChangePass", "no");
					addPageMessage(respage.getString("password_expired") + " "
							+ respage.getString("if_you_do_not_want_change_leave_blank"));
				}
				forwardPage(Page.RESET_PASSWORD);
			} else {

				if (ub.getNumVisitsToMainMenu() <= 1) {
					if (ub.getLastVisitDate() != null) {
						addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName()
								+ ". " + respage.getString("last_logged") + " "
								+ local_df.format(ub.getLastVisitDate()) + ". ");
					} else {
						addPageMessage(respage.getString("welcome") + " " + ub.getFirstName() + " " + ub.getLastName()
								+ ". ");
					}

					if (currentStudy.getStatus().isLocked()) {
						addPageMessage(respage.getString("current_study_locked"));
					} else if (currentStudy.getStatus().isFrozen()) {
						addPageMessage(respage.getString("current_study_frozen"));
					}
				}

				Integer assignedDiscrepancies = getDiscrepancyNoteDAO().getViewNotesCountWithFilter(ub.getId(),
						currentStudy.getId());
				request.setAttribute("assignedDiscrepancies", assignedDiscrepancies == null ? 0 : assignedDiscrepancies);

				int parentStudyId = currentStudy.getParentStudyId() > 0 ? currentStudy.getParentStudyId()
						: currentStudy.getId();
				StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
				StudyParameterValueBean parentSPV = spvdao.findByHandleAndStudy(parentStudyId, "subjectIdGeneration");
				currentStudy.getStudyParameterConfig().setSubjectIdGeneration(parentSPV.getValue());
				String idSetting = parentSPV.getValue();
				if (idSetting.equals("auto editable") || idSetting.equals("auto non-editable")) {
					request.setAttribute("label", resword.getString("id_generated_Save_Add"));
				}
                
                if (currentRole.isInvestigator() || currentRole.isClinicalResearchCoordinator()) {
                    setupListStudySubjectTable();
                } 
                if (currentRole.getRole() == Role.STUDY_MONITOR) {
                    setupSubjectSDVTable();
                } else if (currentRole.isSysAdmin() || currentRole.isStudyAdministrator() || currentRole.isStudyDirector()) {
                    if (currentStudy.getStatus().isPending()) {
                    	session.setAttribute("skipURL", "true");
                    	
                        response.sendRedirect(request.getContextPath() + Page.MANAGE_STUDY_MODULE);
                        return;
                    }
					setupStudySiteStatisticsTable();
					setupSubjectEventStatusStatisticsTable();
					setupStudySubjectStatusStatisticsTable();
					if (currentStudy.getParentStudyId() == 0) {
						setupStudyStatisticsTable();
					}

				}
				udao.updatePasswdHistory(ub); 
				forwardPage(Page.MENU);
			}

		} else {
			studies = (ArrayList) sdao.findAllByUser(ub.getName());
			request.setAttribute("studies", studies);
			session.setAttribute("userBean1", ub);

			if (pwdChangeRequired != 1) {
				udao.updatePasswdHistory(ub); 
				forwardPage(Page.MENU);
			}
		}

	}

	private void setupSubjectSDVTable() {

		request.setAttribute("studyId", currentStudy.getId());
		request.setAttribute("showMoreLink", "true");
		String sdvMatrix = getSDVUtil().renderEventCRFTableWithLimit(request, currentStudy.getId(), "");
		request.setAttribute("sdvMatrix", sdvMatrix);
	}

	private void setupStudySubjectStatusStatisticsTable() {

		StudySubjectStatusStatisticsTableFactory factory = new StudySubjectStatusStatisticsTableFactory();
		factory.setStudySubjectDao(getStudySubjectDAO());
		factory.setCurrentStudy(currentStudy);
		factory.setStudyDao(getStudyDAO());
		String studySubjectStatusStatistics = factory.createTable(request, response).render();
		request.setAttribute("studySubjectStatusStatistics", studySubjectStatusStatistics);
	}

	private void setupSubjectEventStatusStatisticsTable() {

		EventStatusStatisticsTableFactory factory = new EventStatusStatisticsTableFactory();
		factory.setStudySubjectDao(getStudySubjectDAO());
		factory.setCurrentStudy(currentStudy);
		factory.setStudyEventDao(getStudyEventDAO());
		factory.setStudyDao(getStudyDAO());
		String subjectEventStatusStatistics = factory.createTable(request, response).render();
		request.setAttribute("subjectEventStatusStatistics", subjectEventStatusStatistics);
	}

	private void setupStudySiteStatisticsTable() {

		SiteStatisticsTableFactory factory = new SiteStatisticsTableFactory();
		factory.setStudySubjectDao(getStudySubjectDAO());
		factory.setCurrentStudy(currentStudy);
		factory.setStudyDao(getStudyDAO());
		String studySiteStatistics = factory.createTable(request, response).render();
		request.setAttribute("studySiteStatistics", studySiteStatistics);

	}

	private void setupStudyStatisticsTable() {

		StudyStatisticsTableFactory factory = new StudyStatisticsTableFactory();
		factory.setStudySubjectDao(getStudySubjectDAO());
		factory.setCurrentStudy(currentStudy);
		factory.setStudyDao(getStudyDAO());
		String studyStatistics = factory.createTable(request, response).render();
		request.setAttribute("studyStatistics", studyStatistics);

	}

	private void setupListStudySubjectTable() {

		ListStudySubjectTableFactory factory = new ListStudySubjectTableFactory(true);
		factory.setStudyEventDefinitionDao(getStudyEventDefinitionDao());
		factory.setSubjectDAO(getSubjectDAO());
		factory.setStudySubjectDAO(getStudySubjectDAO());
		factory.setStudyEventDAO(getStudyEventDAO());
		factory.setStudyBean(currentStudy);
		factory.setStudyGroupClassDAO(getStudyGroupClassDAO());
		factory.setSubjectGroupMapDAO(getSubjectGroupMapDAO());
		factory.setStudyDAO(getStudyDAO());
		factory.setCurrentRole(currentRole);
		factory.setCurrentUser(ub);
		factory.setEventCRFDAO(getEventCRFDAO());
		factory.setEventDefintionCRFDAO(getEventDefinitionCRFDAO());
		factory.setDiscrepancyNoteDAO(getDiscrepancyNoteDAO());
		factory.setStudyGroupDAO(getStudyGroupDAO());
		factory.setDynamicEventDao(getDynamicEventDao());
		String findSubjectsHtml = factory.createTable(request, response).render();
		request.setAttribute("findSubjectsHtml", findSubjectsHtml);
	}

	public StudyEventDefinitionDAO getStudyEventDefinitionDao() {
		studyEventDefinitionDAO = studyEventDefinitionDAO == null ? new StudyEventDefinitionDAO(sm.getDataSource())
				: studyEventDefinitionDAO;
		return studyEventDefinitionDAO;
	}

	public DynamicEventDao getDynamicEventDao() {
		dynamicEventDao = this.dynamicEventDao == null ? new DynamicEventDao(sm.getDataSource()) : dynamicEventDao;
		return dynamicEventDao;
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

	public StudyGroupDAO getStudyGroupDAO() {
		studyGroupDAO = this.studyGroupDAO == null ? new StudyGroupDAO(sm.getDataSource()) : studyGroupDAO;
		return studyGroupDAO;
	}

	public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
		discrepancyNoteDAO = this.discrepancyNoteDAO == null ? new DiscrepancyNoteDAO(sm.getDataSource())
				: discrepancyNoteDAO;
		return discrepancyNoteDAO;
	}

	public SDVUtil getSDVUtil() {
		return (SDVUtil) SpringServletAccess.getApplicationContext(context).getBean("sdvUtil");
	}

}
