package com.clinovo.model;

import com.clinovo.enums.BooleanEnum;
import com.clinovo.util.DateUtil;
import com.clinovo.util.RequestUtil;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.AbstractTableFactory;
import org.akaza.openclinica.control.DefaultActionsEditor;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.web.table.sdv.SDVSimpleListFilter;
import org.jmesa.core.filter.FilterMatcher;
import org.jmesa.core.filter.MatcherKey;
import org.jmesa.facade.TableFacade;
import org.jmesa.view.component.Row;
import org.jmesa.view.editor.BasicCellEditor;
import org.springframework.context.MessageSource;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Email Log Child entries Table eFactory.
 */
public class EmailLogChildTableFactory extends AbstractTableFactory {
	public static final String EMAIL_AUDIT_LOG_TABLE_NAME = "emailAuditLogChildTableName";
	public static final String DATE_SENT = "dateSent";
	public static final String SENDER = "sender";
	public static final String SENT_BY = "sentBy";
	public static final String STATUS = "status";
	public static final String ERROR = "error";
	public static final String OBJECT = "object";

	private List<EmailLog> logs;
	private MessageSource messageSource;
	private DataSource dataSource;
	private UserAccountDAO userAccountDAO;
	private List<String> userNames;
	private Locale locale;

	/**
	 * Public constructor.
	 *
	 * @param logs          List AuditLogEmail
	 * @param messageSource Message Source
	 * @param dataSource    Data Source.
	 * @param locale        Locale.
	 */
	public EmailLogChildTableFactory(DataSource dataSource, List<EmailLog> logs,
								MessageSource messageSource, Locale locale) {
		this.logs = logs;
		this.messageSource = messageSource;
		this.dataSource = dataSource;
		userNames = new ArrayList<String>();
		this.locale = locale;
	}

	@Override
	protected String getTableName() {
		return EMAIL_AUDIT_LOG_TABLE_NAME;
	}

	@Override
	public void configureTableFacade(HttpServletResponse response, TableFacade tableFacade) {
		super.configureTableFacade(response, tableFacade);
		tableFacade.addFilterMatcher(new MatcherKey(Date.class, DATE_SENT),
				new FilterMatcher() {
					public boolean evaluate(Object itemValue, String filterValue) {
						return true;
					}
				});
	}

	@Override
	protected void configureColumns(TableFacade tableFacade, Locale locale) {
		tableFacade.setColumnProperties(DATE_SENT, SENDER, SENT_BY, STATUS, ERROR);
		UserAccountBean currentUser = RequestUtil.getUserAccountBean();
		Row row = tableFacade.getTable().getRow();
		configureColumn(row.getColumn(DATE_SENT), getMessageSource().getMessage("date_sent", null, locale),
				new DateEditor(DateUtil.DatePattern.DATE, currentUser.getUserTimeZoneId()), new DefaultActionsEditor(locale), true, true);
		configureColumn(row.getColumn(SENDER), getMessageSource().getMessage("sender", null, locale), null,
				null, true, true);
		configureColumn(row.getColumn(SENT_BY), getMessageSource().getMessage("sent_by", null, locale),
				new BasicCellEditor(), new SDVSimpleListFilter(userNames), true, true);
		configureColumn(row.getColumn(STATUS), getMessageSource().getMessage("rule_status", null, locale),
				new BasicCellEditor(), new SDVSimpleListFilter(getStatusesList()), true, true);
		configureColumn(row.getColumn(ERROR), getMessageSource().getMessage("error", null, locale), null,
				null, true, false);
	}

	@Override
	public void setDataAndLimitVariables(TableFacade tableFacade) {
		Collection<HashMap<Object, Object>> tableData = new ArrayList<HashMap<Object, Object>>();
		for (EmailLog logEntry : logs) {
			HashMap<Object, Object> h = new HashMap<Object, Object>();
			h.put(DATE_SENT, logEntry.getDateSent());
			h.put(SENDER, logEntry.getSender());
			h.put(SENT_BY, getNameOfTheSender(logEntry));
			h.put(STATUS, getColoredStatus(logEntry));
			h.put(ERROR, logEntry.getError());
			h.put(OBJECT, logEntry);

			tableData.add(h);
		}
		tableFacade.setTotalRows(logs.size());
		tableFacade.setItems(tableData);
	}

	private String getNameOfTheSender(EmailLog logEntry) {
		int userId = logEntry.getSentBy();
		UserAccountBean user = (UserAccountBean) getUserAccountDAO().findByPK(userId);
		userNames.add(user.getName());
		return user.getName();
	}

	private String getColoredStatus(EmailLog logEntry) {
		String text = messageSource.getMessage("email_status." + logEntry.getWasSent().toString(), null, locale);
		String cssClass = logEntry.wasSent() ? "aka_green_highlight" : "aka_red_highlight";
		return "<span class=\"" + cssClass + "\">" + text + "</span>";
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public MessageSource getMessageSource() {
		return messageSource;
	}

	/**
	 * Get user account dao.
	 *
	 * @return UserAccountDAO.
	 */
	public UserAccountDAO getUserAccountDAO() {
		if (userAccountDAO == null) {
			userAccountDAO = new UserAccountDAO(dataSource);
		}
		return userAccountDAO;
	}

	private List<String> getStatusesList() {
		List<String> result = new ArrayList<String>();
		result.add(messageSource.getMessage("email_status." + BooleanEnum.FALSE.toString(), null, locale));
		result.add(messageSource.getMessage("email_status." + BooleanEnum.TRUE.toString(), null, locale));
		return result;
	}
}
