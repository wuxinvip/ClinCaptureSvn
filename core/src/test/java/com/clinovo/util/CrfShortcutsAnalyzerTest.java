/*******************************************************************************
 * CLINOVO RESERVES ALL RIGHTS TO THIS SOFTWARE, INCLUDING SOURCE AND DERIVED BINARY CODE. BY DOWNLOADING THIS SOFTWARE YOU AGREE TO THE FOLLOWING LICENSE:
 *
 * Subject to the terms and conditions of this Agreement including, Clinovo grants you a non-exclusive, non-transferable, non-sublicenseable limited license without license fees to reproduce and use internally the software complete and unmodified for the sole purpose of running Programs on one computer.
 * This license does not allow for the commercial use of this software except by IRS approved non-profit organizations; educational entities not working in joint effort with for profit business.
 * To use the license for other purposes, including for profit clinical trials, an additional paid license is required. Please contact our licensing department at http://www.clincapture.com/contact for pricing information.
 *
 * You may not modify, decompile, or reverse engineer the software.
 * Clinovo disclaims any express or implied warranty of fitness for use.
 * No right, title or interest in or to any trademark, service mark, logo or trade name of Clinovo or its licensors is granted under this Agreement.
 * THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. CLINOVO FURTHER DISCLAIMS ALL WARRANTIES, EXPRESS AND IMPLIED, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.

 * LIMITATION OF LIABILITY. IN NO EVENT SHALL CLINOVO BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, PUNITIVE OR CONSEQUENTIAL DAMAGES, OR DAMAGES FOR LOSS OF PROFITS, REVENUE, DATA OR DATA USE, INCURRED BY YOU OR ANY THIRD PARTY, WHETHER IN AN ACTION IN CONTRACT OR TORT, EVEN IF ORACLE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. CLINOVO’S ENTIRE LIABILITY FOR DAMAGES HEREUNDER SHALL IN NO EVENT EXCEED TWO HUNDRED DOLLARS (U.S. $200).
 *******************************************************************************/
package com.clinovo.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.akaza.openclinica.DefaultAppContextTest;
import org.akaza.openclinica.bean.managestudy.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.service.StudyParameterConfig;
import org.akaza.openclinica.bean.submit.DisplayItemBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.SectionBean;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.service.DiscrepancyNoteThread;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class CrfShortcutsAnalyzerTest extends DefaultAppContextTest {

	public static final int THREE = 3;
	public static final int FOUR = 4;
	public static final int FIVE = 5;
	private DisplayItemBean displayItemBean;
	private DiscrepancyNoteBean discrepancyNoteBean;
	private List<DiscrepancyNoteThread> noteThreads;
	private CrfShortcutsAnalyzer crfShortcutsAnalyzer;

	@Before
	public void setUp() throws Exception {
		EventCRFBean eventCRFBean = new EventCRFBean();
		eventCRFBean.setId(1);
		eventCRFBean.setCRFVersionId(1);
		ItemDataBean itemDataBean = Mockito.mock(ItemDataBean.class);
		itemDataBean.setId(1);
		displayItemBean = new DisplayItemBean();
		displayItemBean.setData(itemDataBean);
		displayItemBean.setDbData(itemDataBean);
		ItemBean itemBean = new ItemBean();
		itemBean.setId(1);
		displayItemBean.setItem(itemBean);
		displayItemBean.setField("input1");
		noteThreads = new ArrayList<DiscrepancyNoteThread>();
		ArrayList<DiscrepancyNoteBean> discrepancyNotes = new ArrayList<DiscrepancyNoteBean>();
		discrepancyNoteBean = new DiscrepancyNoteBean();
		discrepancyNoteBean.setItemId(1);
		discrepancyNoteBean.setEntityType("itemData");
		discrepancyNoteBean.setParentDnId(0);
		discrepancyNoteBean.setField("input1");
		discrepancyNotes.add(discrepancyNoteBean);
		DiscrepancyNoteThread discrepancyNoteThread = new DiscrepancyNoteThread();
		discrepancyNoteThread.setLinkedNoteList(new LinkedList<DiscrepancyNoteBean>(discrepancyNotes));
		displayItemBean.setDiscrepancyNotes(discrepancyNotes);
		noteThreads.add(discrepancyNoteThread);
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put(CrfShortcutsAnalyzer.SECTION_ID, 0);
		attributes.put(CrfShortcutsAnalyzer.SERVLET_PATH, "");
		crfShortcutsAnalyzer = new CrfShortcutsAnalyzer("http", "post", "/uri", "/clincapture", "http", attributes,
				itemSDVService, new StudyParameterConfig());
	}

	@Test
	public void testThatIsFirstNewDnReturnsCorrectValue() throws Exception {
		discrepancyNoteBean.setResolutionStatusId(1);
		crfShortcutsAnalyzer.prepareCrfShortcutAnchors(displayItemBean, noteThreads, true);
		assertEquals(displayItemBean.getNewDn().size(), 1);
	}

	@Test
	public void testThatIsFirstUpdatedDnReturnsCorrectValue() throws Exception {
		discrepancyNoteBean.setResolutionStatusId(2);
		crfShortcutsAnalyzer.prepareCrfShortcutAnchors(displayItemBean, noteThreads, true);
		assertEquals(displayItemBean.getUpdatedDn().size(), 1);
	}

	@Test
	public void testThatIsFirstResolutionProposedReturnsCorrectValue() throws Exception {
		discrepancyNoteBean.setResolutionStatusId(THREE);
		crfShortcutsAnalyzer.prepareCrfShortcutAnchors(displayItemBean, noteThreads, true);
		assertEquals(displayItemBean.getResolutionProposedDn().size(), 1);
	}

	@Test
	public void testThatIsFirstClosedDnReturnsCorrectValue() throws Exception {
		discrepancyNoteBean.setResolutionStatusId(FOUR);
		crfShortcutsAnalyzer.prepareCrfShortcutAnchors(displayItemBean, noteThreads, true);
		assertEquals(displayItemBean.getClosedDn().size(), 1);
	}

	@Test
	public void testThatIsFirstAnnotationReturnsCorrectValue() throws Exception {
		discrepancyNoteBean.setResolutionStatusId(FIVE);
		crfShortcutsAnalyzer.prepareCrfShortcutAnchors(displayItemBean, noteThreads, true);
		assertEquals(displayItemBean.getAnnotationDn().size(), 1);
	}

	@Test
	public void testThatIsFirstItemToSDVReturnsCorrectValue() throws Exception {
		crfShortcutsAnalyzer.prepareCrfShortcutAnchors(displayItemBean, noteThreads, true);
		assertEquals(displayItemBean.getItemToSDV().size(), 0);
	}

	@Test
	public void testThatPrepareCrfShortcutLinksBuildCorrectUrlForNonPopupPage() throws Exception {
		buildAnalyzerUrl();
		assertEquals("#newDn_1", crfShortcutsAnalyzer.getNextNewDnLink());

	}

	@Test
	public void testThatPrepareCrfShortcutLinksBuildCorrectUrlForPopupPage() throws Exception {
		buildAnalyzerUrl();
		assertEquals("#newDn_1", crfShortcutsAnalyzer.getNextNewDnLink());

	}

	private void buildAnalyzerUrl() {

		EventCRFBean eventCRFBean = new EventCRFBean();
		eventCRFBean.setId(2);
		ItemFormMetadataDAO itemFormMetadataDAO = Mockito.mock(ItemFormMetadataDAO.class);
		ItemFormMetadataBean itemFormMetadataBean = new ItemFormMetadataBean();
		itemFormMetadataBean.setId(FOUR);
		Mockito.when(itemFormMetadataDAO.findByItemIdAndCRFVersionId(Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(itemFormMetadataBean);
		SectionBean section = new SectionBean();
		section.setId(FOUR);
		List<SectionBean> sectionBeans = new ArrayList<SectionBean>();
		sectionBeans.add(section);
		EventDefinitionCRFBean eventDefCRF = new EventDefinitionCRFBean();
		eventDefCRF.setId(6);
		crfShortcutsAnalyzer.prepareCrfShortcutLinks(eventCRFBean, itemFormMetadataDAO, eventDefCRF, sectionBeans,
				noteThreads);
	}
}
