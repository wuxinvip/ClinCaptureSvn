<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterms"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="resnotes"/>

<jsp:include page="../include/managestudy-header.jsp"/>

<jsp:include page="../include/sideAlert.jsp"/>
<c:set var="bioontologyURL" value="${studyToView.studyParameterConfig.defaultBioontologyURL}"/>

<script language="JavaScript">
	<c:import url="../../../includes/js/pages/update_study.js?r=${revisionNumber}" />
</script>

<tr id="sidebar_Instructions_open" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
		<div class="sidebar_tab_content">
			<fmt:message key="enter_the_study_information_requested" bundle="${resword}"/> 
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

<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='newStudy' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='definitions' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='sdvOptions' class='java.util.ArrayList'/>
<jsp:useBean scope="request" id="facRecruitStatusMap" class="java.util.HashMap"/>
<jsp:useBean scope="request" id="statuses" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap"/>

<c:set var="startDate" value="" />
<c:set var="endDate" value="" />
<c:set var="protocolDateVerification" value="" />

<c:forEach var="presetValue" items="${presetValues}">
	<c:if test='${presetValue.key == "startDate"}'>
		<c:set var="startDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "endDate"}'>
		<c:set var="endDate" value="${presetValue.value}" />
	</c:if>
	<c:if test='${presetValue.key == "protocolDateVerification"}'>
		<c:set var="protocolDateVerification" value="${presetValue.value}" />
	</c:if>
</c:forEach>

<script type="text/JavaScript" language="JavaScript">
  
  function updateVersionSelection(vsIds, index, count) {
    var s = "vs" + count;
    var mvs = document.getElementById(s);
    if (vsIds.length > 0) {
      for (i = 0; i < mvs.length; ++i) {
        var t = "," + mvs.options[i].value + ",";
        if (vsIds.indexOf(t) >= 0) {
          mvs.options[i].selected = true;
        } else {
          mvs.options[i].selected = false;
        }
      }
      mvs.options[index].selected = true;
    } else {
      for (i = 0; i < mvs.length; ++i) {
        mvs.options[i].selected = true;
      }
    }
  }

  //make sure current chosen default version among those selected versions.
  function updateThis(multiSelEle, count) {
    var s = "dv" + count;
    var currentDefault = document.getElementById(s);
    for (i = 0; i < multiSelEle.length; ++i) {
      if (multiSelEle.options[i].value == currentDefault.options[currentDefault.selectedIndex].value) {
        multiSelEle.options[i].selected = true;
      }
    }
  }

  function leftnavExpand(strLeftNavRowElementName) {
    var objLeftNavRowElement;

    objLeftNavRowElement = MM_findObj(strLeftNavRowElementName);
    if (objLeftNavRowElement != null) {
      if (objLeftNavRowElement.style) {
        objLeftNavRowElement = objLeftNavRowElement.style;
      }
      objLeftNavRowElement.display = (objLeftNavRowElement.display == "none") ? "" : "none";
      objExCl = MM_findObj("excl_" + strLeftNavRowElementName);
      if (objLeftNavRowElement.display == "none") {
        objExCl.src = "images/bt_Expand.gif";
      } else {
        objExCl.src = "images/bt_Collapse.gif";
      }
    }
  }
</script>

<h1>
  <span class="first_level_header">
 	 <fmt:message key="create_a_new_site" bundle="${resword}"/>
  </span>
</h1>

<form action="CreateSubStudy" method="post" id="createSubStudyForm">
<span class="alert">* </span><fmt:message key="indicates_required_field" bundle="${resword}"/><br>
<input type="hidden" name="action" value="confirm">

<div class="table_title_Manage">
	<a href="javascript:leftnavExpand('siteProperties');">
		<img id="excl_siteProperties" src="images/bt_Collapse.gif" border="0"> 
		<fmt:message key="create_site_properties" bundle="${resword}"/> 
	</a>
</div>

<div id="siteProperties" style="display: all">

<!-- These DIVs define shaded box borders -->
<div style="width: 615px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0" cellspacing="0" style="width: 100%">
   <tr valign="top"><td class="formlabel"><fmt:message key="parent_study" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
   &nbsp;<c:out value="${study.name}"/>
  </div></td></tr>
  
  <tr valign="top"><td class="formlabel"><fmt:message key="site_name" bundle="${resword}"/>:</td><td style="width: 45%;"><div class="formfieldXL_BG">
  <input type="text" name="name" value="<c:out value="${newStudy.name}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="name"/></jsp:include></td><td class="alert" style="width: 14%;"> *</td></tr>
  
  <tr valign="top"><td class="formlabel"><a href="http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId" target="def_win" onClick="openDefWindow('http://prsinfo.clinicaltrials.gov/definitions.html#PrimaryId'); return false;"><b><fmt:message key="unique_protocol_ID" bundle="${resword}"/></b>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="uniqueProId" value="<c:out value="${newStudy.identifier}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueProId"/></jsp:include></td><td class="alert"> *</td></tr>
  
  <tr valign="top"><td class="formlabel"><b><fmt:message key="secondary_IDs" bundle="${resword}"/></b><br>(<fmt:message key="separate_by_commas" bundle="${resword}"/>):</td><td>
  <div class="formtextareaXL4_BG"><textarea class="formtextareaXL4" name="secondProId" rows="4" cols="50"><c:out value="${newStudy.secondaryIdentifier}"/></textarea></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="secondProId"/></jsp:include>
  </td></tr>  
   
  <tr valign="top"><td class="formlabel"><fmt:message key="principal_investigator" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="prinInvestigator" value="<c:out value="${newStudy.principalInvestigator}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="prinInvestigator"/></jsp:include></td><td class="alert"> *</td></tr> 
  
  <tr valign="top"><td class="formlabel"><fmt:message key="brief_summary" bundle="${resword}"/>:</td><td><div class="formtextareaXL4_BG">
  <textarea class="formtextareaXL4" name="description" rows="4" cols="50" maxlength="2000"><c:out value="${newStudy.summary}"/></textarea></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="description"/></jsp:include></td><td></td></tr>

  <tr valign="top"><td class="formlabel">
  	<fmt:message key="protocol_verification" bundle="${resword}"/>:
  </td><td><div class="formfieldXL_BG">
  <input type="text" name="protocolDateVerification" value="<c:out value="${protocolDateVerification}" />" class="formfieldXL" id="protocolDateVerificationField"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="protocolDateVerification"/></jsp:include></td>
  <td>
	  <ui:calendarIcon onClickSelector="'#protocolDateVerificationField'"/>
  </td>
  </tr>

  
  <tr valign="top"><td class="formlabel"><fmt:message key="start_date" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="startDate" value="<c:out value="${startDate}" />" class="formfieldXL" id="startDateField"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="startDate"/></jsp:include></td>
  <td>
	  <ui:calendarIcon onClickSelector="'#startDateField'"/>
  </td>
  </tr>
  
  <tr valign="top"><td class="formlabel"><fmt:message key="estimated_completion_date" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="endDate" value="<c:out value="${endDate}" />" class="formfieldXL" id="endDateField"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="endDate"/></jsp:include></td>
  <td>
	  <ui:calendarIcon onClickSelector="'#endDateField'"/>
  </td></tr>
  
  <tr valign="top"><td class="formlabel"><fmt:message key="expected_total_enrollment" bundle="${resword}"/>:</td><td>
  <div class="formfieldXL_BG"><input type="text" name="expectedTotalEnrollment" value="<c:out value="${newStudy.expectedTotalEnrollment}"/>" class="formfieldXL"></div>
   <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="expectedTotalEnrollment"/></jsp:include>
  </td><td class="alert">*</td></tr>
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facName" value="<c:out value="${newStudy.facilityName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facName"/></jsp:include>
  </td></tr> 
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_city" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facCity" value="<c:out value="${newStudy.facilityCity}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facCity"/></jsp:include>
  </td></tr> 
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_state_province" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facState" value="<c:out value="${newStudy.facilityState}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facState"/></jsp:include>
  </td></tr> 
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_ZIP" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facZip" value="<c:out value="${newStudy.facilityZip}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facZip"/></jsp:include>
  </td></tr>
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_country" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facCountry" value="<c:out value="${newStudy.facilityCountry}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facCountry"/></jsp:include>
  </td></tr> 
  
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_name" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConName" value="<c:out value="${newStudy.facilityContactName}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConName"/></jsp:include>
  </td></tr>   
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_degree" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConDegree" value="<c:out value="${newStudy.facilityContactDegree}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConDegree"/></jsp:include>
  </td></tr> 
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_phone" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConPhone" value="<c:out value="${newStudy.facilityContactPhone}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConPhone"/></jsp:include>
  </td></tr>    
  
  <tr valign="top"><td class="formlabel"><fmt:message key="facility_contact_email" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
  <input type="text" name="facConEmail" value="<c:out value="${newStudy.facilityContactEmail}"/>" class="formfieldXL"></div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="facConEmail"/></jsp:include></td></tr>  
  
   <c:choose>
    <c:when test="${newStudy.parentStudyId == 0}">
       <c:set var="key" value="study_system_status"/>
    </c:when>
    <c:otherwise>
        <c:set var="key" value="site_system_status"/>
    </c:otherwise>
   </c:choose>

   <tr valign="top"><td class="formlabel"><fmt:message key="${key}" bundle="${resword}"/>:</td><td><div class="formfieldXL_BG">
   <input type="text" name="statusName" value="<c:out value="${study.status.name}"/>" class="formfieldL" disabled>
   <input type="hidden" name="statusId" value="${study.status.id}">

   </div>
  <jsp:include page="../showMessage.jsp"><jsp:param name="key" value="statusId"/></jsp:include></td><td class="alert"> *</td></tr>  
  
      
     <!--  Display parameters -->
     
   <c:if test="${paramsMap['collectDob'] != null}">
     <tr valign="top"><td class="formlabel"><fmt:message key="collect_subject_date_of_birth" bundle="${resword}"/>:</td><td>
       <c:choose>
         <c:when test="${paramsMap['collectDob'] == '1'}">
           <input type="radio" checked name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
           <input type="radio" name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
           <input type="radio" name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
         </c:when>
         <c:when test="${paramsMap['collectDob'] == '2'}">
            <input type="radio" name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
            <input type="radio" checked name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
            <input type="radio" name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
         </c:when>
         <c:otherwise>
            <input type="radio" name="collectDob" value="1"><fmt:message key="yes" bundle="${resword}"/>
            <input type="radio" name="collectDob" value="2"><fmt:message key="only_year_of_birth" bundle="${resword}"/>
            <input type="radio" checked name="collectDob" value="3"><fmt:message key="not_used" bundle="${resword}"/>
         </c:otherwise>
      </c:choose>
      </td></tr>
   </c:if>

   <c:if test="${paramsMap['discrepancyManagement'] != null}">
		  <tr valign="top"><td class="formlabel"><fmt:message key="allow_discrepancy_management" bundle="${resword}"/>:</td><td>
		   <c:choose>
		   <c:when test="${paramsMap['discrepancyManagement'] == 'false'}">
		    <input type="radio" name="discrepancyManagement" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" checked name="discrepancyManagement" value="false"><fmt:message key="no" bundle="${resword}"/>
		   </c:when>
		   <c:otherwise>
		    <input type="radio" checked name="discrepancyManagement" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" name="discrepancyManagement" value="false"><fmt:message key="no" bundle="${resword}"/>
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:if>

	<c:if test="${paramsMap['genderRequired'] != null}">
		  <tr valign="top"><td class="formlabel"><fmt:message key="gender_required" bundle="${resword}"/>:</td><td>
		   <c:choose>
		   <c:when test="${paramsMap['genderRequired'] == 'false'}">
		    <input type="radio" name="genderRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" checked name="genderRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
		   </c:when>
		   <c:otherwise>
		    <input type="radio" checked name="genderRequired" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" name="genderRequired" value="false"><fmt:message key="no" bundle="${resword}"/>
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:if>
    <c:if test="${paramsMap['subjectPersonIdRequired'] != null}">
		  <tr valign="top"><td class="formlabel"><fmt:message key="subject_person_ID_required" bundle="${resword}"/>:</td>
			<td colspan="2">
				<c:set var="subjectPersonIdRequired" value=""/>
				<c:set var="subjectPersonIdOptional" value=""/>
				<c:set var="subjectPersonIdCopy" value=""/>
				<c:set var="subjectPersonIdNotUsed" value=""/>

				<c:choose>
					<c:when test="${paramsMap['subjectPersonIdRequired'] == 'required'}">
						<c:set var="subjectPersonIdRequired" value="checked"/>
					</c:when>
					<c:when test="${paramsMap['subjectPersonIdRequired'] == 'optional'}">
						<c:set var="subjectPersonIdOptional" value="checked"/>
					</c:when>
					<c:when test="${paramsMap['subjectPersonIdRequired'] == 'copyFromSSID'}">
						<c:set var="subjectPersonIdCopy" value="checked"/>
					</c:when>
					<c:otherwise>
						<c:set var="subjectPersonIdNotUsed" value="checked"/>
					</c:otherwise>
				</c:choose>

				<input type="radio" onchange="javascript:changeIcon()" ${subjectPersonIdRequired} name="subjectPersonIdRequired" value="required"><fmt:message key="required" bundle="${resword}"/>
				<input type="radio" onchange="javascript:changeIcon()" ${subjectPersonIdOptional} name="subjectPersonIdRequired" value="optional"><fmt:message key="optional" bundle="${resword}"/>
				<input type="radio" onchange="javascript:changeIcon()" ${subjectPersonIdCopy} name="subjectPersonIdRequired" value="copyFromSSID"><fmt:message key="copy_from_ssid" bundle="${resword}"/>
				<input type="radio" onchange="javascript:changeIcon()" ${subjectPersonIdNotUsed} name="subjectPersonIdRequired" value="not used"><fmt:message key="not_used" bundle="${resword}"/>
			</td>
		  </tr>
	</c:if>
	<c:if test="${paramsMap['subjectIdGeneration'] != null}">
		   <tr valign="top"><td class="formlabel"><fmt:message key="how_to_generate_the_subject" bundle="${resword}"/>:</td><td>
		   <c:choose>
		   <c:when test="${paramsMap['subjectIdGeneration'] == 'manual'}">
		    <input type="radio" checked name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
		    <input type="radio" name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
		    <input type="radio" name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
		   </c:when>
		    <c:when test="${paramsMap['subjectIdGeneration'] == 'auto editable'}">
		    <input type="radio" name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
		    <input type="radio" checked name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
		    <input type="radio" name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
		   </c:when>
		   <c:otherwise>
		    <input type="radio" name="subjectIdGeneration" value="manual"><fmt:message key="manual_entry" bundle="${resword}"/>
		    <input type="radio" name="subjectIdGeneration" value="auto editable"><fmt:message key="auto_generated_and_editable" bundle="${resword}"/>
		    <input type="radio" checked name="subjectIdGeneration" value="auto non-editable"><fmt:message key="auto_generated_and_non_editable" bundle="${resword}"/>
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:if>
	<c:if test="${paramsMap['subjectIdPrefixSuffix'] != null}">
		   <tr valign="top"><td class="formlabel"><fmt:message key="generate_study_subject_ID_automatically" bundle="${resword}"/>:</td><td>
		   <c:choose>
		   <c:when test="${paramsMap['subjectIdPrefixSuffix'] == 'true'}">
		    <input type="radio" checked name="subjectIdPrefixSuffix" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" name="subjectIdPrefixSuffix" value="false"><fmt:message key="no" bundle="${resword}"/>

		   </c:when>
		   <c:otherwise>
		    <input type="radio" name="subjectIdPrefixSuffix" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" checked name="subjectIdPrefixSuffix" value="false"><fmt:message key="no" bundle="${resword}"/>

		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:if>
    <c:if test="${paramsMap['markImportedCRFAsCompleted'] != null}">
        <tr valign="top">
            <td class="formlabel"><fmt:message key="markImportedCRFAsCompleted" bundle="${resword}"/></td>
            <td>
                <input type="radio" <c:if test="${newStudy.studyParameterConfig.markImportedCRFAsCompleted == 'yes'}">checked</c:if> name="markImportedCRFAsCompleted" value="yes"><fmt:message key="yes" bundle="${resword}"/>
                <input type="radio" <c:if test="${newStudy.studyParameterConfig.markImportedCRFAsCompleted == 'no'}">checked</c:if> name="markImportedCRFAsCompleted" value="no"><fmt:message key="no" bundle="${resword}"/>
            </td>
        </tr>
    </c:if>
    <c:if test="${paramsMap['autoScheduleEventDuringImport'] != null}">
      <tr valign="top">
        <td class="formlabel"><fmt:message key="autoScheduleEventDuringImport" bundle="${resword}"/></td>
        <td>
          <input type="radio" <c:if test="${newStudy.studyParameterConfig.autoScheduleEventDuringImport == 'yes'}">checked</c:if> name="autoScheduleEventDuringImport" value="yes"><fmt:message key="yes" bundle="${resword}"/>
          <input type="radio" <c:if test="${newStudy.studyParameterConfig.autoScheduleEventDuringImport == 'no'}">checked</c:if> name="autoScheduleEventDuringImport" value="no"><fmt:message key="no" bundle="${resword}"/>
        </td>
      </tr>
    </c:if>
    <c:if test="${paramsMap['autoCreateSubjectDuringImport'] != null}">
      <tr valign="top">
        <td class="formlabel"><fmt:message key="autoCreateSubjectDuringImport" bundle="${resword}"/></td>
        <td>
          <input type="radio" <c:if test="${newStudy.studyParameterConfig.autoCreateSubjectDuringImport == 'yes'}">checked</c:if> name="autoCreateSubjectDuringImport" value="yes"><fmt:message key="yes" bundle="${resword}"/>
          <input type="radio" <c:if test="${newStudy.studyParameterConfig.autoCreateSubjectDuringImport == 'no'}">checked</c:if> name="autoCreateSubjectDuringImport" value="no"><fmt:message key="no" bundle="${resword}"/>
        </td>
      </tr>
    </c:if>
   <c:if test="${paramsMap['allowSdvWithOpenQueries'] != null}">
        <tr valign="top">
            <td class="formlabel"><fmt:message key="allowSdvWithOpenQueries" bundle="${resword}"/></td>
            <td>
                <input type="radio" <c:if test="${newStudy.studyParameterConfig.allowSdvWithOpenQueries == 'yes'}">checked</c:if> name="allowSdvWithOpenQueries" value="yes"><fmt:message key="yes" bundle="${resword}"/>
                <input type="radio" <c:if test="${newStudy.studyParameterConfig.allowSdvWithOpenQueries == 'no'}">checked</c:if> name="allowSdvWithOpenQueries" value="no"><fmt:message key="no" bundle="${resword}"/>
            </td>
        </tr>
    </c:if>
    <c:if test="${paramsMap['useAutoTabbing'] != null}">
        <tr valign="top">
            <td class="formlabel"><fmt:message key="useAutoTabbing" bundle="${resword}"/></td>
            <td>
                <input type="radio" <c:if test="${newStudy.studyParameterConfig.autoTabbing == 'yes'}">checked</c:if> name="autoTabbing" value="yes"><fmt:message key="yes" bundle="${resword}"/>
                <input type="radio" <c:if test="${newStudy.studyParameterConfig.autoTabbing == 'no'}">checked</c:if> name="autoTabbing" value="no"><fmt:message key="no" bundle="${resword}"/>
            </td>
        </tr>
    </c:if>
   <c:if test="${not empty paramsMap['replaceExisitingDataDuringImport']}">
       <tr valign="top">
           <td class="formlabel"><fmt:message key="replaceExisitingDataDuringImport" bundle="${resword}"/></td>
           <td>
               <input type="radio" <c:if test="${newStudy.studyParameterConfig.replaceExisitingDataDuringImport == 'yes'}">checked</c:if> name="replaceExisitingDataDuringImport" value="yes"><fmt:message key="yes" bundle="${resword}"/>
               <input type="radio" <c:if test="${newStudy.studyParameterConfig.replaceExisitingDataDuringImport == 'no'}">checked</c:if> name="replaceExisitingDataDuringImport" value="no"><fmt:message key="no" bundle="${resword}"/>
           </td>
       </tr>
   </c:if>
	<c:if test="${paramsMap['interviewDateRequired'] != null}">
		  <tr valign="top">
		  	<td class="formlabel">
		  		<fmt:message key="interviewer_date_required" bundle="${resword}"/>
		  	</td>
		   <td>
				<input type="radio" ${newStudy.studyParameterConfig.interviewerNameRequired== 'yes' ? 'checked' : ''} onchange="javascript:changeIcon();" onclick="hideUnhideStudyParamRow(this);" data-cc-action="show" data-row-class="interviewDate" name="interviewDateRequired" value="yes">
				<fmt:message key="required" bundle="${resword}"/>
				<input type="radio" ${newStudy.studyParameterConfig.interviewerNameRequired== 'no' ? 'checked' : ''} onchange="javascript:changeIcon();" onclick="hideUnhideStudyParamRow(this);" data-cc-action="show" data-row-class="interviewDate" name="interviewDateRequired" value="no">
				<fmt:message key="optional" bundle="${resword}"/>
				<input type="radio" ${newStudy.studyParameterConfig.interviewerNameRequired== 'not_used' ? 'checked' : ''} onchange="javascript:changeIcon();" onclick="hideUnhideStudyParamRow(this);" data-cc-action="hide" data-row-class="interviewDate" name="interviewDateRequired" value="not_used">
				<fmt:message key="not_used" bundle="${resword}"/>
			</td>
		  </td>
		  </tr>
    </c:if>
	<c:if test="${paramsMap['interviewDateDefault'] != null}">
		  <tr valign="top" class="interviewDate">
		  	<td class="formlabel">
		  		<fmt:message key="interviewer_date_default_as_blank" bundle="${resword}"/>
		  	</td>
		  	<td>
		     <c:choose>
				<c:when test="${newStudy.studyParameterConfig.interviewDateDefault== 'blank'}">
					<input type="radio" onchange="javascript:changeIcon()" onclick="hideUnhideStudyParamRow(this); changeParameterForStudy('interviewDateEditable', 'true');" checked name="interviewDateDefault" value="blank" data-cc-action="hide" data-row-class="interviewDateEditable"><fmt:message key="blank" bundle="${resword}"/>
					<input type="radio" onchange="javascript:changeIcon()" onclick="hideUnhideStudyParamRow(this);" name="interviewDateDefault" value="pre-populated" data-cc-action="show" data-row-class="interviewDateEditable"><fmt:message key="pre_populated_from_SE" bundle="${resword}"/>
				</c:when>
				<c:otherwise>
					<input type="radio" onchange="javascript:changeIcon()" onclick="hideUnhideStudyParamRow(this); changeParameterForStudy('interviewDateEditable', 'true');" name="interviewDateDefault" value="blank" data-cc-action="hide" data-row-class="interviewDateEditable"><fmt:message key="blank" bundle="${resword}"/>
					<input type="radio" onchange="javascript:changeIcon()" onclick="hideUnhideStudyParamRow(this);" checked name="interviewDateDefault" value="re-populated" data-cc-action="show" data-row-class="interviewDateEditable"><fmt:message key="pre_populated_from_SE" bundle="${resword}"/>
				</c:otherwise>
			</c:choose>
		  </td>
		  </tr>
	 </c:if>
	 <c:if test="${paramsMap['interviewDateEditable'] != null}">
		  <tr valign="top" class="interviewDate interviewDateEditable"><td class="formlabel"><fmt:message key="interviewer_date_editable" bundle="${resword}"/></td><td>
		   <c:choose>
		   <c:when test="${newStudy.studyParameterConfig.interviewDateEditable == 'true'}">
		    <input type="radio" checked name="interviewDateEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" name="interviewDateEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
	
		   </c:when>
		   <c:otherwise>
		    <input type="radio" name="interviewDateEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" checked name="interviewDateEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	 </c:if>
	 <c:if test="${paramsMap['interviewerNameRequired'] != null}">
		   <tr valign="top"><td class="formlabel"><fmt:message key="when_entering_data_entry_interviewer" bundle="${resword}"/></td>
		   <td>
			  	<input type="radio" ${newStudy.studyParameterConfig.interviewerNameRequired== 'yes' ? 'checked' : ''} onchange="javascript:changeIcon();" onclick="hideUnhideStudyParamRow(this);" data-cc-action="show" data-row-class="interviewer" name="interviewerNameRequired" value="yes">
				<fmt:message key="required" bundle="${resword}"/>
				<input type="radio" ${newStudy.studyParameterConfig.interviewerNameRequired== 'no' ? 'checked' : ''} onchange="javascript:changeIcon();" onclick="hideUnhideStudyParamRow(this);" data-cc-action="show" data-row-class="interviewer" name="interviewerNameRequired" value="no">
				<fmt:message key="optional" bundle="${resword}"/>
				<input type="radio" ${newStudy.studyParameterConfig.interviewerNameRequired== 'not_used' ? 'checked' : ''} onchange="javascript:changeIcon();" onclick="hideUnhideStudyParamRow(this);" data-cc-action="hide" data-row-class="interviewer" name="interviewerNameRequired" value="not_used">
				<fmt:message key="not_used" bundle="${resword}"/>
		  </td>
		  </tr>
	</c:if>
	<c:if test="${paramsMap['interviewerNameDefault'] != null}">
		  <tr valign="top" class="interviewer"><td class="formlabel"><fmt:message key="interviewer_name_default_as_blank" bundle="${resword}"/></td><td>
			   <c:choose>
				   <c:when test="${newStudy.studyParameterConfig.interviewerNameDefault == 'blank'}">
					    <input type="radio" checked name="interviewerNameDefault" value="blank" onclick="hideUnhideStudyParamRow(this); changeParameterForStudy('interviewerNameEditable', 'true');" data-cc-action="hide" data-row-class="interviewerEditable"><fmt:message key="blank" bundle="${resword}"/>
					    <input type="radio" name="interviewerNameDefault" value="pre-populated" onclick="hideUnhideStudyParamRow(this);" data-cc-action="show" data-row-class="interviewerEditable"><fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>
				   </c:when>
				   <c:otherwise>
					    <input type="radio" name="interviewerNameDefault" value="blank" onclick="hideUnhideStudyParamRow(this); changeParameterForStudy('interviewerNameEditable', 'true');" data-cc-action="hide" data-row-class="interviewerEditable"><fmt:message key="blank" bundle="${resword}"/>
					    <input type="radio" checked name="interviewerNameDefault" value="re-populated" onclick="hideUnhideStudyParamRow(this);" data-cc-action="show" data-row-class="interviewerEditable"><fmt:message key="pre_populated_from_active_user" bundle="${resword}"/>
				   </c:otherwise>
			  </c:choose>
		  </td>
		  </tr>
	</c:if>
	<c:if test="${paramsMap['interviewerNameEditable'] != null}">
		  <tr valign="top" class="interviewer interviewerEditable"><td class="formlabel"><fmt:message key="interviewer_name_editable" bundle="${resword}"/></td><td>
		   <c:choose>
		   <c:when test="${newStudy.studyParameterConfig.interviewerNameEditable == 'true'}">
		    <input type="radio" checked name="interviewerNameEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" name="interviewerNameEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
		   </c:when>
		   <c:otherwise>
		    <input type="radio" name="interviewerNameEditable" value="true"><fmt:message key="yes" bundle="${resword}"/>
		    <input type="radio" checked name="interviewerNameEditable" value="false"><fmt:message key="no" bundle="${resword}"/>
		   </c:otherwise>
		  </c:choose>
		  </td>
		  </tr>
	</c:if>

   <c:if test="${paramsMap['allowCodingVerification'] != null}">
       <tr valign="top">
           <td class="formlabel"><fmt:message key="allowCodingVerification" bundle="${resword}"/></td>
           <td>
               <input type="radio" <c:if test="${newStudy.studyParameterConfig.allowCodingVerification == 'yes'}">checked</c:if> name="allowCodingVerification" value="yes"><fmt:message key="yes" bundle="${resword}"/>
               <input type="radio" <c:if test="${newStudy.studyParameterConfig.allowCodingVerification == 'no'}">checked</c:if> name="allowCodingVerification" value="no"><fmt:message key="no" bundle="${resword}"/>
           </td>
       </tr>
       <tr>
          <td>&nbsp;</td>
      </tr>
   </c:if>
   <c:if test="${paramsMap['defaultBioontologyURL'] != null}">
       <tr valign="top">
           <td class="formlabel">
               <fmt:message key="defaultBioontologyURL" bundle="${resword}"/>:
           </td>
           <td>
               <input id="bioontologyURL" name="defaultBioontologyURL" value="${bioontologyURL}"/>
           </td>
       </tr>
   </c:if>

  <c:if test="${paramsMap['autoCodeDictionaryName'] != null}">
    <tr valign="top">
      <td class="formlabel">
        <fmt:message key="autoCodeDictionaryName" bundle="${resword}"/>
      </td>
      <td>
        <input type="text" name="autoCodeDictionaryName" value="${newStudy.studyParameterConfig.autoCodeDictionaryName}" maxlength="255" size="35">
        <c:set var="autoCodeDictionaryName" value="${newStudy.studyParameterConfig.autoCodeDictionaryName}"/>
        <jsp:include page="../showMessage.jsp">
          <jsp:param name="key" value="autoCodeDictionaryName"/>
        </jsp:include>
      </td>
    </tr>
   </c:if>
   
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
	<c:choose>
	<c:when test="${def.status.id==5 || def.status.id==7}">
	&nbsp&nbsp&nbsp&nbsp<b><img name="ExpandGroup3" src="images/bt_Collapse.gif" border="0"> <c:out value="${def.name}"/> &nbsp&nbsp&nbsp&nbsp (<fmt:message key="this_removed" bundle="${resword}"/>)</b>
	</c:when>
	<c:otherwise>
	&nbsp&nbsp&nbsp&nbsp<b><a href="javascript:leftnavExpand('sed<c:out value="${defCount}"/>');">
    <img id="excl_sed<c:out value="${defCount}"/>" src="images/bt_Expand.gif" border="0"> <c:out value="${def.name}"/></b></a>
	</c:otherwise>
	</c:choose>
	<div id="sed<c:out value="${defCount}"/>" style="display: none">

	<!-- These DIVs define shaded box borders -->
 	<div style="width: 500px">
	<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">
	<div class="tablebox_center">

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
				<td class="table_cell"><fmt:message key="required" bundle="${resword}"/>:
					<c:choose>
						<c:when test="${edc.requiredCRF == true}">
							<input type="checkbox" checked name="requiredCRF<c:out value="${num}"/>" value="yes">
						</c:when>
						<c:otherwise>
							<input type="checkbox" name="requiredCRF<c:out value="${num}"/>" value="yes">
						</c:otherwise>
					</c:choose>
				</td>
	
				<td class="table_cell">&nbsp;</td>
	
				<td class="table_cell"><fmt:message key="password_required" bundle="${resword}"/>: 
					<c:choose>
						<c:when test="${edc.electronicSignature == true}">
							<input type="checkbox" checked name="electronicSignature<c:out value="${num}"/>" value="yes">
						</c:when>
						<c:otherwise>
							<input type="checkbox" name="electronicSignature<c:out value="${num}"/>" value="yes">
						</c:otherwise>
					</c:choose>
				</td>
	
				<td class="table_cell"><fmt:message key="default_version" bundle="${resword}"/>:
				<select name="defaultVersionId<c:out value="${num}"/>" id="dv<c:out value="${num}"/>" onclick="updateVersionSelection('<c:out value="${selectedIds}"/>',document.getElementById('dv<c:out value="${num}"/>').selectedIndex, '<c:out value="${num}"/>')">
					<c:forEach var="version" items="${edc.versions}">
						<c:choose>
							<c:when test="${edc.defaultVersionId == version.id}">
								<option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
							</c:when>
							<c:otherwise>
								<option value="<c:out value="${version.id}"/>"><c:out value="${version.name}"/>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
				</td>
			</tr>

			<tr valign="top">
				<td class="table_cell" colspan="2"><fmt:message key="version_selection" bundle="${resword}"/>&nbsp:
				<select multiple name="versionSelection<c:out value="${num}"/>" id="vs<c:out value="${num}"/>" onclick="updateThis(document.getElementById('vs<c:out value="${num}"/>'), '<c:out value="${num}"/>')" size="${fn:length(edc.versions)}">
					<c:forEach var="version" items="${edc.versions}">
						<c:choose>
						<c:when test="${fn:length(idList) > 0}">
							<c:set var="versionid" value=",${version.id},"/>
							<c:choose>
								<c:when test="${version.id == defaultVersionId}">
									<option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
								</c:when>
								<c:otherwise>
									<c:choose>
										<c:when test="${fn:contains(selectedIds,versionid)}">
											<option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
										</c:when>
										<c:otherwise>
											<option value="<c:out value="${version.id}"/>"><c:out value="${version.name}"/>
										</c:otherwise>
									</c:choose>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<option value="<c:out value="${version.id}"/>" selected><c:out value="${version.name}"/>
						</c:otherwise>
						</c:choose>
					</c:forEach>
				</select>
				</td>

				<td class="table_cell" colspan="2"></td>
			</tr>

			<tr>
				<td class="table_cell" colspan="2"><fmt:message key="hidden_crf" bundle="${resword}"/> :
					<c:choose>
						<c:when test="${!edc.hideCrf}"><input type="checkbox" name="hideCRF<c:out value="${num}"/>" value="yes"></c:when>
						<c:otherwise><input checked="checked" type="checkbox" name="hideCRF<c:out value="${num}"/>" value="yes"></c:otherwise>
					</c:choose>
				</td>

				<td class="table_cell" colspan="2"><fmt:message key="sdv_option" bundle="${resword}"/>:
					<select name="sdvOption<c:out value="${num}"/>">
                        <c:forEach var="sdv" items="${edc.sdvOptions}">
                            <option value="${sdv.code}" ${edc.sourceDataVerification.code == sdv.code ? "selected" : ""}><fmt:message key="${sdv.description}" bundle="${resterms}"/></option>
                        </c:forEach>
					</select>
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
							<input type="radio" name="deQuality${num}" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="dde" class="email_field_trigger uncheckable_radio" ${deQualityDE}/>
							<fmt:message key="double_data_entry" bundle="${resword}"/>

							<input type="radio" name="deQuality${num}" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="evaluation" class="email_field_trigger uncheckable_radio" ${deQualityEvaluatedCRF}/>
							<fmt:message key="crf_data_evaluation" bundle="${resword}"/>
						</c:when>
						<c:otherwise>
							<input type="checkbox" name="deQuality${num}" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="dde" class="email_field_trigger uncheckable_radio" ${deQualityDE}/>
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

					<input type="radio" name="emailOnStep<c:out value="${num}"/>" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="complete" class="email_field_trigger uncheckable_radio" ${emailStepComplete}/>
					<fmt:message key="complete" bundle="${resterms}"/>
		
					<c:choose>
						<c:when test="${edc.emailStep eq 'sign'}">
							<c:set var="emailStepSign" value="checked"/>
						</c:when>
						<c:otherwise>
							<c:set var="emailStepSign" value=""/>
						</c:otherwise>
					</c:choose>
					
					<input type="radio" name="emailOnStep<c:out value="${num}"/>" onclick="javascript:showEmailField(this);" onchange="javascript:changeIcon();" value="sign" class="email_field_trigger uncheckable_radio" ${emailStepSign}/>
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
						<input type="text" name="mailTo${num}" onchange="javascript:changeIcon();" style="width:160px;margin-left:15px" class="email_to_check_field" value="${edc.emailTo}"/>
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
	</div>
	<br>
</c:forEach>

<br><br>


<table border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td>
			<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
							value="<fmt:message key="back" bundle="${resword}"/>"
							class="button_medium medium_back"
							onClick="javascript: confirmBackSmart('<fmt:message key="sure_to_cancel" bundle="${resword}"/>', '${navigationURL}', '${defaultURL}')" />
		</td>

		<td>
			<input type="button" name="Submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium medium_continue" onClick="javascript:validateCustomFields(['email'],['.email_to_check_field'],'#createSubStudyForm');">
		</td>
	</tr>
</table>

</form>
<DIV ID="testdiv1" STYLE="position:absolute;visibility:hidden;background-color:white;layer-background-color:white;"></DIV>
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
