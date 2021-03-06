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

package org.akaza.openclinica.control;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.core.SpringServlet;

/**
 * OCSessionListener.
 */
public class OCSessionListener implements HttpSessionListener {

	/**
	 * {@inheritDoc}
	 */
	public void sessionCreated(HttpSessionEvent arg0) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void sessionDestroyed(HttpSessionEvent arg0) {
		// This method will be called just before session is to be destroyed
		HttpSession session = arg0.getSession();
		UserAccountBean ub = (UserAccountBean) session.getAttribute("userBean");
		if (ub != null) {
			SpringServlet.removeLockedCRF(ub.getId());
			SpringServlet.setStoredSessionAttributes(session);
		}
	}
}
