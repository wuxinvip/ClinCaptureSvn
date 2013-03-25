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

package org.akaza.openclinica.view.display;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.submit.DisplaySectionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.SectionDAO;
import org.akaza.openclinica.view.form.FormBeanUtil;

/**
 * This class handles the responsibility for generating a List of DisplaySectionBeans for a form, such as for a CRF that
 * will be printed. The class is used by PrintCRFServlet and PrintDataEntryServlet.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DisplaySectionBeanHandler {
	private boolean hasStoredData = false;
	private int crfVersionId;
	private int eventCRFId;
	private List<DisplaySectionBean> displaySectionBeans;
	private ServletContext context;
	private DataSource dataSource;

	public DisplaySectionBeanHandler(boolean dataEntry) {
		this.hasStoredData = dataEntry;
	}

	public DisplaySectionBeanHandler(boolean dataEntry, DataSource dataSource, ServletContext context) {
		this(dataEntry);
		if (dataSource != null) {
			this.setDataSource(dataSource);
		}
		if (context != null) {
			this.context = context;
		}
	}

	public int getCrfVersionId() {
		return crfVersionId;
	}

	public void setCrfVersionId(int crfVersionId) {
		this.crfVersionId = crfVersionId;
	}

	public int getEventCRFId() {
		return eventCRFId;
	}

	public void setEventCRFId(int eventCRFId) {
		this.eventCRFId = eventCRFId;
	}

	/**
	 * This method creates a List of DisplaySectionBeans, returning them in the order that the sections appear in a CRF.
	 * This List is "lazily" initialized the first time it is requested.
	 * 
	 * @return A List of DisplaySectionBeans.
	 * @see org.akaza.openclinica.control.managestudy.PrintCRFServlet
	 * @see org.akaza.openclinica.control.managestudy.PrintDataEntryServlet
	 */
	public List<DisplaySectionBean> getDisplaySectionBeans() {
		FormBeanUtil formBeanUtil;
		ArrayList<SectionBean> allCrfSections;
		// DAO classes for getting item definitions
		SectionDAO sectionDao;
		if (displaySectionBeans == null) {
			displaySectionBeans = new ArrayList<DisplaySectionBean>();
			formBeanUtil = new FormBeanUtil();
			if (hasStoredData) {
			}

			// We need a CRF version id to populate the form display
			if (this.crfVersionId == 0) {
				return displaySectionBeans;
			}

			sectionDao = new SectionDAO(dataSource);
			allCrfSections = (ArrayList) sectionDao.findByVersionId(this.crfVersionId);

			// for the purposes of null values, try to obtain a valid
			// eventCrfDefinition id
			EventDefinitionCRFBean eventDefBean = null;
			EventCRFBean eventCRFBean = new EventCRFBean();
			if (eventCRFId > 0) {
				EventCRFDAO ecdao = new EventCRFDAO(dataSource);
				eventCRFBean = (EventCRFBean) ecdao.findByPK(eventCRFId);
				StudyEventDAO sedao = new StudyEventDAO(dataSource);
				StudyEventBean studyEvent = (StudyEventBean) sedao.findByPK(eventCRFBean.getStudyEventId());

				EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(dataSource);
				StudyDAO sdao = new StudyDAO(dataSource);
				StudyBean study = sdao.findByStudySubjectId(eventCRFBean.getStudySubjectId());
				eventDefBean = eventDefinitionCRFDAO.findByStudyEventIdAndCRFVersionId(study, studyEvent.getId(),
						this.crfVersionId);
			}
			eventDefBean = eventDefBean == null ? new EventDefinitionCRFBean() : eventDefBean;
			// Create an array or List of DisplaySectionBeans representing each
			// section
			// for printing
			DisplaySectionBean displaySectionBean;
			for (SectionBean sectionBean : allCrfSections) {
				displaySectionBean = formBeanUtil.createDisplaySectionBWithFormGroupsForPrint(sectionBean.getId(),
						this.crfVersionId, dataSource, eventDefBean.getId(), eventCRFBean, context);
				displaySectionBeans.add(displaySectionBean);
			}
		}
		return displaySectionBeans;
	}

	public void setDisplaySectionBeans(List<DisplaySectionBean> displaySectionBeans) {
		this.displaySectionBeans = displaySectionBeans;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
