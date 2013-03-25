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

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright �� 2003-2005 Akaza Research
 */

package org.akaza.openclinica.bean.admin;

import org.akaza.openclinica.bean.core.AuditableEntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;

import java.util.ArrayList;

/**
 * @author Jun Xu
 * 
 *         A class for display study list properly, group studies by parent and child relationship
 */
public class DisplayStudyBean extends AuditableEntityBean {
	private StudyBean parent;
	private ArrayList children;

	/**
	 * @return Returns the children.
	 */
	public ArrayList getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            The children to set.
	 */
	public void setChildren(ArrayList children) {
		this.children = children;
	}

	/**
	 * @return Returns the parent.
	 */
	public StudyBean getParent() {
		return parent;
	}

	/**
	 * @param parent
	 *            The parent to set.
	 */
	public void setParent(StudyBean parent) {
		this.parent = parent;
	}
}
