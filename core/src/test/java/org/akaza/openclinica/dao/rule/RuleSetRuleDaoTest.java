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
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.dao.hibernate.ViewRuleAssignmentFilter;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.RuleSetBean;
import org.akaza.openclinica.domain.rule.RuleSetRuleBean;
import org.junit.Test;

public class RuleSetRuleDaoTest extends DefaultAppContextTest {

	@Test
	public void testFindById() {
		RuleSetRuleBean ruleSetRuleBean = ruleSetRuleDao.findById(3);
		// Test RuleSetRule
		assertNotNull("RuleSet is null", ruleSetRuleBean);
		assertEquals("The id of the retrieved RuleSet should be 1", new Integer(3), ruleSetRuleBean.getId());

	}

	@Test
	public void testFindByIdEmptyResultSet() {
		RuleSetRuleBean ruleSetRuleBean = ruleSetRuleDao.findById(1);
		// Test Rule
		assertNull("RuleSet is null", ruleSetRuleBean);
	}

	@Test
	public void testFindByRuleSetBeanAndRuleBean() {
		RuleBean persistentRuleBean = ruleDao.findById(1);
		RuleSetBean persistentRuleSetBean = ruleSetDao.findById(1);
		List<RuleSetRuleBean> ruleSetRules = ruleSetRuleDao.findByRuleSetBeanAndRuleBean(persistentRuleSetBean,
				persistentRuleBean);

		assertNotNull("RuleSetRules is null", ruleSetRules);
		assertEquals("The size of RuleSetRules should be 1", new Integer(1), new Integer(ruleSetRules.size()));
	}

	@Test
	public void testThatGetCountWithFilterReturnsCorrectSize() {
		ViewRuleAssignmentFilter filter = new ViewRuleAssignmentFilter();
		filter.addFilter("ignoreWrongRules", true);
		assertEquals(0, ruleSetRuleDao.getCountWithFilter(filter));
	}

	@Test
	public void testThatFindAllByCrfMethodReturnsCorrectCollectionSize() {
		CRFBean crfBean = (CRFBean) crfdao.findByPK(1);
		assertEquals(ruleSetRuleDao.findAllByCrf(crfBean).size(), 5);
	}

	@Test
	public void testThatFindAllByCrfVersionMethodReturnsCorrectCollectionSize() {
		CRFVersionBean crfVersionBean = (CRFVersionBean) crfVersionDao.findByPK(2);
		assertEquals(ruleSetRuleDao.findAllByCrfVersion(crfVersionBean).size(), 1);
	}

	@Test
	public void testThatDeleteAllRulesByCrfMethodWorksFine() {
		CRFBean crfBean = (CRFBean) crfdao.findByPK(1);
		assertEquals(ruleSetRuleDao.findAllByCrf(crfBean).size(), 5);
		ruleSetRuleDao.deleteAllRulesByCrf(crfBean);
		assertEquals(ruleSetRuleDao.findAllByCrf(crfBean).size(), 0);
	}

	@Test
	public void testThatDeleteAllRulesByCrfVersionMethodWorksFine() {
		CRFVersionBean crfVersionBean = (CRFVersionBean) crfVersionDao.findByPK(2);
		assertEquals(ruleSetRuleDao.findAllByCrfVersion(crfVersionBean).size(), 1);
		ruleSetRuleDao.deleteAllRulesByCrfVersion(crfVersionBean);
		assertEquals(ruleSetRuleDao.findAllByCrfVersion(crfVersionBean).size(), 0);
	}
}
