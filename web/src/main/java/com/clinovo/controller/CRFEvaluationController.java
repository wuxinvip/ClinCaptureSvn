package com.clinovo.controller;

import com.clinovo.model.CRFEvaluationTableFactory;
import com.clinovo.util.SessionUtil;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.control.core.BaseController;
import org.akaza.openclinica.dao.service.StudyParameterValueDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * The controller for managing crf evaluation page.
 *
 */
@Controller
public class CRFEvaluationController extends Redirection {

	public static final Logger LOGGER = LoggerFactory.getLogger(CRFEvaluationController.class);

	public static final String STUDY = "study";
	public static final String SHOW_MORE_LINK = "showMoreLink";
	public static final String MAIN_MENU_REDIRECT = "redirect:/MainMenu";
	public static final String CRF_EVALUATION_TABLE = "crfEvaluationTable";
	public static final String PAGE_CRF_EVALUATION = "/pages/crfEvaluation";
	public static final String EVALUATE_WITH_CONTEXT = "evaluateWithContext";
	public static final String EVALUATION_CRF_EVALUATION = "evaluation/crfEvaluation";
	public static final String NO_PERMISSION_TO_EVALUATE = "no_permission_to_evaluate";

	@Autowired
	private DataSource dataSource;

	@Autowired
	private MessageSource messageSource;

	/**
	 * Handle requests from the crf evaluation page.
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @return String
	 * @throws Exception
	 *             an exception
	 */
	@RequestMapping("/crfEvaluation")
	public String crfEvaluation(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String page = EVALUATION_CRF_EVALUATION;
		StudyBean currentStudy = (StudyBean) request.getSession().getAttribute(STUDY);
		StudyUserRoleBean userRole = (StudyUserRoleBean) request.getSession().getAttribute(BaseController.USER_ROLE);
		UserAccountBean userAccountBean = (UserAccountBean) request.getSession().getAttribute(
				BaseController.USER_BEAN_NAME);

		BaseController.removeLockedCRF(userAccountBean.getId());

		if (userRole.getRole() == Role.SYSTEM_ADMINISTRATOR || userRole.getRole() == Role.STUDY_ADMINISTRATOR
				|| userRole.getRole() == Role.STUDY_EVALUATOR) {
			request.setAttribute(
					CRF_EVALUATION_TABLE,
					new CRFEvaluationTableFactory(dataSource, messageSource, new StudyParameterValueDAO(dataSource)
							.findByHandleAndStudy(currentStudy.getId(), EVALUATE_WITH_CONTEXT), request
							.getParameter(SHOW_MORE_LINK)).createTable(request, response).render());
		} else {
			org.akaza.openclinica.control.core.Controller.addPageMessage(
					messageSource.getMessage(NO_PERMISSION_TO_EVALUATE, null, SessionUtil.getLocale(request)), request,
					LOGGER);
			org.akaza.openclinica.control.core.Controller.storePageMessages(request);
			page = MAIN_MENU_REDIRECT;
		}
		return page;
	}

	public String getUrl() {
		return PAGE_CRF_EVALUATION;
	}
}