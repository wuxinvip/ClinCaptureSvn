/*******************************************************************************
 * CLINOVO RESERVES ALL RIGHTS TO THIS SOFTWARE, INCLUDING SOURCE AND DERIVED BINARY CODE. BY DOWNLOADING THIS SOFTWARE YOU AGREE TO THE FOLLOWING LICENSE:
 *
 * Subject to the terms and conditions of this Agreement including, Clinovo grants you a non-exclusive, non-transferable, non-sublicenseable limited license without license fees to reproduce and use internally the software complete and unmodified for the sole purpose of running Programs on one computer.
 * This license does not allow for the commercial use of this software except by IRS approved non-profit organizations; educational entities not working in joint effort with for profit business.
 * To use the license for other purposes, including for profit clinical trials, an additional paid license is required. Please contact our licensing department at http://www.clinovo.com/contact for pricing information.
 *
 * You may not modify, decompile, or reverse engineer the software.
 * Clinovo disclaims any express or implied warranty of fitness for use.
 * No right, title or interest in or to any trademark, service mark, logo or trade name of Clinovo or its licensors is granted under this Agreement.
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. CLINOVO FURTHER DISCLAIMS ALL WARRANTIES, EXPRESS AND IMPLIED, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.

 * LIMITATION OF LIABILITY. IN NO EVENT SHALL CLINOVO BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, PUNITIVE OR CONSEQUENTIAL DAMAGES, OR DAMAGES FOR LOSS OF PROFITS, REVENUE, DATA OR DATA USE, INCURRED BY YOU OR ANY THIRD PARTY, WHETHER IN AN ACTION IN CONTRACT OR TORT, EVEN IF ORACLE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. CLINOVO'S ENTIRE LIABILITY FOR DAMAGES HEREUNDER SHALL IN NO EVENT EXCEED TWO HUNDRED DOLLARS (U.S. $200).
 *******************************************************************************/

package com.clinovo.rest.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.clinovo.i18n.LocaleResolver;
import com.clinovo.rest.exception.RestException;
import com.clinovo.rest.model.UserDetails;
import com.clinovo.rest.service.AuthenticationService;
import com.clinovo.rest.service.OdmService;
import com.clinovo.rest.service.WadlService;
import com.clinovo.rest.util.RequestParametersValidator;

/**
 * PermissionChecker class.
 */
@SuppressWarnings("unused")
public class PermissionChecker extends HandlerInterceptorAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionChecker.class);

	public static final String CLIENT_VERSION = "version";
	public static final String AUTHENTICATE = "authenticate";
	public static final String CLIENT_VERSION_PATTERN = "\\d{1,}.*\\d{1,}";
	public static final String API_AUTHENTICATED_USER_DETAILS = "API_AUTHENTICATED_USER_DETAILS";

	@Autowired
	private DataSource dataSource;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Note that we may be ensure that the connection is SSL. Because we don't allow anything else.
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param handler
	 *            Object
	 * @return boolean
	 * @throws Exception
	 *             an Exception
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		boolean proceed = false;
		LocaleResolver.resolveRestApiLocale();
		if (handler instanceof HandlerMethod) {
			RequestParametersValidator.validate(request, dataSource, messageSource, (HandlerMethod) handler);
			proceed = (((HandlerMethod) handler).getBean() instanceof AuthenticationService
					&& ((HandlerMethod) handler).getMethod().getName().equals(AUTHENTICATE))
					|| ((HandlerMethod) handler).getBean() instanceof WadlService
					|| ((HandlerMethod) handler).getBean() instanceof OdmService;
			if (!proceed) {
				UserDetails userDetails = (UserDetails) request.getSession()
						.getAttribute(API_AUTHENTICATED_USER_DETAILS);
				if (userDetails == null) {
					throw new RestException(messageSource, "rest.userShouldBeAuthenticated",
							HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					proceed = true;
				}
			}
		}
		return proceed;
	}
}
