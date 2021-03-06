package com.clinovo.rest.service;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.akaza.openclinica.DefaultAppContextTest;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.service.StudyParameterValueBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.cdisc.ns.odm.v130.ODM;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.clinovo.rest.filter.RestFilter;
import com.clinovo.rest.odm.RestOdmContainer;

@Ignore
@WebAppConfiguration("/")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:servlet-context.xml")
@SuppressWarnings({"rawtypes", "unchecked"})
public class BaseServiceTest extends DefaultAppContextTest {

	public static final Locale LOCALE = new Locale("en");

	public static final String DEFAULT_CLIENT_VERSION = "1.0";

	// Managed services
	public static final String API_STUDIES = "/studies";
	public static final String API_STUDY_REMOVE = "/study/remove";
	public static final String API_STUDY_RESTORE = "/study/restore";
	public static final String API_STUDY_CREATE = "/study/create";
	public static final String API_STUDY_EDIT = "/study/edit";
	public static final String API_CRF = "/crf";
	public static final String API_CRFS = "/crfs";
	public static final String API_CRF_DELETE = "/crf/delete";
	public static final String API_CRF_REMOVE = "/crf/remove";
	public static final String API_CRF_RESTORE = "/crf/restore";
	public static final String API_CRF_VERSION = "/crfVersion";
	public static final String API_CRF_VERSIONS = "/crfVersions";
	public static final String API_CRF_VERSION_DELETE = "/crfVersion/delete";
	public static final String API_CRF_VERSION_REMOVE = "/crfVersion/remove";
	public static final String API_CRF_VERSION_RESTORE = "/crfVersion/restore";
	public static final String API_CRF_VERSION_LOCK = "/crfVersion/lock";
	public static final String API_CRF_VERSION_UNLOCK = "/crfVersion/unlock";
	public static final String API_CRF_JSON_IMPORT_CRF = "/crf/json/importCrf";
	public static final String API_CRF_JSON_IMPORT_CRF_VERSION = "/crf/json/importCrfVersion";
	public static final String API_EVENT = "/event";
	public static final String API_EVENTS = "/events";
	public static final String API_EVENTS_ORDER = "/events/order";
	public static final String API_EVENT_DELETE = "/event/delete";
	public static final String API_EVENT_DELETE_CRF = "/event/deleteCrf";
	public static final String API_EVENT_REMOVE_CRF = "/event/removeCrf";
	public static final String API_EVENT_RESTORE_CRF = "/event/restoreCrf";
	public static final String API_EVENT_REMOVE = "/event/remove";
	public static final String API_EVENT_RESTORE = "/event/restore";
	public static final String API_EVENT_EDIT = "/event/edit";
	public static final String API_EVENT_CRFS_ORDER = "/event/crfs/order";
	public static final String API_EVENT_ADD_CRF = "/event/addCrf";
	public static final String API_EVENT_EDIT_STUDY_CRF = "/event/editStudyCrf";
	public static final String API_EVENT_EDIT_SITE_CRF = "/event/editSiteCrf";
	public static final String API_EVENT_CREATE = "/event/create";
	public static final String API_WRONG_MAPPING = "/wrongmapping";
	public static final String API_USER = "/user";
	public static final String API_USER_CREATE_USER = "/user/createUser";
	public static final String API_USER_REMOVE = "/user/remove";
	public static final String API_USER_RESTORE = "/user/restore";
	public static final String API_AUTHENTICATION = "/authentication";
	public static final String API_WADL = "/wadl";
	public static final String API_ODM = "/odm";

	private List<CRFBean> crfBeanList;
	private List<StudyBean> studyBeanList;
	private List<CRFVersionBean> crfVersionBeanList;
	private List<UserAccountBean> userAccountBeanList;
	private List<EventDefinitionCRFBean> eventDefinitionCRFBeanList;
	private List<StudyEventDefinitionBean> studyEventDefinitionBeanList;
	private static boolean dbRestored;

	protected Schema schema;

	protected MockMvc mockMvc;

	protected MvcResult result;

	protected boolean revertMailSenderHost;
	protected String mailSenderHost = null;

	protected RestOdmContainer restOdmContainer;

	protected MediaType mediaType = MediaType.APPLICATION_JSON;

	protected long timestamp;
	protected String currentToken;
	protected String rootUserName;
	protected String rootUserPassword;
	protected String defaultStudyName;

	protected StudyBean currentScope;
	protected StudyBean defaultStudy;
	protected UserAccountBean rootUser;

	protected StudyBean newSite;
	protected StudyBean newStudy;
	protected UserAccountBean newUser;

	@Autowired
	protected WebApplicationContext wac;

	@Autowired
	protected org.akaza.openclinica.core.SecurityManager securityManager;

	private void setTestProperties() throws Exception {
		String resource = "rest-test.properties";
		Properties prop = new Properties();
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream stream = loader.getResourceAsStream(resource);
		prop.load(stream);
		rootUserName = prop.getProperty("rest.userName");
		rootUserPassword = prop.getProperty("rest.password");
		defaultStudyName = prop.getProperty("rest.studyName");
	}

	private void updateParameter(StudyParameterValueDAO spvdao, StudyParameterValueBean spv) {
		StudyParameterValueBean spv1 = spvdao.findByHandleAndStudy(spv.getStudyId(), spv.getParameter());
		if (spv1.getId() > 0) {
			spvdao.update(spv);
		} else {
			spvdao.create(spv);
		}
	}

	private void setStudyParameter(String parameterName, String value) {
		StudyParameterValueBean spv = new StudyParameterValueBean();
		spv.setStudyId(currentScope.getId());
		spv.setParameter(parameterName);
		spv.setValue(value);
		updateParameter(studyParameterValueDAO, spv);
	}

	private StudyBean createStudy() throws Exception {
		StudyBean study = new StudyBean();
		study.setName("study_".concat(Long.toString(timestamp)));
		study.setOwner(rootUser);
		study.setCreatedDate(new Date());
		study.setStatus(Status.PENDING);
		studyDAO.create(study);
		return study;
	}

	private StudyBean createSite(int studyId) throws Exception {
		StudyBean site = new StudyBean();
		site.setName("site_".concat(Long.toString(timestamp)));
		site.setParentStudyId(studyId);
		site.setOwner(rootUser);
		site.setCreatedDate(new Date());
		site.setStatus(Status.PENDING);
		return (StudyBean) studyDAO.create(site);
	}

	protected String getSymbols(int size) {
		String result = "";
		for (int i = 1; i <= size; i++) {
			result = result.concat("a");
		}
		return result;
	}

	protected int getMaxId(EntityBean entityBean, int max) {
		return entityBean.getId() > max ? entityBean.getId() : max;
	}

	protected void createNewStudy() throws Exception {
		newStudy = createStudy();
		studyConfigService.setParametersForStudy(newStudy);
		assertTrue(newStudy.getId() > 0);
	}

	protected void createNewSite(int studyId) throws Exception {
		newSite = createSite(studyId);
		studyConfigService.setParametersForSite(newSite);
		assertTrue(newSite.getId() > 0);
	}

	protected EventDefinitionCRFBean createChildEDCForNewSite(int id, StudyBean site) throws Exception {
		EventDefinitionCRFBean eventDefinitionCRFBean = (EventDefinitionCRFBean) eventDefinitionCRFDAO.findByPK(id);
		eventDefinitionCRFBean.setStudyId(site.getId());
		eventDefinitionCRFBean.setParentId(eventDefinitionCRFBean.getId());
		eventDefinitionCRFBean.setSelectedVersionIds(Integer.toString(eventDefinitionCRFBean.getDefaultVersionId()));
		eventDefinitionCRFBean.setId(0);
		eventDefinitionCRFDAO.create(eventDefinitionCRFBean);
		assertTrue(eventDefinitionCRFBean.getId() > 0);
		return eventDefinitionCRFBean;
	}

	protected EventDefinitionCRFBean setStatusForEDC(int id, Status status) {
		EventDefinitionCRFBean eventDefinitionCRFBean = (EventDefinitionCRFBean) eventDefinitionCRFDAO.findByPK(id);
		eventDefinitionCRFBean.setUpdater(rootUser);
		eventDefinitionCRFBean.setStatus(status);
		eventDefinitionCRFBean.setUpdatedDate(new Date());
		eventDefinitionCRFDAO.update(eventDefinitionCRFBean);
		eventDefinitionCRFBean = (EventDefinitionCRFBean) eventDefinitionCRFDAO.findByPK(id);
		assertTrue(eventDefinitionCRFBean.getStatus().equals(status));
		return eventDefinitionCRFBean;
	}

	protected StudyEventDefinitionBean setStatusForSED(int id, Status status) {
		StudyEventDefinitionBean studyEventDefinitionBean = (StudyEventDefinitionBean) studyEventDefinitionDAO
				.findByPK(id);
		studyEventDefinitionBean.setStatus(status);
		studyEventDefinitionBean.setUpdater(rootUser);
		studyEventDefinitionDAO.updateStatus(studyEventDefinitionBean);
		studyEventDefinitionBean = (StudyEventDefinitionBean) studyEventDefinitionDAO.findByPK(id);
		assertEquals(studyEventDefinitionBean.getStatus(), status);
		return studyEventDefinitionBean;
	}

	protected CRFVersionBean setStatusForCrfVersion(int id, Status status) {
		CRFVersionBean crfVersionBean = (CRFVersionBean) crfVersionDao.findByPK(id);
		crfVersionBean.setUpdater(rootUser);
		crfVersionBean.setStatus(status);
		crfVersionDao.update(crfVersionBean);
		crfVersionBean = (CRFVersionBean) crfVersionDao.findByPK(id);
		assertTrue(crfVersionBean.getStatus().equals(status));
		return crfVersionBean;
	}

	private void createNewUser(UserType userType, Role role, String siteName) throws Exception {
		if (role.equals(Role.STUDY_EVALUATOR)) {
			setStudyParameter("studyEvaluator", "yes");
		} else if (role.equals(Role.STUDY_CODER)) {
			setStudyParameter("medicalCoding", "yes");
		}
		String userName = "userName_".concat(Long.toString(timestamp));
		String firstName = "firstName_".concat(Long.toString(timestamp));
		String lastName = "lastName_".concat(Long.toString(timestamp));
		String email = "email@gmail.com";
		String phone = "+375232345678";
		String company = "home";
		MockHttpServletRequestBuilder requestBuilder = post(API_USER_CREATE_USER).param("userName", userName)
				.param("firstName", firstName).param("lastName", lastName).param("email", email).param("phone", phone)
				.param("company", company).param("userType", Integer.toString(userType.getId()))
				.param("allowSoap", "true").param("displayPassword", "true")
				.param("role", Integer.toString(role.getId()));
		if (siteName != null) {
			requestBuilder.param("siteName", siteName);
		}
		MvcResult result = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();
		String password = mediaType.equals(MediaType.APPLICATION_JSON)
				? (String) ((JSONObject) new JSONObject(result.getResponse().getContentAsString()).get("response"))
						.get("password")
				: result.getResponse().getContentAsString().split("<Password>")[1].split("</Password>")[0];
		newUser = (UserAccountBean) userAccountDAO.findByUserName(userName);
		assertTrue(newUser.getId() > 0);
		newUser.setPasswd(password);
	}

	protected void createNewStudyUser(UserType userType, Role role) throws Exception {
		createNewUser(userType, role, null);
	}

	protected void createNewUser(StudyBean studyBean, UserType userType, Role role) throws Exception {
		createNewUser(userType, role, studyBean.isSite() ? studyBean.getName() : null);
	}

	protected void createUserWithoutRole(UserType userType, int studyId) throws Exception {
		String password = securityManager.genPassword();
		UserAccountBean userAccountBean = new UserAccountBean();
		String userName = "john_dong_".concat(Long.toString(timestamp));
		userAccountBean.setName(userName);
		userAccountBean.setFirstName("john");
		userAccountBean.setLastName("dong");
		userAccountBean.setEmail("jd@gmail.com");
		userAccountBean.setPhone("+375232345678");
		userAccountBean.setInstitutionalAffiliation("Clinovo");
		userAccountBean.setActiveStudyId(studyId);
		userAccountBean.setPasswd(securityManager.encryptPassword(password, null));
		userAccountBean.setRunWebservices(false);
		userAccountBean.addUserType(userType);
		userAccountBean.setPasswdTimestamp(null);
		userAccountBean.setLastVisitDate(null);
		userAccountBean.setStatus(Status.AVAILABLE);
		userAccountBean.setPasswdChallengeQuestion("");
		userAccountBean.setPasswdChallengeAnswer("");
		userAccountBean.setOwner(rootUser);
		newUser = (UserAccountBean) userAccountDAO.create(userAccountBean);
		assertTrue(newUser.getId() > 0);
		newUser.setPasswd(password);
	}

	protected void login(String userName, UserType userType, Role role, String password, String studyName)
			throws Exception {
		currentScope = (StudyBean) new StudyDAO(dataSource).findStudyByName(studyName);
		if (currentScope.isSite()) {
			studyConfigService.setParametersForSite(currentScope);
		} else if (!currentScope.isSite()) {
			studyConfigService.setParametersForStudy(currentScope);
		}
		String response = mockMvc
				.perform(post(API_AUTHENTICATION).param("userName", userName).param("password", password)
						.param("studyName", studyName))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		if (mediaType.equals(MediaType.APPLICATION_JSON)) {
			JSONObject jsonObject = new JSONObject(response);
			assertTrue(jsonObject.getJSONObject("response").getString("userName").equals(userName));
			assertTrue(jsonObject.getJSONObject("response").getString("studyName").equals(studyName));
			assertTrue(jsonObject.getJSONObject("response").getString("role").equals(role.getCode()));
			assertTrue(jsonObject.getJSONObject("response").getString("userType").equals(userType.getCode()));
			assertTrue(jsonObject.getJSONObject("response").getString("userStatus")
					.equals(rootUser.getStatus().getName()));
			assertTrue(jsonObject.getJSONObject("response").getString("studyStatus")
					.equals(currentScope.getStatus().getName()));
			currentToken = jsonObject.getJSONObject("response").getString("token");
		} else {
			assertTrue(response.contains("<ODM Description=\"REST Data\""));
			currentToken = response.split("<Token>")[1].split("</Token>")[0];
		}
	}

	private void backupEntities() {
		crfBeanList = crfdao.findAll();
		crfVersionBeanList = crfVersionDao.findAll();
		studyBeanList = (List<StudyBean>) studyDAO.findAll();
		userAccountBeanList = (List<UserAccountBean>) userAccountDAO.findAll();
		eventDefinitionCRFBeanList = (List<EventDefinitionCRFBean>) eventDefinitionCRFDAO.findAll();
		studyEventDefinitionBeanList = (List<StudyEventDefinitionBean>) studyEventDefinitionDAO.findAll();
	}

	private void restoreEntities() {
		int max = 0;
		UserAccountBean updater = (UserAccountBean) userAccountDAO.findByPK(1);
		for (CRFBean crfBean : crfBeanList) {
			max = getMaxId(crfBean, max);
			crfBean.setUpdater(updater);
			crfdao.update(crfBean);
		}
		max = 0;
		for (CRFVersionBean crfVersionBean : crfVersionBeanList) {
			max = getMaxId(crfVersionBean, max);
			crfVersionBean.setUpdater(updater);
			crfVersionDao.update(crfVersionBean);
		}
		max = 0;
		for (EventDefinitionCRFBean eventDefinitionCRFBean : eventDefinitionCRFBeanList) {
			max = getMaxId(eventDefinitionCRFBean, max);
			eventDefinitionCRFBean.setUpdater(updater);
			eventDefinitionCRFDAO.update(eventDefinitionCRFBean);
		}
		eventDefinitionCRFDAO.execute(
				"delete from event_definition_crf where event_definition_crf_id > ".concat(Integer.toString(max)),
				new HashMap());
		max = 0;
		for (StudyEventDefinitionBean studyEventDefinitionBean : studyEventDefinitionBeanList) {
			max = getMaxId(studyEventDefinitionBean, max);
			studyEventDefinitionBean.setUpdater(updater);
			studyEventDefinitionDAO.update(studyEventDefinitionBean);
		}
		studyEventDefinitionDAO.execute(
				"delete from study_event_definition where study_event_definition_id > ".concat(Integer.toString(max)),
				new HashMap());
		max = 0;
		for (UserAccountBean userAccountBean : userAccountBeanList) {
			max = getMaxId(userAccountBean, max);
			userAccountBean.setUpdater(updater);
			userAccountDAO.update(userAccountBean);
		}
		userAccountDAO.execute("delete from authorities where username in ("
				.concat("select user_name from user_account where user_id > ".concat(Integer.toString(max)))
				.concat(")"), new HashMap());
		userAccountDAO.execute("delete from study_user_role where user_name in ("
				.concat("select user_name from user_account where user_id > ".concat(Integer.toString(max)))
				.concat(")"), new HashMap());
		userAccountDAO.execute("delete from user_account where user_id > ".concat(Integer.toString(max)),
				new HashMap());
		max = 0;
		for (StudyBean studyBean : studyBeanList) {
			max = getMaxId(studyBean, max);
			studyBean.setUpdater(updater);
			studyDAO.update(studyBean);
		}
		userAccountDAO.execute("delete from study_user_role where study_id > ".concat(Integer.toString(max)),
				new HashMap());
		studyDAO.execute("delete from study_parameter_value where study_id > ".concat(Integer.toString(max)),
				new HashMap());
		studyDAO.execute("delete from study where study_id > ".concat(Integer.toString(max)), new HashMap());
	}

	@Override
	protected void restoreDb() throws Exception {
		if (!dbRestored) {
			dbRestored = true;
			super.setUp();
		}
	}

	@Before
	public void before() throws Exception {
		backupEntities();

		result = null;
		restOdmContainer = null;
		mailSenderHost = mailSender.getHost();

		schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new FileSystemResourceLoader()
				.getResource("classpath:properties/ClinCapture_Rest_ODM1-3-0.xsd").getURL());

		setTestProperties();

		mockMvc = MockMvcBuilders.webAppContextSetup(wac).addFilters(new RestFilter()).build();

		ResourceBundleProvider.updateLocale(LOCALE);

		timestamp = new Date().getTime();

		defaultStudy = (StudyBean) studyDAO.findStudyByName(defaultStudyName);
		studyConfigService.setParametersForStudy(defaultStudy);

		rootUser = (UserAccountBean) userAccountDAO.findByUserName(rootUserName);

		login(rootUserName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, rootUserPassword, defaultStudyName);
	}

	protected void unmarshalResult() {
		if (result != null && mediaType == MediaType.APPLICATION_XML) {
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(ODM.class, RestOdmContainer.class);
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				jaxbUnmarshaller.setSchema(schema);
				restOdmContainer = (RestOdmContainer) jaxbUnmarshaller
						.unmarshal(new StringReader(result.getResponse().getContentAsString()));
			} catch (Exception ex) {
				logger.error("Error has occurred", ex);
			}
			assertNotNull(restOdmContainer);
		}
	}

	@After
	public void after() {
		restoreEntities();
		if (revertMailSenderHost) {
			mailSender.setHost(mailSenderHost);
		}
		unmarshalResult();
	}

	protected MockHttpServletRequestBuilder get(String url) {
		MockHttpServletRequestBuilder mockHttpServletRequestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.get(url).param("version", DEFAULT_CLIENT_VERSION).secure(true).accept(mediaType);
		if (!url.equals(API_AUTHENTICATION) && !url.equals(API_WADL) && !url.equals(API_ODM)) {
			mockHttpServletRequestBuilder.param("token", currentToken);
		}
		return mockHttpServletRequestBuilder;
	}

	protected MockHttpServletRequestBuilder post(String url) {
		MockHttpServletRequestBuilder mockHttpServletRequestBuilder = org.springframework.test.web.servlet.request.MockMvcRequestBuilders
				.post(url).param("version", DEFAULT_CLIENT_VERSION).secure(true).accept(mediaType);
		if (!url.equals(API_AUTHENTICATION) && !url.equals(API_WADL) && !url.equals(API_ODM)) {
			mockHttpServletRequestBuilder.param("token", currentToken);
		}
		return mockHttpServletRequestBuilder;
	}
}
