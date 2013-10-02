package org.akaza.openclinica.control.core;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.core.SecurityManager;
import org.akaza.openclinica.core.SessionManager;
import org.akaza.openclinica.dao.admin.AuditDAO;
import org.akaza.openclinica.dao.admin.AuditEventDAO;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.discrepancy.DnDescriptionDao;
import org.akaza.openclinica.dao.dynamicevent.DynamicEventDao;
import org.akaza.openclinica.dao.extract.ArchivedDatasetFileDAO;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.akaza.openclinica.dao.hibernate.MeasurementUnitDao;
import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.hibernate.UsageStatsServiceDAO;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.dao.submit.SubjectDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.job.OpenClinicaSchedulerFactoryBean;
import org.akaza.openclinica.service.crfdata.DynamicsMetadataService;
import org.akaza.openclinica.service.rule.RuleSetService;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.akaza.openclinica.view.StudyInfoPanel;
import org.akaza.openclinica.web.filter.OpenClinicaJdbcService;
import org.akaza.openclinica.web.table.sdv.SDVUtil;
import org.quartz.impl.StdScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.ServletContextAware;

@SuppressWarnings("serial")
public abstract class BaseController extends HttpServlet implements HttpRequestHandler, ServletContextAware {

	public static final String STUDY = "study";
	public static final String USER_ROLE = "userRole";
	public static final String USER_BEAN_NAME = "userBean";
	public static final String ERRORS_HOLDER = "errors_holder";
	public static final String SESSION_MANAGER = "sessionManager";

	public static final String BR = "<br/>";
	public static final String STUDY_SHOUD_BE_IN_AVAILABLE_MODE = "studyShoudBeInAvailableMode";

	public static final String STORED_ATTRIBUTES = "RememberLastPage_storedAttributes";
	public static final String SCHEDULER = "schedulerFactoryBean";
	public static final String JOB_HOUR = "jobHour";
	public static final String JOB_MINUTE = "jobMinute";

	public static final String PAGE_MESSAGE = "pageMessages";// for showing
	// page
	// wide message

	public static final String INPUT_MESSAGES = "formMessages"; // for showing
	// input-specific
	// messages

	public static final String PRESET_VALUES = "presetValues"; // for setting
	// preset values

	public static final String ADMIN_SERVLET_CODE = "admin";

	public static final String BEAN_TABLE = "table";

	public static final String STUDY_INFO_PANEL = "panel"; // for setting the
	// side panel

	public static final String BREADCRUMB_TRAIL = "breadcrumbs";

	public static final String POP_UP_URL = "popUpURL";

	// Use this variable as the key for the support url
	public static final String SUPPORT_URL = "supportURL";

	public static final String MODULE = "module";// to determine which module

	public static final String NOT_USED = "not_used";

	protected static ResourceBundle resadmin, resaudit, resexception, resformat, respage, resterm, restext, resword,
			resworkflow;

    private ServletContext servletContext;

	@Autowired
	private DataSource dataSource;
	@Autowired
	private AuthoritiesDao authoritiesDao;
	@Autowired
	private UsageStatsServiceDAO usageStatsServiceDAO;
	@Autowired
	private DatabaseChangeLogDao databaseChangeLogDao;
	@Autowired
	private AuditUserLoginDao auditUserLoginDao;
	@Autowired
	private ConfigurationDao configurationDao;
	@Autowired
	private MeasurementUnitDao measurementUnitDao;
	@Autowired
	private RuleSetDao ruleSetDao;
	@Autowired
	private RuleSetRuleDao ruleSetRuleDao;
	@Autowired
	private RuleSetAuditDao ruleSetAuditDao;
	@Autowired
	private RuleSetRuleAuditDao ruleSetRuleAuditDao;
	@Autowired
	private RuleSetService ruleSetService;
    @Autowired
    private StudyConfigService studyConfigService;    
	@Autowired
	private RulesPostImportContainerService rulesPostImportContainerService;
	@Autowired
	private DynamicsMetadataService dynamicsMetadataService;
	@Autowired
	private OpenClinicaJdbcService openClinicaJdbcService;
	@Autowired
	private OpenClinicaSchedulerFactoryBean scheduler;
	@Autowired
	private SDVUtil sdvUtil;   
    @Autowired
    private CoreResources coreResources;
    @Autowired
    private SecurityManager securityManager;
    @Autowired
    private JavaMailSenderImpl mailSender;

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

	public SimpleDateFormat getLocalDf(HttpServletRequest request) {
		return new SimpleDateFormat(resformat.getString("date_format_string"), request.getLocale());
	}

	public SessionManager getSessionManager(HttpServletRequest request) {
		return (SessionManager) request.getAttribute(SESSION_MANAGER);
	}

	@SuppressWarnings("rawtypes")
	public HashMap getErrorsHolder(HttpServletRequest request) {
		HashMap errors = (HashMap) request.getAttribute(ERRORS_HOLDER);
		if (errors == null) {
			errors = new HashMap();
			request.setAttribute(ERRORS_HOLDER, errors);
		}
		return errors;
	}

	public StudyInfoPanel getStudyInfoPanel(HttpServletRequest request) {
		StudyInfoPanel panel = (StudyInfoPanel) request.getSession().getAttribute(STUDY_INFO_PANEL);
		if (panel == null) {
			panel = new StudyInfoPanel();
			request.getSession().setAttribute(STUDY_INFO_PANEL, panel);
			request.setAttribute(STUDY_INFO_PANEL, panel);
		}
		return panel;
	}

	public StudyBean getCurrentStudy(HttpServletRequest request) {
		return (StudyBean) request.getSession().getAttribute(STUDY);
	}

	public UserAccountBean getUserAccountBean(HttpServletRequest request) {
		return (UserAccountBean) request.getSession().getAttribute(USER_BEAN_NAME);
	}

	public StudyUserRoleBean getCurrentRole(HttpServletRequest request) {
		return (StudyUserRoleBean) request.getSession().getAttribute(USER_ROLE);
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public StdScheduler getStdScheduler() {
		return (StdScheduler) scheduler.getScheduler();
	}

	public StudyConfigService getStudyConfigService() {
		return studyConfigService;
	}

	public SDVUtil getSDVUtil() {
		return sdvUtil;
	}

	public RuleSetService getRuleSetService() {
		return ruleSetService;
	}

	public CoreResources getCoreResources() {
		return coreResources;
	}

	public RulesPostImportContainerService getRulesPostImportContainerService() {
		return rulesPostImportContainerService;
	}

	public DynamicsMetadataService getDynamicsMetadataService() {
		return dynamicsMetadataService;
	}

	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	public JavaMailSenderImpl getMailSender() {
		return mailSender;
	}

	public OpenClinicaJdbcService getOpenClinicaJdbcService() {
		return openClinicaJdbcService;
	}

	public MeasurementUnitDao getMeasurementUnitDao() {
		return measurementUnitDao;
	}

	public RuleSetRuleDao getRuleSetRuleDao() {
		return ruleSetRuleDao;
	}

	public RuleSetRuleAuditDao getRuleSetRuleAuditDao() {
		return ruleSetRuleAuditDao;
	}

	public RuleSetDao getRuleSetDao() {
		return ruleSetDao;
	}

	public RuleSetAuditDao getRuleSetAuditDao() {
		return ruleSetAuditDao;
	}

	@SuppressWarnings("rawtypes")
	public ItemGroupDAO getItemGroupDAO() {
		return new ItemGroupDAO(getDataSource());
	}

	public AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}

	public DatabaseChangeLogDao getDatabaseChangeLogDao() {
		return databaseChangeLogDao;
	}

	public ConfigurationDao getConfigurationDao() {
		return configurationDao;
	}

	public AuditUserLoginDao getAuditUserLoginDao() {
		return auditUserLoginDao;
	}

	public UsageStatsServiceDAO getUsageStatsServiceDAO() {
		return usageStatsServiceDAO;
	}

	public SubjectDAO getSubjectDAO() {
		return new SubjectDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public StudySubjectDAO getStudySubjectDAO() {
		return new StudySubjectDAO(getDataSource());
	}

	public StudyGroupClassDAO getStudyGroupClassDAO() {
		return new StudyGroupClassDAO(getDataSource());
	}

	public SubjectGroupMapDAO getSubjectGroupMapDAO() {
		return new SubjectGroupMapDAO(getDataSource());
	}

	public StudyEventDAO getStudyEventDAO() {
		return new StudyEventDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public StudyDAO getStudyDAO() {
		return new StudyDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public EventCRFDAO getEventCRFDAO() {
		return new EventCRFDAO(getDataSource());
	}

	public EventDefinitionCRFDAO getEventDefinitionCRFDAO() {
		return new EventDefinitionCRFDAO(getDataSource());
	}

	public DiscrepancyNoteDAO getDiscrepancyNoteDAO() {
		return new DiscrepancyNoteDAO(getDataSource());
	}

	public StudyGroupDAO getStudyGroupDAO() {
		return new StudyGroupDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public DynamicEventDao getDynamicEventDao() {
		return new DynamicEventDao(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public StudyEventDefinitionDAO getStudyEventDefinitionDAO() {
		return new StudyEventDefinitionDAO(getDataSource());
	}

	public UserAccountDAO getUserAccountDAO() {
		return new UserAccountDAO(getDataSource());
	}

	public StudyParameterValueDAO getStudyParameterValueDAO() {
		return new StudyParameterValueDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public CRFVersionDAO getCRFVersionDAO() {
		return new CRFVersionDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public CRFDAO getCRFDAO() {
		return new CRFDAO(getDataSource());
	}

	public ArchivedDatasetFileDAO getArchivedDatasetFileDAO() {
		return new ArchivedDatasetFileDAO(getDataSource());
	}

	public DatasetDAO getDatasetDAO() {
		return new DatasetDAO(getDataSource());
	}

	public ItemDataDAO getItemDataDAO() {
		return new ItemDataDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public ItemDAO getItemDAO() {
		return new ItemDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public ItemFormMetadataDAO getItemFormMetadataDAO() {
		return new ItemFormMetadataDAO(getDataSource());
	}

	public AuditDAO getAuditDAO() {
		return new AuditDAO(getDataSource());
	}

	public DnDescriptionDao getDnDescriptionDao() {
		return new DnDescriptionDao(getDataSource());
	}

	public SectionDAO getSectionDAO() {
		return new SectionDAO(getDataSource());
	}

	public AuditEventDAO getAuditEventDAO() {
		return new AuditEventDAO(getDataSource());
	}

	@SuppressWarnings("rawtypes")
	public ItemGroupMetadataDAO getItemGroupMetadataDAO() {
		return new ItemGroupMetadataDAO(getDataSource());
	}
}