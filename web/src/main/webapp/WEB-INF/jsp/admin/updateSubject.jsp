<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:include page="../include/admin-header.jsp"/>

<!-- *JSP* ${pageContext.page['class'].simpleName} -->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">

		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope="session" id="subjectToUpdate" class="org.akaza.openclinica.bean.submit.SubjectBean" />
<jsp:useBean scope="session" id="localBirthDate" class="java.lang.String" />

<body class="aka_bodywidth" onload="<c:if test='${popUpURL != ""}'>openDNoteWindow('<c:out value="${popUpURL}" />');</c:if>">

<c:set var="genderShow" value="${true}"/>
<fmt:message key="gender" bundle="${resword}" var="genderLabel"/>
<c:if test="${study ne null}">
	<c:set var="genderShow" value="${!(study.studyParameterConfig.genderRequired == 'false')}"/>
	<c:set var="genderLabel" value="${study.studyParameterConfig.genderLabel}"/>
</c:if>

<h1>
	<span class="first_level_header">
		<fmt:message key="update_subject_details" bundle="${resword}"/>
	</span>
</h1>

<P><span class="alert">* </span><fmt:message key="indicates_required_field" bundle="${resword}"/></P>
<form action="UpdateSubject" method="post">
<input type="hidden" name="formWithStateFlag" id="formWithStateFlag" value="${formWithStateFlag != null ? formWithStateFlag : ''}" />
<input type="hidden" name="action" value="confirm">
<input type="hidden" name="id" value="<c:out value="${subjectToUpdate.id}"/>">
<input type="hidden" name="studySubId" value="<c:out value="${studySubId}"/>">
<input type="hidden" name="isDataChanged" value="<c:out value="${isDataChanged}"/>">
<!-- These DIVs define shaded box borders -->

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellspacing="10">

	<tr valign="top">
		<td class="formlabel"><fmt:message key="person_ID" bundle="${resword}"/>:</td>
		<td><c:choose>
				<c:when test="${parameters.subjectPersonIdRequired == 'required'}">
					<table><tr>
						<td>
							<div class="formfieldXL_BG">
								<input type="text" name="uniqueIdentifier" value="<c:out value="${fields['personId']}"/>" class="formfieldXL">
							</div>
						</td>
						<td> <span class="alert">*</span>
							<c:if test="${parameters.discrepancyManagement}">
							
							<c:choose>
								<c:when test="${hasUniqueIDNote eq 'yes'}">
									<a href="#" onClick="openDNWindow('ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier', '', event); return false;">
										<img id="flag_uniqueIdentifier" name="flag_uniqueIdentifier" src="${uniqueIDNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
									</a>
								</c:when>
								<c:otherwise>
									<a href="#" onClick="openDNWindow('CreateDiscrepancyNote?stSubjectId=0&name=subject&id=<c:out value="${subjectToUpdate.id}"/>&writeToDB=1&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier', '', event); return false;">
										<img name="flag_uniqueIdentifier" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
										<input type="hidden" value="ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=uniqueIdentifier&column=unique_identifier">
									</a>
								</c:otherwise>
							</c:choose>
							</c:if>
						</td>
					</tr></table>
				</c:when>
				
				<c:when test="${parameters.subjectPersonIdRequired == 'optional'}">
					<table><tr>
							<td>
								<div class="formfieldXL_BG">
									<input type="text" name="uniqueIdentifier" value="<c:out value="${fields['personId']}"/>" class="formfieldXL">
								</div>
							</td>
							<td>
								<c:if test="${parameters.discrepancyManagement}">
								<c:choose>
									<c:when test="${hasUniqueIDNote eq 'yes'}">
										<a href="#" onClick="openDNWindow('ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier', '', event); return false;">
											<img id="flag_uniqueIdentifier" name="flag_uniqueIdentifier" src="${uniqueIDNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
										</a>
									</c:when>
									<c:otherwise>
										<a href="#" onClick="openDNWindow('CreateDiscrepancyNote?stSubjectId=0&name=subject&id=<c:out value="${subjectToUpdate.id}"/>&writeToDB=1&field=uniqueIdentifier&column=unique_identifier','spanAlert-uniqueIdentifier', '', event); return false;">
											<img name="flag_uniqueIdentifier" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
											<input type="hidden" value="ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=uniqueIdentifier&column=unique_identifier">
										</a>
									</c:otherwise>
								</c:choose>
								</c:if>
							</td>
						</tr></table>
				</c:when>

				<c:when test="${parameters.subjectPersonIdRequired == 'copyFromSSID'}">
					<table><tr>
							<td>
								<div class="formfieldXL_BG">
									<input type="text" value="<c:out value="${fields['personId']}"/>" class="formfieldXL" disabled/>
									<input type="hidden" name="uniqueIdentifier" value="<c:out value="${fields['personId']}"/>"/>
								</div>
							</td>
						</tr>
					</table>
				</c:when>

				<c:otherwise>
					<div class="formfieldXL_BG">
						<input type="text" name="uniqueIdentifier" disabled="true" value="<fmt:message key="not_used" bundle="${resword}"/>" class="formfieldXL">
					</div>
				</c:otherwise>
			</c:choose>
			<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="uniqueIdentifier"/></jsp:include>
		</td>
	</tr>

	<c:if test="${parameters.genderRequired}">
	<tr valign="top">
		<td class="formlabel">${parameters.genderLabel}:</td>
		<td>
			<table style="padding-bottom: 4px">
				<tr>
					<td>
						<c:choose>
							<c:when test="${fields['gender'] == 'm'}">
								<input type="radio" name="gender" checked value="m"><fmt:message key="male" bundle="${resword}"/>
								<input type="radio" name="gender" value="f"><fmt:message key="female" bundle="${resword}"/>
								<input type="radio" name="gender" value=""><fmt:message key="not_specified" bundle="${resword}"/>
							</c:when>
							<c:when test="${fields['gender'] == 'f'}">
								<input type="radio" name="gender" value="m"><fmt:message key="male" bundle="${resword}"/>
								<input type="radio" checked name="gender" value="f"><fmt:message key="female" bundle="${resword}"/>
								<input type="radio" name="gender" value=""><fmt:message key="not_specified" bundle="${resword}"/>
							</c:when>
							<c:otherwise>
								<input type="radio" name="gender" value="m"><fmt:message key="male" bundle="${resword}"/>
								<input type="radio" name="gender" value="f"><fmt:message key="female" bundle="${resword}"/>
								<input type="radio" checked name="gender" value=""><fmt:message key="not_specified" bundle="${resword}"/>
							</c:otherwise>
						</c:choose>
					</td>

					<td>
						<c:if test="${parameters.genderRequired}"><span class="alert">*</span></c:if>
						<c:if test="${parameters.discrepancyManagement}">

							<c:choose>
								<c:when test="${hasGenderNote eq 'yes'}">
									<a href="#" onClick="openDNWindow('ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=gender&column=gender','spanAlert-gender', '', event); return false;">
										<img id="flag_gender" name="flag_gender" src="${genderNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
									</a>
								</c:when>
								<c:otherwise>
									<a href="#" onClick="openDNWindow('CreateDiscrepancyNote?stSubjectId=0&name=subject&id=<c:out value="${subjectToUpdate.id}"/>&writeToDB=1&field=gender&column=gender','spanAlert-gender', '', event); return false;">
										<img name="flag_gender" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
										<input type="hidden" value="ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=gender&column=gender">
									</a>
								</c:otherwise>
							</c:choose>

						</c:if>
					</td>
				</tr>
			</table>
			<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="gender"/></jsp:include>
		</td>
	</tr>
	</c:if>

	<c:choose>
	<c:when test="${parameters.collectDob == '1'}">
		<tr valign="top">
			<td class="formlabel"><fmt:message key="date_of_birth" bundle="${resword}"/>:</td>
			<td>
				<table border="0" cellpadding="0" cellspacing="0">
					<tr valign="top">
						<td>
							<div class="formfieldM_BG">
								<input type="text" name="dateOfBirth" id="dateOfBirth" size="15" value="<c:out value="${fields['dateOfBirth']}"/>" class="formfieldM">
							</div>
						</td>
						<td>
							<ui:calendarIcon onClickSelector="'#dateOfBirth'" />
						</td>
						<td class="formlabel">
							(<fmt:message key="date_format" bundle="${resformat}"/>)<span class="alert">*</span>
							<c:if test="${parameters.discrepancyManagement}">
                <c:choose>
                  <c:when test="${hasDOBNote eq 'yes'}">
                    <a href="#" onClick="openDNWindow('ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=dob&column=date_of_birth','spanAlert-dob', '', event); return false;">
                      <img id="flag_dob" name="flag_dob" src="${dOBNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onClick="openDNWindow('CreateDiscrepancyNote?stSubjectId=0&name=subject&id=<c:out value="${subjectToUpdate.id}"/>&writeToDB=1&field=dob&column=date_of_birth','spanAlert-dob', '', event); return false;">
                      <img name="flag_dob" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
                      <input type="hidden" value="ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=dob&column=date_of_birth">
                    </a>
                  </c:otherwise>
                </c:choose>
							</c:if>
						</td>
					</tr>
				</table>
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="dateOfBirth"/></jsp:include>
			</td>
		</tr>
	</c:when>
	<c:when test="${parameters.collectDob == '2'}">
		<tr valign="top">
			<td class="formlabel"><fmt:message key="year_of_birth" bundle="${resword}"/>:</td>
			<td>
				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>
							<div class="formfieldM_BG">
								<input type="text" name="yearOfBirth" size="15" value="<c:out value="${fields['dateOfBirth']}"/>" class="formfieldM">
							</div>
						</td>
						<td class="formlabel">
							(<fmt:message key="date_format_year" bundle="${resformat}"/>)<span class="alert">*</span>
							<c:if test="${parameters.discrepancyManagement}">
                <c:choose>
                  <c:when test="${hasDOBNote eq 'yes'}">
                    <a href="#" onClick="openDNWindow('ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=dob&column=date_of_birth','spanAlert-dob', '', event); return false;">
                      <img id="flag_dob" name="flag_dob" src="${dOBNote.resStatus.iconFilePath}" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>" >
                    </a>
                  </c:when>
                  <c:otherwise>
                    <a href="#" onClick="openDNWindow('CreateDiscrepancyNote?stSubjectId=0&name=subject&id=<c:out value="${subjectToUpdate.id}"/>&writeToDB=1&field=dob&column=date_of_birth','spanAlert-dob', '', event); return false;">
                      <img name="flag_dob" src="images/icon_noNote.gif" border="0" alt="<fmt:message key="discrepancy_note" bundle="${resword}"/>" title="<fmt:message key="discrepancy_note" bundle="${resword}"/>">
                      <input type="hidden" value="ViewDiscrepancyNote?writeToDB=1&stSubjectId=0&id=${subjectToUpdate.id}&name=subject&field=dob&column=date_of_birth">
                    </a>
                  </c:otherwise>
                </c:choose>
							</c:if>
						</td> 
					</tr>
				</table>
				<jsp:include page="../showMessage.jsp"><jsp:param name="key" value="yearOfBirth"/></jsp:include>
			</td>
		</tr>
	</c:when>
	<c:otherwise>
		<tr valign="top">
			<td class="formlabel"><fmt:message key="date_of_birth" bundle="${resword}"/>:</td>
			<td>
				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td>
							<div class="formfieldM_BG">
								<input type="text" name="dateOfBirth" disabled="true" size="15" value="<fmt:message key="not_used" bundle="${resword}"/>" class="formfieldM">
							</div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</c:otherwise>
	</c:choose>
</table>

</div>
</div></div></div></div></div></div></div></div>

</br>
</div>
<input type="button" onclick="formWithStateGoBackSmart('<fmt:message key="you_have_unsaved_data3" bundle="${resword}"/>', '${navigationURL}', '${defaultURL}');"  name="BTN_Smart_Back" value="<fmt:message key="back" bundle="${resword}"/>" class="button_medium medium_back"/>

<c:if test="${parameters.genderRequired || parameters.collectDob != 3 || (parameters.subjectPersonIdRequired != 'not_used' && parameters.subjectPersonIdRequired != 'copyFromSSID')}">
	<input type="submit" name="Submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium medium_continue">
</c:if>

<img src="images/icon_UnchangedData.gif" style="visibility:hidden" alt="Data Status" name="DataStatus_bottom">
</form>
</body>

<jsp:include page="../include/footer.jsp"/>
