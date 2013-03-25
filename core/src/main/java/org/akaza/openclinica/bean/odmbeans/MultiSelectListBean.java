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
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2010 Akaza
 * Research
 *
 */

package org.akaza.openclinica.bean.odmbeans;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author ywang (March, 2010)
 * 
 */
public class MultiSelectListBean extends ElementOIDBean {
	private String name;
	private String dataType;
	private String actualDataType;
	private List<MultiSelectListItemBean> multiSelectListItems = new ArrayList<MultiSelectListItemBean>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getActualDataType() {
		return actualDataType;
	}

	public void setActualDataType(String actualDataType) {
		this.actualDataType = actualDataType;
	}

	public List<MultiSelectListItemBean> getMultiSelectListItems() {
		return multiSelectListItems;
	}

	public void setMultiSelectListItems(List<MultiSelectListItemBean> multiSelectListItems) {
		this.multiSelectListItems = multiSelectListItems;
	}
}
