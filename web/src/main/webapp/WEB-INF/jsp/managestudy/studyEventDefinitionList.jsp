<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<jsp:include page="../include/managestudy-header.jsp"/>


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
<h1><span class="title_manage"><fmt:message key="manage_all_event_definitions_in_study" bundle="${restext}"/> <c:out value="${study.name}"/>
    <a href="javascript:openDocWindow('help/5_2_eventDefinitions_Help.html')"><img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>"></a></span></h1>

<!-- <div style="float:right;padding-right:6px;width:8%;clear:both"> -->
<!--    <a href="javascript:openDocWindow('PrintAllEventCRF')" -->
<!--    onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');" -->
<!--    onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><img -->
<%--    name="bt_Print1" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print_all_available_crf" bundle="${resword}"/>" title="<fmt:message key="print_all_available_crf" bundle="${resword}"/>" align="left" hspace="6"></a> --%>
<!--    </div> -->

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
					value="Smart_<fmt:message key="back" bundle="${resword}"/>" 
					class="button_medium" 
					onClick="javascript: checkGoBackSmartEntryStatus('DataStatus_bottom', '<fmt:message key="you_have_unsaved_data3" bundle="${resword}"/>', '${navigationURL}', '${defaultURL}');"/>
	</td>
	<td>
		<input type="button" name="<fmt:message key="create_event" bundle="${resword}"/>" value="<fmt:message key="create_event" bundle="${resword}"/>" class="button_medium" onclick="javascript:window.location.href='DefineStudyEvent'"/>
	</td>
</table>
<jsp:include page="../include/footer.jsp"/>
