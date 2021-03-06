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

/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.extract;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

import java.util.ArrayList;

/**
 * FilterBean.java, meant to take the place of Query Bean.
 * 
 * @author thickerson
 * 
 * 
 */
@SuppressWarnings({"rawtypes", "serial"})
public class FilterBean extends AuditableEntityBean {
	private String description;
	private String SQLStatement;
	private ArrayList filterDataObjects;
	private String explanation;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSQLStatement() {
		return SQLStatement;
	}

	public void setSQLStatement(String statement) {
		SQLStatement = statement;
	}

	public ArrayList getFilterDataObjects() {
		return filterDataObjects;
	}

	public void setFilterDataObjects(ArrayList filterDataObjects) {
		this.filterDataObjects = filterDataObjects;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}
}
