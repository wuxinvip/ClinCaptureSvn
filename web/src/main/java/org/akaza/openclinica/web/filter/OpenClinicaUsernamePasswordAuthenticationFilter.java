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

package org.akaza.openclinica.web.filter;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SpringServlet;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.domain.technicaladmin.AuditUserLoginBean;
import org.akaza.openclinica.domain.technicaladmin.LoginStatus;
import org.akaza.openclinica.util.InactiveAnalyzer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.TextEscapeUtils;
import org.springframework.util.Assert;

import com.clinovo.i18n.LocaleResolver;

/**
 * Processes an authentication form submission. Called {@code AuthenticationProcessingFilter} prior to Spring Security
 * 3.0.
 * <p>
 * Login forms must present two parameters to this filter: a username and password. The default parameter names to use
 * are contained in the static fields {@link #SPRING_SECURITY_FORM_USERNAME_KEY} and
 * {@link #SPRING_SECURITY_FORM_PASSWORD_KEY}. The parameter names can also be changed by setting the
 * {@code usernameParameter} and {@code passwordParameter} properties.
 * <p>
 * This filter by default responds to the URL {@code /j_spring_security_check}.
 * 
 * @author Ben Alex
 * @author Colin Sampaleanu
 * @author Luke Taylor
 * @since 3.0
 */
@SuppressWarnings({"serial", "static-access"})
public class OpenClinicaUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "j_username";
	public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "j_password";
	public static final String SPRING_SECURITY_LAST_USERNAME_KEY = "SPRING_SECURITY_LAST_USERNAME";

	private String usernameParameter = SPRING_SECURITY_FORM_USERNAME_KEY;
	private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
	private boolean postOnly = true;

	private AuditUserLoginDao auditUserLoginDao;
	private ConfigurationDao configurationDao;
	private DataSource dataSource;

	@Autowired
	private CoreResources coreResources;
	
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private JavaMailSenderImpl mailSender;

	/**
	 * OpenClinicaUsernamePasswordAuthenticationFilter constructor.
	 */
	public OpenClinicaUsernamePasswordAuthenticationFilter() {
		super("/j_spring_security_check");
	}

	/**
	 * Attemp authentication.
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @return Authentication
	 * @throws AuthenticationException
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		if (postOnly && !request.getMethod().equals("POST")) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

		String hostName = request.getRequestURL().toString().replaceAll("http.{0,1}://", "").replaceAll(":.*|/.*", "");
		coreResources.setField("currentHostName", hostName);
		String restoreSessionFlag = request.getParameter("shouldSessionParametersBeRestored");
		if (restoreSessionFlag != null && restoreSessionFlag.equals("true")) {
			SpringServlet.setRestoreSessionFlag(restoreSessionFlag);
		} else {
			SpringServlet.resetRestoreSessionFlag();
		}

		String username = obtainUsername(request);
		String password = obtainPassword(request);

		if (username == null) {
			username = "";
		}

		if (password == null) {
			password = "";
		}

		username = username.trim();

		UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

		// Place the last username attempted into HttpSession for views
		HttpSession session = request.getSession(false);

		if (session != null || getAllowSessionCreation()) {
			request.getSession().setAttribute(SPRING_SECURITY_LAST_USERNAME_KEY,
					TextEscapeUtils.escapeEntities(username));
		}

		// Allow subclasses to set the "details" property
		setDetails(request, authRequest);

		Authentication authentication;
		UserAccountBean userAccountBean = null;

		LocaleResolver.resolveLocale();
		
		try {
			EntityBean eb = getUserAccountDao().findByUserName(username);
			if (eb.getId() != 0) {
				userAccountBean = (UserAccountBean) eb;
			} else {
				throw new AuthenticationException("") {
				};
			}

			if (userAccountBean.getStatus().isLocked()) {
				throw new LockedException("locked");
			}

			InactiveAnalyzer.analyze(userAccountBean, getUserAccountDao(), messageSource, mailSender);

			if (!userAccountBean.getAccountNonLocked()) {
				throw new LockedException("user locked");
			}

			authentication = this.getAuthenticationManager().authenticate(authRequest);
			auditUserLogin(username, LoginStatus.SUCCESSFUL_LOGIN, userAccountBean);
			resetLockCounter(userAccountBean);
			// To remove the locking of Event CRFs previusly locked by this user.
			SpringServlet.removeLockedCRF(userAccountBean.getId());
			SpringServlet.setDomainName(request);
		} catch (LockedException le) {
			auditUserLogin(username, LoginStatus.FAILED_LOGIN_LOCKED, userAccountBean);
			throw le;
		} catch (BadCredentialsException au) {
			auditUserLogin(username, LoginStatus.FAILED_LOGIN, userAccountBean);
			lockAccount(userAccountBean);
			throw au;
		} catch (AuthenticationException ae) {
			auditUserLogin(username, LoginStatus.FAILED_LOGIN, userAccountBean);
			lockAccount(userAccountBean);
			throw ae;
		}
		return authentication;
	}

	private void auditUserLogin(String username, LoginStatus loginStatus, UserAccountBean userAccount) {
		AuditUserLoginBean auditUserLogin = new AuditUserLoginBean();
		auditUserLogin.setUserName(username);
		auditUserLogin.setLoginStatus(loginStatus);
		auditUserLogin.setLoginAttemptDate(new Date());
		auditUserLogin.setUserAccountId(userAccount != null ? userAccount.getId() : null);
		getAuditUserLoginDao().saveOrUpdate(auditUserLogin);
	}

	private void resetLockCounter(UserAccountBean userAccount) {
		if (userAccount != null) {
			getUserAccountDao().updateLockCounter(userAccount.getId(), 0);
		}
	}

	private void lockAccount(UserAccountBean userAccount) {
		Boolean lockFeatureActive = Boolean.valueOf(getConfigurationDao().findByKey("user.lock.switch").getValue());
		if (userAccount != null && lockFeatureActive) {
			Integer count = userAccount.getLockCounter();
			count++;
			getUserAccountDao().updateLockCounter(userAccount.getId(), count);
			//
			String lockCountString = getConfigurationDao().findByKey("user.lock.allowedFailedConsecutiveLoginAttempts")
					.getValue();
			Integer lockThreshold = Integer.valueOf(lockCountString);
			if (count >= lockThreshold) {
				getUserAccountDao().lockUser(userAccount.getId());
			}
		}
	}

	/**
	 * Enables subclasses to override the composition of the password, such as by including additional values and a
	 * separator.
	 * <p>
	 * This might be used for example if a postcode/zipcode was required in addition to the password. A delimiter such
	 * as a pipe (|) should be used to separate the password and extended value(s). The <code>AuthenticationDao</code>
	 * will need to generate the expected password in a corresponding manner.
	 * </p>
	 * 
	 * @param request
	 *            so that request attributes can be retrieved
	 * 
	 * @return the password that will be presented in the <code>Authentication</code> request token to the
	 *         <code>AuthenticationManager</code>
	 */
	protected String obtainPassword(HttpServletRequest request) {
		return request.getParameter(passwordParameter);
	}

	/**
	 * Enables subclasses to override the composition of the username, such as by including additional values and a
	 * separator.
	 * 
	 * @param request
	 *            so that request attributes can be retrieved
	 * 
	 * @return the username that will be presented in the <code>Authentication</code> request token to the
	 *         <code>AuthenticationManager</code>
	 */
	protected String obtainUsername(HttpServletRequest request) {
		return request.getParameter(usernameParameter);
	}

	/**
	 * Provided so that subclasses may configure what is put into the authentication request's details property.
	 * 
	 * @param request
	 *            that an authentication request is being created for
	 * @param authRequest
	 *            the authentication request object that should have its details set
	 */
	protected void setDetails(HttpServletRequest request, UsernamePasswordAuthenticationToken authRequest) {
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
	}

	/**
	 * Sets the parameter name which will be used to obtain the username from the login request.
	 * 
	 * @param usernameParameter
	 *            the parameter name. Defaults to "j_username".
	 */
	public void setUsernameParameter(String usernameParameter) {
		Assert.hasText(usernameParameter, "Username parameter must not be empty or null");
		this.usernameParameter = usernameParameter;
	}

	/**
	 * Sets the parameter name which will be used to obtain the password from the login request..
	 * 
	 * @param passwordParameter
	 *            the parameter name. Defaults to "j_password".
	 */
	public void setPasswordParameter(String passwordParameter) {
		Assert.hasText(passwordParameter, "Password parameter must not be empty or null");
		this.passwordParameter = passwordParameter;
	}

	public void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly;
	}

	public final String getUsernameParameter() {
		return usernameParameter;
	}

	public final String getPasswordParameter() {
		return passwordParameter;
	}

	public AuditUserLoginDao getAuditUserLoginDao() {
		return auditUserLoginDao;
	}

	public void setAuditUserLoginDao(AuditUserLoginDao auditUserLoginDao) {
		this.auditUserLoginDao = auditUserLoginDao;
	}

	public ConfigurationDao getConfigurationDao() {
		return configurationDao;
	}

	public void setConfigurationDao(ConfigurationDao configurationDao) {
		this.configurationDao = configurationDao;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public UserAccountDAO getUserAccountDao() {
		return new UserAccountDAO(dataSource);
	}

}
