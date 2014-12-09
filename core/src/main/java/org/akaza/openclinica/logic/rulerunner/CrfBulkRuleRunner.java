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

package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainer;
import org.akaza.openclinica.domain.rule.RuleBulkExecuteContainerTwo;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.akaza.openclinica.domain.rule.action.ActionProcessor;
import org.akaza.openclinica.domain.rule.action.ActionProcessorFacade;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunLogBean;
import org.akaza.openclinica.domain.rule.action.RuleActionRunBean.Phase;
import org.akaza.openclinica.domain.rule.expression.ExpressionBean;
import org.akaza.openclinica.domain.rule.expression.ExpressionObjectWrapper;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.logic.expressionTree.OpenClinicaExpressionParser;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

/**
 * 
 * CRF Bulk Rule Runner.
 * 
 */
public class CrfBulkRuleRunner extends RuleRunner {

	/**
	 * 
	 * @param ds
	 *            DataSource
	 * @param requestURLMinusServletPath
	 *            String
	 * @param contextPath
	 *            String
	 * @param mailSender
	 *            JavaMailSenderImpl
	 */
	public CrfBulkRuleRunner(DataSource ds, String requestURLMinusServletPath, String contextPath,
			JavaMailSenderImpl mailSender) {
		super(ds, requestURLMinusServletPath, contextPath, mailSender);
	}

	/**
	 * Organize objects in a certain way so that we can show to Users on UI. step1 : Get StudyEvent , eventCrf ,
	 * crfVersion from studyEventId.
	 * 
	 * @param crfViewSpecificOrderedObjects
	 * @param ruleSet
	 * @param rule
	 * @return
	 */
	private HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> populateForCrfBasedRulesView(
			HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> crfViewSpecificOrderedObjects,
			RuleSetBean ruleSet, RuleBean rule, String result, StudyBean currentStudy, List<RuleActionBean> actions) {

		// step1
		StudyEventBean studyEvent = (StudyEventBean) getStudyEventDao().findByPK(
				Integer.valueOf(getExpressionService().getStudyEventDefenitionOrdninalCurated(result)));
		EventCRFBean eventCrf = (EventCRFBean) getEventCrfDao().findAllByStudyEventAndCrfOrCrfVersionOid(studyEvent,
				getExpressionService().getCrfOid(ruleSet.getTarget().getValue())).get(0);
		CRFVersionBean crfVersion = (CRFVersionBean) getCrfVersionDao().findByPK(eventCrf.getCRFVersionId());

		RuleBulkExecuteContainer key = new RuleBulkExecuteContainer(crfVersion.getName(), rule, ruleSet
				.getOriginalTarget().getValue(), actions);
		String key2String = getExpressionService().getCustomExpressionUsedToCreateView(ruleSet.getTarget().getValue(),
				studyEvent.getSampleOrdinal());
		String studyEventDefinitionName = getExpressionService().getStudyEventDefinitionFromExpression(
				ruleSet.getTarget().getValue(), currentStudy).getName();
		studyEventDefinitionName += " [" + studyEvent.getSampleOrdinal() + "]";

		String itemGroupName = getExpressionService().getItemGroupNameAndOrdinal(ruleSet.getTarget().getValue());
		ItemGroupBean itemGroupBean = getExpressionService().getItemGroupExpression(ruleSet.getTarget().getValue());
		ItemBean itemBean = getExpressionService().getItemExpression(ruleSet.getTarget().getValue(), itemGroupBean);
		String itemName = itemBean.getName();

		RuleBulkExecuteContainerTwo key2 = new RuleBulkExecuteContainerTwo(key2String, studyEvent,
				studyEventDefinitionName, itemGroupName, itemName);
		StudySubjectBean studySubject = (StudySubjectBean) getStudySubjectDao()
				.findByPK(studyEvent.getStudySubjectId());

		if (crfViewSpecificOrderedObjects.containsKey(key)) {
			HashMap<RuleBulkExecuteContainerTwo, Set<String>> k = crfViewSpecificOrderedObjects.get(key);
			if (k.containsKey(key2)) {
				k.get(key2).add(
						String.valueOf(studySubject.getLabel() + "," + studySubject.getCreatedDate() + ","
								+ studySubject.getStatus().getName() + "," + studySubject.getStudyName()));
			} else {
				HashSet<String> values = new HashSet<String>();
				values.add(String.valueOf(studySubject.getLabel() + "," + studySubject.getCreatedDate() + ","
						+ studySubject.getStatus().getName() + "," + studySubject.getStudyName()));
				k.put(key2, values);
			}
		} else {
			HashMap<RuleBulkExecuteContainerTwo, Set<String>> k = new HashMap<RuleBulkExecuteContainerTwo, Set<String>>();
			HashSet<String> values = new HashSet<String>();
			values.add(String.valueOf(studySubject.getLabel() + "," + studySubject.getCreatedDate() + ","
					+ studySubject.getStatus().getName() + "," + studySubject.getStudyName()));
			k.put(key2, values);
			crfViewSpecificOrderedObjects.put(key, k);
		}
		return crfViewSpecificOrderedObjects;
	}

	/**
	 * 
	 * @param ruleSets
	 *            List of RuleSetBeans
	 * @param executionMode
	 *            ExecutionMode
	 * @param currentStudy
	 *            StudyBean
	 * @param variableAndValue
	 *            HashMap
	 * @param ub
	 *            UserAccountBean
	 * @return HashMap
	 */
	@Deprecated
	public HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> runRulesBulkOLD(
			List<RuleSetBean> ruleSets, ExecutionMode executionMode, StudyBean currentStudy,
			HashMap<String, String> variableAndValue, UserAccountBean ub) {

		if (variableAndValue == null || variableAndValue.isEmpty()) {
			logger.warn("You must be executing Rules in Batch");
			variableAndValue = new HashMap<String, String>();
		}

		HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> crfViewSpecificOrderedObjects = new HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>>();
		for (RuleSetBean ruleSet : ruleSets) {
			for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
				ruleSet.setTarget(expressionBean);

				for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
					String result = null;
					RuleBean rule = ruleSetRule.getRuleBean();
					ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(),
							ruleSet, variableAndValue);
					try {
						OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
						result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());

						// Actions
						List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result,
								Phase.BATCH);

						ItemDataBean itemData = getExpressionService().getItemDataBeanFromDb(
								ruleSet.getTarget().getValue());
						if (itemData != null) {
							Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
							while (itr.hasNext()) {
								RuleActionBean ruleActionBean = itr.next();
								RuleActionRunLogBean ruleActionRunLog = new RuleActionRunLogBean(
										ruleActionBean.getActionType(), itemData, itemData.getValue(), ruleSetRule
												.getRuleBean().getOid());
								if (getRuleActionRunLogDao().findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
									itr.remove();
								}
							}
						}

						List<RuleActionBean> actionBeansToShow = new ArrayList<RuleActionBean>();
						if (actionListBasedOnRuleExecutionResult.size() > 0) {
							// if (!dryRun) {
							for (RuleActionBean ruleAction : actionListBasedOnRuleExecutionResult) {
								ruleAction.setCuratedMessage(curateMessage(ruleAction, ruleSetRule));
								// getDiscrepancyNoteService().saveFieldNotes(ruleAction.getSummary(), itemDataBeanId,
								// "ItemData", currentStudy, ub);
								ActionProcessor ap = ActionProcessorFacade.getActionProcessor(
										ruleAction.getActionType(), ds, getMailSender(), dynamicsMetadataService,
										ruleSet, getRuleActionRunLogDao(), ruleSetRule);
								RuleActionBean rab = ap.execute(RuleRunnerMode.RULSET_BULK, executionMode, ruleAction,
										itemData, DiscrepancyNoteBean.ITEM_DATA, currentStudy, ub,
										prepareEmailContents(ruleSet, ruleSetRule, currentStudy, ruleAction));
								if (rab != null) {
									actionBeansToShow.add(ruleAction);
								}
							}
							if (actionBeansToShow.size() > 0) {
								crfViewSpecificOrderedObjects = populateForCrfBasedRulesView(
										crfViewSpecificOrderedObjects, ruleSet, rule, result, currentStudy,
										actionBeansToShow);
							}

						}
					} catch (OpenClinicaSystemException osa) {
						String errorMessage = "RuleSet with target  : " + ruleSet.getTarget().getValue()
								+ " , Ran Rule : " + rule.getName() + " , It resulted in an error due to : "
								+ osa.getMessage();
						// log error
						logger.warn(errorMessage);

					}
				}
			}
		}
		logCrfViewSpecificOrderedObjects(crfViewSpecificOrderedObjects);
		return crfViewSpecificOrderedObjects;
	}

	/**
	 * 
	 * @param ruleSets
	 *            List
	 * @param executionMode
	 *            ExecutionMode
	 * @param currentStudy
	 *            StudyBean
	 * @param variableAndValue
	 *            HashMap
	 * @param ub
	 *            UserAccountBean
	 * @return HashMap
	 */
	public HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> runRulesBulk(
			List<RuleSetBean> ruleSets, ExecutionMode executionMode, StudyBean currentStudy,
			HashMap<String, String> variableAndValue, UserAccountBean ub) {

		if (variableAndValue == null || variableAndValue.isEmpty()) {
			logger.warn("You must be executing Rules in Batch");
			variableAndValue = new HashMap<String, String>();
		}

		HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>> crfViewSpecificOrderedObjects = new HashMap<RuleBulkExecuteContainer, HashMap<RuleBulkExecuteContainerTwo, Set<String>>>();
		HashMap<String, ArrayList<RuleActionContainer>> toBeExecuted = new HashMap<String, ArrayList<RuleActionContainer>>();
		for (RuleSetBean ruleSet : ruleSets) {
			String key = getExpressionService().getItemOid(ruleSet.getOriginalTarget().getValue());
			List<RuleActionContainer> allActionContainerListBasedOnRuleExecutionResult = null;
			if (toBeExecuted.containsKey(key)) {
				allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
			} else {
				toBeExecuted.put(key, new ArrayList<RuleActionContainer>());
				allActionContainerListBasedOnRuleExecutionResult = toBeExecuted.get(key);
			}
			ItemDataBean itemData = null;

			for (ExpressionBean expressionBean : ruleSet.getExpressions()) {
				ruleSet.setTarget(expressionBean);

				for (RuleSetRuleBean ruleSetRule : ruleSet.getRuleSetRules()) {
					String result = null;
					RuleBean rule = ruleSetRule.getRuleBean();
					ExpressionObjectWrapper eow = new ExpressionObjectWrapper(ds, currentStudy, rule.getExpression(),
							ruleSet, variableAndValue);
					try {
						OpenClinicaExpressionParser oep = new OpenClinicaExpressionParser(eow);
						result = oep.parseAndEvaluateExpression(rule.getExpression().getValue());
						itemData = getExpressionService().getItemDataBeanFromDb(ruleSet.getTarget().getValue());

						// Actions
						List<RuleActionBean> actionListBasedOnRuleExecutionResult = ruleSetRule.getActions(result,
								Phase.BATCH);

						if (itemData != null) {
							Iterator<RuleActionBean> itr = actionListBasedOnRuleExecutionResult.iterator();
							while (itr.hasNext()) {
								RuleActionBean ruleActionBean = itr.next();
								RuleActionRunLogBean ruleActionRunLog = new RuleActionRunLogBean(
										ruleActionBean.getActionType(), itemData, itemData.getValue(), ruleSetRule
												.getRuleBean().getOid());
								if (getRuleActionRunLogDao().findCountByRuleActionRunLogBean(ruleActionRunLog) > 0) {
									itr.remove();
								}
							}
						}
						for (RuleActionBean ruleActionBean : actionListBasedOnRuleExecutionResult) {
							RuleActionContainer ruleActionContainer = new RuleActionContainer(ruleActionBean,
									expressionBean, itemData, ruleSet);
							allActionContainerListBasedOnRuleExecutionResult.add(ruleActionContainer);
						}
						logger.info(
								"RuleSet with target  : {} , Ran Rule : {}  The Result was : {} , Based on that {} action will be executed in {} mode. ",
								new Object[] { ruleSet.getTarget().getValue(), rule.getName(), result,
										actionListBasedOnRuleExecutionResult.size(), executionMode.name() });
					} catch (OpenClinicaSystemException osa) {
						logger.error(osa.getLocalizedMessage());
						// TODO report something useful
					}
				}
			}
		}

		for (Map.Entry<String, ArrayList<RuleActionContainer>> entry : toBeExecuted.entrySet()) {
			// Sort the list of actions
			Collections.sort(entry.getValue(), new RuleActionContainerComparator());
			HashMap<Key, List<RuleActionBean>> hms = new HashMap<Key, List<RuleActionBean>>();
			for (RuleActionContainer ruleActionContainer : entry.getValue()) {
				ruleActionContainer.getRuleSetBean().setTarget(ruleActionContainer.getExpressionBean());
				ruleActionContainer.getRuleAction().setCuratedMessage(
						curateMessage(ruleActionContainer.getRuleAction(), ruleActionContainer.getRuleAction()
								.getRuleSetRule()));
				ActionProcessor ap = ActionProcessorFacade.getActionProcessor(ruleActionContainer.getRuleAction()
						.getActionType(), ds, getMailSender(), dynamicsMetadataService, ruleActionContainer
						.getRuleSetBean(), getRuleActionRunLogDao(), ruleActionContainer.getRuleAction()
						.getRuleSetRule());

				ap.execute(
						RuleRunnerMode.RULSET_BULK,
						executionMode,
						ruleActionContainer.getRuleAction(),
						ruleActionContainer.getItemDataBean(),
						DiscrepancyNoteBean.ITEM_DATA,
						currentStudy,
						ub,
						prepareEmailContents(ruleActionContainer.getRuleSetBean(), ruleActionContainer.getRuleAction()
								.getRuleSetRule(), currentStudy, ruleActionContainer.getRuleAction()));

				Key k = new Key(ruleActionContainer.getRuleSetBean(), ruleActionContainer.getExpressionBean()
						.getValue(), ruleActionContainer.getRuleAction().getRuleSetRule().getRuleBean());
				if (hms.containsKey(k)) {
					hms.get(k).add(ruleActionContainer.getRuleAction());
				} else {
					List<RuleActionBean> theActionBeansToShow = new ArrayList<RuleActionBean>();
					theActionBeansToShow.add(ruleActionContainer.getRuleAction());
					hms.put(k, theActionBeansToShow);
				}
			}
			for (Map.Entry<Key, List<RuleActionBean>> theEntry : hms.entrySet()) {
				Key key = theEntry.getKey();
				List<RuleActionBean> value = theEntry.getValue();
				crfViewSpecificOrderedObjects = populateForCrfBasedRulesView(crfViewSpecificOrderedObjects,
						key.getRuleSet(), key.getRule(), key.getResult(), currentStudy, value);
			}
		}
		return crfViewSpecificOrderedObjects;
	}

}

class Key {

	private RuleSetBean ruleSet;
	private String result;
	private RuleBean rule;

	public Key(RuleSetBean ruleSet, String result, RuleBean rule) {
		super();
		this.ruleSet = ruleSet;
		this.result = result;
		this.rule = rule;
	}

	public RuleSetBean getRuleSet() {
		return ruleSet;
	}

	public void setRuleSet(RuleSetBean ruleSet) {
		this.ruleSet = ruleSet;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public RuleBean getRule() {
		return rule;
	}

	public void setRule(RuleBean rule) {
		this.rule = rule;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		result = prime * result + ((ruleSet == null) ? 0 : ruleSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Key other = (Key) obj;
		if (result == null) {
			if (other.result != null) {
				return false;
			}
		} else if (!result.equals(other.result)) {
			return false;
		}
		if (rule == null) {
			if (other.rule != null) {
				return false;
			}
		} else if (!rule.equals(other.rule)) {
			return false;
		}
		if (ruleSet == null) {
			if (other.ruleSet != null) {
				return false;
			}
		} else if (!ruleSet.equals(other.ruleSet)) {
			return false;
		}
		return true;
	}
}
