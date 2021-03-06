<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/managestudy-header.jsp"/>
<script type="text/javascript" language="javascript">
    jQuery(window).load(function(){

    	highlightLastAccessedObject();
    });
    
</script>


<!-- *JSP* ${pageContext.page['class'].simpleName} -->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		<div class="sidebar_tab_content">
        <fmt:message key="study_can_have_many_event_with_more_CRF" bundle="${restext}"/>
        <br><br>
        <fmt:message key="click_up_down_to_order" bundle="${restext}"/>
         <br><br>
        <fmt:message key="event_also_locked_prevent" bundle="${restext}"/>
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${restext}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<jsp:useBean scope="request" id="defSize" type="java.lang.Integer" />
<h1>
	<span class="first_level_header">
		<fmt:message key="manage_all_event_definitions_in_study" bundle="${restext}"/> <c:out value="${study.name}"/>
		<a href="javascript:openDocWindow('help/5_2_eventDefinitions_Help.html')">
			<img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>">
		</a>
	</span>
</h1>

<div style="clear:both">

</div>

<p></p>
<c:import url="../include/showTableForEventDefinition.jsp">
    <c:param name="rowURL" value="showStudyEventDefinitionRow.jsp" />
    <c:param name="groupNum" value="${0}"/>
</c:import>
<br><br>



<table>
	<td>
		<input type="button" name="BTN_Smart_Back_A" id="GoToPreviousPage" 
					value="<fmt:message key="back" bundle="${resword}"/>" 
					class="button_medium medium_back" 
					onClick="javascript: checkGoBackSmartEntryStatus('DataStatus_bottom', '<fmt:message key="you_have_unsaved_data3" bundle="${resword}"/>', '${navigationURL}', '${defaultURL}');"/>
	</td>
	<td>
		<c:set var="createEventBTNCaption"><fmt:message key="create_event" bundle="${resword}"/></c:set>
		<input type="button" name="createEventBTN" value="${createEventBTNCaption}"
			   class="${ui:getHtmlButtonCssClass(createEventBTNCaption, "")}"
			   onclick="javascript:window.location.href='DefineStudyEvent?actionName=init'"/>
	</td>
	<c:if test="${isAnyCalendaredEventExist}">
		<td>
			<c:set var="calendaredEventsBTNCaption"><fmt:message key="calendared_events" bundle="${resword}"/></c:set>
			<input type="button" name="calendaredEventsBTN" value="${calendaredEventsBTNCaption}"
				   class="${ui:getHtmlButtonCssClass(calendaredEventsBTNCaption, "")}"
				   onclick="javascript:openDocWindow('ShowCalendarFunc?id=<c:out value="${study.id}"/>')"/>
		</td>
	</c:if>
</table>
<input id="accessAttributeName" type="hidden" value="data-cc-eventDefinitionId">
<jsp:include page="../include/footer.jsp"/>
