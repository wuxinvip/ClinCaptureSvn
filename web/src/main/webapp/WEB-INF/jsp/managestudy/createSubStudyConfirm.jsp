<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<%@ taglib uri="/WEB-INF/tlds/format/date/date-time-format.tld" prefix="cc-fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="resnotes"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterms"/>

<c:set var="bioontologyURL" value="${studyToView.studyParameterConfig.defaultBioontologyURL}"/>

<jsp:include page="../include/managestudy-header.jsp"/>

<jsp:include page="../include/sideAlert.jsp"/>

<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
        <fmt:message key="confirm_the_study_information_entered_to_create_the_site"  bundle="${resword}"/> 
		</div>

		</td>
	
	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>
<script language="JavaScript">
    function leftnavExpand(strLeftNavRowElementName){
      var objLeftNavRowElement;

      objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
      if (objLeftNavRowElement != null) {
        if (objLeftNavRowElement.style) { objLeftNavRowElement = objLeftNavRowElement.style; }
          objLeftNavRowElement.display = (objLeftNavRowElement.display == "none" ) ? "" : "none";
          objExCl = MM_findObj("excl_"+strLeftNavRowElementName);
          if(objLeftNavRowElement.display == "none"){
              objExCl.src = "images/bt_Expand.gif";
          }else{
              objExCl.src = "images/bt_Collapse.gif";
          }
        }
      }
 </script>

<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='definitions' class='java.util.ArrayList'/>

<h1>
	<span class="first_level_header">
		<fmt:message key="confirm_site" bundle="${resword}"/> 
	</span>
</h1>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
<div class="textbox_center">

<table border="0" cellpadding="0" cellspacing="0" width="100%">
	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="parent_study" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${study.name}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="site_name" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.name}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column"><b><fmt:message key="unique_protocol_ID" bundle="${resword}"/></b>:</td>
		<td class="table_cell">
			<c:out value="${newStudy.identifier}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column"><b><fmt:message key="secondary_IDs" bundle="${resword}"/></b>
		</td>
		<td class="table_cell">
			<c:out value="${newStudy.secondaryIdentifier}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="principal_investigator" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.principalInvestigator}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="brief_summary" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.summary}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="protocol_verification" bundle="${resword}" />:</td>
		<td class="table_cell">
			<cc-fmt:formatDate value="${newStudy.protocolDateVerification}" dateTimeZone="${userBean.userTimeZoneId}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="start_date" bundle="${resword}" />:</td>
		<td class="table_cell">
			<cc-fmt:formatDate value="${newStudy.datePlannedStart}" dateTimeZone="${userBean.userTimeZoneId}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="estimated_completion_date" bundle="${resword}" />:</td>
		<td class="table_cell">
			<cc-fmt:formatDate value="${newStudy.datePlannedEnd}" dateTimeZone="${userBean.userTimeZoneId}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="expected_total_enrollment" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.expectedTotalEnrollment}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_name" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityName}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_city" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityCity}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_state_province" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityState}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_ZIP" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityZip}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_country" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityCountry}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_contact_name" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityContactName}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_contact_degree" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityContactDegree}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_contact_phone" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityContactPhone}" />
		</td>
	</tr>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="facility_contact_email" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="${newStudy.facilityContactEmail}" />
		</td>
	</tr>

	<c:choose>
		<c:when test="${newStudy.parentStudyId == 0}">
			<c:set var="key" value="study_system_status" /></c:when>
		<c:otherwise>
			<c:set var="key" value="site_system_status" /></c:otherwise>
	</c:choose>

	<tr valign="top">
		<td class="table_header_column">
			<fmt:message key="${key}" bundle="${resword}" />:</td>
		<td class="table_cell">
			<c:out value="Available" />
		</td>
	</tr>

	<c:forEach var="config" items="${newStudy.studyParameters}">
		<c:choose>
			<c:when test="${config.parameter.handle=='collectDOB'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="collect_subject_date_of_birth" bundle="${resword}" />:</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value == '1'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:when test="${config.value.value == '2'}">
								<fmt:message key="only_year_of_birth" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="not_used" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='discrepancyManagement'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="allow_discrepancy_management" bundle="${resword}" />:</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value == 'false'}">
								<fmt:message key="no" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="yes" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='genderRequired'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="gender_required" bundle="${resword}" />:</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value == 'false'}">
								<fmt:message key="no" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="yes" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='subjectPersonIdRequired'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="subject_person_ID_required" bundle="${resword}" />:</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value == 'required'}">
								<fmt:message key="required" bundle="${resword}" />
							</c:when>
							<c:when test="${config.value.value == 'optional'}">
								<fmt:message key="optional" bundle="${resword}" />
							</c:when>
							<c:when test="${config.value.value == 'copyFromSSID'}">
								<fmt:message key="copy_from_ssid" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="not_used" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='subjectIdGeneration'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="how_to_generate_the_subject" bundle="${resword}" />:</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value == 'manual'}">
								<fmt:message key="manual_entry" bundle="${resword}" />
							</c:when>
							<c:when test="${config.value.value == 'auto-editable'}">
								<fmt:message key="auto_generated_and_editable" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="auto_generated_and_non_editable" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='subjectIdPrefixSuffix'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="generate_subject_ID_automatically" bundle="${resword}" />:</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value == 'true'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='markImportedCRFAsCompleted'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="markImportedCRFAsCompleted" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'yes'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='autoScheduleEventDuringImport'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="autoScheduleEventDuringImport" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'yes'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='autoCreateSubjectDuringImport'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="autoCreateSubjectDuringImport" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'yes'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='allowSdvWithOpenQueries'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="allowSdvWithOpenQueries" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'yes'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

            <c:when test="${config.parameter.handle=='autoTabbing'}">
                <tr valign="top">
                    <td class="table_header_column">
                        <fmt:message key="useAutoTabbing" bundle="${resword}" />
                    </td>
                    <td class="table_cell">
                        <c:choose>
                            <c:when test="${config.value.value== 'yes'}">
                                <fmt:message key="yes" bundle="${resword}" />
                            </c:when>
                            <c:otherwise>
                                <fmt:message key="no" bundle="${resword}" />
                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>
            </c:when>

			<c:when test="${config.parameter.handle=='replaceExisitingDataDuringImport'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="replaceExisitingDataDuringImport" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'yes'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='interviewerNameRequired'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="when_entering_data_entry_interviewer" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'yes'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:when test="${config.value.value== 'no'}">
								<fmt:message key="no" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="not_used" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='interviewerNameDefault'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="interviewer_name_default_as_blank" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'blank'}">
								<fmt:message key="blank" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="pre_populated_from_active_user" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='interviewerNameEditable'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="interviewer_name_editable" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'true'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='interviewDateRequired'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="interviewer_date_required" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'yes'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:when test="${config.value.value== 'no'}">
								<fmt:message key="no" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="not_used" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>

			<c:when test="${config.parameter.handle=='interviewDateDefault'}">
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="interviewer_date_default_as_blank" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'blank'}">
								<fmt:message key="blank" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="pre_populated_from_SE" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:when>
			
			<c:otherwise>
				<tr valign="top">
					<td class="table_header_column">
						<fmt:message key="interviewer_date_editable" bundle="${resword}" />
					</td>
					<td class="table_cell">
						<c:choose>
							<c:when test="${config.value.value== 'true'}">
								<fmt:message key="yes" bundle="${resword}" />
							</c:when>
							<c:otherwise>
								<fmt:message key="no" bundle="${resword}" />
							</c:otherwise>
						</c:choose>
					</td>
				</tr>
			</c:otherwise>
		</c:choose>
	</c:forEach>
</table>
</div>
</div></div></div></div></div></div></div></div>

</div> 

<c:if test = "${not empty definitions}">
	<div class="table_title_Manage"><fmt:message key="update_site_event_definitions" bundle="${resword}"/></div>
</c:if>

<c:set var="defCount" value="0"/>
<c:forEach var="def" items="${definitions}">
	<c:set var="defCount" value="${defCount+1}"/>
	&nbsp&nbsp&nbsp&nbsp<b><a href="javascript:leftnavExpand('sed<c:out value="${defCount}"/>');">
    <img id="excl_sed<c:out value="${defCount}"/>" src="images/bt_Expand.gif" border="0"> <c:out value="${def.name}"/></b></a>
	<div id="sed<c:out value="${defCount}"/>" style="display: none">

	<!-- These DIVs define shaded box borders -->
	<div style="width: 100%">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">

	<table border="0" >
		<tr><td class="table_header_column" colspan="3" width="100px">Name</td><td class="table_cell" width="400px"><c:out value="${def.name}"/></td></tr>
		<tr><td class="table_header_column" colspan="3">Description</td><td class="table_cell"><c:out value="${def.description}"/></td></tr>
	</table>

	</div>
	</div></div></div></div></div></div></div></div>
	</div>

	<div class="table_title_manage"><fmt:message key="CRFs" bundle="${resword}"/></div>
	<div style="width: 100%">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B">
	<div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="textbox_center">
	
	<table border="0" cellpadding="0" cellspacing="0" width="100%">
		<c:set var="count" value="0"/>
		<c:forEach var="edc" items="${def.crfs}">

		<c:set var="num" value="${count}-${edc.id}" />
		<tr valign="top" bgcolor="#F5F5F5">
			<td class="table_header_column" colspan="4"><c:out value="${edc.crfName}"/></td>
		</tr>

		<c:if test="${edc.status.id==1}">
		<c:choose>
			<c:when test="${fn:length(edc.selectedVersionIds)>0}">
				<c:set var="idList" value="${edc.selectedVersionIdList}"/>
				<c:set var="selectedIds" value=",${edc.selectedVersionIds},"/>
			</c:when>
			<c:otherwise>
				<c:set var="idList" value=""/>
				<c:set var="selectedIds" value=""/>
			</c:otherwise>
		</c:choose>

		<tr valign="top">
			<td class="table_cell" colspan="2"><fmt:message key="required" bundle="${resword}"/>:
				<c:choose>
					<c:when test="${edc.requiredCRF == true}">
						<input type="checkbox" checked name="requiredCRF<c:out value="${num}"/>" value="yes" disabled>
					</c:when>
					<c:otherwise>
						<input type="checkbox" name="requiredCRF<c:out value="${num}"/>" value="yes" disabled>
					</c:otherwise>
				</c:choose>
			</td>

			<td class="table_cell"><fmt:message key="password_required" bundle="${resword}"/>:
				<c:choose>
					<c:when test="${edc.electronicSignature == true}">
						<input type="checkbox" checked name="electronicSignature<c:out value="${num}"/>" value="yes" disabled>
					</c:when>
					<c:otherwise>
						<input type="checkbox" name="electronicSignature<c:out value="${num}"/>" value="yes" disabled>
					</c:otherwise>
				</c:choose>
			</td>

			<td class="table_cell"><fmt:message key="default_version" bundle="${resword}"/>:
				<select name="defaultVersionId<c:out value="${num}"/>" id="dv<c:out value="${num}"/>" onclick="updateVersionSelection('<c:out value="${selectedIds}"/>',document.getElementById('dv<c:out value="${num}"/>').selectedIndex, '<c:out value="${num}"/>')" disabled>
					<c:forEach var="version" items="${edc.versions}">
						<c:choose>
							<c:when test="${edc.defaultVersionId == version.id}">
								<option value="<c:out value="${version.id}"/>" selected disabled><c:out value="${version.name}"/>
							</c:when>
							<c:otherwise>
								<option value="<c:out value="${version.id}"/>" disabled><c:out value="${version.name}"/>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
			</td>
		</tr>

		<tr valign="top">
			<td class="table_cell" colspan="2"><fmt:message key="version_selection" bundle="${resword}"/>:
			<select multiple name="versionSelection<c:out value="${num}"/>" id="vs<c:out value="${num}"/>" onclick="updateThis(document.getElementById('vs<c:out value="${num}"/>'), '<c:out value="${num}"/>')" size="${fn:length(edc.versions)}">
				<c:forEach var="version" items="${edc.versions}">
					<c:choose>
					<c:when test="${fn:length(idList) > 0}">
						<c:set var="versionid" value=",${version.id},"/>
						<c:choose>
						<c:when test="${version.id == defaultVersionId}">
							<option value="<c:out value="${version.id}"/>" selected disabled><c:out value="${version.name}"/>
						</c:when>
						<c:otherwise>
							<c:choose>
							<c:when test="${fn:contains(selectedIds,versionid)}">
								<option value="<c:out value="${version.id}"/>" selected disabled><c:out value="${version.name}"/>
							</c:when>
							<c:otherwise>
								<option value="<c:out value="${version.id}"/>" disabled><c:out value="${version.name}"/>
							</c:otherwise>
							</c:choose>
						</c:otherwise>
						</c:choose>
					</c:when>
					<c:otherwise>
						<option value="<c:out value="${version.id}"/>" selected disabled><c:out value="${version.name}"/>
					</c:otherwise>
					</c:choose>
				</c:forEach>
			</select>
			</td>

			<td class="table_cell" colspan="2"></td>
		</tr>

		<tr>
			<td class="table_cell" colspan="4"><fmt:message key="hidden_crf" bundle="${resword}"/> :
				<c:choose>
					<c:when test="${!edc.hideCrf}"><input type="checkbox" name="hideCRF<c:out value="${num}"/>" value="yes" disabled></c:when>
					<c:otherwise><input checked="checked" type="checkbox" name="hideCRF<c:out value="${num}"/>" value="yes" disabled></c:otherwise>
				</c:choose>
			</td>
		</tr>

        <tr valign="top">
            <td class="table_cell" colspan="4">
                <fmt:message key="data_entry_quality" bundle="${resword}"/>:
                <c:set var="deQualityDE" value=""/>
                <c:set var="deQualityEvaluatedCRF" value=""/>
                <c:choose>
                    <c:when test="${edc.doubleEntry == true}">
                        <c:set var="deQualityDE" value="checked"/>
                    </c:when>
                    <c:when test="${edc.evaluatedCRF == true}">
                        <c:set var="deQualityEvaluatedCRF" value="checked"/>
                    </c:when>
                </c:choose>

				<c:choose>
					<c:when test="${study.studyParameterConfig.studyEvaluator == 'yes' || edc.evaluatedCRF == true}">
						<input type="radio" name="deQuality${num}" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="dde" class="email_field_trigger uncheckable_radio" ${deQualityDE} disabled/>
						<fmt:message key="double_data_entry" bundle="${resword}"/>

						<input type="radio" name="deQuality${num}" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="evaluation" class="email_field_trigger uncheckable_radio" ${deQualityEvaluatedCRF} disabled/>
						<fmt:message key="crf_data_evaluation" bundle="${resword}"/>
					</c:when>
					<c:otherwise>
						<input type="checkbox" name="deQuality${num}" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="dde" class="email_field_trigger uncheckable_radio" ${deQualityDE} disabled/>
						<fmt:message key="double_data_entry" bundle="${resword}"/>
					</c:otherwise>
				</c:choose>
            </td>
        </tr>

		<tr valign="top">
			<td class="table_cell" colspan="2">
				
				<fmt:message key="send_email_when" bundle="${resword}"/>:
				<c:choose>
					<c:when test="${edc.emailStep eq 'complete'}">
						<c:set var="emailStepComplete" value="checked"/>
					</c:when>
					<c:otherwise>
						<c:set var="emailStepComplete" value=""/>
					</c:otherwise>
				</c:choose>

				<input type="radio" name="emailOnStep<c:out value="${num}"/>" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="complete" class="email_field_trigger uncheckable_radio" ${emailStepComplete} disabled/>
				<fmt:message key="complete" bundle="${resterms}"/>

				<c:choose>
					<c:when test="${edc.emailStep eq 'sign'}">
						<c:set var="emailStepSign" value="checked"/>
					</c:when>
					<c:otherwise>
						<c:set var="emailStepSign" value=""/>
					</c:otherwise>
				</c:choose>

				<input type="radio" name="emailOnStep<c:out value="${num}"/>" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="sign" class="email_field_trigger uncheckable_radio" ${emailStepSign} disabled/>
				<fmt:message key="sign" bundle="${resterms}"/>
			</td>

			<td class="table_cell" colspan="2">
				<c:choose>
					<c:when test="${empty edc.emailTo}">
						<c:set var="display" value="none"/>
					</c:when>
					<c:otherwise>
						<c:set var="display" value="block"/>
					</c:otherwise>
				</c:choose>

				<span class="email_wrapper" style="display:${display}">
					<fmt:message key="email_crf_to" bundle="${resword}"/>: 
					<input type="text" name="mailTo${num}" onchange="javascript:changeIcon();" style="width:115px;margin-left:79px" class="email_to_check_field" value="${edc.emailTo}" disabled/>
				</span>
				<span class="alert" style="display:none"><fmt:message key="enter_valid_email" bundle="${resnotes}"/></span>
			</td>
		</tr>
		<c:set var="count" value="${count+1}"/>
		</c:if>

		<tr><td class="table_divider" colspan="8">&nbsp;</td></tr>
		</c:forEach>
	</table>

	</div>
	</div></div></div></div></div></div></div></div>
	</div>
	</div><br>
</c:forEach>

<br><br>

<table border="0" cellpadding="0" cellspacing="0">    
<tr>
<td>
<form action="CreateSubStudy" method="post">
<input type="hidden" name="action" value="back">
 <input type="submit" name="back" value="<fmt:message key="back" bundle="${resword}"/>" class="button_medium medium_back">
</form>
</td>
<td>
<form action="CreateSubStudy" method="post">
<input type="hidden" name="action" value="submit">
 <input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium medium_submit">
</form>
</td>
</tr>
</table>

<br><br>

<!-- EXPANDING WORKFLOW BOX -->

<table border="0" cellpadding="0" cellspacing="0" style="position: relative; left: -14px;">
	<tr>
		<td id="sidebar_Workflow_closed" style="display: none">
		<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/<fmt:message key="image_dir" bundle="${resformat}"/>/tab_Workflow_closed.gif" border="0"></a>
	</td>
	<td id="sidebar_Workflow_open" style="display: all">
	<table border="0" cellpadding="0" cellspacing="0" class="workflowBox">
		<tr>
			<td class="workflowBox_T" valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td class="workflow_tab">
					<a href="javascript:leftnavExpand('sidebar_Workflow_closed'); leftnavExpand('sidebar_Workflow_open');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

					<b><fmt:message key="workflow" bundle="${resword}"/></b>

					</td>
				</tr>
			</table>
			</td>
			<td class="workflowBox_T" align="right" valign="top"><img src="images/workflowBox_TR.gif"></td>
		</tr>
		<tr>
			<td colspan="2" class="workflowbox_B">
			<div class="box_R"><div class="box_B"><div class="box_BR">
				<div class="workflowBox_center">


		<!-- Workflow items -->

				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_manage">



							<fmt:message key="manage_study" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_manage">


							 <fmt:message key="manage_sites" bundle="${resword}"/>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
						<td><img src="images/arrow.gif"></td>
						<td>

				<!-- These DIVs define shaded box borders -->
						<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

							<div class="textbox_center" align="center">

							<span class="title_manage">


							 <b><fmt:message key="create_new_site" bundle="${resword}"/></b>


							</span>

							</div>
						</div></div></div></div></div></div></div></div>

						</td>
					</tr>
				</table>


		<!-- end Workflow items -->

				</div>
			</div></div></div>
			</td>
		</tr>
	</table>
	</td>
   </tr>
</table>

<!-- END WORKFLOW BOX -->
<jsp:include page="../include/footer.jsp"/>
