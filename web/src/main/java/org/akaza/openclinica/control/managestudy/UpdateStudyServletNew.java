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

import com.clinovo.exception.CodeException;
import com.clinovo.model.DiscrepancyDescription;
import com.clinovo.model.DiscrepancyDescriptionType;
import com.clinovo.service.DiscrepancyDescriptionService;
import com.clinovo.util.ValidatorHelper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.InterventionBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.Controller;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.springframework.stereotype.Component;

@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
@Component
public class UpdateStudyServletNew extends Controller {

	public static final String INPUT_START_DATE = "startDate";
	public static final String INPUT_END_DATE = "endDate";
	public static final String INPUT_VER_DATE = "protocolDateVerification";

	/**
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 */
	@Override
	public void mayProceed(HttpServletRequest request, HttpServletResponse response)
			throws InsufficientPermissionException {
		UserAccountBean ub = getUserAccountBean(request);
		StudyUserRoleBean currentRole = getCurrentRole(request);

		if (ub.isSysAdmin()) {
			return;
		}
		Role r = currentRole.getRole();
		if (r.equals(Role.STUDY_DIRECTOR) || r.equals(Role.STUDY_ADMINISTRATOR)) {
			return;
		}
		addPageMessage(
				respage.getString("no_have_correct_privilege_current_study")
						+ respage.getString("change_study_contact_sysadmin"), request);
		throw new InsufficientPermissionException(Page.MENU_SERVLET, resexception.getString("may_not_submit_data"), "1");
	}

	@Override
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

		StudyBean currentStudy = getCurrentStudy(request);

		HashMap errors = getErrorsHolder(request);
		StudyInfoPanel panel = getStudyInfoPanel(request);
		panel.reset();

		FormProcessor fp = new FormProcessor(request);
		Validator v = new Validator(new ValidatorHelper(request, getConfigurationDao()));
		int studyId = fp.getInt("id");
		studyId = studyId == 0 ? fp.getInt("studyId") : studyId;
		String action = fp.getString("action");
		StudyDAO sdao = new StudyDAO(getDataSource());
		DiscrepancyDescriptionService dDescriptionService = (DiscrepancyDescriptionService) SpringServletAccess
				.getApplicationContext(getServletContext()).getBean("discrepancyDescriptionService");

		Map<String, List<DiscrepancyDescription>> dDescriptionsMap = dDescriptionService
				.findAllSortedDescriptionsFromStudy(studyId);

		StudyBean study = (StudyBean) sdao.findByPK(studyId);
		if (study.getId() != currentStudy.getId()) {
			addPageMessage(respage.getString("not_current_study") + respage.getString("change_study_contact_sysadmin"),
					request);
			forwardPage(Page.MENU_SERVLET, request, response);
			return;
		}

		study.setId(studyId);
		StudyConfigService scs = new StudyConfigService(getDataSource());
		study = scs.setParametersForStudy(study);

		request.setAttribute("dDescriptionsMap", dDescriptionsMap);

		request.setAttribute("studyToView", study);
		request.setAttribute("studyId", studyId + "");
		request.setAttribute("studyPhaseMap", CreateStudyServlet.studyPhaseMap);
		ArrayList statuses = Status.toStudyUpdateMembersList();
		statuses.add(Status.PENDING);
		request.setAttribute("statuses", statuses);

		String interventional = resadmin.getString("interventional");
		boolean isInterventional = interventional.equalsIgnoreCase(study.getProtocolType());

		request.setAttribute("isInterventional", isInterventional ? "1" : "0");
		String protocolType = study.getProtocolTypeKey();

		if (study.getParentStudyId() > 0) {
			StudyBean parentStudy = (StudyBean) sdao.findByPK(study.getParentStudyId());
			request.setAttribute("parentStudy", parentStudy);
		}

		ArrayList interventionArray = new ArrayList();
		if (isInterventional) {
			interventionArray = parseInterventions((study));
		}
		setMaps(request, isInterventional, interventionArray);

		SimpleDateFormat local_df = getLocalDf(request);

		if (!action.equals("submit")) {

			// First Load First Form
			if (study.getDatePlannedStart() != null) {
				fp.addPresetValue(INPUT_START_DATE, local_df.format(study.getDatePlannedStart()));
			}
			if (study.getDatePlannedEnd() != null) {
				fp.addPresetValue(INPUT_END_DATE, local_df.format(study.getDatePlannedEnd()));
			}
			if (study.getProtocolDateVerification() != null) {
				fp.addPresetValue(INPUT_VER_DATE, local_df.format(study.getProtocolDateVerification()));
			}
			setPresetValues(fp.getPresetValues(), request);
			// first load 2nd form
		}
		if (action.equals("submit")) {

			validateStudy1(fp, study, errors, v);
			validateStudy2(fp, study, v);
			validateStudy3(fp, study, v, isInterventional);
			validateStudy4(fp, study, errors, v);
			validateStudy5(fp, study, errors, v);
			validateStudy6(fp, study, errors, v);
			validateStudy7(fp, errors, v, study.getId(), dDescriptionsMap);
			confirmWholeStudy(fp, study, errors, v);

			request.setAttribute("studyToView", study);
			if (!errors.isEmpty()) {
				logger.debug("found errors : " + errors.toString());
				request.setAttribute("formMessages", errors);
				request.setAttribute("dDescriptions", dDescriptionsMap);

				forwardPage(Page.UPDATE_STUDY_NEW, request, response);
			} else {
				study.setProtocolType(protocolType);
				submitStudy(study, dDescriptionsMap, request);
				study.setStudyParameters(getStudyParameterValueDAO().findParamConfigByStudy(study));
				addPageMessage(respage.getString("the_study_has_been_updated_succesfully"), request);
				ArrayList pageMessages = (ArrayList) request.getAttribute(PAGE_MESSAGE);
				request.getSession().setAttribute("pageMessages", pageMessages);
				response.sendRedirect(request.getContextPath() + "/pages/studymodule");
			}
		} else {
			forwardPage(Page.UPDATE_STUDY_NEW, request, response);
		}
	}

	private void validateStudy1(FormProcessor fp, StudyBean study, HashMap errors, Validator v) {

		v.addValidation("name", Validator.NO_BLANKS);
		v.addValidation("uniqueProId", Validator.NO_BLANKS);
		v.addValidation("description", Validator.NO_BLANKS);
		v.addValidation("prinInvestigator", Validator.NO_BLANKS);
		v.addValidation("sponsor", Validator.NO_BLANKS);

		v.addValidation("secondProId", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("collaborators", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);
		v.addValidation("protocolDescription", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1000);

		v.addValidation("studySubjectIdLabel", Validator.NO_BLANKS);
		v.addValidation("secondaryIdLabel", Validator.NO_BLANKS);
		v.addValidation("dateOfEnrollmentForStudyLabel", Validator.NO_BLANKS);
		v.addValidation("genderLabel", Validator.NO_BLANKS);

		v.addValidation("startDateTimeLabel", Validator.NO_BLANKS);
		v.addValidation("endDateTimeLabel", Validator.NO_BLANKS);

		errors.putAll(v.validate());

		if (fp.getString("name").trim().length() > 100) {
			Validator.addError(errors, "name", resexception.getString("maximum_lenght_name_100"));
		}
		if (fp.getString("uniqueProId").trim().length() > 30) {
			Validator.addError(errors, "uniqueProId", resexception.getString("maximum_lenght_unique_protocol_30"));
		}
		if (fp.getString("description").trim().length() > 2000) {
			Validator.addError(errors, "description", resexception.getString("maximum_lenght_brief_summary_2000"));
		}
		if (fp.getString("prinInvestigator").trim().length() > 255) {
			Validator.addError(errors, "prinInvestigator",
					resexception.getString("maximum_lenght_principal_investigator_255"));
		}
		if (fp.getString("sponsor").trim().length() > 255) {
			Validator.addError(errors, "sponsor", resexception.getString("maximum_lenght_sponsor_255"));
		}
		if (fp.getString("officialTitle").trim().length() > 255) {
			Validator.addError(errors, "officialTitle", resexception.getString("maximum_lenght_official_title_255"));
		}

		if (fp.getString("studySubjectIdLabel").trim().length() > 255) {
			Validator.addError(errors, "studySubjectIdLabel",
					resexception.getString("maximum_lenght_studySubjectIdLabel_255"));
		}
		if (fp.getString("secondaryIdLabel").trim().length() > 255) {
			Validator.addError(errors, "secondaryIdLabel",
					resexception.getString("maximum_lenght_secondaryIdLabel_255"));
		}
		if (fp.getString("dateOfEnrollmentForStudyLabel").trim().length() > 255) {
			Validator.addError(errors, "dateOfEnrollmentForStudyLabel",
					resexception.getString("maximum_lenght_dateOfEnrollmentForStudyLabel_255"));
		}
		if (fp.getString("genderLabel").trim().length() > 255) {
			Validator.addError(errors, "genderLabel", resexception.getString("maximum_lenght_genderLabel_255"));
		}

		if (fp.getString("startDateTimeLabel").trim().length() > 255) {
			Validator.addError(errors, "startDateTimeLabel",
					resexception.getString("maximum_lenght_startDateTimeLabel_255"));
		}
		if (fp.getString("endDateTimeLabel").trim().length() > 255) {
			Validator.addError(errors, "endDateTimeLabel",
					resexception.getString("maximum_lenght_endDateTimeLabel_255"));
		}

		createStudyBean(fp, study);
	}

	private void validateStudy2(FormProcessor fp, StudyBean study, Validator v) {

		v.addValidation(INPUT_START_DATE, Validator.IS_A_DATE);
		if (!StringUtil.isBlank(fp.getString(INPUT_END_DATE))) {
			v.addValidation(INPUT_END_DATE, Validator.IS_A_DATE);
		}
		if (!StringUtil.isBlank(fp.getString(INPUT_VER_DATE))) {
			v.addValidation(INPUT_VER_DATE, Validator.IS_A_DATE);
		}

		logger.info("has validation errors");
		SimpleDateFormat local_df = getLocalDf(fp.getRequest());
		try {
			local_df.parse(fp.getString(INPUT_START_DATE));
			fp.addPresetValue(INPUT_START_DATE, local_df.format(fp.getDate(INPUT_START_DATE)));
		} catch (ParseException pe) {
			fp.addPresetValue(INPUT_START_DATE, fp.getString(INPUT_START_DATE));
		}
		try {
			local_df.parse(fp.getString(INPUT_VER_DATE));
			fp.addPresetValue(INPUT_VER_DATE, local_df.format(fp.getDate(INPUT_VER_DATE)));
		} catch (ParseException pe) {
			fp.addPresetValue(INPUT_VER_DATE, fp.getString(INPUT_VER_DATE));
		}
		try {
			local_df.parse(fp.getString(INPUT_END_DATE));
			fp.addPresetValue(INPUT_END_DATE, local_df.format(fp.getDate(INPUT_END_DATE)));
		} catch (ParseException pe) {
			fp.addPresetValue(INPUT_END_DATE, fp.getString(INPUT_END_DATE));
		}
		updateStudy2(fp, study);
		setPresetValues(fp.getPresetValues(), fp.getRequest());

	}

	private void validateStudy3(FormProcessor fp, StudyBean study, Validator v, boolean isInterventional) {

		v.addValidation("purpose", Validator.NO_BLANKS);
		for (int i = 0; i < 10; i++) {
			String type = fp.getString("interType" + i);
			String name = fp.getString("interName" + i);
			if (!StringUtil.isBlank(type) && StringUtil.isBlank(name)) {
				v.addValidation("interName", Validator.NO_BLANKS);
				fp.getRequest().setAttribute("interventionError", respage.getString("name_cannot_be_blank_if_type"));
				break;
			}
			if (!StringUtil.isBlank(name) && StringUtil.isBlank(type)) {
				v.addValidation("interType", Validator.NO_BLANKS);
				fp.getRequest().setAttribute("interventionError", respage.getString("name_cannot_be_blank_if_name"));
				break;
			}
		}
		updateStudy3(study, isInterventional, fp);

	}

	private void validateStudy4(FormProcessor fp, StudyBean study, HashMap errors, Validator v) {

		v.addValidation("conditions", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 500);
		v.addValidation("keywords", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("eligibility", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 500);
		errors.putAll(v.validate());
		if (fp.getInt("expectedTotalEnrollment") <= 0) {
			Validator.addError(errors, "expectedTotalEnrollment",
					respage.getString("expected_total_enrollment_must_be_a_positive_number"));
		}

		study.setConditions(fp.getString("conditions"));
		study.setKeywords(fp.getString("keywords"));
		study.setEligibility(fp.getString("eligibility"));
		study.setGender(fp.getString("gender"));
		if (fp.getString("ageMax").length() > 3) {
			Validator.addError(errors, "ageMax", respage.getString("condition_eligibility_3"));
		}
		study.setAgeMax(fp.getString("ageMax"));

		study.setAgeMin(fp.getString("ageMin"));
		study.setHealthyVolunteerAccepted(fp.getBoolean("healthyVolunteerAccepted"));
		study.setExpectedTotalEnrollment(fp.getInt("expectedTotalEnrollment"));
		fp.getRequest().setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
	}

	private void validateStudy5(FormProcessor fp, StudyBean study, HashMap errors, Validator v) {

		if (!StringUtil.isBlank(fp.getString("facConEmail"))) {
			v.addValidation("facConEmail", Validator.IS_A_EMAIL);
		}
		v.addValidation("facName", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("facCity", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("facState", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 20);
		v.addValidation("facZip", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO,
				64);
		v.addValidation("facCountry", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 64);
		v.addValidation("facConName", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("facConDegree", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("facConPhone", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("facConEmail", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

		errors.putAll(v.validate());

		study.setFacilityCity(fp.getString("facCity"));
		study.setFacilityContactDegree(fp.getString("facConDrgree"));
		study.setFacilityName(fp.getString("facName"));
		study.setFacilityContactEmail(fp.getString("facConEmail"));
		study.setFacilityContactPhone(fp.getString("facConPhone"));
		study.setFacilityContactName(fp.getString("facConName"));
		study.setFacilityCountry(fp.getString("facCountry"));
		study.setFacilityContactDegree(fp.getString("facConDegree"));
		study.setFacilityState(fp.getString("facState"));
		study.setFacilityZip(fp.getString("facZip"));

		if (!errors.isEmpty()) {
			fp.getRequest().setAttribute("formMessages", errors);
			fp.getRequest().setAttribute("facRecruitStatusMap", CreateStudyServlet.facRecruitStatusMap);
		}
	}

	private void validateStudy6(FormProcessor fp, StudyBean study, HashMap errors, Validator v) {
		v.addValidation("medlineIdentifier", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
		v.addValidation("url", Validator.LENGTH_NUMERIC_COMPARISON, NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO,
				255);
		v.addValidation("urlDescription", Validator.LENGTH_NUMERIC_COMPARISON,
				NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);

		errors.putAll(v.validate());

		study.setMedlineIdentifier(fp.getString("medlineIdentifier"));
		study.setResultsReference(fp.getBoolean("resultsReference"));
		study.setUrl(fp.getString("url"));
		study.setUrlDescription(fp.getString("urlDescription"));
		if (!errors.isEmpty()) {
			fp.getRequest().setAttribute("formMessages", errors);
		}
	}

	private void validateStudy7(FormProcessor fp, HashMap errors, Validator v, int studyId,
			Map<String, List<DiscrepancyDescription>> dDescriptionsMap) {
		validateSpecifiedDescriptions(fp, errors, v, studyId, dDescriptionsMap.get("dnUpdateDescriptions"),
				"updateName", "updateVisibilityLevel", "updateDescriptionId", "updateDescriptionError",
				DiscrepancyDescriptionType.DescriptionType.UPDATE_DESCRIPTION.getId());
		validateSpecifiedDescriptions(fp, errors, v, studyId, dDescriptionsMap.get("dnCloseDescriptions"), "closeName",
				"closeVisibilityLevel", "closeDescriptionId", "closeDescriptionError",
				DiscrepancyDescriptionType.DescriptionType.CLOSE_DESCRIPTION.getId());
		validateSpecifiedDescriptions(fp, errors, v, studyId, dDescriptionsMap.get("dnRFCDescriptions"), "dnRFCName",
				"dnRFCVisibilityLevel", "dnRFCDescriptionId", "dnRFCDescriptionError",
				DiscrepancyDescriptionType.DescriptionType.RFC_DESCRIPTION.getId());
	}

	private void validateSpecifiedDescriptions(FormProcessor fp, HashMap errors, Validator v, int studyId,
			List<DiscrepancyDescription> newDescriptions, String descriptionName, String visibilityLevel,
			String descriptionId, String descriptionError, int typeId) {
		newDescriptions.clear();
		for (int i = 0; i < 25; i++) {
			v.addValidation(descriptionName + i, Validator.LENGTH_NUMERIC_COMPARISON,
					NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
			// set list of dn descriptions for specified type here
			if (!"".equals(fp.getString(descriptionName + i))) {
				DiscrepancyDescription dDescription = new DiscrepancyDescription();
				dDescription.setTypeId(typeId);
				dDescription.setName(fp.getString(descriptionName + i));
				switch (fp.getInt(visibilityLevel + i)) {
				case 1:
					dDescription.setVisibilityLevel("Study");
					break;
				case 2:
					dDescription.setVisibilityLevel("Site");
					break;
				default:
					dDescription.setVisibilityLevel("Study and Site");
				}
				if (fp.getInt(descriptionId + i) != 0) {
					dDescription.setId(fp.getInt(descriptionId + i));
				}
				dDescription.setStudyId(studyId);
				newDescriptions.add(dDescription);
			}
		}
		errors.putAll(v.validate());
		for (int i = 0; i < newDescriptions.size(); i++) {
			DiscrepancyDescription rfcTerm1 = newDescriptions.get(i);
			for (int j = 0; j < i; j++) {
				DiscrepancyDescription rfcTerm2 = newDescriptions.get(j);
				if (rfcTerm1.getName().equals(rfcTerm2.getName())) {
					Validator.addError(errors, descriptionError + i,
							respage.getString("please_correct_the_duplicate_description_found_in_row") + " " + (j + 1));
				}
			}
		}
	}

	private void confirmWholeStudy(FormProcessor fp, StudyBean study, HashMap errors, Validator v) {
		errors.putAll(v.validate());
		if (study.getStatus().isLocked()) {
			study.getStudyParameterConfig().setDiscrepancyManagement("false");
		} else {
			study.getStudyParameterConfig().setDiscrepancyManagement(fp.getString("discrepancyManagement"));
		}
		study.getStudyParameterConfig().setCollectDob(fp.getString("collectDob"));
		study.getStudyParameterConfig().setGenderRequired(fp.getString("genderRequired"));
		study.getStudyParameterConfig().setInterviewerNameRequired(fp.getString("interviewerNameRequired"));
		study.getStudyParameterConfig().setInterviewerNameDefault(fp.getString("interviewerNameDefault"));
		study.getStudyParameterConfig().setInterviewDateEditable(fp.getString("interviewDateEditable"));
		study.getStudyParameterConfig().setInterviewDateRequired(fp.getString("interviewDateRequired"));
		study.getStudyParameterConfig().setInterviewerNameEditable(fp.getString("interviewerNameEditable"));
		study.getStudyParameterConfig().setInterviewDateDefault(fp.getString("interviewDateDefault"));
		study.getStudyParameterConfig().setSubjectIdGeneration(fp.getString("subjectIdGeneration"));
		study.getStudyParameterConfig().setSubjectPersonIdRequired(fp.getString("subjectPersonIdRequired"));
		study.getStudyParameterConfig().setSubjectIdPrefixSuffix(fp.getString("subjectIdPrefixSuffix"));
		study.getStudyParameterConfig().setPersonIdShownOnCRF(fp.getString("personIdShownOnCRF"));
		study.getStudyParameterConfig().setSecondaryLabelViewable(fp.getString("secondaryLabelViewable"));
		study.getStudyParameterConfig().setAdminForcedReasonForChange(fp.getString("adminForcedReasonForChange"));
		study.getStudyParameterConfig().setEventLocationRequired(fp.getString("eventLocationRequired"));
		study.getStudyParameterConfig().setSecondaryIdRequired(fp.getString("secondaryIdRequired"));
		study.getStudyParameterConfig().setDateOfEnrollmentForStudyRequired(
				fp.getString("dateOfEnrollmentForStudyRequired"));
		study.getStudyParameterConfig().setStudySubjectIdLabel(fp.getString("studySubjectIdLabel"));
		study.getStudyParameterConfig().setSecondaryIdLabel(fp.getString("secondaryIdLabel"));
		study.getStudyParameterConfig().setDateOfEnrollmentForStudyLabel(fp.getString("dateOfEnrollmentForStudyLabel"));
		study.getStudyParameterConfig().setGenderLabel(fp.getString("genderLabel"));
		study.getStudyParameterConfig().setStartDateTimeRequired(fp.getString("startDateTimeRequired"));
		study.getStudyParameterConfig().setUseStartTime(fp.getString("useStartTime"));
		study.getStudyParameterConfig().setEndDateTimeRequired(fp.getString("endDateTimeRequired"));
		study.getStudyParameterConfig().setUseEndTime(fp.getString("useEndTime"));
		study.getStudyParameterConfig().setStartDateTimeLabel(fp.getString("startDateTimeLabel"));
		study.getStudyParameterConfig().setEndDateTimeLabel(fp.getString("endDateTimeLabel"));
		study.getStudyParameterConfig().setMarkImportedCRFAsCompleted(fp.getString("markImportedCRFAsCompleted"));
		study.getStudyParameterConfig().setAutoScheduleEventDuringImport(fp.getString("autoScheduleEventDuringImport"));
		study.getStudyParameterConfig().setAllowSdvWithOpenQueries(fp.getString("allowSdvWithOpenQueries"));
		study.getStudyParameterConfig().setReplaceExisitingDataDuringImport(
				fp.getString("replaceExisitingDataDuringImport"));
		study.getStudyParameterConfig().setAllowCodingVerification(fp.getString("allowCodingVerification"));
		study.getStudyParameterConfig().setDefaultBioontologyURL(
				fp.getString("defaultBioontologyURL"));
		study.getStudyParameterConfig().setAutoCodeDictionaryName(fp.getString("autoCodeDictionaryName"));
		study.getStudyParameterConfig().setMedicalCodingApprovalNeeded(fp.getString("medicalCodingApprovalNeeded"));
        study.getStudyParameterConfig().setMedicalCodingContextNeeded(fp.getString("medicalCodingContextNeeded"));

		try {

			// Create custom dictionary
			if (study.getStudyParameterConfig().getAutoCodeDictionaryName() != null
					&& !study.getStudyParameterConfig().getAutoCodeDictionaryName().isEmpty()) {
				getDictionaryService().createDictionary(study.getStudyParameterConfig().getAutoCodeDictionaryName(),
						study);
			}
		} catch (CodeException e) {

			logger.info("Custom dictionary with similar name exists");
		}

		if (!errors.isEmpty()) {
			fp.getRequest().setAttribute("formMessages", errors);
		}
	}

	private StudyBean createStudyBean(FormProcessor fp, StudyBean study) {
		study.setId(fp.getInt("studyId"));
		study.setName(fp.getString("name"));
		study.setOfficialTitle(fp.getString("officialTitle"));
		study.setIdentifier(fp.getString("uniqueProId"));
		study.setSecondaryIdentifier(fp.getString("secondProId"));
		study.setPrincipalInvestigator(fp.getString("prinInvestigator"));

		study.setSummary(fp.getString("description"));
		study.setProtocolDescription(fp.getString("protocolDescription"));

		study.setSponsor(fp.getString("sponsor"));
		study.setCollaborators(fp.getString("collaborators"));
		return study;

	}

	private boolean updateStudy2(FormProcessor fp, StudyBean study) {

		study.setOldStatus(study.getStatus());
		study.setStatus(Status.get(fp.getInt("status")));

		if (StringUtil.isBlank(fp.getString(INPUT_VER_DATE))) {
			study.setProtocolDateVerification(null);
		} else {
			study.setProtocolDateVerification(fp.getDate(INPUT_VER_DATE));
		}

		study.setDatePlannedStart(fp.getDate(INPUT_START_DATE));

		if (StringUtil.isBlank(fp.getString(INPUT_END_DATE))) {
			study.setDatePlannedEnd(null);
		} else {
			study.setDatePlannedEnd(fp.getDate(INPUT_END_DATE));
		}

		study.setPhase(fp.getString("phase"));

		if (fp.getInt("genetic") == 1) {
			study.setGenetic(true);
		} else {
			study.setGenetic(false);
		}

		String interventional = resadmin.getString("interventional");
		return interventional.equalsIgnoreCase(study.getProtocolType());
	}

	private void updateStudy3(StudyBean study, boolean isInterventional, FormProcessor fp) {

		study.setPurpose(fp.getString("purpose"));
		ArrayList interventionArray = new ArrayList();
		if (isInterventional) {
			study.setAllocation(fp.getString("allocation"));
			study.setMasking(fp.getString("masking"));
			study.setControl(fp.getString("control"));
			study.setAssignment(fp.getString("assignment"));
			study.setEndpoint(fp.getString("endpoint"));

			StringBuilder interventions = new StringBuilder();

			for (int i = 0; i < 10; i++) {
				String type = fp.getString("interType" + i);
				String name = fp.getString("interName" + i);
				if (!StringUtil.isBlank(type) && !StringUtil.isBlank(name)) {
					InterventionBean ib = new InterventionBean(fp.getString("interType" + i), fp.getString("interName"
							+ i));
					interventionArray.add(ib);
					interventions.append(ib.toString()).append(",");
				}
			}
			study.setInterventions(interventions.toString());

		} else {
			study.setDuration(fp.getString("duration"));
			study.setSelection(fp.getString("selection"));
			study.setTiming(fp.getString("timing"));
		}
		fp.getRequest().setAttribute("interventions", interventionArray);
	}

	private ArrayList parseInterventions(StudyBean sb) {
		ArrayList inters = new ArrayList();
		String interventions = sb.getInterventions();
		try {
			if (!StringUtil.isBlank(interventions)) {
				StringTokenizer st = new StringTokenizer(interventions, ",");
				while (st.hasMoreTokens()) {
					String s = st.nextToken();
					StringTokenizer st1 = new StringTokenizer(s, "/");
					String type = st1.nextToken();
					String name = st1.nextToken();
					InterventionBean ib = new InterventionBean(type, name);
					inters.add(ib);

				}
			}
		} catch (NoSuchElementException nse) {
			return new ArrayList();
		}
		return inters;

	}

	private void setMaps(HttpServletRequest request, boolean isInterventional, ArrayList interventionArray) {
		if (isInterventional) {
			request.setAttribute("interPurposeMap", CreateStudyServlet.interPurposeMap);
			request.setAttribute("allocationMap", CreateStudyServlet.allocationMap);
			request.setAttribute("maskingMap", CreateStudyServlet.maskingMap);
			request.setAttribute("controlMap", CreateStudyServlet.controlMap);
			request.setAttribute("assignmentMap", CreateStudyServlet.assignmentMap);
			request.setAttribute("endpointMap", CreateStudyServlet.endpointMap);
			request.setAttribute("interTypeMap", CreateStudyServlet.interTypeMap);
			request.getSession().setAttribute("interventions", interventionArray);
		} else {
			request.setAttribute("obserPurposeMap", CreateStudyServlet.obserPurposeMap);
			request.setAttribute("selectionMap", CreateStudyServlet.selectionMap);
			request.setAttribute("timingMap", CreateStudyServlet.timingMap);
		}
	}

	private void submitDescriptions(Map<String, List<DiscrepancyDescription>> dDescriptionsMap, int studyId) {
		submitSpecifiedDescriptions(dDescriptionsMap.get("dnUpdateDescriptions"),
				DiscrepancyDescriptionType.DescriptionType.UPDATE_DESCRIPTION.getId(), studyId);
		submitSpecifiedDescriptions(dDescriptionsMap.get("dnCloseDescriptions"),
				DiscrepancyDescriptionType.DescriptionType.CLOSE_DESCRIPTION.getId(), studyId);
		submitSpecifiedDescriptions(dDescriptionsMap.get("dnRFCDescriptions"),
				DiscrepancyDescriptionType.DescriptionType.RFC_DESCRIPTION.getId(), studyId);
	}

	private void submitSpecifiedDescriptions(List<DiscrepancyDescription> newDescriptions, int typeId, int studyId) {
		DiscrepancyDescriptionService dDescriptionService = (DiscrepancyDescriptionService) SpringServletAccess
				.getApplicationContext(getServletContext()).getBean("discrepancyDescriptionService");

		// DiscrepancyDescriptions-section start
		Map<Integer, DiscrepancyDescription> idToDnDescriptionMap = new HashMap<Integer, DiscrepancyDescription>();

		for (DiscrepancyDescription dDescription : dDescriptionService.findAllByStudyIdAndTypeId(studyId, typeId)) {
			idToDnDescriptionMap.put(dDescription.getId(), dDescription);
		}
		for (DiscrepancyDescription dDescription : newDescriptions) {
			if (idToDnDescriptionMap.keySet().contains(dDescription.getId())) {
				DiscrepancyDescription dDescriptionOld = idToDnDescriptionMap.get(dDescription.getId());
				if (!dDescription.getName().equals(dDescriptionOld.getName())
						|| !dDescription.getVisibilityLevel().equals(dDescriptionOld.getVisibilityLevel())) {
					// description was changed
					dDescriptionService.saveDiscrepancyDescription(dDescription);
					idToDnDescriptionMap.remove(dDescription.getId());
				} else {
					// description wasn't changed
					idToDnDescriptionMap.remove(dDescription.getId());
				}
			} else {
				// description is new (id=0)
				dDescriptionService.saveDiscrepancyDescription(dDescription);
			}
		}
		// delete unneeded descriptions
		for (DiscrepancyDescription dDescriptionForDelete : idToDnDescriptionMap.values()) {
			dDescriptionService.deleteDiscrepancyDescription(dDescriptionForDelete);
		}
	}

	private void submitStudy(StudyBean study1, Map<String, List<DiscrepancyDescription>> dDescriptionsMap,
			HttpServletRequest request) throws CodeException {
		UserAccountBean ub = getUserAccountBean(request);

		StudyDAO sdao = new StudyDAO(getDataSource());
		StudyParameterValueDAO spvdao = new StudyParameterValueDAO(getDataSource());
		submitDescriptions(dDescriptionsMap, study1.getId());

		logger.info("study bean to be updated:" + study1.getName());
		study1.setUpdatedDate(new Date());
		study1.setUpdater(ub);
		sdao.update(study1);
		logger.debug("about to create dn descripts");

		ArrayList siteList = (ArrayList) sdao.findAllByParent(study1.getId());
		if (siteList.size() > 0) {
			sdao.updateSitesStatus(study1);
		}

		StudyParameterValueBean spv = new StudyParameterValueBean();

		spv.setStudyId(study1.getId());
		spv.setParameter("collectDob");
		spv.setValue(new Integer(study1.getStudyParameterConfig().getCollectDob()).toString());
		updateParameter(spvdao, spv);

		spv.setParameter("discrepancyManagement");
		spv.setValue(study1.getStudyParameterConfig().getDiscrepancyManagement());
		updateParameter(spvdao, spv);

		spv.setParameter("genderRequired");
		spv.setValue(study1.getStudyParameterConfig().getGenderRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("subjectPersonIdRequired");
		spv.setValue(study1.getStudyParameterConfig().getSubjectPersonIdRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("interviewerNameRequired");
		spv.setValue(study1.getStudyParameterConfig().getInterviewerNameRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("interviewerNameDefault");
		spv.setValue(study1.getStudyParameterConfig().getInterviewerNameDefault());
		updateParameter(spvdao, spv);

		spv.setParameter("interviewerNameEditable");
		spv.setValue(study1.getStudyParameterConfig().getInterviewerNameEditable());
		updateParameter(spvdao, spv);

		List<StudyBean> sites;
		sites = (ArrayList) sdao.findAllByParent(study1.getId());
		if (sites != null && (!sites.isEmpty())) {
			updateInterviewerForSites(study1, sites, spvdao, "interviewerNameEditable");
		}

		spv.setParameter("interviewDateRequired");
		spv.setValue(study1.getStudyParameterConfig().getInterviewDateRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("interviewDateDefault");
		spv.setValue(study1.getStudyParameterConfig().getInterviewDateDefault());
		updateParameter(spvdao, spv);

		spv.setParameter("interviewDateEditable");
		spv.setValue(study1.getStudyParameterConfig().getInterviewDateEditable());
		updateParameter(spvdao, spv);
		if (sites != null && (!sites.isEmpty())) {
			updateInterviewerForSites(study1, sites, spvdao, "interviewDateEditable");
		}
		spv.setParameter("subjectIdGeneration");
		spv.setValue(study1.getStudyParameterConfig().getSubjectIdGeneration());
		updateParameter(spvdao, spv);

		spv.setParameter("subjectIdPrefixSuffix");
		spv.setValue(study1.getStudyParameterConfig().getSubjectIdPrefixSuffix());
		updateParameter(spvdao, spv);

		spv.setParameter("personIdShownOnCRF");
		spv.setValue(study1.getStudyParameterConfig().getPersonIdShownOnCRF());
		updateParameter(spvdao, spv);

		spv.setParameter("secondaryLabelViewable");
		spv.setValue(study1.getStudyParameterConfig().getSecondaryLabelViewable());
		updateParameter(spvdao, spv);

		spv.setParameter("adminForcedReasonForChange");
		spv.setValue(study1.getStudyParameterConfig().getAdminForcedReasonForChange());
		updateParameter(spvdao, spv);

		spv.setParameter("eventLocationRequired");
		spv.setValue(study1.getStudyParameterConfig().getEventLocationRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("secondaryIdRequired");
		spv.setValue(study1.getStudyParameterConfig().getSecondaryIdRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("dateOfEnrollmentForStudyRequired");
		spv.setValue(study1.getStudyParameterConfig().getDateOfEnrollmentForStudyRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("studySubjectIdLabel");
		spv.setValue(study1.getStudyParameterConfig().getStudySubjectIdLabel());
		updateParameter(spvdao, spv);

		spv.setParameter("secondaryIdLabel");
		spv.setValue(study1.getStudyParameterConfig().getSecondaryIdLabel());
		updateParameter(spvdao, spv);

		spv.setParameter("dateOfEnrollmentForStudyLabel");
		spv.setValue(study1.getStudyParameterConfig().getDateOfEnrollmentForStudyLabel());
		updateParameter(spvdao, spv);

		spv.setParameter("genderLabel");
		spv.setValue(study1.getStudyParameterConfig().getGenderLabel());
		updateParameter(spvdao, spv);

		spv.setParameter("startDateTimeRequired");
		spv.setValue(study1.getStudyParameterConfig().getStartDateTimeRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("useStartTime");
		spv.setValue(study1.getStudyParameterConfig().getUseStartTime());
		updateParameter(spvdao, spv);

		spv.setParameter("endDateTimeRequired");
		spv.setValue(study1.getStudyParameterConfig().getEndDateTimeRequired());
		updateParameter(spvdao, spv);

		spv.setParameter("useEndTime");
		spv.setValue(study1.getStudyParameterConfig().getUseEndTime());
		updateParameter(spvdao, spv);

		spv.setParameter("startDateTimeLabel");
		spv.setValue(study1.getStudyParameterConfig().getStartDateTimeLabel());
		updateParameter(spvdao, spv);

		spv.setParameter("endDateTimeLabel");
		spv.setValue(study1.getStudyParameterConfig().getEndDateTimeLabel());
		updateParameter(spvdao, spv);

		spv.setParameter("markImportedCRFAsCompleted");
		spv.setValue(study1.getStudyParameterConfig().getMarkImportedCRFAsCompleted());
		updateParameter(spvdao, spv);

		spv.setParameter("autoScheduleEventDuringImport");
		spv.setValue(study1.getStudyParameterConfig().getAutoScheduleEventDuringImport());
		updateParameter(spvdao, spv);

		spv.setParameter("allowSdvWithOpenQueries");
		spv.setValue(study1.getStudyParameterConfig().getAllowSdvWithOpenQueries());
		updateParameter(spvdao, spv);

		spv.setParameter("replaceExisitingDataDuringImport");
		spv.setValue(study1.getStudyParameterConfig().getReplaceExisitingDataDuringImport());
		updateParameter(spvdao, spv);

		spv.setParameter("allowCodingVerification");
		spv.setValue(study1.getStudyParameterConfig().getAllowCodingVerification());
		updateParameter(spvdao, spv);

		spv.setParameter("defaultBioontologyURL");
		spv.setValue(study1.getStudyParameterConfig().getDefaultBioontologyURL());
		updateParameter(spvdao, spv);

		spv.setParameter("autoCodeDictionaryName");
		spv.setValue(study1.getStudyParameterConfig().getAutoCodeDictionaryName());
		updateParameter(spvdao, spv);

		spv.setParameter("medicalCodingApprovalNeeded");
		spv.setValue(study1.getStudyParameterConfig().getMedicalCodingApprovalNeeded());
		updateParameter(spvdao, spv);

        spv.setParameter("medicalCodingContextNeeded");
        spv.setValue(study1.getStudyParameterConfig().getMedicalCodingContextNeeded());
        updateParameter(spvdao, spv);

		try {

			// Create custom dictionary
			if (study1.getStudyParameterConfig().getAutoCodeDictionaryName() != null
					&& !study1.getStudyParameterConfig().getAutoCodeDictionaryName().isEmpty()) {
				getDictionaryService().createDictionary(study1.getStudyParameterConfig().getAutoCodeDictionaryName(),
						study1);
			}
		} catch (CodeException e) {

			logger.info("Custom dictionary with similar name exists");
		}

		StudyBean curStudy = (StudyBean) request.getSession().getAttribute("study");
		if (curStudy != null && study1.getId() == curStudy.getId()) {
			request.getSession().setAttribute("study", study1);
		}
		// update manage_pedigrees for all sites
		ArrayList children = (ArrayList) sdao.findAllByParent(study1.getId());
		for (Object aChildren : children) {
			StudyBean child = (StudyBean) aChildren;
			child.setType(study1.getType());// same as parent's type
			child.setUpdatedDate(new Date());
			child.setUpdater(ub);
			sdao.update(child);
			StudyParameterValueBean childspv = new StudyParameterValueBean();
			childspv.setStudyId(child.getId());

			childspv.setParameter("collectDob");
			childspv.setValue(new Integer(study1.getStudyParameterConfig().getCollectDob()).toString());
			updateParameter(spvdao, childspv);

			childspv.setParameter("genderRequired");
			childspv.setValue(study1.getStudyParameterConfig().getGenderRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("discrepancyManagement");
			childspv.setValue(study1.getStudyParameterConfig().getDiscrepancyManagement());
			updateParameter(spvdao, childspv);

			childspv.setParameter("genderRequired");
			childspv.setValue(study1.getStudyParameterConfig().getGenderRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("subjectPersonIdRequired");
			childspv.setValue(study1.getStudyParameterConfig().getSubjectPersonIdRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("subjectIdGeneration");
			childspv.setValue(study1.getStudyParameterConfig().getSubjectIdGeneration());
			updateParameter(spvdao, childspv);

			childspv.setParameter("subjectIdPrefixSuffix");
			childspv.setValue(study1.getStudyParameterConfig().getSubjectIdPrefixSuffix());
			updateParameter(spvdao, childspv);

			childspv.setParameter("personIdShownOnCRF");
			childspv.setValue(study1.getStudyParameterConfig().getPersonIdShownOnCRF());
			updateParameter(spvdao, childspv);

			childspv.setParameter("secondaryLabelViewable");
			childspv.setValue(study1.getStudyParameterConfig().getSecondaryLabelViewable());
			updateParameter(spvdao, childspv);

			childspv.setParameter("adminForcedReasonForChange");
			childspv.setValue(study1.getStudyParameterConfig().getAdminForcedReasonForChange());
			updateParameter(spvdao, childspv);

			childspv.setParameter("eventLocationRequired");
			childspv.setValue(study1.getStudyParameterConfig().getEventLocationRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("secondaryIdRequired");
			childspv.setValue(study1.getStudyParameterConfig().getSecondaryIdRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("dateOfEnrollmentForStudyRequired");
			childspv.setValue(study1.getStudyParameterConfig().getDateOfEnrollmentForStudyRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("studySubjectIdLabel");
			childspv.setValue(study1.getStudyParameterConfig().getStudySubjectIdLabel());
			updateParameter(spvdao, childspv);

			childspv.setParameter("secondaryIdLabel");
			childspv.setValue(study1.getStudyParameterConfig().getSecondaryIdLabel());
			updateParameter(spvdao, childspv);

			childspv.setParameter("dateOfEnrollmentForStudyLabel");
			childspv.setValue(study1.getStudyParameterConfig().getDateOfEnrollmentForStudyLabel());
			updateParameter(spvdao, childspv);

			childspv.setParameter("genderLabel");
			childspv.setValue(study1.getStudyParameterConfig().getGenderLabel());
			updateParameter(spvdao, childspv);

			childspv.setParameter("startDateTimeRequired");
			childspv.setValue(study1.getStudyParameterConfig().getStartDateTimeRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("useStartTime");
			childspv.setValue(study1.getStudyParameterConfig().getUseStartTime());
			updateParameter(spvdao, childspv);

			childspv.setParameter("endDateTimeRequired");
			childspv.setValue(study1.getStudyParameterConfig().getEndDateTimeRequired());
			updateParameter(spvdao, childspv);

			childspv.setParameter("useEndTime");
			childspv.setValue(study1.getStudyParameterConfig().getUseEndTime());
			updateParameter(spvdao, childspv);

			childspv.setParameter("startDateTimeLabel");
			childspv.setValue(study1.getStudyParameterConfig().getStartDateTimeLabel());
			updateParameter(spvdao, childspv);

			childspv.setParameter("endDateTimeLabel");
			childspv.setValue(study1.getStudyParameterConfig().getEndDateTimeLabel());
			updateParameter(spvdao, childspv);

			childspv.setParameter("markImportedCRFAsCompleted");
			childspv.setValue(study1.getStudyParameterConfig().getMarkImportedCRFAsCompleted());
			updateParameter(spvdao, childspv);

			childspv.setParameter("autoScheduleEventDuringImport");
			childspv.setValue(study1.getStudyParameterConfig().getAutoScheduleEventDuringImport());
			updateParameter(spvdao, childspv);

			childspv.setParameter("allowSdvWithOpenQueries");
			childspv.setValue(study1.getStudyParameterConfig().getAllowSdvWithOpenQueries());
			updateParameter(spvdao, childspv);

			childspv.setParameter("replaceExisitingDataDuringImport");
			childspv.setValue(study1.getStudyParameterConfig().getReplaceExisitingDataDuringImport());
			updateParameter(spvdao, childspv);

			childspv.setParameter("allowCodingVerification");
			childspv.setValue(study1.getStudyParameterConfig().getAllowCodingVerification());
			updateParameter(spvdao, childspv);

			childspv.setParameter("defaultBioontologyURL");
			childspv.setValue(study1.getStudyParameterConfig().getDefaultBioontologyURL());
			updateParameter(spvdao, childspv);

			childspv.setParameter("autoCodeDictionaryName");
			childspv.setValue(study1.getStudyParameterConfig().getAutoCodeDictionaryName());
			updateParameter(spvdao, childspv);

			try {

				// Create custom dictionary
				if (child.getStudyParameterConfig().getAutoCodeDictionaryName() != null
						&& !child.getStudyParameterConfig().getAutoCodeDictionaryName().isEmpty()) {
					getDictionaryService().createDictionary(
							child.getStudyParameterConfig().getAutoCodeDictionaryName(), child);
				}
			} catch (CodeException e) {
				logger.info("Custom dictionary with similar name exists");
			}
		}
	}

	@Override
	protected String getAdminServlet(HttpServletRequest request) {
		return Controller.ADMIN_SERVLET_CODE;
	}

	private void updateParameter(StudyParameterValueDAO spvdao, StudyParameterValueBean spv) {
		StudyParameterValueBean spv1 = spvdao.findByHandleAndStudy(spv.getStudyId(), spv.getParameter());
		if (spv1.getId() > 0) {
			logger.debug("Updating " + spv.getParameter() + " for study " + spv.getStudyId());
			spvdao.update(spv);
		} else {
			logger.debug("Creating " + spv.getParameter() + " for study " + spv.getStudyId());
			spvdao.create(spv);
		}
	}

	private void updateInterviewerForSites(StudyBean studyBean, List<StudyBean> sites,
			StudyParameterValueDAO studyParameterValueDAO, String parameterType) {

		StudyParameterValueBean studyParameterValueBean = new StudyParameterValueBean();

		if ("interviewerNameEditable".equalsIgnoreCase(parameterType)) {
			studyParameterValueBean.setParameter("interviewerNameEditable");
			studyParameterValueBean.setValue(studyBean.getStudyParameterConfig().getInterviewerNameEditable());
		} else {
			studyParameterValueBean.setParameter("interviewDateEditable");
			studyParameterValueBean.setValue(studyBean.getStudyParameterConfig().getInterviewDateEditable());
		}
		for (StudyBean siteBean : sites) {
			studyParameterValueBean.setStudyId(siteBean.getId());
			updateParameter(studyParameterValueDAO, studyParameterValueBean);
		}
	}
}
