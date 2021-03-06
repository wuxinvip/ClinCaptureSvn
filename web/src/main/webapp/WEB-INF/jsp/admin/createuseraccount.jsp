<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<jsp:include page="../include/admin-header.jsp"/>

<jsp:include page="../include/sideAlert.jsp"/>

<tr id="sidebar_Instructions_open" style="display: none">
    <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>
        <b><fmt:message key="instructions" bundle="${resword}"/></b>
        <div class="sidebar_tab_content"></div>
    </td>
</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
    <td class="sidebar_tab">
        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>
        <b><fmt:message key="instructions" bundle="${resword}"/></b>
    </td>
</tr>

<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='studies' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='roles' class='java.util.HashMap'/>
<jsp:useBean scope="request" id="presetValues" class="java.util.HashMap" />

<c:forEach var="currRole" items="${roles}" varStatus="status">
    <c:set var="rolesCount" value="${status.count}"/>
</c:forEach>
<c:choose>
    <c:when test="${isThisStudy}">
        <c:set var="inclRoleCode1" value="2"/>
        <c:set var="inclRoleCode2" value="6"/>
        <c:set var="inclRoleCode3" value="7"/>
        <c:set var="inclRoleCode4" value="8"/>
        <c:set var="inclRoleCode5" value="10"/>
    </c:when>
    <c:otherwise>
        <c:set var="inclRoleCode1" value="4"/>
        <c:set var="inclRoleCode2" value="5"/>
        <c:set var="inclRoleCode3" value="9"/>
    </c:otherwise>
</c:choose>

<c:set var="userName" value=""/>
<c:set var="firstName" value=""/>
<c:set var="lastName" value=""/>
<c:set var="email" value=""/>
<c:set var="phone" value=""/>
<c:set var="company" value=""/>
<c:set var="activeStudyId" value="${0}"/>
<c:set var="roleId" value="${0}"/>
<c:set var="userTypeId" value="${2}"/>
<c:set var="allowSoap" value="false"/>
<c:set var="displayPassword" value="false"/>

<c:forEach var="presetValue" items="${presetValues}">
    <c:if test='${presetValue.key == "userName"}'>
        <c:set var="userName" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "firstName"}'>
        <c:set var="firstName" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "lastName"}'>
        <c:set var="lastName" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "email"}'>
        <c:set var="email" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "phone"}'>
        <c:set var="phone" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "company"}'>
        <c:set var="company" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "activeStudy"}'>
        <c:set var="activeStudyId" value="${presetValue.value}"/>
    </c:if>
    <c:if test="${activeStudyId == 0}">
        <c:set var="activeStudyId" value="${activeStudy}"/>
    </c:if>
    <c:if test='${presetValue.key == "role"}'>
        <c:set var="roleId" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "type"}'>
        <c:set var="userTypeId" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "displayPassword"}'>
        <c:set var="displayPassword" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "allowSoap"}'>
        <c:set var="allowSoap" value="${presetValue.value}"/>
    </c:if>
    <c:if test='${presetValue.key == "notifyPassword"}'>
        <c:set var="notifyPassword" value="${presetValue.value}"/>
    </c:if>
	<c:if test='${presetValue.key == "timeZone"}'>
		<c:set var="timeZone" value="${empty presetValue.value ? defaultTimeZoneID : presetValue.value}"/>
	</c:if>
</c:forEach>

<script type="text/JavaScript" language="JavaScript">
    function sendUrl() {
        document.getElementById('changeRoles').value = 'true';
        document.forms[1].submit();
    }
</script>

<h1>
	<span class="first_level_header">
		<fmt:message key="create_a_user_account" bundle="${resword}"/>
	</span>
</h1>

<span class="alert">*</span><fmt:message key="indicates_required_field" bundle="${resword}"/>
<form action="CreateUserAccount" method="post">
<jsp:include page="../include/showSubmitted.jsp" />

<div style="width: 450px">

<div class="box_T">
<div class="box_L">
<div class="box_R">
<div class="box_B">
<div class="box_TL">
<div class="box_TR">
<div class="box_BL">
<div class="box_BR">

<div class="tablebox_center">

<input type="hidden" id="changeRoles" name="changeRoles">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
<tr>
<tr valign="top">
    <td class="formlabel"><fmt:message key="username2" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <input type="text" id="userName" onchange="javascript:changeIcon();" name="userName" value="<c:out value="${userName}"/>" size="20" class="formfieldM"/>
                    </div>
                </td>
                <td class="alert">*</td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="userName"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr valign="top">
    <td class="formlabel"><fmt:message key="first_name" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <input type="text" id="firstName" name="firstName" onchange="javascript:changeIcon();" value="<c:out value="${firstName}"/>" size="20" class="formfieldM"/>
                    </div>
                </td>
                <td class="alert">*</td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="firstName"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr valign="top">
    <td class="formlabel"><fmt:message key="last_name" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <input type="text" id="lastName" name="lastName" onchange="javascript:changeIcon();" value="<c:out value="${lastName}"/>" size="20" class="formfieldM"/>
                    </div>
                </td>
                <td class="alert">*</td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="lastName"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr valign="top">
    <td class="formlabel"><fmt:message key="email" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <input type="text" id="email" name="email" onchange="javascript:changeIcon();" value="<c:out value="${email}"/>" size="20" class="formfieldM"/>
                    </div>
                </td>
                <td>(<fmt:message key="username@institution" bundle="${resword}"/>) <span class="alert">*</span></td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="email"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr valign="top">
    <td class="formlabel"><fmt:message key="phone" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <input type="text" id="phone" name="phone" maxlength="20" onchange="javascript:changeIcon();" value="<c:out value="${phone}"/>" size="20" class="formfieldM"/>
                    </div>
                </td>
                <td><span style="white-space: nowrap;">(<fmt:message key="phone_number_format_ex" bundle="${resword}"/> <fmt:message key="phone_format" bundle="${resformat}"/>)</span><br/>&nbsp;</td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="phone"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr valign="top">
    <td class="formlabel"><fmt:message key="institutional_affiliation" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <input type="text" id="company" name="company" onchange="javascript:changeIcon();" value="<c:out value="${company}"/>" size="20" class="formfieldM"/>
                    </div>
                </td>
                <td class="alert">*</td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="company"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>
	<tr valign="top">
		<td class="formlabel"><fmt:message key="time_zone" bundle="${resword}"/>:</td>
		<td valign="top">
			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td valign="top">
						<div class="formfieldXL_BG">
							<select id="timeZone" name="timeZone" class="formfieldXL">
								<c:forEach var="timeZoneID" items="${timeZoneIDsSorted}">
									<option value='<c:out value="${timeZoneID.key}" />' <c:if test="${timeZoneID.key == timeZone}">selected</c:if>>
										<c:out value="${timeZoneID.value}"/>
									</option>
								</c:forEach>
							</select>
						</div>
					</td>
				</tr>
			</table>
		</td>
	</tr>

<tr valign="top" id="studyTr">
    <td class="formlabel"><fmt:message key="active_study" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldXL_BG">
                        <select name="activeStudy" id="activeStudy" class="formfieldXL" onchange="sendUrl();">
                            <option value="0">-<fmt:message key="select" bundle="${resword}"/>-</option>
                            <c:forEach var="study" items="${studies}">
                                <c:choose>
                                    <c:when test="${activeStudy == study.id}">
                                        <c:choose>
                                            <c:when test="${study.parentStudyId>0}">
                                                <option value='<c:out value="${study.id}" />' selected>&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${study.name}"/></option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value='<c:out value="${study.id}" />' selected><c:out value="${study.name}"/></option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${study.parentStudyId>0}">
                                                <option value='<c:out value="${study.id}" />'>&nbsp;&nbsp;&nbsp;&nbsp;<c:out value="${study.name}"/></option>
                                            </c:when>
                                            <c:otherwise>
                                                <option value='<c:out value="${study.id}" />'><c:out value="${study.name}"/></option>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </select>
                    </div>
                </td>
                <td class="alert">*</td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="activeStudy"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>

<tr valign="top" id="roleTr">
    <td class="formlabel"><fmt:message key="role" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <select name="role" id="role" onchange="javascript:changeIcon();" class="formfieldM">
                            <option value="0">-<fmt:message key="select" bundle="${resword}"/>-</option>
                            <c:forEach var="currRole" items="${roles}">
                                <c:if test="${currRole.key == inclRoleCode1 || currRole.key == inclRoleCode2 || currRole.key == inclRoleCode3 || currRole.key == inclRoleCode4 || currRole.key == inclRoleCode5}">
                                    <option value='<c:out value="${currRole.key}" />'
                                            <c:if test="${roleId == currRole.key}">selected</c:if>><c:out value="${currRole.value}"/>
                                    </option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </div>
                </td>
                <td class="alert">*</td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="role"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr valign="top">
    <td class="formlabel"><fmt:message key="user_type" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <div class="formfieldM_BG">
                        <select name="type" id="type" onchange="javascript:changeIcon();" class="formfieldM">
                            <c:forEach var="currType" items="${types}">
                                <c:choose>
                                    <c:when test="${userTypeId == currType.id}">
                                        <option value='<c:out value="${currType.id}" />' selected><c:out value="${currType.name}"/></option>
                                    </c:when>
                                    <c:otherwise>
                                        <option value='<c:out value="${currType.id}" />'><c:out value="${currType.name}"/></option>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </select>
                    </div>
                </td>
                <td>&nbsp;</td>
            </tr>
        </table>
    </td>
</tr>
<tr valign="top">
    <td class="formlabel"><fmt:message key="can_run_web_services" bundle="${resword}"/>:</td>
    <td valign="top">
        <table border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td valign="top">
                    <br><input type="checkbox" name="allowSoap" onchange="javascript:changeIcon();" id="allowSoap" value="true"
                               <c:if test="${allowSoap}">checked</c:if>/>
                </td>
                <td></td>
            </tr>
            <tr>
                <td colspan="2">
                    <jsp:include page="../showMessage.jsp">
                        <jsp:param name="key" value="allowSoap"/>
                    </jsp:include>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr valign="top">
    <td class="formlabel"><fmt:message key="user_password_generated" bundle="${resword}"/>:</td>
    <td>
        <input type="radio" id="displayPwd0" checked name="displayPassword" onchange="javascript:changeIcon();" value="false">
        <fmt:message key="send_user_password_via_email" bundle="${resword}"/>
        <br>
        <input type="radio" id="displayPwd1" name="displayPassword" onchange="javascript:changeIcon();" value="true" <c:if test="${displayPassword}">checked</c:if>>
        <fmt:message key="show_user_password_to_admin" bundle="${resword}"/>
    </td>
</tr>
</table>
</div>

</div>
</div>
</div>
</div>
</div>
</div>
</div>
</div>

</div>
<br>
<table border="0" cellpadding="0" cellspacing="0">
    <tr>
        <td>
            <input type="button" name="BTN_Smart_Back_A" id="GoToPreviousPage" value="<fmt:message key="back" bundle="${resword}"/>" class="button_medium medium_back"
                   onClick="javascript: checkGoBackSmartEntryStatus('DataStatus_bottom', '<fmt:message key="you_have_unsaved_data3" bundle="${resword}"/>', '${navigationURL}', '${defaultURL}');"/>
        </td>
        <td>
            <input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium medium_submit"/>
        </td>
        <td>
            <img src="images/icon_UnchangedData.gif" style="visibility:hidden" title="You have not changed any data in this page." alt="Data Status" name="DataStatus_bottom">
            <c:if test="${pageIsChanged ne null && pageIsChanged eq true}">
                <script>
                    $("img[name=DataStatus_bottom]").attr("src", "images/icon_UnsavedData.gif");
                </script>
            </c:if>
        </td>
    </tr>
</table>
</form>
<jsp:include page="../include/footer.jsp"/>
