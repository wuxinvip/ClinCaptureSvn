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
package org.akaza.openclinica.dao.managestudy;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupBean;
import org.akaza.openclinica.bean.managestudy.StudyGroupClassBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class StudyGroupDAO extends AuditableEntityDAO {

	protected void setQueryNames() {
		findAllByStudyName = "findAllByStudy";
		findByPKAndStudyName = "findByPKAndStudy";
	}

	public StudyGroupDAO(DataSource ds) {
		super(ds);
		setQueryNames();
	}

	public StudyGroupDAO(DataSource ds, DAODigester digester) {
		super(ds);
		this.digester = digester;
		setQueryNames();
	}

	@Override
	protected void setDigesterName() {
		digesterName = SQLFactory.getInstance().DAO_STUDYGROUP;
	}

	@Override
	public void setTypesExpected() {
		// study_group_id int4 ,
		// name varchar(255),
		// description varchar(1000),
		// study_group_class_id numeric,
		this.unsetTypeExpected();
		this.setTypeExpected(1, TypeNames.INT);
		this.setTypeExpected(2, TypeNames.STRING);
		this.setTypeExpected(3, TypeNames.STRING);
		this.setTypeExpected(4, TypeNames.INT);
	}

	/**
	 * <p>
	 * getEntityFromHashMap, the method that gets the object from the database query.
	 */
	public Object getEntityFromHashMap(HashMap hm) {
		StudyGroupBean eb = new StudyGroupBean();
		eb.setId(((Integer) hm.get("study_group_id")).intValue());
		eb.setName((String) hm.get("name"));
		eb.setDescription((String) hm.get("description"));
		eb.setStudyGroupClassId(((Integer) hm.get("study_group_class_id")).intValue());

		return eb;
	}

	public Collection findAll() {
		this.setTypesExpected();
		ArrayList alist = this.select(digester.getQuery("findAll"));
		ArrayList al = new ArrayList();
		Iterator it = alist.iterator();
		while (it.hasNext()) {
			StudyGroupBean eb = (StudyGroupBean) this.getEntityFromHashMap((HashMap) it.next());
			al.add(eb);
		}
		return al;
	}

	public List<StudyGroupBean> findAllByGroupClass(StudyGroupClassBean group) {
		List<StudyGroupBean> answer = new ArrayList();
		this.setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(new Integer(1), new Integer(group.getId()));

		ArrayList alist = this.select(digester.getQuery("findAllByGroupClass"), variables);

		Iterator it = alist.iterator();
		while (it.hasNext()) {
			HashMap hm = (HashMap) it.next();
			StudyGroupBean g = (StudyGroupBean) this.getEntityFromHashMap(hm);
			answer.add(g);
		}

		return answer;
	}

	public ArrayList getGroupByStudySubject(int studySubjectId, int studyId, int parentStudyId) {
		ArrayList answer = new ArrayList();
		this.setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(1, studySubjectId);
		variables.put(2, studyId);
		variables.put(3, parentStudyId);

		ArrayList alist = this.select(digester.getQuery("getGroupByStudySubject"), variables);

		Iterator it = alist.iterator();
		while (it.hasNext()) {
			HashMap hm = (HashMap) it.next();
			StudyGroupBean g = (StudyGroupBean) this.getEntityFromHashMap(hm);
			answer.add(g);
		}

		return answer;
	}

	public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
		ArrayList al = new ArrayList();

		return al;
	}

	public StudyGroupBean findByPK(int id) {
		StudyGroupBean eb = new StudyGroupBean();
		this.setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(new Integer(1), new Integer(id));

		String sql = digester.getQuery("findByPK");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();

		if (it.hasNext()) {
			eb = (StudyGroupBean) this.getEntityFromHashMap((HashMap) it.next());
		}

		return eb;
	}

	public EntityBean findByStudyId(int studyId) {
		StudyGroupBean eb = new StudyGroupBean();
		this.setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(new Integer(1), new Integer(studyId));

		String sql = digester.getQuery("findByStudyId");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();

		if (it.hasNext()) {
			eb = (StudyGroupBean) this.getEntityFromHashMap((HashMap) it.next());
		}

		return eb;
	}

	/*
	 * created for Extract Bean, we give the method a study subject bean and it returns for us a hash map of class ids
	 * pointing to study group beans, tbh July 2007
	 */
	public HashMap findByStudySubject(StudySubjectBean studySubject) {
		HashMap classBeanMap = new HashMap();
		HashMap variables = new HashMap();

		variables.put(new Integer(1), new Integer(studySubject.getId()));

		String sql = digester.getQuery("findByStudySubject");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();

		if (it.hasNext()) {
			StudyGroupBean sgbean = (StudyGroupBean) this.getEntityFromHashMap((HashMap) it.next());
			classBeanMap.put(new Integer(sgbean.getStudyGroupClassId()), sgbean);
		}
		return classBeanMap;
	}

	public HashMap findSubjectGroupMaps(int studyId) {
		logger.info("testing with variable: " + studyId);
		HashMap subjectGroupMaps = new HashMap();
		ArrayList groupMaps = new ArrayList();
		HashMap subjectGroupMap = new HashMap();
		HashMap variables = new HashMap();

		this.setTypesExpected();
		this.setTypeExpected(5, TypeNames.INT);

		variables.put(new Integer(1), new Integer(studyId));

		String sql = digester.getQuery("findSubjectGroupMaps");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();

		while (it.hasNext()) {
			logger.info("iteration over answers...");
			subjectGroupMap = new HashMap();

			HashMap answers = (HashMap) it.next();

			Integer studySubjectId = (Integer) answers.get("study_subject_id");
			logger.info("iteration over answers..." + studySubjectId.intValue());
			if (subjectGroupMaps.containsKey(studySubjectId)) {
				groupMaps = (ArrayList) subjectGroupMaps.get(studySubjectId);
			} else {
				groupMaps = new ArrayList();
			}
			StudyGroupBean sgbean = (StudyGroupBean) this.getEntityFromHashMap(answers);

			subjectGroupMap.put(new Integer(sgbean.getStudyGroupClassId()), sgbean);
			groupMaps.add(subjectGroupMap);
			logger.info("subjectgroupmaps: just put in " + sgbean.getStudyGroupClassId());
			subjectGroupMaps.put(studySubjectId, groupMaps);
		}
		return subjectGroupMaps;
	}

	/**
	 * Creates a new StudyGroup
	 */
	public EntityBean create(EntityBean eb) {
		StudyGroupBean sb = (StudyGroupBean) eb;
		HashMap variables = new HashMap();

		variables.put(new Integer(1), sb.getName());
		variables.put(new Integer(2), sb.getDescription());
		variables.put(new Integer(3), new Integer(sb.getStudyGroupClassId()));

		executeWithPK(digester.getQuery("create"), variables);
		if (isQuerySuccessful()) {
			eb.setId(getLatestPK());
		}
		return sb;
	}

	/**
	 * Updates a StudyGroup
	 */
	public EntityBean update(EntityBean eb) {
		StudyGroupBean sb = (StudyGroupBean) eb;
		HashMap variables = new HashMap();

		// UPDATE study_group SET study_group_class_id=?, name=?,
		// description=?
		// WHERE study_group_id=?
		variables.put(new Integer(1), new Integer(sb.getStudyGroupClassId()));
		variables.put(new Integer(2), sb.getName());
		variables.put(new Integer(3), sb.getDescription());
		variables.put(new Integer(4), new Integer(sb.getId()));

		String sql = digester.getQuery("update");
		this.execute(sql, variables);

		return sb;
	}

	public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
			boolean blnAscendingSort, String strSearchPhrase) {
		ArrayList al = new ArrayList();

		return al;
	}

	public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
		ArrayList al = new ArrayList();

		return al;
	}

	public StudyGroupBean findByNameAndGroupClassID(String name, int studyGroupClassId) {
		StudyGroupBean eb = new StudyGroupBean();
		this.setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(new Integer(1), name);
		variables.put(new Integer(2), new Integer(studyGroupClassId));

		String sql = digester.getQuery("findByNameAndGroupClassId");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();

		if (it.hasNext()) {
			eb = (StudyGroupBean) this.getEntityFromHashMap((HashMap) it.next());
		}

		return eb;
	}

	/**
	 * Deletes all SubjectGroups by StudyGroupClassId id.
	 *
	 * @param groupId the study group class id, to search on.
	 * if no records, matching the SQL query, were found, do nothing.
	 */
	public void deleteAllByStudyGroupClassId(int groupId) {
		HashMap variables = new HashMap();
		variables.put(1, groupId);
		this.execute(digester.getQuery("deleteAllByStudyGroupClassId"), variables);
	}
}
