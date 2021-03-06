package org.akaza.openclinica.control.managestudy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectGroupMapBean;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.submit.AddNewSubjectServlet;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.akaza.openclinica.dao.submit.SubjectGroupMapDAO;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.akaza.openclinica.view.Page;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockRequestDispatcher;
import org.springframework.mock.web.MockServletContext;

import com.clinovo.i18n.LocaleResolver;

@RunWith(PowerMockRunner.class)
@SuppressWarnings("unused")
public class UpdateStudySubjectServletTest {

	@Mock
	private StudySubjectDAO mockedStudySubjectDAO;
	@Mock
	private DiscrepancyNoteDAO mockedDiscrepancyNoteDAO;
	@Mock
	private SubjectGroupMapDAO mockedSubjectGroupMapDAO;
	@Mock
	private StudyGroupClassDAO mockedStudyGroupClassDAO;
	@Mock
	private StudyParameterValueDAO mockedStudyParameterValueDAO;
	@Mock
	private MockServletContext mockedServletContext;
	@Mock
	private MockRequestDispatcher mockedRequestDispatcher;

	private UpdateStudySubjectServlet spiedUpdateStudySubjectServlet = Mockito.spy(new UpdateStudySubjectServlet());

	private MockHttpServletResponse spiedResponse = Mockito.spy(new MockHttpServletResponse());

	private MockHttpServletRequest request;

	private StudySubjectBean subjectToUpdate;

	private SimpleDateFormat dateFormat;

	private static Locale locale = new Locale("en");

	@Before
	public void beforeTestCaseRun() throws Exception {
		// first obtaining all the required Resource Bundle instances for tests,
		// then stubbing all the static methods of the ResourceBundleProvider class
		ResourceBundleProvider.updateLocale(locale);

		request = new MockHttpServletRequest();
		LocaleResolver.updateLocale(request, locale);

		subjectToUpdate = new StudySubjectBean();

		dateFormat = new SimpleDateFormat(ResourceBundleProvider.getFormatBundle().getString("date_format_string"),
				locale);

		// current system user
		UserAccountBean currentUser = new UserAccountBean();
		currentUser.setId(1);

		// current study
		StudyBean currentStudy = new StudyBean();
		currentStudy.setId(1);

		// current user role
		StudyUserRoleBean currentRole = new StudyUserRoleBean();
		currentRole.setRole(Role.STUDY_ADMINISTRATOR);

		// setting up Servlet Context mock
		Mockito.when(mockedServletContext.getRequestDispatcher(Mockito.any(String.class)))
				.thenReturn(mockedRequestDispatcher);

		// setting up DAO mocks
		StudySubjectBean emptySubjectBean = new StudySubjectBean();
		Mockito.when(mockedStudySubjectDAO.findByPK(Mockito.any(Integer.class))).thenReturn(subjectToUpdate);
		Mockito.when(mockedStudySubjectDAO.findAnotherBySameLabel(Mockito.any(String.class), Mockito.any(Integer.class),
				Mockito.any(Integer.class))).thenReturn(emptySubjectBean);
		Mockito.when(mockedStudySubjectDAO.findAnotherBySameLabelInSites(Mockito.any(String.class),
				Mockito.any(Integer.class), Mockito.any(Integer.class))).thenReturn(emptySubjectBean);
		Mockito.when(mockedDiscrepancyNoteDAO.findAllByEntityAndColumnAndStudy(Mockito.any(StudyBean.class),
				Mockito.any(String.class), Mockito.any(Integer.class), Mockito.any(String.class))).thenReturn(null);
		Mockito.when(mockedSubjectGroupMapDAO.findAllByStudySubject(Mockito.any(Integer.class)))
				.thenReturn(new ArrayList<SubjectGroupMapBean>());

		// setting up spied UpdateStudySubjectServlet
		Mockito.doReturn(mockedServletContext).when(spiedUpdateStudySubjectServlet).getServletContext();
		Mockito.doReturn(currentUser).when(spiedUpdateStudySubjectServlet).getUserAccountBean(request);
		Mockito.doReturn(currentStudy).when(spiedUpdateStudySubjectServlet).getCurrentStudy(request);
		Mockito.doReturn(currentRole).when(spiedUpdateStudySubjectServlet).getCurrentRole(request);
		Mockito.doReturn(mockedStudySubjectDAO).when(spiedUpdateStudySubjectServlet).getStudySubjectDAO();
		Mockito.doReturn(mockedDiscrepancyNoteDAO).when(spiedUpdateStudySubjectServlet).getDiscrepancyNoteDAO();
		Mockito.doReturn(mockedSubjectGroupMapDAO).when(spiedUpdateStudySubjectServlet).getSubjectGroupMapDAO();
		Mockito.doReturn(mockedStudyGroupClassDAO).when(spiedUpdateStudySubjectServlet).getStudyGroupClassDAO();
		Mockito.doReturn(mockedStudyParameterValueDAO).when(spiedUpdateStudySubjectServlet).getStudyParameterValueDAO();
		Mockito.doNothing().when(spiedUpdateStudySubjectServlet).checkStudyLocked(Mockito.any(Page.class),
				Mockito.any(String.class), Mockito.any(MockHttpServletRequest.class),
				Mockito.any(MockHttpServletResponse.class));
		Mockito.doNothing().when(spiedUpdateStudySubjectServlet).checkStudyFrozen(Mockito.any(Page.class),
				Mockito.any(String.class), Mockito.any(MockHttpServletRequest.class),
				Mockito.any(MockHttpServletResponse.class));
	}

	@Test
	public void testUpdateStudySubjectServletBehaviorWhenRequestParameterSubjectIdIsInvalid() throws Exception {

		// 1. SETTING UP TEST

		String action = "show";

		subjectToUpdate.setId(0);

		request.setParameter("action", action);

		// 2. TESTING BEHAVIOR

		spiedUpdateStudySubjectServlet.processRequest(request, spiedResponse);

		Mockito.verify(mockedServletContext).getRequestDispatcher(Page.LIST_STUDY_SUBJECTS.getFileName());
		Mockito.verify(mockedRequestDispatcher).forward(request, spiedResponse);
	}

	@Test
	public void testUpdateStudySubjectServletBehaviorWhenRequestParameterActionIsInvalid() throws Exception {

		// 1. SETTING UP TEST

		int subjectId = 1;
		String action = "someAction";

		subjectToUpdate.setId(subjectId);

		request.setParameter("id", String.valueOf(subjectId));
		request.setParameter("action", action);

		// 2. TESTING BEHAVIOR

		spiedUpdateStudySubjectServlet.processRequest(request, spiedResponse);

		Mockito.verify(mockedServletContext).getRequestDispatcher(Page.LIST_STUDY_SUBJECTS.getFileName());
		Mockito.verify(mockedRequestDispatcher).forward(request, spiedResponse);
	}

	@Test
	public void testUpdateStudySubjectServletBehaviorWhenRequestParameterActionIsAbsent() throws Exception {

		// 1. SETTING UP TEST

		int subjectId = 1;

		subjectToUpdate.setId(subjectId);

		request.setParameter("id", String.valueOf(subjectId));

		// 2. TESTING BEHAVIOR

		spiedUpdateStudySubjectServlet.processRequest(request, spiedResponse);

		Mockito.verify(mockedServletContext).getRequestDispatcher(Page.LIST_STUDY_SUBJECTS.getFileName());
		Mockito.verify(mockedRequestDispatcher).forward(request, spiedResponse);
	}

	@Test
	public void testUpdateStudySubjectServletBehaviorOnActionShow() throws Exception {

		// 1. SETTING UP TEST

		int subjectId = 1;
		String action = "show";

		subjectToUpdate.setId(subjectId);
		subjectToUpdate.setEnrollmentDate(new Date());

		request.setParameter("id", String.valueOf(subjectId));
		request.setParameter("action", action);

		// 2. TESTING BEHAVIOR

		spiedUpdateStudySubjectServlet.processRequest(request, spiedResponse);

		Assert.assertEquals(subjectToUpdate, request.getSession().getAttribute("studySub"));

		Assert.assertEquals(subjectToUpdate.getDynamicGroupClassId(),
				request.getSession().getAttribute("selectedDynGroupClassId"));

		Mockito.verify(mockedServletContext).getRequestDispatcher(Page.UPDATE_STUDY_SUBJECT.getFileName());
		Mockito.verify(mockedRequestDispatcher).forward(request, spiedResponse);
	}

	@Test
	public void testUpdateStudySubjectServletBehaviorOnActionConfirm() throws Exception {

		// 1. SETTING UP TEST

		int subjectId = 1;
		String action = "confirm";

		subjectToUpdate.setId(subjectId);
		subjectToUpdate.setEnrollmentDate(new Date());

		// assuming that the "show" stage already have ben passed,
		// and the subjectToUpdate bean needed to be saved in the session before the "confirm" stage
		request.getSession().setAttribute("studySub", subjectToUpdate);

		request.getSession().setAttribute(AddNewSubjectServlet.FORM_DISCREPANCY_NOTES_NAME, new FormDiscrepancyNotes());

		String subjectLabel = "SS-0001";
		request.setAttribute("label", subjectLabel);

		request.setParameter("id", String.valueOf(subjectId));
		request.setParameter("action", action);

		// 2. TESTING BEHAVIOR

		spiedUpdateStudySubjectServlet.processRequest(request, spiedResponse);

		Assert.assertEquals(subjectToUpdate, request.getSession().getAttribute("studySub"));

		Mockito.verify(mockedServletContext).getRequestDispatcher(Page.UPDATE_STUDY_SUBJECT_CONFIRM.getFileName());
		Mockito.verify(mockedRequestDispatcher).forward(request, spiedResponse);
	}

	@Test
	public void testUpdateStudySubjectServletBehaviorOnActionBack() throws Exception {

		// 1. SETTING UP TEST

		int subjectId = 1;
		String action = "back";

		subjectToUpdate.setId(subjectId);

		request.setParameter("id", String.valueOf(subjectId));
		request.setParameter("action", action);

		// 2. TESTING BEHAVIOR

		spiedUpdateStudySubjectServlet.processRequest(request, spiedResponse);

		Mockito.verify(mockedServletContext).getRequestDispatcher(Page.UPDATE_STUDY_SUBJECT.getFileName());
		Mockito.verify(mockedRequestDispatcher).forward(request, spiedResponse);
	}

	@Test
	public void testUpdateStudySubjectServletBehaviorOnActionSubmit() throws Exception {

		// 1. SETTING UP TEST

		int subjectId = 1;
		String action = "submit";

		subjectToUpdate.setId(subjectId);

		request.setParameter("id", String.valueOf(subjectId));
		request.setParameter("action", action);

		// assuming that the "confirm" stage already have ben passed,
		// and the subjectToUpdate bean needed to be saved in the session before the "submit" stage
		request.getSession().setAttribute("studySub", subjectToUpdate);

		// assuming that the subjectToUpdate is not assigned to any of the dynamic group classes
		request.getSession().setAttribute("selectedDynGroupClassId", 0);

		// assuming that the subjectToUpdate is not assigned to any of the regular group classes
		request.getSession().setAttribute("groups", new ArrayList<StudyGroupClassBean>());

		// 2. TESTING BEHAVIOR

		spiedUpdateStudySubjectServlet.processRequest(request, spiedResponse);

		// verifying that StudySubject record in the data base is updated
		Mockito.verify(mockedStudySubjectDAO).update(subjectToUpdate);

		// verifying that no SubjectGroupMap records in the data base are created/updated/deleted,
		// as study subject has no subject group classes assigned
		Mockito.verify(mockedSubjectGroupMapDAO, Mockito.never()).create(Mockito.any(SubjectGroupMapBean.class));
		Mockito.verify(mockedSubjectGroupMapDAO, Mockito.never()).update(Mockito.any(SubjectGroupMapBean.class));
		Mockito.verify(mockedSubjectGroupMapDAO, Mockito.never()).deleteTestGroupMap(Mockito.any(Integer.class));

		String expectedURLToRedirect = new StringBuilder("").append(Page.VIEW_STUDY_SUBJECT_SERVLET.getFileName())
				.append("?id=").append(subjectToUpdate.getId()).toString();
		ArgumentCaptor<String> urlArgument = ArgumentCaptor.forClass(String.class);

		// verifying that the UpdateStudySubjectServlet redirects client to the ViewStudySubjectServlet
		Mockito.verify(spiedResponse).sendRedirect(urlArgument.capture());
		Assert.assertTrue(urlArgument.getValue().endsWith(expectedURLToRedirect));
	}
}
