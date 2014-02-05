package com.clinovo.util;

import org.akaza.openclinica.control.core.BaseController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class PageMessagesUtil {

	private PageMessagesUtil() {
	}

	public static void addPageMessage(HttpServletRequest request, String pageMessage) {
		List<String> pageMessages = (List<String>) request.getAttribute(BaseController.PAGE_MESSAGE);
		if (pageMessages == null) {
			pageMessages = new ArrayList<String>();
			request.setAttribute(BaseController.PAGE_MESSAGE, pageMessages);
		}
		pageMessages.add(pageMessage);
	}
}