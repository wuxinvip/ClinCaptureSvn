package com.clinovo.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.extract.DatasetBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.dao.extract.DatasetDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.clinovo.service.CRFMaskingService;
import com.clinovo.service.DatasetService;

/**
 * Implementation of DatasetService interface.
 */
@Service("datasetService")
@SuppressWarnings({"rawtypes", "unused", "unchecked"})
public class DatasetServiceImpl implements DatasetService {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private CRFMaskingService maskingService;

	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private DatasetDAO getDatasetDAO() {
		return new DatasetDAO(dataSource);
	}

	private void disableDataset(DatasetBean datasetBean, UserAccountBean updater, Status status) throws Exception {
		datasetBean.setStatus(status);
		datasetBean.setUpdater(updater);
		datasetBean.setUpdatedDate(new Date());
		getDatasetDAO().update(datasetBean);
	}

	private void enableDataset(DatasetBean datasetBean, UserAccountBean updater) throws Exception {
		datasetBean.setUpdater(updater);
		datasetBean.setUpdatedDate(new Date());
		datasetBean.setStatus(Status.AVAILABLE);
		getDatasetDAO().update(datasetBean);
	}

	private void disableDatasets(StudyBean studyBean, UserAccountBean updater, Status status) throws Exception {
		List<DatasetBean> datasetBeanList = getDatasetDAO().findAllByStudyId(studyBean);
		for (DatasetBean datasetBean : datasetBeanList) {
			if (datasetBean.getStatus().isAvailable()) {
				disableDataset(datasetBean, updater, status);
			}
		}
	}

	private void enableDatasets(StudyBean studyBean, UserAccountBean updater) throws Exception {
		List<DatasetBean> datasetBeanList = getDatasetDAO().findAllByStudyId(studyBean);
		for (DatasetBean datasetBean : datasetBeanList) {
			if (datasetBean.getStudyBean().getStatus().isAvailable()
					&& (datasetBean.getStatus().isLocked() || datasetBean.getStatus().isAutoDeleted())) {
				enableDataset(datasetBean, updater);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDataset(DatasetBean datasetBean, UserAccountBean updater) throws Exception {
		disableDataset(datasetBean, updater, Status.DELETED);
	}

	/**
	 * {@inheritDoc}
	 */
	public void restoreDataset(DatasetBean datasetBean, UserAccountBean updater) throws Exception {
		enableDataset(datasetBean, updater);
	}

	/**
	 * {@inheritDoc}
	 */
	public void lockDataset(DatasetBean datasetBean, UserAccountBean updater) throws Exception {
		disableDataset(datasetBean, updater, Status.LOCKED);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unlockDataset(DatasetBean datasetBean, UserAccountBean updater) throws Exception {
		enableDataset(datasetBean, updater);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeDatasets(StudyBean studyBean, UserAccountBean updater) throws Exception {
		disableDatasets(studyBean, updater, Status.AUTO_DELETED);
	}

	/**
	 * {@inheritDoc}
	 */
	public void restoreDatasets(StudyBean studyBean, UserAccountBean updater) throws Exception {
		enableDatasets(studyBean, updater);
	}

	/**
	 * {@inheritDoc}
	 */
	public void lockDatasets(StudyBean studyBean, UserAccountBean updater) throws Exception {
		disableDatasets(studyBean, updater, Status.LOCKED);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unlockDatasets(StudyBean studyBean, UserAccountBean updater) throws Exception {
		enableDatasets(studyBean, updater);
	}

	/**
	 * {@inheritDoc}
	 */
	public DatasetBean create(DatasetBean datasetBean) {
		return (DatasetBean) new DatasetDAO(dataSource).create(datasetBean);
	}

	/**
	 * {@inheritDoc}
	 */
	public DatasetBean initialDatasetData(int datasetId, UserAccountBean ub) {
		ItemDAO idao = new ItemDAO(dataSource);
		DatasetDAO datasetDAO = getDatasetDao();
		DatasetBean dataset = (DatasetBean) datasetDAO.findByPK(datasetId);
		List<String> excludeItems = new ArrayList<String>();
		List<String> eventsAndCrfs = new ArrayList<String>();
		if (dataset.getExcludeItems() != null && !dataset.getExcludeItems().trim().isEmpty()) {
			excludeItems = Arrays.asList(dataset.getExcludeItems().trim().split(","));
		}
		if (dataset.getSedIdAndCRFIdPairs() != null && !dataset.getSedIdAndCRFIdPairs().trim().isEmpty()) {
			eventsAndCrfs = Arrays.asList(dataset.getSedIdAndCRFIdPairs().trim().split(","));
		}
		String sql = dataset.getSQLStatement();
		sql = sql.split("study_event_definition_id in")[1];
		String[] ss = sql.split("and item_id in");
		String sedIds = ss[0];
		String[] sss = ss[1].split("and");
		String itemIds = sss[0];

		Map<Integer, ItemBean> itemBeanMap = new HashMap<Integer, ItemBean>();
		datasetDAO.setItemTypesExpected();
		ArrayList itemBeanList = datasetDAO.selectItemBeans(itemIds);
		for (Object row : itemBeanList) {
			ItemBean itemBean = (ItemBean) idao.getEntityFromHashMap((HashMap) row);
			itemBeanMap.put(itemBean.getId(), itemBean);
		}

		logger.debug("begin to execute GetDefinitionCrfItemSql");
		datasetDAO.setDefinitionCrfItemTypesExpected();
		ArrayList alist = datasetDAO.selectNotMaskedDefinitionCrfItemIds(ub.getId(), ub.getActiveStudyId(), sedIds,
				itemIds);
		for (Object anAlist : alist) {
			HashMap row = (HashMap) anAlist;
			boolean masked = row.get("masked") != null && (Integer) row.get("masked") > 0;
			if (!masked) {
				Integer itemId = (Integer) row.get("item_id");
				ItemBean ib = itemBeanMap.get(itemId).clone();
				Integer defId = (Integer) row.get("sed_id");
				String defName = (String) row.get("sed_name");
				String crfName = (String) row.get("crf_name");
				Integer crfVersionId = (Integer) row.get("cv_version_id");
				String crfVersionName = (String) row.get("cv_name");
				String key = defId + "_" + crfVersionId + "_" + itemId;
				if (!dataset.getItemMap().containsKey(key)) {
					ib.setDefId(defId);
					ib.setSelected(isItemSelected(key, excludeItems, eventsAndCrfs));
					ib.setDefName(defName);
					ib.setCrfName(crfName);
					ib.setDatasetItemMapKey(key);
					ItemFormMetadataBean imf = new ItemFormMetadataBean();
					imf.setCrfVersionName(crfVersionName);
					imf.setCrfVersionId(crfVersionId);
					ib.setItemMeta(imf);
					if (!dataset.getEventIds().contains(defId)) {
						dataset.getEventIds().add(defId);
					}
					dataset.getItemIds().add(itemId);
					dataset.getItemDefCrf().add(ib);
					dataset.getItemMap().put(key, ib);
				}
			} else if (itemShouldBeChecked(row, eventsAndCrfs)) {
				dataset.setContainsMaskedCRFs(true);
			}
		}
		dataset.setSubjectGroupIds(datasetDAO.getGroupIds(dataset.getId()));
		Collections.sort(dataset.getItemDefCrf(), new ItemBean.ItemBeanComparator());
		return dataset;
	}

	private boolean itemShouldBeChecked(HashMap row, List<String> eventsAndCRFs) {
		Integer defId = (Integer) row.get("sed_id");
		Integer crfVersionId = (Integer) row.get("cv_version_id");
		String keyEventCRF = defId + "_" + crfVersionId;
		return eventsAndCRFs.size() != 0 && eventsAndCRFs.contains(keyEventCRF);
	}

	private boolean isItemSelected(String key, List<String> excludeItems, List<String> eventsAndCRFs) {
		String[] arguments = key.split("_");
		String keyEventCRF = arguments[0] + "_" + arguments[1];
		return eventsAndCRFs.size() != 0
				? eventsAndCRFs.contains(keyEventCRF) && !excludeItems.contains(key)
				: !excludeItems.contains(key);
	}

	private boolean isItemFromMaskedCRFs(HashMap map, UserAccountBean ub) {
		if (ub != null) {
			int crfId = (Integer) map.get("crf_id");
			int sedId = (Integer) map.get("sed_id");
			EventDefinitionCRFDAO edcDao = new EventDefinitionCRFDAO(dataSource);
			EventDefinitionCRFBean edcBean = edcDao.findByStudyEventDefinitionIdAndCRFIdAndStudyId(sedId, crfId,
					ub.getActiveStudyId());
			return maskingService.isEventDefinitionCRFMasked(edcBean.getId(), ub.getId(), ub.getActiveStudyId());
		}
		return false;
	}

	private DatasetDAO getDatasetDao() {
		return new DatasetDAO(dataSource);
	}
}
