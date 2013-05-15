package com.clinovo.rule.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.ws.WebServiceException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.clinovo.BaseTest;
import com.clinovo.context.SubmissionContext;
import com.clinovo.context.impl.XMLSubmissionContext;
import com.clinovo.model.WebServiceResult;
import com.clinovo.rule.WebServiceAction;
import com.clinovo.util.XMLUtil;

public class HttpTransportProtocolTest extends BaseTest {

	private PostMethod method;
	private SubmissionContext context;
	private HttpTransportProtocol protocol;

	@Before
	public void setUp() throws Exception {

		context = new XMLSubmissionContext();

		WebServiceAction action = createWebServiceAction();

		context.setAction(action);

		method = createPostMethodMock(action.getRandomizationUrl(), XMLUtil.docToString(webServiceReturnValue));

		protocol = new HttpTransportProtocol();
		protocol.setHttpMethod(method);

		context.setAction(action);
		protocol.setSubmissionContext(context);

	}

	@Test(expected = WebServiceException.class)
	public void testThatCallThrowsExceptionWhenCalledWithInvalidInput() throws Exception {

		protocol.setSubmissionContext(null);
		protocol.call();
	}

	@Test(expected = WebServiceException.class)
	public void testThatFailedHttpCallRaisesWebServiceException() throws Exception {

		String failureMessage = "<result><message>Respect other people's security you tard</message></result>";

		Mockito.when(method.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);
		Mockito.when(method.getResponseBodyAsString()).thenReturn(failureMessage);

		protocol.call();
	}

	@Test(expected = WebServiceException.class)
	public void testThatUnVailableHttpReturnCodeHttpCallRaisesWebServiceException() throws Exception {

		String failureMessage = "<result><message>The randomization service is down</message></result>";

		Mockito.when(method.getResponseBodyAsString()).thenReturn(failureMessage);
		Mockito.when(method.getStatusCode()).thenReturn(HttpStatus.SC_SERVICE_UNAVAILABLE);

		protocol.call();
	}

	@Test
	public void testThatCallDoesNotReturnNull() throws Exception {

		WebServiceResult result = protocol.call();
		assertNotNull("Should never return null", result);
	}

	@Test
	public void testThatCallReturnsValidWebServiceResultWithTreatment() throws Exception {

		WebServiceResult result = protocol.call();

		assertNotNull("Should have a valid Treatment specified", result.getTreatment());
	}

	@Test
	public void testThatCallReturnsValidWebServiceResultWithACorrectTreatment() throws Exception {

		WebServiceResult result = protocol.call();

		assertEquals("Should have a correct Treatment specified", "2", result.getTreatment());
	}

	@Test
	public void testThatCallReturnsValidWebServiceResultWithPatientId() throws Exception {

		WebServiceResult result = protocol.call();

		assertNotNull("Should have a valid patient Id specified", result.getPatientId());
	}

	@Test
	public void testThatCallReturnsValidWebServiceResultWithACorrectPatientId() throws Exception {

		WebServiceResult result = protocol.call();

		assertEquals("Should have a correct patientId specified", "subject2", result.getPatientId());
	}

	@Test
	public void testThatCallReturnsValidWebServiceResultWithSiteId() throws Exception {

		WebServiceResult result = protocol.call();

		assertNotNull("Should have a valid Randomization result specified", result.getRandomizationResult());
	}

	@Test
	public void testThatCallReturnsValidWebServiceResultWithACorrectSiteId() throws Exception {

		WebServiceResult result = protocol.call();

		assertEquals("Should have a correct Randomization result specified", "radiotherapy",
				result.getRandomizationResult());
	}
}