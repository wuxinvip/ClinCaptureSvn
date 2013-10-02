package com.clinovo.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.akaza.openclinica.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.clinovo.model.Status.CodeStatus;

@Entity
@Table(name = "coded_item")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "coded_item_id_seq") })
public class CodedItem extends AbstractMutableDomainObject {

	private int itemId = -1;
	private int subjectId = -1;
	private int itemDataId = -1;
	private int eventCrfId = -1;
	private int crfVersionId = -1;
	
	private String codedTerm = "";
	private String dictionary = "";
	private String verbatimTerm = "";
	private String status = String.valueOf(CodeStatus.NOT_CODED);

	public String getCodedTerm() {
		return codedTerm;
	}

	public void setCodedTerm(String codedTerm) {
		this.codedTerm = codedTerm;
	}

	public String getDictionary() {
		return dictionary;
	}

	public void setDictionary(String dictionary) {
		this.dictionary = dictionary;
	}

	public String getVerbatimTerm() {
		return verbatimTerm;
	}

	public void setVerbatimTerm(String verbatimTerm) {
		this.verbatimTerm = verbatimTerm;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int getEventCrfId() {
		return this.eventCrfId;
	}
	
	public void setEventCrfId(int eventCRFId) {
		this.eventCrfId = eventCRFId;
	}

	@Transient
	public CodeStatus getCodeStatus() {
		return CodeStatus.valueOf(status);
	}

	@Transient
	public boolean isCoded() {

		return !this.status.equals("NOT_CODED");
	}

	public int getCrfVersionId() {
		return this.crfVersionId;
	}
	
	public void setCrfVersionId(int crfVersionId) {
		this.crfVersionId = crfVersionId;
	}

	public int getSubjectId() {
		return this.subjectId;
	}
	
	public void setSubjectId(int studySubjectId) {
		this.subjectId = studySubjectId;
	}

	public int getItemDataId() {
		return itemDataId;
	}

	public void setItemDataId(int itemDataId) {
		this.itemDataId = itemDataId;
	}
}