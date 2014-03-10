package com.clinovo.controller;

import com.clinovo.bean.display.DisplayWidgetsLayoutBean;
import com.clinovo.bean.display.DisplayWidgetsRowWithName;
import com.clinovo.model.Widget;
import com.clinovo.model.WidgetsLayout;
import com.clinovo.service.WidgetService;
import com.clinovo.service.WidgetsLayoutService;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@SuppressWarnings({ "unused", "rawtypes" })
public class WidgetsLayoutController {

	@Autowired
	private DataSource datasource;

	@Autowired
	private WidgetsLayoutService widgetLayoutService;

	@Autowired
	private WidgetService widgetService;

	@RequestMapping("/configureHomePage")
	public ModelMap configureHomePageHandler(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ModelMap model = new ModelMap();
		ResourceBundleProvider.updateLocale(request.getLocale());

		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute("userBean");
		StudyBean sb = (StudyBean) request.getSession().getAttribute("study");

		int studyId = sb.getId();
		int userId = ub.getId();
		int id = 1;

		List<WidgetsLayout> widgetsLayout = widgetLayoutService.findAllByStudyIdAndUserId(studyId, userId);
		List<DisplayWidgetsLayoutBean> dispayWidgetsLayout = new ArrayList<DisplayWidgetsLayoutBean>();

		for (WidgetsLayout currentLayout : widgetsLayout) {

			Widget currentWidget = widgetService.findByChildsId(currentLayout.getId());

			String widgetName = currentWidget.getWidgetName().toLowerCase().replaceAll(" ", "_");

			DisplayWidgetsLayoutBean currentDisplay = new DisplayWidgetsLayoutBean();

			currentDisplay.setWidgetName(widgetName + ".jsp");
			currentDisplay.setOrdinal(currentLayout.getOrdinal());
			currentDisplay.setWidgetId(currentWidget.getId());

			dispayWidgetsLayout.add(currentDisplay);
		}
		Collections.sort(dispayWidgetsLayout, DisplayWidgetsLayoutBean.comparatorForDisplayWidgetsLayout);
		model.addAttribute("dispayWidgetsLayout", dispayWidgetsLayout);

		return model;
	}

	@RequestMapping("/saveHomePage")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public @ResponseBody
	void saveHomePage(HttpServletRequest request) {

		String orderInColumn1 = request.getParameter("orderInColumn1");
		String orderInColumn2 = request.getParameter("orderInColumn2");
		String unusedWidgets = request.getParameter("unusedWidgets");
		int userId = Integer.parseInt(request.getParameter("userId"));
		int studyId = Integer.parseInt(request.getParameter("studyId"));

		if (!orderInColumn1.isEmpty()) {
			int ordinalCounter1 = 1;
			List<String> widgetsIdsColumn1 = Arrays.asList(orderInColumn1.split("\\s*,\\s*"));

			for (String widgetIdColumn1 : widgetsIdsColumn1) {
				WidgetsLayout currentWidgetLayout = widgetLayoutService.findByWidgetIdAndStudyIdAndUserId(
						Integer.parseInt(widgetIdColumn1), studyId, userId);
				currentWidgetLayout.setOrdinal(ordinalCounter1);
				widgetLayoutService.saveWidgetLayout(currentWidgetLayout);
				ordinalCounter1 = ordinalCounter1 + 2;
			}
		}

		if (!orderInColumn2.isEmpty()) {
			int ordinalCounter2 = 2;
			List<String> widgetsIdsColumn2 = Arrays.asList(orderInColumn2.split("\\s*,\\s*"));

			for (String widgetIdColumn2 : widgetsIdsColumn2) {

				WidgetsLayout currentWidgetLayout = widgetLayoutService.findByWidgetIdAndStudyIdAndUserId(
						Integer.parseInt(widgetIdColumn2), studyId, userId);
				currentWidgetLayout.setOrdinal(ordinalCounter2);
				widgetLayoutService.saveWidgetLayout(currentWidgetLayout);
				ordinalCounter2 = ordinalCounter2 + 2;
			}
		}

		if (!unusedWidgets.isEmpty()) {
			List<String> unusedWidgetsIds = Arrays.asList(unusedWidgets.split("\\s*,\\s*"));

			for (String unusedWidgetsId : unusedWidgetsIds) {
				WidgetsLayout currentWidgetLayout = widgetLayoutService.findByWidgetIdAndStudyIdAndUserId(
						Integer.parseInt(unusedWidgetsId), studyId, userId);
				currentWidgetLayout.setOrdinal(0);
				widgetLayoutService.saveWidgetLayout(currentWidgetLayout);
			}
		}
	}

	@RequestMapping("/initNdsAssignedToMeWidget")
	public void initNdsAssignedToMeWidget(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setDateHeader("Expires", -1);
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Cache-Control", "no-store");

		int currentUser = Integer.parseInt(request.getParameter("userId"));
		StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
		DiscrepancyNoteDAO discrepancyNoteDao = new DiscrepancyNoteDAO(datasource);

		Integer newDns = discrepancyNoteDao.getViewNotesCountWithFilter(" AND dn.assigned_user_id = " + currentUser
				+ " AND dn.resolution_status_id = 1", currentStudy);

		if (newDns == null) {
			newDns = 0;
		}
		Integer updatedDns = discrepancyNoteDao.getViewNotesCountWithFilter(" AND dn.assigned_user_id = " + currentUser
				+ " AND dn.resolution_status_id = 2", currentStudy);

		if (updatedDns == null) {
			updatedDns = 0;
		}
		Integer resolutionProposedDns = discrepancyNoteDao.getViewNotesCountWithFilter(" AND dn.assigned_user_id = "
				+ currentUser + " AND dn.resolution_status_id = 3", currentStudy);

		if (resolutionProposedDns == null) {
			resolutionProposedDns = 0;
		}
		Integer closedDns = discrepancyNoteDao.getViewNotesCountWithFilter(" AND dn.assigned_user_id = " + currentUser
				+ " AND dn.resolution_status_id = 4", currentStudy);

		if (closedDns == null) {
			closedDns = 0;
		}
		String result = newDns + "," + updatedDns + "," + resolutionProposedDns + "," + closedDns;
		response.getWriter().println(result);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping("/initEventsCompletionWidget")
	public String initEventsCompletionWidget(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1);
		response.setHeader("Cache-Control", "no-store");

		ResourceBundleProvider.updateLocale(request.getLocale());

		String page = "widgets/includes/eventsCompletionChart";
		String action = request.getParameter("action");

		StudyEventDAO studyEventDAO = new StudyEventDAO(datasource);
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(datasource);
		StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(datasource);

		boolean hasPrevious;
		boolean hasNext;
		int displayFrom = Integer.parseInt(request.getParameter("lastElement"));
		int maxDisplayNumber = 5;
		int studyId = Integer.parseInt(request.getParameter("studyId"));

		if (action.equals("goBack")) {
			displayFrom -= maxDisplayNumber;
		}

		if (action.equals("goForward")) {
			displayFrom += maxDisplayNumber;
		}

		SubjectEventStatus[] subjectEventStatuses = { SubjectEventStatus.SCHEDULED,
				SubjectEventStatus.DATA_ENTRY_STARTED, SubjectEventStatus.SOURCE_DATA_VERIFIED,
				SubjectEventStatus.SIGNED, SubjectEventStatus.COMPLETED, SubjectEventStatus.SKIPPED,
				SubjectEventStatus.STOPPED, SubjectEventStatus.LOCKED };

		StudyBean sb = (StudyBean) request.getSession().getAttribute("study");
		List<StudyEventDefinitionBean> studyEventDefinitions;
		int countOfSubject;

		if (sb.isSite(sb.getParentStudyId())) {
			countOfSubject = studySubjectDAO.getCountofStudySubjectsAtStudyOrSite(sb);
			studyEventDefinitions = studyEventDefinitionDAO.findAllActiveByParentStudyId(sb.getParentStudyId());
		} else {
			studyEventDefinitions = studyEventDefinitionDAO.findAllActiveByStudyId(sb.getId());
			countOfSubject = studySubjectDAO.getCountofStudySubjectsAtStudy(sb);
		}

		List<DisplayWidgetsRowWithName> eventCompletionRows = new ArrayList<DisplayWidgetsRowWithName>();

		for (int i = displayFrom; i < studyEventDefinitions.size() && i < displayFrom + maxDisplayNumber; i++) {
			DisplayWidgetsRowWithName currentRow = new DisplayWidgetsRowWithName();
			LinkedHashMap<String, Integer> countOfSubjectEventStatuses = new LinkedHashMap<String, Integer>();
			int countOfSubjectsStartedEvent = 0;

			for (SubjectEventStatus subjectEventStatus : subjectEventStatuses) {
				int eventsWithStatus = studyEventDAO.getCountofEventsBasedOnEventStatusAndStudyEventDefinitionId(sb,
						subjectEventStatus, studyEventDefinitions.get(i));

				int eventsWithStatusNoRepeats = studyEventDAO
						.getEventCountFromEventStatusAndStudyEventDefinitionIdNoRepeats(sb, subjectEventStatus,
								studyEventDefinitions.get(i));

				countOfSubjectEventStatuses.put(subjectEventStatus.getName().toLowerCase().replaceAll(" ", "_"),
						eventsWithStatus);

				countOfSubjectsStartedEvent += eventsWithStatusNoRepeats;
			}

			countOfSubjectEventStatuses.put("not_scheduled", countOfSubject - countOfSubjectsStartedEvent);
			currentRow.setId(studyEventDefinitions.get(i).getId());
			currentRow.setRowName(studyEventDefinitions.get(i).getName());
			currentRow.setRowValues(countOfSubjectEventStatuses);
			eventCompletionRows.add(currentRow);
		}

		hasPrevious = displayFrom != 0;

		hasNext = displayFrom + 5 <= studyEventDefinitions.size();

		model.addAttribute("eventCompletionRows", eventCompletionRows);
		model.addAttribute("eventCompletionHasNext", hasNext);
		model.addAttribute("eventCompletionHasPrevious", hasPrevious);
		model.addAttribute("eventCompletionLastElement", displayFrom);

		return page;
	}

	@RequestMapping("/initSubjectStatusCount")
	public String initSubjectStatusCountWidget(HttpServletRequest request, HttpServletResponse response, Model model)
			throws IOException {

		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		response.setDateHeader("Expires", -1);
		response.setHeader("Cache-Control", "no-store");

		ResourceBundleProvider.updateLocale(request.getLocale());

		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(datasource);
		StudyBean sb = (StudyBean) request.getSession().getAttribute("study");

		int availableSubjects = studySubjectDAO.getCountofStudySubjectsBasedOnStatus(sb, Status.AVAILABLE);
		int removedSubjects = studySubjectDAO.getCountofStudySubjectsBasedOnStatus(sb, Status.DELETED);
		int lockedSubjects = studySubjectDAO.getCountofStudySubjectsBasedOnStatus(sb, Status.LOCKED);
		int signedSubjects = studySubjectDAO.getCountofStudySubjectsBasedOnStatus(sb, Status.SIGNED);

		model.addAttribute("countOfAvailableSubjects", availableSubjects);
		model.addAttribute("countOfRemovedSubjects", removedSubjects);
		model.addAttribute("countOfLockedSubjects", lockedSubjects);
		model.addAttribute("countOfSignedSubjects", signedSubjects);

		String page = "widgets/includes/subjectStatusCountChart";

		return page;
	}
}
