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
package org.akaza.openclinica.control.submit;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.sax.SAXSource;

import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.upload.FileUploadHelper;
import org.akaza.openclinica.bean.service.NamespaceFilter;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.control.core.SpringServlet;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.akaza.openclinica.web.SQLInitServlet;
import com.clinovo.crfdata.ImportCRFDataService;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.clinovo.i18n.LocaleResolver;
import com.clinovo.util.ValidatorHelper;

/**
 * Create a new CRF verison by uploading excel file. Makes use of several other classes to validate and provide accurate
 * validation. More specifically, uses XmlSchemaValidationHelper, ImportCRFDataService, ODMContainer, and others to
 * import all the XML in the ODM 1.3 standard.
 * 
 */
@SuppressWarnings("rawtypes")
@Component
public class ImportCRFDataServlet extends SpringServlet {

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

		checkStudyLocked(Page.MENU_SERVLET, getResPage().getString("current_study_locked"), request, response);
		checkStudyFrozen(Page.MENU_SERVLET, getResPage().getString("current_study_frozen"), request, response);

		if (ub.isSysAdmin()) {
			return;
		}

		Role r = currentRole.getRole();
		if (r.equals(Role.STUDY_DIRECTOR) || r.equals(Role.STUDY_ADMINISTRATOR) || r.equals(Role.INVESTIGATOR)
				|| r.equals(Role.CLINICAL_RESEARCH_COORDINATOR)) {
			return;
		}

		addPageMessage(getResPage().getString("no_have_correct_privilege_current_study")
				+ getResPage().getString("change_study_contact_sysadmin"), request);
		throw new InsufficientPermissionException(Page.MENU_SERVLET, getResException().getString("may_not_submit_data"),
				"1");
	}

	@Override
	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		UserAccountBean ub = getUserAccountBean(request);
		StudyBean currentStudy = getCurrentStudy(request);

		StudyInfoPanel panel = getStudyInfoPanel(request);
		panel.reset();
		panel.setStudyInfoShown(false);
		panel.setOrderedData(true);

		FormProcessor fp = new FormProcessor(request);
		// checks which module the requests are from
		String module = fp.getString(MODULE);
		// keep the module in the session
		request.getSession().setAttribute(MODULE, module);

		String action = request.getParameter("action");
		CRFVersionBean version = (CRFVersionBean) request.getSession().getAttribute("version");

		if (StringUtil.isBlank(action)) {
			logger.info("action is blank");
			request.setAttribute("version", version);
			forwardPage(Page.IMPORT_CRF_DATA, request, response);
			return;
		}
		if ("confirm".equalsIgnoreCase(action)) {
			String dir = SQLInitServlet.getField("filePath");
			if (!new File(dir).exists()) {
				logger.info("The filePath in datainfo.properties is invalid " + dir);
				addPageMessage(getResPage().getString("filepath_you_defined_not_seem_valid"), request);
				forwardPage(Page.IMPORT_CRF_DATA, request, response);
				return;
			}
			// All the uploaded files will be saved in filePath/crf/original/
			String theDir = dir + "crf" + File.separator + "original" + File.separator;
			if (!new File(theDir).isDirectory()) {
				new File(theDir).mkdirs();
				logger.info("Made the directory " + theDir);
			}
			File f = null;
			try {
				HashMap errorsMap = new HashMap();
				f = uploadFile(request, errorsMap);
			} catch (Exception e) {
				logger.warn("*** Found exception during file upload***");
				e.printStackTrace();

			}
			if (f == null) {
				forwardPage(Page.IMPORT_CRF_DATA, request, response);
				return;
			}

			boolean fail = false;
			ODMContainer odmContainer;
			request.getSession().removeAttribute("odmContainer");
			JAXBContext jaxbContext = JAXBContext.newInstance(ODMContainer.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			XMLReader reader = XMLReaderFactory.createXMLReader();
			NamespaceFilter namespaceFilter = new NamespaceFilter("http://www.cdisc.org/ns/odm/v1.3", true);
			namespaceFilter.setParent(reader);

			InputSource inputSource = new InputSource(new FileInputStream(f));
			SAXSource saxSource = new SAXSource(namespaceFilter, inputSource);

			try {
				odmContainer = (ODMContainer) jaxbUnmarshaller.unmarshal(saxSource);
				logger.debug("Found crf data container for study oid: "
						+ odmContainer.getCrfDataPostImportContainer().getStudyOID());
				logger.debug("found length of subject list: "
						+ odmContainer.getCrfDataPostImportContainer().getSubjectData().size());
				addPageMessage(getResPage().getString("passed_xml_validation"), request);
			} catch (Exception me1) {
				me1.printStackTrace();
				MessageFormat mf = new MessageFormat("");
				mf.applyPattern(getResPage().getString("your_xml_is_not_well_formed"));
				Object[] arguments = {me1.getMessage()};
				addPageMessage(mf.format(arguments), request);
				forwardPage(Page.IMPORT_CRF_DATA, request, response);
				return;
			}

			ImportCRFDataService importCRFDataService = new ImportCRFDataService(getRuleSetService(),
					getItemSDVService(), getStudySubjectIdService(), getDataSource(),
					LocaleResolver.getLocale(request));
			List<String> errors = importCRFDataService.validateStudyMetadata(odmContainer, ub.getActiveStudyId(), ub);
			if (errors != null) {
				// add to session
				// forward to another page
				logger.info(errors.toString());
				for (String error : errors) {
					addPageMessage(error, request);
				}
				if (errors.size() > 0) {
					// fail = true;
					forwardPage(Page.IMPORT_CRF_DATA, request, response);
					return;
				} else {
					addPageMessage(getResPage().getString("passed_study_check"), request);
					addPageMessage(getResPage().getString("passed_oid_metadata_check"), request);
				}

			}
			logger.debug("passed error check");

			List<EventCRFBean> eventCRFBeans = importCRFDataService.fetchEventCRFBeans(odmContainer, ub);
			List<DisplayItemBeanWrapper> displayItemBeanWrappers = new ArrayList<DisplayItemBeanWrapper>();
			HashMap<String, String> totalValidationErrors = new HashMap<String, String>();
			HashMap<String, String> hardValidationErrors = new HashMap<String, String>();
			ValidatorHelper validatorHelper = new ValidatorHelper(request, getConfigurationDao());

			if (eventCRFBeans == null) {
				fail = true;
				addPageMessage(getResPage().getString("no_event_status_matching"), request);
			} else {
				ArrayList<Integer> permittedEventCRFIds = new ArrayList<Integer>();
				if (!eventCRFBeans.isEmpty()) {
					for (EventCRFBean eventCRFBean : eventCRFBeans) {

						DataEntryStage dataEntryStage = eventCRFBean.getStage();
						Status eventCRFStatus = eventCRFBean.getStatus();
						if (eventCRFStatus.equals(Status.AVAILABLE)
								|| dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
								|| dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
								|| dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
								|| dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
							permittedEventCRFIds.add(eventCRFBean.getId());
						}
					}

					if (eventCRFBeans.size() >= permittedEventCRFIds.size()) {
						addPageMessage(getResPage().getString("passed_event_crf_status_check"), request);
					} else {
						fail = true;
						addPageMessage(getResPage().getString("the_event_crf_not_correct_status"), request);
					}

					try {
						List<DisplayItemBeanWrapper> tempDisplayItemBeanWrappers;
						tempDisplayItemBeanWrappers = importCRFDataService.lookupValidationErrors(validatorHelper,
								odmContainer, ub, totalValidationErrors, hardValidationErrors, permittedEventCRFIds);
						displayItemBeanWrappers.addAll(tempDisplayItemBeanWrappers);
					} catch (NullPointerException npe1) {
						// what if you have 2 event crfs but the third is a fake?
						fail = true;
						logger.debug("threw a NPE after calling lookup validation errors");
						addPageMessage(getResPage().getString("an_error_was_thrown_while_validation_errors"), request);
					} catch (OpenClinicaException oce1) {
						fail = true;
						logger.debug(
								"threw an OCE after calling lookup validation errors " + oce1.getOpenClinicaMessage());
						addPageMessage(oce1.getOpenClinicaMessage(), request);
					}
				} else {
					fail = true;
					addPageMessage(getResPage().getString("no_event_crfs_matching_the_xml_metadata"), request);
				}
			}
			if (fail) {
				logger.debug("failed here - forwarding...");
				forwardPage(Page.IMPORT_CRF_DATA, request, response);
			} else {
				addPageMessage(getResPage().getString("passing_crf_edit_checks"), request);
				request.getSession().setAttribute("odmContainer", odmContainer);
				request.getSession().setAttribute("importedData", displayItemBeanWrappers);
				request.getSession().setAttribute("validationErrors", totalValidationErrors);
				request.getSession().setAttribute("hardValidationErrors", hardValidationErrors);

				logger.debug("+++ content of total validation errors: " + totalValidationErrors.toString());
				SummaryStatsBean ssBean = importCRFDataService.generateSummaryStatsBean(odmContainer,
						displayItemBeanWrappers, validatorHelper);
				request.getSession().setAttribute("summaryStats", ssBean);
				request.getSession().setAttribute("subjectData",
						odmContainer.getCrfDataPostImportContainer().getSubjectData());

				if (request.getAttribute(ImportCRFDataService.IMPORT_HAS_DATA_TO_SKIP) != null) {
					if (request.getAttribute(ImportCRFDataService.IMPORT_HAS_DATA_TO_REPLACE_EXISTING) != null) {
						addPageMessage(getResWord().getString("import_msg_part1") + " "
								+ (currentStudy.getParentStudyId() > 0
								? getResWord().getString("site")
								: getResWord().getString("study"))
								+ " " + getResWord().getString("import_msg_part2"), request);
					}
					if (request.getAttribute(ImportCRFDataService.IMPORT_HAS_DATA_FOR_UNAVAILABLE_VERSIONS) != null) {
						addPageMessage(getResWord().getString("import_contains_unavailable_crf_versions"), request);
					}
				}

				forwardPage(Page.VERIFY_IMPORT_SERVLET, request, response);
			}
		}
	}

	/*
	 * Given the MultipartRequest extract the first File validate that it is an xml file and then return it.
	 */
	private File getFirstFile(HttpServletRequest request, HashMap errorsMap) {
		File f = null;
		FileUploadHelper uploadHelper = new FileUploadHelper();
		List<File> files = uploadHelper.returnFiles(request);
		for (File file : files) {
			f = file;
			if (f == null) {
				logger.info("file is empty.");
				Validator.addError(errorsMap, "xml_file", "You have to provide an XML file!");
			} else if (!f.getName().contains(".xml") && !f.getName().contains(".XML")) {
				logger.info("file name:" + f.getName());
				addPageMessage(getResPage().getString("file_you_uploaded_not_seem_xml_file"), request);
				f = null;
			}
		}
		return f;
	}

	/**
	 * Uploads the xml file.
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param errorsMap
	 *            HashMap
	 * @return File
	 * @throws Exception
	 *             an Exception
	 */
	public File uploadFile(HttpServletRequest request, HashMap errorsMap) throws Exception {
		return getFirstFile(request, errorsMap);
	}

	@Override
	protected String getAdminServlet(HttpServletRequest request) {
		UserAccountBean ub = getUserAccountBean(request);
		if (ub.isSysAdmin()) {
			return SpringServlet.ADMIN_SERVLET_CODE;
		} else {
			return "";
		}
	}
}
