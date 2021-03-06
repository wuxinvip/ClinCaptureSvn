package com.clinovo.jmesa.evaluation;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.dao.core.CoreResources;
import org.akaza.openclinica.dao.managestudy.CriteriaCommand;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CRFEvaluationFilter class.
 */
public class CRFEvaluationFilter implements CriteriaCommand {

	public static final String CRF_NAME = "crfName";
	public static final String EVENT_NAME = "eventName";
	public static final String CRF_STATUS = "crfStatus";
	public static final String STUDY_SUBJECT_ID = "studySubjectId";
	public static final String EC_COMPLETE_YEAR = "crfCompletedYear";
	public static final String EVALUATION_STATUS = "evaluation_status";

	public static final String C_NAME = "c.name";
	public static final String SS_LABEL = "ss.label";
	public static final String SED_NAME = "sed.name";
	public static final String EC_STATUS_ID = "ec.status_id";
	public static final String EC_C_DATE = "ec.date_completed";
	public static final String EC_DATE_VALIDATE_COMPLETED = "ec.date_validate_completed";

	private Map<Object, Status> optionsMap;
	private MessageSource messageSource;
	private Locale locale;
	private List<Filter> filters = new ArrayList<Filter>();
	private HashMap<String, String> columnMapping = new HashMap<String, String>();

	/**
	 * CRFEvaluationFilter constructor.
	 * 
	 * @param optionsMap
	 *            Map<String, Status>
	 * @param messageSource
	 *            MessageSource
	 * @param locale
	 *            Locale
	 */
	public CRFEvaluationFilter(Map<Object, Status> optionsMap, MessageSource messageSource, Locale locale) {
		this.optionsMap = optionsMap;
		columnMapping.put(CRF_NAME, C_NAME);
		columnMapping.put(EVENT_NAME, SED_NAME);
		columnMapping.put(CRF_STATUS, EC_STATUS_ID);
		columnMapping.put(STUDY_SUBJECT_ID, SS_LABEL);
		columnMapping.put(EC_COMPLETE_YEAR, EC_C_DATE);
		this.messageSource = messageSource;
		this.locale = locale;
	}

	/**
	 * Method that adds a filter.
	 * 
	 * @param property
	 *            String property
	 * @param value
	 *            Object value
	 */
	public void addFilter(String property, Object value) {
		filters.add(new Filter(property, value));
	}

	/**
	 * Method that executes all filters.
	 * 
	 * @param criteria
	 *            String criteria
	 * @return String sql string
	 */
	public String execute(String criteria) {
		String theCriteria = "";
		for (Filter filter : filters) {
			theCriteria = theCriteria.concat(buildCriteria(criteria, filter.getProperty(), filter.getValue()));
		}
		return theCriteria;
	}

	private String buildCriteria(String criteria, String property, Object value) {
		StringBuilder theCriteria = new StringBuilder("");
		value = StringEscapeUtils.escapeSql(value.toString());
		if (value != null) {
			if (property.equals(CRF_STATUS)) {
				Status status = optionsMap.get(value);
				if (status.equals(Status.DELETED)) {
					theCriteria
							.append(" AND ((")
							.append(columnMapping.get(property))
							.append(" in (5,7) or se.subject_event_status_id in (10)) and not(c.status_id in (5,6,7)) and not(cv.status_id in (5,6,7))) ");
				} else if (status.equals(Status.LOCKED)) {
					theCriteria
							.append(" AND (")
							.append(columnMapping.get(property))
							.append(" in (6) or se.subject_event_status_id in (5,6,7) or c.status_id in (5,6,7) or cv.status_id in (5,6,7)) ");
				} else if (status.equals(Status.SIGNED)) {
					theCriteria
							.append(" AND (")
							.append(columnMapping.get(property))
							.append(" = 2 and se.subject_event_status_id = 8 and not(c.status_id in (5,6,7)) and not(cv.status_id in (5,6,7))) ");
				} else if (status.equals(Status.SOURCE_DATA_VERIFIED)) {
					theCriteria
							.append(" AND (")
							.append(columnMapping.get(property))
							.append(" = 2 and ec.sdv_status = ")
							.append(CoreResources.getDBType().equalsIgnoreCase("oracle") ? "1" : "true")
							.append(" and not(se.subject_event_status_id in (5,6,7,8)) and not(c.status_id in (5,6,7)) and not(cv.status_id in (5,6,7))) ");
				} else if (status.equals(Status.COMPLETED)) {
					theCriteria
							.append(" AND (")
							.append(columnMapping.get(property))
							.append(" = 2 and ec.sdv_status = ")
							.append(CoreResources.getDBType().equalsIgnoreCase("oracle") ? "0" : "false")
							.append(" and not(se.subject_event_status_id in (5,6,7,8)) and not(c.status_id in (5,6,7)) and not(cv.status_id in (5,6,7))) ");
				} else if (status.equals(Status.INITIAL_DATA_ENTRY_COMPLETED)) {
					theCriteria
							.append(" AND (")
							.append(columnMapping.get(property))
							.append(" in (4) and ec.validator_id = 0 and se.subject_event_status_id in (1,3,4,8,9) and not(c.status_id in (5,6,7)) and not(cv.status_id in (5,6,7))) ");
				} else if (status.equals(Status.DOUBLE_DATA_ENTRY)) {
					theCriteria
							.append(" AND (")
							.append(columnMapping.get(property))
							.append(" in (4) and ec.validator_id != 0 and se.subject_event_status_id in (1,3,4,8,9) and not(c.status_id in (5,6,7)) and not(cv.status_id in (5,6,7))) ");
				}
			} else if (property.equals(EC_COMPLETE_YEAR)) {
				theCriteria.append(" AND EXTRACT(YEAR FROM ").append(EC_C_DATE).append(") = ").append(value.toString());
			} else if (property.equals(EVALUATION_STATUS)) {
				if (value.equals(messageSource.getMessage("ready_for_evaluation", null, locale))) {
					theCriteria.append(" AND (").append(EC_DATE_VALIDATE_COMPLETED).append(" IS NULL) ");
				} else if (value.equals(messageSource.getMessage("evaluation_completed", null, locale))) {
					theCriteria.append(" AND NOT(").append(EC_DATE_VALIDATE_COMPLETED).append(" IS NULL) ");
				}
			} else {
				theCriteria.append(" AND UPPER(").append(columnMapping.get(property)).append(") like UPPER('%")
						.append(value.toString()).append("%')");
			}
		}
		return criteria.concat(theCriteria.toString());
	}

	private class Filter {
		private final String property;
		private final Object value;

		public Filter(String property, Object value) {
			this.property = property;
			this.value = value;
		}

		public String getProperty() {
			return property;
		}

		public Object getValue() {
			return value;
		}
	}
}
