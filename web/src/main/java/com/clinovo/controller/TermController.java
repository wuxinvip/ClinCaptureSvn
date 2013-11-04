package com.clinovo.controller;

import javax.servlet.http.HttpServletRequest;

import com.clinovo.model.CodedItem;
import com.clinovo.service.CodedItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.clinovo.model.Term;
import com.clinovo.service.TermService;

@Controller
public class TermController {

	@Autowired
	private TermService termService;

    @Autowired
    private CodedItemService itemService;
	
	/**
	 * Handle for deleting a given term from a custom dictionary.
	 * 
	 * @param request The request containing the term code.
	 * 
	 * @return Empty string
	 * 
	 * @throws Exception For all exceptions
	 */
    @RequestMapping("/deleteTerm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTermHandler(HttpServletRequest request) {

        String code = request.getParameter("code");
        String itemId = request.getParameter("item");
        String dictionary = request.getParameter("dictionary");

        CodedItem codedItem = itemService.findByItemData(Integer.valueOf(itemId));

        if(codedItem != null) {
            Term term = codedItem.isCoded() ? termService.findTermByCode(code) : termService.findByTermAndExternalDictionary(code, dictionary);

            if (term != null) {
                termService.deleteTerm(term);
            }
        }
    }
}
