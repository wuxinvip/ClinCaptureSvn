/*******************************************************************************
 * CLINOVO RESERVES ALL RIGHTS TO THIS SOFTWARE, INCLUDING SOURCE AND DERIVED BINARY CODE. BY DOWNLOADING THIS SOFTWARE YOU AGREE TO THE FOLLOWING LICENSE:
 * 
 * Subject to the terms and conditions of this Agreement including, Clinovo grants you a non-exclusive, non-transferable, non-sublicenseable limited license without license fees to reproduce and use internally the software complete and unmodified for the sole purpose of running Programs on one computer. 
 * This license does not allow for the commercial use of this software except by IRS approved non-profit organizations; educational entities not working in joint effort with for profit business.
 * To use the license for other purposes, including for profit clinical trials, an additional paid license is required. Please contact our licensing department at http://www.clincapture.com/contact for pricing information.
 * 
 * You may not modify, decompile, or reverse engineer the software.
 * Clinovo disclaims any express or implied warranty of fitness for use. 
 * No right, title or interest in or to any trademark, service mark, logo or trade name of Clinovo or its licensors is granted under this Agreement.
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. CLINOVO FURTHER DISCLAIMS ALL WARRANTIES, EXPRESS AND IMPLIED, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.

 * LIMITATION OF LIABILITY. IN NO EVENT SHALL CLINOVO BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, PUNITIVE OR CONSEQUENTIAL DAMAGES, OR DAMAGES FOR LOSS OF PROFITS, REVENUE, DATA OR DATA USE, INCURRED BY YOU OR ANY THIRD PARTY, WHETHER IN AN ACTION IN CONTRACT OR TORT, EVEN IF ORACLE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. CLINOVOâ€™S ENTIRE LIABILITY FOR DAMAGES HEREUNDER SHALL IN NO EVENT EXCEED TWO HUNDRED DOLLARS (U.S. $200).
 *******************************************************************************/
package com.clinovo.dao;

import java.util.List;

import org.akaza.openclinica.dao.hibernate.AbstractDomainDao;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.clinovo.model.Widget;

/**
 * This class is the database interface for widgets.
 */

@Repository
@SuppressWarnings("unchecked")
public class WidgetDAO extends AbstractDomainDao<Widget> {

	@Override
	public Class<Widget> domainClass() {
		return Widget.class;
	}

	/**
	 * Retrieves all the widgets from the database.
	 * 
	 * @return List of all widgets
	 */
	public List<Widget> findAll() {
		String query = "from  " + this.getDomainClassName();
		Query q = this.getCurrentSession().createQuery(query);
		return (List<Widget>) q.setCacheable(true).list();
	}

	/**
	 * Retrieves widgets selected by id.
	 * 
	 * @param id
	 *            The ID of widget to filter on.
	 * @return Widget selected by id
	 */
	public Widget findById(int id) {
		String query = "from " + getDomainClassName() + " wi where wi.id = :id";
		Query q = getCurrentSession().createQuery(query);
		q.setInteger("id", id);
		return (Widget) q.setCacheable(true).uniqueResult();
	}

	/**
	 * Retrieves widget selected by child.
	 * 
	 * @param id
	 *            of widgetLayout, using which Widget should be found.
	 * @return Widget selected by id of layout.
	 */
	public Widget findByChildsId(int id) {
		String query = "select wi from " + getDomainClassName() + " wi join wi.widgetsLayout wl where wl.id = :id";
		Query q = getCurrentSession().createQuery(query);
		q.setInteger("id", id);
		return (Widget) q.setCacheable(true).uniqueResult();
	}
}
