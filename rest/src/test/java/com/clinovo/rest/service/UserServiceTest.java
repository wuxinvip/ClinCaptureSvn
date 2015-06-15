package com.clinovo.rest.service;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.UserType;
import org.junit.Test;

public class UserServiceTest extends BaseServiceTest {

	@Test
	public void testThatItIsImpossibleToCreateAUserIfUserTypeParameterHasWrongValue() throws Exception {
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", "123").param("allowsoap", "true").param("displaypassword", "true")
						.param("role", Integer.toString(3456)).accept(mediaType).secure(true).session(session))
				.andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsImpossibleToCreateAUserIfRoleParameterHasWrongValue() throws Exception {
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", "123123").accept(mediaType).secure(true)
						.session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToCreateAUserWithRoleThatDoesNotExist() throws Exception {
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(3456)).accept(mediaType)
						.secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignStudyCoderToSite() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.STUDY_CODER.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignStudyEvaluatorToSite() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.STUDY_EVALUATOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignStudyMonitorToSite() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.STUDY_MONITOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignStudyDirectorToSite() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.STUDY_DIRECTOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignStudyAdminToSite() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true")
						.param("role", Integer.toString(Role.STUDY_ADMINISTRATOR.getId())).accept(mediaType)
						.secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignSiteMonitorToStudy() throws Exception {
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.SITE_MONITOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignCRCToStudy() throws Exception {
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true")
						.param("role", Integer.toString(Role.CLINICAL_RESEARCH_COORDINATOR.getId())).accept(mediaType)
						.secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatItIsNotPossibleToAssignInvestigatorToStudy() throws Exception {
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_user_".concat(Long.toString(timestamp)))
						.param("firstname", "firstname_".concat(Long.toString(timestamp)))
						.param("lastname", "lastname_".concat(Long.toString(timestamp)))
						.param("email", "test@gmail.com").param("phone", "111111111111").param("company", "clinovo")
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.INVESTIGATOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isInternalServerError());
	}

	@Test
	public void testThatCreatedInvestigatorWithoutAdministrativePrivilegesIsNotAbleToCreateAUser() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		createNewUser(UserType.USER, Role.INVESTIGATOR);
		login(newUser.getName(), UserType.USER, Role.INVESTIGATOR, newUser.getPasswd(), newSite.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_".concat(newUser.getName()))
						.param("firstname", newUser.getFirstName()).param("lastname", newUser.getLastName())
						.param("email", newUser.getEmail()).param("phone", newUser.getPhone())
						.param("company", newUser.getInstitutionalAffiliation())
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.INVESTIGATOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isForbidden());
	}

	@Test
	public void testThatCreatedInvestigatorWithAdministrativePrivilegesIsAbleToLogin() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		createNewUser(UserType.SYSADMIN, Role.INVESTIGATOR);
		login(newUser.getName(), UserType.SYSADMIN, Role.INVESTIGATOR, newUser.getPasswd(), newSite.getName());
	}

	@Test
	public void testThatCreatedCrcWithoutAdministrativePrivilegesIsNotAbleToCreateAUser() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		createNewUser(UserType.USER, Role.CLINICAL_RESEARCH_COORDINATOR);
		login(newUser.getName(), UserType.USER, Role.CLINICAL_RESEARCH_COORDINATOR, newUser.getPasswd(),
				newSite.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_".concat(newUser.getName()))
						.param("firstname", newUser.getFirstName()).param("lastname", newUser.getLastName())
						.param("email", newUser.getEmail()).param("phone", newUser.getPhone())
						.param("company", newUser.getInstitutionalAffiliation())
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true")
						.param("role", Integer.toString(Role.CLINICAL_RESEARCH_COORDINATOR.getId())).accept(mediaType)
						.secure(true).session(session)).andExpect(status().isForbidden());
	}

	@Test
	public void testThatCreatedCrcWithAdministrativePrivilegesIsAbleToLogin() throws Exception {
		createNewStudy();
		createNewSite(newStudy.getId());
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newSite.getName());
		createNewUser(UserType.SYSADMIN, Role.CLINICAL_RESEARCH_COORDINATOR);
		login(newUser.getName(), UserType.SYSADMIN, Role.CLINICAL_RESEARCH_COORDINATOR, newUser.getPasswd(),
				newSite.getName());
	}

	@Test
	public void testThatCreatedStudyEvaluatorWithoutAdministrativePrivilegesIsNotAbleToCreateAUser() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.USER, Role.STUDY_EVALUATOR);
		login(newUser.getName(), UserType.USER, Role.STUDY_EVALUATOR, newUser.getPasswd(), newStudy.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_".concat(newUser.getName()))
						.param("firstname", newUser.getFirstName()).param("lastname", newUser.getLastName())
						.param("email", newUser.getEmail()).param("phone", newUser.getPhone())
						.param("company", newUser.getInstitutionalAffiliation())
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.STUDY_EVALUATOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isForbidden());
	}

	@Test
	public void testThatCreatedStudyEvaluatorWithAdministrativePrivilegesIsAbleToLogin() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.SYSADMIN, Role.STUDY_EVALUATOR);
		login(newUser.getName(), UserType.SYSADMIN, Role.STUDY_EVALUATOR, newUser.getPasswd(), newStudy.getName());
	}

	@Test
	public void testThatCreatedStudyCoderWithoutAdministrativePrivilegesIsNotAbleToCreateAUser() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.USER, Role.STUDY_CODER);
		login(newUser.getName(), UserType.USER, Role.STUDY_CODER, newUser.getPasswd(), newStudy.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_".concat(newUser.getName()))
						.param("firstname", newUser.getFirstName()).param("lastname", newUser.getLastName())
						.param("email", newUser.getEmail()).param("phone", newUser.getPhone())
						.param("company", newUser.getInstitutionalAffiliation())
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.STUDY_CODER.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isForbidden());
	}

	@Test
	public void testThatCreatedStudyCoderWithAdministrativePrivilegesIsAbleToLogin() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.SYSADMIN, Role.STUDY_CODER);
		login(newUser.getName(), UserType.SYSADMIN, Role.STUDY_CODER, newUser.getPasswd(), newStudy.getName());
	}

	@Test
	public void testThatCreatedStudyMonitorWithoutAdministrativePrivilegesIsNotAbleToCreateAUser() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.USER, Role.STUDY_MONITOR);
		login(newUser.getName(), UserType.USER, Role.STUDY_MONITOR, newUser.getPasswd(), newStudy.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_".concat(newUser.getName()))
						.param("firstname", newUser.getFirstName()).param("lastname", newUser.getLastName())
						.param("email", newUser.getEmail()).param("phone", newUser.getPhone())
						.param("company", newUser.getInstitutionalAffiliation())
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true").param("role", Integer.toString(Role.STUDY_MONITOR.getId()))
						.accept(mediaType).secure(true).session(session)).andExpect(status().isForbidden());
	}

	@Test
	public void testThatCreatedStudyMonitorWithAdministrativePrivilegesIsAbleToLogin() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.SYSADMIN, Role.STUDY_MONITOR);
		login(newUser.getName(), UserType.SYSADMIN, Role.STUDY_MONITOR, newUser.getPasswd(), newStudy.getName());
	}

	@Test
	public void testThatCreatedStudyAdminWithoutAdministrativePrivilegesIsNotAbleToCreateAUser() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.USER, Role.STUDY_ADMINISTRATOR);
		login(newUser.getName(), UserType.USER, Role.STUDY_ADMINISTRATOR, newUser.getPasswd(), newStudy.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", "new_".concat(newUser.getName()))
						.param("firstname", newUser.getFirstName()).param("lastname", newUser.getLastName())
						.param("email", newUser.getEmail()).param("phone", newUser.getPhone())
						.param("company", newUser.getInstitutionalAffiliation())
						.param("usertype", Integer.toString(UserType.USER.getId())).param("allowsoap", "true")
						.param("displaypassword", "true")
						.param("role", Integer.toString(Role.STUDY_ADMINISTRATOR.getId())).accept(mediaType)
						.secure(true).session(session)).andExpect(status().isForbidden());
	}

	@Test
	public void testThatCreatedStudyAdminWithAdministrativePrivilegesIsAbleToLogin() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.SYSADMIN, Role.STUDY_ADMINISTRATOR);
		login(newUser.getName(), UserType.SYSADMIN, Role.STUDY_ADMINISTRATOR, newUser.getPasswd(), newStudy.getName());
	}

	@Test
	public void testThatCreateUserRequestReturnsCode500IfUsernameHasBeenTakenAlready() throws Exception {
		createNewStudy();
		login(userName, UserType.SYSADMIN, Role.SYSTEM_ADMINISTRATOR, password, newStudy.getName());
		createNewUser(UserType.SYSADMIN, Role.STUDY_ADMINISTRATOR);
		login(newUser.getName(), UserType.SYSADMIN, Role.STUDY_ADMINISTRATOR, newUser.getPasswd(), newStudy.getName());
		this.mockMvc.perform(
				post(API_USER_CREATE).param("username", newUser.getName()).param("firstname", newUser.getFirstName())
						.param("lastname", newUser.getLastName()).param("email", newUser.getEmail())
						.param("phone", newUser.getPhone()).param("company", newUser.getInstitutionalAffiliation())
						.param("usertype", Integer.toString(UserType.SYSADMIN.getId())).param("allowsoap", "true")
						.param("displaypassword", "true")
						.param("role", Integer.toString(Role.STUDY_ADMINISTRATOR.getId())).accept(mediaType)
						.secure(true).session(session)).andExpect(status().isInternalServerError());
	}
}