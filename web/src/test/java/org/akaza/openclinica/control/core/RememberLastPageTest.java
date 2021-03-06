package org.akaza.openclinica.control.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;

import com.clinovo.i18n.LocaleResolver;

public class RememberLastPageTest {

	public static final String URL = "url?param1=1";
	public static final String SAVED_URL = "savedUrl";
	public static final String DEFAULT_URL = "defUrl?param1=1";

	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private RememberLastPage rememberLastPage;

	@Before
	public void setUp() throws Exception {
		Locale locale = Locale.ENGLISH;

		MockHttpSession session = new MockHttpSession();
		request = new MockHttpServletRequest();
		request.setMethod("GET");
		LocaleResolver.updateLocale(request, locale);
		ResourceBundleProvider.updateLocale(LocaleResolver.getLocale(request));
		Whitebox.setInternalState(request, "session", session);

		session.setAttribute(SAVED_URL, URL);

		response = new MockHttpServletResponse();

		rememberLastPage = Mockito.mock(RememberLastPage.class);

		Mockito.when(rememberLastPage.getUrlKey()).thenReturn(SAVED_URL);
		Mockito.when(rememberLastPage.getDefaultUrl(request)).thenReturn(DEFAULT_URL);
		Mockito.when(rememberLastPage.getSavedUrl(SAVED_URL, request)).thenReturn(URL);
		Mockito.when(rememberLastPage.shouldRedirect(request, response)).thenCallRealMethod();
	}

	@Test
	public void testThatShouldRedirectReturnsTrue() throws Exception {
		Mockito.when(rememberLastPage.userDoesNotUseJmesaTableForNavigation(request)).thenReturn(true);
		boolean result = rememberLastPage.shouldRedirect(request, response);
		assertTrue(result);
	}

	@Test
	public void testThatShouldRedirectReturnsFalse() throws Exception {
		Mockito.when(rememberLastPage.userDoesNotUseJmesaTableForNavigation(request)).thenReturn(false);
		boolean result = rememberLastPage.shouldRedirect(request, response);
		assertFalse(result);
	}
}
