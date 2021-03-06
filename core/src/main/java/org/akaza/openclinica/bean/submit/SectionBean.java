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
package org.akaza.openclinica.bean.submit;

import java.util.ArrayList;

import org.akaza.openclinica.bean.core.AuditableEntityBean;

/**
 * <P>
 * SectionBean.java, meant to collect items in a CRF on one page; organizational collection of items per their metadata.
 * 
 * @author thickerson
 * 
 * 
 */
@SuppressWarnings({"rawtypes", "serial"})
public class SectionBean extends AuditableEntityBean {

	private int crfVersionId;

	private String label;

	private String title;

	private String instructions;

	private String subtitle;

	private String pageNumberLabel;

	private int ordinal;

	private int parentId;

	private int borders;

	/**
	 * How many items are in this section? Not in the database. Only used for display.
	 */
	private int numItems = 0;

	private ArrayList items; // no in DB

	private ArrayList<ItemGroupBean> groups; // YW, 08-21-2007, not in DB

	/**
	 * How many items need validation? This is computed as the number of items in the section whose status is pending.
	 * Not in the database. Only used for display.
	 */
	private int numItemsNeedingValidation = 0;

	/**
	 * How many items are completed? This is computed as the number of items in the section whose status is uncompleted.
	 * Not in the database. Only used for display.
	 */
	private int numItemsCompleted = 0;

	// if section contains simple conditional display item
	private boolean hasSCDItem;

	private boolean processDefaultValues;

	/**
	 * The Section whose id == parentId. Not in the database. Only used for display.
	 */
	private SectionBean parent;

	/**
	 * Constructor.
	 */
	public SectionBean() {
		crfVersionId = 0;
		label = "";
		title = "";
		instructions = "";
		subtitle = "";
		pageNumberLabel = "";
		ordinal = 0;
		parentId = 0;
		hasSCDItem = false;

		// we do this so that we don't go into infinite recursion
		// however in getParent() we guarantee that the returned value
		// is never null
		parent = null;
		items = new ArrayList();
		groups = new ArrayList<ItemGroupBean>();
		borders = 0;
	}

	public int getBorders() {
		return borders;
	}

	public void setBorders(int borders) {
		this.borders = borders;
	}

	/**
	 * @return Returns the crfVersionId.
	 */
	public int getCRFVersionId() {
		return crfVersionId;
	}

	/**
	 * @param versionId
	 *            The crfVersionId to set.
	 */
	public void setCRFVersionId(int versionId) {
		crfVersionId = versionId;
	}

	/**
	 * @return Returns the description.
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * @param description
	 *            The description to set.
	 */
	public void setInstructions(String description) {
		this.instructions = description;
	}

	/**
	 * @return Returns the header.
	 */
	public String getSubtitle() {
		return subtitle;
	}

	/**
	 * @param header
	 *            The header to set.
	 */
	public void setSubtitle(String header) {
		this.subtitle = header;
	}

	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return Returns the pageNumberLabel.
	 */
	public String getPageNumberLabel() {
		return pageNumberLabel;
	}

	/**
	 * @param pageNumberLabel
	 *            The pageNumberLabel to set.
	 */
	public void setPageNumberLabel(String pageNumberLabel) {
		this.pageNumberLabel = pageNumberLabel;
	}

	/**
	 * @return Returns the parentId.
	 */
	public int getParentId() {
		return parentId;
	}

	/**
	 * @param parentId
	 *            The parentId to set.
	 */
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Returns the ordinal.
	 */
	public int getOrdinal() {
		return ordinal;
	}

	/**
	 * @param ordinal
	 *            The ordinal to set.
	 */
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	@Override
	public String getName() {
		return getLabel();
	}

	@Override
	public void setName(String name) {
		setLabel(name);
	}

	/**
	 * @return Returns the numItems.
	 */
	public int getNumItems() {
		return numItems;
	}

	/**
	 * @param numItems
	 *            The numItems to set.
	 */
	public void setNumItems(int numItems) {
		this.numItems = numItems;
	}

	/**
	 * @return Returns the numItemsCompleted.
	 */
	public int getNumItemsCompleted() {
		return numItemsCompleted;
	}

	/**
	 * @param numItemsCompleted
	 *            The numItemsCompleted to set.
	 */
	public void setNumItemsCompleted(int numItemsCompleted) {
		this.numItemsCompleted = numItemsCompleted;
	}

	/**
	 * @return Returns the numItemsNeedingValidation.
	 */
	public int getNumItemsNeedingValidation() {
		return numItemsNeedingValidation;
	}

	/**
	 * @param numItemsNeedingValidation
	 *            The numItemsNeedingValidation to set.
	 */
	public void setNumItemsNeedingValidation(int numItemsNeedingValidation) {
		this.numItemsNeedingValidation = numItemsNeedingValidation;
	}

	/**
	 * @return Returns the parent.
	 */
	public SectionBean getParent() {
		if (parent == null) {
			parent = new SectionBean();
		}
		return parent;
	}

	/**
	 * @param parent
	 *            The parent to set.
	 */
	public void setParent(SectionBean parent) {
		this.parent = parent;
	}

	/**
	 * @return Returns the items.
	 */
	public ArrayList getItems() {
		return items;
	}

	/**
	 * @param items
	 *            The items to set.
	 */
	public void setItems(ArrayList items) {
		this.items = items;
	}

	public ArrayList<ItemGroupBean> getGroups() {
		return groups;
	}

	public void setGroups(ArrayList<ItemGroupBean> groups) {
		this.groups = groups;
	}

	public boolean hasSCDItem() {
		return hasSCDItem;
	}

	public void setHasSCDItem(boolean hasSCDItem) {
		this.hasSCDItem = hasSCDItem;
	}

	public boolean isProcessDefaultValues() {
		return processDefaultValues;
	}

	public void setProcessDefaultValues(boolean processDefaultValues) {
		this.processDefaultValues = processDefaultValues;
	}
}
