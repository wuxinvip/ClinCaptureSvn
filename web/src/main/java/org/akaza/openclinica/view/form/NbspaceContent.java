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
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.akaza.openclinica.view.form;

import org.jdom.Content;

/**
 * This class has the sole purpose of creating a non-breaking space character for inside a textarea tag, in order to
 * force JDOM to create a non-empty tag (&lt;textarea> &lt;/textarea>, as opposed to &lt;textarea />).
 */
@SuppressWarnings({ "serial" })
public class NbspaceContent extends Content {
	@Override
	public String getValue() {
		return "&nbsp;";
	}
}
