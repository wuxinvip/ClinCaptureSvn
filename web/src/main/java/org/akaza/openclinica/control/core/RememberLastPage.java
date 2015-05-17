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

package org.akaza.openclinica.control.core;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings({"rawtypes", "unchecked", "serial"})
public abstract class RememberLastPage extends Controller {

	protected abstract String getUrlKey(HttpServletRequest request);

	protected abstract String getDefaultUrl(HttpServletRequest request);

	protected abstract boolean userDoesNotUseJmesaTableForNavigation(HttpServletRequest request);

	private boolean redirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean result = false;
		String url = getSavedUrl(getUrlKey(request), request);
		if (url != null) {
			result = true;
			storeAttributes(request);
			// for navigation purpose (to prevent double url in back-button stack)
			request.getSession().setAttribute("skipURL", "true");
			response.sendRedirect(response.encodeRedirectURL(url));
		}
		return result;
	}

	protected boolean shouldRedirect(HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean result = false;
		String key = getUrlKey(request);
		String defaultUrl = getDefaultUrl(request);
		String keyValue = getSavedUrl(key, request);
		if (keyValue == null && defaultUrl != null) {
			saveUrl(key, request.getRequestURL() + defaultUrl, request);
		}
		if (request.getMethod().equalsIgnoreCase("POST")) {
			String referer = request.getHeader(REFERER);
			String url = request.getRequestURL().toString();
			if (referer != null
					&& !referer.replaceAll("http.*://", "").toLowerCase()
							.startsWith(url.replaceAll("http.*://", "").toLowerCase())) {
				result = redirect(request, response);
			} else if (defaultUrl != null) {
				saveUrl(key, request.getRequestURL() + defaultUrl, request);
			}
		} else if (request.getMethod().equalsIgnoreCase("GET")) {
			if (userDoesNotUseJmesaTableForNavigation(request)) {
				result = redirect(request, response);
			} else {
				if (request.getQueryString() != null) {
					saveUrl(key, request.getRequestURL() + "?" + request.getQueryString(), request);
				}
			}
		}
		if (!result) {
			restoreAttributes(request);
		}
		return result;
	}

	protected void saveUrl(String key, String value, HttpServletRequest request) {
		request.getSession().setAttribute(key, value);
	}

	protected String getSavedUrl(String key, HttpServletRequest request) {
		return (String) request.getSession().getAttribute(key);
	}

	private void storeAttributes(HttpServletRequest request) {
		Map storedAttributes = new HashMap();
		storedAttributes.put(PAGE_MESSAGE, request.getAttribute(PAGE_MESSAGE));
		request.getSession().setAttribute(STORED_ATTRIBUTES, storedAttributes);
	}

	private void restoreAttributes(HttpServletRequest request) {
		Map<String, Object> storedAttributes = (Map) request.getSession().getAttribute(STORED_ATTRIBUTES);
		request.getSession().removeAttribute(STORED_ATTRIBUTES);
		if (storedAttributes != null) {
			request.setAttribute(PAGE_MESSAGE, storedAttributes.get(PAGE_MESSAGE));
		}
	}
}
