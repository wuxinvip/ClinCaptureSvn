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
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 *
 * Created on Sep 23, 2005
 */
package org.akaza.openclinica.control.managestudy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.akaza.openclinica.bean.core.DiscrepancyNoteType;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteStatisticBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.control.RememberLastPage;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.submit.ListNotesTableFactory;
import org.akaza.openclinica.control.submit.SubmitDataServlet;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.jmesa.facade.TableFacade;

/**
 * 
 * View a list of all discrepancy notes in current study
 * 
 * @author ssachs
 * @author jxu
 */
public class ViewNotesServlet extends RememberLastPage {

	private static final long serialVersionUID = 1L;

	public static final String PRINT = "print";
	public static final String RESOLUTION_STATUS = "resolutionStatus";
	public static final String TYPE = "discNoteType";
	public static final String WIN_LOCATION = "window_location";
	public static final String NOTES_TABLE = "notesTable";
	public static final String DISCREPANCY_NOTE_TYPE = "discrepancyNoteType";
	public static final String DISCREPANCY_NOTE_TYPE_PARAM = "listNotes_f_discrepancyNoteBean.disType";
	public static final String DISCREPANCY_NOTE_STATUS_PARAM = "listNotes_f_discrepancyNoteBean.resolutionStatus";
	public static final String DN_LIST_URL = "dnListUrl";
	public static final int ALL = -1;
	private boolean showMoreLink;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.akaza.openclinica.control.core.SecureController#processRequest()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void processRequest() throws Exception {
		analyzeUrl();
		String module = request.getParameter("module");
		String moduleStr = "manage";
		if (module != null && module.trim().length() > 0) {
			if ("submit".equals(module)) {
				request.setAttribute("module", "submit");
				moduleStr = "submit";
			} else if ("admin".equals(module)) {
				request.setAttribute("module", "admin");
				moduleStr = "admin";
			} else {
				request.setAttribute("module", "manage");
			}
		}

		FormProcessor fp = new FormProcessor(request);
		if (fp.getString("showMoreLink").equals("")) {
			showMoreLink = true;
		} else {
			showMoreLink = Boolean.parseBoolean(fp.getString("showMoreLink"));
		}

		int oneSubjectId = fp.getInt("id");
		// BWP 11/03/2008 3029: This session attribute in removed in
		// ResolveDiscrepancyServlet.mayProceed() >>
		session.setAttribute("subjectId", oneSubjectId);
		// >>

		/*
		 * ClinCapture #71 get discrepancy notes' type filter value by name
		 */
		int discNoteTypeId = 0;
		try {
			DiscrepancyNoteType discNoteType = DiscrepancyNoteType.getByName(request
					.getParameter(DISCREPANCY_NOTE_TYPE_PARAM));
			discNoteTypeId = discNoteType.getId();
		} catch (Exception e) {
			e.printStackTrace();
			discNoteTypeId = ALL;
		}
		request.setAttribute(DISCREPANCY_NOTE_TYPE, discNoteTypeId);

		boolean removeSession = fp.getBoolean("removeSession");

		// BWP 11/03/2008 3029: This session attribute in removed in
		// ResolveDiscrepancyServlet.mayProceed() >>
		session.setAttribute("module", module);
		// >>

		// Do we only want to view the notes for 1 subject?
		String viewForOne = fp.getString("viewForOne");

		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(sm.getDataSource());
		dndao.setFetchMapping(true);

		/*
		 * ClinCapture #71 get discrepancy notes' status filter value by name
		 */
		int resolutionStatusId = 0;
		try {
			ResolutionStatus resolutionStatus = ResolutionStatus.getByName(request
					.getParameter(DISCREPANCY_NOTE_STATUS_PARAM));
			resolutionStatusId = resolutionStatus.getId();
		} catch (Exception e) {
			e.printStackTrace();
			resolutionStatusId = ALL;
		}

		if (removeSession) {
			session.removeAttribute(WIN_LOCATION);
			session.removeAttribute(NOTES_TABLE);
		}

		// after resolving a note, user wants to go back to view notes page, we
		// save the current URL
		// so we can go back later
		session.setAttribute(WIN_LOCATION, "ViewNotes?viewForOne=" + viewForOne + "&id=" + oneSubjectId + "&module="
				+ module + " &removeSession=1");

		boolean hasAResolutionStatus = resolutionStatusId >= 1 && resolutionStatusId <= 5;
		Set<Integer> resolutionStatusIds = (HashSet) session.getAttribute(RESOLUTION_STATUS);
		// remove the session if there is no resolution status
		if (!hasAResolutionStatus && resolutionStatusIds != null) {
			session.removeAttribute(RESOLUTION_STATUS);
			resolutionStatusIds = null;
		}
		if (hasAResolutionStatus) {
			if (resolutionStatusIds == null) {
				resolutionStatusIds = new HashSet<Integer>();
			}
			resolutionStatusIds.add(resolutionStatusId);
			session.setAttribute(RESOLUTION_STATUS, resolutionStatusIds);
		}

		StudySubjectDAO subdao = new StudySubjectDAO(sm.getDataSource());
		StudyDAO studyDao = new StudyDAO(sm.getDataSource());

		SubjectDAO sdao = new SubjectDAO(sm.getDataSource());

		UserAccountDAO uadao = new UserAccountDAO(sm.getDataSource());
		CRFVersionDAO crfVersionDao = new CRFVersionDAO(sm.getDataSource());
		CRFDAO crfDao = new CRFDAO(sm.getDataSource());
		StudyEventDAO studyEventDao = new StudyEventDAO(sm.getDataSource());
		StudyEventDefinitionDAO studyEventDefinitionDao = new StudyEventDefinitionDAO(sm.getDataSource());
		EventDefinitionCRFDAO eventDefinitionCRFDao = new EventDefinitionCRFDAO(sm.getDataSource());
		ItemDataDAO itemDataDao = new ItemDataDAO(sm.getDataSource());
		ItemDAO itemDao = new ItemDAO(sm.getDataSource());
		EventCRFDAO eventCRFDao = new EventCRFDAO(sm.getDataSource());

		ListNotesTableFactory factory = new ListNotesTableFactory(showMoreLink);
		factory.setSubjectDao(sdao);
		factory.setStudySubjectDao(subdao);
		factory.setUserAccountDao(uadao);
		factory.setStudyDao(studyDao);
		factory.setCurrentStudy(currentStudy);
		factory.setDiscrepancyNoteDao(dndao);
		factory.setCrfDao(crfDao);
		factory.setCrfVersionDao(crfVersionDao);
		factory.setStudyEventDao(studyEventDao);
		factory.setStudyEventDefinitionDao(studyEventDefinitionDao);
		factory.setEventDefinitionCRFDao(eventDefinitionCRFDao);
		factory.setItemDao(itemDao);
		factory.setItemDataDao(itemDataDao);
		factory.setEventCRFDao(eventCRFDao);
		factory.setModule(moduleStr);
		factory.setDiscNoteType(discNoteTypeId);
		factory.setResolutionStatus(resolutionStatusId);

		// Set data source
		factory.setDataSource(sm.getDataSource());

		TableFacade tf = factory.createTable(request, response);

		if ("yes".equalsIgnoreCase(fp.getString(PRINT))) {
			request.setAttribute("allNotes", factory.getNotesForPrintPop(tf.getLimit()));
			forwardPage(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY_PRINT);
			return;
		}

		String viewNotesHtml = tf.render();

		request.setAttribute("viewNotesHtml", viewNotesHtml);
		String viewNotesURL = this.getPageURL();
		session.setAttribute("viewNotesURL", viewNotesURL);
		String viewNotesPageFileName = this.getPageServletFileName();
		session.setAttribute("viewNotesPageFileName", viewNotesPageFileName);

		List<DiscrepancyNoteStatisticBean> statisticBeans = dndao.countNotesStatistic(currentStudy);
		Map<String, Map<String, String>> customStat = ListNotesTableFactory.getNotesStatistics(statisticBeans);
		Map<String, String> customTotalMap = ListNotesTableFactory.getNotesTypesStatistics(statisticBeans);

		request.setAttribute("summaryMap", customStat);
		request.setAttribute("mapKeys", ResolutionStatus.getMembers());
		request.setAttribute("typeNames", DiscrepancyNoteUtil.getTypeNames(resterm));
		request.setAttribute("typeKeys", customTotalMap);
		request.setAttribute("grandTotal", customTotalMap.get("Total"));
		// long endTime = System.currentTimeMillis();

		// System.out.println("Time taken[" + (startTime)/1000 + "]");
		// System.out.println("Time taken[" + (endTime)/1000 + "]");
		// System.out.println("Time taken[" + (endTime - startTime)/1000 + "]");

		analyzeForward(Page.VIEW_DISCREPANCY_NOTES_IN_STUDY);
	}

	@SuppressWarnings("rawtypes")
	public ArrayList<DiscrepancyNoteBean> filterForOneSubject(ArrayList<DiscrepancyNoteBean> allNotes, int subjectId,
			int resolutionStatus) {

		if (allNotes == null || allNotes.isEmpty() || subjectId == 0)
			return allNotes;
		// Are the D Notes filtered by resolution?
		boolean filterByRes = resolutionStatus >= 1 && resolutionStatus <= 5;

		ArrayList<DiscrepancyNoteBean> filteredNotes = new ArrayList<DiscrepancyNoteBean>();
		StudySubjectDAO subjectDao = new StudySubjectDAO(sm.getDataSource());
		StudySubjectBean studySubjBean = (StudySubjectBean) subjectDao.findByPK(subjectId);

		for (DiscrepancyNoteBean discBean : allNotes) {
			if (discBean.getSubjectName().equalsIgnoreCase(studySubjBean.getLabel())) {
				if (!filterByRes) {
					filteredNotes.add(discBean);
				} else {
					if (discBean.getResolutionStatusId() == resolutionStatus) {
						filteredNotes.add(discBean);
					}
				}
			}
		}

		return filteredNotes;
	}

	@Override
	protected void mayProceed() throws InsufficientPermissionException {

		if (SubmitDataServlet.mayViewData(ub, currentRole)) {
			return;
		}

		addPageMessage(respage.getString("no_permission_to_view_discrepancies")
				+ respage.getString("change_study_contact_sysadmin"));
		throw new InsufficientPermissionException(Page.MENU_SERVLET,
				resexception.getString("not_study_director_or_study_cordinator"), "1");
	}

	@Override
	protected String getUrlKey() {
		return DN_LIST_URL;
	}

	@Override
	protected String getDefaultUrl() {
		FormProcessor fp = new FormProcessor(request);
		return "?module=" + fp.getString("module")
				+ "&maxRows=15&showMoreLink=true&listNotes_tr_=true&listNotes_p_=1&listNotes_mr_=15";
	}

	@Override
	protected boolean userDoesNotUseJmesaTableForNavigation() {
		return request.getQueryString() == null || !request.getQueryString().contains("&listNotes_")
				|| request.getQueryString().contains("&print=yes");
	}
}
