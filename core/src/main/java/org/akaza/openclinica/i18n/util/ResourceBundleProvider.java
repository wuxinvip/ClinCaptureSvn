package org.akaza.openclinica.i18n.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import com.clinovo.i18n.ClinCaptureResourceBundle;

/**
 * ResourceBundleProvider.
 */
public class ResourceBundleProvider {

	/**
	 * A Map of the locales corresponding to each Thread.
	 */
	public static HashMap<Thread, Locale> localeMap = new HashMap<Thread, Locale>();
	/**
	 * Contains the set of ResourceBundles associated to each locale.
	 */
	static HashMap<Locale, HashMap<String, ResourceBundle>> resBundleSetMap = new HashMap<Locale, HashMap<String, ResourceBundle>>();

	public static synchronized void updateLocale(Locale locale) {
		localeMap.put(Thread.currentThread(), locale);
		if (!resBundleSetMap.containsKey(locale)) {
			HashMap<String, ResourceBundle> resBundleSet = new HashMap<String, ResourceBundle>();
			resBundleSet.put("org.akaza.openclinica.i18n.admin", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.admin", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.audit_events", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.audit_events", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.exceptions", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.exceptions", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.format", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.format", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.page_messages", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.page_messages", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.notes", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.notes", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.terms", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.terms", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.words", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.words", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.workflow", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.workflow", locale));
			resBundleSet.put("org.akaza.openclinica.i18n.buildNumber", new ClinCaptureResourceBundle(
					"org.akaza.openclinica.i18n.buildNumber", locale));
			resBundleSetMap.put(locale, resBundleSet);
		}
	}

	public static Locale getLocale() {
		return localeMap.get(Thread.currentThread());
	}

	public static ResourceBundle getAdminBundle() {
		return getResBundle("org.akaza.openclinica.i18n.admin");
	}

	public static ResourceBundle getAdminBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.admin", locale);
	}

	public static ResourceBundle getAuditEventsBundle() {
		return getResBundle("org.akaza.openclinica.i18n.audit_events");
	}

	public static ResourceBundle getAuditEventsBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.audit_events", locale);
	}

	public static ResourceBundle getExceptionsBundle() {
		return getResBundle("org.akaza.openclinica.i18n.exceptions");
	}

	public static ResourceBundle getExceptionsBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.exceptions", locale);
	}

	public static ResourceBundle getFormatBundle() {
		return getResBundle("org.akaza.openclinica.i18n.format");
	}

	public static ResourceBundle getFormatBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.format", locale);
	}

	public static ResourceBundle getPageMessagesBundle() {
		return getResBundle("org.akaza.openclinica.i18n.page_messages");
	}

	public static ResourceBundle getPageMessagesBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.page_messages", locale);
	}

	public static ResourceBundle getTermsBundle() {
		return getResBundle("org.akaza.openclinica.i18n.terms");
	}

	public static ResourceBundle getTermsBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.terms", locale);
	}

	public static ResourceBundle getWordsBundle() {
		return getResBundle("org.akaza.openclinica.i18n.words");
	}

	public static ResourceBundle getWordsBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.words", locale);
	}

	public static ResourceBundle getTextsBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.notes", locale);
	}

	public static ResourceBundle getTextsBundle() {
		return getResBundle("org.akaza.openclinica.i18n.notes");
	}

	public static ResourceBundle getWorkflowBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.workflow", locale);
	}

	public static ResourceBundle getWorkflowBundle() {
		return getResBundle("org.akaza.openclinica.i18n.workflow");
	}

	public static ResourceBundle getBuildNumberBundle(Locale locale) {
		return getResBundle("org.akaza.openclinica.i18n.buildNumber", locale);
	}

	public static ResourceBundle getBuildNumberBundle() {
		return getResBundle("org.akaza.openclinica.i18n.buildNumber");
	}

	/**
	 * Returns the required bundle, using the current thread to determine the appropiate locale.
	 * 
	 * @param name
	 *            requested bundle name.
	 * @return ResourceBundle
	 */
	private static ResourceBundle getResBundle(String name) {

		return resBundleSetMap.get(localeMap.get(Thread.currentThread())).get(name);
	}

	/**
	 * 
	 * @param name
	 *            Required bundle name
	 * @param locale
	 *            Required locale
	 * @return The corresponding ResourceBundle
	 */
	public static ResourceBundle getResBundle(String name, Locale locale) {
		return resBundleSetMap.get(locale).get(name);
	}

	/**
	 * Method returns property value from admin bundle.
	 * 
	 * @param key
	 *            String
	 * @return String
	 */
	public static String getResAdmin(String key) {
		return getAdminBundle().containsKey(key) ? getAdminBundle().getString(key) : key;
	}

	/**
	 * Method returns property value from terms bundle.
	 * 
	 * @param key
	 *            String
	 * @return String
	 */
	public static String getResTerm(String key) {
		return getTermsBundle().containsKey(key) ? getTermsBundle().getString(key) : key;
	}

	/**
	 * Method returns property value from words bundle.
	 * 
	 * @param key
	 *            String
	 * @return String
	 */
	public static String getResWord(String key) {
		return getWordsBundle().containsKey(key) ? getWordsBundle().getString(key) : key;
	}

	/**
	 * Method returns property value from notes bundle.
	 * 
	 * @param key
	 *            String
	 * @return String
	 */
	public static String getResNotes(String key) {
		return getTextsBundle().containsKey(key) ? getTextsBundle().getString(key) : key;
	}

	/**
	 * Method returns property value from format bundle.
	 *
	 * @param key String
	 * @return String
	 */
	public static String getResFormat(String key) {
		return getFormatBundle().containsKey(key) ? getFormatBundle().getString(key) : key;
	}
}
