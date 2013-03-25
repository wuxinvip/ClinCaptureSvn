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
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.managestudy;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

import java.util.ArrayList;

/**
 * @author jxu
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class StudyGroupBean extends AuditableEntityBean {
	// STUDY_GROUP_ID NAME STUDY_ID OWNER_ID
	// DATE_CREATED GROUP_TYPE_ID STATUS_ID DATE_UPDATED
	// UPDATE_ID
	private String description = "";
	private int studyGroupClassId;
	private ArrayList subjectMaps = new ArrayList(); // not in DB

	/**
	 * @return Returns the subjectMaps.
	 */
	public ArrayList getSubjectMaps() {
		return subjectMaps;
	}

	/**
	 * @param subjectMaps
	 *            The subjectMaps to set.
	 */
	public void setSubjectMaps(ArrayList subjectMaps) {
		this.subjectMaps = subjectMaps;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the studyGroupClassId.
	 */
	public int getStudyGroupClassId() {
		return studyGroupClassId;
	}

	/**
	 * @param studyGroupClassId
	 *            The studyGroupClassId to set.
	 */
	public void setStudyGroupClassId(int studyGroupClassId) {
		this.studyGroupClassId = studyGroupClassId;
	}
}
