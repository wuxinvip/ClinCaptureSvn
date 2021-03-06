/*******************************************************************************
 * ClinCapture, Copyright (C) 2009-2015 Clinovo Inc.
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

/* OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 2003-2005 Akaza
 * Research
 *
 */
package org.akaza.openclinica.bean.extract.odm;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.odmbeans.AuditLogBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.ChildNoteBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNoteBean;
import org.akaza.openclinica.bean.odmbeans.DiscrepancyNotesBean;
import org.akaza.openclinica.bean.odmbeans.OdmClinicalDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportFormDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportStudyEventDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ExportSubjectDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemDataBean;
import org.akaza.openclinica.bean.submit.crfdata.ImportItemGroupDataBean;
import org.akaza.openclinica.bean.submit.crfdata.SubjectGroupDataBean;
import org.apache.commons.lang.StringEscapeUtils;
import org.joda.time.format.DateTimeFormat;

import com.clinovo.util.DateUtil;

/**
 * Create ODM XML ClinicalData Element for a study.
 * 
 * @author ywang (May, 2008)
 */

public class ClinicalDataReportBean extends OdmXmlReportBean {

	private OdmClinicalDataBean clinicalData;

	public ClinicalDataReportBean(OdmClinicalDataBean clinicaldata) {
		super();
		this.clinicalData = clinicaldata;
	}

	/**
	 * has not been implemented yet
	 */
	@Override
	public void createOdmXml(boolean isDataset) {
	}

	public void addNodeClinicalData(boolean header, boolean footer) {

		String odmVersion = this.getODMVersion();
		// when collecting data, only item with value has been collected.
		StringBuffer xml = this.getXmlOutput();
		String indent = this.getIndent();
		String nls = "\n";
		if (header) {
			xml.append(indent + "<ClinicalData StudyOID=\"" + StringEscapeUtils.escapeXml(clinicalData.getStudyOID())
					+ "\" MetaDataVersionOID=\""
					+ StringEscapeUtils.escapeXml(this.clinicalData.getMetaDataVersionOID()) + "\">");
			xml.append(nls);
		}
		ArrayList<ExportSubjectDataBean> subs = (ArrayList<ExportSubjectDataBean>) this.clinicalData
				.getExportSubjectData();
		for (ExportSubjectDataBean sub : subs) {
			xml.append(indent + indent + "<SubjectData SubjectKey=\""
					+ StringEscapeUtils.escapeXml(sub.getSubjectOID()));
			if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
				xml.append("\" OpenClinica:StudySubjectID=\"" + StringEscapeUtils.escapeXml(sub.getStudySubjectId()));
				String uniqueIdentifier = sub.getUniqueIdentifier();
				if (uniqueIdentifier != null && uniqueIdentifier.length() > 0) {
					xml.append("\" OpenClinica:UniqueIdentifier=\"" + StringEscapeUtils.escapeXml(uniqueIdentifier));
				}
				String status = sub.getStatus();
				if (status != null && status.length() > 0) {
					xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
				}
				String secondaryId = sub.getSecondaryId();
				if (secondaryId != null && secondaryId.length() > 0) {
					xml.append("\"  OpenClinica:SecondaryID=\"" + StringEscapeUtils.escapeXml(secondaryId));
				}
				Integer year = sub.getYearOfBirth();
				if (year != null) {
					xml.append("\" OpenClinica:YearOfBirth=\"" + sub.getYearOfBirth());
				} else {
					if (sub.getDateOfBirth() != null) {
						xml.append("\" OpenClinica:DateOfBirth=\""
								+ DateTimeFormat.forPattern(DateUtil.DatePattern.ISO_DATE.getPattern())
								.print(sub.getDateOfBirth().getTime()));
					}
				}
				String gender = sub.getSubjectGender();
				if (gender != null && gender.length() > 0) {
					xml.append("\" OpenClinica:Sex=\"" + StringEscapeUtils.escapeXml(gender));
				}
			}
			xml.append("\">");
			xml.append(nls);
			//
			ArrayList<ExportStudyEventDataBean> ses = (ArrayList<ExportStudyEventDataBean>) sub
					.getExportStudyEventData();
			for (ExportStudyEventDataBean se : ses) {
				xml.append(indent + indent + indent + "<StudyEventData StudyEventOID=\""
						+ StringEscapeUtils.escapeXml(se.getStudyEventOID()));
				if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
					String location = se.getLocation();
					if (location != null && location.length() > 0) {
						xml.append("\" OpenClinica:StudyEventLocation=\"" + StringEscapeUtils.escapeXml(location));
					}
					DateUtil.DatePattern datePattern;
					if (se.getStartDate() != null) {
						datePattern = se.getStartTimeFlag() ? DateUtil.DatePattern.ISO_TIMESTAMP
								: DateUtil.DatePattern.ISO_DATE;
						xml.append("\" OpenClinica:StartDate=\""
								+ DateUtil.printDate(se.getStartDate(), getTargetTimeZoneId(), datePattern));
					}
					if (se.getEndDate() != null) {
						datePattern = se.getEndTimeFlag() ? DateUtil.DatePattern.ISO_TIMESTAMP
								: DateUtil.DatePattern.ISO_DATE;
						xml.append("\" OpenClinica:EndDate=\""
								+ DateUtil.printDate(se.getEndDate(), getTargetTimeZoneId(), datePattern));
					}
					String status = se.getStatus();
					if (status != null && status.length() > 0) {
						xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
					}
					if (se.getAgeAtEvent() != null) {
						xml.append("\" OpenClinica:SubjectAgeAtEvent=\"" + se.getAgeAtEvent());
					}
				}
				xml.append("\"");
				if (!"-1".equals(se.getStudyEventRepeatKey())) {
					xml.append(" StudyEventRepeatKey=\"" + se.getStudyEventRepeatKey() + "\"");
				}
				xml.append(">");
				xml.append(nls);
				//
				ArrayList<ExportFormDataBean> forms = se.getExportFormData();
				for (ExportFormDataBean form : forms) {
					xml.append(indent + indent + indent + indent + "<FormData FormOID=\""
							+ StringEscapeUtils.escapeXml(form.getFormOID()));
					if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
						String crfVersion = form.getCrfVersion();
						if (crfVersion != null && crfVersion.length() > 0) {
							xml.append("\" OpenClinica:Version=\"" + StringEscapeUtils.escapeXml(crfVersion));
						}
						String interviewerName = form.getInterviewerName();
						if (interviewerName != null && interviewerName.length() > 0) {
							xml.append("\" OpenClinica:InterviewerName=\""
									+ StringEscapeUtils.escapeXml(interviewerName));
						}
						if (form.getInterviewDate() != null) {
							xml.append("\" OpenClinica:InterviewDate=\"" + DateUtil.printDate(form.getInterviewDate(),
									getTargetTimeZoneId(), DateUtil.DatePattern.ISO_DATE));
						}
						String status = form.getStatus();
						if (status != null && status.length() > 0) {
							if (form.isShowStatus()) {
								xml.append("\" OpenClinica:Status=\"" + StringEscapeUtils.escapeXml(status));
							}
							Set<Integer> sectionIdList = clinicalData.getPartialSectionIdMap().get(form.getId());
							if (status.equalsIgnoreCase(Status.PARTIAL_DATA_ENTRY.getName()) && sectionIdList != null
									&& sectionIdList.size() > 0) {
								xml.append("\" " + "PartialSections=\""
										+ sectionIdList.toString().replaceAll("\\[|\\]", ""));
							}
						}
					}

					xml.append("\">");
					xml.append(nls);

					ArrayList<ImportItemGroupDataBean> igs = form.getItemGroupData();
					for (ImportItemGroupDataBean ig : igs) {
						xml.append(indent + indent + indent + indent + indent + "<ItemGroupData ItemGroupOID=\""
								+ StringEscapeUtils.escapeXml(ig.getItemGroupOID()) + "\" ");
						if (!"-1".equals(ig.getItemGroupRepeatKey())) {
							xml.append("ItemGroupRepeatKey=\"" + ig.getItemGroupRepeatKey() + "\" ");
						}
						xml.append("TransactionType=\"Insert\">");
						xml.append(nls);
						ArrayList<ImportItemDataBean> items = ig.getItemData();
						for (ImportItemDataBean item : items) {
							boolean printValue = true;
							xml.append(indent + indent + indent + indent + indent + indent + "<ItemData ItemOID=\""
									+ StringEscapeUtils.escapeXml(item.getItemOID()) + "\" ");
							if ("Yes".equals(item.getIsNull())) {
								xml.append("IsNull=\"Yes\"");
								if (!item.isHasValueWithNull()) {
									printValue = false;
								}
								if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
									xml.append(" OpenClinica:ReasonForNull=\""
											+ StringEscapeUtils.escapeXml(item.getReasonForNull()) + "\" ");
									if (!printValue) {
										xml.append("/>");
										xml.append(nls);
									}
								}
							}
							if (printValue) {
								Boolean hasElm = false;
								xml.append("Value=\"" + StringEscapeUtils.escapeXml(item.getValue()) + "\"");

								String muRefOid = item.getMeasurementUnitRef().getElementDefOID();
								if (muRefOid != null && muRefOid.length() > 0) {
									if (hasElm) {
									} else {
										xml.append(">");
										xml.append(nls);
										hasElm = true;
									}
									xml.append(indent + indent + indent + indent + indent + indent + indent
											+ "<MeasurementUnitRef MeasurementUnitOID=\""
											+ StringEscapeUtils.escapeXml(muRefOid) + "\"/>");
									xml.append(nls);
								}

								if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
									if (item.getAuditLogs() != null && item.getAuditLogs().getAuditLogs().size() > 0) {
										if (hasElm) {
										} else {
											xml.append(">");
											xml.append(nls);
											hasElm = true;
										}
										this.addAuditLogs(item.getAuditLogs(), indent + indent + indent + indent
												+ indent + indent + indent);
									}
									//
									if (item.getDiscrepancyNotes() != null
											&& item.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
										if (!hasElm) {
											xml.append(">");
											xml.append(nls);
											hasElm = true;
										}
										this.addDiscrepancyNotes(item.getDiscrepancyNotes(), indent + indent + indent
												+ indent + indent + indent + indent);
									}
								}
								if (hasElm) {
									xml.append(indent + indent + indent + indent + indent + indent + "</ItemData>");
									xml.append(nls);
									hasElm = false;
								} else {
									xml.append("/>");
									xml.append(nls);
								}
							}
						}
						xml.append(indent + indent + indent + indent + indent + "</ItemGroupData>");
						xml.append(nls);
					}
					//
					if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
						if (form.getAuditLogs() != null && form.getAuditLogs().getAuditLogs().size() > 0) {
							this.addAuditLogs(form.getAuditLogs(), indent + indent + indent + indent + indent);
						}
						//
						if (form.getDiscrepancyNotes() != null
								&& form.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
							this.addDiscrepancyNotes(form.getDiscrepancyNotes(), indent + indent + indent + indent
									+ indent);
						}
					}
					xml.append(indent + indent + indent + indent + "</FormData>");
					xml.append(nls);
				}
				//
				if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
					if (se.getAuditLogs() != null && se.getAuditLogs().getAuditLogs().size() > 0) {
						this.addAuditLogs(se.getAuditLogs(), indent + indent + indent + indent);
					}
					//
					if (se.getDiscrepancyNotes() != null && se.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
						this.addDiscrepancyNotes(se.getDiscrepancyNotes(), indent + indent + indent + indent);
					}
				}
				xml.append(indent + indent + indent + "</StudyEventData>");
				xml.append(nls);
			}
			if ("oc1.2".equalsIgnoreCase(odmVersion) || "oc1.3".equalsIgnoreCase(odmVersion)) {
				ArrayList<SubjectGroupDataBean> sgddata = (ArrayList<SubjectGroupDataBean>) sub.getSubjectGroupData();
				if (sgddata.size() > 0) {
					for (SubjectGroupDataBean sgd : sgddata) {
						String cid = sgd.getStudyGroupClassId() != null ? "OpenClinica:StudyGroupClassID=\""
								+ StringEscapeUtils.escapeXml(sgd.getStudyGroupClassId()) + "\" " : "";
						if (cid.length() > 0) {
							String cn = sgd.getStudyGroupClassName() != null ? "OpenClinica:StudyGroupClassName=\""
									+ StringEscapeUtils.escapeXml(sgd.getStudyGroupClassName()) + "\" " : "";
							String gn = sgd.getStudyGroupName() != null ? "OpenClinica:StudyGroupName=\""
									+ StringEscapeUtils.escapeXml(sgd.getStudyGroupName()) + "\" " : "";
							xml.append(indent + indent + indent + "<OpenClinica:SubjectGroupData " + cid + cn + gn);
						}
						xml.append(" />");
						xml.append(nls);
					}
				}
				//
				if (sub.getAuditLogs() != null && sub.getAuditLogs().getAuditLogs().size() > 0) {
					this.addAuditLogs(sub.getAuditLogs(), indent + indent + indent);
				}
				//
				if (sub.getDiscrepancyNotes() != null && sub.getDiscrepancyNotes().getDiscrepancyNotes().size() > 0) {
					this.addDiscrepancyNotes(sub.getDiscrepancyNotes(), indent + indent + indent);
				}
			}
			xml.append(indent + indent + "</SubjectData>");
			xml.append(nls);
		}
		if (footer) {
			xml.append(indent + "</ClinicalData>");
			xml.append(nls);
		}
	}

	protected void addAuditLogs(AuditLogsBean auditLogs, String currentIndent) {
		if (auditLogs != null) {
			ArrayList<AuditLogBean> audits = auditLogs.getAuditLogs();
			if (audits != null && audits.size() > 0) {
				StringBuffer xml = this.getXmlOutput();
				String indent = this.getIndent();
				String nls = "\n";
				xml.append(currentIndent + "<OpenClinica:AuditLogs EntityID=\"" + auditLogs.getEntityID() + "\">");
				xml.append(nls);
				for (AuditLogBean audit : audits) {
					this.addOneAuditLog(audit, currentIndent + indent);
				}
				xml.append(currentIndent + "</OpenClinica:AuditLogs>");
				xml.append(nls);
			}
		}
	}

	protected void addOneAuditLog(AuditLogBean audit, String currentIndent) {
		if (audit != null) {
			StringBuffer xml = this.getXmlOutput();
			String nls = "\n";
			String i = audit.getOid();
			String u = audit.getUserId();
			String userName = audit.getUserName();
			String name = audit.getName();
			Date d = audit.getDatetimeStamp();
			String t = audit.getType();
			String r = audit.getReasonForChange();
			String o = audit.getOldValue();
			String n = audit.getNewValue();
			String vt = audit.getValueType();
			Boolean p = i.length() > 0 || u.length() > 0 || d != null || t.length() > 0 || r.length() > 0
					|| o.length() > 0 || n.length() > 0 ? true : false;
			if (p) {
				xml.append(currentIndent + "<OpenClinica:AuditLog ");
				if (i.length() > 0) {
					xml.append("ID=\"" + StringEscapeUtils.escapeXml(i) + "\" ");
				}
				if (u.length() > 0) {
					xml.append("UserID=\"" + StringEscapeUtils.escapeXml(u) + "\" ");
				}
				if (userName.length() > 0) {
					xml.append("UserName=\"" + StringEscapeUtils.escapeXml(userName) + "\" ");
				}
				if (name.length() > 0) {
					xml.append("Name=\"" + StringEscapeUtils.escapeXml(name) + "\" ");
				}
				if (d != null) {
					xml.append("DateTimeStamp=\"" + DateUtil.printDate(d, getTargetTimeZoneId(),
							DateUtil.DatePattern.ISO_TIMESTAMP) + "\" ");
				}
				if (t.length() > 0) {
					xml.append(nls);
					xml.append(currentIndent + "                      AuditType=\"" + t + "\" ");
				}
				if (r.length() > 0) {
					xml.append(nls);
					xml.append(currentIndent + "                      ReasonForChange=\""
							+ StringEscapeUtils.escapeXml(r) + "\" ");
				}
				if (o.length() > 0) {
					xml.append(nls);
					xml.append(currentIndent + "                      OldValue=\"" + StringEscapeUtils.escapeXml(o)
							+ "\" ");
				}
				if (n.length() > 0) {
					xml.append(nls);
					xml.append(currentIndent + "                      NewValue=\"" + StringEscapeUtils.escapeXml(n)
							+ "\"");
				}
				if (vt.length() > 0) {
					xml.append(nls);
					xml.append(currentIndent + "                      ValueType=\"" + StringEscapeUtils.escapeXml(vt) + "\"");
				}
				xml.append("/>");
				xml.append(nls);
			}
		}
	}

	protected void addDiscrepancyNotes(DiscrepancyNotesBean discrepancyNotesBean, String currentIndent) {
		if (discrepancyNotesBean != null) {
			ArrayList<DiscrepancyNoteBean> dns = discrepancyNotesBean.getDiscrepancyNotes();
			if (dns != null && dns.size() > 0) {
				StringBuffer xml = this.getXmlOutput();
				String indent = this.getIndent();
				String nls = "\n";
				xml.append(currentIndent + "<OpenClinica:DiscrepancyNotes EntityID=\"" + discrepancyNotesBean.getEntityID() + "\">");
				xml.append(nls);
				for (DiscrepancyNoteBean dn : dns) {
					this.addOneDN(dn, currentIndent + indent);
				}
				xml.append(currentIndent + "</OpenClinica:DiscrepancyNotes>");
				xml.append(nls);
			}
		}
	}

	protected void addOneDN(DiscrepancyNoteBean dn, String currentIndent) {
		StringBuffer xml = this.getXmlOutput();
		String indent = this.getIndent();
		String nls = "\n";
		xml.append(currentIndent + "<OpenClinica:DiscrepancyNote ");
		if (dn.getOid() != null) {
			String i = dn.getOid();
			if (i.length() > 0) {
				xml.append("ID=\"" + StringEscapeUtils.escapeXml(i) + "\" ");
			}
		}
		if (dn.getStatus() != null) {
			String s = dn.getStatus();
			if (s.length() > 0) {
				xml.append("Status=\"" + s + "\" ");
			}
		}
		if (dn.getNoteType() != null) {
			String s = dn.getNoteType();
			if (s.length() > 0) {
				xml.append("NoteType=\"" + s + "\" ");
			}
		}
		if (dn.getDateUpdated() != null) {
			xml.append("DateUpdated=\"" + DateUtil.printDate(dn.getDateUpdated(), getTargetTimeZoneId(),
					DateUtil.DatePattern.ISO_DATE) + "\" ");
		}
		if (dn.getEntityName() != null) {
			String s = dn.getEntityName();
			if (s.length() > 0) {
				xml.append("EntityName=\"" + s + "\" ");
			}
		}
		int n = dn.getNumberOfChildNotes();
		if (n > 0) {
			xml.append("NumberOfChildNotes=\"" + dn.getNumberOfChildNotes() + "\"");
		}
		xml.append(">");
		xml.append(nls);
		if (dn.getChildNotes() != null && dn.getChildNotes().size() > 0) {
			for (ChildNoteBean cn : dn.getChildNotes()) {
				xml.append(currentIndent + indent + "<OpenClinica:ChildNote ");
				if (cn.getOid() != null) {
					String s = cn.getOid();
					if (s.length() > 0) {
						xml.append("ID=\"" + s + "\" ");
					}
				}
				if (cn.getStatus() != null) {
					String s = cn.getStatus();
					if (s.length() > 0) {
						xml.append("Status=\"" + s + "\" ");
					}
				}
				if (cn.getDateCreated() != null) {
					xml.append("DateCreated=\"" + DateUtil.printDate(cn.getDateCreated(), getTargetTimeZoneId(),
							DateUtil.DatePattern.ISO_DATE) + "\" ");
				}
				if (cn.getOwnerUserName() != "") {
					String ownerUserName = cn.getOwnerUserName();
					if (ownerUserName.length() > 0) {
						xml.append("UserName=\"" + ownerUserName + "\" ");
					}

				}
				if (cn.getOwnerFirstName() != "" || cn.getOwnerLastName() != "") {
					String ownerLastName = cn.getOwnerLastName();
					String ownerFirstName = cn.getOwnerFirstName();
					if (ownerLastName.length() > 0 || ownerFirstName.length() > 0) {
						xml.append("Name=\"" + ownerFirstName + " " + ownerLastName + "\"");
					}

				}
				xml.append(">");
				xml.append(nls);
				if (cn.getDescription() != null) {
					String dc = cn.getDescription();
					if (dc.length() > 0) {
						xml.append(currentIndent + indent + indent + "<OpenClinica:Description>"
								+ StringEscapeUtils.escapeXml(dc) + "</OpenClinica:Description>");
						xml.append(nls);
					}
				}
				if (cn.getDetailedNote() != null) {
					String nt = cn.getDetailedNote();
					if (nt.length() > 0) {
						xml.append(currentIndent + indent + indent + "<OpenClinica:DetailedNote>"
								+ StringEscapeUtils.escapeXml(nt) + "</OpenClinica:DetailedNote>");
						xml.append(nls);
					}
				}
				if (cn.getUserRef() != null) {
					String uid = cn.getUserRef().getElementDefOID();
					String userName = cn.getUserRef().getUserName();
					String fullName = cn.getUserRef().getFullName();
					String temp = "";
					if (userName.length() > 0) {
						temp += " OpenClinica:UserName=\"" + StringEscapeUtils.escapeXml(userName) + "\"";
					}
					if (fullName.length() > 0) {
						temp += " OpenClinica:FullName=\"" + StringEscapeUtils.escapeXml(fullName) + "\"";
					}
					if (uid.length() > 0) {
						xml.append(currentIndent + indent + indent + "<UserRef UserOID=\"" + StringEscapeUtils.escapeXml(uid)
								+ " \"" + temp + "/>");
						xml.append(nls);
					}
				}
				xml.append(currentIndent + indent + "</OpenClinica:ChildNote>");
				xml.append(nls);
			}
		}
		xml.append(currentIndent + "</OpenClinica:DiscrepancyNote>");
		xml.append(nls);
	}

	public void setClinicalData(OdmClinicalDataBean clinicaldata) {
		this.clinicalData = clinicaldata;
	}

	public OdmClinicalDataBean getClinicalData() {
		return this.clinicalData;
	}
}
