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

import java.util.ArrayList;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.service.StudyParamsConfig;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.control.core.SecureController;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.domain.SourceDataVerification;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.web.InsufficientPermissionException;

@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class InitUpdateSubStudyServlet extends SecureController {

	/**
     * 
     */
	@Override
	public void mayProceed() throws InsufficientPermissionException {
		checkStudyLocked(Page.SITE_LIST_SERVLET, respage.getString("current_study_locked"));
		if (ub.isSysAdmin()) {
			return;
		}
		if (currentRole.getRole().equals(Role.STUDY_DIRECTOR) || currentRole.getRole().equals(Role.STUDY_ADMINISTRATOR)) {
			return;
		}

		addPageMessage(respage.getString("no_have_correct_privilege_current_study")
				+ respage.getString("change_study_contact_sysadmin"));
		throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("not_study_director"), "1");

	}

	@Override
	public void processRequest() throws Exception {

		StudyDAO sdao = new StudyDAO(sm.getDataSource());
		String idString = request.getParameter("id");
		logger.info("study id:" + idString);
		if (StringUtil.isBlank(idString)) {
			addPageMessage(respage.getString("please_choose_a_study_to_edit"));
			forwardPage(Page.STUDY_LIST_SERVLET);
		} else {
			int studyId = Integer.valueOf(idString.trim()).intValue();
			StudyBean study = (StudyBean) sdao.findByPK(studyId);

			checkRoleByUserAndStudy(ub, study.getParentStudyId(), study.getId());

			String parentStudyName = "";
			StudyBean parent = new StudyBean();
			if (study.getParentStudyId() > 0) {
				parent = (StudyBean) sdao.findByPK(study.getParentStudyId());
				parentStudyName = parent.getName();
				// at this time, this feature is only available for site
				createEventDefinitions(parent);
			}

			if (currentStudy.getId() != study.getId()) {
				ArrayList parentConfigs = currentStudy.getStudyParameters();
				ArrayList configs = new ArrayList();
				StudyParameterValueDAO spvdao = new StudyParameterValueDAO(sm.getDataSource());
				for (int i = 0; i < parentConfigs.size(); i++) {
					StudyParamsConfig scg = (StudyParamsConfig) parentConfigs.get(i);
					if (scg != null) {
						// find the one that sub study can change
						if (scg.getValue().getId() > 0 && scg.getParameter().isOverridable()) {
							StudyParameterValueBean spvb = spvdao.findByHandleAndStudy(study.getId(), scg
									.getParameter().getHandle());
							if (spvb.getId() > 0) {
								// the sub study itself has the parameter
								scg.setValue(spvb);
							}
							configs.add(scg);
						}
					}

				}

				study.setStudyParameters(configs);
			}
			request.setAttribute("parentStudy", parent);
			session.setAttribute("parentName", parentStudyName);
			session.setAttribute("newStudy", study);
			request.setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
			request.setAttribute("statuses", Status.toStudyUpdateMembersList());

			FormProcessor fp = new FormProcessor(request);
			logger.info("start date:" + study.getDatePlannedEnd());
			if (study.getDatePlannedEnd() != null) {
				fp.addPresetValue(UpdateSubStudyServlet.INPUT_END_DATE, local_df.format(study.getDatePlannedEnd()));
			}
			if (study.getDatePlannedStart() != null) {
				fp.addPresetValue(UpdateSubStudyServlet.INPUT_START_DATE, local_df.format(study.getDatePlannedStart()));
			}
			setPresetValues(fp.getPresetValues());
			if (study.getProtocolDateVerification() != null) {
				fp.addPresetValue(UpdateSubStudyServlet.INPUT_VER_DATE,
						local_df.format(study.getProtocolDateVerification()));
			}

			forwardPage(Page.UPDATE_SUB_STUDY);
		}

	}

	private void createEventDefinitions(StudyBean parentStudy) {
		int siteId = Integer.valueOf(request.getParameter("id").trim());
		ArrayList<StudyEventDefinitionBean> seds = new ArrayList<StudyEventDefinitionBean>();
		StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(sm.getDataSource());
		EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(sm.getDataSource());
		CRFVersionDAO cvdao = new CRFVersionDAO(sm.getDataSource());
		CRFDAO cdao = new CRFDAO(sm.getDataSource());
		seds = sedDao.findAllByStudy(parentStudy);
		for (StudyEventDefinitionBean sed : seds) {
			int defId = sed.getId();
			ArrayList<EventDefinitionCRFBean> edcs = (ArrayList<EventDefinitionCRFBean>) edcdao
					.findAllByDefinitionAndSiteIdAndParentStudyId(defId, siteId, parentStudy.getId());
			ArrayList<EventDefinitionCRFBean> defCrfs = new ArrayList<EventDefinitionCRFBean>();
			for (EventDefinitionCRFBean edcBean : edcs) {
				int edcStatusId = edcBean.getStatus().getId();
				CRFBean crf = (CRFBean) cdao.findByPK(edcBean.getCrfId());
				int crfStatusId = crf.getStatusId();
				if (edcStatusId == 5 || edcStatusId == 7 || crfStatusId == 5 || crfStatusId == 7) {
				} else {
					ArrayList<CRFVersionBean> versions = (ArrayList<CRFVersionBean>) cvdao.findAllActiveByCRF(edcBean
							.getCrfId());
					edcBean.setVersions(versions);
					edcBean.setCrfName(crf.getName());
					CRFVersionBean defaultVersion = (CRFVersionBean) cvdao.findByPK(edcBean.getDefaultVersionId());
					edcBean.setDefaultVersionName(defaultVersion.getName());
					String sversionIds = edcBean.getSelectedVersionIds();
					ArrayList<Integer> idList = new ArrayList<Integer>();
					if (sversionIds.length() > 0) {
						String[] ids = sversionIds.split("\\,");
						for (String id : ids) {
							idList.add(Integer.valueOf(id));
						}
					}
					edcBean.setSelectedVersionIdList(idList);
					defCrfs.add(edcBean);
				}
			}
			logger.debug("definitionCrfs size=" + defCrfs.size() + " total size=" + edcs.size());
			sed.setCrfs(defCrfs);
			sed.setCrfNum(defCrfs.size());
		}
		// not sure if request is better, since not sure if there is another
		// process using this.
		session.setAttribute("definitions", seds);
		session.setAttribute("sdvOptions", this.setSDVOptions());
	}

	private ArrayList<String> setSDVOptions() {
		ArrayList<String> sdvOptions = new ArrayList<String>();
		sdvOptions.add(SourceDataVerification.AllREQUIRED.toString());
		sdvOptions.add(SourceDataVerification.PARTIALREQUIRED.toString());
		sdvOptions.add(SourceDataVerification.NOTREQUIRED.toString());
		sdvOptions.add(SourceDataVerification.NOTAPPLICABLE.toString());
		return sdvOptions;
	}

	@Override
	protected String getAdminServlet() {
		if (ub.isSysAdmin()) {
			return SecureController.ADMIN_SERVLET_CODE;
		} else {
			return "";
		}
	}

}
