package com.clinovo.context;

import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.RequestEntity;

import com.clinovo.model.WebServiceResult;
import com.clinovo.rule.WebServiceAction;

public interface SubmissionContext {

	WebServiceAction getAction();

	void setHttpClient(HttpClient client);

	String authenticate() throws Exception;

	void setAction(WebServiceAction action);

	List<Header> getHttpHeaders() throws Exception;

	RequestEntity getRequestEntity() throws Exception;

	WebServiceResult processResponse(String response, int httpStatus) throws Exception;

}
