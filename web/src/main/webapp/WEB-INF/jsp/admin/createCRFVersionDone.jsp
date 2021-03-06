<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<c:choose>
<c:when test="${userBean.sysAdmin}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-header.jsp"/>
</c:otherwise>
</c:choose>

<!--
<crf-id>${crfId}</crf-id>
-->
<!--
<crf-version-id>${crfVersionId}</crf-version-id>
-->

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


<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope="request" id="queries" class="java.util.ArrayList"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<h1>
	<span class="first_level_header">
		<fmt:message key="create_a_new_CRF_version" bundle="${resword}"/> - <fmt:message key="data_committed_successfully" bundle="${resword}"/>
	</span>
</h1>
<br/>
<fmt:message key="the_new_CRF_version_was_committed_into" bundle="${restext}"/>
<br/>
<br>
<table>
<td>
 <input id="GoToCRFList" class="button_medium" type="button" name="BTN_Exit" title=""<fmt:message key="go_back_to_the_CRF_list" bundle="${restext}"/>"" value="<fmt:message key="exit" bundle="${resword}"/>" onclick="window.location.href=('ListCRF');"/>
</td>
<td>
 <input id="ViewCRF" class="button_medium" type="button" name="BTN_View" title="<fmt:message key="view_CRF_version_data_entry" bundle="${resword}"/>" value="<fmt:message key="view_CRF" bundle="${resword}"/>" onclick="window.location.href=('ViewSectionDataEntry?crfVersionId=${crfVersionId}&tabId=1');"/>
</td>
<td> 
 <input id="ViewCRFMetadata" class="button_medium" type="button" name="BTN_View_Metadata" title="<fmt:message key="view_CRF_metadata" bundle="${resword}"/>" value="<fmt:message key="view_CRF_metadata" bundle="${resword}"/>" onclick="window.location.href=('ViewCRFVersion?id=${crfVersionId}');"/>
</td>
	<td>
		<input id="GoToEventsList" class="button_medium" type="button" name="BTN_View_Events" title=""<fmt:message key="nav_view_events" bundle="${restext}"/>"" value="<fmt:message key="nav_view_events" bundle="${resword}"/>" onclick="window.location.href=('ListEventDefinition');"/>
	</td>
</table>

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
