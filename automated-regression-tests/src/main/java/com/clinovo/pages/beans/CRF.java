package com.clinovo.pages.beans;

import java.util.Comparator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CRF{
	
	public static final String CRFS_TO_CHECK_SDV_STATUS = "CRFs to check SDV tatus";

	public static ComparatorForItemOIDs comparatorForItemOIDs = new ComparatorForItemOIDs();
	
	private Map<String, String> fieldNameToValueMap;
	private String markComplete = "no";

	public Map<String, String> getFieldNameToValueMap() {
		return fieldNameToValueMap;
	}

	public void setFieldNameToValueMap(Map<String, String> fieldNameToValueMap) {
		this.fieldNameToValueMap = fieldNameToValueMap;
	}

	public static CRF fillStudyDetailsFromTableRow(Map<String, String> values) {
		CRF crf = new CRF();
		if (values.containsKey("Mark Complete")) {
			crf.setMarkComplete(values.get("Mark Complete"));
			values.remove("Mark Complete");
		}
		crf.setFieldNameToValueMap(values);
		
		return crf;
	}

	public String getMarkComplete() {
		return markComplete;
	}

	public void setMarkComplete(String markComplete) {
		this.markComplete = markComplete;
	}
}

class ComparatorForItemOIDs implements Comparator<String> {
	public int compare(String str1, String str2) {
		
		//str = ({a})GROUP_NAME_{b}input{c}(T)
		int a1, b1, c1, a2, b2, c2;
		
		Pattern p = Pattern.compile("\\((\\d+)\\).+");
		Matcher m1 = p.matcher(str1);
		Matcher m2 = p.matcher(str2);
		a1 = m1.matches()? Integer.parseInt(m1.group(1)) : 0;
		a2 = m2.matches()? Integer.parseInt(m2.group(1)) : 0;
		
		p = Pattern.compile(".+_(\\d+)input.+");
		m1 = p.matcher(str1);
		m2 = p.matcher(str2);
		b1 = m1.matches()? Integer.parseInt(m1.group(1)) : 0;
		b2 = m2.matches()? Integer.parseInt(m2.group(1)) : 0;
		
		p = Pattern.compile(".*input(\\d+).*");
		m1 = p.matcher(str1);
		m2 = p.matcher(str2);
		c1 = m1.matches()? Integer.parseInt(m1.group(1)) : 0;
		c2 = m2.matches()? Integer.parseInt(m2.group(1)) : 0;
		
		if (a1 > a2) return 1;
		if (a2 > a1) return -1;
		if (b1 > b2) return 1;
		if (b2 > b1) return -1;
		if (c1 > c2) return 1;
		if (c2 > c1) return -1;
		
		return 0;
	}
}