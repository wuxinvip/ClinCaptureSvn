/*******************************************************************************
 * Copyright (C) 2009-2013 Clinovo Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the Lesser GNU General Public License as published by the Free Software Foundation, either version 2.1 of the License, or(at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Lesser GNU General Public License for more details.
 * 
 * You should have received a copy of the Lesser GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.akaza.openclinica.dao.rule;

import java.util.List;

import org.akaza.openclinica.DefaultAppContextTest;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleAuditBean;
import org.junit.Test;

public class RuleSetRuleAuditDaoTest extends DefaultAppContextTest {

	@Test
	public void testFindAllByRuleSet() {

		RuleSetBean ruleSet = ruleSetDao.findById(1);
		List<RuleSetRuleAuditBean> ruleSetRuleAudits = ruleSetRuleAuditDao.findAllByRuleSet(ruleSet);

		assertNotNull("ruleSetAudits is null", ruleSetRuleAudits);
		assertEquals("The size of the ruleSetRuleAudits is not 2", new Integer(2),
				Integer.valueOf(ruleSetRuleAudits.size()));

	}

	@Test
	public void testFindById() {

		RuleSetRuleAuditBean ruleSetRuleAuditBean = ruleSetRuleAuditDao.findById(1);

		assertNotNull("ruleSetRuleAuditBean is null", ruleSetRuleAuditBean);
		assertEquals("The ruleSetRuleAuditBean.getRuleSetRule.getId should be 3", new Integer(3),
				Integer.valueOf(ruleSetRuleAuditBean.getRuleSetRuleBean().getId()));

	}

}
