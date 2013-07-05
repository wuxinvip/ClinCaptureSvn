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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.EntityBeanTable;
import org.akaza.openclinica.web.bean.StudyEventRow;

/**
 *  Handles user request of "view study events"
 *  
 * @author jxu
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked", "serial"})
public class ViewStudyEventsServlet extends SecureController {

	Locale locale;

	public static final String INPUT_STARTDATE = "startDate";

	public static final String INPUT_ENDDATE = "endDate";

	public static final String INPUT_DEF_ID = "definitionId";

	public static final String INPUT_STATUS_ID = "statusId";

	public static final String STATUS_MAP = "statuses";

	public static final String DEFINITION_MAP = "definitions";

	public static final String PRINT = "print";

	/**
	 * Checks whether the user has the right permission to proceed function
	 */
	@Override
	public void mayProceed() throws InsufficientPermissionException {

		locale = request.getLocale();

		if (ub.isSysAdmin()) {
			return;
		}

		if (SubmitDataServlet.mayViewData(ub, currentRole)) {
			return;
		}

		addPageMessage(respage.getString("no_have_correct_privilege_current_study")
				+ respage.getString("change_study_contact_sysadmin"));
		throw new InsufficientPermissionException(Page.MENU_SERVLET, restext.getString("not_correct_role"), "1");
	}

	@Override
	public void processRequest() throws Exception {
		FormProcessor fp = new FormProcessor(request);
		// checks which module requests are from
		String module = fp.getString(MODULE);
		request.setAttribute(MODULE, module);

		int sedId = fp.getInt("sedId");
		int statusId = fp.getInt(INPUT_STATUS_ID);
		int definitionId = fp.getInt(INPUT_DEF_ID);
		Date startDate = fp.getDate(INPUT_STARTDATE);
		Date endDate = fp.getDate(INPUT_ENDDATE);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int month = cal.get(Calendar.MONTH) + 1;
		int year = cal.get(Calendar.YEAR);
		String defaultStartDateString = month + "/01/" + year;
		Date defaultStartDate = new Date();

		defaultStartDate = new SimpleDateFormat("MM/dd/yy").parse(defaultStartDateString);
		DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
		defaultStartDateString = dateFormatter.format(defaultStartDate);

		cal.setTime(defaultStartDate);
		cal.add(Calendar.DATE, 30);
		Date defaultEndDate = cal.getTime();

		if (!fp.isSubmitted()) {
			logger.info("not submitted");
			HashMap presetValues = new HashMap();

			presetValues.put(INPUT_STARTDATE, local_df.format(defaultStartDate));
			presetValues.put(INPUT_ENDDATE, local_df.format(defaultEndDate));
			startDate = defaultStartDate;
			endDate = defaultEndDate;
			setPresetValues(presetValues);
		} else {
			Validator v = new Validator(request);
			v.addValidation(INPUT_STARTDATE, Validator.IS_A_DATE);
			v.addValidation(INPUT_ENDDATE, Validator.IS_A_DATE);
			errors = v.validate();
			if (!errors.isEmpty()) {
				setInputMessages(errors);
				startDate = defaultStartDate;
				endDate = defaultEndDate;
			}
			fp.addPresetValue(INPUT_STARTDATE, fp.getString(INPUT_STARTDATE));
			fp.addPresetValue(INPUT_ENDDATE, fp.getString(INPUT_ENDDATE));
			fp.addPresetValue(INPUT_DEF_ID, fp.getInt(INPUT_DEF_ID));
			fp.addPresetValue(INPUT_STATUS_ID, fp.getInt(INPUT_STATUS_ID));
			setPresetValues(fp.getPresetValues());
		}

		request.setAttribute(STATUS_MAP, SubjectEventStatus.toArrayList());

		StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
		ArrayList definitions = seddao.findAllByStudy(currentStudy);
		request.setAttribute(DEFINITION_MAP, definitions);

		ArrayList allEvents = new ArrayList();
		allEvents = genTables(fp, definitions, startDate, endDate, sedId, definitionId, statusId);

		request.setAttribute("allEvents", allEvents);

		String queryUrl = INPUT_STARTDATE + "=" + local_df.format(startDate) + "&" + INPUT_ENDDATE + "="
				+ local_df.format(endDate) + "&" + INPUT_DEF_ID + "=" + definitionId + "&" + INPUT_STATUS_ID + "="
				+ statusId + "&" + "sedId=" + sedId + "&submitted=" + fp.getInt("submitted");
		request.setAttribute("queryUrl", queryUrl);
		if ("yes".equalsIgnoreCase(fp.getString(PRINT))) {
			allEvents = genEventsForPrint(fp, definitions, startDate, endDate, sedId, definitionId, statusId);
			request.setAttribute("allEvents", allEvents);
			forwardPage(Page.VIEW_STUDY_EVENTS_PRINT);
		} else {
			forwardPage(Page.VIEW_STUDY_EVENTS);
		}

	}

	private ArrayList genTables(FormProcessor fp, ArrayList definitions, Date startDate, Date endDate, int sedId,
			int definitionId, int statusId) {
		StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
		EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
		ArrayList allEvents = new ArrayList();
		definitions = findDefinitionById(definitions, definitionId);
		StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
		ArrayList studySubjects = ssdao.findAllByStudyId(currentStudy.getId());
		for (int i = 0; i < definitions.size(); i++) {
			ViewEventDefinitionBean ved = new ViewEventDefinitionBean();
			StudyEventDefinitionBean sed = (StudyEventDefinitionBean) definitions.get(i);

			ved.setDefinition(sed);

			ArrayList events = new ArrayList();
			for (int s = 0; s < studySubjects.size(); ++s) {
				StudySubjectBean ssb = (StudySubjectBean) studySubjects.get(s);
				ArrayList evts = sedao.findAllWithSubjectLabelByStudySubjectAndDefinition(ssb, sed.getId());

				for (int v = 0; v < evts.size(); ++v) {
					StudyEventBean seb = (StudyEventBean) evts.get(v);
					if (!(currentRole.isStudyDirector() || currentRole.isStudyAdministrator())
							&& seb.getSubjectEventStatus().isLocked()) {
						seb.setEditable(false);
					}
					events.add(seb);
				}
			}

			int subjectScheduled = 0;
			int subjectCompleted = 0;
			int subjectDiscontinued = 0;
			events = findEventByStatusAndDate(events, statusId, startDate, endDate);

			Date firstStartDateForScheduled = null;
			Date lastCompletionDate = null;
			// find the first firstStartDateForScheduled
			for (int k = 0; k < events.size(); k++) {
				StudyEventBean se = (StudyEventBean) events.get(k);
				if (se.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)) {
					firstStartDateForScheduled = se.getDateStarted();
					break;
				}

			}
			// find the first lastCompletionDate
			for (int k = 0; k < events.size(); k++) {
				StudyEventBean se = (StudyEventBean) events.get(k);
				if (se.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED) && se.getDateEnded() != null) {
					lastCompletionDate = se.getDateEnded();
					break;
				}
			}

			for (int j = 0; j < events.size(); j++) {
				StudyEventBean se = (StudyEventBean) events.get(j);
				if (se.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)) {
					subjectScheduled++;
					if (se.getDateStarted().before(new Date())) {
						ArrayList eventCRFs = ecdao.findAllByStudyEvent(se);
						if (eventCRFs.isEmpty()) {
							se.setScheduledDatePast(true);
						}
					}
					if (firstStartDateForScheduled == null) {
						firstStartDateForScheduled = se.getDateStarted();
					} else if (se.getDateStarted().before(firstStartDateForScheduled)) {
						firstStartDateForScheduled = se.getDateStarted();
					}
				} else if (se.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED)) {
					subjectCompleted++;
					if (lastCompletionDate == null) {
						lastCompletionDate = se.getDateEnded();
					} else if (se.getDateEnded() != null && se.getDateEnded().after(lastCompletionDate)) {
						lastCompletionDate = se.getDateEnded();
					}
				} else if (se.getSubjectEventStatus().getId() > 4) {
					subjectDiscontinued++;
				}
			}

			ved.setSubjectCompleted(subjectCompleted);
			ved.setSubjectScheduled(subjectScheduled);
			ved.setSubjectDiscontinued(subjectDiscontinued);
			ved.setFirstScheduledStartDate(firstStartDateForScheduled);
			ved.setLastCompletionDate(lastCompletionDate);

			EntityBeanTable table;
			if (sedId == sed.getId()) {// apply finding function or ordering
				// function
				// to a specific table
				table = fp.getEntityBeanTable();
			} else {
				table = new EntityBeanTable();
			}
			table.setSortingIfNotExplicitlySet(1, false);// sort by event
			// start date,
			// desc
			ArrayList allEventRows = StudyEventRow.generateRowsFromBeans(events);

			String[] columns = {
					currentStudy == null ? resword.getString("study_subject_ID") : currentStudy
							.getStudyParameterConfig().getStudySubjectIdLabel(),
					resword.getString("event_date_started"), resword.getString("subject_event_status"),
					resword.getString("actions") };
			table.setColumns(new ArrayList(Arrays.asList(columns)));
			table.hideColumnLink(3);
			HashMap args = new HashMap();
			args.put("sedId", new Integer(sed.getId()).toString());
			args.put("definitionId", new Integer(definitionId).toString());
			args.put("statusId", new Integer(statusId).toString());
			args.put("startDate", local_df.format(startDate));
			args.put("endDate", local_df.format(endDate));
			table.setQuery("ViewStudyEvents", args);
			table.setRows(allEventRows);
			table.computeDisplay();

			ved.setStudyEventTable(table);

			if (!events.isEmpty()) {
				allEvents.add(ved);
			}
		}

		return allEvents;
	}

	/**
	 * Generates an arraylist of study events for printing
	 * 
	 * @param fp
	 * @param definitions
	 * @param startDate
	 * @param endDate
	 * @param sedId
	 * @param definitionId
	 * @param statusId
	 * @return
	 */
	private ArrayList genEventsForPrint(FormProcessor fp, ArrayList definitions, Date startDate, Date endDate,
			int sedId, int definitionId, int statusId) {
		StudyEventDAO sedao = new StudyEventDAO(sm.getDataSource());
		EventCRFDAO ecdao = new EventCRFDAO(sm.getDataSource());
		ArrayList allEvents = new ArrayList();
		definitions = findDefinitionById(definitions, definitionId);
		StudySubjectDAO ssdao = new StudySubjectDAO(sm.getDataSource());
		ArrayList studySubjects = ssdao.findAllByStudyId(currentStudy.getId());
		for (int i = 0; i < definitions.size(); i++) {
			ViewEventDefinitionBean ved = new ViewEventDefinitionBean();
			StudyEventDefinitionBean sed = (StudyEventDefinitionBean) definitions.get(i);

			ved.setDefinition(sed);

			ArrayList events = new ArrayList();
			for (int s = 0; s < studySubjects.size(); ++s) {
				StudySubjectBean ssb = (StudySubjectBean) studySubjects.get(s);
				ArrayList evts = sedao.findAllWithSubjectLabelByStudySubjectAndDefinition(ssb, sed.getId());

				for (int v = 0; v < evts.size(); ++v) {
					events.add(evts.get(v));
				}
			}

			int subjectScheduled = 0;
			int subjectCompleted = 0;
			int subjectDiscontinued = 0;
			events = findEventByStatusAndDate(events, statusId, startDate, endDate);

			Date firstStartDateForScheduled = null;
			Date lastCompletionDate = null;
			// find the first firstStartDateForScheduled
			for (int k = 0; k < events.size(); k++) {
				StudyEventBean se = (StudyEventBean) events.get(k);
				if (se.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)) {
					firstStartDateForScheduled = se.getDateStarted();
					break;
				}

			}
			// find the first lastCompletionDate
			for (int k = 0; k < events.size(); k++) {
				StudyEventBean se = (StudyEventBean) events.get(k);
				if (se.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED)) {
					lastCompletionDate = se.getDateEnded();
					break;
				}
			}

			for (int j = 0; j < events.size(); j++) {
				StudyEventBean se = (StudyEventBean) events.get(j);
				if (se.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)) {
					subjectScheduled++;
					if (se.getDateStarted().before(new Date())) {
						ArrayList eventCRFs = ecdao.findAllByStudyEvent(se);
						if (eventCRFs.isEmpty()) {
							se.setScheduledDatePast(true);
						}
					}
					if (firstStartDateForScheduled == null) {
						firstStartDateForScheduled = se.getDateStarted();
					} else if (se.getDateStarted().before(firstStartDateForScheduled)) {
						firstStartDateForScheduled = se.getDateStarted();
					}
				} else if (se.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED)) {
					subjectCompleted++;
					if (lastCompletionDate == null) {
						lastCompletionDate = se.getDateEnded();
					} else if (se.getDateEnded().after(lastCompletionDate)) {
						lastCompletionDate = se.getDateEnded();
					}
				} else if (se.getSubjectEventStatus().getId() > 4) {
					// dropped out/stopped/skipped/relapse
					subjectDiscontinued++;
				}

			}
			ved.setSubjectCompleted(subjectCompleted);
			ved.setSubjectScheduled(subjectScheduled);
			ved.setSubjectDiscontinued(subjectDiscontinued);
			ved.setFirstScheduledStartDate(firstStartDateForScheduled);
			ved.setLastCompletionDate(lastCompletionDate);

			ved.setStudyEvents(events);

			if (!events.isEmpty()) {
				allEvents.add(ved);
			}
		}
		return allEvents;
	}

	private ArrayList findDefinitionById(ArrayList definitions, int definitionId) {
		if (definitionId > 0) {
			for (int i = 0; i < definitions.size(); i++) {
				StudyEventDefinitionBean sed = (StudyEventDefinitionBean) definitions.get(i);
				if (sed.getId() == definitionId) {
					ArrayList a = new ArrayList();
					a.add(sed);
					return a;
				}
			}
		}
		return definitions;
	}

	private ArrayList findEventByStatusAndDate(ArrayList events, int statusId, Date startDate, Date endDate) {
		ArrayList a = new ArrayList();
		for (int i = 0; i < events.size(); i++) {
			StudyEventBean se = (StudyEventBean) events.get(i);
			if (!se.getDateStarted().before(startDate) && !se.getDateStarted().after(endDate)) {
				a.add(se);
			}
		}
		ArrayList b = new ArrayList();
		if (statusId > 0) {
			for (int i = 0; i < a.size(); i++) {
				StudyEventBean se = (StudyEventBean) a.get(i);
				if (se.getSubjectEventStatus().getId() == statusId) {
					b.add(se);
				}
			}
			return b;
		}
		return a;
	}
}
