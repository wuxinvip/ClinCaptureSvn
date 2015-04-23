package org.akaza.openclinica.dao.submit;

import org.akaza.openclinica.DefaultAppContextTest;
import org.junit.Test;

public class ItemFormMetadataDAOTest extends DefaultAppContextTest {

	@Test
	public void testTotalStudySubjects() {
		assertNotNull(imfdao.findAllByCRFVersionIdAndItemId(2, 5));
	}

	@Test
	public void testThatGetCrfSectionsMetricReturnsCorrectValue() {
		assertEquals(10, imfdao.getCrfSectionsMetric(1));
	}

	@Test
	public void testThatHasItemsToSDVReturnsCorrectValue() {
		assertFalse(imfdao.hasItemsToSDV(1));
	}

	@Test
	public void testThatFindAllCrfVersionItemMetadataReturnsCorrectNumberOfItems() {
		assertEquals(27, imfdao.findAllCrfVersionItemMetadata(1).size());
	}
}
