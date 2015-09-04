package com.clinovo.lib.crf.service.impl;

import java.util.Locale;

import org.akaza.openclinica.DefaultAppContextTest;
import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;

import com.clinovo.lib.crf.builder.CrfBuilder;
import com.clinovo.lib.crf.factory.CrfBuilderFactory;

public class FormStudioImportCrfServiceImplTest extends DefaultAppContextTest {

	private StudyBean studyBean;

	private UserAccountBean owner;

	private CrfBuilder crfBuilder;

	@Autowired
	private CrfBuilderFactory crfBuilderFactory;

	@Before
	public void before() {
		studyBean = (StudyBean) studyDAO.findByPK(1);
		owner = (UserAccountBean) userAccountDAO.findByPK(1);
	}

	@After
	public void after() {
		if (crfBuilder != null && crfBuilder.getCrfBean() != null && crfBuilder.getCrfBean().getId() > 0) {
			deleteCrfService.deleteCrf(crfBuilder.getCrfBean().getId());
		}
	}

	@Test
	public void testThatCrfBuilderProcessesTheTestCrfWithCorrectQuantityOfSections() throws Exception {
		String jsonData = IOUtils
				.toString(new DefaultResourceLoader().getResource("data/json/testCrf.json").getInputStream(), "UTF-8");
		crfBuilder = crfBuilderFactory.getCrfBuilder(jsonData, studyBean, owner, Locale.ENGLISH,
				ResourceBundleProvider.getPageMessagesBundle());
		crfBuilder.build();
		assertEquals(crfBuilder.getSections().size(), 2);
	}

	@Test
	public void testThatCrfBuilderProcessesTheTestCrfWithCorrectQuantityOfItemGroups() throws Exception {
		String jsonData = IOUtils
				.toString(new DefaultResourceLoader().getResource("data/json/testCrf.json").getInputStream(), "UTF-8");
		crfBuilder = crfBuilderFactory.getCrfBuilder(jsonData, studyBean, owner, Locale.ENGLISH,
				ResourceBundleProvider.getPageMessagesBundle());
		crfBuilder.build();
		assertEquals(crfBuilder.getItemGroups().size(), 2);
	}

	@Test
	public void testThatCrfBuilderProcessesTheTestCrfWithCorrectQuantityOfItems() throws Exception {
		String jsonData = IOUtils
				.toString(new DefaultResourceLoader().getResource("data/json/testCrf.json").getInputStream(), "UTF-8");
		crfBuilder = crfBuilderFactory.getCrfBuilder(jsonData, studyBean, owner, Locale.ENGLISH,
				ResourceBundleProvider.getPageMessagesBundle());
		crfBuilder.build();
		assertEquals(crfBuilder.getItems().size(), 18);
	}

	@Test
	public void testThatCrfBuilderProcessesTheTestCrfWithCorrectCrfName() throws Exception {
		String jsonData = IOUtils
				.toString(new DefaultResourceLoader().getResource("data/json/testCrf.json").getInputStream(), "UTF-8");
		crfBuilder = crfBuilderFactory.getCrfBuilder(jsonData, studyBean, owner, Locale.ENGLISH,
				ResourceBundleProvider.getPageMessagesBundle());
		crfBuilder.build();
		assertEquals(crfBuilder.getCrfBean().getName(), "testCRF");
	}

	@Test
	public void testThatCrfBuilderProcessesTheTestCrfWithCorrectCrfVersion() throws Exception {
		String jsonData = IOUtils
				.toString(new DefaultResourceLoader().getResource("data/json/testCrf.json").getInputStream(), "UTF-8");
		crfBuilder = crfBuilderFactory.getCrfBuilder(jsonData, studyBean, owner, Locale.ENGLISH,
				ResourceBundleProvider.getPageMessagesBundle());
		crfBuilder.build();
		assertEquals(crfBuilder.getCrfVersionBean().getName(), "v1.0");
	}

	@Test
	public void testThatCrfBuilderSavesDataFromTheTestCrfCorrectly() throws Exception {
		String jsonData = IOUtils
				.toString(new DefaultResourceLoader().getResource("data/json/testCrf.json").getInputStream(), "UTF-8");
		crfBuilder = crfBuilderFactory.getCrfBuilder(jsonData, studyBean, owner, Locale.ENGLISH,
				ResourceBundleProvider.getPageMessagesBundle());
		crfBuilder.build();
		crfBuilder.save();
		CRFBean crfBean = (CRFBean) crfdao.findByPK(crfBuilder.getCrfBean().getId());
		assertEquals(crfBean.getName(), "testCRF");
		assertTrue(crfBean.getId() > 0);
		CRFVersionBean crfVersionBean = (CRFVersionBean) crfVersionDao.findByPK(crfBuilder.getCrfBean().getId());
		assertEquals(crfVersionBean.getName(), "v1.0");
		assertTrue(crfVersionBean.getId() > 0);
	}
}