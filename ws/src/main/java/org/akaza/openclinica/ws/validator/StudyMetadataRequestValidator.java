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

package org.akaza.openclinica.ws.validator;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.ws.bean.BaseStudyDefinitionBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.sql.DataSource;

public class StudyMetadataRequestValidator implements Validator {

	DataSource dataSource;
	StudyDAO studyDAO;
	StudySubjectDAO studySubjectDAO;
	StudyEventDefinitionDAO studyEventDefinitionDAO;
	UserAccountDAO userAccountDAO;
	BaseVSValidatorImplementation helper;

	public StudyMetadataRequestValidator(DataSource dataSource) {
		this.dataSource = dataSource;
		helper = new BaseVSValidatorImplementation();
	}

	@SuppressWarnings("rawtypes")
	public boolean supports(Class clazz) {
		return BaseStudyDefinitionBean.class.equals(clazz);
	}

	public void validate(Object obj, Errors e) {
		BaseStudyDefinitionBean studyMetadataRequest = (BaseStudyDefinitionBean) obj;

		if (studyMetadataRequest.getStudyUniqueId() == null) {// && studyMetadataRequest.getSiteUniqueId() == null) {
			e.reject("studyEventDefinitionRequestValidator.study_does_not_exist");
			return;
		}
		StudyBean study = helper.verifyStudy(getStudyDAO(), studyMetadataRequest.getStudyUniqueId(), null, e);
		if (study == null)
			return;
		int site_id = -1;
		StudyBean site;
		if (studyMetadataRequest.getSiteUniqueId() != null) {
			site = helper.verifySite(getStudyDAO(), studyMetadataRequest.getStudyUniqueId(),
					studyMetadataRequest.getSiteUniqueId(), null, e);

			if (site != null) {
				site_id = site.getId();
			}
		}
		helper.verifyUser(studyMetadataRequest.getUser(), getUserAccountDAO(), study.getId(), site_id, e);

		//
		// StudyUserRoleBean studySur =
		// getUserAccountDAO().findRoleByUserNameAndStudyId(studyMetadataRequest.getUser().getName(), study.getId());
		// if (studySur.getStatus() != Status.AVAILABLE) {
		// e.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
		// "You do not have sufficient privileges to proceed with this operation.");
		// return;
		// }

		// if (studyMetadataRequest.getStudyUniqueId() != null && studyMetadataRequest.getSiteUniqueId() == null) {
		// StudyBean study = getStudyDAO().findByUniqueIdentifier(studyMetadataRequest.getStudyUniqueId());
		// if (study == null) {
		// e.reject("subjectTransferValidator.study_does_not_exist", new Object[] {
		// studyMetadataRequest.getStudyUniqueId() }, "Study identifier you specified "
		// + studyMetadataRequest.getStudyUniqueId() + " does not correspond to a valid study.");
		// return;
		// }
		//
		// StudyUserRoleBean studySur =
		// getUserAccountDAO().findRoleByUserNameAndStudyId(studyMetadataRequest.getUser().getName(), study.getId());
		// if (studySur.getStatus() != Status.AVAILABLE) {
		// e.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
		// "You do not have sufficient privileges to proceed with this operation.");
		// return;
		// }
		// }
		// if (studyMetadataRequest.getStudyUniqueId() != null && studyMetadataRequest.getSiteUniqueId() != null) {
		// StudyBean study = getStudyDAO().findByUniqueIdentifier(studyMetadataRequest.getStudyUniqueId());
		// StudyBean site = getStudyDAO().findByUniqueIdentifier(studyMetadataRequest.getSiteUniqueId());
		// if (study == null || site == null || site.getParentStudyId() != study.getId()) {
		// e.reject("studyEventDefinitionRequestValidator.invalid_study_identifier_site_identifier");
		// return;
		// }
		// StudyUserRoleBean siteSur =
		// getUserAccountDAO().findRoleByUserNameAndStudyId(studyMetadataRequest.getUser().getName(), site.getId());
		// if (siteSur.getStatus() != Status.AVAILABLE) {
		// e.reject("studyEventDefinitionRequestValidator.insufficient_permissions",
		// "You do not have sufficient privileges to proceed with this operation.");
		// return;
		// }
		// }
	}

	public StudyDAO getStudyDAO() {
		return this.studyDAO != null ? studyDAO : new StudyDAO(dataSource);
	}

	public StudySubjectDAO getStudySubjectDAO() {
		return this.studySubjectDAO != null ? studySubjectDAO : new StudySubjectDAO(dataSource);
	}

	public StudyEventDefinitionDAO getStudyEventDefinitionDAO() {
		return this.studyEventDefinitionDAO != null ? studyEventDefinitionDAO : new StudyEventDefinitionDAO(dataSource);
	}

	public UserAccountDAO getUserAccountDAO() {
		return this.userAccountDAO != null ? userAccountDAO : new UserAccountDAO(dataSource);
	}

}
