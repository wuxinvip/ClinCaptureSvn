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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.clinovo.model.CodedItem;
import org.akaza.openclinica.bean.admin.AuditBean;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.NumericComparisonOperator;
import org.akaza.openclinica.bean.core.ResolutionStatus;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.core.Utils;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.bean.submit.DisplayItemWithGroupBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.DisplayTableOfContentsBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.bean.submit.SCDItemDisplayInfo;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.control.core.CoreSecureController;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.FormProcessor;
import org.akaza.openclinica.control.form.RuleValidator;
import org.akaza.openclinica.control.form.ScoreItemValidator;
import org.akaza.openclinica.control.form.Validation;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.control.managestudy.ViewNotesServlet;
import org.akaza.openclinica.control.managestudy.ViewStudySubjectServlet;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.core.form.StringUtil;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
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
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.logic.expressionTree.ExpressionTreeHelper;
import org.akaza.openclinica.logic.rulerunner.MessageContainer.MessageType;
import org.akaza.openclinica.logic.score.ScoreCalculator;
import org.akaza.openclinica.service.DiscrepancyNoteThread;
import org.akaza.openclinica.service.DiscrepancyNoteUtil;
import org.akaza.openclinica.service.calendar.CalendarLogic;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.akaza.openclinica.service.crfdata.InstantOnChangeService;
import org.akaza.openclinica.service.crfdata.SimpleConditionalDisplayService;
import org.akaza.openclinica.service.crfdata.front.InstantOnChangeFrontStrGroup;
import org.akaza.openclinica.service.crfdata.front.InstantOnChangeFrontStrParcel;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.util.DAOWrapper;
import org.akaza.openclinica.util.DiscrepancyShortcutsAnalyzer;
import org.akaza.openclinica.util.SubjectEventStatusUtil;
import org.akaza.openclinica.view.Page;
import org.akaza.openclinica.view.form.FormBeanUtil;
import org.akaza.openclinica.web.InconsistentStateException;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.quartz.impl.StdScheduler;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.clinovo.service.CodedItemService;
import com.clinovo.util.ValidatorHelper;

/**
 * @author ssachs
 */
@SuppressWarnings({"unchecked", "rawtypes", "serial"})
public abstract class DataEntryServlet extends CoreSecureController {

    Locale locale;
    protected CodedItemService codedItemService;

	public static final String DATA_ENTRY_CURRENT_CRF_VERSION_OID = "dataEntryCurrentCrfVersionOid";
	public static final String DATA_ENTRY_CURRENT_CRF_OID = "dataEntryCurrentCrfOid";
    public static final String ACTION = "action";
    
	// these inputs come from the form, from another JSP via POST,
	// or from another JSP via GET
	// e.g. InitialDataEntry?eventCRFId=123&sectionId=234
	public static final String INPUT_EVENT_CRF_ID = "eventCRFId";

	public static final String INPUT_SECTION_ID = "sectionId";

	// these inputs are used when other servlets redirect you here
	// this is most typically the case when the user enters data and clicks the
	// "Previous" or "Next" button
	public static final String INPUT_EVENT_CRF = "event";

	public static final String INPUT_SECTION = "section";

	/**
	 * A bean used to indicate that servlets to which this servlet forwards should ignore any parameters, in particular
	 * the "submitted" parameter which controls FormProcessor.isSubmitted. If an attribute with this name is set in the
	 * request, the servlet to which this servlet forwards should consider fp.isSubmitted to always return false.
	 */
	public static final String INPUT_IGNORE_PARAMETERS = "ignore";

	/**
	 * A bean used to indicate that we are not validating inputs, that is, that the user is "confirming" values which
	 * did not validate properly the first time. If an attribute with this name is set in the request, this servlet
	 * should not perform any validation on the form inputs.
	 */
	public static final String INPUT_CHECK_INPUTS = "checkInputs";

	/**
	 * The name of the form input on which users write annotations.
	 */
	public static final String INPUT_ANNOTATIONS = "annotations";

	/**
	 * The name of the attribute in the request which hold the preset annotations form value.
	 */
	public static final String BEAN_ANNOTATIONS = "annotations";

	// names of submit buttons in the JSP
	public static final String RESUME_LATER = "submittedResume";

	public static final String GO_PREVIOUS = "submittedPrev";

	public static final String GO_NEXT = "submittedNext";

	public static final String BEAN_DISPLAY = "section";

	public static final String TOC_DISPLAY = "toc"; // from
	// TableOfContentServlet

	// these inputs are displayed on the table of contents and
	// are used to edit Event CRF properties
	public static final String INPUT_INTERVIEWER = "interviewer";

	public static final String INPUT_INTERVIEW_DATE = "interviewDate";

	public static final String INTERVIEWER_NAME_NOTE = "InterviewerNameNote";

	public static final String INTERVIEWER_DATE_NOTE = "InterviewerDateNote";

	public static final String INPUT_TAB = "tabId";

	public static final String INPUT_MARK_COMPLETE = "markComplete";

	public static final String VALUE_YES = "Yes";

	// these are only for use with ACTION_START_INITIAL_DATA_ENTRY
	public static final String INPUT_EVENT_DEFINITION_CRF_ID = "eventDefinitionCRFId";

	public static final String INPUT_CRF_VERSION_ID = "crfVersionId";

	public static final String INPUT_STUDY_EVENT_ID = "studyEventId";

	public static final String INPUT_SUBJECT_ID = "subjectId";

	public static final String GO_EXIT = "submittedExit";

	public static final String GROUP_HAS_DATA = "groupHasData";
	public static final String HAS_DATA_FLAG = "hasDataFlag";
	// See the session variable in DoubleDataEntryServlet
	public static final String DDE_PROGESS = "doubleDataProgress";

	public static final String INTERVIEWER_NAME = "interviewer_name";

	public static final String DATE_INTERVIEWED = "date_interviewed";

	public static final String NOTE_SUBMITTED = "note_submitted";

	public static final String SECTION_BEAN = "section_bean";
	public static final String ALL_SECTION_BEANS = "all_section_bean";

	public static final String EVENT_DEF_CRF_BEAN = "event_def_crf_bean";

	public static final String ALL_ITEMS_LIST = "all_items_list";

	public static final String CV_INSTANT_META = "cvInstantMeta";

	private DataSource dataSource;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			ServletContext context = getServletContext();
			SessionManager sm = new SessionManager(SpringServletAccess.getApplicationContext(context));
			dataSource = sm.getDataSource();
		} catch (Exception ne) {
			ne.printStackTrace();
		}
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	@Override
	protected abstract void mayProceed(HttpServletRequest request, HttpServletResponse response)
			throws InsufficientPermissionException;

	private String getSectionFirstFieldId(int sectionId) {

		ItemDAO itemDAO = new ItemDAO(getDataSource());
		List<ItemBean> items = itemDAO.findAllBySectionId(sectionId);
		if (!items.isEmpty()) {
			return new Integer(items.get(0).getId()).toString();
		}
		return "";
	}

	private void logMe(String message) {
		logger.trace(message);
	}

	private void prepareSessionNotesIfValidationsWillFail(HttpServletRequest request, boolean hasGroup,
			boolean isSubmitted, ArrayList<DiscrepancyNoteBean> allNotes) {
		try {
			if (request.getMethod().equalsIgnoreCase("POST") && request.getAttribute("section") == null) {
				request.setAttribute("section", getDisplayBean(hasGroup, false, request, isSubmitted).getSection());
				FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) request.getSession().getAttribute(
						AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
				if (fdn != null) {
					for (Object list : fdn.getFieldNotes().values()) {
						for (DiscrepancyNoteBean discrepancyNoteBean : (List<DiscrepancyNoteBean>) list) {
							if (discrepancyNoteBean.getId() == 0) {
								allNotes.add(discrepancyNoteBean);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("An error has occured in the prepareSessionNotesIfValidationsWillFail method.", e);
		}
	}
    
	@SuppressWarnings("unused")
	@Override
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FormProcessor fp = new FormProcessor(request);

        String action = fp.getString(ACTION);

		ConfigurationDao configurationDao = SpringServletAccess.getApplicationContext(
				request.getSession().getServletContext()).getBean(ConfigurationDao.class);
		ValidatorHelper validatorHelper = new ValidatorHelper(request, configurationDao);

		// JN:The following were the the global variables, moved as local.
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		SectionBean sb = (SectionBean) request.getAttribute(SECTION_BEAN);
        
        ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(getDataSource());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource(), locale);        
        EventCRFDAO ecdao = new EventCRFDAO(getDataSource());
        SectionDAO sdao = new SectionDAO(getDataSource());
        
		HttpSession session = request.getSession();
		StudyBean currentStudy = (StudyBean) session.getAttribute("study");
		StudyUserRoleBean currentRole = (StudyUserRoleBean) session.getAttribute("userRole");
		/**
		 * Determines whether the form was submitted. Calculated once in processRequest. The reason we don't use the
		 * normal means to determine if the form was submitted (ie FormProcessor.isSubmitted) is because when we use
		 * forwardPage, Java confuses the inputs from the just-processed form with the inputs for the forwarded-to page.
		 * This is a problem since frequently we're forwarding from one (submitted) section to the next (unsubmitted)
		 * section. If we use the normal means, Java will always think that the unsubmitted section is, in fact,
		 * submitted. This member is guaranteed to be calculated before shouldLoadDBValues() is called.
		 */
		boolean isSubmitted = false;

		boolean hasGroup = false;

		logMe("Enterting DataEntry Servlet" + System.currentTimeMillis());
		EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(getDataSource());
		locale = request.getLocale();

		FormDiscrepancyNotes discNotes;

		panel.setStudyInfoShown(false);
		String age = "";
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);

		// Repeating groups rows appear if validation returns to the same section
		int isFirstTimeOnSection = fp.getInt("isFirstTimeOnSection");
		request.setAttribute("isFirstTimeOnSection", isFirstTimeOnSection + "");

		request.setAttribute("expandCrfInfo", false);

		if (fp.getString(GO_EXIT).equals("") && !isSubmitted && fp.getString("tabId").equals("")
				&& fp.getString("sectionId").equals("")) {
			if (getUnavailableCRFList().containsKey(ecb.getId())) {
				int userId = (Integer) getUnavailableCRFList().get(ecb.getId());
				UserAccountDAO udao = new UserAccountDAO(getDataSource());
				UserAccountBean ubean = (UserAccountBean) udao.findByPK(userId);
				addPageMessage(
						resword.getString("CRF_unavailable") + " " + ubean.getName() + " "
								+ resword.getString("Currently_entering_data") + " "
								+ resword.getString("Leave_the_CRF"), request);

				forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET, request, response);
			} else {
				lockThisEventCRF(ecb.getId(), ub.getId());
			}
		}

		if (!ecb.isActive()) {
			throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET,
					resexception.getString("event_not_exists"));
		}

		logMe("Enterting DataEntry Get the status/number of item discrepancy notes" + System.currentTimeMillis());
		// Get the status/number of item discrepancy notes
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		ArrayList<DiscrepancyNoteBean> allNotes = new ArrayList<DiscrepancyNoteBean>();
		List<DiscrepancyNoteBean> eventCrfNotes = new ArrayList<DiscrepancyNoteBean>();
		List<DiscrepancyNoteThread> noteThreads = new ArrayList<DiscrepancyNoteThread>();
		dndao = new DiscrepancyNoteDAO(getDataSource());
		allNotes = dndao.findAllTopNotesByEventCRF(ecb.getId());
		eventCrfNotes = dndao.findOnlyParentEventCRFDNotesFromEventCRF(ecb);
		if (!eventCrfNotes.isEmpty()) {
			allNotes.addAll(eventCrfNotes);

		}
		logMe("Entering DataEntry Create disc note threads out of the various notes" + System.currentTimeMillis());
		// Create disc note threads out of the various notes
		DiscrepancyNoteUtil dNoteUtil = new DiscrepancyNoteUtil();
		prepareSessionNotesIfValidationsWillFail(request, hasGroup, isSubmitted, allNotes);
        noteThreads = dNoteUtil.createThreadsOfParents(allNotes, getDataSource(), currentStudy, null, -1, true);

		String fromViewNotes = fp.getString("fromViewNotes");
		if (fromViewNotes != null && "1".equals(fromViewNotes)) {
			request.setAttribute("fromViewNotes", fromViewNotes);
		}

		logMe("Entering Create studySubjDao.. ++++stuff" + System.currentTimeMillis());
		StudySubjectDAO ssdao = new StudySubjectDAO(getDataSource());
		StudySubjectBean ssb = (StudySubjectBean) ssdao.findByPK(ecb.getStudySubjectId());
		Status s = ssb.getStatus();
		if ("removed".equalsIgnoreCase(s.getName()) || "auto-removed".equalsIgnoreCase(s.getName())) {
			addPageMessage(
					respage.getString("you_may_not_perform_data_entry_on_a_CRF")
							+ respage.getString("study_subject_has_been_deleted"), request);
			request.setAttribute("id", new Integer(ecb.getStudySubjectId()).toString());
			forwardPage(Page.VIEW_STUDY_SUBJECT_SERVLET, request, response);
		}

		HashMap<String, String> newUploadedFiles = (HashMap<String, String>) session.getAttribute("newUploadedFiles");
		if (newUploadedFiles == null) {
			newUploadedFiles = new HashMap<String, String>();
		}
		request.setAttribute("newUploadedFiles", newUploadedFiles);
		if (!fp.getString("exitTo").equals("")) {
			request.setAttribute("exitTo", fp.getString("exitTo"));
		}
		// some EVENT CRF CHECK
		logMe("Entering some EVENT CRF CHECK" + System.currentTimeMillis());
		if (!fp.getString(GO_EXIT).equals("")) {
			session.removeAttribute(GROUP_HAS_DATA);
			session.removeAttribute("to_create_crf");
			session.removeAttribute("mayProcessUploading");
			// Removing the user and EventCRF from the locked CRF List
			getUnavailableCRFList().remove(ecb.getId());
			if (newUploadedFiles.size() > 0) {
				if (this.unloadFiles(newUploadedFiles)) {

				} else {
					String missed = "";
					Iterator iter = newUploadedFiles.keySet().iterator();
					while (iter.hasNext()) {
						missed += " " + newUploadedFiles.get(iter.next());
					}
					addPageMessage(respage.getString("uploaded_files_not_deleted_or_not_exist") + ": " + missed,
							request);
				}
			}
			session.removeAttribute("newUploadedFiles");
			addPageMessage(respage.getString("exit_without_saving"), request);
			if (fromViewNotes != null && "1".equals(fromViewNotes)) {
				String viewNotesURL = (String) session.getAttribute("viewNotesURL");
				if (viewNotesURL != null && viewNotesURL.length() > 0) {
					response.sendRedirect(response.encodeRedirectURL(viewNotesURL));
				}
				return;
			}
			String fromResolvingNotes = fp.getString("fromResolvingNotes", true);
			String winLocation = (String) session.getAttribute(ViewNotesServlet.WIN_LOCATION);
			if (!StringUtil.isBlank(fromResolvingNotes) && !StringUtil.isBlank(winLocation)) {
				response.sendRedirect(response.encodeRedirectURL(winLocation));
			} else {
				if (!fp.getString("exitTo").equals("")) {
					response.sendRedirect(response.encodeRedirectURL(fp.getString("exitTo")));
				} else
					response.sendRedirect(response.encodeRedirectURL("EnterDataForStudyEvent?eventId="
							+ ecb.getStudyEventId()));
			}
			return;
		}
		logMe("Entering some EVENT CRF CHECK DONE" + System.currentTimeMillis());
		// checks if the section has items in item group
		// for repeating items
		// hasGroup = getInputBeans();
		hasGroup = checkGroups(fp, ecb);

		Boolean b = (Boolean) request.getAttribute(INPUT_IGNORE_PARAMETERS);
		isSubmitted = fp.isSubmitted() && b == null;
		// variable is used for fetching any null values like "not applicable"
		int eventDefinitionCRFId = 0;
		if (fp != null) {
			eventDefinitionCRFId = fp.getInt("eventDefinitionCRFId");
		}

		StudyBean study = (StudyBean) session.getAttribute("study");
		// constructs the list of items used on UI
		logMe("Entering some EVENT DEF CRF CHECK " + System.currentTimeMillis());

		if (eventDefinitionCRFId <= 0) {
			// TODO we have to get that id before we can continue
			EventDefinitionCRFBean edcBean = edcdao.findByStudyEventIdAndCRFVersionId(study, ecb.getStudyEventId(),
					ecb.getCRFVersionId());
			eventDefinitionCRFId = edcBean.getId();
		}

        List<SectionBean> allSections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
        DiscrepancyShortcutsAnalyzer.prepareDnShortcutLinks(request, ecb, sdao, ifmdao, eventDefinitionCRFId, allSections,
                noteThreads);
        logMe("Entering DataEntry Create disc note threads out of the various notes DONE" + System.currentTimeMillis());

		logMe("Entering some EVENT DEF CRF CHECK DONE " + System.currentTimeMillis());
		logMe("Entering some Study EVENT DEF CRF CHECK  " + System.currentTimeMillis());
		StudyEventDAO seDao = new StudyEventDAO(getDataSource());
		EventDefinitionCRFBean edcBean = (EventDefinitionCRFBean) edcdao.findByPK(eventDefinitionCRFId);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) edcdao.findByPK(eventDefinitionCRFId);
		request.setAttribute(EVENT_DEF_CRF_BEAN, edcb);// JN:Putting the event_def_crf_bean in the request attribute.

		StudyEventBean studyEventBean = (StudyEventBean) seDao.findByPK(ecb.getStudyEventId());
		edcBean.setId(eventDefinitionCRFId);

		request.setAttribute("studyEvent", studyEventBean);

		StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(getDataSource());
		StudyEventDefinitionBean studyEventDefinition = (StudyEventDefinitionBean) seddao.findByPK(edcBean
				.getStudyEventDefinitionId());

		CRFDAO cdao = new CRFDAO(getDataSource());
		CRFVersionDAO cvdao = new CRFVersionDAO(getDataSource());
		CRFVersionBean crfVersionBean = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
		CRFBean crfBean = (CRFBean) cdao.findByPK(crfVersionBean.getCrfId());

		request.setAttribute(DATA_ENTRY_CURRENT_CRF_VERSION_OID, crfVersionBean.getOid());
		request.setAttribute(DATA_ENTRY_CURRENT_CRF_OID, crfBean.getOid());

		Phase phase2 = Phase.INITIAL_DATA_ENTRY;
		if (getServletPage(request).equals(Page.DOUBLE_DATA_ENTRY_SERVLET)) {
			phase2 = Phase.DOUBLE_DATA_ENTRY;
		} else if (getServletPage(request).equals(Page.ADMIN_EDIT_SERVLET)) {
			phase2 = Phase.ADMIN_EDITING;
		}
		logMe("Entering ruleSets::: CreateAndInitializeRuleSet:::" + Thread.currentThread());
		logMe("Entering ruleSets::: CreateAndInitializeRuleSet:::" + Thread.currentThread() + "currentStudy?"
				+ currentStudy + "studyEventDefinition" + studyEventDefinition + "crfVersionBean" + crfVersionBean
				+ "studyEventBean" + studyEventBean + "ecb" + ecb);
		logMe("Entering getDisplayBean:::::Thread::::" + Thread.currentThread());
		DisplaySectionBean section = getDisplayBean(hasGroup, false, request, isSubmitted);
		if (section.getSection().hasSCDItem()) {
			SimpleConditionalDisplayService cds0 = (SimpleConditionalDisplayService) SpringServletAccess
					.getApplicationContext(getServletContext()).getBean("simpleConditionalDisplayService");
			section = cds0.initConditionalDisplays(section);
		}
		logMe("Entering  Find out the id of the section's first field " + System.currentTimeMillis());

		// Find out the id of the section's first field
		String firstFieldId = getSectionFirstFieldId(section.getSection().getId());
		request.setAttribute("formFirstField", firstFieldId);

		logMe("Entering  displayItemWithGroups " + System.currentTimeMillis());
		List<DisplayItemWithGroupBean> displayItemWithGroups = createItemWithGroups(section, hasGroup,
				eventDefinitionCRFId, request);
		logMe("Entering  displayItemWithGroups end " + System.currentTimeMillis());
		this.getItemMetadataService().updateGroupDynamicsInSection(displayItemWithGroups, section.getSection().getId(),
				ecb);
		section.setDisplayItemGroups(displayItemWithGroups);
		DisplayTableOfContentsBean toc = TableOfContentsServlet.getDisplayBeanWithShownSections(
				getDataSource(),
				(DisplayTableOfContentsBean) request.getAttribute(TOC_DISPLAY),
				(DynamicsMetadataService) SpringServletAccess.getApplicationContext(getServletContext()).getBean(
						"dynamicsMetadataService"));
		request.setAttribute(TOC_DISPLAY, toc);
		LinkedList<Integer> sectionIdsInToc = TableOfContentsServlet.sectionIdsInToc(toc);

		logMe("Entering  displayItemWithGroups sdao.findPrevious  " + System.currentTimeMillis());
		int sIndex = TableOfContentsServlet.sectionIndexInToc(section.getSection(), toc, sectionIdsInToc);
		SectionBean previousSec = this.prevSection(section.getSection(), ecb, toc, sIndex);
		logMe("Entering  displayItemWithGroups sdao.findPrevious  end " + System.currentTimeMillis());
		SectionBean nextSec = this.nextSection(section.getSection(), ecb, toc, sIndex);
		section.setFirstSection(!previousSec.isActive());
		section.setLastSection(!nextSec.isActive());

		// this is for generating side info panel
		// and the information panel under the Title
		SubjectDAO subjectDao = new SubjectDAO(getDataSource());
		StudyDAO studydao = new StudyDAO(getDataSource());
		SubjectBean subject = (SubjectBean) subjectDao.findByPK(ssb.getSubjectId());

		// Get the study then the parent study
		logMe("Entering  Get the study then the parent study   " + System.currentTimeMillis());
		if (study.getParentStudyId() > 0) {
			// this is a site,find parent
			StudyBean parentStudy = (StudyBean) studydao.findByPK(study.getParentStudyId());
			request.setAttribute("studyTitle", parentStudy.getName());
			request.setAttribute("siteTitle", study.getName());
		} else {
			request.setAttribute("studyTitle", study.getName());
		}

		logMe("Entering  Get the study then the parent study end  " + System.currentTimeMillis());
		// Let us process the age
		if (currentStudy.getStudyParameterConfig().getCollectDob().equals("1")) {
			Date enrollmentDate = ssb.getEnrollmentDate();
			age = Utils.getInstance().processAge(enrollmentDate, subject.getDateOfBirth());
		}
		ArrayList beans = ViewStudySubjectServlet.getDisplayStudyEventsForStudySubject(ssb, getDataSource(), ub,
				currentRole);
		request.setAttribute("studySubject", ssb);
		request.setAttribute("subject", subject);
		request.setAttribute("beans", beans);
		request.setAttribute("eventCRF", ecb);
		request.setAttribute("age", age);
		request.setAttribute(
				"decryptedPassword",
				((SecurityManager) SpringServletAccess.getApplicationContext(getServletContext()).getBean(
						"securityManager")).encrytPassword("root", getUserDetails()));

		// set up interviewer name and date
		fp.addPresetValue(INPUT_INTERVIEWER, ecb.getInterviewerName());

		if (ecb.getDateInterviewed() != null) {
			String idateFormatted = local_df.format(ecb.getDateInterviewed());
			fp.addPresetValue(INPUT_INTERVIEW_DATE, idateFormatted);
		} else {
			fp.addPresetValue(INPUT_INTERVIEW_DATE, "");
		}
		setPresetValues(fp.getPresetValues(), request);
		logMe("Entering Checks !submitted  " + System.currentTimeMillis());
		if (!isSubmitted) {
			// TODO: prevent data enterer from seeing results of first round of
			// data
			// entry, if this is second round
			logMe("Entering Checks !submitted entered  " + System.currentTimeMillis());
			long t = System.currentTimeMillis();
			request.setAttribute(BEAN_DISPLAY, section);
			request.setAttribute(BEAN_ANNOTATIONS, getEventCRFAnnotations(request));
			session.setAttribute("shouldRunValidation", null);
			session.setAttribute("rulesErrors", null);
			session.setAttribute(DataEntryServlet.NOTE_SUBMITTED, null);

			discNotes = new FormDiscrepancyNotes();
            
			section = populateNotesWithDBNoteCounts(discNotes, noteThreads, section, request);
			populateInstantOnChange(request.getSession(), ecb, section);
			logger.debug("+++ just ran populateNotes, printing field notes: " + discNotes.getFieldNotes().toString());
			logger.debug("found disc notes: " + discNotes.getNumExistingFieldNotes().toString());
			session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);

			if (section.getSection().hasSCDItem()) {
				section = SCDItemDisplayInfo.generateSCDDisplayInfo(
						section,
						this.getServletPage(request).equals(Page.INITIAL_DATA_ENTRY)
								|| this.getServletPage(request).equals(Page.ADMIN_EDIT_SERVLET)
								&& !this.isAdminForcedReasonForChange(request));
			}

			int keyId = ecb.getId();
			session.removeAttribute(DoubleDataEntryServlet.COUNT_VALIDATE + keyId);

			setUpPanel(section);
			if (newUploadedFiles.size() > 0) {
				if (this.unloadFiles(newUploadedFiles)) {

				} else {
					String missed = "";
					Iterator iter = newUploadedFiles.keySet().iterator();
					while (iter.hasNext()) {
						missed += " " + newUploadedFiles.get(iter.next());
					}
					addPageMessage(respage.getString("uploaded_files_not_deleted_or_not_exist") + ": " + missed,
							request);
				}
			}
			logMe("Entering Checks !submitted entered end forwarding page " + System.currentTimeMillis());
			logMe("Time Took for this block" + (System.currentTimeMillis() - t));
			request.getSession().setAttribute("dnAdditionalCreatingParameters", createDNParametersMap(request, section));
			forwardPage(getJSPPage(), request, response);
			return;
		} else {
			logMe("Entering Checks !submitted not entered  " + System.currentTimeMillis());
			//
			// VALIDATION / LOADING DATA
			//
			// If validation is required for this round, we will go through
			// each item and add an appropriate validation to the Validator
			//
			// Otherwise, we will just load the data into the DisplayItemBean
			// so that we can write to the database later.
			//
			// Validation is required if two conditions are met:
			// 1. The user clicked a "Save" button, not a "Confirm" button
			// 2. In this type of data entry servlet, when the user clicks
			// a Save button, the inputs are validated
			//

			boolean validate = fp.getBoolean(INPUT_CHECK_INPUTS) && validateInputOnFirstRound();
			// did the user click a "Save" button?
			// is validation required in this type of servlet when the user
			// clicks
			// "Save"?
			// We can conclude that the user is trying to save data; therefore,
			// set a request
			// attribute indicating that default values for items shouldn't be
			// displayed
			// in the application UI that will subsequently be displayed
			// TODO: find a better, less random place for this
			// session.setAttribute(HAS_DATA_FLAG, true);

			errors = new HashMap();

			discNotes = (FormDiscrepancyNotes) session.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
			if (discNotes == null) {
				discNotes = new FormDiscrepancyNotes();
			}

			// all items- inlcude items in item groups and other single items
			List<DisplayItemWithGroupBean> allItems = section.getDisplayItemGroups();
			String attachedFilePath = Utils.getAttachedFilePath(currentStudy);

			DiscrepancyValidator v = new DiscrepancyValidator(validatorHelper, discNotes);
			RuleValidator ruleValidator = new RuleValidator(validatorHelper);
			logger.debug("SZE 1  :: " + allItems.size());
			logMe("Looping inside !submitted  " + System.currentTimeMillis());
			for (int i = 0; i < allItems.size(); i++) {
				logger.trace("===itering through items: " + i);
				DisplayItemWithGroupBean diwg = allItems.get(i);
				if (diwg.isInGroup()) {
					// for the items in groups
					DisplayItemGroupBean dgb = diwg.getItemGroup();
					List<DisplayItemGroupBean> dbGroups = diwg.getDbItemGroups();
					List<DisplayItemGroupBean> formGroups = new ArrayList<DisplayItemGroupBean>();
					logger.info("got db item group size " + dbGroups.size());

					if (validate) {
						logger.debug("===IF VALIDATE NOT A SINGLE ITEM: got to this part in the validation loop: "
								+ dgb.getGroupMetaBean().getName());
						formGroups = validateDisplayItemGroupBean(v, dgb, dbGroups, formGroups, request, response);
						logger.debug("form group size after validation " + formGroups.size());
					} else {
						logger.debug("+++ELSE NOT A SINGLE ITEM: got to this part in the validation loop: "
								+ dgb.getGroupMetaBean().getName());
						formGroups = loadFormValueForItemGroup(dgb, dbGroups, formGroups, eventDefinitionCRFId, request);
						logger.debug("form group size without validation " + formGroups.size());
					}

					diwg.setItemGroup(dgb);
					diwg.setItemGroups(formGroups);

					allItems.set(i, diwg);

				} else {// other single items
					DisplayItemBean dib = diwg.getSingleItem();
					
					if (validate) {
						// generate input name here?
						// DisplayItemGroupBean dgb = diwg.getItemGroup();
						String itemName = getInputName(dib);
						// no Item group for single item, so just use blank
						// string as parameter for inputName

						dib = validateDisplayItemBean(v, dib, "", request);// this
						// should be
						// used,
						// otherwise,
						// DDE not
						// working-jxu

						logger.debug("&&& found name: " + itemName);
						logger.debug("input VALIDATE " + itemName + ": " + fp.getString(itemName));
						logger.debug("input " + itemName + " has a response set of "
								+ dib.getMetadata().getResponseSet().getOptions().size() + " options");
					} else {
						String itemName = getInputName(dib);
						logger.debug("input NONVALIDATE " + itemName + ": " + fp.getString(itemName));
						dib = loadFormValue(dib, request);
					}

					ArrayList children = dib.getChildren();
					for (int j = 0; j < children.size(); j++) {
						DisplayItemBean child = (DisplayItemBean) children.get(j);
						String itemName = getInputName(child);
						child.loadFormValue(fp.getString(itemName));
						if (validate) {
							child = validateDisplayItemBean(v, child, itemName, request);
						} else {
							child = loadFormValue(child, request);
						}
						logger.debug("Checking child value for " + itemName + ": " + child.getData().getValue());
						children.set(j, child);
					}

					dib.setChildren(children);
					diwg.setSingleItem(runDynamicsItemCheck(dib, null, request));
					logger.debug(" I : " + i);
					allItems.set(i, diwg);

				}
			}
			logMe("Loop ended  " + System.currentTimeMillis());
			List<ItemBean> itemBeansWithSCDShown = new ArrayList<ItemBean>();
			if (validate && section.getSection().hasSCDItem()) {
				logMe(" Validate and Loop  " + System.currentTimeMillis());

				for (int i = 0; i < allItems.size(); ++i) {
					DisplayItemBean dib = allItems.get(i).getSingleItem();
					ItemFormMetadataBean ifmb = dib.getMetadata();
					if (ifmb.getParentId() == 0) {
						if (dib.getScdData().getScdSetsForControl().size() > 0) {
							// for control item
							// dib has to loadFormValue first. Here loadFormValue has been done in
							section.setShowSCDItemIds(SimpleConditionalDisplayService.conditionalDisplayToBeShown(dib,
									section.getShowSCDItemIds()));
						}
                        if (dib.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId() > 0) {
							// for scd item
							// a control item is always before its scd item
							dib.setIsSCDtoBeShown(section.getShowSCDItemIds().contains(dib.getMetadata().getItemId()));
							if (dib.getIsSCDtoBeShown())
								itemBeansWithSCDShown.add(dib.getItem());

							validateSCDItemBean(v, dib);
						}
						ArrayList<DisplayItemBean> children = dib.getChildren();
						for (int j = 0; j < children.size(); j++) {
							DisplayItemBean child = children.get(j);
							if (child.getScdData().getScdSetsForControl().size() > 0) {
								// for control item
								// dib has to loadFormValue first. Here loadFormValue has been done in
								section.setShowSCDItemIds(SimpleConditionalDisplayService.conditionalDisplayToBeShown(
										child, section.getShowSCDItemIds()));
							} else if (child.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId() > 0) {
								// for scd item
								// a control item is always before its scd item
								child.setIsSCDtoBeShown(section.getShowSCDItemIds().contains(
										child.getMetadata().getItemId()));
								if (child.getIsSCDtoBeShown())
									itemBeansWithSCDShown.add(dib.getItem());
								validateSCDItemBean(v, child);
							}
						}
					}
				}
				logMe(" Validate and Loop end  " + System.currentTimeMillis());
			}
			logMe(" Validate and Loop end  " + System.currentTimeMillis());
			// JN:calling again here, for the items with simple conditional display and rules.
			List<RuleSetBean> ruleSets = createAndInitializeRuleSet(currentStudy, studyEventDefinition, crfVersionBean,
					studyEventBean, ecb, true, request, response, itemBeansWithSCDShown);
			boolean shouldRunRules = getRuleSetService(request).shouldRunRulesForRuleSets(ruleSets, phase2);

			HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid = null;
			groupOrdinalPLusItemOid = runRules(allItems, ruleSets, true, shouldRunRules, MessageType.ERROR, phase2,
					ecb, request);
			logMe("allItems  Loop begin  " + System.currentTimeMillis());

			for (int i = 0; i < allItems.size(); i++) {
				DisplayItemWithGroupBean diwg = allItems.get(i);
				if (diwg.isInGroup()) {
					// for the items in groups
					DisplayItemGroupBean dgb = diwg.getItemGroup();
					List<DisplayItemGroupBean> dbGroups = diwg.getDbItemGroups();
					List<DisplayItemGroupBean> formGroups = new ArrayList<DisplayItemGroupBean>();
					if (validate) {
						formGroups = validateDisplayItemGroupBean(v, dgb, dbGroups, formGroups, ruleValidator,
								groupOrdinalPLusItemOid, request, response);
						logger.debug("*** form group size after validation " + formGroups.size());
					}
					diwg.setItemGroup(dgb);
					diwg.setItemGroups(formGroups);

					allItems.set(i, diwg);

				} else {// other single items
					DisplayItemBean dib = diwg.getSingleItem();
					if (validate) {
						dib = validateDisplayItemBean(v, dib, "", ruleValidator, groupOrdinalPLusItemOid, false, null,
								request);//
					}
					ArrayList children = dib.getChildren();
					for (int j = 0; j < children.size(); j++) {

						DisplayItemBean child = (DisplayItemBean) children.get(j);
						String itemName = getInputName(child);
						child.loadFormValue(fp.getString(itemName));
						if (validate) {
							child = validateDisplayItemBean(v, child, "", ruleValidator, groupOrdinalPLusItemOid,
									false, null, request);
						}
						children.set(j, child);
						logger.debug(" J (children): " + j);
					}

					dib.setChildren(children);
					diwg.setSingleItem(runDynamicsItemCheck(dib, null, request));
					logger.debug(" I : " + i);
					allItems.set(i, diwg);
				}
			}
			logMe("allItems  Loop end  " + System.currentTimeMillis());
			// A map from item name to item bean object.
			HashMap<String, ItemBean> scoreItems = new HashMap<String, ItemBean>();
			HashMap<String, String> scoreItemdata = new HashMap<String, String>();
			HashMap<Integer, String> oldItemdata = prepareSectionItemdata(sb.getId(), request);
			// hold all item names of changed ItemBean in current section
			TreeSet<String> changedItems = new TreeSet<String>();
			ArrayList<String> changedItemNamesList = new ArrayList<String>();
			// holds complete disply item beans for checking against 'request
			// for change' restriction
			ArrayList<DisplayItemBean> changedItemsList = new ArrayList<DisplayItemBean>();
			// key is repeating item name, value is its display item group bean
			HashMap<String, DisplayItemGroupBean> changedItemsMap = new HashMap<String, DisplayItemGroupBean>();
			// key is itemid, value is set of itemdata-ordinal
			HashMap<Integer, TreeSet<Integer>> itemOrdinals = prepareItemdataOrdinals(request);

			// prepare item data for scoring
			updateDataOrdinals(allItems);
			section.setDisplayItemGroups(allItems);
			scoreItems = prepareScoreItems(request);
			scoreItemdata = prepareScoreItemdata(request);
			logMe("allItems 2 Loop begin  " + System.currentTimeMillis());
			for (int i = 0; i < allItems.size(); i++) {
				DisplayItemWithGroupBean diwb = allItems.get(i);
				if (diwb.isInGroup()) {
					List<DisplayItemGroupBean> dbGroups = diwb.getDbItemGroups();
					for (int j = 0; j < dbGroups.size(); j++) {
						DisplayItemGroupBean displayGroup = dbGroups.get(j);
						List<DisplayItemBean> items = displayGroup.getItems();
						if ("remove".equalsIgnoreCase(displayGroup.getEditFlag())) {
							for (DisplayItemBean displayItem : items) {
								int itemId = displayItem.getItem().getId();
								int ordinal = displayItem.getData().getOrdinal();
								if (itemOrdinals.containsKey(itemId)) {
									itemOrdinals.get(itemId).remove(ordinal);
								}
								if (scoreItemdata.containsKey(itemId + "_" + ordinal)) {
									scoreItemdata.remove(itemId + "_" + ordinal);
								}
								String formName;
								if (j == 0) {
									formName = getGroupItemInputName(displayGroup, j, displayItem);

									logger.debug("GET: changed formName to " + formName);

								} else {
									formName = getGroupItemManualInputName(displayGroup, j, displayItem);
									logger.debug("GET-MANUAL: changed formName to " + formName);
								}
								changedItems.add(formName);
								changedItemsList.add(displayItem);
								changedItemNamesList.add(formName);
								changedItemsMap.put(formName, displayGroup);
								logger.debug("adding to changed items map: " + formName);
							}
						}
					}

					List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
					HashMap<Integer, Integer> maxOrdinals = new HashMap<Integer, Integer>();
					boolean first = true;
					for (int j = 0; j < dgbs.size(); j++) {
						DisplayItemGroupBean displayGroup = dgbs.get(j);
						List<DisplayItemBean> items = displayGroup.getItems();
						boolean isAdd = "add".equalsIgnoreCase(displayGroup.getEditFlag()) ? true : false;
						for (DisplayItemBean displayItem : items) {
							ItemBean ib = displayItem.getItem();
							String itemName = ib.getName();
							int itemId = ib.getId();
							if (first) {
								maxOrdinals.put(itemId,
										iddao.getMaxOrdinalForGroup(ecb, sb, displayGroup.getItemGroupBean()));
							}
							ItemDataBean idb = displayItem.getData();
							String value = idb.getValue();
							scoreItems.put(itemName, ib);
							int ordinal = displayItem.getData().getOrdinal();
							if (isAdd && scoreItemdata.containsKey(itemId + "_" + ordinal)) {
								int formMax = 1;
								if (maxOrdinals.containsKey(itemId)) {
									formMax = maxOrdinals.get(itemId);
								}
								int dbMax = iddao.getMaxOrdinalForGroup(ecb, sb, displayGroup.getItemGroupBean());
								ordinal = ordinal >= dbMax ? formMax + 1 : ordinal;
								maxOrdinals.put(itemId, ordinal);
								displayItem.getData().setOrdinal(ordinal);
								scoreItemdata.put(itemId + "_" + ordinal, value);
							} else {
								scoreItemdata.put(itemId + "_" + ordinal, value);
							}
							if (itemOrdinals.containsKey(itemId)) {
								itemOrdinals.get(itemId).add(ordinal);
							} else {
								TreeSet<Integer> ordinalSet = new TreeSet<Integer>();
								ordinalSet.add(ordinal);
								itemOrdinals.put(itemId, ordinalSet);
							}
							if (isChanged(displayItem, oldItemdata, attachedFilePath)) {
								String formName;
								if (j == 0) {
									formName = getGroupItemInputName(displayGroup, 0, displayItem);
									logger.debug("RESET: formName group-item-input:" + formName);

								} else {
									formName = getGroupItemManualInputName(displayGroup, j, displayItem);
									logger.debug("RESET: formName group-item-input-manual:" + formName);
								}
								changedItems.add(formName);
								changedItemsList.add(displayItem);
								changedItemNamesList.add(formName);
								changedItemsMap.put(formName, displayGroup);
								logger.debug("adding to changed items map: " + formName);
								if (j > diwb.getDbItemGroups().size() - 1) {
									break; // for new added row we have to show only one '!' for first input if the
											// "reason for change" is active
								}
							}
						}
						first = false;
					}
				} else {
					DisplayItemBean dib = diwb.getSingleItem();
					ItemBean ib = dib.getItem();
					ItemDataBean idb = dib.getData();
					int itemId = ib.getId();
					String itemName = ib.getName();
					String value = idb.getValue();
					scoreItems.put(itemName, ib);
					// for items which are not in any group, their ordinal is
					// set as 1
					TreeSet<Integer> ordinalset = new TreeSet<Integer>();
					ordinalset.add(1);
					itemOrdinals.put(itemId, ordinalset);
					scoreItemdata.put(itemId + "_" + 1, value);
					if (isChanged(idb, oldItemdata, dib, attachedFilePath)) {
						changedItems.add(getInputName(dib));
						changedItemsList.add(dib);
						changedItemNamesList.add(getInputName(dib));
						changedItemsMap.put(getInputName(dib), new DisplayItemGroupBean());
					}

					ArrayList children = dib.getChildren();
					for (int j = 0; j < children.size(); j++) {
						DisplayItemBean child = (DisplayItemBean) children.get(j);
						ItemBean cib = child.getItem();
						scoreItems.put(cib.getName(), cib);
						TreeSet<Integer> cordinalset = new TreeSet<Integer>();
						cordinalset.add(1);
						itemOrdinals.put(itemId, cordinalset);
						scoreItemdata.put(cib.getId() + "_" + 1, child.getData().getValue());
						if (isChanged(child.getData(), oldItemdata, child, attachedFilePath)) {
							changedItems.add(getInputName(child));
							changedItemsList.add(child);
							changedItemNamesList.add(getInputName(child));
							changedItemsMap.put(getInputName(child), new DisplayItemGroupBean());
						}
					}
				}
			}
			logMe("allItems 2 Loop end  " + System.currentTimeMillis());
			// do calculation for 'calculation' and 'group-calculation' type
			// items
			// and write the result in DisplayItemBean's ItemDateBean - data
			ScoreItemValidator sv = new ScoreItemValidator(validatorHelper, discNotes);
			// *** doing calc here, load it where? ***
			SessionManager sm = (SessionManager) request.getSession().getAttribute("sm");
			ScoreCalculator sc = new ScoreCalculator(sm, ecb, ub);
			logMe("allItems 3 Loop begin  " + System.currentTimeMillis());
			for (int i = 0; i < allItems.size(); i++) {
				DisplayItemWithGroupBean diwb = allItems.get(i);
				if (diwb.isInGroup()) {
					List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
					for (int j = 0; j < dgbs.size(); j++) {
						DisplayItemGroupBean displayGroup = dgbs.get(j);

						List<DisplayItemBean> items = displayGroup.getItems();
						for (DisplayItemBean displayItem : items) {
							ItemFormMetadataBean ifmb = displayItem.getMetadata();
							int responseTypeId = ifmb.getResponseSet().getResponseTypeId();
							if (responseTypeId == 8 || responseTypeId == 9) {
								StringBuffer err = new StringBuffer();
								ResponseOptionBean robBean = (ResponseOptionBean) ifmb.getResponseSet().getOptions()
										.get(0);
								String value = "";

								String inputName = "";
								// note that we have to use
								if (displayGroup.isAuto()) {
									inputName = getGroupItemInputName(displayGroup, displayGroup.getFormInputOrdinal(),
											displayItem);
									logger.debug("returning input name: " + inputName);
								} else {
									inputName = getGroupItemManualInputName(displayGroup,
											displayGroup.getFormInputOrdinal(), displayItem);
									logger.debug("returning input name: " + inputName);
								}
								if (robBean.getValue().startsWith("func: getexternalvalue")
										|| robBean.getValue().startsWith("func: getExternalValue")) {

									value = fp.getString(inputName);
									logger.debug("*** just set " + fp.getString(inputName) + " for line 815 "
											+ displayItem.getItem().getName() + " with input name " + inputName);

								} else {
									value = sc.doCalculation(displayItem, scoreItems, scoreItemdata, itemOrdinals, err,
											displayItem.getData().getOrdinal());
								}
								displayItem.loadFormValue(value);
								if (isChanged(displayItem, oldItemdata, attachedFilePath)) {
									changedItemsList.add(displayItem);
									changedItems.add(inputName);
									changedItemNamesList.add(inputName);
									changedItemsMap.put(inputName, displayGroup);
								}

								request.setAttribute(inputName, value);
								if (validate) {
									displayItem = validateCalcTypeDisplayItemBean(sv, displayItem, inputName, request);
									if (err.length() > 0) {
										Validation validation = new Validation(Validator.CALCULATION_FAILED);
										validation.setErrorMessage(err.toString());
										sv.addValidation(inputName, validation);
									}
								}
							}
						}
					}
				} else {
					DisplayItemBean dib = diwb.getSingleItem();
					ItemFormMetadataBean ifmb = dib.getMetadata();
					int responseTypeId = ifmb.getResponseSet().getResponseTypeId();
					if (responseTypeId == 8 || responseTypeId == 9) {
						StringBuffer err = new StringBuffer();
						ResponseOptionBean robBean = (ResponseOptionBean) ifmb.getResponseSet().getOptions().get(0);
						String value = "";
						if (robBean.getValue().startsWith("func: getexternalvalue")
								|| robBean.getValue().startsWith("func: getExternalValue")) {
							String itemName = getInputName(dib);
							value = fp.getString(itemName);
							logger.debug("just set " + fp.getString(itemName) + " for " + dib.getItem().getName());
							logger.debug("found in fp: " + fp.getString(dib.getItem().getName()));
						} else {
							value = sc.doCalculation(dib, scoreItems, scoreItemdata, itemOrdinals, err, 1);
						}
						dib.loadFormValue(value);
						if (isChanged(dib.getData(), oldItemdata, dib, attachedFilePath)) {
							changedItems.add(getInputName(dib));
							changedItemsList.add(dib);
							changedItemNamesList.add(getInputName(dib));
							changedItemsMap.put(getInputName(dib), new DisplayItemGroupBean());
						}
						String inputName = getInputName(dib);
						request.setAttribute(inputName, value);
						if (validate) {
							dib = validateCalcTypeDisplayItemBean(sv, dib, "", request);
							if (err.length() > 0) {
								Validation validation = new Validation(Validator.CALCULATION_FAILED);
								validation.setErrorMessage(err.toString());
								sv.addValidation(inputName, validation);
							}
						}
					}

					ArrayList<DisplayItemBean> children = dib.getChildren();
					for (int j = 0; j < children.size(); j++) {
						DisplayItemBean child = (DisplayItemBean) children.get(j);
						ItemFormMetadataBean cifmb = child.getMetadata();
						int resTypeId = cifmb.getResponseSet().getResponseTypeId();
						if (resTypeId == 8 || resTypeId == 9) {
							StringBuffer cerr = new StringBuffer();
							child.getDbData().setValue(child.getData().getValue());
							ResponseOptionBean crobBean = (ResponseOptionBean) cifmb.getResponseSet().getOptions()
									.get(0);
							String cvalue = "";
							if (crobBean.getValue().startsWith("func: getexternalvalue")
									|| crobBean.getValue().startsWith("func: getExternalValue")) {
								String itemName = getInputName(child);
								cvalue = fp.getString(itemName);
								logger.debug("just set " + fp.getString(itemName) + " for " + child.getItem().getName());

							} else {
								cvalue = sc.doCalculation(child, scoreItems, scoreItemdata, itemOrdinals, cerr, 1);
							}
							child.loadFormValue(cvalue);
							if (isChanged(child.getData(), oldItemdata, child, attachedFilePath)) {
								changedItems.add(getInputName(child));
								changedItemsList.add(child);
								changedItemNamesList.add(getInputName(child));
								changedItemsMap.put(getInputName(child), new DisplayItemGroupBean());
							}
							String cinputName = getInputName(child);
							request.setAttribute(cinputName, cvalue);
							if (validate) {
								child = validateCalcTypeDisplayItemBean(sv, child, "", request);
								if (cerr.length() > 0) {
									Validation cvalidation = new Validation(Validator.CALCULATION_FAILED);
									cvalidation.setErrorMessage(cerr.toString());
									sv.addValidation(cinputName, cvalidation);
								}
							}
						}
						children.set(j, child);
					}
				}
			}

			logMe("allItems 3 Loop end  " + System.currentTimeMillis());

			// we have to do this since we loaded all the form values into the
			// display
			// item beans above
			// section.setItems(items);
			// setting this AFTER we populate notes - will that make a difference?
			section.setDisplayItemGroups(allItems);

			if (currentStudy.getStudyParameterConfig().getInterviewerNameRequired().equals("yes")) {
				v.addValidation(INPUT_INTERVIEWER, Validator.NO_BLANKS);
			}

			if (currentStudy.getStudyParameterConfig().getInterviewDateRequired().equals("yes")) {
				v.addValidation(INPUT_INTERVIEW_DATE, Validator.NO_BLANKS);
			}

			if (!StringUtil.isBlank(fp.getString(INPUT_INTERVIEW_DATE))) {
				v.addValidation(INPUT_INTERVIEW_DATE, Validator.IS_A_DATE);
				v.alwaysExecuteLastValidation(INPUT_INTERVIEW_DATE);
			}

			if (section.getSection().hasSCDItem()) {
				section = SCDItemDisplayInfo.generateSCDDisplayInfo(
						section,
						this.getServletPage(request).equals(Page.INITIAL_DATA_ENTRY)
								|| this.getServletPage(request).equals(Page.ADMIN_EDIT_SERVLET)
								&& !this.isAdminForcedReasonForChange(request));
			}

			if (session.getAttribute(DataEntryServlet.NOTE_SUBMITTED) != null) {
				Map<Object, Boolean> newNotesMap = (Map<Object, Boolean>) session
						.getAttribute(DataEntryServlet.NOTE_SUBMITTED);
				for (Object fieldName : newNotesMap.keySet()) {
					if (fieldName instanceof String && !StringUtil.isBlank((String) fieldName)) {
						v.removeFieldValidations((String) fieldName);
						ruleValidator.removeFieldValidations((String) fieldName);
					}
				}
			}

            //get hardrules. skip soft if errors > 0
			errors = v.validate();
            if (errors.size() > 0 && !checkDobleDataEntryErrors(errors)) {
                request.setAttribute("Hardrules", true);
            }
			reshuffleErrorGroupNamesKK(errors, allItems, request);

			if (this.isAdminForcedReasonForChange(request) && this.isAdministrativeEditing() && errors.isEmpty()) {
				// "You have changed data after this CRF was marked complete. "
				// +
				// "You must provide a Reason For Change discrepancy note for this item before you can save this updated information."
				String error = respage.getString("reason_for_change_error");
				// change everything here from changed items list to changed
				// items map
				if (changedItemsMap.size() > 0) {
                    request.setAttribute("Hardrules", true);
					logger.debug("found admin force reason for change: changed items " + changedItems.toString()
							+ " and changed items list: " + changedItemsList.toString() + " changed items map: "
							+ changedItemsMap.toString());
					logMe("DisplayItemBean  Loop begin  " + System.currentTimeMillis());
					for (DisplayItemBean displayItem : changedItemsList) {
						int index = changedItemsList.indexOf(displayItem);
						String formName = changedItemNamesList.get(index);

						ItemDataBean idb = displayItem.getData();
						ItemFormMetadataBean ifmb = displayItem.getMetadata();
						logger.debug("-- found group label " + ifmb.getGroupLabel());
						if (!ifmb.getGroupLabel().equalsIgnoreCase("Ungrouped")
								&& !ifmb.getGroupLabel().equalsIgnoreCase("")) {
							DisplayItemGroupBean digb = changedItemsMap.remove(formName);
							if (digb != null) {
								this.setReasonForChangeError(idb, formName, error, request);
							}
						} else {
							this.setReasonForChangeError(idb, formName, error, request);
							logger.debug("form name added: " + formName);
						}
					}
					logMe("DisplayItemBean  Loop end  " + System.currentTimeMillis());
				}
				reshuffleErrorGroupNamesKK(errors, allItems, request);
			}

			populateNotesWithDBNoteCounts(discNotes, noteThreads, section, request);

			logger.debug("errors here: " + errors.toString());
			logMe("error check  Loop begin  " + System.currentTimeMillis());
			if (errors.isEmpty() && shouldRunRules) {
				logger.debug("Errors was empty");
				if (session.getAttribute("rulesErrors") != null) {
					// rules have already generated errors, Let's compare old
					// error list with new
					// error list, if lists not same show errors.
					HashMap h = ruleValidator.validate();
					reshuffleErrorGroupNamesKK(h, allItems, request);
					Set<String> a = (Set<String>) session.getAttribute("rulesErrors");
					Set<String> ba = h.keySet();
					Boolean showErrors = false;
					for (Object key : ba) {
						if (!a.contains(key)) {
							showErrors = true;
						}
					}
					if (showErrors) {
						errors = h;
						if (errors.size() > 0) {
							session.setAttribute("shouldRunValidation", "1");
							session.setAttribute("rulesErrors", errors.keySet());
						} else {
							session.setAttribute("shouldRunValidation", null);
							session.setAttribute("rulesErrors", null);
						}
					} else {
						session.setAttribute("shouldRunValidation", null);
						session.setAttribute("rulesErrors", null);

					}

				} else if (session.getAttribute("shouldRunValidation") != null
						&& session.getAttribute("shouldRunValidation").toString().equals("1")) {
					session.setAttribute("shouldRunValidation", null);
					session.setAttribute("rulesErrors", null);
				} else {
                    //get softrules
					errors = ruleValidator.validate();
					reshuffleErrorGroupNamesKK(errors, allItems, request);
					if (errors.size() > 0) {
						session.setAttribute("shouldRunValidation", "1");
						session.setAttribute("rulesErrors", errors.keySet());
					}
				}
			}

			if (!errors.isEmpty()) {
				if (errors.keySet().contains(INPUT_INTERVIEWER) || errors.keySet().contains(INPUT_INTERVIEW_DATE)) {
					request.setAttribute("expandCrfInfo", true);
				}
				
				logger.debug("threw an error with data entry...");
				
				String[] textFields = { INPUT_INTERVIEWER, INPUT_INTERVIEW_DATE };
				fp.setCurrentStringValuesAsPreset(textFields);
				setPresetValues(fp.getPresetValues(), request);
				logMe("!errors if  Loop begin  " + System.currentTimeMillis());
				HashMap<String, ArrayList<String>> siErrors = sv.validate();

				if (siErrors != null && !siErrors.isEmpty()) {
					Iterator iter = siErrors.keySet().iterator();
					while (iter.hasNext()) {
						String fieldName = iter.next().toString();
						errors.put(fieldName, siErrors.get(fieldName));
					}
				}
				// we should 'shift' the names here, tbh 02/2010
				// need: total number of rows, manual rows, all row names
				// plus: error names
				Iterator iter2 = errors.keySet().iterator();
				while (iter2.hasNext()) {
					String fieldName = iter2.next().toString();
					logger.debug("found error " + fieldName);
				}
				Iterator iter3 = errors.keySet().iterator();
				while (iter3.hasNext()) {
					String fieldName = iter3.next().toString();
					logger.debug("found error after shuffle " + fieldName);
				}
				
				request.setAttribute("markComplete", fp.getString(INPUT_MARK_COMPLETE));
				request.setAttribute(BEAN_DISPLAY, section);
				request.setAttribute(BEAN_ANNOTATIONS, fp.getString(INPUT_ANNOTATIONS));
				setInputMessages(errors, request);
				addPageMessage(respage.getString("errors_in_submission_see_below"), request);
				request.setAttribute("hasError", "true");
				session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
				setUpPanel(section);
				request.getSession().setAttribute("dnAdditionalCreatingParameters", createDNParametersMap(request, section));
				forwardPage(getJSPPage(), request, response);
			} else {
				logger.debug("Do we hit this in save ?????");
				logMe("Do we hit this in save ????  " + System.currentTimeMillis());

				boolean success = true;
				boolean temp = true;

				// save interviewer name and date into DB
				ecb.setInterviewerName(fp.getString(INPUT_INTERVIEWER));
				if (!StringUtil.isBlank(fp.getString(INPUT_INTERVIEW_DATE))) {
					ecb.setDateInterviewed(fp.getDate(INPUT_INTERVIEW_DATE));
				} else {
					ecb.setDateInterviewed(null);
				}

				if (ecdao == null) {
					ecdao = new EventCRFDAO(getDataSource());
				}
				// set validator id for DDE
				DataEntryStage stage = ecb.getStage();
				if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
						|| stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
					ecb.setValidatorId(ub.getId());

				}

				if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
						&& changedItemsList.size() > 0) {
					studyEventBean.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
					studyEventBean.setUpdater(ub);
					studyEventBean.setUpdatedDate(new Date());
					seDao.update(studyEventBean);
				}

				// If the Study Subject's Satus is signed and we save a section
				// , change status to available
				logger.debug("Status of Study Subject {}", ssb.getStatus().getName());
				if (ssb.getStatus() == Status.SIGNED && changedItemsList.size() > 0) {
					logger.debug("Status of Study Subject is Signed we are updating");
					StudySubjectDAO studySubjectDao = new StudySubjectDAO(getDataSource());
					ssb.setStatus(Status.AVAILABLE);
					ssb.setUpdater(ub);
					ssb.setUpdatedDate(new Date());
					studySubjectDao.update(ssb);
				}
				if (ecb.isSdvStatus() && changedItemsList.size() > 0) {
					logger.debug("Status of Study Subject is SDV we are updating");
					StudySubjectDAO studySubjectDao = new StudySubjectDAO(getDataSource());
					ssb.setStatus(Status.AVAILABLE);
					ssb.setUpdater(ub);
					ssb.setUpdatedDate(new Date());
					studySubjectDao.update(ssb);
					ecb.setSdvStatus(false);
					ecb.setSdvUpdateId(ub.getId());
				}

				ecb = (EventCRFBean) ecdao.update(ecb);

				if (ecb.isNotStarted()) {
					ecb.setOwner(ub);
				}
				ecb.setNotStarted(false);
				ecdao.update(ecb);
				
				request.setAttribute("previousSEStatus", studyEventBean.getSubjectEventStatus());
				
				StudyEventDefinitionBean sedb = (StudyEventDefinitionBean) seddao.findByPK(studyEventBean
						.getStudyEventDefinitionId());
				SubjectEventStatusUtil.determineSubjectEventState(studyEventBean, study, new DAOWrapper(studydao,
						seDao, ssdao, ecdao, edcdao, dndao));
				studyEventBean = (StudyEventBean) seDao.update(studyEventBean);

				// save discrepancy notes into DB
				FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session
						.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
				dndao = new DiscrepancyNoteDAO(getDataSource());

				AddNewSubjectServlet.saveFieldNotes(INPUT_INTERVIEWER, fdn, dndao, ecb.getId(), "EventCRF",
						currentStudy);
				AddNewSubjectServlet.saveFieldNotes(INPUT_INTERVIEW_DATE, fdn, dndao, ecb.getId(), "EventCRF",
						currentStudy);

				allItems = section.getDisplayItemGroups();

                logger.debug("all items before saving into DB" + allItems.size());

                resetCodedItemTerms(allItems, iddao, ecb, changedItemsList);

				this.output(allItems);
				// TODO:Seems longer here, check this
				logMe("DisplayItemWithGroupBean allitems4 " + System.currentTimeMillis());
				for (int i = 0; i < allItems.size(); i++) {
					DisplayItemWithGroupBean diwb = allItems.get(i);

					// we don't write success = success && writeToDB here
					// since the short-circuit mechanism may prevent Java
					// from executing writeToDB.
					if (diwb.isInGroup()) {

						List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
						// using the above gets us the correct number of manual groups, tbh 01/2010
						List<DisplayItemGroupBean> dbGroups = diwb.getDbItemGroups();
						logger.debug("item group size: " + dgbs.size());
						logger.debug("item db-group size: " + dbGroups.size());
						for (int j = 0; j < dgbs.size(); j++) {
							DisplayItemGroupBean displayGroup = dgbs.get(j);
							List<DisplayItemBean> items = displayGroup.getItems();
							// this ordinal will only useful to create a new
							// item data
							// update an item data won't touch its ordinal
							int nextOrdinal = iddao.getMaxOrdinalForGroup(ecb, sb, displayGroup.getItemGroupBean()) + 1;

							for (DisplayItemBean displayItem : items) {
								String fileName = this.addAttachedFilePath(displayItem, attachedFilePath);
								displayItem.setEditFlag(displayGroup.getEditFlag());
								logger.debug("group item value: " + displayItem.getData().getValue());
								if ("add".equalsIgnoreCase(displayItem.getEditFlag()) && fileName.length() > 0
										&& !newUploadedFiles.containsKey(fileName)) {
									displayItem.getData().setValue("");
								}
								temp = writeToDB(displayItem, iddao, nextOrdinal, request);
								logger.debug("just executed writeToDB - 1");
								logger.debug("next ordinal: " + nextOrdinal);
								if (temp && newUploadedFiles.containsKey(fileName)) {
									newUploadedFiles.remove(fileName);
								}
								String inputName = "";
								if (displayGroup.isAuto()) {
									inputName = getGroupItemInputName(displayGroup, displayGroup.getFormInputOrdinal(),
											displayItem);
								} else {
									inputName = this.getGroupItemManualInputName(displayGroup,
											displayGroup.getFormInputOrdinal(), displayItem);
								}
								logger.trace("&&& we get previous looking at input name: " + inputName);
								// input name 2 removed from below
								AddNewSubjectServlet.saveFieldNotes(inputName, fdn, dndao, displayItem.getData()
										.getId(), "itemData", currentStudy);
								success = success && temp;
							}
						}
						for (int j = 0; j < dbGroups.size(); j++) {
							DisplayItemGroupBean displayGroup = dbGroups.get(j);
							if ("remove".equalsIgnoreCase(displayGroup.getEditFlag())) {
								List<DisplayItemBean> items = displayGroup.getItems();
								for (DisplayItemBean displayItem : items) {
									String fileName = this.addAttachedFilePath(displayItem, attachedFilePath);
									displayItem.setEditFlag(displayGroup.getEditFlag());
									logger.debug("group item value: " + displayItem.getData().getValue());
									if ("add".equalsIgnoreCase(displayItem.getEditFlag()) && fileName.length() > 0
											&& !newUploadedFiles.containsKey(fileName)) {
										displayItem.getData().setValue("");
									}
									temp = writeToDB(displayItem, iddao, 0, request);
									logger.debug("just executed writeToDB - 2");
									if (temp && newUploadedFiles.containsKey(fileName)) {
										newUploadedFiles.remove(fileName);
									}
									// just use 0 here since update doesn't
									// touch ordinal
									success = success && temp;
								}
							}
						}

					} else {
						DisplayItemBean dib = diwb.getSingleItem();
						// TODO work on this line

						this.addAttachedFilePath(dib, attachedFilePath);
						temp = writeToDB(dib, iddao, 1, request);
						logger.debug("just executed writeToDB - 3");
						if (temp && newUploadedFiles.containsKey(dib.getItem().getId() + "")) {
							// so newUploadedFiles will contain only failed file
							// items;
							newUploadedFiles.remove(dib.getItem().getId() + "");
						}

						String inputName = getInputName(dib);
						logger.trace("3 - found input name: " + inputName);
						AddNewSubjectServlet.saveFieldNotes(inputName, fdn, dndao, dib.getData().getId(), "itemData",
								currentStudy);

						success = success && temp;

						ArrayList childItems = dib.getChildren();
						for (int j = 0; j < childItems.size(); j++) {
							DisplayItemBean child = (DisplayItemBean) childItems.get(j);
							this.addAttachedFilePath(child, attachedFilePath);
							temp = writeToDB(child, iddao, 1, request);
							logger.debug("just executed writeToDB - 4");
							if (temp && newUploadedFiles.containsKey(child.getItem().getId() + "")) {
								// so newUploadedFiles will contain only failed
								// file items;
								newUploadedFiles.remove(child.getItem().getId() + "");
							}
							inputName = getInputName(child);
							AddNewSubjectServlet.saveFieldNotes(inputName, fdn, dndao, child.getData().getId(),
									"itemData", currentStudy);
							success = success && temp;
						}
					}
				}

				logMe("DisplayItemWithGroupBean allitems4 end " + System.currentTimeMillis());
				List<Integer> prevShownDynItemDataIds = shouldRunRules ? this.getItemMetadataService()
						.getDynamicsItemFormMetadataDao()
						.findShowItemDataIdsInSection(section.getSection().getId(), ecb.getCRFVersionId(), ecb.getId())
						: new ArrayList<Integer>();
				logMe("DisplayItemWithGroupBean dryrun  start" + System.currentTimeMillis());
				HashMap<String, ArrayList<String>> rulesPostDryRun = runRules(allItems, ruleSets, false,
						shouldRunRules, MessageType.WARNING, phase2, ecb, request);

				HashMap<String, ArrayList<String>> errorsPostDryRun = new HashMap<String, ArrayList<String>>();
				// additional step needed, run rules and see if any items are 'shown' AFTER saving data
				logMe("DisplayItemWithGroupBean dryrun  end" + System.currentTimeMillis());
				boolean inSameSection = false;
				logMe("DisplayItemWithGroupBean allitems4 " + System.currentTimeMillis());
				if (!rulesPostDryRun.isEmpty()) {
					// in same section?

					// iterate through the OIDs and see if any of them belong to this section
					Iterator iter3 = rulesPostDryRun.keySet().iterator();
					while (iter3.hasNext()) {
						String fieldName = iter3.next().toString();
						logger.debug("found oid after post dry run " + fieldName);
						// set up a listing of OIDs in the section
						// BUT: Oids can have the group name in them.
						int ordinal = -1;
						String newFieldName = fieldName;
						String[] fieldNames = fieldName.split("\\.");
						if (fieldNames.length == 2) {
							newFieldName = fieldNames[1];
							// check items in item groups here?
							if (fieldNames[0].contains("[")) {
								int p1 = fieldNames[0].indexOf("[");
								int p2 = fieldNames[0].indexOf("]");
								try {
									ordinal = Integer.valueOf(fieldNames[0].substring(p1 + 1, p2));
								} catch (NumberFormatException e) {
									ordinal = -1;
								}
								fieldNames[0] = fieldNames[0].substring(0, p1);
							}
						}
						List<DisplayItemWithGroupBean> displayGroupsWithItems = section.getDisplayItemGroups();
						for (int i = 0; i < displayGroupsWithItems.size(); i++) {
							DisplayItemWithGroupBean itemWithGroup = displayGroupsWithItems.get(i);
							if (itemWithGroup.isInGroup()) {
								logger.debug("found group: " + fieldNames[0]);
								// do something there
								List<DisplayItemGroupBean> digbs = itemWithGroup.getItemGroups();
								logger.debug("digbs size: " + digbs.size());
								for (int j = 0; j < digbs.size(); j++) {
									DisplayItemGroupBean displayGroup = digbs.get(j);
									if (displayGroup.getItemGroupBean().getOid().equals(fieldNames[0])
											&& displayGroup.getOrdinal() == ordinal - 1) {
										List<DisplayItemBean> items = displayGroup.getItems();

										for (int k = 0; k < items.size(); k++) {
											DisplayItemBean dib = items.get(k);
											if (dib.getItem().getOid().equals(newFieldName)) {
												if (!dib.getMetadata().isShowItem()) {
													logger.debug("found item in group "
															+ this.getGroupItemInputName(displayGroup, j, dib)
															+ " vs. " + fieldName + " and is show item: "
															+ dib.getMetadata().isShowItem());
													dib.getMetadata().setShowItem(true);
												}
												if (prevShownDynItemDataIds == null
														|| !prevShownDynItemDataIds.contains(dib.getData().getId())) {
													inSameSection = true;
													errorsPostDryRun.put(
															this.getGroupItemInputName(displayGroup, j, dib),
															rulesPostDryRun.get(fieldName));
												}
											}
											items.set(k, dib);
										}
										displayGroup.setItems(items);
										digbs.set(j, displayGroup);
									}
								}
								itemWithGroup.setItemGroups(digbs);
							} else {
								DisplayItemBean displayItemBean = itemWithGroup.getSingleItem();
								ItemBean itemBean = displayItemBean.getItem();
								if (newFieldName.equals(itemBean.getOid())) {
									if (!displayItemBean.getMetadata().isShowItem()) {
										// double check there?
										logger.debug("found item " + this.getInputName(displayItemBean) + " vs. "
												+ fieldName + " and is show item: "
												+ displayItemBean.getMetadata().isShowItem());
										// if is repeating, use the other input name? no

										displayItemBean.getMetadata().setShowItem(true);
										if (prevShownDynItemDataIds == null
												|| !prevShownDynItemDataIds.contains(displayItemBean.getData().getId())) {
											inSameSection = true;
											errorsPostDryRun.put(this.getInputName(displayItemBean),
													rulesPostDryRun.get(fieldName));
										}
									}
								}
								itemWithGroup.setSingleItem(displayItemBean);
							}
							displayGroupsWithItems.set(i, itemWithGroup);
						}
						logMe("DisplayItemWithGroupBean allitems4  end,begin" + System.currentTimeMillis());
						List<DisplayItemWithGroupBean> itemGroups = section.getDisplayItemGroups();
						for (DisplayItemWithGroupBean itemGroup : itemGroups) {
							DisplayItemGroupBean displayGroup = itemGroup.getItemGroup();
							if (newFieldName.equals(displayGroup.getItemGroupBean().getOid())) {
								if (!displayGroup.getGroupMetaBean().isShowGroup()) {
									inSameSection = true;
									logger.debug("found itemgroup " + displayGroup.getItemGroupBean().getOid()
											+ " vs. " + fieldName + " and is show item: "
											+ displayGroup.getGroupMetaBean().isShowGroup());
									// hmmm how to set highlighting for a group?
									errorsPostDryRun.put(displayGroup.getItemGroupBean().getOid(),
											rulesPostDryRun.get(fieldName));
									displayGroup.getGroupMetaBean().setShowGroup(true);
									// add necessary rows to the display group here????
									// we have to set the items in the itemGroup for the displayGroup
									loadItemsWithGroupRows(itemGroup, sb, edcb, ecb, request);

								}
							}
						}
						logMe("DisplayItemWithGroupBean allitems4  end,end" + System.currentTimeMillis());
						section.setDisplayItemGroups(displayGroupsWithItems);

					}
					//
					this.getItemMetadataService().updateGroupDynamicsInSection(displayItemWithGroups,
							section.getSection().getId(), ecb);
					toc = TableOfContentsServlet.getDisplayBeanWithShownSections(getDataSource(),
							(DisplayTableOfContentsBean) request.getAttribute(TOC_DISPLAY),
							(DynamicsMetadataService) SpringServletAccess.getApplicationContext(getServletContext())
									.getBean("dynamicsMetadataService"));
					request.setAttribute(TOC_DISPLAY, toc);
					sectionIdsInToc = TableOfContentsServlet.sectionIdsInToc(toc);
					sIndex = TableOfContentsServlet.sectionIndexInToc(section.getSection(), toc, sectionIdsInToc);
					previousSec = this.prevSection(section.getSection(), ecb, toc, sIndex);
					nextSec = this.nextSection(section.getSection(), ecb, toc, sIndex);
					section.setFirstSection(!previousSec.isActive());
					section.setLastSection(!nextSec.isActive());

					// if so, stay at this section
					logger.debug(" in same section: " + inSameSection);
					// System.out.println(" in same section: " + inSameSection);
					if (inSameSection) {
						// copy of one line from early on around line 400, forcing a re-show of the items
						// section = getDisplayBean(hasGroup, true);// include all items, tbh
						// below a copy of three lines from the if errors = true line, tbh 03/2010
						String[] textFields = { INPUT_INTERVIEWER, INPUT_INTERVIEW_DATE };
						fp.setCurrentStringValuesAsPreset(textFields);
						setPresetValues(fp.getPresetValues(), request);
						// below essetially a copy except for rulesPostDryRun
						request.setAttribute(BEAN_DISPLAY, section);
						request.setAttribute(BEAN_ANNOTATIONS, fp.getString(INPUT_ANNOTATIONS));
						setInputMessages(errorsPostDryRun, request);
						addPageMessage(respage.getString("your_answers_activated_hidden_items"), request);
						request.setAttribute("hasError", "true");
						request.setAttribute("hasShown", "true");

						session.setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, discNotes);
						setUpPanel(section);
						forwardPage(getJSPPage(), request, response);
					}
				}

				if (!inSameSection) {// else if not in same section, progress as usual

					ArrayList<String> updateFailedItems = sc.redoCalculations(scoreItems, scoreItemdata, changedItems,
							itemOrdinals, sb.getId());
					success = updateFailedItems.size() > 0 ? false : true;

					// now check if CRF is marked complete
					boolean markComplete = fp.getString(INPUT_MARK_COMPLETE).equals(VALUE_YES);
					boolean markSuccessfully = false; // if the CRF was marked
					// complete
					// successfully
					if (markComplete && section.isLastSection()) {
						logger.debug("need to mark CRF as complete");
						markSuccessfully = markCRFComplete(request);
						logger.debug("...marked CRF as complete: " + markSuccessfully);
						if (!markSuccessfully) {
							request.setAttribute(BEAN_DISPLAY, section);
							request.setAttribute(BEAN_ANNOTATIONS, fp.getString(INPUT_ANNOTATIONS));
							setUpPanel(section);
							forwardPage(getJSPPage(), request, response);
							return;
						}
					}

					// now write the event crf bean to the database
					String annotations = fp.getString(INPUT_ANNOTATIONS);
					setEventCRFAnnotations(annotations, request);
					Date now = new Date();
					ecb.setUpdatedDate(now);
					ecb.setUpdater(ub);
					ecb = (EventCRFBean) ecdao.update(ecb);
					success = success && ecb.isActive();

					StudyEventDAO sedao = new StudyEventDAO(getDataSource());
					StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
					seb.setUpdatedDate(now);
					seb.setUpdater(ub);
					seb = (StudyEventBean) sedao.update(seb);
					success = success && seb.isActive();

					request.setAttribute(INPUT_IGNORE_PARAMETERS, Boolean.TRUE);

					if (newUploadedFiles.size() > 0) {
						if (this.unloadFiles(newUploadedFiles)) {

						} else {
							String missed = "";
							Iterator iter = newUploadedFiles.keySet().iterator();
							while (iter.hasNext()) {
								missed += " " + newUploadedFiles.get(iter.next());
							}
							addPageMessage(
									respage.getString("uploaded_files_not_deleted_or_not_exist") + ": " + missed,
									request);
						}
					}
					if (!success) {
						
						if (updateFailedItems.size() > 0) {
							String mess = "";
							for (String ss : updateFailedItems) {
								mess += ss + ", ";
							}
							mess = mess.substring(0, mess.length() - 2);
							addPageMessage(resexception.getString("item_save_failed_because_database_error") + mess,
									request);
						} else {
							
							addPageMessage(resexception.getString("database_error"), request);
						}
						request.setAttribute(BEAN_DISPLAY, section);
						session.removeAttribute(GROUP_HAS_DATA);
						session.removeAttribute(HAS_DATA_FLAG);
						session.removeAttribute(DDE_PROGESS);
						session.removeAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
						logger.debug("try to remove to_create_crf");
						session.removeAttribute("to_create_crf");

						forwardPage(Page.LIST_STUDY_SUBJECTS_SERVLET, request, response);
					} else {
						boolean forwardingSucceeded = false;

						if (!fp.getString(GO_PREVIOUS).equals("")) {
							if (previousSec.isActive()) {
								forwardingSucceeded = true;
								request.setAttribute(INPUT_EVENT_CRF, ecb);
								request.setAttribute(INPUT_SECTION, previousSec);
								int tabNum = 0;
								if (fp.getString("tabId") == null) {
									tabNum = 1;
								} else {
									tabNum = fp.getInt("tabId");
								}
								request.setAttribute("tabId", new Integer(tabNum - 1).toString());
								forwardPage(getServletPage(request), request, response);
							}
						} else if (!fp.getString(GO_NEXT).equals("")) {
							if (nextSec.isActive()) {
								forwardingSucceeded = true;
								request.setAttribute(INPUT_EVENT_CRF, ecb);
								request.setAttribute(INPUT_SECTION, nextSec);
								int tabNum = 0;
								if (fp.getString("tabId") == null) {
									tabNum = 1;
								} else {
									tabNum = fp.getInt("tabId");
								}
								request.setAttribute("tabId", new Integer(tabNum + 1).toString());
								forwardPage(getServletPage(request), request, response);
							}
						}

						if (!forwardingSucceeded) {
							if (markSuccessfully) {
								addPageMessage(respage.getString("data_saved_CRF_marked_complete"), request);
								session.removeAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
								session.removeAttribute(GROUP_HAS_DATA);
								session.removeAttribute(HAS_DATA_FLAG);
								session.removeAttribute(DDE_PROGESS);
								session.removeAttribute("to_create_crf");

								request.setAttribute("eventId", new Integer(ecb.getStudyEventId()).toString());
								forwardPage(Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET, request, response);
							} else {
								// use clicked 'save'
								addPageMessage(respage.getString("data_saved_continue_entering_edit_later"), request);
								request.setAttribute(INPUT_EVENT_CRF, ecb);
								request.setAttribute(INPUT_EVENT_CRF_ID, new Integer(ecb.getId()).toString());
								// forward to the next section if the previous one
								// is not the last section
								if (!section.isLastSection()) {
									request.setAttribute(INPUT_SECTION, nextSec);
									request.setAttribute(INPUT_SECTION_ID, new Integer(nextSec.getId()).toString());
									session.removeAttribute("mayProcessUploading");
								} else if (section.isLastSection()) { 
									// already the last section, should go back to
									// view event page
									session.removeAttribute(GROUP_HAS_DATA);
									session.removeAttribute(HAS_DATA_FLAG);
									session.removeAttribute(DDE_PROGESS);
									session.removeAttribute("to_create_crf");
									session.removeAttribute("mayProcessUploading");

									request.setAttribute("eventId", new Integer(ecb.getStudyEventId()).toString());
									if (fromViewNotes != null && "1".equals(fromViewNotes)) {
										String viewNotesPageFileName = (String) session
												.getAttribute("viewNotesPageFileName");
										session.removeAttribute("viewNotesPageFileName");
										session.removeAttribute("viewNotesURL");
										if (viewNotesPageFileName != null && viewNotesPageFileName.length() > 0) {
											forwardPage(Page.setNewPage(viewNotesPageFileName, "View Notes"), request,
													response);
										}
									}
									// Clinovo Ticket #173 start
									StdScheduler scheduler = getScheduler(request);
									CalendarLogic calLogic = new CalendarLogic(getDataSource(), scheduler);
									String message = calLogic.MaxMinDaysValidator(seb);
									if (!"empty".equalsIgnoreCase(message)) {
										addPageMessage(message, request);
									}
									// end
									forwardPage(Page.ENTER_DATA_FOR_STUDY_EVENT_SERVLET, request, response);
									return;

								}

								int tabNum = 0;
								if (fp.getString("tabId") == null) {
									tabNum = 1;
								} else {
									tabNum = fp.getInt("tabId");
								}
								if (!section.isLastSection()) {
									request.setAttribute("tabId", new Integer(tabNum + 1).toString());
								}

								forwardPage(getServletPage(request), request, response);

							}
						}
					}
				}

				SubjectEventStatusUtil.determineSubjectEventStates(sedb, ssb, study, new DAOWrapper(studydao, seDao,
						ssdao, ecdao, edcdao, dndao));
			}
		}
	}

    private void resetCodedItemTerms(List<DisplayItemWithGroupBean> allItems, ItemDataDAO iddao, EventCRFBean ecrfBean, ArrayList<DisplayItemBean> changedItemsList) throws Exception {

        for (int i = 0; i < allItems.size(); i++) {
            DisplayItemWithGroupBean diwb = allItems.get(i);
            if (diwb.isInGroup()) {
                List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
                for (int j = 0; j < dgbs.size(); j++) {
                    DisplayItemGroupBean displayGroup = dgbs.get(j);
                    List<DisplayItemBean> items = displayGroup.getItems();
                    for (DisplayItemBean item : items) {
                        if (item.getItem().getDataType().equals(ItemDataType.CODE)) {
                            codedTermValidation(changedItemsList, item, ecrfBean, iddao);
                        }
                    }
                }
            } else {
                DisplayItemBean dib = diwb.getSingleItem();
                if (dib.getItem().getDataType().equals(ItemDataType.CODE)) {
                    codedTermValidation(changedItemsList, dib, ecrfBean, iddao);
                }
            }
        }
    }


    private void codedTermValidation(ArrayList<DisplayItemBean> changedItemsList, DisplayItemBean item, EventCRFBean ecrfBean, ItemDataDAO iddao) throws Exception {

        ItemFormMetadataDAO itemMetaDAO = new ItemFormMetadataDAO(dataSource);
        ItemDAO itemDAO = new ItemDAO(dataSource);

        ItemFormMetadataBean meta = itemMetaDAO.findByItemIdAndCRFVersionId(item.getItem().getId(),
                ecrfBean.getCRFVersionId());
        ItemBean refItem = (ItemBean) itemDAO.findByNameAndCRFVersionId(meta.getCodeRef(),
                ecrfBean.getCRFVersionId());
        ItemDataBean refItemData = iddao.findByItemIdAndEventCRFIdAndOrdinal(refItem.getId(), ecrfBean.getId(), item.getData().getOrdinal());

        if (refItemData.getId() > 0) {
            for (DisplayItemBean displayItemBean : changedItemsList) {
                if (refItemData.getId() == displayItemBean.getData().getId() &&
                        !refItemData.getValue().equalsIgnoreCase(displayItemBean.getData().getValue())) {
                    CodedItem codedItem = (CodedItem) getCodedItemService().findByItemData(item.getData().getId());
                    if (codedItem.getId() > 0 && codedItem.getStatus().equals("CODED")) {
                        resetCodedItem(codedItem, displayItemBean.getData().getValue());
                        item.getData().setValue("");
                    }
                }
            }
        }
    }

    private void resetCodedItem(CodedItem codedItem, String newTerm) throws Exception {
        codedItem.setVerbatimTerm(newTerm);
        codedItem.setDictionary("");
        codedItem.setCodedTerm("");
        codedItem.setStatus("NOT_CODED");
        getCodedItemService().saveCodedItem(codedItem);
    }

    protected void setReasonForChangeError(ItemDataBean idb, String formName, String error, HttpServletRequest request) {
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		HttpSession session = request.getSession();
		FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session
				.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
		HashMap fieldNotes = fdn.getFieldNotes();
		Set<String> setOfItemNamesWithRFCErrors = request.getAttribute("setOfItemNamesWithRFCErrors") == null? new TreeSet<String>() : (TreeSet<String>)request.getAttribute("setOfItemNamesWithRFCErrors");
		int existingNotes = dndao.findNumExistingNotesForItem(idb.getId());
		if (existingNotes > 0) {

			logger.debug("has a note in db " + formName);

			/*
			 * Having existing notes is not enough to let it pass through after changing data. There has to be a
			 * DiscrepancyNote for the latest changed data
			 */
			HashMap<String, Boolean> noteSubmitted = (HashMap<String, Boolean>) session
					.getAttribute(DataEntryServlet.NOTE_SUBMITTED);
			if ((noteSubmitted == null || noteSubmitted.get(formName) == null || !(Boolean) noteSubmitted.get(formName))
					&& (noteSubmitted == null || noteSubmitted.get(idb.getId()) == null || !(Boolean) noteSubmitted
							.get(idb.getId()))) {
				errors.put(formName, error);
				setOfItemNamesWithRFCErrors.add(formName);
			} else {
				logger.debug("found note in session");
				logger.debug("has a note in db: entered an error here: " + formName + ": " + errors.toString());
			}

		} else if (fieldNotes.containsKey(formName)) {
			System.out.println("has note in session");
		} else {
			logger.debug("setting an error for " + formName);
			errors.put(formName, error);
			setOfItemNamesWithRFCErrors.add(formName);
		}
		request.setAttribute("setOfItemNamesWithRFCErrors", setOfItemNamesWithRFCErrors);
	}

	/**
	 * Get the input beans - the EventCRFBean and the SectionBean. For both beans, look first in the request attributes
	 * to see if the bean has been stored there. If not, look in the parameters for the bean id, and then retrieve the
	 * bean from the database. The beans are stored as protected class members.
	 * 
	 * @param request
	 *            TODO
	 */
	@SuppressWarnings("unused")
	protected void getInputBeans(HttpServletRequest request) throws InsufficientPermissionException {

		HttpSession session = request.getSession();
		StudyBean currentStudy = (StudyBean) session.getAttribute("study");

		FormProcessor fp = new FormProcessor(request);
		EventCRFDAO ecdao = new EventCRFDAO(getDataSource());

		SectionDAO sdao = new SectionDAO(getDataSource());
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		if (ecb == null) {
			int eventCRFId = fp.getInt(INPUT_EVENT_CRF_ID, true);
			logger.debug("found event crf id: " + eventCRFId);
			if (eventCRFId > 0) {
				logger.debug("***NOTE*** that we didnt have to create an event crf because we already have one: "
						+ eventCRFId);
				// there is an event CRF already, only need to update
				ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);
                int crfVersionId = fp.getInt(INPUT_CRF_VERSION_ID);
				if (ecb.isNotStarted() && crfVersionId > 0 && crfVersionId != ecb.getCRFVersionId()) {
					ecb.setCRFVersionId(crfVersionId);
					ecb = (EventCRFBean) ecdao.update(ecb);
				}
				int studyEventId = fp.getInt(INPUT_STUDY_EVENT_ID);
				request.setAttribute(INPUT_EVENT_CRF, ecb);
				if (studyEventId > 0) {
					StudyEventDAO sedao = new StudyEventDAO(getDataSource());
					StudyEventBean sEvent = (StudyEventBean) sedao.findByPK(studyEventId);
					ecb = updateECB(sEvent, request);
				}
                session.setAttribute("ecb", ecb);
				request.setAttribute(INPUT_EVENT_CRF, ecb);
			} else {
				// CRF id <=0, so we need to create a new CRF
				// use toCreateCRF as a flag to prevent user to submit event CRF
				// more than once
				// for example, user reloads the page
				String toCreateCRF = (String) session.getAttribute("to_create_crf");
				if (StringUtil.isBlank(toCreateCRF) || "0".equals(toCreateCRF)) {
					session.setAttribute("to_create_crf", "1");
				}
				try {
					logger.debug("Initial: to create an event CRF.");
					String toCreateCRF1 = (String) session.getAttribute("to_create_crf");
					if (!StringUtil.isBlank(toCreateCRF1) && "1".equals(toCreateCRF1)) {
						ecb = createEventCRF(request, fp);
						session.setAttribute("ecb", ecb);
						request.setAttribute(INPUT_EVENT_CRF, ecb);
						session.setAttribute("to_create_crf", "0");
					} else {
						ecb = (EventCRFBean) session.getAttribute("ecb");
					}
					
				} catch (InconsistentStateException ie) {
					ie.printStackTrace();
					addPageMessage(ie.getOpenClinicaMessage(), request);
					throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET,
							ie.getOpenClinicaMessage(), "1");
				} catch (NullPointerException ne) {
					ne.printStackTrace();
					addPageMessage(ne.getMessage(), request);
					throw new InsufficientPermissionException(Page.LIST_STUDY_SUBJECTS_SERVLET, ne.getMessage(), "1");
				}
			}
		}
		// added to allow sections shown on this page
		DisplayTableOfContentsBean displayBean = new DisplayTableOfContentsBean();
		displayBean = TableOfContentsServlet.getDisplayBean(ecb, getDataSource(), currentStudy);
		request.setAttribute(TOC_DISPLAY, displayBean);

		int sectionId = fp.getInt(INPUT_SECTION_ID, true);
		ArrayList sections;
		if (sectionId <= 0) {
			StudyEventDAO studyEventDao = new StudyEventDAO(getDataSource());
			int maximumSampleOrdinal = studyEventDao.getMaxSampleOrdinal(displayBean.getStudyEventDefinition(),
					displayBean.getStudySubject());
			request.setAttribute("maximumSampleOrdinal", maximumSampleOrdinal);

			sections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());

			for (int i = 0; i < sections.size(); i++) {
				SectionBean sb = (SectionBean) sections.get(i);
				sectionId = sb.getId();// find the first section of this CRF
				break;
			}
		}
		SectionBean sb = new SectionBean();
		if (sectionId > 0) {
			sb = (SectionBean) sdao.findByPK(sectionId);
		}

		int tabId = fp.getInt("tabId", true);
		if (tabId <= 0) {
			tabId = 1;
		}
		request.setAttribute(INPUT_TAB, new Integer(tabId));
		request.setAttribute(SECTION_BEAN, sb);
	}

	/**
	 * Tries to check if a seciton has item groups
	 * 
	 * @param fp
	 *            TODO
	 * @param ecb
	 *            TODO
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	protected boolean checkGroups(FormProcessor fp, EventCRFBean ecb) {
		int sectionId = fp.getInt(INPUT_SECTION_ID, true);
		SectionDAO sdao = new SectionDAO(getDataSource());
		if (sectionId <= 0) {
			ArrayList sections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());

			for (int i = 0; i < sections.size(); i++) {
				SectionBean sb = (SectionBean) sections.get(i);
				sectionId = sb.getId();// find the first section of this CRF
				break;
			}
		}

		// we will look into db to see if any repeating items for this CRF
		// section
		ItemGroupDAO igdao = new ItemGroupDAO(getDataSource());
		// find any item group which doesn't equal to 'Ungrouped'
		ItemGroupBean itemGroup = igdao.findTopOneGroupBySectionId(sectionId);
		if (itemGroup != null && itemGroup.getId() > 0) {
			logger.trace("This section has group");
			return true;
		}
		return false;

	}

	/**
	 * Creates a new Event CRF or update the exsiting one, that is, an event CRF can be created but not item data yet,
	 * in this case, still consider it is not started(called uncompleted before)
	 * 
	 * @param request
	 *            TODO
	 * @param fp
	 *            TODO
	 * 
	 * @return
	 * @throws Exception
	 */
	private EventCRFBean createEventCRF(HttpServletRequest request, FormProcessor fp) throws InconsistentStateException {
		locale = request.getLocale();
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
		StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
		EventCRFBean ecb;
		EventCRFDAO ecdao = new EventCRFDAO(getDataSource());

		int crfVersionId = fp.getInt(INPUT_CRF_VERSION_ID);

		logger.trace("***FOUND*** crfversionid: " + crfVersionId);
		int studyEventId = fp.getInt(INPUT_STUDY_EVENT_ID);
		int eventDefinitionCRFId = fp.getInt(INPUT_EVENT_DEFINITION_CRF_ID);
		int subjectId = fp.getInt(INPUT_SUBJECT_ID);
		int eventCRFId = fp.getInt(INPUT_EVENT_CRF_ID);

		logger.trace("look specifically wrt event crf id: " + eventCRFId);

		logger.trace("Creating event CRF.  Study id: " + currentStudy.getId() + "; CRF Version id: " + crfVersionId
				+ "; Study Event id: " + studyEventId + "; Event Definition CRF id: " + eventDefinitionCRFId
				+ "; Subject: " + subjectId);

		StudySubjectDAO ssdao = new StudySubjectDAO(getDataSource());
		StudySubjectBean ssb = ssdao.findBySubjectIdAndStudy(subjectId, currentStudy);

		if (ssb.getId() <= 0) {
			logger.trace("throwing ISE with study subject bean id of " + ssb.getId());
			throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET,
					resexception.getString("begin_data_entry_without_event_but_subject"));
		}

		StudyEventDefinitionDAO seddao = new StudyEventDefinitionDAO(getDataSource());
		StudyEventDefinitionBean sedb = seddao.findByEventDefinitionCRFId(eventDefinitionCRFId);
		if (sedb.getId() <= 0) {
			addPageMessage(resexception.getString("begin_data_entry_without_event_but_study"), request);
			throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET,
					resexception.getString("begin_data_entry_without_event_but_study"));
		}

		CRFVersionDAO cvdao = new CRFVersionDAO(getDataSource());
		EntityBean eb = cvdao.findByPK(crfVersionId);

		if (eb.getId() <= 0) {
			throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET,
					resexception.getString("begin_data_entry_without_event_but_CRF"));
		}

		StudyEventDAO sedao = new StudyEventDAO(getDataSource());
		StudyEventBean sEvent = (StudyEventBean) sedao.findByPK(studyEventId);
		StudyBean studyWithSED = currentStudy;
		if (currentStudy.getParentStudyId() > 0) {
			studyWithSED = new StudyBean();
			studyWithSED.setId(currentStudy.getParentStudyId());
		}

		AuditableEntityBean aeb = sedao.findByPKAndStudy(studyEventId, studyWithSED);

		if (aeb.getId() <= 0) {
			addPageMessage(resexception.getString("begin_data_entry_without_event_but_especified_event"), request);
			throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET,
					resexception.getString("begin_data_entry_without_event_but_especified_event"));
		}

		ecb = new EventCRFBean();
		if (eventCRFId == 0) {// no event CRF created yet
			ArrayList ecList = ecdao.findByEventSubjectVersion(sEvent, ssb, (CRFVersionBean) eb);
			if (ecList.size() > 0) {
				ecb = (EventCRFBean) ecList.get(0);
			} else {
				ecb.setAnnotations("");
				ecb.setCreatedDate(new Date());
				ecb.setCRFVersionId(crfVersionId);

				if (currentStudy.getStudyParameterConfig().getInterviewerNameDefault().equals("blank")) {
					ecb.setInterviewerName("");
				} else {
					// default will be event's owner name
					ecb.setInterviewerName(sEvent.getOwner().getName());

				}
				if (!currentStudy.getStudyParameterConfig().getInterviewDateDefault().equals("blank")) {
					if (sEvent.getDateStarted() != null) {
						ecb.setDateInterviewed(sEvent.getDateStarted());// default
						// date
					} else {
						ecb.setDateInterviewed(null);
					}
				} else {
					ecb.setDateInterviewed(null);
				}
				
				// above depreciated, try without it
				ecb.setOwner(ub);
				
				ecb.setNotStarted(true);
				ecb.setStatus(Status.AVAILABLE);
				ecb.setCompletionStatusId(1);
				ecb.setStudySubjectId(ssb.getId());
				ecb.setStudyEventId(studyEventId);
				ecb.setValidateString("");
				ecb.setValidatorAnnotations("");

				ecb = (EventCRFBean) ecdao.create(ecb);
				logger.debug("*********CREATED EVENT CRF");
			}
		} else {
			// there is an event CRF already, only need to update
			ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);
			ecb.setCRFVersionId(crfVersionId);
			ecb.setUpdatedDate(new Date());
			ecb.setUpdater(ub);
			ecb = updateECB(sEvent, request);
			ecb = (EventCRFBean) ecdao.update(ecb);
		}

		if (ecb.getId() <= 0) {
			addPageMessage(resexception.getString("new_event_CRF_not_created"), request);
			throw new InconsistentStateException(Page.LIST_STUDY_SUBJECTS_SERVLET,
					resexception.getString("new_event_CRF_not_created"));
		} else {
			if (sEvent.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)) {
				sEvent.setSubjectEventStatus(SubjectEventStatus.COMPLETED);
				sEvent.setUpdater(ub);
				sEvent.setUpdatedDate(new Date());
				sedao.update(sEvent);
			} else {
				sEvent.setUpdater(ub);
				sEvent.setUpdatedDate(new Date());
				sedao.update(sEvent);
			}
		}

		return ecb;
	}

	/**
	 * Read in form values and write them to a display item bean. Note that this results in the form value being written
	 * to both the response set bean and the item data bean. The ResponseSetBean is used to display preset values on the
	 * form in the event of error, and the ItemDataBean is used to send values to the database.
	 * 
	 * @param dib
	 *            The DisplayItemBean to write data into.
	 * @param request
	 *            TODO
	 * @return The DisplayItemBean, with form data loaded.
	 */
	protected DisplayItemBean loadFormValue(DisplayItemBean dib, HttpServletRequest request) {
		String inputName = getInputName(dib);
		FormProcessor fp = new FormProcessor(request);
		org.akaza.openclinica.bean.core.ResponseType rt = dib.getMetadata().getResponseSet().getResponseType();

		if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX)
				|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)
				|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
			dib.loadFormValue(fp.getStringArray(inputName));
		} else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CALCULATION)
				|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.GROUP_CALCULATION)) {
			dib.loadFormValue(dib.getData().getValue());
			ResponseOptionBean rob = (ResponseOptionBean) dib.getMetadata().getResponseSet().getOptions().get(0);
			logger.trace("test print of options for coding: " + rob.getValue());
		} else {
			logger.trace("test print: " + inputName + ": " + fp.getString(inputName));
			dib.loadFormValue(fp.getString(inputName));
		}

		return dib;
	}

	/**
	 * This methods will create an array of DisplayItemGroupBean, which contains multiple rows for an item group on the
	 * data entry form.
	 * 
	 * @param digb
	 *            The Item group which has multiple data rows
	 * @param dbGroups
	 *            The original array got from DB which contains multiple data rows
	 * @param formGroups
	 *            The new array got from front end which contains multiple data rows
	 * @param request
	 *            TODO
	 * @return new constructed formGroups, compare to dbGroups, some rows are update, some new ones are added and some
	 *         are removed
	 */
	protected List<DisplayItemGroupBean> loadFormValueForItemGroup(DisplayItemGroupBean digb,
			List<DisplayItemGroupBean> dbGroups, List<DisplayItemGroupBean> formGroups, int eventDefCRFId,
			HttpServletRequest request) {
		SectionBean sb = (SectionBean) request.getAttribute(SECTION_BEAN);
		int manualRows = 0;
		int repeatMax = digb.getGroupMetaBean().getRepeatMax();
		FormProcessor fp = new FormProcessor(request);
		ItemDAO idao = new ItemDAO(getDataSource());
		List<ItemBean> itBeans = idao.findAllItemsByGroupId(digb.getItemGroupBean().getId(), sb.getCRFVersionId());
		logger.debug("+++ starting to review groups: " + repeatMax);

		long timeCheck = System.currentTimeMillis();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);

		// adding this code from below, since we want to pass a null values list
		// in all cases of getDisplayBeansFromItems().
		// For adding null values to display items
		FormBeanUtil formBeanUtil = new FormBeanUtil();
		List<String> nullValuesList = new ArrayList<String>();
		// Get a List<String> of any null values such as NA or NI
		// method returns null values as a List<String>
		nullValuesList = formBeanUtil.getNullValuesByEventCRFDefId(eventDefCRFId, getDataSource());
		long two = System.currentTimeMillis() - timeCheck;
		int firstLoopBreak = 0;
		int secondLoopBreak = 0;
		for (int i = 0; i < repeatMax; i++) {
			DisplayItemGroupBean formGroup = new DisplayItemGroupBean();
			formGroup.setItemGroupBean(digb.getItemGroupBean());
			formGroup.setGroupMetaBean(runDynamicsCheck(digb.getGroupMetaBean(), request));

			ItemGroupBean igb = digb.getItemGroupBean();

			// want to do deep copy here, so always get a fresh copy for items,
			// may use other better way to do, like clone
			List<DisplayItemBean> dibs = FormBeanUtil.getDisplayBeansFromItems(itBeans, getDataSource(), ecb,
					sb.getId(), nullValuesList, getServletContext());

			// get the values from the manually created rows first- not from the
			// rep model
			// second half of the if line is here to protect against looping 
			// split the if loop into two parts, so that we can get what's existing first
			// and then get newly created rows later
			if (fp.getStartsWith(igb.getOid() + "_manual" + i + "input")) {
				formGroup.setOrdinal(i);
				formGroup.setFormInputOrdinal(i);
				formGroup.setAuto(false);
				formGroup.setInputId(igb.getOid() + "_manual" + i);
				logger.debug("1: set auto to false for " + igb.getOid() + " " + i);

				dibs = processInputForGroupItem(fp, dibs, i, digb, false);

				formGroup.setItems(dibs);
				formGroups.add(formGroup);
				manualRows++;
			} else if (!StringUtil.isBlank(fp.getString(igb.getOid() + "_manual" + i + ".newRow"))) {
				formGroup.setOrdinal(i);
				formGroup.setFormInputOrdinal(i);
				formGroup.setInputId(igb.getOid() + "_manual" + i + ".newRow");

				formGroup.setAuto(false);

				logger.debug("2: set auto to false for " + igb.getOid() + " " + i);

				dibs = processInputForGroupItem(fp, dibs, i, digb, false);

				formGroup.setItems(dibs);
				formGroups.add(formGroup);
				manualRows++;

			} else {
				firstLoopBreak++;
			}
		}

		logger.trace("+++ starting to review groups 3: " + repeatMax);
		two = System.currentTimeMillis() - timeCheck;
		logger.trace("time 3: " + two + "ms");

		int ordinal = 0;
		// had the call to form bean utils here
		for (int i = 0; i < repeatMax; i++) {
			DisplayItemGroupBean formGroup = new DisplayItemGroupBean();
			formGroup.setItemGroupBean(digb.getItemGroupBean());
			formGroup.setGroupMetaBean(runDynamicsCheck(digb.getGroupMetaBean(), request));
			ItemGroupBean igb = digb.getItemGroupBean();
			List<DisplayItemBean> dibs = FormBeanUtil.getDisplayBeansFromItems(itBeans, getDataSource(), ecb,
					sb.getId(), nullValuesList, getServletContext());
			logger.trace("+++count for dibs after deep copy: " + dibs.size());
			two = System.currentTimeMillis() - timeCheck;
			if (fp.getStartsWith(igb.getOid() + "_" + i + "input")) {
				formGroup.setInputId(igb.getOid() + "_" + i);
				if (i != 0) {
					formGroup.setOrdinal(++ordinal + manualRows);
					formGroup.setFormInputOrdinal(i);
				}
				formGroup.setAuto(true);
				logger.debug("1: set auto to TRUE for " + igb.getOid() + " " + i);
				dibs = processInputForGroupItem(fp, dibs, i, digb, true);
				logger.trace("+++ group ordinal: " + i + " igb name " + igb.getName());

				formGroup.setItems(dibs);
				formGroups.add(formGroup);
			} else if (!StringUtil.isBlank(fp.getString(igb.getOid() + "_" + i + ".newRow"))) {
				formGroup.setInputId(igb.getOid() + "_" + i);
				if (i != 0) {
					formGroup.setOrdinal(++ordinal + manualRows);
					formGroup.setFormInputOrdinal(i);
				}
				formGroup.setAuto(true);

				logger.debug("2: set auto to TRUE for " + igb.getOid() + " " + i);

				dibs = processInputForGroupItem(fp, dibs, i, digb, true);
				logger.trace("+++ group ordinal: " + i + " igb name " + igb.getName());

				formGroup.setItems(dibs);
				formGroups.add(formGroup);
			} else {
				secondLoopBreak++;
			}

		} 
		logger.debug("first loop: " + firstLoopBreak);
		logger.debug("second loop: " + secondLoopBreak);
		logger.trace("+++ starting to review groups 4: " + repeatMax);
		two = System.currentTimeMillis() - timeCheck;
		logger.trace("time 4: " + two + "ms");
		logger.debug(" manual rows " + manualRows + " formGroup size " + formGroups.size());

		request.setAttribute("manualRows", new Integer(manualRows));
		logger.trace("+++ starting to review groups 5: " + repeatMax);
		two = System.currentTimeMillis() - timeCheck;
		logger.trace("time 5: " + two + "ms");
		Collections.sort(formGroups);// sort all the rows by ordinal

		logger.trace("group row size:" + formGroups.size());
		// suppose we have 3 rows of data from db, the orginal order is 0,1,2,
		// repetition model will submit row number in [ ] like the following:
		// 0,1,2.. consecutive numbers, means no row removed in between
		// 0,1,3,4.. the 2rd row is removed, 3 and 4 are new rows
		int previous = -1;
		for (int j = 0; j < formGroups.size(); j++) {
			DisplayItemGroupBean formItemGroup = formGroups.get(j);
			logger.trace("formGroup Ordinal:" + formItemGroup.getOrdinal());
			// the below if loop addresses a specific problem with the repeating model
			// if we cut a row out of a long list, the repeater returns double ordinals of another row
			// and a second row gets deleted by mistake.
			if (formItemGroup.getOrdinal() == previous) {
				logger.debug("found a match btw previous and ordinal");
				formItemGroup.setEditFlag("edit");
				formItemGroup.setOrdinal(previous + 1);
			}
			if (formItemGroup.getOrdinal() > dbGroups.size() - 1) {
				formItemGroup.setEditFlag("add");
			} else {
				for (int i = 0; i < dbGroups.size(); i++) {
					DisplayItemGroupBean dbItemGroup = dbGroups.get(i);
					if (formItemGroup.getOrdinal() == i) {
						// the first row is different, it could be blank on page
						// just for
						// display, so need to insert this row, not update
						if ("initial".equalsIgnoreCase(dbItemGroup.getEditFlag())) {
							formItemGroup.setEditFlag("add");
						} else {
							dbItemGroup.setEditFlag("edit");
							// need to set up item data id in order to update
							for (DisplayItemBean dib : dbItemGroup.getItems()) {
								ItemDataBean data = dib.getData();
								for (DisplayItemBean formDib : formItemGroup.getItems()) {
									if (formDib.getItem().getId() == dib.getItem().getId()) {
										formDib.getData().setId(data.getId());
										// this will save the data from IDE
										// complete, used only for DDE
										formDib.setDbData(dib.getData());
										logger.debug("+++ +++ form dib get data set id " + data.getId());
										break;
									}
								}
							}

							formItemGroup.setEditFlag("edit");
						}
						break;
					}
				}
			}
			previous = formItemGroup.getOrdinal();

		}

		logger.trace("+++ === DB group row:" + dbGroups.size());

		logger.trace("+++ === DB group contents: " + dbGroups.toString());

		// why do we need to remove this one row below?
		// For the existing rows in dbGroups,if cannot get the edit flag or
		// initial flag for a row,
		// it means the row is removed on the front-end, so the back-end servlet
		// cannot get it.
		for (int i = 0; i < dbGroups.size(); i++) {
			DisplayItemGroupBean dbItemGroup = dbGroups.get(i);
			logger.trace("+++ found edit flag of " + dbItemGroup.getEditFlag() + " for #" + dbItemGroup.getOrdinal());
			if (!"edit".equalsIgnoreCase(dbItemGroup.getEditFlag())
					&& !"initial".equalsIgnoreCase(dbItemGroup.getEditFlag())) {
				if (dbItemGroup.getGroupMetaBean().isShowGroup()) {
					logger.trace("+++ one row removed, edit flag was " + dbItemGroup.getEditFlag());
					logger.debug("+++ one row removed, edit flag was " + dbItemGroup.getEditFlag());
					dbItemGroup.setEditFlag("remove");
				}
			}
		}
		logger.debug("+++ about to return form groups: " + formGroups.toString());
		for (int j = 0; j < formGroups.size(); j++) {
			DisplayItemGroupBean formGroup = formGroups.get(j);
			formGroup.setIndex(j);
		}
		return formGroups;
	}

	/**
	 * @return <code>true</code> if processRequest should validate inputs when the user clicks the "Save" button,
	 *         <code>false</code> otherwise.
	 */
	protected abstract boolean validateInputOnFirstRound();

	/**
	 * Validate the input from the form corresponding to the provided item. Implementing methods should load data from
	 * the form into the bean before validating. The loadFormValue method should be used for this purpose.
	 * <p/>
	 * validateDisplayItemBeanText, validateDisplayItemBeanSingleCV, and validateDisplayItemBeanMultipleCV are provided
	 * to make implementing this method easy.
	 * 
	 * @param v
	 *            The Validator to add validations to.
	 * @param dib
	 *            The DisplayItemBean to validate.
	 * @param request
	 *            TODO
	 * @return The DisplayItemBean which is validated and has form values loaded into it.
	 */
	protected abstract DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib,
			String inputName, HttpServletRequest request);

	protected void validateSCDItemBean(DiscrepancyValidator v, DisplayItemBean dib) {
		ItemFormMetadataBean ibMeta = dib.getMetadata();
		ItemDataBean idb = dib.getData();
		if (StringUtil.isBlank(idb.getValue())) {
			if (ibMeta.isRequired() && dib.getIsSCDtoBeShown()) {
				v.addValidation(this.getInputName(dib), Validator.IS_REQUIRED);
			}
		} else {
			validateShownSCDToBeHiddenSingle(v, dib);
		}
	}

	protected DisplayItemBean validateDisplayItemBean(DiscrepancyValidator v, DisplayItemBean dib, String inputName,
			RuleValidator rv, HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid, Boolean fireRuleValidation,
			ArrayList<String> messages, HttpServletRequest request) {
		return dib;
	}

	protected abstract List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v,
			DisplayItemGroupBean dib, List<DisplayItemGroupBean> digbs, List<DisplayItemGroupBean> formGroups,
			HttpServletRequest request, HttpServletResponse response);

	protected List<DisplayItemGroupBean> validateDisplayItemGroupBean(DiscrepancyValidator v, DisplayItemGroupBean dib,
			List<DisplayItemGroupBean> digbs, List<DisplayItemGroupBean> formGroups, RuleValidator rv,
			HashMap<String, ArrayList<String>> groupOrdinalPLusItemOid, HttpServletRequest request,
			HttpServletResponse response) {
		return digbs;
	}

	/*
	 * function written out here to return itemMetadataGroupBeans after they have been checked for show/hide via
	 * dynamics.
	 */
	private ItemGroupMetadataBean runDynamicsCheck(ItemGroupMetadataBean metadataBean, HttpServletRequest request) {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		try {
			if (!metadataBean.isShowGroup()) {
				boolean showGroup = getItemMetadataService().isGroupShown(metadataBean.getId(), ecb);

				if (getServletPage(request).equals(Page.DOUBLE_DATA_ENTRY_SERVLET)) {
					showGroup = getItemMetadataService().hasGroupPassedDDE(metadataBean.getId(), ecb.getId());
				}
				metadataBean.setShowGroup(showGroup);
			}
		} catch (OpenClinicaException oce) {
			// do nothing for right now, just store the bean
			logger.debug("throws an OCE for " + metadataBean.getId());
		}
		return metadataBean;
	}

	private DisplayItemBean runDynamicsItemCheck(DisplayItemBean dib, Object newParam, HttpServletRequest request) {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		try {
			if (!dib.getMetadata().isShowItem()) {
				boolean showItem = getItemMetadataService().isShown(dib.getItem().getId(), ecb, dib.getData());
				dib.getMetadata().setShowItem(showItem);
			}
		} catch (NullPointerException npe) {
			logger.debug("found NPE! item id " + dib.getItem().getId());
		}

		return dib;

	}

	/*
	 * Perform validation for calculation and group-calculation type. <br> Pre-condition: passed DisplayItemBean
	 * parameter has been loaded with value. @param sv
	 * 
	 */
	protected DisplayItemBean validateCalcTypeDisplayItemBean(ScoreItemValidator sv, DisplayItemBean dib,
			String inputName, HttpServletRequest request) {

		dib = validateDisplayItemBeanText(sv, dib, inputName, request);

		return dib;
	}

	/**
	 * Peform validation on a item which has a TEXT or TEXTAREA response type. If the item has a null value, it's
	 * automatically validated. Otherwise, it's checked against its data type.
	 * 
	 * @param v
	 *            The Validator to add validations to.
	 * @param dib
	 *            The DisplayItemBean to validate.
	 * @param request
	 *            TODO
	 * @return The DisplayItemBean which is validated.
	 */
	protected DisplayItemBean validateDisplayItemBeanText(DiscrepancyValidator v, DisplayItemBean dib,
			String inputName, HttpServletRequest request) {

		FormProcessor fp = new FormProcessor(request);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		if (StringUtil.isBlank(inputName)) {// for single items
			inputName = getInputName(dib);
		}
		ItemBean ib = dib.getItem();
		ItemFormMetadataBean ibMeta = dib.getMetadata();
		ItemDataType idt = ib.getDataType();
		ItemDataBean idb = dib.getData();

		boolean isNull = false;
		ArrayList nullValues = edcb.getNullValuesList();
		for (int i = 0; i < nullValues.size(); i++) {
			NullValue nv = (NullValue) nullValues.get(i);
			if (nv.getName().equals(fp.getString(inputName))) {
				isNull = true;
			}
		}

		if (!isNull) {
			if (StringUtil.isBlank(idb.getValue())) {
				// check required first
				if (ibMeta.isRequired() && ibMeta.isShowItem()) {
					v.addValidation(inputName, Validator.IS_REQUIRED);
				}
			} else {

				if (idt.equals(ItemDataType.ST)) {
					// a string's size could be more than 255, which is more
					// than
					// the db field length
					if(ibMeta.getResponseSet().getResponseType() == org.akaza.openclinica.bean.core.ResponseType.TEXTAREA) {
                        v.addValidation(inputName, Validator.LENGTH_NUMERIC_COMPARISON,
                                NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 1024);
                    } else {
                        v.addValidation(inputName, Validator.LENGTH_NUMERIC_COMPARISON,
                                NumericComparisonOperator.LESS_THAN_OR_EQUAL_TO, 255);
                    }

				} else if (idt.equals(ItemDataType.INTEGER)) {
					v.addValidation(inputName, Validator.IS_AN_INTEGER);
					v.alwaysExecuteLastValidation(inputName);

				} else if (idt.equals(ItemDataType.REAL)) {

					v.addValidation(inputName, Validator.IS_A_NUMBER);
					v.alwaysExecuteLastValidation(inputName);
				} else if (idt.equals(ItemDataType.BL)) {
					// there is no validation here since this data type is
					// explicitly
					// allowed to be null
					// if the string input for this field parses to a non-zero
					// number, the
					// value will be true; otherwise, 0
				} else if (idt.equals(ItemDataType.BN)) {

				} else if (idt.equals(ItemDataType.SET)) {
					v.addValidation(inputName, Validator.IN_RESPONSE_SET_SINGLE_VALUE, dib.getMetadata()
							.getResponseSet());
				} else if (idt.equals(ItemDataType.DATE)) {
					v.addValidation(inputName, Validator.IS_A_DATE);
					v.alwaysExecuteLastValidation(inputName);
				} else if (idt.equals(ItemDataType.PDATE)) {
					v.addValidation(inputName, Validator.IS_PARTIAL_DATE);
					v.alwaysExecuteLastValidation(inputName);
				}
				if (ibMeta.getWidthDecimal().length() > 0) {
					ArrayList<String> params = new ArrayList<String>();
					params.add(0, idt.getName());
					params.add(1, ibMeta.getWidthDecimal());
					v.addValidation(inputName, Validator.IS_VALID_WIDTH_DECIMAL, params);
					v.alwaysExecuteLastValidation(inputName);
				}

				customValidation(v, dib, inputName);

			}
		}
		return dib;
	}

	protected DisplayItemBean validateDisplayItemBeanSingleCV(RuleValidator v, DisplayItemBean dib, String inputName,
			ArrayList<String> messages) {
		if (StringUtil.isBlank(inputName)) {
			inputName = getInputName(dib);
		}
		ItemFormMetadataBean ibMeta = dib.getMetadata();
		ItemDataBean idb = dib.getData();
		if (StringUtil.isBlank(idb.getValue())) {
			if (ibMeta.isRequired() && ibMeta.isShowItem()) {
				v.addValidation(inputName, Validator.IS_REQUIRED);
			}
			v.addValidation(inputName, Validator.IS_AN_RULE, messages);
		} else {
			v.addValidation(inputName, Validator.IS_AN_RULE, messages);
		}
		return dib;
	}

	/**
	 * Peform validation on a item which has a RADIO or SINGLESELECTresponse type. This function checks that the input
	 * isn't blank, and that its value comes from the controlled vocabulary (ResponseSetBean) in the DisplayItemBean.
	 * 
	 * @param v
	 *            The Validator to add validations to.
	 * @param dib
	 *            The DisplayItemBean to validate.
	 * @return The DisplayItemBean which is validated.
	 */
	protected DisplayItemBean validateDisplayItemBeanSingleCV(DiscrepancyValidator v, DisplayItemBean dib,
			String inputName) {
		if (StringUtil.isBlank(inputName)) {
			inputName = getInputName(dib);
		}
		ItemFormMetadataBean ibMeta = dib.getMetadata();
		ItemDataBean idb = dib.getData();
		if (StringUtil.isBlank(idb.getValue())) {
			if (ibMeta.isRequired() && ibMeta.isShowItem()) {
				v.addValidation(inputName, Validator.IS_REQUIRED);
			}
		} else {
			v.addValidation(inputName, Validator.IN_RESPONSE_SET_SINGLE_VALUE, dib.getMetadata().getResponseSet());
		}
		customValidation(v, dib, inputName);
		return dib;
	}

	protected void validateShownSCDToBeHiddenSingle(DiscrepancyValidator v, DisplayItemBean dib) {
		String value = dib.getData().getValue();
		boolean hasDN = dib.getDiscrepancyNotes() != null && dib.getDiscrepancyNotes().size() > 0 ? true : false;
		if (value != null && value.length() > 0 && !dib.getIsSCDtoBeShown() && !hasDN) {
			String message = dib.getScdData().getScdItemMetadataBean().getMessage();
			Validation vl = new Validation(Validator.TO_HIDE_CONDITIONAL_DISPLAY);
			vl.setErrorMessage(message);
			v.addValidation(getInputName(dib), vl);
		}
	}

	/**
	 * Peform validation on a item which has a RADIO or SINGLESELECT response type. This function checks that the input
	 * isn't blank, and that its value comes from the controlled vocabulary (ResponseSetBean) in the DisplayItemBean.
	 * 
	 * @param v
	 *            The Validator to add validations to.
	 * @param dib
	 *            The DisplayItemBean to validate.
	 * @return The DisplayItemBean which is validated.
	 */
	protected DisplayItemBean validateDisplayItemBeanMultipleCV(DiscrepancyValidator v, DisplayItemBean dib,
			String inputName) {
		if (StringUtil.isBlank(inputName)) {
			inputName = getInputName(dib);
		}
		ItemFormMetadataBean ibMeta = dib.getMetadata();
		ItemDataBean idb = dib.getData();
		if (StringUtil.isBlank(idb.getValue())) {
			if (ibMeta.isRequired() && ibMeta.isShowItem()) {
				v.addValidation(inputName, Validator.IS_REQUIRED);
			}
		} else {
			v.addValidation(inputName, Validator.IN_RESPONSE_SET, dib.getMetadata().getResponseSet());
		}
		customValidation(v, dib, inputName);
		return dib;
	}

	/**
	 * @param dib
	 *            A DisplayItemBean representing an input on the CRF.
	 * @return The name of the input in the HTML form.
	 */
	public final String getInputName(DisplayItemBean dib) {
		ItemBean ib = dib.getItem();
		String inputName = "input" + ib.getId();
		return inputName;
	}

	public final String getGroupItemInputName(DisplayItemGroupBean digb, int rowCount, int manualRows,
			DisplayItemBean dib) {
		int ordinal = rowCount - manualRows;
		String inputName = digb.getItemGroupBean().getOid() + "_" + ordinal + getInputName(dib);
		logger.debug("===returning: " + inputName);
		return inputName;
	}

	/**
	 * Creates an input name for an item data entry in an item group
	 * 
	 * @param digb
	 * @param ordinal
	 * @param dib
	 * @return
	 */
	public final String getGroupItemInputName(DisplayItemGroupBean digb, int ordinal, DisplayItemBean dib) {
		String inputName = digb.getItemGroupBean().getOid() + "_" + ordinal + getInputName(dib);
		logger.debug("+++returning: " + inputName);
		return inputName;
	}

	/**
	 * Writes data from the DisplayItemBean to the database. Note that if the bean contains an inactive ItemDataBean,
	 * the ItemDataBean is created; otherwise, the ItemDataBean is updated.
	 * 
	 * @param dib
	 *            The DisplayItemBean from which to write data.
	 * @param iddao
	 *            The DAO to use to access the database.
	 * @param request
	 *            TODO
	 * @return <code>true</code> if the query succeeded, <code>false</code> otherwise.
	 * @throws Exception 
	 */
	protected boolean writeToDB(DisplayItemBean dib, ItemDataDAO iddao, int ordinal, HttpServletRequest request) throws Exception {
		
		ItemDataBean idb = dib.getData();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		if (dib.getEditFlag() != null && "remove".equalsIgnoreCase(dib.getEditFlag())
				&& getItemMetadataService().isShown(idb.getItemId(), ecb, idb)) {
			getItemMetadataService().hideItem(dib.getMetadata(), ecb, idb);
		} else {
			if (getServletPage(request).equals(Page.DOUBLE_DATA_ENTRY_SERVLET)) {
				if (!dib.getMetadata().isShowItem()
						&& !(dib.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId() > 0)
						&& idb.getValue().equals("")
						&& !getItemMetadataService().hasPassedDDE(dib.getMetadata(), ecb, idb)) {
					
					logger.debug("*** not shown - not writing for idb id " + dib.getData().getId() + " and item id "
							+ dib.getItem().getId());
					return true;
				}
			} else {
				
				// We should skip coded items
				if (!dib.getMetadata().isShowItem() && idb.getValue().equals("")
						&& !getItemMetadataService().isShown(dib.getItem().getId(), ecb, dib.getData())
						&& !(dib.getScdData().getScdItemMetadataBean().getScdItemFormMetadataId() > 0)
						&& !(dib.getItem().getItemDataTypeId() == 12)) {
					
					logger.debug("*** not shown - not writing for idb id " + dib.getData().getId() + " and item id "
							+ dib.getItem().getId());
					return true;
				}
			}
		}
		
		return writeToDB(idb, dib, iddao, ordinal, request);
	}

	protected boolean writeToDB(ItemDataBean itemData, DisplayItemBean dib, ItemDataDAO iddao, int ordinal,
			HttpServletRequest request) throws Exception {
		ItemDataBean idb = itemData;
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
		StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		idb.setItemId(dib.getItem().getId());
		idb.setEventCRFId(ecb.getId());

		if (idb.getValue().equals("")) {
			idb.setStatus(getBlankItemStatus());
		} else {
			idb.setStatus(getNonBlankItemStatus(request));
		}
		if (StringUtil.isBlank(dib.getEditFlag())) {

			if (!idb.isActive()) {
				// will this need to change for double data entry?
				idb.setOrdinal(ordinal);
				idb.setCreatedDate(new Date());
				idb.setOwner(ub);
				idb = (ItemDataBean) iddao.create(idb);
				
				// create coded items for event/crf
				if (getCodedItemService() != null) {
					codedItemService.createCodedItem(ecb, dib.getItem(), idb, currentStudy);
				}
				
			} else {
				
				idb.setUpdater(ub);
				// should we update the logic here for nonrepeats?
				logger.info("update item update_id " + idb.getUpdater().getId());
				idb = (ItemDataBean) iddao.updateValue(idb);
			}
		} else {
			
			// for the items in group, they have editFlag
			if ("add".equalsIgnoreCase(dib.getEditFlag())) {
				
				idb.setOrdinal(ordinal);
				idb.setCreatedDate(new Date());
				idb.setOwner(ub);
				logger.debug("create a new item data" + idb.getItemId() + idb.getValue());
				idb.setUpdater(ub);
				idb = (ItemDataBean) iddao.upsert(idb);
				
				if (getCodedItemService() != null) {
					codedItemService.createCodedItem(ecb, dib.getItem(), idb, currentStudy);
				}
				
			} else if ("edit".equalsIgnoreCase(dib.getEditFlag())) {
				
				idb.setUpdater(ub);

				logger.info("update item update_id " + idb.getUpdater().getId());
				// update tbh #5999, #5998; if an item_data was not included in
				// an import data, it won't exist; we need to check on item_data_id
				// to make sure we are running the correct command on the db
				if (idb.getId() != 0) {
					
					idb.setUpdatedDate(new Date());
					idb = (ItemDataBean) iddao.updateValue(idb);
					
				} else {
					
					idb.setCreatedDate(new Date());
					idb.setOrdinal(ordinal);
					idb.setOwner(ub);
					idb = (ItemDataBean) iddao.upsert(idb);
					logger.debug("just ran upsert! " + idb.getId());
				}

			} else if ("remove".equalsIgnoreCase(dib.getEditFlag())) {
				
				logger.debug("REMOVE an item data" + idb.getItemId() + idb.getValue());
				idb.setUpdater(ub);
				idb.setStatus(Status.DELETED);
				idb = (ItemDataBean) iddao.updateValueForRemoved(idb);

				DiscrepancyNoteDAO dnDao = new DiscrepancyNoteDAO(getDataSource());
				List dnNotesOfRemovedItem = dnDao.findExistingNotesForItemData(idb.getId());
				if (!dnNotesOfRemovedItem.isEmpty()) {
					DiscrepancyNoteBean itemParentNote = null;
					for (Object obj : dnNotesOfRemovedItem) {
						if (((DiscrepancyNoteBean) obj).getParentDnId() == 0) {
							itemParentNote = (DiscrepancyNoteBean) obj;
						}
					}
					DiscrepancyNoteBean dnb = new DiscrepancyNoteBean();
					if (itemParentNote != null) {
						dnb.setParentDnId(itemParentNote.getId());
						dnb.setDiscrepancyNoteTypeId(itemParentNote.getDiscrepancyNoteTypeId());
					}
					dnb.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
					dnb.setStudyId(currentStudy.getId());
					dnb.setAssignedUserId(ub.getId());
					dnb.setOwner(ub);
					dnb.setEntityType(DiscrepancyNoteBean.ITEM_DATA);
					dnb.setEntityId(idb.getId());
					dnb.setCreatedDate(new Date());
					dnb.setColumn("value");
					dnb.setDescription("The item has been removed, this Discrepancy Note has been Closed.");
					dnDao.create(dnb);
					dnDao.createMapping(dnb);
					itemParentNote.setResolutionStatusId(ResolutionStatus.CLOSED.getId());
					dnDao.update(itemParentNote);
				}
			}
		}

		return idb.isActive();
	}

	protected String addAttachedFilePath(DisplayItemBean dib, String attachedFilePath) {
		String fileName = "";
		ItemDataBean idb = dib.getData();
		String itemDataValue = idb.getValue();
		String dbValue = dib.getDbData().getValue();
		ResponseSetBean rsb = dib.getMetadata().getResponseSet();
		org.akaza.openclinica.bean.core.ResponseType rt = rsb.getResponseType();
		if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.FILE) && itemDataValue.length() > 0) {
			File file = new File(itemDataValue);
			fileName = file.getName();
			if (itemDataValue.length() > fileName.length()) {
				idb.setValue(itemDataValue);
			} else {
				File f = new File(dbValue);
				fileName = f.getName();
				if (fileName.equals(itemDataValue) && dbValue.length() > itemDataValue.length()) {
					// since filename is unique by timestamp, re-upload will
					// append
					// another timestamp to the same filename
					idb.setValue(dbValue);
				} else {
					idb.setValue(attachedFilePath + itemDataValue);
					fileName = itemDataValue;
				}
			}
		}
		return fileName;
	}

	/**
	 * Retrieve the status which should be assigned to ItemDataBeans which have blank values for this data entry
	 * servlet.
	 */
	protected abstract Status getBlankItemStatus();

	// unavailable in admin. editing

	/**
	 * Retrieve the status which should be assigned to ItemDataBeans which have non-blank values for this data entry
	 * servlet.
	 * 
	 * @param request
	 *            TODO
	 */
	protected abstract Status getNonBlankItemStatus(HttpServletRequest request);

	// unavailable in admin. editing

	/**
	 * Get the eventCRF's annotations as appropriate for this data entry servlet.
	 * 
	 * @param request
	 *            TODO
	 */
	protected abstract String getEventCRFAnnotations(HttpServletRequest request);

	/**
	 * Set the eventCRF's annotations properties as appropriate for this data entry servlet.
	 * 
	 * @param request
	 *            TODO
	 */
	protected abstract void setEventCRFAnnotations(String annotations, HttpServletRequest request);

	/**
	 * Retrieve the DisplaySectionBean which will be used to display the Event CRF Section on the JSP, and also is used
	 * to controll processRequest.
	 * 
	 * @param request
	 *            TODO
	 * @param isSubmitted
	 *            TODO
	 */
	protected DisplaySectionBean getDisplayBean(boolean hasGroup, boolean includeUngroupedItems,
			HttpServletRequest request, boolean isSubmitted) throws Exception {
		DisplaySectionBean section = new DisplaySectionBean();
		FormProcessor fp = new FormProcessor(request);
		HttpSession session = request.getSession();
		StudyBean study = (StudyBean) session.getAttribute("study");
		SessionManager sm = (SessionManager) request.getSession().getAttribute("sm");
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		SectionBean sb = (SectionBean) request.getAttribute(SECTION_BEAN);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		SectionDAO sdao;
		// Find out whether there are ungrouped items in this section
		boolean hasUngroupedItems = false;
		int eventDefinitionCRFId = fp.getInt("eventDefinitionCRFId");

		if (eventDefinitionCRFId <= 0) { // TODO: this block of code repeats
			EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(getDataSource());
			EventDefinitionCRFBean edcBean = edcdao.findByStudyEventIdAndCRFVersionId(study, ecb.getStudyEventId(),
					ecb.getCRFVersionId());
			eventDefinitionCRFId = edcBean.getId();
		}
		logger.trace("eventDefinitionCRFId " + eventDefinitionCRFId);
		// Use this class to find out whether there are ungrouped items in this
		// section
		FormBeanUtil formBeanUtil = new FormBeanUtil();
		List<DisplayItemGroupBean> itemGroups = new ArrayList<DisplayItemGroupBean>();
		if (hasGroup) {
			DisplaySectionBean newDisplayBean = new DisplaySectionBean();
			if (includeUngroupedItems) {
				// Null values: this method adds null values to the
				// displayitembeans
				newDisplayBean = formBeanUtil.createDisplaySectionBWithFormGroups(sb.getId(), ecb.getCRFVersionId(),
						getDataSource(), eventDefinitionCRFId, ecb, getServletContext());
			} else {
				newDisplayBean = formBeanUtil.createDisplaySectionWithItemGroups(study, sb.getId(), ecb,
						ecb.getStudyEventId(), sm, eventDefinitionCRFId, getServletContext());
			}
			itemGroups = newDisplayBean.getDisplayFormGroups();
			logger.trace("found item group size: " + itemGroups.size() + " and to string: " + itemGroups.toString());
			section.setDisplayFormGroups(itemGroups);

		}

		// Find out whether any display items are *not* grouped; see issue 1689
		hasUngroupedItems = formBeanUtil.sectionHasUngroupedItems(getDataSource(), sb.getId(), itemGroups);

		sdao = new SectionDAO(getDataSource());
		sb.setHasSCDItem(hasUngroupedItems ? sdao.hasSCDItem(sb.getId()) : false);

		section.setEventCRF(ecb);

		if (sb.getParentId() > 0) {
			SectionBean parent = (SectionBean) sdao.findByPK(sb.getParentId());
			sb.setParent(parent);
		}

		section.setSection(sb);

		CRFVersionDAO cvdao = new CRFVersionDAO(getDataSource());
		CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
		section.setCrfVersion(cvb);

		CRFDAO cdao = new CRFDAO(getDataSource());
		CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
		section.setCrf(cb);

		section.setEventDefinitionCRF(edcb);

		// setup DAO's here to avoid creating too many objects
		ItemDAO idao = new ItemDAO(getDataSource());
		ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(getDataSource());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());

		// Use itemGroups to determine if there are any ungrouped items

		// get all the parent display item beans not in group
		logMe("Entering getParentDisplayItems::: Thread is? " + Thread.currentThread());
		ArrayList displayItems = getParentDisplayItems(hasGroup, sb, edcb, idao, ifmdao, iddao, hasUngroupedItems,
				request);
		logMe("Entering getParentDisplayItems::: Done and Thread is? " + Thread.currentThread());

		logger.debug("just ran get parent display, has group " + hasGroup + " has ungrouped " + hasUngroupedItems);
		// now sort them by ordinal
		Collections.sort(displayItems);

		// now get the child DisplayItemBeans
		for (int i = 0; i < displayItems.size(); i++) {
			DisplayItemBean dib = (DisplayItemBean) displayItems.get(i);
			dib.setChildren(getChildrenDisplayItems(dib, edcb, request));

			// TODO use the setData command here to make sure we get a value?
			// On Submition of the Admin Editing form the loadDBValue does not required
			//
			if (ecb.getStage() == DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE
					|| ecb.getStage() == DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) {
				if (shouldLoadDBValues(dib) && !isSubmitted) {
					dib.loadDBValue();
				}
			} else {
				if (shouldLoadDBValues(dib)) {
					logger.trace("should load db values is true, set value");
					dib.loadDBValue();
					logger.trace("just got data loaded: " + dib.getData().getValue());
				}
			}

			displayItems.set(i, dib);
		}

		section.setItems(displayItems);

		return section;
	}

	/**
	 * Retrieve the DisplaySectionBean which will be used to display the Event CRF Section on the JSP, and also is used
	 * to controll processRequest.
	 * 
	 * @param request
	 *            TODO
	 */
	protected ArrayList getAllDisplayBeans(HttpServletRequest request) throws Exception {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		ArrayList sections = new ArrayList();
		HttpSession session = request.getSession();
		StudyBean study = (StudyBean) session.getAttribute("study");
		SectionDAO sdao = new SectionDAO(getDataSource());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		// ALL_SECTION_BEANS
		ArrayList<SectionBean> allSectionBeans = (ArrayList<SectionBean>) request.getAttribute(ALL_SECTION_BEANS);

		for (int j = 0; j < allSectionBeans.size(); j++) {

			SectionBean sb = allSectionBeans.get(j);

			DisplaySectionBean section = new DisplaySectionBean();
			section.setEventCRF(ecb);

			if (sb.getParentId() > 0) {
				SectionBean parent = (SectionBean) sdao.findByPK(sb.getParentId());
				sb.setParent(parent);
			}

			section.setSection(sb);

			CRFVersionDAO cvdao = new CRFVersionDAO(getDataSource());
			CRFVersionBean cvb = (CRFVersionBean) cvdao.findByPK(ecb.getCRFVersionId());
			section.setCrfVersion(cvb);

			CRFDAO cdao = new CRFDAO(getDataSource());
			CRFBean cb = (CRFBean) cdao.findByPK(cvb.getCrfId());
			section.setCrf(cb);

			EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(getDataSource());
			EventDefinitionCRFBean edcb = edcdao.findByStudyEventIdAndCRFVersionId(study, ecb.getStudyEventId(),
					cvb.getId());

			section.setEventDefinitionCRF(edcb);

			// setup DAO's here to avoid creating too many objects
			ItemDAO idao = new ItemDAO(getDataSource());
			ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(getDataSource());
			iddao = new ItemDataDAO(getDataSource());

			// get all the display item beans
			ArrayList displayItems = getParentDisplayItems(false, sb, edcb, idao, ifmdao, iddao, false, request);

			logger.debug("222 just ran get parent display, has group " + " FALSE has ungrouped FALSE");
			// now sort them by ordinal
			Collections.sort(displayItems);

			// now get the child DisplayItemBeans
			for (int i = 0; i < displayItems.size(); i++) {
				DisplayItemBean dib = (DisplayItemBean) displayItems.get(i);
				dib.setChildren(getChildrenDisplayItems(dib, edcb, request));

				if (shouldLoadDBValues(dib)) {
					logger.trace("should load db values is true, set value");
					dib.loadDBValue();
				}

				displayItems.set(i, dib);
			}

			section.setItems(displayItems);
			sections.add(section);

		}
		return sections;
	}

	/**
	 * For each single item in this section which is a parent, get a DisplayItemBean corresponding to that item. Note
	 * that an item is a parent iff its parentId == 0.
	 * 
	 * @param sb
	 *            The section whose items we are retrieving.
	 * @param hasUngroupedItems
	 * @param request
	 *            TODO
	 * 
	 * @return An array of DisplayItemBean objects, one per parent item in the section. Note that there is no guarantee
	 *         on the ordering of the objects.
	 * @throws Exception
	 */
	private ArrayList getParentDisplayItems(boolean hasGroup, SectionBean sb, EventDefinitionCRFBean edcb,
			ItemDAO idao, ItemFormMetadataDAO ifmdao, ItemDataDAO iddao, boolean hasUngroupedItems,
			HttpServletRequest request) throws Exception {
		ArrayList answer = new ArrayList();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);

		// DisplayItemBean objects are composed of an ItemBean, ItemDataBean and
		// ItemFormDataBean.
		// However the DAOs only provide methods to retrieve one type of bean at
		// a
		// time (per section)
		// the displayItems hashmap allows us to compose these beans into
		// DisplayItemBean objects,
		// while hitting the database only three times
		HashMap displayItems = new HashMap();

		ArrayList items = new ArrayList();
		ArrayList itemsUngrped = new ArrayList();
		if (hasGroup) {
			if (hasUngroupedItems) {
				itemsUngrped = idao.findAllUngroupedParentsBySectionId(sb.getId(), sb.getCRFVersionId());
			}
		}

		logger.trace("no item groups");
		items = idao.findAllNonRepeatingParentsBySectionId(sb.getId());
		items.addAll(itemsUngrped);
		// }
		logger.debug("items size" + items.size());
		for (int i = 0; i < items.size(); i++) {
			DisplayItemBean dib = new DisplayItemBean();
			dib.setEventDefinitionCRF(edcb);
			ItemBean ib = (ItemBean) items.get(i);
			dib.setItem(ib);
			displayItems.put(new Integer(dib.getItem().getId()), dib);
		}

		ArrayList data = iddao.findAllBySectionIdAndEventCRFId(sb.getId(), ecb.getId());
		for (int i = 0; i < data.size(); i++) {
			ItemDataBean idb = (ItemDataBean) data.get(i);
			DisplayItemBean dib = (DisplayItemBean) displayItems.get(new Integer(idb.getItemId()));
			if (dib != null) {
				dib.setData(idb);
				displayItems.put(new Integer(idb.getItemId()), dib);
			}
		}

		ArrayList metadata = ifmdao.findAllBySectionId(sb.getId());
		for (int i = 0; i < metadata.size(); i++) {
			ItemFormMetadataBean ifmb = (ItemFormMetadataBean) metadata.get(i);
			DisplayItemBean dib = (DisplayItemBean) displayItems.get(new Integer(ifmb.getItemId()));
			if (dib != null) {
				logMe("Entering thread before getting ItemMetadataService:::" + Thread.currentThread());
				boolean showItem = getItemMetadataService().isShown(ifmb.getItemId(), ecb, dib.getData());
				if (getServletPage(request).equals(Page.DOUBLE_DATA_ENTRY_SERVLET)) {
					showItem = getItemMetadataService().hasPassedDDE(ifmb, ecb, dib.getData());
				}
				// is the above needed for children items too?
				boolean passedDDE = getItemMetadataService().hasPassedDDE(ifmb, ecb, dib.getData());
				if (showItem) { // we are only showing, not hiding
					logger.debug("set show item " + ifmb.getItemId() + " idb " + dib.getData().getId() + " show item "
							+ showItem + " passed dde " + passedDDE);
					ifmb.setShowItem(true);
				} else {
					logger.debug("DID NOT set show item " + ifmb.getItemId() + " idb " + dib.getData().getId()
							+ " show item " + showItem + " passed dde " + passedDDE + " value "
							+ dib.getData().getValue());
				}
				dib.setMetadata(ifmb);
				displayItems.put(new Integer(ifmb.getItemId()), dib);
			}
		}

		Iterator hmIt = displayItems.keySet().iterator();
		while (hmIt.hasNext()) {
			Integer key = (Integer) hmIt.next();
			DisplayItemBean dib = (DisplayItemBean) displayItems.get(key);
			answer.add(dib);
			logger.debug("*** getting with key: " + key + " display item bean with value: " + dib.getData().getValue());
		}
		logger.debug("*** test of the display items: " + displayItems.toString());

		return answer;
	}

	/**
	 * Get the DisplayItemBean objects corresponding to the items which are children of the specified parent.
	 * 
	 * @param parent
	 *            The item whose children are to be retrieved.
	 * @param request
	 *            TODO
	 * @return An array of DisplayItemBean objects corresponding to the items which are children of parent, and are
	 *         sorted by column number (ascending), then ordinal (ascending).
	 */
	private ArrayList getChildrenDisplayItems(DisplayItemBean parent, EventDefinitionCRFBean edcb,
			HttpServletRequest request) {
		ArrayList answer = new ArrayList();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		int parentId = parent.getItem().getId();
		ItemDAO idao = new ItemDAO(getDataSource());
		ArrayList childItemBeans = idao.findAllByParentIdAndCRFVersionId(parentId, ecb.getCRFVersionId());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		ItemFormMetadataDAO ifmdao = new ItemFormMetadataDAO(getDataSource());
		for (int i = 0; i < childItemBeans.size(); i++) {
			ItemBean child = (ItemBean) childItemBeans.get(i);
			ItemDataBean data = iddao.findByItemIdAndEventCRFId(child.getId(), ecb.getId());
			ItemFormMetadataBean metadata = ifmdao.findByItemIdAndCRFVersionId(child.getId(), ecb.getCRFVersionId());

			DisplayItemBean dib = new DisplayItemBean();
			dib.setEventDefinitionCRF(edcb);
			dib.setItem(child);
			// tbh
			if (!getServletPage(request).equals(Page.DOUBLE_DATA_ENTRY_SERVLET)) {
				dib.setData(data);
			}
			dib.setDbData(data);
			boolean showItem = getItemMetadataService().isShown(metadata.getItemId(), ecb, data);
			if (getServletPage(request).equals(Page.DOUBLE_DATA_ENTRY_SERVLET)) {
				showItem = getItemMetadataService().hasPassedDDE(metadata, ecb, data);
			}

			if (showItem) {
				logger.debug("set show item: " + metadata.getItemId() + " data " + data.getId());
				metadata.setShowItem(true);
			}

			dib.setMetadata(metadata);

			if (shouldLoadDBValues(dib)) {
				logger.trace("should load db values is true, set value");
				dib.loadDBValue();
				logger.trace("just loaded the child value: " + dib.getData().getValue());
			}

			answer.add(dib);
		}

		// this is a pretty slow and memory intensive way to sort... see if we
		// can
		// have the db do this instead
		Collections.sort(answer);

		return answer;
	}

	/**
	 * gets the available dynamics service
	 */
	public DynamicsMetadataService getItemMetadataService() {
		DynamicsMetadataService itemMetadataService = null;
		itemMetadataService = itemMetadataService != null ? itemMetadataService
				: (DynamicsMetadataService) SpringServletAccess.getApplicationContext(getServletContext()).getBean(
						"dynamicsMetadataService");
		return itemMetadataService;
	}

	/**
	 * @return The Page object which represents this servlet's JSP.
	 */
	protected abstract Page getJSPPage();

	/**
	 * @param request
	 *            TODO
	 * @return The Page object which represents this servlet.
	 */
	protected abstract Page getServletPage(HttpServletRequest request);

	protected abstract boolean shouldLoadDBValues(DisplayItemBean dib);

	protected void setUpPanel(DisplaySectionBean section) {
		resetPanel();
		panel.setStudyInfoShown(false);
		panel.setOrderedData(true);

	}

	/*
	 * change to explicitly re-set the section bean after reviewing the disc note counts
	 */
	protected DisplaySectionBean populateNotesWithDBNoteCounts(FormDiscrepancyNotes discNotes,
			List<DiscrepancyNoteThread> noteThreads, DisplaySectionBean section, HttpServletRequest request) {
        StudyBean currentStudy = (StudyBean) request.getSession().getAttribute(STUDY);
        DiscrepancyNoteUtil dNoteUtil = new DiscrepancyNoteUtil();
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		ArrayList<DiscrepancyNoteBean> ecNotes = dndao.findEventCRFDNotesFromEventCRF(ecb);
		ArrayList<DiscrepancyNoteBean> existingNameNotes = new ArrayList();
		ArrayList<DiscrepancyNoteBean> existingIntrvDateNotes = new ArrayList();
		long t = System.currentTimeMillis();
		logMe("Method:populateNotesWithDBNoteCounts" + t);
		for (int i = 0; i < ecNotes.size(); i++) {
			DiscrepancyNoteBean dn = ecNotes.get(i);
			if (INTERVIEWER_NAME.equalsIgnoreCase(dn.getColumn())) {
				discNotes.setNumExistingFieldNotes(INPUT_INTERVIEWER, 1);
				request.setAttribute("hasNameNote", "yes");
				request.setAttribute(INTERVIEWER_NAME_NOTE, dn);
				if (dn.getParentDnId() == 0)
					existingNameNotes.add(dn);

			}

			if (DATE_INTERVIEWED.equalsIgnoreCase(dn.getColumn())) {
				discNotes.setNumExistingFieldNotes(INPUT_INTERVIEW_DATE, 1);
				request.setAttribute("hasDateNote", "yes");
				request.setAttribute(INTERVIEWER_DATE_NOTE, dn);
				if (dn.getParentDnId() == 0)
					existingIntrvDateNotes.add(dn);
			}
		}
		logMe("Method:populateNotesWithDBNoteCounts before calling setToolTipEventNotes" + System.currentTimeMillis()
				+ "time took:" + (System.currentTimeMillis() - t));
		setToolTipEventNotes(request);

		request.setAttribute("nameNoteResStatus", DiscrepancyNoteUtil.getDiscrepancyNoteResolutionStatus(existingNameNotes));
		request.setAttribute("IntrvDateNoteResStatus", DiscrepancyNoteUtil.getDiscrepancyNoteResolutionStatus(existingIntrvDateNotes));

		request.setAttribute("existingNameNotes", existingNameNotes);

		request.setAttribute("existingIntrvDateNotes", existingIntrvDateNotes);

		List<DisplayItemWithGroupBean> allItems = section.getDisplayItemGroups();
		logger.debug("start to populate notes: " + section.getDisplayItemGroups().size());
		this.output(allItems);
		logMe("Looping through allItems time:" + System.currentTimeMillis() + "time took from the begining"
				+ (System.currentTimeMillis() - t));
		for (int k = 0; k < allItems.size(); k++) {
			DisplayItemWithGroupBean itemWithGroup = allItems.get(k);

			if (itemWithGroup.isInGroup()) {
				logger.debug("group item DNote...");
				List<DisplayItemGroupBean> digbs = itemWithGroup.getItemGroups();
				logger.trace("digbs size: " + digbs.size());
				for (int i = 0; i < digbs.size(); i++) {
					DisplayItemGroupBean displayGroup = digbs.get(i);
					List<DisplayItemBean> items = displayGroup.getItems();
					for (int j = 0; j < items.size(); j++) {
						DisplayItemBean dib = items.get(j);
						String inputName;
						if (i == 0) {
							inputName = getGroupItemInputName(displayGroup, i, dib);
						} else {
							inputName = getGroupItemManualInputName(displayGroup, i, dib);
						}

						int itemDataId = 0;
						if (i <= itemWithGroup.getDbItemGroups().size() - 1) {
							itemDataId = dib.getData().getId();
						} else {
							dib.getData().setId(0);
						}

                        ArrayList dbNotes = dndao.findExistingNotesForItemData(itemDataId);
                        ArrayList notes = discNotes.getNotes(inputName);
                        notes.addAll(dbNotes);
                        noteThreads = dNoteUtil.createThreadsOfParents(notes, getDataSource(), currentStudy, null, -1, true);
                        discNotes.setNumExistingFieldNotes(inputName, dbNotes.size());
                        dib.setNumDiscrepancyNotes(dbNotes.size() + notes.size());
						dib.setDiscrepancyNoteStatus(getDiscrepancyNoteResolutionStatus(dndao, itemDataId, discNotes.getNotes(inputName)));
						dib = setTotals(dib, itemDataId, notes, ecb.getId());
                        DiscrepancyShortcutsAnalyzer.prepareDnShortcutAnchors(request, dib, noteThreads);
						logger.debug("dib note size:" + dib.getNumDiscrepancyNotes() + " " + dib.getData().getId()
								+ " " + inputName);
						items.set(j, dib);
					}
					displayGroup.setItems(items);
					digbs.set(i, displayGroup);
				}
				itemWithGroup.setItemGroups(digbs);

			} else {
				logger.trace("single item db note");
				DisplayItemBean dib = itemWithGroup.getSingleItem();
				try {
					ResponseOptionBean rob = (ResponseOptionBean) dib.getMetadata().getResponseSet().getOptions()
							.get(0);
					logger.trace("test print of options for coding: " + rob.getValue());
				} catch (NullPointerException e) {
					logger.error(e.getMessage());
				}
				int itemDataId = dib.getData().getId();
				int itemId = dib.getItem().getId();
				String inputFieldName = "input" + itemId;

                ArrayList dbNotes = dndao.findExistingNotesForItemData(itemDataId);
                ArrayList notes = discNotes.getNotes(inputFieldName);
                notes.addAll(dbNotes);
                discNotes.setNumExistingFieldNotes(inputFieldName, dbNotes.size());
				dib.setNumDiscrepancyNotes(dbNotes.size() + notes.size());
				dib.setDiscrepancyNoteStatus(getDiscrepancyNoteResolutionStatus(dndao, itemDataId,
						discNotes.getNotes(inputFieldName)));
				dib = setTotals(dib, itemDataId, discNotes.getNotes(inputFieldName), ecb.getId());
                noteThreads = dNoteUtil.createThreadsOfParents(notes, getDataSource(), currentStudy, null, -1, true);
                DiscrepancyShortcutsAnalyzer.prepareDnShortcutAnchors(request, dib, noteThreads);

				ArrayList childItems = dib.getChildren();

				for (int j = 0; j < childItems.size(); j++) {
					DisplayItemBean child = (DisplayItemBean) childItems.get(j);
					int childItemDataId = child.getData().getId();
					int childItemId = child.getItem().getId();
					String childInputFieldName = "input" + childItemId;

					logger.debug("*** setting " + childInputFieldName);
                    ArrayList dbChildNotes = dndao.findExistingNotesForItemData(childItemId);
                    ArrayList childNotes = discNotes.getNotes(inputFieldName);
                    childNotes.addAll(dbNotes);
                    noteThreads = dNoteUtil.createThreadsOfParents(notes, getDataSource(), currentStudy, null, -1, true);
                    discNotes.setNumExistingFieldNotes(childInputFieldName, dbChildNotes.size());
					child.setNumDiscrepancyNotes(dbChildNotes.size() + childNotes.size());
					child.setDiscrepancyNoteStatus(getDiscrepancyNoteResolutionStatus(dndao, childItemDataId,
							discNotes.getNotes(childInputFieldName)));
					child = setTotals(child, childItemDataId, discNotes.getNotes(childInputFieldName), ecb.getId());
                    DiscrepancyShortcutsAnalyzer.prepareDnShortcutAnchors(request, child, noteThreads);
					childItems.set(j, child);
				}
				dib.setChildren(childItems);
				itemWithGroup.setSingleItem(runDynamicsItemCheck(dib, null, request));
			}
			allItems.set(k, itemWithGroup);
		}

		section.setDisplayItemGroups(allItems);
		return section;
	}

	private void setToolTipEventNotes(HttpServletRequest request) {
		long t = System.currentTimeMillis();

		logMe("Method:setToolTipEventNotes" + t);
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		ArrayList<DiscrepancyNoteBean> ecNotes = dndao.findEventCRFDNotesToolTips(ecb);
		ArrayList<DiscrepancyNoteBean> nameNotes = new ArrayList();
		ArrayList<DiscrepancyNoteBean> dateNotes = new ArrayList();

		for (int i = 0; i < ecNotes.size(); i++) {
			DiscrepancyNoteBean dn = ecNotes.get(i);
			if (INTERVIEWER_NAME.equalsIgnoreCase(dn.getColumn())) {
				nameNotes.add(dn);
			}

			if (DATE_INTERVIEWED.equalsIgnoreCase(dn.getColumn())) {
				dateNotes.add(dn);
			}
		}
		request.setAttribute("nameNotes", nameNotes);
		request.setAttribute("intrvDates", dateNotes);
		logMe("existing Method:setToolTipEventNotes, time took" + (System.currentTimeMillis() - t));
	}

	/**
	 * To set the totals of each resolution status on the DisplayItemBean for each item.
	 * 
	 * @param dib
	 * @param notes
	 * @param ecbId
	 *            TODO
	 */
	private DisplayItemBean setTotals(DisplayItemBean dib, int itemDataId, ArrayList<DiscrepancyNoteBean> notes,
			int ecbId) {
		long t = System.currentTimeMillis();
		logMe("Method::::::setTotals" + t);
		int totNew = 0, totRes = 0, totClosed = 0, totUpdated = 0, totNA = 0;
		boolean hasOtherThread = false;
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		ArrayList<DiscrepancyNoteBean> existingNotes = dndao.findExistingNotesForToolTip(itemDataId);
		dib.setDiscrepancyNotes(existingNotes);

		for (DiscrepancyNoteBean obj : dib.getDiscrepancyNotes()) {
			DiscrepancyNoteBean note = obj;

			if (note.getParentDnId() == 0) {
				totNew++;
			}
		}

		ArrayList parentNotes = dndao.findExistingNotesForItemData(itemDataId);
		// Adding this to show the value of only parent threads on discrepancy notes tool tip
		for (Object obj : parentNotes) {
			DiscrepancyNoteBean note = (DiscrepancyNoteBean) obj;
			/*
			 * We would only take the resolution status of the parent note of any note thread. If there are more than
			 * one note thread, the thread with the worst resolution status will be taken.
			 */

			if (note.getParentDnId() == 0) {
				if (hasOtherThread) {
					totNew++;
				}
				hasOtherThread = true;
			}
		}

		logMe("time taken thus far, before audit log check" + (System.currentTimeMillis() - t));
		long t1 = System.currentTimeMillis();
		AuditDAO adao = new AuditDAO(getDataSource());
		ArrayList itemAuditEvents = adao.checkItemAuditEventsExist(dib.getItem().getId(), "item_data", ecbId);
		if (itemAuditEvents.size() > 0) {
			AuditBean itemFirstAudit = (AuditBean) itemAuditEvents.get(0);
			String firstRFC = itemFirstAudit.getReasonForChange();
			String oldValue = itemFirstAudit.getOldValue();
			if (firstRFC != null && "initial value".equalsIgnoreCase(firstRFC)
					&& (oldValue == null || oldValue.isEmpty())) {
				dib.getData().setAuditLog(false);
			} else {
				dib.getData().setAuditLog(true);
			}
		}
		logMe("time taken thus far, after audit log check" + (System.currentTimeMillis() - t));
		logMe("Only for audit check::" + (System.currentTimeMillis() - t1));
		dib.setTotNew(totNew);// totNew is used for parent thread count
		dib.setTotRes(totRes);
		dib.setTotUpdated(totUpdated);
		dib.setTotClosed(totClosed);
		dib.setTotNA(totNA);

		logMe("returning back..time taken" + (System.currentTimeMillis() - t));
		return dib;
	}

	/**
	 * The following methods are for 'mark CRF complete'
	 * 
	 * @param request
	 *            TODO
	 * 
	 * @return
	 */

	@SuppressWarnings("deprecation")
	protected boolean markCRFComplete(HttpServletRequest request) throws Exception {
		locale = request.getLocale();
		HttpSession session = request.getSession();
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		EventCRFDAO ecdao = new EventCRFDAO(getDataSource());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		getEventCRFBean(request);
		getEventDefinitionCRFBean(request);
		DataEntryStage stage = ecb.getStage();

		logger.trace("inout_event_crf_id:" + ecb.getId());

		if (stage.equals(DataEntryStage.UNCOMPLETED) || stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
				|| stage.equals(DataEntryStage.LOCKED)) {
			addPageMessage(respage.getString("not_mark_CRF_complete1"), request);
			return false;
		}

		if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE) || stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {

			if (!edcb.isDoubleEntry()) {
				addPageMessage(respage.getString("not_mark_CRF_complete2"), request);
				return false;
			}
		}

		if (isEachRequiredFieldFillout(request) == false) {
			addPageMessage(respage.getString("not_mark_CRF_complete4"), request);
			return false;
		}

		Status newStatus = ecb.getStatus();
		boolean ide = true;
		if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && edcb.isDoubleEntry()) {
			newStatus = Status.PENDING;
			ecb.setUpdaterId(ub.getId());
			ecb.setUpdater(ub);
			ecb.setUpdatedDate(new Date());
			ecb.setDateCompleted(new Date());
		} else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && !edcb.isDoubleEntry()) {
			newStatus = Status.UNAVAILABLE;
			ecb.setUpdaterId(ub.getId());
			ecb.setUpdater(ub);
			ecb.setUpdatedDate(new Date());
			ecb.setDateCompleted(new Date());
			ecb.setDateValidateCompleted(new Date());
			
		} else if (stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE) && edcb.isDoubleEntry()) {
			newStatus = Status.UNAVAILABLE;
			ecb.setUpdaterId(ub.getId());
			ecb.setUpdater(ub);
			ecb.setUpdatedDate(new Date());
			ecb.setDateCompleted(new Date());
			ecb.setDateValidateCompleted(new Date());
			ide = false;
		} else if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
				|| stage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
			newStatus = Status.UNAVAILABLE;
			ecb.setDateValidateCompleted(new Date());
			ide = false;
		}

		// for the non-reviewed sections, no item data in DB yet, need to
		// create them
		if (!isEachSectionReviewedOnce(request)) {
			boolean canSave = saveItemsToMarkComplete(newStatus, request);
			if (canSave == false) {
				addPageMessage(respage.getString("not_mark_CRF_complete3"), request);
				return false;
			}
		}
		ecb.setStatus(newStatus);
		/*
		 * Marking the data entry as signed if the corresponding EventDefinitionCRF is being enabled for electronic
		 * signature.
		 */
		if (edcb.isElectronicSignature()) {
			ecb.setElectronicSignatureStatus(true);
		}
		ecb = (EventCRFBean) ecdao.update(ecb);
		// note the below statement only updates the DATES, not the STATUS
		ecdao.markComplete(ecb, ide);

		// update all the items' status to complete
		iddao.updateStatusByEventCRF(ecb, newStatus);

		// change status for study event
		StudyDAO sdao = new StudyDAO(getDataSource());
		StudySubjectDAO ssdao = new StudySubjectDAO(getDataSource());
		StudyEventDAO sedao = new StudyEventDAO(getDataSource());
		StudyEventBean seb = (StudyEventBean) sedao.findByPK(ecb.getStudyEventId());
		seb.setUpdatedDate(new Date());
		seb.setUpdater(ub);
		
		SubjectEventStatus previousSEStatus = (SubjectEventStatus) request.getAttribute("previousSEStatus");
		
		EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(getDataSource());
		DiscrepancyNoteDAO discDao = new DiscrepancyNoteDAO(getDataSource());
		StudyBean study = (StudyBean) session.getAttribute("study");
		SubjectEventStatusUtil.determineSubjectEventState(seb, study, new DAOWrapper(sdao, sedao, ssdao, ecdao, edcdao,
				discDao));
		seb = (StudyEventBean) sedao.update(seb);

		//Clinovo calendar func
		if (seb.getSubjectEventStatus() == SubjectEventStatus.COMPLETED && previousSEStatus != SubjectEventStatus.COMPLETED) {
			System.out.println("AutoSchedule");
			StdScheduler scheduler = getScheduler(request);
			CalendarLogic calLogic = new CalendarLogic(getDataSource(),scheduler);
			calLogic.ScheduleSubjectEvents(seb);
			String message = calLogic.MaxMinDaysValidator(seb);
			if (!"empty".equalsIgnoreCase(message)) {
				addPageMessage(message, request);
			}
		}
		//end

		request.setAttribute(INPUT_EVENT_CRF, ecb);
		request.setAttribute(EVENT_DEF_CRF_BEAN, edcb);
		return true;
	}

	@SuppressWarnings("unused")
	private void getEventCRFBean(HttpServletRequest request) {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		FormProcessor fp = new FormProcessor(request);
		int eventCRFId = fp.getInt(INPUT_EVENT_CRF_ID);

		EventCRFDAO ecdao = new EventCRFDAO(getDataSource());
		ecb = (EventCRFBean) ecdao.findByPK(eventCRFId);
	}

	protected boolean isEachRequiredFieldFillout(HttpServletRequest request) {
		HttpSession session = request.getSession();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		DiscrepancyNoteDAO dndao = new DiscrepancyNoteDAO(getDataSource());
		// need to update this method to accomodate dynamics, tbh
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		ItemDAO idao = new ItemDAO(getDataSource());
		ItemFormMetadataDAO itemFormMetadataDao = new ItemFormMetadataDAO(getDataSource());
		int allRequiredNum = idao.findAllRequiredByCRFVersionId(ecb.getCRFVersionId());
		int allRequiredFilledOut = iddao.findAllRequiredByEventCRFId(ecb);
		int allRequiredButHidden = itemFormMetadataDao.findCountAllHiddenByCRFVersionId(ecb.getCRFVersionId());
		int allHiddenButShown = itemFormMetadataDao.findCountAllHiddenButShownByEventCRFId(ecb.getId());
		// add all hidden items minus all hidden but now shown items to the allRequiredFilledOut variable

		if (allRequiredNum > allRequiredFilledOut + allRequiredButHidden - allHiddenButShown) {
			logger.debug("using crf version number: " + ecb.getCRFVersionId());
			logger.debug("allRequiredNum > allRequiredFilledOut:" + allRequiredNum + " " + allRequiredFilledOut
					+ " plus " + allRequiredButHidden + " minus " + allHiddenButShown);
			return false;
		}
		// had to change the query below to allow for hidden items here
		ArrayList allFilled = iddao.findAllBlankRequiredByEventCRFId(ecb.getId(), ecb.getCRFVersionId());
		int numNotes = 0;
		if (!allFilled.isEmpty()) {
			logger.trace("allFilled is not empty");
			FormDiscrepancyNotes fdn = (FormDiscrepancyNotes) session
					.getAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME);
			HashMap idNotes = fdn.getIdNotes();
			for (int i = 0; i < allFilled.size(); i++) {
				ItemDataBean idb = (ItemDataBean) allFilled.get(i);
				int exsitingNotes = dndao.findNumExistingNotesForItem(idb.getId());
				if (exsitingNotes > 0) {
					logger.trace("has existing note");
					numNotes++;
				} else if (idNotes.containsKey(idb.getId())) {
					logger.trace("has note in session");
					numNotes++;
				}
			}
			logger.trace("numNotes allFilled.size:" + numNotes + " " + allFilled.size());
			if (numNotes >= allFilled.size()) {
				logger.trace("all required are filled out");
				return true;
			} else {
				logger.debug("numNotes < allFilled.size() " + numNotes + ": " + allFilled.size());
				return false;
			}
		}
		return true;
	}

	/**
	 * 06/13/2007- jxu Since we don't require users to review each section before mark a CRF as complete, we need to
	 * create item data in the database because items will not be created unless the section which contains the items is
	 * reviewed by users
	 * 
	 * @param request
	 *            TODO
	 */
	private boolean saveItemsToMarkComplete(Status completeStatus, HttpServletRequest request) throws Exception {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		SectionDAO sdao = new SectionDAO(getDataSource());
		ArrayList sections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		ItemDAO idao = new ItemDAO(getDataSource());
		for (int i = 0; i < sections.size(); i++) {
			SectionBean sb = (SectionBean) sections.get(i);
			if (!isCreateItemReqd(sb, request)) {
				ArrayList items = idao.findAllBySectionId(sb.getId());
				for (int j = 0; j < items.size(); j++) {
					ItemBean item = (ItemBean) items.get(j);
					ArrayList<ItemDataBean> itemBean = iddao.findAllByEventCRFIdAndItemIdNoStatus(ecb.getId(),
							item.getId());
					ItemDataBean idb = new ItemDataBean();
					idb.setItemId(item.getId());
					idb.setEventCRFId(ecb.getId());
					idb.setCreatedDate(new Date());
					idb.setOrdinal(1);
					idb.setOwner(ub);
					if (completeStatus != null) {// to avoid null exception
						idb.setStatus(completeStatus);
					} else {
						idb.setStatus(Status.UNAVAILABLE);
					}
					idb.setValue("");
					boolean save = true;
					if (itemBean.size() > 0)
						save = false;
					if (save) {
						iddao.create(idb);

					}
				}
			}
		}

		return true;
	}

	/**
	 * Checks if a section is reviewed at least once by user updated tbh 03/2011, to fix duplicates issues
	 * 
	 * @param sb
	 * @param request
	 *            TODO
	 * @return
	 */
	protected boolean isSectionReviewedOnce(SectionBean sb, HttpServletRequest request) {
		SectionDAO sdao = new SectionDAO(getDataSource());
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		DataEntryStage stage = ecb.getStage();

		HashMap numItemsHM = sdao.getNumItemsBySectionId();
		HashMap numItemsPendingHM = sdao.getNumItemsPendingBySectionId(ecb);
		HashMap numItemsCompletedHM = sdao.getNumItemsCompletedBySectionId(ecb);
		HashMap numItemsBlankHM = sdao.getNumItemsBlankBySectionId(ecb);

		Integer key = new Integer(sb.getId());

		int numItems = TableOfContentsServlet.getIntById(numItemsHM, key);
		int numItemsPending = TableOfContentsServlet.getIntById(numItemsPendingHM, key);
		int numItemsCompleted = TableOfContentsServlet.getIntById(numItemsCompletedHM, key);
		int numItemsBlank = TableOfContentsServlet.getIntById(numItemsBlankHM, key);
		System.out.println(" for " + key + " num items " + numItems + " num items blank " + numItemsBlank
				+ " num items pending " + numItemsPending + " completed " + numItemsCompleted);

		if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && edcb.isDoubleEntry()) {
			if (numItemsPending == 0 && numItems > 0) {
				System.out.println("returns false on ide loop " + key);
				return false;
			}
		} else {

			if (numItemsCompleted == 0 && numItems > 0) {
				System.out.println("returns false on other loop " + key);
				return false;
			}

		}

		return true;

	}

	protected boolean isCreateItemReqd(SectionBean sb, HttpServletRequest request) {
		SectionDAO sdao = new SectionDAO(getDataSource());
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		DataEntryStage stage = ecb.getStage();

		HashMap numItemsHM = sdao.getNumItemsBySectionId();
		HashMap numItemsPendingHM = sdao.getNumItemsPendingBySectionId(ecb);
		HashMap numItemsCompletedHM = sdao.getNumItemsCompletedBySection(ecb);
		HashMap numItemsBlankHM = sdao.getNumItemsBlankBySectionId(ecb);

		Integer key = new Integer(sb.getId());

		int numItems = TableOfContentsServlet.getIntById(numItemsHM, key);
		int numItemsPending = TableOfContentsServlet.getIntById(numItemsPendingHM, key);
		int numItemsCompleted = TableOfContentsServlet.getIntById(numItemsCompletedHM, key);
		int numItemsBlank = TableOfContentsServlet.getIntById(numItemsBlankHM, key);
		System.out.println(" for " + key + " num items " + numItems + " num items blank " + numItemsBlank
				+ " num items pending " + numItemsPending + " completed " + numItemsCompleted);

		if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && edcb.isDoubleEntry()) {
			if (numItemsPending == 0 && numItems > 0) {
				System.out.println("returns false on ide loop " + key);
				return false;
			}
		} else {

			if (numItemsCompleted < numItems) {
				return false;
			}

		}

		return true;

	}

	/**
	 * Checks if all the sections in an event crf are reviewed once tbh updated to prevent duplicates, 03/2011
	 * 
	 * @param request
	 *            TODO
	 * 
	 * @return
	 */
	protected boolean isEachSectionReviewedOnce(HttpServletRequest request) {
		SectionDAO sdao = new SectionDAO(getDataSource());
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		DataEntryStage stage = ecb.getStage();
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		
		ArrayList<SectionBean> sections = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());

		HashMap numItemsHM = sdao.getNumItemsBySectionId();
		HashMap numItemsPendingHM = sdao.getNumItemsPendingBySectionId(ecb);
		HashMap numItemsCompletedHM = sdao.getNumItemsCompletedBySectionId(ecb);

		for (int i = 0; i < sections.size(); i++) {
			SectionBean sb = sections.get(i);
			Integer key = new Integer(sb.getId());

			int numItems = TableOfContentsServlet.getIntById(numItemsHM, key);
			int numItemsPending = TableOfContentsServlet.getIntById(numItemsPendingHM, key);
			int numItemsCompleted = TableOfContentsServlet.getIntById(numItemsCompletedHM, key);

			if (stage.equals(DataEntryStage.INITIAL_DATA_ENTRY) && edcb.isDoubleEntry()) {
				if (numItemsPending == 0 && numItems > 0) {
					return false;
				}

			} else {
				if (numItemsCompleted == 0 && numItems > 0) {
					return false;
				} else if (numItemsCompleted < numItems) {
					return false;
				}
			}
		}

		return true;
		// if we get this far, all sections are checked and we return a true
		// return true;
	}

	@SuppressWarnings("unused")
	protected void getEventDefinitionCRFBean(HttpServletRequest request) {
		HttpSession session = request.getSession();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		{
			EventDefinitionCRFDAO edcdao = new EventDefinitionCRFDAO(getDataSource());
			StudyBean study = (StudyBean) session.getAttribute("study");
			edcb = edcdao.findByStudyEventIdAndCRFVersionId(study, ecb.getStudyEventId(), ecb.getCRFVersionId());
		}
	}

	private DisplayItemBean getDisplayItemBeanForFirstItemDataBean(List<DisplayItemBean> items,
			List<ItemDataBean> dataItems) {
		for (DisplayItemBean displayItemBean : items) {
			for (ItemDataBean itemDataBean : dataItems) {
				if (displayItemBean.getItem().getId() == itemDataBean.getItemId()) {
					return displayItemBean;
				}
			}
		}
		return items.get(0);
	}
    
	/**
	 * Constructs a list of DisplayItemWithGroupBean, which is used for display a section of items on the UI
	 * 
	 * @param dsb
	 * @param hasItemGroup
	 * @param request
	 *            TODO
	 * @return
	 */
	protected List<DisplayItemWithGroupBean> createItemWithGroups(DisplaySectionBean dsb, boolean hasItemGroup,
			int eventCRFDefId, HttpServletRequest request) {
		HttpSession session = request.getSession();
		List<DisplayItemWithGroupBean> displayItemWithGroups = new ArrayList<DisplayItemWithGroupBean>();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		ItemDAO idao = new ItemDAO(getDataSource());
		// For adding null values to display items
		FormBeanUtil formBeanUtil = new FormBeanUtil();
		List<String> nullValuesList = new ArrayList<String>();
		SectionBean sb = (SectionBean) request.getAttribute(SECTION_BEAN);
		EventDefinitionCRFBean edcb = (EventDefinitionCRFBean) request.getAttribute(EVENT_DEF_CRF_BEAN);
		nullValuesList = formBeanUtil.getNullValuesByEventCRFDefId(eventCRFDefId, getDataSource());
		ArrayList items = dsb.getItems();
		logger.trace("single items size: " + items.size());
		for (int i = 0; i < items.size(); i++) {
			DisplayItemBean item = (DisplayItemBean) items.get(i);
			DisplayItemWithGroupBean newOne = new DisplayItemWithGroupBean();
			newOne.setSingleItem(runDynamicsItemCheck(item, null, request));
			newOne.setOrdinal(item.getMetadata().getOrdinal());
			newOne.setInGroup(false);
			newOne.setPageNumberLabel(item.getMetadata().getPageNumberLabel());
			displayItemWithGroups.add(newOne);
		}

		if (hasItemGroup) {
			ItemDataDAO iddao = new ItemDataDAO(getDataSource());
			ArrayList data = iddao.findAllActiveBySectionIdAndEventCRFId(sb.getId(), ecb.getId());
			if (data != null && data.size() > 0) {
				session.setAttribute(HAS_DATA_FLAG, true);
			}
			logger.trace("found data: " + data.size());
			logger.trace("data.toString: " + data.toString());

			for (DisplayItemGroupBean itemGroup : dsb.getDisplayFormGroups()) {
				logger.debug("found one itemGroup");
				DisplayItemWithGroupBean newOne = new DisplayItemWithGroupBean();
				// to arrange item groups and other single items, the ordinal of
				// a item group will be the ordinal of the first item in this
				// group
				DisplayItemBean firstItem = getDisplayItemBeanForFirstItemDataBean(itemGroup.getItems(), data);

				newOne.setPageNumberLabel(firstItem.getMetadata().getPageNumberLabel());

				newOne.setItemGroup(itemGroup);
				newOne.setInGroup(true);
				newOne.setOrdinal(itemGroup.getGroupMetaBean().getOrdinal());

				List<ItemBean> itBeans = idao.findAllItemsByGroupId(itemGroup.getItemGroupBean().getId(),
						sb.getCRFVersionId());

				boolean hasData = false;
				int checkAllColumns = 0;
				// if a group has repetitions, the number of data of
				// first item should be same as the row number
				for (int i = 0; i < data.size(); i++) {
					ItemDataBean idb = (ItemDataBean) data.get(i);

					logger.debug("check all columns: " + checkAllColumns);
					if (idb.getItemId() == firstItem.getItem().getId()) {
						hasData = true;
						logger.debug("set has data to --TRUE--");
						checkAllColumns = 0;
						// so that we only fire once a row
						logger.debug("has data set to true");
						DisplayItemGroupBean digb = new DisplayItemGroupBean();
						// always get a fresh copy for items, may use other
						// better way to
						// do deep copy, like clone
						List<DisplayItemBean> dibs = FormBeanUtil.getDisplayBeansFromItems(itBeans, getDataSource(),
								ecb, sb.getId(), edcb, 0, getServletContext());

						digb.setItems(dibs);
						logger.trace("set with dibs list of : " + dibs.size());
						digb.setGroupMetaBean(runDynamicsCheck(itemGroup.getGroupMetaBean(), request));
						digb.setItemGroupBean(itemGroup.getItemGroupBean());
						newOne.getItemGroups().add(digb);
						newOne.getDbItemGroups().add(digb);
					}
				}
                
				List<DisplayItemGroupBean> groupRows = newOne.getItemGroups();
				logger.trace("how many group rows:" + groupRows.size());
				logger.trace("how big is the data:" + data.size());
				if (hasData) {
					session.setAttribute(GROUP_HAS_DATA, Boolean.TRUE);
					// iterate through the group rows, set data for each item in
					// the group
					for (int i = 0; i < groupRows.size(); i++) {
						DisplayItemGroupBean displayGroup = groupRows.get(i);
						for (DisplayItemBean dib : displayGroup.getItems()) {
							for (int j = 0; j < data.size(); j++) {
								ItemDataBean idb = (ItemDataBean) data.get(j);
								if (idb.getItemId() == dib.getItem().getId() && !idb.isSelected()) {
									idb.setSelected(true);
									dib.setData(idb);
									logger.debug("--> set data " + idb.getId() + ": " + idb.getValue());

									if (shouldLoadDBValues(dib)) {
										logger.debug("+++should load db values is true, set value");
										dib.loadDBValue();
										logger.debug("+++data loaded: " + idb.getName() + ": " + idb.getOrdinal() + " "
												+ idb.getValue());
										logger.debug("+++try dib OID: " + dib.getItem().getOid());
									}
									break;
								}
							}
						}

					}
				} else {
					session.setAttribute(GROUP_HAS_DATA, Boolean.FALSE);
					// no data, still add a blank row for displaying
					DisplayItemGroupBean digb2 = new DisplayItemGroupBean();
					List<DisplayItemBean> dibs = FormBeanUtil.getDisplayBeansFromItems(itBeans, getDataSource(), ecb,
							sb.getId(), nullValuesList, getServletContext());
					digb2.setItems(dibs);
					logger.trace("set with nullValuesList of : " + nullValuesList);
					digb2.setEditFlag("initial");
					digb2.setGroupMetaBean(itemGroup.getGroupMetaBean());
					digb2.setItemGroupBean(itemGroup.getItemGroupBean());
					newOne.getItemGroups().add(digb2);
					newOne.getDbItemGroups().add(digb2);

				}

				displayItemWithGroups.add(newOne);
			}

		}
		Collections.sort(displayItemWithGroups);

		return displayItemWithGroups;
	}

	protected void loadItemsWithGroupRows(DisplayItemWithGroupBean itemWithGroup, SectionBean sb,
			EventDefinitionCRFBean edcb, EventCRFBean ecb, HttpServletRequest request) {
		// this method is a copy of the method: createItemWithGroups ,
		// only modified for load one DisplayItemWithGroupBean.
		//

		ItemDAO idao = new ItemDAO(getDataSource());
		// For adding null values to display items
		FormBeanUtil formBeanUtil = new FormBeanUtil();
		List<String> nullValuesList = new ArrayList<String>();
		// method returns null values as a List<String>
		nullValuesList = formBeanUtil.getNullValuesByEventCRFDefId(edcb.getId(), getDataSource());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		ArrayList data = iddao.findAllActiveBySectionIdAndEventCRFId(sb.getId(), ecb.getId());
		DisplayItemGroupBean itemGroup = itemWithGroup.getItemGroup();
		// to arrange item groups and other single items, the ordinal of
		// a item group will be the ordinal of the first item in this
		// group
		DisplayItemBean firstItem = getDisplayItemBeanForFirstItemDataBean(itemGroup.getItems(), data);

		itemWithGroup.setPageNumberLabel(firstItem.getMetadata().getPageNumberLabel());

		itemWithGroup.setItemGroup(itemGroup);
		itemWithGroup.setInGroup(true);
		itemWithGroup.setOrdinal(itemGroup.getGroupMetaBean().getOrdinal());

		List<ItemBean> itBeans = idao.findAllItemsByGroupId(itemGroup.getItemGroupBean().getId(), sb.getCRFVersionId());

		boolean hasData = false;
		int checkAllColumns = 0;
		// if a group has repetitions, the number of data of
		// first item should be same as the row number
		for (int i = 0; i < data.size(); i++) {
			ItemDataBean idb = (ItemDataBean) data.get(i);

			logger.debug("check all columns: " + checkAllColumns);
			if (idb.getItemId() == firstItem.getItem().getId()) {
				hasData = true;
				logger.debug("set has data to --TRUE--");
				checkAllColumns = 0;
				// so that we only fire once a row
				logger.debug("has data set to true");
				DisplayItemGroupBean digb = new DisplayItemGroupBean();
				// always get a fresh copy for items, may use other
				// better way to
				// do deep copy, like clone
				List<DisplayItemBean> dibs = FormBeanUtil.getDisplayBeansFromItems(itBeans, getDataSource(), ecb,
						sb.getId(), edcb, 0, getServletContext());

				digb.setItems(dibs);
				logger.trace("set with dibs list of : " + dibs.size());
				digb.setGroupMetaBean(runDynamicsCheck(itemGroup.getGroupMetaBean(), request));
				digb.setItemGroupBean(itemGroup.getItemGroupBean());
				itemWithGroup.getItemGroups().add(digb);
				itemWithGroup.getDbItemGroups().add(digb);
			}
		}

		List<DisplayItemGroupBean> groupRows = itemWithGroup.getItemGroups();
		logger.trace("how many group rows:" + groupRows.size());
		logger.trace("how big is the data:" + data.size());
		if (hasData) {
			// iterate through the group rows, set data for each item in
			// the group
			for (int i = 0; i < groupRows.size(); i++) {
				DisplayItemGroupBean displayGroup = groupRows.get(i);
				for (DisplayItemBean dib : displayGroup.getItems()) {
					for (int j = 0; j < data.size(); j++) {
						ItemDataBean idb = (ItemDataBean) data.get(j);
						if (idb.getItemId() == dib.getItem().getId() && !idb.isSelected()) {
							idb.setSelected(true);
							dib.setData(idb);
							logger.debug("--> set data " + idb.getId() + ": " + idb.getValue());

							if (shouldLoadDBValues(dib)) {
								logger.debug("+++should load db values is true, set value");
								dib.loadDBValue();
								logger.debug("+++data loaded: " + idb.getName() + ": " + idb.getOrdinal() + " "
										+ idb.getValue());
								logger.debug("+++try dib OID: " + dib.getItem().getOid());
							}
							break;
						}
					}
				}

			}
		} else {
			// no data, still add a blank row for displaying
			DisplayItemGroupBean digb2 = new DisplayItemGroupBean();
			List<DisplayItemBean> dibs = FormBeanUtil.getDisplayBeansFromItems(itBeans, getDataSource(), ecb,
					sb.getId(), nullValuesList, getServletContext());
			digb2.setItems(dibs);
			logger.trace("set with nullValuesList of : " + nullValuesList);
			digb2.setEditFlag("initial");
			digb2.setGroupMetaBean(itemGroup.getGroupMetaBean());
			digb2.setItemGroupBean(itemGroup.getItemGroupBean());
			itemWithGroup.getItemGroups().add(digb2);
			itemWithGroup.getDbItemGroups().add(digb2);

		}
	}

	private List<DisplayItemBean> processInputForGroupItem(FormProcessor fp, List<DisplayItemBean> dibs, int i,
			DisplayItemGroupBean digb, boolean isAuto) {
		for (int j = 0; j < dibs.size(); j++) {
			DisplayItemBean displayItem = dibs.get(j);
			String inputName = "";
			org.akaza.openclinica.bean.core.ResponseType rt = displayItem.getMetadata().getResponseSet()
					.getResponseType();
			if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX)
					|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {

				if (isAuto) {
					inputName = getGroupItemInputName(digb, i, displayItem);
				} else {
					inputName = getGroupItemManualInputName(digb, i, displayItem);
				}
				ArrayList valueArray = fp.getStringArray(inputName);
				displayItem.loadFormValue(valueArray);

			} else {
				if (isAuto) {
					inputName = getGroupItemInputName(digb, i, displayItem);
				} else {
					inputName = getGroupItemManualInputName(digb, i, displayItem);
				}
				displayItem.loadFormValue(fp.getString(inputName));
				if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
					ensureSelectedOption(displayItem);
				}
			}

		}
		return dibs;
	}

	public final String getGroupItemManualInputName(DisplayItemGroupBean digb, int ordinal, DisplayItemBean dib) {
		String inputName = digb.getItemGroupBean().getOid() + "_manual" + ordinal + getInputName(dib);
		logger.info("+++ returning manual: " + inputName);
		return inputName;
	}

	private EventCRFBean updateECB(StudyEventBean sEvent, HttpServletRequest request) {
		StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		if (!currentStudy.getStudyParameterConfig().getInterviewerNameDefault().equals("blank")
				&& ("".equals(ecb.getInterviewerName()) || ecb.getInterviewerName() == null)) {
			// default will be event's owner name
			ecb.setInterviewerName(sEvent.getOwner().getName());
		}

		if (!currentStudy.getStudyParameterConfig().getInterviewDateDefault().equals("blank")
				&& ("".equals(ecb.getDateInterviewed()) || ecb.getDateInterviewed() == null)) {
			if (sEvent.getDateStarted() != null) {
				ecb.setDateInterviewed(sEvent.getDateStarted());// default date
			} else {
				ecb.setDateInterviewed(null);
			}
		}

		return ecb;
	}

	protected HashMap<String, ItemBean> prepareScoreItems(HttpServletRequest request) {
		HashMap<String, ItemBean> scoreItems = new HashMap<String, ItemBean>();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		ItemDAO idao = new ItemDAO(getDataSource());
		ArrayList<ItemBean> ibs = idao.findAllItemsByVersionId(ecb.getCRFVersionId());
		for (ItemBean ib : ibs) {
			scoreItems.put(ib.getName(), ib);
		}

		return scoreItems;
	}

	protected HashMap<String, String> prepareScoreItemdata(HttpServletRequest request) {
		HashMap<String, String> scoreItemdata = new HashMap<String, String>();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		SectionDAO sdao = new SectionDAO(getDataSource());
		ArrayList<SectionBean> sbs = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
		for (SectionBean section : sbs) {
			HashMap<String, String> data = prepareSectionItemDataBeans(section.getId(), request);
			if (data != null && data.size() > 0) {
				scoreItemdata.putAll(data);
			}
		}
		return scoreItemdata;
	}

	protected HashMap<String, String> prepareSectionItemDataBeans(int sectionId, HttpServletRequest request) {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		HashMap<String, String> scoreItemdata = new HashMap<String, String>();
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		ArrayList<ItemDataBean> idbs = iddao.findAllActiveBySectionIdAndEventCRFId(sectionId, ecb.getId());
		for (ItemDataBean idb : idbs) {
			if (idb.getId() > 0) {
				int ordinal = idb.getOrdinal();
				ordinal = ordinal > 0 ? ordinal : 1;
				scoreItemdata.put(idb.getItemId() + "_" + ordinal, idb.getValue().length() > 0 ? idb.getValue() : "");
			}
		}
		return scoreItemdata;
	}

	@SuppressWarnings("null")
	protected HashMap<Integer, TreeSet<Integer>> prepareItemdataOrdinals(HttpServletRequest request) {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		HashMap<Integer, TreeSet<Integer>> ordinals = new HashMap<Integer, TreeSet<Integer>>();
		SectionDAO sdao = new SectionDAO(getDataSource());
		ArrayList<SectionBean> sbs = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		for (SectionBean section : sbs) {
			ArrayList<ItemDataBean> idbs = iddao.findAllActiveBySectionIdAndEventCRFId(section.getId(), ecb.getId());
			if (idbs != null && idbs.size() > 0) {
				for (ItemDataBean idb : idbs) {
					int itemId = idb.getItemId();
					TreeSet<Integer> os = new TreeSet<Integer>();
					if (ordinals == null) {
						os.add(idb.getOrdinal());
						ordinals.put(itemId, os);
					} else if (ordinals.containsKey(itemId)) {
						os = ordinals.get(itemId);
						os.add(idb.getOrdinal());
						ordinals.put(itemId, os);
					} else {
						os.add(idb.getOrdinal());
						ordinals.put(itemId, os);
					}
				}
			}
		}
		return ordinals;
	}

	protected HashMap<Integer, Integer> prepareGroupSizes(HashMap<String, ItemBean> scoreItems,
			HttpServletRequest request) {
		HashMap<Integer, Integer> groupSizes = new HashMap<Integer, Integer>();
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		SectionDAO sdao = new SectionDAO(getDataSource());
		Iterator iter = scoreItems.keySet().iterator();
		while (iter.hasNext()) {
			int itemId = scoreItems.get(iter.next().toString()).getId();
			groupSizes.put(itemId, 1);
		}

		ArrayList<SectionBean> sbs = sdao.findAllByCRFVersionId(ecb.getCRFVersionId());
		for (SectionBean section : sbs) {
			ArrayList<ItemDataBean> idbs = iddao.findAllActiveBySectionIdAndEventCRFId(section.getId(), ecb.getId());
			for (ItemDataBean idb : idbs) {
				int itemId = idb.getItemId();
				if (groupSizes != null && groupSizes.containsKey(itemId)) {
					int groupsize = iddao.getGroupSize(itemId, ecb.getId());
					groupsize = groupsize > 0 ? groupsize : 1;
					groupSizes.put(itemId, groupsize);
				}
			}
		}
		return groupSizes;
	}

	protected HashMap<Integer, String> prepareSectionItemdata(int sectionId, HttpServletRequest request) {
		EventCRFBean ecb = (EventCRFBean) request.getAttribute(INPUT_EVENT_CRF);
		ItemDataDAO iddao = new ItemDataDAO(getDataSource());
		HashMap<Integer, String> itemdata = new HashMap<Integer, String>();
		ArrayList<ItemDataBean> idbs = iddao.findAllActiveBySectionIdAndEventCRFId(sectionId, ecb.getId());
		for (ItemDataBean idb : idbs) {
			itemdata.put(idb.getId(), idb.getValue());
		}
		return itemdata;
	}

	protected boolean isChanged(ItemDataBean idb, HashMap<Integer, String> oldItemdata) {
		String value = idb.getValue();
		if (!oldItemdata.containsKey(idb.getId()))
			return true;
		else {
			String oldValue = oldItemdata.get(idb.getId());
			if (oldValue != null) {
				if (value == null)
					return true;
				else if (!oldValue.equals(value))
					return true;
			} else if (value != null)
				return true;
		}
		return false;
	}

	protected boolean isChanged(DisplayItemBean dib, HashMap<Integer, String> oldItemdata, String attachedFilePath) {
		ItemDataBean idb = dib.getData();
		String value = idb.getValue();
		if (!oldItemdata.containsKey(idb.getId()))
			return true;
		else {
			String oldValue = oldItemdata.get(idb.getId());
			if (oldValue != null) {
				if (value == null)
					return true;
				else if (dib.getItem().getDataType().getId() == 11) {
					String theOldValue = oldValue.split("(/|\\\\)")[oldValue.split("(/|\\\\)").length - 1].trim();
					return !value.equals(theOldValue);
				} else if (!oldValue.equals(value))
					return true;
			} else if (value != null)
				return true;
		}
		return false;
	}

	protected boolean isChanged(ItemDataBean idb, HashMap<Integer, String> oldItemdata, DisplayItemBean dib,
			String attachedFilePath) {
		return isChanged(dib, oldItemdata, attachedFilePath);
	}

	/**
	 * Output, just logs all contents of the allItems list. tbh, 01/2010
	 * 
	 * @param displayItemWithGroups
	 */
	protected void output(List<DisplayItemWithGroupBean> displayItemWithGroups) {
		for (int i = 0; i < displayItemWithGroups.size(); ++i) {
			DisplayItemWithGroupBean diwb = displayItemWithGroups.get(i);

			if (diwb.isInGroup()) {
				List<DisplayItemGroupBean> dbGroups = diwb.getDbItemGroups();
				logger.trace("+++++++ DB ITEM GROUPS ++++++++");
				for (int j = 0; j < dbGroups.size(); j++) {
					DisplayItemGroupBean displayGroup = dbGroups.get(j);
					List<DisplayItemBean> items = displayGroup.getItems();
					for (DisplayItemBean displayItem : items) {
						int itemId = displayItem.getItem().getId();
						int ordinal = displayItem.getData().getOrdinal();
						if ("initial".equalsIgnoreCase(displayGroup.getEditFlag())) {
							logger.trace("* found initial: " + itemId + " " + ordinal);
						} else {
							logger.trace("** found NOT initial: " + itemId + " " + ordinal);
						}
					}
				}

				List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
				logger.trace("+++++++++ ITEM GROUPS ++++++++++");
				for (int j = 0; j < dgbs.size(); j++) {
					DisplayItemGroupBean displayGroup = dgbs.get(j);
					List<DisplayItemBean> oItems = displayGroup.getItems();
					String editFlag = displayGroup.getEditFlag();
					for (DisplayItemBean displayItem : oItems) {
						int itemId = displayItem.getItem().getId();
						logger.trace("*** found edit Flag " + itemId + ": " + editFlag);
					}
				}
			}
		}
	}

	protected void updateDataOrdinals(List<DisplayItemWithGroupBean> displayItemWithGroups) {
		for (int i = 0; i < displayItemWithGroups.size(); ++i) {
			DisplayItemWithGroupBean diwb = displayItemWithGroups.get(i);
			HashMap<Integer, String> editFlags = new HashMap<Integer, String>();
			HashMap<Integer, Integer> nextOrdinals = new HashMap<Integer, Integer>();
			if (diwb.isInGroup()) {
				List<DisplayItemGroupBean> dbGroups = diwb.getDbItemGroups();
				for (int j = 0; j < dbGroups.size(); j++) {
					DisplayItemGroupBean displayGroup = dbGroups.get(j);
					List<DisplayItemBean> items = displayGroup.getItems();
					for (DisplayItemBean displayItem : items) {
						int itemId = displayItem.getItem().getId();
						int ordinal = displayItem.getData().getOrdinal();
						if ("initial".equalsIgnoreCase(displayGroup.getEditFlag())) {
							nextOrdinals.put(itemId, 1);
						} else {
							if (nextOrdinals.containsKey(itemId)) {
								int max = nextOrdinals.get(itemId);
								nextOrdinals.put(itemId, ordinal > max ? ordinal + 1 : max);
							} else {
								nextOrdinals.put(itemId, ordinal + 1);
							}
						}
						editFlags.put(displayItem.getData().getId(), displayGroup.getEditFlag());
					}
				}

				List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
				int nextOrdinal = 0;
				for (int j = 0; j < dgbs.size(); j++) {
					DisplayItemGroupBean displayGroup = dgbs.get(j);
					List<DisplayItemBean> oItems = displayGroup.getItems();
					String editFlag = displayGroup.getEditFlag();
					for (DisplayItemBean displayItem : oItems) {
						int itemId = displayItem.getItem().getId();
						nextOrdinal = nextOrdinals.get(itemId);
						int ordinal = 0;
						String editflag = "add".equalsIgnoreCase(editFlag) ? editFlag : editFlags.get(displayItem
								.getData().getId());
						if (editflag.length() > 0) {
							if ("add".equalsIgnoreCase(editflag)) {
								ordinal = nextOrdinals.get(itemId);
								displayItem.getData().setOrdinal(ordinal);
								nextOrdinals.put(itemId, nextOrdinal + 1);
							} else if ("edit".equalsIgnoreCase(editflag)) {
								displayItem.getData().setOrdinal(displayItem.getDbData().getOrdinal());
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Customized validation for item input
	 * 
	 * @param v
	 * @param dib
	 * @param inputName
	 */
	private void customValidation(DiscrepancyValidator v, DisplayItemBean dib, String inputName) {
		String customValidationString = dib.getMetadata().getRegexp();
		if (!StringUtil.isBlank(customValidationString)) {
			Validation customValidation = null;

			if (customValidationString.startsWith("func:")) {
				try {
					customValidation = Validator.processCRFValidationFunction(customValidationString);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (customValidationString.startsWith("regexp:")) {
				try {
					customValidation = Validator.processCRFValidationRegex(customValidationString);
				} catch (Exception e) {
				}
			}

			if (customValidation != null) {
				customValidation.setErrorMessage(dib.getMetadata().getRegexpErrorMsg());
				v.addValidation(inputName, customValidation);
			}
		}
	}

	private String ifValueIsDate(ItemBean itemBean, String value, boolean dryRun) {

		String dateFormat = ResourceBundleProvider.getFormatBundle().getString("date_format_string");
		String dateRegexp = ResourceBundleProvider.getFormatBundle().getString("date_regexp");

		String theFinalValue = value;
		if (value != null && itemBean.getDataType() == ItemDataType.DATE && dryRun) {
			theFinalValue = ExpressionTreeHelper.ifValidDateFormatAsyyyyMMdd(value, dateFormat, dateRegexp);
		} else {
			theFinalValue = ExpressionTreeHelper.isValidDateMMddyyyy(value);
		}
		return theFinalValue;
	}

	/**
	 * This method will populate grouped and variableAndValue HashMaps grouped : Used to correctly populate group
	 * ordinals variableAndValue : Holds itemOID , value (in Form ) pairs passed in to rule processor
	 * 
	 * @param allItems
	 */
	private Container populateRuleSpecificHashMaps(List<DisplayItemWithGroupBean> allItems, Container c, Boolean dryRun) {

		for (DisplayItemWithGroupBean displayItemWithGroupBean : allItems) {
			if (displayItemWithGroupBean.getSingleItem() != null) {
				if (displayItemWithGroupBean.getSingleItem().getItem().getOid() != null) {
					c.grouped.put(displayItemWithGroupBean.getSingleItem().getItem().getOid(), 1);
					c.variableAndValue.put(
							displayItemWithGroupBean.getSingleItem().getItem().getOid(),
							ifValueIsDate(displayItemWithGroupBean.getSingleItem().getItem(), displayItemWithGroupBean
									.getSingleItem().getData().getValue(), dryRun));
					logger.debug("Type : " + displayItemWithGroupBean.getSingleItem().getItem().getItemDataTypeId());
					for (Object displayItemBean : displayItemWithGroupBean.getSingleItem().getChildren()) {
						String oid = ((DisplayItemBean) displayItemBean).getItem().getOid();
						String value = ifValueIsDate(((DisplayItemBean) displayItemBean).getItem(),
								((DisplayItemBean) displayItemBean).getData().getValue(), dryRun);
						logger.debug("Type : " + ((DisplayItemBean) displayItemBean).getItem().getItemDataTypeId());
						c.grouped.put(oid, 1);
						c.variableAndValue.put(oid, value);

					}
				}
				logger.debug("Item Name : {} , Item Value : {} , Item Data Ordinal : {} , Item OID : {} ",
						new Object[] { displayItemWithGroupBean.getSingleItem().getItem().getName(),
								displayItemWithGroupBean.getSingleItem().getData().getValue(),
								displayItemWithGroupBean.getSingleItem().getData().getOrdinal(),
								displayItemWithGroupBean.getSingleItem().getItem().getOid() });
			}
			// Items in repeating groups
			for (DisplayItemGroupBean itemGroupBean : displayItemWithGroupBean.getItemGroups()) {
				logger.debug("Item Group Name : {} , Item Group OID : {} , Ordinal : {} ", new Object[] {
						itemGroupBean.getItemGroupBean().getName(), itemGroupBean.getItemGroupBean().getOid(),
						itemGroupBean.getIndex() });
				for (DisplayItemBean displayItemBean : itemGroupBean.getItems()) {
					String key1 = itemGroupBean.getItemGroupBean().getOid() + "[" + (itemGroupBean.getIndex() + 1)
							+ "]." + displayItemBean.getItem().getOid();
					String key = itemGroupBean.getItemGroupBean().getOid() + "." + displayItemBean.getItem().getOid();
					c.variableAndValue.put(key1,
							ifValueIsDate(displayItemBean.getItem(), displayItemBean.getData().getValue(), dryRun));
					if (c.grouped.containsKey(key)) {
						c.grouped.put(key, c.grouped.get(key) + 1);
					} else {
						c.grouped.put(key, 1);
					}
					logger.debug("Item Name : {} , Item Value : {} , Item OID : {} ", new Object[] {
							displayItemBean.getItem().getName(), displayItemBean.getData().getValue(),
							displayItemBean.getItem().getOid() });
				}
			}
		}
		if (logger.isDebugEnabled()) {
			for (String key : c.grouped.keySet()) {
				logger.debug("key : {} , value : {}", key, c.grouped.get(key));
			}
			for (String key : c.variableAndValue.keySet()) {
				logger.debug("key : {} , value : {}", key, c.variableAndValue.get(key));
			}
		}
		return c;
	}

	private List<RuleSetBean> createAndInitializeRuleSet(StudyBean currentStudy,
			StudyEventDefinitionBean studyEventDefinition, CRFVersionBean crfVersionBean,
			StudyEventBean studyEventBean, EventCRFBean eventCrfBean, Boolean shouldRunRules,
			HttpServletRequest request, HttpServletResponse response, List<ItemBean> itemBeansWithSCDShown) {
		if (shouldRunRules) {
			logMe("Current Thread:::" + Thread.currentThread());
			List<RuleSetBean> ruleSets = getRuleSetService(request).getRuleSetsByCrfStudyAndStudyEventDefinition(
					currentStudy, studyEventDefinition, crfVersionBean);
			logMe("Current Thread:::" + Thread.currentThread() + "RuleSet Now?" + ruleSets);
			if (ruleSets != null && ruleSets.size() > 0) {

				ruleSets = getRuleSetService(request).filterByStatusEqualsAvailable(ruleSets);
				ruleSets = getRuleSetService(request).filterRuleSetsByStudyEventOrdinal(ruleSets, studyEventBean,
						crfVersionBean, studyEventDefinition);
				ruleSets = getRuleSetService(request).filterRuleSetsByHiddenItems(ruleSets, eventCrfBean,
						crfVersionBean, itemBeansWithSCDShown);
			}
			return ruleSets != null && ruleSets.size() > 0 ? ruleSets : new ArrayList<RuleSetBean>();
		} else
			return new ArrayList<RuleSetBean>();
	}

	private HashMap<String, ArrayList<String>> runRules(List<DisplayItemWithGroupBean> allItems,
			List<RuleSetBean> ruleSets, Boolean dryRun, Boolean shouldRunRules, MessageType mt, Phase phase,
			EventCRFBean ecb, HttpServletRequest request) {
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
		StudyBean currentStudy = (StudyBean) request.getSession().getAttribute("study");
		if (shouldRunRules) {
			Container c = new Container();
			try {
				c = populateRuleSpecificHashMaps(allItems, c, dryRun);
				ruleSets = getRuleSetService(request).filterRuleSetsBySectionAndGroupOrdinal(ruleSets, c.grouped);
				ruleSets = getRuleSetService(request).solidifyGroupOrdinalsUsingFormProperties(ruleSets, c.grouped);
				// next line here ?
			} catch (NullPointerException npe) {
				logger.debug("found NPE " + npe.getMessage());
				npe.printStackTrace();
			}
			logger.debug("running rules ... rule sets size is " + ruleSets.size());
			return getRuleSetService(request).runRulesInDataEntry(ruleSets, dryRun, currentStudy, ub,
					c.variableAndValue, phase, ecb, request).getByMessageType(mt);
		} else {
			return new HashMap<String, ArrayList<String>>();
		}

	}

	protected abstract boolean shouldRunRules();

	protected abstract boolean isAdministrativeEditing();

	protected abstract boolean isAdminForcedReasonForChange(HttpServletRequest request);

	private RuleSetServiceInterface getRuleSetService(HttpServletRequest request) {

		RuleSetServiceInterface ruleSetService = null;

		ruleSetService = ruleSetService != null ? ruleSetService : (RuleSetServiceInterface) SpringServletAccess
				.getApplicationContext(getServletContext()).getBean("ruleSetService");
		ruleSetService.setContextPath(getContextPath(request));
		ruleSetService.setMailSender((JavaMailSenderImpl) SpringServletAccess
				.getApplicationContext(getServletContext()).getBean("mailSender"));
		ruleSetService.setRequestURLMinusServletPath(getRequestURLMinusServletPath(request));
		return ruleSetService;
	}

	private void ensureSelectedOption(DisplayItemBean displayItemBean) {
		if (displayItemBean == null || displayItemBean.getData() == null) {
			return;
		}
		ItemDataBean itemDataBean = displayItemBean.getData();
		String dataValue = itemDataBean.getValue();
		if ("".equalsIgnoreCase(dataValue)) {
			return;
		}

		List<ResponseOptionBean> responseOptionBeans = new ArrayList<ResponseOptionBean>();
		ResponseSetBean responseSetBean = displayItemBean.getMetadata().getResponseSet();
		if (responseSetBean == null) {
			return;
		}
		responseOptionBeans = responseSetBean.getOptions();
		String tempVal = "";
		for (ResponseOptionBean responseOptionBean : responseOptionBeans) {
			tempVal = responseOptionBean.getValue();
			if (tempVal != null && tempVal.equalsIgnoreCase(dataValue)) {
				responseOptionBean.setSelected(true);
			}
		}
	}

	protected boolean unloadFiles(HashMap<String, String> newUploadedFiles) {
		boolean success = true;
		Iterator iter = newUploadedFiles.keySet().iterator();
		while (iter.hasNext()) {
			String itemId = (String) iter.next();
			String filename = newUploadedFiles.get(itemId);
			File f = new File(filename);
			if (f.exists()) {
				if (f.delete()) {
					newUploadedFiles.remove("filename");
				} else {
					success = false;
				}
			} else {
				newUploadedFiles.remove("filename");
			}
		}
		return success;
	}

	class Container {
		HashMap<String, Integer> grouped;
		HashMap<String, String> variableAndValue;

		public Container() {
			super();
			this.grouped = new HashMap<String, Integer>();
			this.variableAndValue = new HashMap<String, String>();
		}
	}

	public int getManualRows(List<DisplayItemGroupBean> formGroups) {
		int manualRows = 0;
		for (int j = 0; j < formGroups.size(); j++) {
			DisplayItemGroupBean formItemGroup = formGroups.get(j);
			logger.debug("begin formGroup Ordinal:" + formItemGroup.getOrdinal());

			if (formItemGroup.isAuto() == false) {
				manualRows = manualRows + 1;
			}

		}
		logger.debug("+++ returning manual rows: " + manualRows + " from a form group size of " + formGroups.size());
		return manualRows;
	}

	private HashMap reshuffleErrorGroupNamesKK(HashMap errors, List<DisplayItemWithGroupBean> allItems,
			HttpServletRequest request) {
		int manualRows = 0;
		if (errors != null && errors.size() > 0) {
			for (int i = 0; i < allItems.size(); i++) {
				DisplayItemWithGroupBean diwb = allItems.get(i);

				if (diwb.isInGroup()) {
					List<DisplayItemGroupBean> dgbs = diwb.getItemGroups();
					for (int j = 0; j < dgbs.size(); j++) {
						DisplayItemGroupBean digb = dgbs.get(j);

						List<DisplayItemBean> dibs = digb.getItems();

						if (j == 0) { // first repeat
							for (DisplayItemBean dib : dibs) {
								String intendedKey = digb.getInputId() + getInputName(dib);
								String replacementKey = digb.getItemGroupBean().getOid() + "_" + j + getInputName(dib);
								if (!replacementKey.equals(intendedKey) && errors.containsKey(intendedKey)) {
									errors.put(replacementKey, errors.get(intendedKey));
									errors.remove(intendedKey);
									logger.debug("removing: " + intendedKey + " and replacing it with "
											+ replacementKey);
								}
							}
						} else { // everything
							manualRows++;
							for (DisplayItemBean dib : dibs) {
								String intendedKey = digb.getInputId() + getInputName(dib);
								String replacementKey = digb.getItemGroupBean().getOid() + "_manual"
										+ digb.getOrdinal() + getInputName(dib);
								if (!replacementKey.equals(intendedKey) && errors.containsKey(intendedKey)) {
									errors.put(replacementKey, errors.get(intendedKey));
									errors.remove(intendedKey);
									logger.debug("removing: " + intendedKey + " and replacing it with "
											+ replacementKey);
								}
							}
						}
					}
				}
			}
		}
		request.setAttribute("manualRows", new Integer(manualRows));
		return errors;
	}

	/* Determining the resolution status that will be shown in color flag for an item. */
	public static int getDiscrepancyNoteResolutionStatus(DiscrepancyNoteDAO dndao, int itemDataId, ArrayList formNotes) {
		int resolutionStatus = 0;
		boolean hasOtherThread = false;
		ArrayList existingNotes = dndao.findExistingNotesForItemData(itemDataId);
		for (Object obj : existingNotes) {
			DiscrepancyNoteBean note = (DiscrepancyNoteBean) obj;
			/*
			 * We would only take the resolution status of the parent note of any note thread. If there are more than
			 * one note thread, the thread with the worst resolution status will be taken.
			 */
			if (note.getParentDnId() == 0) {
				if (hasOtherThread) {
					if (resolutionStatus > note.getResolutionStatusId()) {
						resolutionStatus = note.getResolutionStatusId();
					}
				} else {
					resolutionStatus = note.getResolutionStatusId();
				}
				hasOtherThread = true;
			}
		}

		if (formNotes == null || formNotes.isEmpty()) {
			return resolutionStatus;
		}

		for (Object obj : formNotes) {
			DiscrepancyNoteBean note = (DiscrepancyNoteBean) obj;
			if (note.getParentDnId() == 0) {
				if (hasOtherThread) {
					if (resolutionStatus > note.getResolutionStatusId()) {
						resolutionStatus = note.getResolutionStatusId();
					}
				} else {
					resolutionStatus = note.getResolutionStatusId();
				}
				hasOtherThread = true;
			}
		}

		return resolutionStatus;
	}

	/*
	 * Update DisplaySectionBean's firstSection & lastSection;
	 */
	protected void updateDisplaySectionPlace(DisplaySectionBean displaySectionBean, DisplayTableOfContentsBean toc,
			HttpServletRequest request) {
		if (toc != null) {
			ArrayList<SectionBean> sectionBeans = toc.getSections();
			if (sectionBeans != null && sectionBeans.size() > 0) {
				int sid = displaySectionBean.getSection().getId();
				displaySectionBean.setFirstSection(sid == sectionBeans.get(0).getId() ? true : false);
				displaySectionBean.setLastSection(sid == sectionBeans.get(sectionBeans.size() - 1).getId() ? true
						: false);
			}
		}
	}

	protected SectionBean prevSection(SectionBean sb, EventCRFBean ecb, DisplayTableOfContentsBean toc, int sbPos) {
		SectionBean p = new SectionBean();
		ArrayList<SectionBean> sectionBeans = new ArrayList<SectionBean>();
		if (toc != null) {
			sectionBeans = toc.getSections();
			if (sbPos > 0) {
				p = sectionBeans.get(sbPos - 1);
			}
		}
		return p != null && p.getId() > 0 ? p : new SectionBean();
	}

	protected SectionBean nextSection(SectionBean sb, EventCRFBean ecb, DisplayTableOfContentsBean toc, int sbPos) {
		SectionBean n = new SectionBean();
		ArrayList<SectionBean> sectionBeans = new ArrayList<SectionBean>();
		if (toc != null) {
			sectionBeans = toc.getSections();
			int size = sectionBeans.size();
			if (sbPos >= 0 && size > 1 && sbPos < size - 1) {
				n = sectionBeans.get(sbPos + 1);
			}
		}
		return n != null && n.getId() > 0 ? n : new SectionBean();
	}

	public void mayAccess(HttpServletRequest request) throws InsufficientPermissionException {
		FormProcessor fp = new FormProcessor(request);
		EventCRFDAO edao = new EventCRFDAO(getDataSource());
		UserAccountBean ub = (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
		int eventCRFId = fp.getInt("ecId", true);
		if (eventCRFId == 0) {
			eventCRFId = fp.getInt("eventCRFId", true);
		}

		if (eventCRFId > 0) {
			if (!entityIncluded(eventCRFId, ub.getName(), edao, getDataSource())) {
				addPageMessage(respage.getString("required_event_CRF_belong"), request);
				throw new InsufficientPermissionException(Page.MENU_SERVLET,
						resexception.getString("entity_not_belong_studies"), "1");
			}
		}

	}

	protected void populateInstantOnChange(HttpSession session, EventCRFBean ecb, DisplaySectionBean section) {
		int cvId = ecb.getCRFVersionId();
		int sectionId = section.getSection().getId();
		InstantOnChangeService ins = (InstantOnChangeService) SpringServletAccess.getApplicationContext(
				getServletContext()).getBean("instantOnChangeService");
		InstantOnChangeFrontStrParcel strsInSec = new InstantOnChangeFrontStrParcel();
		HashMap<Integer, InstantOnChangeFrontStrGroup> nonRepOri = null;
		HashMap<String, Map<Integer, InstantOnChangeFrontStrGroup>> grpOri = null;
		HashMap<Integer, InstantOnChangeFrontStrParcel> instantOnChangeFrontStrParcels = (HashMap<Integer, InstantOnChangeFrontStrParcel>) session
				.getAttribute(CV_INSTANT_META + cvId);
		if (instantOnChangeFrontStrParcels != null && instantOnChangeFrontStrParcels.containsKey(sectionId)) {
			strsInSec = instantOnChangeFrontStrParcels.get(sectionId);
			nonRepOri = (HashMap<Integer, InstantOnChangeFrontStrGroup>) strsInSec.getNonRepOrigins();
			grpOri = (HashMap<String, Map<Integer, InstantOnChangeFrontStrGroup>>) strsInSec.getRepOrigins();
		} else if (instantOnChangeFrontStrParcels == null || instantOnChangeFrontStrParcels.size() == 0) {
			instantOnChangeFrontStrParcels = (HashMap<Integer, InstantOnChangeFrontStrParcel>) ins
					.instantOnChangeFrontStrParcelInCrfVersion(cvId);
			if (instantOnChangeFrontStrParcels.size() > 0) {
				session.setAttribute(CV_INSTANT_META + cvId, instantOnChangeFrontStrParcels);
				if (instantOnChangeFrontStrParcels.containsKey(sectionId)) {
					strsInSec = instantOnChangeFrontStrParcels.get(sectionId);
					if (strsInSec != null) {
						grpOri = (HashMap<String, Map<Integer, InstantOnChangeFrontStrGroup>>) strsInSec
								.getRepOrigins();
						nonRepOri = (HashMap<Integer, InstantOnChangeFrontStrGroup>) strsInSec.getNonRepOrigins();
					}
				}
			}
		}
		if (grpOri != null && grpOri.size() > 0) {
			ins.itemGroupsInstantUpdate(section.getDisplayItemGroups(), grpOri);
		}
		if (nonRepOri != null && nonRepOri.size() > 0) {
			ins.itemsInstantUpdate(section.getDisplayItemGroups(), nonRepOri);
		}
	}

	private StdScheduler getScheduler(HttpServletRequest request) {
		StdScheduler scheduler = (StdScheduler) SpringServletAccess.getApplicationContext(request.getSession()
				.getServletContext()).getBean("schedulerFactoryBean");
		return scheduler;
	}

    private static boolean checkDobleDataEntryErrors(HashMap errors) {
        Iterator iter = errors.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry pairs = (Map.Entry) iter.next();
            if (pairs.getValue().toString().contains(respage.getString("value_you_specified"))) {
                return true;
            }
        }
        return false;
    }
    
	protected CodedItemService getCodedItemService() {

		if (codedItemService == null) {
			codedItemService = SpringServletAccess.getApplicationContext(getServletContext()).getBean(
					CodedItemService.class);
		}

		return codedItemService;
	}
	
	private Map<String, HashMap<String, String>> createDNParametersMap(HttpServletRequest request, DisplaySectionBean section) {
		//we create map with parameters for creating DNs for each field in CRF
		Map<String, HashMap<String, String>> dnCreatingParameters = new HashMap<String, HashMap<String, String>>();
		for (int i = 0; i < section.getDisplayItemGroups().size(); i++) {
			DisplayItemWithGroupBean diwgb = (DisplayItemWithGroupBean) section.getDisplayItemGroups().get(i);
			String inputName;
			if (diwgb.isInGroup()) {
				List<DisplayItemGroupBean> dbGroups = diwgb.getDbItemGroups();
				for (int j = 0; j < dbGroups.size(); j++) {
					DisplayItemGroupBean displayGroup = dbGroups.get(j);
					for (DisplayItemBean displayItem : displayGroup.getItems()) {
						// DisplayItemBean from repeating Group
						if (j == 0) {
							inputName = getGroupItemInputName(displayGroup, j, displayItem);
						} else {
							inputName = getGroupItemManualInputName(displayGroup, j, displayItem);
						}						
						dnCreatingParameters.put(inputName, calculateDNParametersForOneItem(displayItem, inputName, request));
					}
				}
			} else {
				DisplayItemBean displayItem = diwgb.getSingleItem();
				inputName = getInputName(displayItem);
				dnCreatingParameters.put(inputName, calculateDNParametersForOneItem(displayItem, inputName, request));
			}
				
		}
		return dnCreatingParameters;
	}
	
	private HashMap<String, String> calculateDNParametersForOneItem(
			DisplayItemBean dib, String field, HttpServletRequest request) {
		HashMap<String, String> result = new HashMap<String, String>();
		// calculate parameters block
		
		String isInError = "0";
		HashMap formMessages = (HashMap) request.getAttribute("formMessages");
		if (formMessages != null) {
			if (formMessages.keySet().contains(field)) {
				isInError = "1";
			}
		}
		
		String isInRFCError = "0";
		Set<String> setOfItemNamesWithRFCErrors = (Set<String>) request.getAttribute("setOfItemNamesWithRFCErrors");
		if (setOfItemNamesWithRFCErrors != null) {
			if (setOfItemNamesWithRFCErrors.contains(field)) {
				isInRFCError = "1";
			}
		}
		
		String isInFVCError = "0";
		if ("1".equals(isInError) && "0".equals(isInRFCError)) {
			isInFVCError = "1";
		}
		
		result.put("isInError", isInError);
		result.put("isInRFCError", isInRFCError);
		result.put("isInFVCError", isInFVCError);
		result.put("isDataChanged", "0");
		result.put("field", field);
		
		return result;
	}
	
}
