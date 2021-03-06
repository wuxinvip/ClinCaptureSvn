/*******************************************************************************
 * ClinCapture, Copyright (C) 2009-2013 Clinovo Inc.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the Lesser GNU General Public License 
 * as published by the Free Software Foundation, either version 2.1 of the License, or(at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Lesser GNU General Public License for more details.
 * 
 * You should have received a copy of the Lesser GNU General Public License along with this program.  
 \* If not, see <http://www.gnu.org/licenses/>. Modified by Clinovo Inc 01/29/2013.
 ******************************************************************************/

/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.logic.expressionTree;

import java.util.Date;
import java.util.HashMap;

import org.akaza.openclinica.bean.core.ItemDataType;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.submit.SubjectBean;
import org.akaza.openclinica.exception.OpenClinicaSystemException;
import org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Krikor Krumlian
 * 
 */
public class OpenClinicaExpressionParser {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	TextIO textIO;
	private ExpressionService expressionService;
	private final String ERROR_MESSAGE_KEY = "OCRERR_0005";

	public static final String ONE = "1";
	public static final String NOT_USED = "not_used";

	private HashMap<String, String> testValues;
	private HashMap<String, String> responseTestValues;

	private Date subjectDob;
	private Date subjectEnrollment;
	private boolean importRulesMode = true;
	private DateTimeZone targetTimeZone;

	public OpenClinicaExpressionParser() {
		textIO = new TextIO();
		this.targetTimeZone = DateTimeZone.getDefault();
	}

	public HashMap<String, String> getTestValues() {
		return testValues;
	}

	public void setTestValues(HashMap<String, String> testValues) {
		this.testValues = testValues;
	}

	public HashMap<String, String> getResponseTestValues() {
		if (responseTestValues == null) {
			responseTestValues = new HashMap<String, String>();
		}
		return responseTestValues;
	}

	public void setResponseTestValues(HashMap<String, String> responseTestValues) {
		this.responseTestValues = responseTestValues;
	}

	public OpenClinicaExpressionParser(ExpressionService expressionService, DateTimeZone targetTimeZone) {
		textIO = new TextIO();
		this.expressionService = expressionService;
		this.targetTimeZone = targetTimeZone;
	}

	public OpenClinicaExpressionParser(StudyBean currentStudy, SubjectBean subjectBean,
			StudySubjectBean studySubjectBean, ExpressionService expressionService, DateTimeZone targetTimeZone) {
		this(expressionService, targetTimeZone);
		importRulesMode = false;
		if (currentStudy.getStudyParameterConfig().getCollectDob().equalsIgnoreCase(ONE)) {
			setSubjectDob(subjectBean.getDateOfBirth());
		}
		if (!currentStudy.getStudyParameterConfig().getDateOfEnrollmentForStudyRequired().equalsIgnoreCase(NOT_USED)) {
			setSubjectEnrollment(studySubjectBean.getEnrollmentDate());
		}
	}

	public void parseExpression(String expression) throws OpenClinicaSystemException {
		getTextIO().fillBuffer(expression);
		getTextIO().skipBlanks();
		ExpressionNode exp = expressionTree(false);
		if (getTextIO().peek() != '\n')
			throw new OpenClinicaSystemException(ERROR_MESSAGE_KEY);
		exp.printStackCommands();
	}

	public String parseAndEvaluateExpression(String expression) {
		return parseAndEvaluateExpression(expression, false);
	}

	public String parseAndEvaluateExpression(String expression, Boolean optimiseRuleValidator)
			throws OpenClinicaSystemException {
		getTextIO().fillBuffer(expression);
		getTextIO().skipBlanks();
		ExpressionNode exp = expressionTree(optimiseRuleValidator);
		if (getTextIO().peek() != '\n')
			throw new OpenClinicaSystemException(ERROR_MESSAGE_KEY);
		return exp.calculate();
	}

	public String parseAndTestEvaluateExpression(String expression) throws OpenClinicaSystemException {
		getTextIO().fillBuffer(expression);
		getTextIO().skipBlanks();
		ExpressionNode exp = expressionTree(false);
		if (getTextIO().peek() != '\n')
			throw new OpenClinicaSystemException(ERROR_MESSAGE_KEY);
		return exp.testValue();

	}

	public HashMap<String, String> parseAndTestEvaluateExpression(String expression, HashMap<String, String> h)
			throws OpenClinicaSystemException {
		getTextIO().fillBuffer(expression);
		getTextIO().skipBlanks();
		ExpressionNode exp = expressionTree(false);

		if (getTextIO().peek() != '\n')
			throw new OpenClinicaSystemException(ERROR_MESSAGE_KEY);
		setTestValues(h);
		// HashMap<String, String> theTestValues = getTestValues();
		HashMap<String, String> theTestValues = getResponseTestValues();
		theTestValues.put("result", exp.testValue());
		return theTestValues;

	}

	/**
	 * Reads an expression from the current line of input and builds an expression tree that represents the expression.
	 * 
	 * @return an ExpNode which is a pointer to the root node of the expression tree
	 * @throws OpenClinicaSystemException
	 *             if a syntax error is found in the input
	 */
	protected ExpressionNode expressionTree() throws OpenClinicaSystemException {
		return expressionTree(false);
	}

	protected ExpressionNode expressionTree(Boolean optimiseRuleValidator) throws OpenClinicaSystemException {
		try {
			textIO.skipBlanks();
			boolean negative; // True if there is a leading minus sign.
			negative = false;
			if (textIO.peek() == '-') {
				textIO.getAnyChar();
				negative = true;
			}
			ExpressionNode exp; // The expression tree for the expression.
			exp = termTree3(optimiseRuleValidator); // Start with the first term.
			if (negative)
				exp = new UnaryMinusNode(exp);
			textIO.skipBlanks();

			while (textIO.peek() == 'o' && textIO.peek(3).matches("or ") || textIO.peek() == 'a'
					&& textIO.peek(4).matches("and ")) {
				// Read the next term and combine it with the
				// previous terms into a bigger expression tree.
				// char op = textIO.getAnyChar();
				String op = textIO.peek() == 'o' ? textIO.getAnyString(3) : textIO.getAnyString(4);
				logger.info("Operator" + op);
				ExpressionNode nextTerm = termTree3(optimiseRuleValidator);
				exp = ExpressionNodeFactory.getExpNode(Operator.getByDescription(op), exp, nextTerm);
				textIO.skipBlanks();
			}
			exp.setExpressionParser(this);
			exp.setExpressionService(expressionService);
			return exp;
		} catch (NullPointerException e) {
			throw new OpenClinicaSystemException(ERROR_MESSAGE_KEY);
		} catch (OpenClinicaSystemException e) {
			throw e;
		}
	} // end expressionTree()

	private ExpressionNode termTree3(Boolean optimiseRuleValidator) throws OpenClinicaSystemException {
		textIO.skipBlanks();
		ExpressionNode term; // The expression tree representing the term.
		term = termTree2(optimiseRuleValidator);
		textIO.skipBlanks();

		while (textIO.peek() == 'e' && textIO.peek(3).matches("eq ") || textIO.peek() == 'n'
				&& textIO.peek(3).matches("ne ") || textIO.peek() == 'c' && textIO.peek(3).matches("ct ")
				|| textIO.peek() == 'n' && textIO.peek(4).matches("nct ") || textIO.peek() == 'g'
				&& textIO.peek(3).matches("gt ") || textIO.peek() == 'g' && textIO.peek(4).matches("gte ")
				|| textIO.peek() == 'l' && textIO.peek(3).matches("lt ") || textIO.peek() == 'l'
				&& textIO.peek(4).matches("lte ")) {
			// Read the next term and combine it with the
			// previous terms into a bigger expression tree.
			// char op = textIO.getAnyChar();
			String op = textIO.peek(4).matches("gte ") || textIO.peek(4).matches("lte ")
					|| textIO.peek(4).matches("nct ") ? textIO.getAnyString(4) : textIO.getAnyString(3);
			ExpressionNode nextTerm = termTree2(optimiseRuleValidator);
			term = ExpressionNodeFactory.getExpNode(Operator.getByDescription(String.valueOf(op)), term, nextTerm);
			// term = new BooleanOpNode(Operator.getByDescription(op), term,
			// nextTerm);
			textIO.skipBlanks();
		}
		term.setExpressionParser(this);
		return term;
	} // end termValue()

	private ExpressionNode termTree2(Boolean optimiseRuleValidator) throws OpenClinicaSystemException {
		textIO.skipBlanks();
		ExpressionNode term; // The expression tree representing the term.
		term = termTree(optimiseRuleValidator);
		textIO.skipBlanks();

		while (textIO.peek() == '+' || textIO.peek() == '-') {
			// Read the next term and combine it with the
			// previous terms into a bigger expression tree.
			char op = textIO.getAnyChar();
			ExpressionNode nextTerm = termTree(optimiseRuleValidator);
			term = ExpressionNodeFactory.getExpNode(Operator.getByDescription(String.valueOf(op)), term, nextTerm);
			// term = new
			// BinOpNode(Operator.getByDescription(String.valueOf(op)), term,
			// nextTerm);
			textIO.skipBlanks();
		}
		term.setExpressionParser(this);
		return term;
	} // end termValue()

	/**
	 * Reads a term from the current line of input and builds an expression tree that represents the expression.
	 * 
	 * @return an ExpNode which is a pointer to the root node of the expression tree
	 * @throws OpenClinicaSystemException
	 *             if a syntax error is found in the input
	 */

	private ExpressionNode termTree(Boolean optimiseRuleValidator) throws OpenClinicaSystemException {
		textIO.skipBlanks();
		ExpressionNode term; // The expression tree representing the term.
		term = factorTree(optimiseRuleValidator);
		textIO.skipBlanks();

		while (textIO.peek() == '*' || textIO.peek() == '/') {
			// Read the next factor, and combine it with the
			// previous factors into a bigger expression tree.
			char op = textIO.getAnyChar();
			ExpressionNode nextFactor = factorTree(optimiseRuleValidator);
			term = ExpressionNodeFactory.getExpNode(Operator.getByDescription(String.valueOf(op)), term, nextFactor);
			// term = new
			// BinOpNode(Operator.getByDescription(String.valueOf(op)), term,
			// nextFactor);
			textIO.skipBlanks();
		}
		term.setExpressionParser(this);
		return term;
	} // end termValue()

	/**
	 * Reads a factor from the current line of input and builds an expression tree that represents the expression.
	 * 
	 * @return an ExpNode which is a pointer to the root node of the expression tree
	 * @throws OpenClinicaSystemException
	 *             if a syntax error is found in the input
	 */

	private ExpressionNode factorTree(Boolean optimiseRuleValidator) throws OpenClinicaSystemException {
		ExpressionNode resultNode = null;
		textIO.skipBlanks();
		char ch = textIO.peek();
		logger.info("TheChar is : " + ch);
		if (Character.isDigit(ch)) {
			String dateOrNum = textIO.getDate();
			if (dateOrNum == null) {
				dateOrNum = String.valueOf(textIO.getDouble());

			}
			logger.info("TheNum is : " + dateOrNum);
			resultNode = new ConstantNode(dateOrNum);
		} else if (ch == '(') {
			// The factor is an expression in parentheses.
			// Return a tree representing that expression.
			textIO.getAnyChar(); // Read the "("
			ExpressionNode exp = expressionTree(optimiseRuleValidator);
			textIO.skipBlanks();
			if (textIO.peek() != ')')
				throw new OpenClinicaSystemException("OCRERR_0006");
			textIO.getAnyChar(); // Read the ")"
			return exp;
		} else if (String.valueOf(ch).matches("\\w+")) {
			String k = textIO.getWord();
			logger.info("TheWord 1 is : " + k);
			if (optimiseRuleValidator) {
				resultNode = new OpenClinicaVariableNode(k, expressionService, targetTimeZone, optimiseRuleValidator);
			} else {
				resultNode = new OpenClinicaVariableNode(k, expressionService, targetTimeZone);
			}

		} else if (String.valueOf(ch).matches("\"")) {
			String k = textIO.getDoubleQuoteWord();
			logger.info("TheWord 2 is : " + k);
			resultNode = new ConstantNode(k);
		} else if (ch == '\n')
			throw new OpenClinicaSystemException("OCRERR_0007");
		else if (ch == ')')
			throw new OpenClinicaSystemException("OCRERR_0008");
		else if (ch == '+' || ch == '-' || ch == '*' || ch == '/')
			throw new OpenClinicaSystemException("OCRERR_0009");
		else
			throw new OpenClinicaSystemException("OCRERR_0010", new Object[] { ch });
		resultNode.setExpressionParser(this);
		return resultNode;
	} // end factorTree()

	/**
	 * @return the textIO
	 */
	public TextIO getTextIO() {
		return textIO;
	}

	/**
	 * @param textIO
	 *            the textIO to set
	 */
	public void setTextIO(TextIO textIO) {
		this.textIO = textIO;
	}

	public Date getSubjectDob() {
		return subjectDob;
	}

	public void setSubjectDob(Date subjectDob) {
		this.subjectDob = subjectDob;
	}

	public Date getSubjectEnrollment() {
		return subjectEnrollment;
	}

	public void setSubjectEnrollment(Date subjectEnrollment) {
		this.subjectEnrollment = subjectEnrollment;
	}

	public boolean isImportRulesMode() {
		return importRulesMode;
	}

	public void setImportRulesMode(boolean importRulesMode) {
		this.importRulesMode = importRulesMode;
	}

	public boolean isDateItem() {
		boolean result = false;
		try {
			result = expressionService.getExpressionWrapper().getRuleSet().getItem().getItemDataTypeId() == ItemDataType.DATE
					.getId();
		} catch (Exception ex) {
			//
		}
		return result;
	}
	/*
	 * public static void main(String[] args) { SimpleParser4 smp4 = new SimpleParser4(); // textIO.putln("\n\nEnter an
	 * expression, or press return to end."); // textIO.put("\n? "); smp4.getTextIO().fillBuffer(" ((2 +2 * 4+2 *2 gte
	 * 15) or false or false or 3+4 * 2 lt 10) and yellow eq \"yellow\" "); //
	 * smp4.getTextIO().fillBuffer(" \"yellow\" eq yellow "); smp4.getTextIO().skipBlanks(); try { ExpNode exp =
	 * smp4.expressionTree(); logger.info("\nValue 1 is " + exp.value());
	 * logger.info("\nOrder of postfix evaluation is:\n"); exp.printStackCommands(); } catch (OpenClinicaSystemException
	 * e) { logger.info("\n*** Error in input: " + e.getMessage()); logger.info("*** Discarding input: " +
	 * smp4.getTextIO().getln()); } } // end main()
	 */
}
