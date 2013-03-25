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

package org.akaza.openclinica.domain.crfdata;

import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dyn_item_group_metadata")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "dyn_item_group_metadata_id_seq") })
public class DynamicsItemGroupMetadataBean extends AbstractMutableDomainObject {

	private boolean showGroup;
	private int eventCrfId;
	private int itemGroupMetadataId;
	private int itemGroupId;
	private int passedDde;

	public DynamicsItemGroupMetadataBean() {
		passedDde = 0;
	}

	public DynamicsItemGroupMetadataBean(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {
		setEventCrfId(eventCrfBean.getId());
		setItemGroupMetadataId(metadataBean.getId());
		setItemGroupId(metadataBean.getItemGroupId());
		setShowGroup(metadataBean.isShowGroup());
		setPassedDde(0);
	}

	public boolean isShowGroup() {
		return showGroup;
	}

	public void setShowGroup(boolean showGroup) {
		this.showGroup = showGroup;
	}

	public int getPassedDde() {
		return passedDde;
	}

	public void setPassedDde(int passedDde) {
		this.passedDde = passedDde;
	}

	public int getEventCrfId() {
		return eventCrfId;
	}

	public void setEventCrfId(int eventCrfId) {
		this.eventCrfId = eventCrfId;
	}

	public int getItemGroupMetadataId() {
		return itemGroupMetadataId;
	}

	public void setItemGroupMetadataId(int itemGroupMetadataId) {
		this.itemGroupMetadataId = itemGroupMetadataId;
	}

	public int getItemGroupId() {
		return itemGroupId;
	}

	public void setItemGroupId(int itemGroupId) {
		this.itemGroupId = itemGroupId;
	}

}
