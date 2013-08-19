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

package org.akaza.openclinica.web.crfdata;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.DataEntryStage;
import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.core.NullValue;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.DisplayItemBeanWrapper;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ResponseOptionBean;
import org.akaza.openclinica.bean.submit.crfdata.FormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ODMContainer;
import org.akaza.openclinica.bean.submit.crfdata.StudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean;
import org.akaza.openclinica.control.form.DiscrepancyValidator;
import org.akaza.openclinica.control.form.FormDiscrepancyNotes;
import org.akaza.openclinica.control.form.Validator;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.service.StudyConfigService;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.exception.OpenClinicaException;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ImportCRFDataService {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private final DataSource ds;

	private ItemDataDAO itemDataDao;

    private Locale locale;

	public static ResourceBundle respage;

    public static ResourceBundle resformat;

	public ImportCRFDataService(DataSource ds, Locale locale) {
		ResourceBundleProvider.updateLocale(locale);
		respage = ResourceBundleProvider.getPageMessagesBundle(locale);
        resformat = ResourceBundleProvider.getFormatBundle(locale);
        this.locale = locale;
		this.ds = ds;
	}

	public List<EventCRFBean> fetchEventCRFBeans(ODMContainer odmContainer, UserAccountBean ub) {
		ArrayList<EventCRFBean> eventCRFBeans = new ArrayList<EventCRFBean>();
		ArrayList<Integer> eventCRFBeanIds = new ArrayList<Integer>();
		EventCRFDAO eventCrfDAO = new EventCRFDAO(ds);
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
		StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
		StudyDAO studyDAO = new StudyDAO(ds);
        CRFDAO crfDAO = new CRFDAO(ds);
		StudyEventDAO studyEventDAO = new StudyEventDAO(ds);

		String studyOID = odmContainer.getCrfDataPostImportContainer().getStudyOID();
		StudyBean studyBean = studyDAO.findByOid(studyOID);
		ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
		for (SubjectDataBean subjectDataBean : subjectDataBeans) {
			ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

			StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(),
					studyBean.getId());
			for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
				ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();

				String sampleOrdinal = studyEventDataBean.getStudyEventRepeatKey() == null ? "1" : studyEventDataBean
						.getStudyEventRepeatKey();

				StudyEventDefinitionBean studyEventDefinitionBean = studyEventDefinitionDAO.findByOidAndStudy(
						studyEventDataBean.getStudyEventOID(), studyBean.getId(), studyBean.getParentStudyId());
				logger.info("find all by def and subject " + studyEventDefinitionBean.getName() + " study subject "
						+ studySubjectBean.getName());

				StudyEventBean studyEventBean = (StudyEventBean) studyEventDAO
						.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(),
								studyEventDefinitionBean.getId(), Integer.parseInt(sampleOrdinal));
				if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.LOCKED)
						|| studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SIGNED)
						|| studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.STOPPED)) {
					return null;
				}
				for (FormDataBean formDataBean : formDataBeans) {

					CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);

					ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formDataBean.getFormOID());
					for (CRFVersionBean crfVersionBean : crfVersionBeans) {
						CRFBean crfBean = ((CRFBean) crfDAO.findByPK(crfVersionBean.getCrfId()));
						ArrayList<EventCRFBean> eventCrfBeans = new EventCRFDAO(ds)
								.findAllByStudyEventAndCrfOrCrfVersionOid(studyEventBean, crfBean.getOid());
						// what if we have begun with creating a study
						// event, but haven't entered data yet? this would
						// have us with a study event, but no corresponding
						// event crf, yet.
						if (eventCrfBeans.isEmpty()) {
							logger.debug("   found no event crfs from Study Event id " + studyEventBean.getId()
									+ ", location " + studyEventBean.getLocation());
							// spell out criteria and create a bean if
							// necessary, avoiding false-positives
							if (studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.SCHEDULED)
									|| studyEventBean.getSubjectEventStatus().equals(
											SubjectEventStatus.DATA_ENTRY_STARTED)
									|| studyEventBean.getSubjectEventStatus().equals(SubjectEventStatus.COMPLETED)) {
								EventCRFBean newEventCrfBean = new EventCRFBean();
								newEventCrfBean.setStudyEventId(studyEventBean.getId());
								newEventCrfBean.setStudySubjectId(studySubjectBean.getId());
								newEventCrfBean.setCRFVersionId(crfVersionBean.getId());
								newEventCrfBean.setDateInterviewed(new Date());
								newEventCrfBean.setOwner(ub);
								newEventCrfBean.setInterviewerName(ub.getName());
								newEventCrfBean.setCompletionStatusId(1);// place
								// filler
								newEventCrfBean.setStatus(Status.AVAILABLE);
								newEventCrfBean.setStage(DataEntryStage.INITIAL_DATA_ENTRY);
								// these will be updated later in the
								// workflow
								newEventCrfBean = (EventCRFBean) eventCrfDAO.create(newEventCrfBean);
								eventCrfBeans.add(newEventCrfBean);
								logger.debug("   created and added new event crf");
							}
						} 

						for (EventCRFBean ecb : eventCrfBeans) {
							Integer ecbId = new Integer(ecb.getId());

							if (!eventCRFBeanIds.contains(ecbId)) {
								eventCRFBeans.add(ecb);
								eventCRFBeanIds.add(ecbId);
							}
						}
					}
				}
			}
		}
		// if it's null, throw an error, since they should be existing beans for
		// iteration one
		return eventCRFBeans;
	}

	@SuppressWarnings("unused")
	public SummaryStatsBean generateSummaryStatsBean(ODMContainer odmContainer, List<DisplayItemBeanWrapper> wrappers) {
		int countSubjects = 0;
		int countEventCRFs = 0;
		int discNotesGenerated = 0;
		for (DisplayItemBeanWrapper wr : wrappers) {
			HashMap validations = wr.getValidationErrors();
			discNotesGenerated += validations.size();
		}
		ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
		countSubjects += subjectDataBeans.size();
		for (SubjectDataBean subjectDataBean : subjectDataBeans) {
			ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();

			for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
				countEventCRFs += 1;
			}
		}

		SummaryStatsBean ssBean = new SummaryStatsBean();
		ssBean.setDiscNoteCount(discNotesGenerated);
		ssBean.setEventCrfCount(countEventCRFs);
		ssBean.setStudySubjectCount(countSubjects);
		return ssBean;
	}

	public List<DisplayItemBeanWrapper> lookupValidationErrors(HttpServletRequest request, ODMContainer odmContainer,
			UserAccountBean ub, HashMap<String, String> totalValidationErrors,
			HashMap<String, String> hardValidationErrors, ArrayList<Integer> permittedEventCRFIds)
			throws OpenClinicaException {

		DisplayItemBeanWrapper displayItemBeanWrapper = null;
		HashMap validationErrors = new HashMap();
		List<DisplayItemBeanWrapper> wrappers = new ArrayList<DisplayItemBeanWrapper>();
		ImportHelper importHelper = new ImportHelper();
		FormDiscrepancyNotes discNotes = new FormDiscrepancyNotes();
		DiscrepancyValidator discValidator = new DiscrepancyValidator(request, discNotes);
		// create a second Validator, this one for hard edit checks
		HashMap<String, String> hardValidator = new HashMap<String, String>();

		StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
		StudyDAO studyDAO = new StudyDAO(ds);
		StudyBean studyBean = studyDAO.findByOid(odmContainer.getCrfDataPostImportContainer().getStudyOID());
		StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
		StudyEventDefinitionDAO sedDao = new StudyEventDefinitionDAO(ds);
        StudyConfigService scs = new StudyConfigService(ds);
        studyBean = scs.setParametersForStudy(studyBean);
		int maxOrdinal = 1;
		HashMap<String, ItemDataBean> blankCheck = new HashMap<String, ItemDataBean>();
		ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();
		int totalEventCRFCount = 0;
		int totalItemDataBeanCount = 0;
		for (SubjectDataBean subjectDataBean : subjectDataBeans) {
			ArrayList<DisplayItemBean> displayItemBeans = new ArrayList<DisplayItemBean>();
			logger.debug("iterating through subject data beans: found " + subjectDataBean.getSubjectOID());
			ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
			totalEventCRFCount += studyEventDataBeans.size();

			StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(subjectDataBean.getSubjectOID(),
					studyBean.getId());

			for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
				// resetting max ordinal here, to try and stop 'multiple item creation'
				maxOrdinal = 1;
				int parentStudyId = studyBean.getParentStudyId();
				StudyEventDefinitionBean sedBean = sedDao.findByOidAndStudy(studyEventDataBean.getStudyEventOID(),
						studyBean.getId(), parentStudyId);
				ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
				logger.debug("iterating through study event data beans: found " + studyEventDataBean.getStudyEventOID());

				int ordinal = 1;
				try {
					ordinal = new Integer(studyEventDataBean.getStudyEventRepeatKey()).intValue();
				} catch (Exception e) {
					// trying to catch NPEs, because tags can be without the
					// repeat key
				}
				StudyEventBean studyEvent = (StudyEventBean) studyEventDAO
						.findByStudySubjectIdAndDefinitionIdAndOrdinal(studySubjectBean.getId(), sedBean.getId(),
								ordinal);

				displayItemBeans = new ArrayList<DisplayItemBean>();

				for (FormDataBean formDataBean : formDataBeans) {
					maxOrdinal = 1;// JN:Moving maxOrdinal here, so max ordinal is there per form rather than per study
									// eventData bean

                    CRFDAO crfDAO = new CRFDAO(ds);
					CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
					EventCRFDAO eventCRFDAO = new EventCRFDAO(ds);
					ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formDataBean.getFormOID());
					ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean.getItemGroupData();
					if ((crfVersionBeans == null) || (crfVersionBeans.size() == 0)) {
						MessageFormat mf = new MessageFormat("");
						mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
						Object[] arguments = { formDataBean.getFormOID() };

						throw new OpenClinicaException(mf.format(arguments), "");
					}
					CRFVersionBean crfVersion = crfVersionBeans.get(0);
					// if you have a mispelled form oid you get an error here
					// need to error out gracefully and post an error
					logger.debug("iterating through form beans: found " + crfVersion.getOid());
					// may be the point where we cut off item groups etc and
					// instead work on sections
					CRFBean crfBean = ((CRFBean) crfDAO.findByPK(crfVersion.getCrfId()));
					ArrayList<EventCRFBean> eventCrfBeans = eventCRFDAO.findAllByStudyEventAndCrfOrCrfVersionOid(
							studyEvent, crfBean.getOid());
					EventCRFBean eventCRFBean = eventCrfBeans.get(0);
					if (eventCRFBean.isNotStarted() && eventCRFBean.getCRFVersionId() != crfVersion.getId()) {
						eventCRFBean.setCRFVersionId(crfVersion.getId());
						eventCRFBean = (EventCRFBean) eventCRFDAO.update(eventCRFBean);
					}
					EventDefinitionCRFDAO eventDefinitionCRFDAO = new EventDefinitionCRFDAO(ds);
					EventDefinitionCRFBean eventDefinitionCRF = eventDefinitionCRFDAO
							.findByStudyEventIdAndCRFVersionId(studyBean, studyEvent.getId(), crfVersion.getId());
					if (permittedEventCRFIds.contains(new Integer(eventCRFBean.getId()))) {
						for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {

							ArrayList<ItemBean> blankCheckItems = new ArrayList<ItemBean>();
							ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean.getItemData();
							logger.debug("iterating through group beans: " + itemGroupDataBean.getItemGroupOID());
							// put a checker in here
							ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
							ItemGroupBean testBean = itemGroupDAO.findByOid(itemGroupDataBean.getItemGroupOID());
							if (testBean == null) {
								MessageFormat mf = new MessageFormat("");
								mf.applyPattern(respage.getString("your_item_group_oid_for_form_oid"));
								Object[] arguments = { itemGroupDataBean.getItemGroupOID(), formDataBean.getFormOID() };

								throw new OpenClinicaException(mf.format(arguments), "");
							}
							totalItemDataBeanCount += itemDataBeans.size();

							for (ImportItemDataBean importItemDataBean : itemDataBeans) {
								logger.debug("   iterating through item data beans: " + importItemDataBean.getItemOID());
								ItemDAO itemDAO = new ItemDAO(ds);
								ItemFormMetadataDAO itemFormMetadataDAO = new ItemFormMetadataDAO(ds);
								// ItemDataDAO itemDataDAO = new ItemDataDAO(ds);

								List<ItemBean> itemBeans = itemDAO.findByOid(importItemDataBean.getItemOID());
								if (!itemBeans.isEmpty()) {
									ItemBean itemBean = itemBeans.get(0);
                                    itemBean.setImportItemDataBean(importItemDataBean);
									logger.debug("   found " + itemBean.getName());
									DisplayItemBean displayItemBean = new DisplayItemBean();
									displayItemBean.setItem(itemBean);

									ArrayList<ItemFormMetadataBean> metadataBeans = itemFormMetadataDAO
											.findAllByItemId(itemBean.getId());
									logger.debug("      found metadata item beans: " + metadataBeans.size());
									int groupOrdinal = 1;
									if (itemGroupDataBean.getItemGroupRepeatKey() != null) {
										try {
											groupOrdinal = new Integer(itemGroupDataBean.getItemGroupRepeatKey())
													.intValue();
											if (groupOrdinal > maxOrdinal) {
												maxOrdinal = groupOrdinal;
											}
										} catch (Exception e) {
											logger.debug("found npe for group ordinals, line 344!");
										}
									}
									ItemDataBean itemDataBean = createItemDataBean(itemBean, eventCRFBean,
											importItemDataBean.getValue(), ub, groupOrdinal);
									blankCheckItems.add(itemBean);
									String newKey = groupOrdinal + "_" + itemGroupDataBean.getItemGroupOID() + "_"
											+ itemBean.getOid() + "_" + subjectDataBean.getSubjectOID();
									blankCheck.put(newKey, itemDataBean);
									logger.info("adding " + newKey + " to blank checks");
									if (!metadataBeans.isEmpty()) {
										ItemFormMetadataBean metadataBean = metadataBeans.get(0);
										// also
										// possible
										// nullpointer

										displayItemBean.setData(itemDataBean);
										displayItemBean.setMetadata(metadataBean);
										// set event def crf?
										displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
										String eventCRFRepeatKey = studyEventDataBean.getStudyEventRepeatKey();
										// if you do indeed leave off this in the XML it will pass but return 'null' tbh
										attachValidator(displayItemBean, importHelper, discValidator, hardValidator,
												request, eventCRFRepeatKey, studySubjectBean.getOid());
										checkExistingData(request, displayItemBean, itemBean, studyBean);
										displayItemBeans.add(displayItemBean);

									} else {
										MessageFormat mf = new MessageFormat("");
										mf.applyPattern(respage.getString("no_metadata_could_be_found"));
										Object[] arguments = { importItemDataBean.getItemOID() };

										throw new OpenClinicaException(mf.format(arguments), "");
									}
								} else {
									// report the error there
									MessageFormat mf = new MessageFormat("");
									mf.applyPattern(respage.getString("no_item_could_be_found"));
									Object[] arguments = { importItemDataBean.getItemOID() };

									throw new OpenClinicaException(mf.format(arguments), "");

								}
							}// end item data beans
							logger.debug(".. found blank check: " + blankCheck.toString());

							for (int i = 1; i <= maxOrdinal; i++) {
								for (ItemBean itemBean : blankCheckItems) {
									String newKey = i + "_" + itemGroupDataBean.getItemGroupOID() + "_"
											+ itemBean.getOid() + "_" + subjectDataBean.getSubjectOID();
									if (blankCheck.get(newKey) == null) {
										// if it already exists, Do Not Add It.
										ItemDataBean itemDataCheck = getItemDataDao()
												.findByItemIdAndEventCRFIdAndOrdinal(itemBean.getId(),
														eventCRFBean.getId(), i);
										logger.debug("found item data bean id: " + itemDataCheck.getId()
												+ " for ordinal " + i);
										if (itemDataCheck.getId() == 0) {
											ItemDataBean blank = createItemDataBean(itemBean, eventCRFBean, "", ub, i);
											DisplayItemBean displayItemBean = new DisplayItemBean();
											displayItemBean.setItem(itemBean);
											displayItemBean.setData(blank);
											// displayItemBean.setMetadata(metadataBean);
											// set event def crf?
											displayItemBean.setEventDefinitionCRF(eventDefinitionCRF);
											checkExistingData(request, displayItemBean, itemBean, studyBean);
											displayItemBeans.add(displayItemBean);
											logger.debug("... adding display item bean");
										}
									}
									logger.debug("found a blank at " + i + ", adding " + blankCheckItems.size()
											+ " blank items");
								}
							}
							blankCheckItems = new ArrayList<ItemBean>();
						}// end item group data beans

					}

					crfBean = crfDAO.findByVersionId(crfVersion.getCrfId());
					// seems like an extravagance, but is not contained in crf
					// version or event crf bean
					validationErrors = discValidator.validate("noregex"); // do not validate against regex provided in
																			// Excel template
					for (Object errorKey : validationErrors.keySet()) {
						if (!totalValidationErrors.containsKey(errorKey.toString()))
							totalValidationErrors.put(errorKey.toString(), validationErrors.get(errorKey).toString());
						logger.debug("+++ adding " + errorKey.toString());
					}
					logger.debug("-- hard validation checks: --");
					for (Object errorKey : hardValidator.keySet()) {
						logger.debug(errorKey.toString() + " -- " + hardValidator.get(errorKey));
						hardValidationErrors.put(errorKey.toString(), hardValidator.get(errorKey));
					}

					String studyEventId = studyEvent.getId() + "";
					String crfVersionId = crfVersion.getId() + "";

					logger.debug("creation of wrapper: original count of display item beans " + displayItemBeans.size()
							+ ", count of item data beans " + totalItemDataBeanCount + " count of validation errors "
							+ validationErrors.size() + " count of study subjects " + subjectDataBeans.size()
							+ " count of event crfs " + totalEventCRFCount + " count of hard error checks "
							+ hardValidator.size());
					// possibly create the import summary here
					logger.debug("creation of wrapper: max ordinal found " + maxOrdinal);
					// check if we need to overwrite
					DataEntryStage dataEntryStage = eventCRFBean.getStage();
					boolean overwrite = false;
					if (dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY_COMPLETE)
							|| dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY_COMPLETE)
							|| dataEntryStage.equals(DataEntryStage.INITIAL_DATA_ENTRY)
							|| dataEntryStage.equals(DataEntryStage.DOUBLE_DATA_ENTRY)) {
						overwrite = true;
					}
					displayItemBeanWrapper = new DisplayItemBeanWrapper(displayItemBeans, true, overwrite,
							validationErrors, studyEventId, crfVersionId, studyEventDataBean.getStudyEventOID(),
							studySubjectBean.getLabel(), eventCRFBean.getCreatedDate(), crfBean.getName(),
							crfVersion.getName(), studySubjectBean.getOid(),
							studyEventDataBean.getStudyEventRepeatKey());

					// JN: Commenting out the following code, since we shouldn't re-initialize at this point, as
					// validationErrors would get overwritten and the
					// older errors will be overriden. Moving it after the form.
					// Removing the comments for now, since it seems to be creating duplicate Discrepancy Notes.
					validationErrors = new HashMap();
					discValidator = new DiscrepancyValidator(request, discNotes);
					// reset to allow for new errors...
				}
				wrappers.add(displayItemBeanWrapper);
			}
		}

		return wrappers;
	}

	private void checkExistingData(HttpServletRequest request, DisplayItemBean displayItemBean, ItemBean itemBean, StudyBean currentStudy) {
		if (currentStudy.getStudyParameterConfig().getReplaceExisitingDataDuringImport().equals("no")) {
			ItemDataBean existingItemDataBean = getItemDataDao().findByItemIdAndEventCRFIdAndOrdinal(
					displayItemBean.getItem().getId(), displayItemBean.getData().getEventCRFId(),
					displayItemBean.getData().getOrdinal());
			if (existingItemDataBean != null && existingItemDataBean.getId() > 0
					&& existingItemDataBean.getValue() != null && !existingItemDataBean.getValue().isEmpty()) {
				displayItemBean.setSkip(true);
                request.setAttribute("hasSkippedItems", true);
				itemBean.getImportItemDataBean().setSkip(true);
			}
		}
	}
    
	private ItemDataBean createItemDataBean(ItemBean itemBean, EventCRFBean eventCrfBean, String value,
			UserAccountBean ub, int ordinal) {

		ItemDataBean itemDataBean = new ItemDataBean();
		itemDataBean.setItemId(itemBean.getId());
		itemDataBean.setEventCRFId(eventCrfBean.getId());
		itemDataBean.setCreatedDate(new Date());
		itemDataBean.setOrdinal(ordinal);
		itemDataBean.setOwner(ub);
		itemDataBean.setStatus(Status.UNAVAILABLE);
		itemDataBean.setValue(value);

		return itemDataBean;
	}

	private void attachValidator(DisplayItemBean displayItemBean, ImportHelper importHelper, DiscrepancyValidator v,
			HashMap<String, String> hardv, javax.servlet.http.HttpServletRequest request, String eventCRFRepeatKey,
			String studySubjectOID) throws OpenClinicaException {
		org.akaza.openclinica.bean.core.ResponseType rt = displayItemBean.getMetadata().getResponseSet()
				.getResponseType();
		String itemOid = displayItemBean.getItem().getOid() + "_" + eventCRFRepeatKey + "_"
				+ displayItemBean.getData().getOrdinal() + "_" + studySubjectOID;
		// note the above, generating an ordinal on top of the OID to view
		// errors. adding event crf repeat key here, tbh
		// need to add a per-subject differentator, what if its a diff subject
		// with the same item, repeat key and ordinal???

		if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXT)
				|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.TEXTAREA)) {
			ItemFormMetadataBean ifm = displayItemBean.getMetadata();
			String widthDecimal = ifm.getWidthDecimal();
			// what if it's a date? parse if out so that we go from iso 8601 to
			// mm/dd/yyyy
			if (displayItemBean.getItem().getDataType().equals(ItemDataType.DATE)) {
				if (!"".equals(displayItemBean.getData().getValue())) {
					String dateValue = displayItemBean.getData().getValue();
					SimpleDateFormat sdf_sqldate = new SimpleDateFormat("yyyy-MM-dd");
					try {
						sdf_sqldate.parse(dateValue);
						displayItemBean.getData().setValue(dateValue);
					} catch (ParseException pe) {
						try {
							/*
							 * here we are trying to parse the dates from the old XML files that were generated before
							 * the fix for the #414 we can remove it in a future
							 */
							sdf_sqldate = new SimpleDateFormat(resformat.getString("date_format_string"), locale);
							sdf_sqldate.parse(dateValue);
							displayItemBean.getData().setValue(dateValue);
						} catch (ParseException pe1) {
							// next version; fail if it does not pass iso 8601
							MessageFormat mf = new MessageFormat("");
							mf.applyPattern(respage.getString("you_have_a_date_value_which_is_not"));
							Object[] arguments = { displayItemBean.getItem().getOid() };

							hardv.put(itemOid, mf.format(arguments));

						}
					}
				}

			} else if (displayItemBean.getItem().getDataType().equals(ItemDataType.ST)) {
				int width = Validator.parseWidth(widthDecimal);
				if (width > 0 && displayItemBean.getData().getValue().length() > width) {
					hardv.put(itemOid, "This value exceeds required width=" + width);
				}
			}
			// what if it's a number? should be only numbers
			else if (displayItemBean.getItem().getDataType().equals(ItemDataType.INTEGER)) {
				try {
					int width = Validator.parseWidth(widthDecimal);
					if (width > 0 && displayItemBean.getData().getValue().length() > width) {
						hardv.put(itemOid, "This value exceeds required width=" + width);
					}
					// now, didn't check decimal for testInt.
				} catch (Exception e) {// should be a sub class
					if (!"".equals(displayItemBean.getData().getValue())) {
						hardv.put(itemOid, "This value is not an integer.");
					}
				}
			}
			// what if it's a float? should be only numbers
			else if (displayItemBean.getItem().getDataType().equals(ItemDataType.REAL)) {
				try {
					int width = Validator.parseWidth(widthDecimal);
					if (width > 0 && displayItemBean.getData().getValue().length() > width) {
						hardv.put(itemOid, "This value exceeds required width=" + width);
					}
					int decimal = Validator.parseDecimal(widthDecimal);
					if (decimal > 0
							&& BigDecimal.valueOf(new Double(displayItemBean.getData().getValue()).doubleValue())
									.scale() > decimal) {
						hardv.put(itemOid, "This value exceeds required decimal=" + decimal);
					}
				} catch (Exception ee) {
					if (!"".equals(displayItemBean.getData().getValue())) {
						hardv.put(itemOid, "This value is not a real number.");
					}
				}
			}

			request.setAttribute(itemOid, displayItemBean.getData().getValue());
			displayItemBean = importHelper.validateDisplayItemBeanText(v, displayItemBean, itemOid);

		} else if (rt.equals(ResponseType.CALCULATION) || rt.equals(ResponseType.GROUP_CALCULATION)) {
			if (displayItemBean.getItem().getDataType().equals(ItemDataType.REAL)) {
				ItemFormMetadataBean ifm = displayItemBean.getMetadata();
				String widthDecimal = ifm.getWidthDecimal();
				int decimal = Validator.parseDecimal(widthDecimal);
				if (decimal > 0) {
					try {
						Double d = new Double(displayItemBean.getData().getValue());
						if (BigDecimal.valueOf(d).scale() > decimal) {
							hardv.put(itemOid, "This value exceeds required decimal=" + decimal);
						}
					} catch (Exception e) {
						if (!"".equals(displayItemBean.getData().getValue())) {
							hardv.put(itemOid, "This value is not a real number.");
						}
					}
				}
			}
		}

		else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.RADIO)
				|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECT)) {
			String theValue = matchValueWithOptions(displayItemBean, displayItemBean.getData().getValue(),
					displayItemBean.getMetadata().getResponseSet().getOptions());
			request.setAttribute(itemOid, theValue);
			logger.debug("        found the value for radio/single: " + theValue);
			if (theValue == null && displayItemBean.getData().getValue() != null
					&& !displayItemBean.getData().getValue().isEmpty()) {
				logger.debug("-- theValue was NULL, the real value was " + displayItemBean.getData().getValue());
				hardv.put(itemOid, "This is not in the correct response set.");
			}
			displayItemBean = importHelper.validateDisplayItemBeanSingleCV(v, displayItemBean, itemOid);
		} else if (rt.equals(org.akaza.openclinica.bean.core.ResponseType.CHECKBOX)
				|| rt.equals(org.akaza.openclinica.bean.core.ResponseType.SELECTMULTI)) {
			String theValue = matchValueWithManyOptions(displayItemBean, displayItemBean.getData().getValue(),
					displayItemBean.getMetadata().getResponseSet().getOptions());
			request.setAttribute(itemOid, theValue);
			if (theValue == null && displayItemBean.getData().getValue() != null
					&& !displayItemBean.getData().getValue().isEmpty()) {
				hardv.put(itemOid, "This is not in the correct response set.");
			}
			displayItemBean = importHelper.validateDisplayItemBeanMultipleCV(v, displayItemBean, itemOid);
		}
	}

	private String matchValueWithOptions(DisplayItemBean displayItemBean, String value, List options) {
		String returnedValue = null;
		if (!options.isEmpty()) {
			for (Object responseOption : options) {
				ResponseOptionBean responseOptionBean = (ResponseOptionBean) responseOption;
				if (responseOptionBean.getValue().equals(value)) {
					displayItemBean.getData().setValue(((ResponseOptionBean) responseOption).getValue());
					return ((ResponseOptionBean) responseOption).getValue();

				}
			}
		}
		return returnedValue;
	}

	/*
	 * difference from the above is only a 'contains' in the place of an 'equals'. and a few other switches...also need
	 * to keep in mind that there are non-null values that need to be taken into account
	 */
	private String matchValueWithManyOptions(DisplayItemBean displayItemBean, String value, List options) {
		String returnedValue = null;
		String entireOptions = "";

		String[] simValues = value.split(",");

		String simValue = value.replace(",", "");
		// also remove all spaces, so they will fit up with the entire set
		// of options
		simValue = simValue.replace(" ", "");
		boolean checkComplete = true;
		if (!options.isEmpty()) {
			for (Object responseOption : options) {
				ResponseOptionBean responseOptionBean = (ResponseOptionBean) responseOption;
				entireOptions += responseOptionBean.getValue();

			}
			// remove spaces, since they are causing problems:
			entireOptions = entireOptions.replace(" ", "");
			// following may be superfluous, tbh

			ArrayList nullValues = displayItemBean.getEventDefinitionCRF().getNullValuesList();

			for (Object nullValue : nullValues) {
				NullValue nullValueTerm = (NullValue) nullValue;
				entireOptions += nullValueTerm.getName();
			}

			for (String sim : simValues) {
				sim = sim.replace(" ", "");
				checkComplete = entireOptions.contains(sim);
				if (!checkComplete) {
					return returnedValue;
				}
			}
		}
		return value;
	}

	public List<String> validateStudyMetadata(ODMContainer odmContainer, int currentStudyId) {
		List<String> errors = new ArrayList<String>();
		MessageFormat mf = new MessageFormat("");

		try {
			StudyDAO studyDAO = new StudyDAO(ds);
			String studyOid = odmContainer.getCrfDataPostImportContainer().getStudyOID();
			StudyBean studyBean = studyDAO.findByOid(studyOid);
			if (studyBean == null) {
				mf.applyPattern(respage.getString("your_study_oid_does_not_reference_an_existing"));
				Object[] arguments = { studyOid };

				errors.add(mf.format(arguments));
				logger.debug("unknown study OID");
				throw new OpenClinicaException("Unknown Study OID", "");

			} else if (studyBean.getId() != currentStudyId) {
				mf.applyPattern(respage.getString("your_current_study_is_not_the_same_as"));
				Object[] arguments = { studyBean.getName() };
				errors.add(mf.format(arguments));
			}
			ArrayList<SubjectDataBean> subjectDataBeans = odmContainer.getCrfDataPostImportContainer().getSubjectData();

            StudyEventDefinitionDAO studyEventDefinitionDAO = new StudyEventDefinitionDAO(ds);
            StudySubjectDAO studySubjectDAO = new StudySubjectDAO(ds);
            StudyEventDAO studyEventDAO = new StudyEventDAO(ds);
            CRFVersionDAO crfVersionDAO = new CRFVersionDAO(ds);
            ItemGroupDAO itemGroupDAO = new ItemGroupDAO(ds);
            EventCRFDAO eventCRFDAO = new EventCRFDAO(ds);
            ItemDAO itemDAO = new ItemDAO(ds);
            CRFDAO crfDAO = new CRFDAO(ds);

			if (subjectDataBeans != null) {// need to do this so as not to
				// throw the exception below and
				// report all available errors, tbh
				for (SubjectDataBean subjectDataBean : subjectDataBeans) {
					String oid = subjectDataBean.getSubjectOID();
					StudySubjectBean studySubjectBean = studySubjectDAO.findByOidAndStudy(oid, studyBean.getId());
					if (studySubjectBean == null) {
						mf.applyPattern(respage.getString("your_subject_oid_does_not_reference"));
						Object[] arguments = { oid };
						errors.add(mf.format(arguments));

						logger.debug("logged an error with subject oid " + oid);
					}

					ArrayList<StudyEventDataBean> studyEventDataBeans = subjectDataBean.getStudyEventData();
					if (studyEventDataBeans != null) {
						for (StudyEventDataBean studyEventDataBean : studyEventDataBeans) {
							String sedOid = studyEventDataBean.getStudyEventOID();
							StudyEventDefinitionBean studyEventDefintionBean = studyEventDefinitionDAO
									.findByOidAndStudy(sedOid, studyBean.getId(), studyBean.getParentStudyId());
							if (studyEventDefintionBean == null) {
								mf.applyPattern(respage.getString("your_study_event_oid_for_subject_oid"));
								Object[] arguments = { sedOid, oid };
								errors.add(mf.format(arguments));
								logger.debug("logged an error with se oid " + sedOid + " and subject oid " + oid);
							}

							ArrayList<FormDataBean> formDataBeans = studyEventDataBean.getFormData();
							if (formDataBeans != null) {
								for (FormDataBean formDataBean : formDataBeans) {
									String formOid = formDataBean.getFormOID();
									ArrayList<CRFVersionBean> crfVersionBeans = crfVersionDAO.findAllByOid(formOid);
									// ideally we should look to compare
									// versions within
									// seds;
									// right now just check nulls
									if (crfVersionBeans != null) {
										for (CRFVersionBean crfVersionBean : crfVersionBeans) {
											if (crfVersionBean == null) {
												mf.applyPattern(respage
														.getString("your_crf_version_oid_for_study_event_oid"));
												Object[] arguments = { formOid, sedOid };
												errors.add(mf.format(arguments));

												logger.debug("logged an error with form " + formOid + " and se oid "
														+ sedOid);
											} else {
												CRFBean crfBean = ((CRFBean) crfDAO.findByPK(crfVersionBean.getCrfId()));
												List<StudyEventBean> studyEventBeanList = (List<StudyEventBean>) studyEventDAO
														.findAllByDefinitionAndSubject(studyEventDefintionBean,
																studySubjectBean);
												for (StudyEventBean studyEventBean : studyEventBeanList) {
													List<EventCRFBean> eventCRFBeanList = (List<EventCRFBean>) eventCRFDAO
															.findAllByStudyEventAndCrfOrCrfVersionOid(studyEventBean,
																	crfBean.getOid());
													for (EventCRFBean eventCRFBean : eventCRFBeanList) {
														if (!eventCRFBean.isNotStarted()
																&& eventCRFBean.getCRFVersionId() != crfVersionBean
																		.getId()) {
															mf.applyPattern(respage
																	.getString("you_already_have_started_other_crf_version_for_study_event_and_subject"));
															Object[] arguments = { studyEventDefintionBean.getName(),
																	studySubjectBean.getName() };
															errors.add(mf.format(arguments));
														}
													}
												}
											}
										}
									} else {
										mf.applyPattern(respage.getString("your_crf_version_oid_did_not_generate"));
										Object[] arguments = { formOid };
										errors.add(mf.format(arguments));
									}

									ArrayList<ImportItemGroupDataBean> itemGroupDataBeans = formDataBean
											.getItemGroupData();
									if (itemGroupDataBeans != null) {
										for (ImportItemGroupDataBean itemGroupDataBean : itemGroupDataBeans) {
											String itemGroupOID = itemGroupDataBean.getItemGroupOID();
											List<ItemGroupBean> itemGroupBeans = itemGroupDAO
													.findAllByOid(itemGroupOID);
											if (itemGroupBeans != null) {
												logger.debug("number of item group beans: " + itemGroupBeans.size());
												logger.debug("item group oid: " + itemGroupOID);
												for (ItemGroupBean itemGroupBean : itemGroupBeans) {
													if (itemGroupBean == null) {
														mf.applyPattern(respage
																.getString("your_item_group_oid_for_form_oid"));
														Object[] arguments = { itemGroupOID, formOid };
														errors.add(mf.format(arguments));
													}
												}
											} else {
												mf.applyPattern(respage.getString("the_item_group_oid_did_not"));
												Object[] arguments = { itemGroupOID };
												errors.add(mf.format(arguments));
											}

											ArrayList<ImportItemDataBean> itemDataBeans = itemGroupDataBean
													.getItemData();
											if (itemDataBeans != null) {
												for (ImportItemDataBean itemDataBean : itemDataBeans) {
													String itemOID = itemDataBean.getItemOID();
													List<ItemBean> itemBeans = itemDAO.findByOid(itemOID);
													if (itemBeans != null) {

														logger.debug("found itembeans: ");

														for (ItemBean itemBean : itemBeans) {

															if (itemBean == null) {
																mf.applyPattern(respage
																		.getString("your_item_oid_for_item_group_oid"));
																Object[] arguments = { itemOID, itemGroupOID };
																errors.add(mf.format(arguments));

															} else {
																logger.debug("found " + itemBean.getOid() + ", passing");

															}
														}
													}
												}
											} else {
												mf.applyPattern(respage
														.getString("the_item_group_oid_did_not_contain_item_data"));
												Object[] arguments = { itemGroupOID };
												errors.add(mf.format(arguments));
											}
										}
									} else {
										mf.applyPattern(respage.getString("your_study_event_contains_no_form_data"));
										Object[] arguments = { sedOid };
										errors.add(mf.format(arguments));
									}
								}

							}
						}
					}
				}
			}
		} catch (OpenClinicaException oce) {

		} catch (NullPointerException npe) {
			logger.debug("found a nullpointer here");
		}
		// if errors == null you pass, if not you fail
		return errors;
	}

	private ItemDataDAO getItemDataDao() {
		itemDataDao = this.itemDataDao != null ? itemDataDao : new ItemDataDAO(ds);
		return itemDataDao;
	}

}
