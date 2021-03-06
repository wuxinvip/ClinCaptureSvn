<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<%@ taglib uri="/WEB-INF/tlds/format/date/date-time-format.tld" prefix="cc-fmt" %>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<jsp:include page="../include/admin-header.jsp"/>

<jsp:include page="../include/sidebar.jsp"/>

<jsp:useBean scope="request" id="displayEventCRF" class="org.akaza.openclinica.bean.submit.DisplayEventCRFBean"/>
<jsp:useBean scope="request" id="event" class="org.akaza.openclinica.bean.managestudy.StudyEventBean"/>
<jsp:useBean scope="request" id="items" class="java.util.ArrayList"/>
<jsp:useBean scope="request" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<h1>
	<span class="first_level_header">
		<fmt:message key="delete_CRF_from_event" bundle="${resword}"/>
	</span>
</h1>
<p>
<fmt:message key="confirm_deletion_of_this_CRF" bundle="${restext}">
	<fmt:param value="${event.studyEventDefinition.name}"/>
	<fmt:param>
	  <cc-fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
	</fmt:param>
</fmt:message>
</p>
<jsp:include page="../include/alertbox.jsp" />

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr valign="top"><td class="table_header_column"><fmt:message key="event_definition_name" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.studyEventDefinition.name}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="location" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.location}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="visit" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.sampleOrdinal}"/></td></tr>

  <tr valign="top">
	  <td class="table_header_column">
		  <fmt:message key="date_started" bundle="${resword}"/>:
	  </td>
	  <td class="table_cell">
		  <cc-fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
	  </td>
  </tr>
  <tr valign="top">
	  <td class="table_header_column">
		  <fmt:message key="date_ended" bundle="${resword}"/>:
	  </td>
	  <td class="table_cell">
		  <cc-fmt:formatDate value="${event.dateEnded}" pattern="${dteFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
	  </td>
  </tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="status" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${event.status.name}"/>
  </td></tr>

 </table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<span class="table_title_admin"><fmt:message key="event_CRF" bundle="${resword}"/></span>
<div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
		<td class="table_header_column_top"><fmt:message key="CRF_name" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="version" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="date_interviewed" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="interviewer_name" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="owner" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="completion_status" bundle="${resword}"/></td>

	 </tr>
	<tr>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.crf.name}" /></td>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.crfVersion.name}" /></td>
		<td class="table_cell">
			<cc-fmt:formatDate value="${displayEventCRF.eventCRF.dateInterviewed}" pattern="${dteFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
			&nbsp;
		</td>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.interviewerName}"/>&nbsp;</td>
		<td class="table_cell"><c:out value="${displayEventCRF.eventCRF.owner.name}" /></td>
		<td class="table_cell"><c:out value="${displayEventCRF.stage.name}" /></td>

	 </tr>

 </table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 <c:if test="${!empty items}">
 <span class="table_title_admin"><fmt:message key="item_data" bundle="${resword}"/></span>
 <div style="width: 600px">
 <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">
    <tr>
		<td class="table_header_column_top"><fmt:message key="Id" bundle="${resword}"/></td>
		<td class="table_header_column_top"><fmt:message key="value" bundle="${resword}"/></td>
	 </tr>
	 <c:set var="count" value="0"/>
  <c:forEach var="item" items="${items}">
	<tr>
		<td class="table_cell"><c:out value="${item.itemId}" /></td>
		<td class="table_cell"><c:out value="${item.value}" />&nbsp;</td>


		<c:if test="${item.status.name !='removed' && item.status.name !='auto-removed'}">
		<c:set var="count" value="${count+1}"/>
		</c:if>
	 </tr>
	</c:forEach>

 </table>

</div>
</div></div></div></div></div></div></div></div>

</div>
<br>
 </c:if>
   <c:choose>
    <c:when test="${!empty items && count>0}">
     <form action='DeleteEventCRF?action=submit&ecId=<c:out value="${displayEventCRF.eventCRF.id}"/>&ssId=<c:out value="${studySub.id}"/>' method="POST">
     
<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium medium_back"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />

      <input type="submit" name="BTN_Submit"  value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium medium_submit" onClick='return confirmSubmit({ message: "<fmt:message key="this_CRF_has_data_want_delete" bundle="${restext}"/>", height: 150, width: 500, submit: this });'>
     </form>
    </c:when>
    <c:otherwise>
      <form action='DeleteEventCRF?action=submit&ecId=<c:out value="${displayEventCRF.eventCRF.id}"/>&ssId=<c:out value="${studySub.id}"/>' method="POST">

<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium medium_back"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />
      
      <input type="submit" name="BTN_Submit"  value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium medium_submit" onClick='return confirmSubmit({ message: "<fmt:message key="are_you_sure_you_want_to_delete_it" bundle="${restext}"/>", height: 150, width: 500, submit: this });'>
     </form>
    </c:otherwise>
   </c:choose>

<c:choose>
  <c:when test="${userBean.sysAdmin}">
  <c:import url="../include/workflow.jsp">
   <c:param name="module" value="admin"/>
  </c:import>
 </c:when>
  <c:otherwise>
   <c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/>
  </c:import>
  </c:otherwise>
 </c:choose>
<jsp:include page="../include/footer.jsp"/>
