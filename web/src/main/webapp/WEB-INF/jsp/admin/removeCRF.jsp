<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<%@ taglib uri="/WEB-INF/tlds/format/date/date-time-format.tld" prefix="cc-fmt" %>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:choose>
	<c:when test="${userBean.sysAdmin}">
		<c:import url="../include/admin-header.jsp"/>
	</c:when>
	<c:otherwise>
		<c:import url="../include/managestudy-header.jsp"/>
	</c:otherwise>
</c:choose>

<!-- *JSP* ${pageContext.page['class'].simpleName} -->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->

<tr id="sidebar_Instructions_open" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
		<div class="sidebar_tab_content">
		</div>
	</td>
</tr>
<tr id="sidebar_Instructions_closed" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_expand.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
	</td>
</tr>

<jsp:include page="../include/sideInfo.jsp"/>
<jsp:useBean scope='request' id='eventCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='crfToRemove' class='org.akaza.openclinica.bean.admin.CRFBean'/>

<h1>
	<span class="first_level_header">
		<fmt:message key="confirm_removal_of_CRF" bundle="${resword}"/>
	</span>
</h1>

<p><fmt:message key="you_choose_to_remove_the_following_CRF" bundle="${restext}"/>:</p>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center_admin">
	<table class="table_vertical">
		<tr>
			<td><fmt:message key="name" bundle="${resword}"/>:</td>
			<td> <c:out value="${crfToRemove.name}"/></td>
		</tr>
		<tr>
			<td><fmt:message key="description" bundle="${resword}"/>:</td>
			<td> <c:out value="${crfToRemove.description}"/></td>
		</tr>
	</table>
</div>

</div></div></div></div></div></div></div></div>
</div>
<br/>

<span class="table_title_Admin">
	<fmt:message key="CRF_versions" bundle="${resword}"/>
</span>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center_admin">
	<table class="table_horizontal">
		<tr>
			<td><fmt:message key="CRF_name" bundle="${resword}"/></td>
			<td><fmt:message key="version_name" bundle="${resword}"/></td>
			<td><fmt:message key="description" bundle="${resword}"/></td>
			<td><fmt:message key="status" bundle="${resword}"/></td>
			<td><fmt:message key="revision_notes" bundle="${resword}"/></td>
		</tr>
		<c:forEach var ="version" items="${crfToRemove.versions}">
		<tr>
			<td><c:out value="${crfToRemove.name}"/></td>
			<td><c:out value="${version.name}"/></td>
			<td><c:out value="${version.description}"/></td>
			<c:choose>
				<c:when test="${version.status.available}">
					<td class="aka_green_highlight"><c:out value="${version.status.name}"/></td>	
				</c:when>
				<c:when test="${version.status.deleted || version.status.locked}">
					<td class="aka_red_highlight"><c:out value="${version.status.name}"/></td>	
				</c:when>
				<c:otherwise>
					<td><c:out value="${version.status.name}"/></td>	
				</c:otherwise>
			</c:choose>
			<td><c:out value="${version.revisionNotes}"/></td>
		</tr>
		</c:forEach>
	</table>
</div>

</div></div></div></div></div></div></div></div>
</div>
<br>

<span class="table_title_Admin">
	<fmt:message key="event_CRFs" bundle="${resword}"/>
</span>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center_admin">
	<table class="table_horizontal">
		<tr valign="top">
			<td><fmt:message key="SE" bundle="${resword}"/></td>
			<td><fmt:message key="date_interviewed" bundle="${resword}"/></td>
			<td><fmt:message key="status" bundle="${resword}"/></td>
		</tr>
		<c:forEach var="eventCRF" items="${eventCRFs}">
		<tr valign="top">
			<td><c:out value="${eventCRF.eventName}"/></td>
			<td>
				<cc-fmt:formatDate value="${eventCRF.dateInterviewed}" pattern="${dteFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
			</td>
			<c:choose>
				<c:when test="${eventCRF.status.available}">
					<td class="aka_green_highlight"><c:out value="${eventCRF.status.name}"/></td>	
				</c:when>
				<c:when test="${eventCRF.status.deleted || eventCRF.status.locked}">
					<td class="aka_red_highlight"><c:out value="${eventCRF.status.name}"/></td>	
				</c:when>
				<c:otherwise>
					<td><c:out value="${eventCRF.status.name}"/></td>	
				</c:otherwise>
			</c:choose>
		</tr>
		</c:forEach>
	</table>
</div>

</div></div></div></div></div></div></div></div>
</div>
<br>

<form action='RemoveCRF?action=submit&id=<c:out value="${crfToRemove.id}"/>' method="POST">
  <input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium medium_back"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />
  <input type="submit" name="submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium medium_submit" onClick='return confirmSubmit({ message: "<fmt:message key="if_you_remove_this_CRF" bundle="${restext}"/>", height: 150, width: 500, submit: this });'>
  <input type = "hidden" name = "confirmPagePassed" value = "true" />
</form>

<jsp:include page="../include/footer.jsp"/>
