package org.akaza.openclinica;

import javax.sql.DataSource;

import org.akaza.openclinica.dao.dynamicevent.DynamicEventDao;
import org.akaza.openclinica.dao.hibernate.AuditUserLoginDao;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.hibernate.ConfigurationDao;
import org.akaza.openclinica.dao.hibernate.DatabaseChangeLogDao;
import org.akaza.openclinica.dao.hibernate.RuleDao;
import org.akaza.openclinica.dao.hibernate.RuleSetAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleAuditDao;
import org.akaza.openclinica.dao.hibernate.RuleSetRuleDao;
import org.akaza.openclinica.dao.managestudy.DiscrepancyNoteDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyGroupClassDAO;
import org.akaza.openclinica.dao.discrepancy.DnDescriptionDao;
import org.akaza.openclinica.service.rule.RuleSetServiceInterface;
import org.akaza.openclinica.service.rule.RulesPostImportContainerService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * To avoid the constant loading of beans from the application context, which can take a lot of memory on the test, we
 * load all the required test beans in this class and re use them in the tests.
 * 
 */
@SuppressWarnings("rawtypes")
public abstract class DefaultAppContextTest extends AbstractContextSentiveTest {

	protected DataSource dataSource = getDataSource();
	
	protected StudyEventDAO studyEventDao;
	protected DynamicEventDao dynamicEventDao;
	protected DiscrepancyNoteDAO discrepancyNoteDAO;
	protected DnDescriptionDao dnDescriptionDao;
	protected StudyGroupClassDAO studyGroupClassDAO;
	// DAOS
	@Autowired protected RuleDao ruleDao;
	@Autowired protected RuleSetDao ruleSetDao;
	@Autowired protected AuthoritiesDao authoritiesDao;
	@Autowired protected RuleSetRuleDao ruleSetRuleDao;
	
	@Autowired protected RuleSetAuditDao ruleSetAuditDao;
	@Autowired protected ConfigurationDao configurationDao;
	@Autowired protected AuditUserLoginDao auditUserLoginDao;
	@Autowired protected RuleSetRuleAuditDao ruleSetRuleAuditDao;
	@Autowired protected DatabaseChangeLogDao databaseChangeLogDao;

	// Services
	@Autowired protected RuleSetServiceInterface ruleSetService;
	@Autowired protected RulesPostImportContainerService postImportContainerService;
	

	@Before
	public void initializeDAOs() throws Exception {
		
		super.setUp();
		
		// DAO that require data source
		studyEventDao = new StudyEventDAO(dataSource);
		dynamicEventDao = new DynamicEventDao(dataSource);
		discrepancyNoteDAO = new DiscrepancyNoteDAO(dataSource);
		studyGroupClassDAO = new StudyGroupClassDAO(dataSource);
		dnDescriptionDao = new DnDescriptionDao(dataSource);
	}
}
