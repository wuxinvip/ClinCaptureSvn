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

import com.clinovo.util.EventDefinitionCRFUtil;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SpringServlet;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

/**
 * View the details of a study event definition.
 *
 * @author jxu
 */
@SuppressWarnings({"unchecked", "rawtypes"})
@Component
public class ViewEventDefinitionServlet extends SpringServlet {
	/**
	 * Checks whether the user has the correct privilege.
	 */
	@Override
	public void mayProceed(HttpServletRequest request, HttpServletResponse response)
			throws InsufficientPermissionException {
		UserAccountBean ub = getUserAccountBean(request);
		StudyUserRoleBean currentRole = getCurrentRole(request);

		if (ub.isSysAdmin() || currentRole.getRole().equals(Role.STUDY_ADMINISTRATOR)) {
			return;
		}
		addPageMessage(getResPage().getString("no_have_correct_privilege_current_study") + " "
						+ getResPage().getString("change_study_contact_sysadmin"), request);
		throw new InsufficientPermissionException(Page.LIST_DEFINITION_SERVLET,
				getResException().getString("not_director"), "1");
	}

	@Override
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		StudyBean currentStudy = getCurrentStudy(request);
		FormProcessor fp = new FormProcessor(request);
		int defId = fp.getInt("id", true);

		if (defId == 0) {
			addPageMessage(getResPage().getString("please_choose_a_definition_to_view"), request);
			forwardPage(Page.LIST_DEFINITION_SERVLET, request, response);
			return;
		}
		StudyEventDefinitionDAO eventDefinitionDAO = new StudyEventDefinitionDAO(getDataSource());
		StudyEventDefinitionBean sed = (StudyEventDefinitionBean) eventDefinitionDAO.findByPK(defId);

		if (currentStudy.getId() != sed.getStudyId()) {
			addPageMessage(getResPage().getString("no_have_correct_privilege_current_study") + " "
							+ getResPage().getString("change_active_study_or_contact"), request);
			forwardPage(Page.MENU_SERVLET, request, response);
			return;
		}
		checkRoleByUserAndStudy(request, response, getUserAccountBean(request), sed.getStudyId(), 0);
		EventDefinitionCRFDAO edao = new EventDefinitionCRFDAO(getDataSource());
		ArrayList<EventDefinitionCRFBean> eventDefinitionCRFs = (ArrayList<EventDefinitionCRFBean>) edao.findAllByDefinition(currentStudy, defId);
		CRFVersionDAO cvdao = new CRFVersionDAO(getDataSource());
		CRFDAO cdao = new CRFDAO(getDataSource());
		EventDefinitionCRFUtil.resetAddedEvents(request.getSession());
		boolean showEvaluationColumn = currentStudy.getStudyParameterConfig().getStudyEvaluator().equalsIgnoreCase("yes");

		for (EventDefinitionCRFBean edc : eventDefinitionCRFs) {
			ArrayList versions = (ArrayList) cvdao.findAllByCRF(edc.getCrfId());
			edc.setVersions(versions);
			CRFBean crf = (CRFBean) cdao.findByPK(edc.getCrfId());
			edc.setCrfName(crf.getName());
			if (crf.getStatus().equals(Status.AVAILABLE)) {
				edc.setOwner(crf.getOwner());
			}
			CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edc.getDefaultVersionId());
			edc.setDefaultVersionName(defaultVersion.getName());
			if (edc.isEvaluatedCRF()) {
				showEvaluationColumn = true;
			}
		}
		request.setAttribute("definition", sed);
		request.setAttribute("defSize", eventDefinitionCRFs.size());
		request.setAttribute("showEvaluationColumn", showEvaluationColumn);
		request.setAttribute(EventDefinitionCRFUtil.EVENT_DEFINITION_CRFS_LABEL, eventDefinitionCRFs);
		forwardPage(Page.VIEW_EVENT_DEFINITION, request, response);
	}
}
