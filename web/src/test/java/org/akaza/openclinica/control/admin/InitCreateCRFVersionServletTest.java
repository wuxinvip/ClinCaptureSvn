package org.akaza.openclinica.control.admin;

import static junit.framework.Assert.assertEquals;

import java.util.Locale;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.clinovo.i18n.LocaleResolver;

@SuppressWarnings("deprecation")
public class InitCreateCRFVersionServletTest {

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private InitCreateCRFVersionServlet initCreateCRFVersionServlet;

	@Before
	public void setUp() throws Exception {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		initCreateCRFVersionServlet = Mockito.mock(InitCreateCRFVersionServlet.class);

		request.setParameter("name", "CRF Name");
		request.setParameter("crfId", "1");

		Locale locale = Locale.ENGLISH;
		LocaleResolver.updateLocale(request, locale);
		ResourceBundleProvider.updateLocale(locale);
		Whitebox.setInternalState(initCreateCRFVersionServlet, "logger",
				LoggerFactory.getLogger("InitCreateCRFVersionServlet"));

		Mockito.doCallRealMethod().when(initCreateCRFVersionServlet).processRequest(request, response);
		Mockito.doCallRealMethod().when(initCreateCRFVersionServlet).getStudyInfoPanel(request);
	}

	@Test
	public void testThatInitCreateCRFVersionSetCRFNameInSession() throws Exception {
		initCreateCRFVersionServlet.processRequest(request, response);
		assertEquals("CRF Name", request.getSession().getAttribute("crfName"));
	}
}
