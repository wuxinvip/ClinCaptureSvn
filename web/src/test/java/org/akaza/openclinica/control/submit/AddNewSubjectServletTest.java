package org.akaza.openclinica.control.submit;

import com.clinovo.util.SessionUtil;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.web.InsufficientPermissionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import javax.sql.DataSource;
import java.util.Locale;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResourceBundleProvider.class)
public class AddNewSubjectServletTest {

	@Mock
	private AddNewSubjectServlet addNewSubjectServlet;
	@Mock
	private MockHttpServletRequest request;
	@Mock
	private MockHttpServletResponse response;
	@Mock
	private DataSource dataSource;
	@Mock
	private UserAccountBean userAccountBean;
	@Mock
	private StudyBean studyBean;
	@Mock
	private StudyUserRoleBean studyUserRoleBean;

	@Before
	public void setUp() throws Exception {
		MockHttpSession session = new MockHttpSession();
		PowerMockito.when(request.getSession()).thenReturn(session);
		Locale locale = Locale.ENGLISH;
		SessionUtil.updateLocale(session, locale);
		ResourceBundleProvider.updateLocale(locale);
		Whitebox.setInternalState(addNewSubjectServlet, "restext", ResourceBundleProvider.getTextsBundle());
		PowerMockito.doReturn(userAccountBean).when(addNewSubjectServlet).getUserAccountBean(request);
		PowerMockito.doReturn(studyUserRoleBean).when(addNewSubjectServlet).getCurrentRole(request);
		PowerMockito.doReturn(studyBean).when(addNewSubjectServlet).getCurrentStudy(request);
		PowerMockito.doCallRealMethod().when(addNewSubjectServlet).processRequest(request, response);
	}

	@Test(expected = InsufficientPermissionException.class)
	public void testThatCoderDoesNotHaveAccessToAddNewSubjectPage() throws Exception {
		Role role = Role.STUDY_CODER;
		studyUserRoleBean.setRole(role);
		PowerMockito.doReturn(role).when(studyUserRoleBean).getRole();
		userAccountBean.addUserType(UserType.USER);
		addNewSubjectServlet.processRequest(request, response);
	}

	@Test(expected = InsufficientPermissionException.class)
	public void testThatEvaluatorDoesNotHaveAccessToAddNewSubjectPage() throws Exception {
		Role role = Role.STUDY_EVALUATOR;
		studyUserRoleBean.setRole(role);
		PowerMockito.doReturn(role).when(studyUserRoleBean).getRole();
		userAccountBean.addUserType(UserType.USER);
		addNewSubjectServlet.processRequest(request, response);
	}

}