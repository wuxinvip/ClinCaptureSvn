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
package org.akaza.openclinica.control.extract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.control.RememberLastPage;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.bean.DatasetRow;
import org.akaza.openclinica.web.bean.EntityBeanTable;

/**
 * ViewDatasetsServlet.java, the view datasets function accessed from the extract datasets main page.
 * 
 * @author thickerson
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked", "serial"})
public class ViewDatasetsServlet extends RememberLastPage {

	Locale locale;

    public static final String SAVED_VIEW_DATASETS_URL = "savedViewDatasetsUrl";

	public static String getLink(int dsId) {
		return "ViewDatasets?action=details&datasetId=" + dsId;
	}

	@Override
	public void processRequest() throws Exception {
        analyzeUrl();
		DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
		String action = request.getParameter("action");
		resetPanel();
		request.setAttribute(STUDY_INFO_PANEL, panel);
		session.removeAttribute("newDataset");
		if (StringUtil.isBlank(action)) {
			StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(sm.getDataSource());
			StudyBean studyWithEventDefinitions = currentStudy;
			if (currentStudy.getParentStudyId() > 0) {
				studyWithEventDefinitions = new StudyBean();
				studyWithEventDefinitions.setId(currentStudy.getParentStudyId());

			}
			ArrayList seds = seddao.findAllActiveByStudy(studyWithEventDefinitions);
			CRFDAO crfdao = new CRFDAO(sm.getDataSource());
			HashMap events = new LinkedHashMap();
			for (int i = 0; i < seds.size(); i++) {
				StudyEventDefinitionBean sed = (StudyEventDefinitionBean) seds.get(i);
				ArrayList crfs = (ArrayList) crfdao.findAllActiveByDefinition(sed);
				if (!crfs.isEmpty()) {
					events.put(sed, crfs);
				}
			}
			session.setAttribute("eventsForCreateDataset", events);
			// YW >>

			FormProcessor fp = new FormProcessor(request);

			EntityBeanTable table = fp.getEntityBeanTable();
			ArrayList datasets = new ArrayList();
			// if (ub.isSysAdmin()) {
			// datasets = dsdao.findAllByStudyIdAdmin(currentStudy.getId());
			// } else {
			datasets = dsdao.findAllByStudyId(currentStudy.getId());
			// }

			ArrayList datasetRows = DatasetRow.generateRowsFromBeans(datasets);

			String[] columns = { resword.getString("dataset_name"), resword.getString("description"),
					resword.getString("created_by"), resword.getString("created_date"), resword.getString("status"),
					resword.getString("actions") };
			table.setColumns(new ArrayList(Arrays.asList(columns)));
			table.hideColumnLink(5);
			table.addLink(resword.getString("show_only_my_datasets"), "ViewDatasets?action=owner&ownerId=" + ub.getId());
			// table.addLink(resword.getString("create_dataset"), "CreateDataset");
			table.setQuery("ViewDatasets", new HashMap());
			table.setRows(datasetRows);
			table.computeDisplay();

			request.setAttribute("table", table);
			// this is the old code that the tabling code replaced:
			// ArrayList datasets = (ArrayList)dsdao.findAll();
			// request.setAttribute("datasets", datasets);
            analyzeForward(Page.VIEW_DATASETS);
		} else {
			if ("owner".equalsIgnoreCase(action)) {
				FormProcessor fp = new FormProcessor(request);
				int ownerId = fp.getInt("ownerId");
				EntityBeanTable table = fp.getEntityBeanTable();

				ArrayList datasets = (ArrayList) dsdao.findByOwnerId(ownerId, currentStudy.getId());

				/*
				 * if (datasets.isEmpty()) { forwardPage(Page.VIEW_EMPTY_DATASETS); } else {
				 */

				ArrayList datasetRows = DatasetRow.generateRowsFromBeans(datasets);
				String[] columns = { resword.getString("dataset_name"), resword.getString("description"),
						resword.getString("created_by"), resword.getString("created_date"),
						resword.getString("status"), resword.getString("actions") };
				table.setColumns(new ArrayList(Arrays.asList(columns)));
				table.hideColumnLink(5);
				table.addLink(resword.getString("show_all_datasets"), "ViewDatasets");
				// table.addLink(resword.getString("create_dataset"), "CreateDataset");
				table.setQuery("ViewDatasets?action=owner&ownerId=" + ub.getId(), new HashMap());
				table.setRows(datasetRows);
				table.computeDisplay();
				request.setAttribute("table", table);
				// this is the old code:

				// ArrayList datasets = (ArrayList)dsdao.findByOwnerId(ownerId);
				// request.setAttribute("datasets", datasets);
                analyzeForward(Page.VIEW_DATASETS);
				// }
			} else if ("details".equalsIgnoreCase(action)) {
				FormProcessor fp = new FormProcessor(request);
				int datasetId = fp.getInt("datasetId");

				DatasetBean db = initializeAttributes(datasetId);
				StudyDAO sdao = new StudyDAO(sm.getDataSource());
				StudyBean study = (StudyBean) sdao.findByPK(db.getStudyId());

				if (study.getId() != currentStudy.getId() && study.getParentStudyId() != currentStudy.getId()) {
					addPageMessage(respage.getString("no_have_correct_privilege_current_study") + " "
							+ respage.getString("change_active_study_or_contact"));
					forwardPage(Page.MENU_SERVLET);
					return;
				}

				/*
				 * EntityBeanTable table = fp.getEntityBeanTable(); ArrayList datasetRows =
				 * DatasetRow.generateRowFromBean(db); String[] columns = { "Dataset Name", "Description", "Created By",
				 * "Created Date", "Status", "Actions" }; table.setColumns(new ArrayList(Arrays.asList(columns)));
				 * table.hideColumnLink(5); table.setQuery("ViewDatasets", new HashMap()); table.setRows(datasetRows);
				 * table.computeDisplay(); request.setAttribute("table", table);
				 */
				request.setAttribute("dataset", db);

				forwardPage(Page.VIEW_DATASET_DETAILS);
			}
		}

	}

	@Override
	public void mayProceed() throws InsufficientPermissionException {

		locale = request.getLocale();

		if (ub.isSysAdmin()) {
			return;
		}

		if (!currentRole.getRole().equals(Role.CLINICAL_RESEARCH_COORDINATOR)) {
			return;
		}

		addPageMessage(respage.getString("no_have_correct_privilege_current_study")
				+ respage.getString("change_study_contact_sysadmin"));
		throw new InsufficientPermissionException(Page.MENU,
				resexception.getString("not_allowed_access_extract_data_servlet"), "1");

	}

	/**
	 * Initialize data of a DatasetBean and set session attributes for displaying selected data of this DatasetBean
	 * 
	 * @param db
	 * @return
	 * 
	 * @author ywang (Feb, 2008)
	 */
	public DatasetBean initializeAttributes(int datasetId) {
		DatasetDAO dsdao = new DatasetDAO(sm.getDataSource());
		DatasetBean db = dsdao.initialDatasetData(datasetId);
		session.setAttribute("newDataset", db);
		StudyGroupClassDAO sgcdao = new StudyGroupClassDAO(sm.getDataSource());
		StudyDAO studydao = new StudyDAO(sm.getDataSource());
		StudyBean theStudy = (StudyBean) studydao.findByPK(sm.getUserBean().getActiveStudyId());
		ArrayList<StudyGroupClassBean> allSelectedGroups = sgcdao.findAllActiveByStudy(theStudy);
		ArrayList<Integer> selectedSubjectGroupIds = db.getSubjectGroupIds();
		if (selectedSubjectGroupIds != null && allSelectedGroups != null) {
			for (Integer id : selectedSubjectGroupIds) {
				for (int i = 0; i < allSelectedGroups.size(); ++i) {
					if (allSelectedGroups.get(i).getId() == id) {
						allSelectedGroups.get(i).setSelected(true);
						break;
					}
				}
			}
		}
        db.setAllSelectedGroups(allSelectedGroups);

		return db;
	}

    @Override
    protected String getUrlKey() {
        return SAVED_VIEW_DATASETS_URL;
    }

    @Override
    protected String getDefaultUrl() {
        FormProcessor fp = new FormProcessor(request);
        String eblFiltered = fp.getString("ebl_filtered");
        String eblFilterKeyword = fp.getString("ebl_filterKeyword");
        String eblSortColumnInd = fp.getString("ebl_sortColumnInd");
        String eblSortAscending = fp.getString("ebl_sortAscending");
        return "?submitted=1&module=" + fp.getString("module") + "&ebl_page=1&ebl_sortColumnInd="
                + (!eblSortColumnInd.isEmpty() ? eblSortColumnInd : "0") + "&ebl_sortAscending="
                + (!eblSortAscending.isEmpty() ? eblSortAscending : "1") + "&ebl_filtered="
                + (!eblFiltered.isEmpty() ? eblFiltered : "0") + "&ebl_filterKeyword="
                + (!eblFilterKeyword.isEmpty() ? eblFilterKeyword : "") + "&&ebl_paginated=1";
    }

    @Override
    protected boolean userDoesNotUseJmesaTableForNavigation() {
        return request.getQueryString() == null || !request.getQueryString().contains("&ebl_page=");
    }
}
