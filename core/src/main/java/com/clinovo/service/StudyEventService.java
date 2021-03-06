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
package com.clinovo.service;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;

/**
 * StudyEventService.
 */
public interface StudyEventService {

	/**
	 * Removes study event.
	 *
	 * @param studyEventBean
	 *            StudyEventBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void removeStudyEvent(StudyEventBean studyEventBean, UserAccountBean updater) throws Exception;

	/**
	 * Restores study event.
	 *
	 * @param studyEventBean
	 *            StudyEventBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void restoreStudyEvent(StudyEventBean studyEventBean, UserAccountBean updater) throws Exception;

	/**
	 * Locks study event.
	 *
	 * @param studyEventBean
	 *            StudyEventBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void lockStudyEvent(StudyEventBean studyEventBean, UserAccountBean updater) throws Exception;

	/**
	 * Unlocks study event.
	 *
	 * @param studyEventBean
	 *            StudyEventBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void unlockStudyEvent(StudyEventBean studyEventBean, UserAccountBean updater) throws Exception;

	/**
	 * Removes study events.
	 *
	 * @param studyEventDefinitionBean
	 *            StudyEventDefinitionBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void removeStudyEvents(StudyEventDefinitionBean studyEventDefinitionBean, UserAccountBean updater) throws Exception;

	/**
	 * Restores study events.
	 *
	 * @param studyEventDefinitionBean
	 *            StudyEventDefinitionBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void restoreStudyEvents(StudyEventDefinitionBean studyEventDefinitionBean, UserAccountBean updater)
			throws Exception;

	/**
	 * Removes study events.
	 *
	 * @param studySubjectBean
	 *            StudySubjectBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void removeStudyEvents(StudySubjectBean studySubjectBean, UserAccountBean updater) throws Exception;

	/**
	 * Restores study events.
	 *
	 * @param studySubjectBean
	 *            StudySubjectBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void restoreStudyEvents(StudySubjectBean studySubjectBean, UserAccountBean updater) throws Exception;

	/**
	 * Locks study events.
	 *
	 * @param studySubjectBean
	 *            StudySubjectBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void lockStudyEvents(StudySubjectBean studySubjectBean, UserAccountBean updater) throws Exception;

	/**
	 * unlocks study events.
	 *
	 * @param studySubjectBean
	 *            StudySubjectBean
	 * @param updater
	 *            UserAccountBean
	 * @throws Exception
	 *             an Exception
	 */
	void unlockStudyEvents(StudySubjectBean studySubjectBean, UserAccountBean updater) throws Exception;

	/**
	 * Deletes study event with all related data.
	 * 
	 * @param studyEventDefinitionBean
	 *            StudyEventDefinitionBean
	 * @param studySubjectBean
	 *            StudySubjectBean
	 * @param studyEventBean
	 *            StudyEventBean
	 * @param userAccountBean
	 *            UserAccountBean
	 */
	void deleteStudyEvent(StudyEventDefinitionBean studyEventDefinitionBean, StudySubjectBean studySubjectBean,
			StudyEventBean studyEventBean, UserAccountBean userAccountBean);
}
