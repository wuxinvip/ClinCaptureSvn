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
 * Copyright 2003-2008 Akaza Research
 *
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 */
package org.akaza.openclinica.dao.submit;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;

import com.clinovo.enums.study.StudyOrigin;

/**
 * <p>
 * CRFVersionDAO.java, the data access object for versions of instruments in the database. Each of these are related to
 * Sections, a versioning map that links them with Items, and an Event, which then links to a Study.
 * 
 * @author thickerson
 */
@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public class CRFVersionDAO extends AuditableEntityDAO implements ICRFVersionDAO {

	@Override
	protected void setDigesterName() {
		digesterName = SQLFactory.getInstance().DAO_CRFVERSION;
	}

	/**
	 * 
	 * @param ds
	 *            DataSource
	 */
	public CRFVersionDAO(DataSource ds) {
		super(ds);
	}

	/**
	 * 
	 * @param ds
	 *            DataSource
	 * @param digester
	 *            DAODigester
	 */
	public CRFVersionDAO(DataSource ds, DAODigester digester) {
		super(ds);
		this.digester = digester;
	}

	/**
	 * This constructor sets up the Locale for JUnit tests; see the locale member variable in EntityDAO, and its
	 * initializeI18nStrings() method.
	 * 
	 * @param ds
	 *            DataSource
	 * @param digester
	 *            DAODigester
	 * @param locale
	 *            Locale
	 */
	public CRFVersionDAO(DataSource ds, DAODigester digester, Locale locale) {

		this(ds, digester);
		this.locale = locale;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityBean update(EntityBean eb) {
		// UPDATE CRF_VERSION SET CRF_ID=?,STATUS_ID=?,NAME=?,
		// DESCRIPTION=?,DATE_UPDATED=NOW(),UPDATE_ID=?,REVISION_NOTES =? WHERE
		// CRF_VERSION_ID=?
		int index = 1;
		CRFVersionBean ib = (CRFVersionBean) eb;
		HashMap variables = new HashMap();
		variables.put(index++, ib.getCrfId());
		variables.put(index++, ib.getStatus().getId());
		variables.put(index++, ib.getName());
		variables.put(index++, ib.getDescription());
		variables.put(index++, ib.getUpdater().getId());
		variables.put(index++, ib.getRevisionNotes());
		variables.put(index++, ib.getId());
		this.execute(digester.getQuery("update"), variables);
		return eb;
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityBean create(EntityBean eb) {
		return create(eb, null);
	}

	/**
	 * Creates new CRFVersionBean.
	 *
	 * @param eb
	 *            EntityBean
	 * @param con
	 *            Connection
	 * @return EntityBean
	 */
	public EntityBean create(EntityBean eb, Connection con) {
		int index = 1;
		CRFVersionBean cb = (CRFVersionBean) eb;
		cb.setOid(getValidOid(cb, cb.getDescription(), cb.getName()));
		HashMap variables = new HashMap();
		variables.put(index++, cb.getCrfId());
		variables.put(index++, cb.getStatus().getId());
		variables.put(index++, cb.getName());
		variables.put(index++, cb.getDescription());
		variables.put(index++, cb.getOwner().getId());
		variables.put(index++, cb.getOid());
		variables.put(index, cb.getRevisionNotes());
		executeWithPK(digester.getQuery("create"), variables, null, con);
		if (isQuerySuccessful()) {
			cb.setId(getLatestPK());
		}
		return cb;
	}

	@Override
	public void setTypesExpected() {
		int index = 1;
		this.unsetTypeExpected();
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index++, TypeNames.STRING);
		this.setTypeExpected(index++, TypeNames.STRING);

		this.setTypeExpected(index++, TypeNames.STRING);
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index++, TypeNames.TIMESTAMP);

		this.setTypeExpected(index++, TypeNames.TIMESTAMP);
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index, TypeNames.STRING);

	}

	/**
	 * {@inheritDoc}
	 */
	public Object getEntityFromHashMap(HashMap hm) {
		CRFVersionBean eb = new CRFVersionBean();
		super.setEntityAuditInformation(eb, hm);
		eb.setId((Integer) hm.get("crf_version_id"));

		eb.setName((String) hm.get("name"));
		eb.setDescription((String) hm.get("description"));
		eb.setCrfId((Integer) hm.get("crf_id"));
		eb.setRevisionNotes((String) hm.get("revision_notes"));
		eb.setOid((String) hm.get("oc_oid"));
		eb.setStatusId((Integer) hm.get("status_id"));

		String crfName = (String) hm.get("crf_name");
		if (crfName != null) {
			eb.setCrfName(crfName);
		}

		return eb;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CRFVersionBean> findAll() {
		this.setTypesExpected();
		ArrayList alist = this.select(digester.getQuery("findAll"));
		ArrayList al = new ArrayList();
		for (Object anAlist : alist) {
			CRFVersionBean eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) anAlist);
			al.add(eb);
		}
		return al;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
		return new ArrayList();
	}

	/**
	 * 
	 * @param crfId
	 *            CRF Id
	 * @return Collection of CRF Versions
	 */
	public Collection findAllByCRF(int crfId) {
		this.setTypesExpected();
		HashMap variables = new HashMap();
		variables.put(1, crfId);
		String sql = digester.getQuery("findAllByCRF");
		ArrayList alist = this.select(sql, variables);
		ArrayList al = new ArrayList();
		for (Object anAlist : alist) {
			CRFVersionBean eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) anAlist);
			al.add(eb);
		}
		return al;
	}

	/**
	 * 
	 * @param crfId
	 *            CRF Id
	 * @return Collection of CRF Versions
	 */
	public Collection findAllActiveByCRF(int crfId) {
		this.setTypesExpected();
		HashMap variables = new HashMap();
		variables.put(1, crfId);
		String sql = digester.getQuery("findAllActiveByCRF");
		ArrayList alist = this.select(sql, variables);
		ArrayList al = new ArrayList();
		for (Object anAlist : alist) {
			CRFVersionBean eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) anAlist);
			al.add(eb);
		}
		return al;
	}

	/**
	 * 
	 * @param versionId
	 *            CRF Version Id
	 * @return Collection of Items
	 */
	public Collection findItemFromMap(int versionId) {
		int index = 1;
		this.unsetTypeExpected();
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index++, TypeNames.STRING);
		this.setTypeExpected(index++, TypeNames.INT);
		HashMap variables = new HashMap();
		variables.put(1, versionId);
		String sql = digester.getQuery("findItemFromMap");
		ArrayList alist = this.select(sql, variables);
		ArrayList al = new ArrayList();
		for (Object anAlist : alist) {
			ItemBean eb = new ItemBean();
			HashMap hm = (HashMap) anAlist;
			eb.setId((Integer) hm.get("item_id"));
			eb.setName((String) hm.get("name"));
			Integer ownerId = (Integer) hm.get("owner_id");
			eb.setOwnerId(ownerId);

			al.add(eb);
		}
		return al;
	}

	/**
	 * 
	 * @param versionId
	 *            CRF Version Id
	 * @return Collection of Items
	 */
	public Collection findItemUsedByOtherVersion(int versionId) {
		int index = 1;
		this.unsetTypeExpected();
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index++, TypeNames.STRING);
		this.setTypeExpected(index++, TypeNames.INT);
		HashMap variables = new HashMap();
		variables.put(1, versionId);
		String sql = digester.getQuery("findItemUsedByOtherVersion");
		ArrayList alist = this.select(sql, variables);
		ArrayList al = new ArrayList();
		for (Object anAlist : alist) {
			ItemBean eb = new ItemBean();
			HashMap hm = (HashMap) anAlist;
			eb.setId((Integer) hm.get("item_id"));
			eb.setName((String) hm.get("name"));
			eb.setOwnerId((Integer) hm.get("owner_id"));
			al.add(eb);
		}
		return al;
	}

	/**
	 * 
	 * @param versionId
	 *            CRF Version Id
	 * @return List of unshared Items
	 */
	public ArrayList findNotSharedItemsByVersion(int versionId) {
		int index = 1;
		this.unsetTypeExpected();
		this.setTypeExpected(index++, TypeNames.INT);
		this.setTypeExpected(index++, TypeNames.STRING);
		this.setTypeExpected(index++, TypeNames.INT);
		HashMap variables = new HashMap();
		variables.put(1, versionId);
		variables.put(2, versionId);
		String sql = digester.getQuery("findNotSharedItemsByVersion");
		ArrayList alist = this.select(sql, variables);
		ArrayList al = new ArrayList();
		for (Object anAlist : alist) {
			ItemBean eb = new ItemBean();
			HashMap hm = (HashMap) anAlist;
			eb.setId((Integer) hm.get("item_id"));
			eb.setName((String) hm.get("name"));
			eb.setOwnerId((Integer) hm.get("owner_id"));
			al.add(eb);
		}
		return al;
	}

	/**
	 * 
	 * @param versionId
	 *            CRF Version Id
	 * @return True if item is used by other version, false otherwise
	 */
	public boolean isItemUsedByOtherVersion(int versionId) {
		this.setTypesExpected();
		HashMap variables = new HashMap();
		variables.put(1, versionId);
		String sql = digester.getQuery("isItemUsedByOtherVersion");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();
		return it.hasNext();
	}

	/**
	 * 
	 * @param itemId
	 *            Item to check
	 * @return True if item has data, false otherwise
	 */
	public boolean hasItemData(int itemId) {
		this.setTypesExpected();
		HashMap variables = new HashMap();
		variables.put(1, itemId);
		String sql = digester.getQuery("hasItemData");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();
		return it.hasNext();
	}

	/**
	 * {@inheritDoc}
	 */
	public EntityBean findByPK(int id) {
		CRFVersionBean eb = new CRFVersionBean();
		this.setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(1, id);

		String sql = digester.getQuery("findByPK");
		ArrayList alist = this.select(sql, variables);
		Iterator it = alist.iterator();

		if (it.hasNext()) {
			eb = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
		}
		return eb;

	}

	/**
	 * Method that finds crf version by crf version name, crf name and study.
	 * 
	 * @param version
	 *            CRF version
	 * @param crfName
	 *            Name of CRF
	 * @param studyBean
	 *            StudyBean
	 * @return CRFVersionBean
	 */
	public EntityBean findByFullNameAndStudy(String version, String crfName, StudyBean studyBean) {
		String sql;
		int index = 1;
		setTypesExpected();
		CRFVersionBean crfVersionBean = new CRFVersionBean();

		HashMap variables = new HashMap();
		variables.put(index++, version.trim());
		variables.put(index++, crfName.trim());

		if (studyBean.getOrigin().equalsIgnoreCase(StudyOrigin.STUDIO.getName())) {
			sql = digester.getQuery("findByFullNameAndStudy");
			variables.put(index, studyBean.getParentStudyId() > 0 ? studyBean.getParentStudyId() : studyBean.getId());
		} else {
			sql = digester.getQuery("findByFullNameInGUIStudies");
		}

		List<HashMap> mapList = select(sql, variables);
		Iterator<HashMap> mapIterator = mapList.iterator();
		if (mapIterator.hasNext()) {
			crfVersionBean = (CRFVersionBean) getEntityFromHashMap(mapIterator.next());
		}
		return crfVersionBean;
	}

	/**
	 * Generates all the delete queries for deleting a version.
	 * 
	 * @param versionId
	 *            CRF Version
	 * @param items
	 *            ArrayList
	 * @return List of deleted queries
	 */
	private ArrayList generateDeleteQueries(int versionId, ArrayList items) {
		ArrayList sqls = new ArrayList();
		String sql = digester.getQuery("deleteScdItemMetadataByVersion") + versionId + ")";
		sqls.add(sql);
		sql = digester.getQuery("deleteItemMetaDataByVersion") + versionId;
		sqls.add(sql);
		sql = digester.getQuery("deleteSectionsByVersion") + versionId;
		sqls.add(sql);
		sql = digester.getQuery("deleteItemMapByVersion") + versionId;
		sqls.add(sql);
		sql = digester.getQuery("deleteResponseSetsByVersion") + versionId;
		sqls.add(sql);

		sql = digester.getQuery("deleteItemGroupMetaByVersion") + versionId;
		sqls.add(sql);

		for (Object item1 : items) {
			ItemBean item = (ItemBean) item1;
			sql = digester.getQuery("deleteNotSharedItemDataByVersion") + item.getId();
			sqls.add(sql);
		}

		for (Object item1 : items) {
			ItemBean item = (ItemBean) item1;
			sql = digester.getQuery("deleteItemsByVersion") + item.getId();
			sqls.add(sql);
		}

		sql = digester.getQuery("deleteResponseSetByVersion") + versionId;
		sqls.add(sql);

		sql = digester.getQuery("deleteEventCrfByVersion") + versionId;
		sqls.add(sql);

		sql = digester.getQuery("delete") + versionId;
		sqls.add(sql);
		return sqls;

	}

	/**
	 * Deletes crf version. Note: it will be better to use the DeleteCrfService.deleteCrfVersion(int crfVersionId)
	 * instead of this method, cuz service has additional logic.
	 * 
	 * @param crfVersionBean
	 *            CRFVersionBean
	 * @param items
	 *            ArrayList
	 */
	public void deleteCrfVersion(CRFVersionBean crfVersionBean, ArrayList items) {
		Statement statement = null;
		Connection connection = null;
		try {
			connection = ds.getConnection();
			connection.setAutoCommit(false);
			List<String> queryList = (List<String>) generateDeleteQueries(crfVersionBean.getId(), items);
			for (String query : queryList) {
				statement = connection.createStatement();
				statement.executeUpdate(query);
				statement.close();
			}
			connection.commit();
		} catch (Exception ex) {
			logger.error("Error has occurred.", ex);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (Exception ex) {
				logger.error("Error has occurred.", ex);
			}
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (Exception ex) {
				logger.error("Error has occurred.", ex);
			}
		}
	}

	private String getOid(CRFVersionBean crfVersion, String crfName, String crfVersionName) {

		String oid;
		try {
			oid = crfVersion.getOid() != null
					? crfVersion.getOid()
					: crfVersion.getOidGenerator(ds).generateOid(crfName, crfVersionName);
			return oid;
		} catch (Exception e) {
			throw new RuntimeException("CANNOT GENERATE OID");
		}
	}

	/**
	 * 
	 * @param crfVersion
	 *            CRFVersionBean
	 * @param crfName
	 *            CRF Name
	 * @param crfVersionName
	 *            CRF Version Name
	 * @return CRF Version OID
	 */
	public String getValidOid(CRFVersionBean crfVersion, String crfName, String crfVersionName) {

		String oid = getOid(crfVersion, crfName, crfVersionName);
		logger.info(oid);
		String oidPreRandomization = oid;
		while (findAllByOid(oid).size() > 0) {
			oid = crfVersion.getOidGenerator(ds).randomizeOid(oidPreRandomization);
		}
		return oid;

	}

	/**
	 * {@inheritDoc}
	 */
	public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn,
			boolean blnAscendingSort, String strSearchPhrase) {
		return new ArrayList();
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
		return new ArrayList();
	}

	/**
	 * 
	 * @param oid
	 *            OC OID
	 * @return List of CRF Versions
	 */
	public ArrayList findAllByOid(String oid) {
		HashMap variables = new HashMap();
		variables.put(1, oid);

		return executeFindAllQuery("findAllByOid", variables);
	}

	/**
	 * 
	 * @param crfVersionId
	 *            CRF Version Id
	 * @return CRF Id
	 */
	public int getCRFIdFromCRFVersionId(int crfVersionId) {
		int answer = 0;

		this.unsetTypeExpected();
		this.setTypeExpected(1, TypeNames.INT);

		HashMap variables = new HashMap();
		variables.put(1, crfVersionId);

		String sql = digester.getQuery("getCRFIdFromCRFVersionId");
		ArrayList rows = select(sql, variables);

		if (rows.size() > 0) {
			HashMap h = (HashMap) rows.get(0);
			answer = (Integer) h.get("crf_id");
		}
		return answer;
	}

	/**
	 * 
	 * @param crfId
	 *            CRF Id
	 * @return List of CRF Versions
	 */
	public ArrayList findAllByCRFId(int crfId) {
		HashMap variables = new HashMap();
		variables.put(1, crfId);

		return executeFindAllQuery("findAllByCRFId", variables);
	}

	/**
	 * Returns all crf versions by study ud.
	 * 
	 * @param studyId
	 *            int
	 * @return List of CRF Versions
	 */
	public List<CRFVersionBean> findAllByStudyId(int studyId) {
		List<CRFVersionBean> crfVersionList = new ArrayList<CRFVersionBean>();
		int index = 1;
		unsetTypeExpected();
		setTypeExpected(index++, TypeNames.INT);
		setTypeExpected(index++, TypeNames.INT);
		setTypeExpected(index++, TypeNames.STRING);
		setTypeExpected(index++, TypeNames.STRING);
		setTypeExpected(index++, TypeNames.STRING);
		setTypeExpected(index++, TypeNames.INT);
		setTypeExpected(index++, TypeNames.TIMESTAMP);
		setTypeExpected(index++, TypeNames.TIMESTAMP);
		setTypeExpected(index++, TypeNames.INT);
		setTypeExpected(index++, TypeNames.INT);
		setTypeExpected(index++, TypeNames.STRING);
		setTypeExpected(index, TypeNames.STRING);
		HashMap variables = new HashMap();
		variables.put(1, studyId);
		String sql = digester.getQuery("findAllByStudyId");
		List<HashMap> rows = select(sql, variables);
		for (HashMap map : rows) {
			crfVersionList.add((CRFVersionBean) getEntityFromHashMap(map));
		}
		return crfVersionList;
	}

	/**
	 * 
	 * @param crfId
	 *            CRF Id
	 * @param versionName
	 *            CRF Version Name
	 * @return CRF Version Id
	 */
	public Integer findCRFVersionId(int crfId, String versionName) {
		this.unsetTypeExpected();
		this.setTypeExpected(1, TypeNames.INT);
		HashMap variables = new HashMap();
		variables.put(1, crfId);
		variables.put(2, versionName);
		ArrayList result = this.select(digester.getQuery("findCRFVersionId"), variables);
		HashMap map;
		Integer crfVersionId = null;
		if (result.iterator().hasNext()) {
			map = (HashMap) result.iterator().next();
			crfVersionId = (Integer) map.get("crf_version_id");
		}
		return crfVersionId;
	}

	/**
	 * 
	 * @param oid
	 *            OC OID
	 * @return CRF Version
	 */
	public CRFVersionBean findByOid(String oid) {
		CRFVersionBean crfVersionBean;
		this.unsetTypeExpected();
		setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(1, oid);
		String sql = digester.getQuery("findByOID");

		ArrayList rows = this.select(sql, variables);
		Iterator it = rows.iterator();

		if (it.hasNext()) {
			crfVersionBean = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
			return crfVersionBean;
		} else {
			return null;
		}
	}

	/**
	 * Find new CRF Version instead of deleted.
	 * 
	 * @param deletedCRFVersionId
	 *            int
	 * @return CRFVersionBean to be set instead of deleted
	 */
	public CRFVersionBean findLatestAfterDeleted(int deletedCRFVersionId) {

		CRFVersionBean crfVersionBean;
		this.unsetTypeExpected();
		setTypesExpected();

		HashMap variables = new HashMap();
		variables.put(1, deletedCRFVersionId);
		variables.put(2, deletedCRFVersionId);
		String sql = digester.getQuery("findLatestAfterDeleted");

		ArrayList rows = this.select(sql, variables);
		Iterator it = rows.iterator();

		if (it.hasNext()) {
			crfVersionBean = (CRFVersionBean) this.getEntityFromHashMap((HashMap) it.next());
			return crfVersionBean;
		} else {
			return null;
		}
	}
}
