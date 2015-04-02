<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>


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

<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='request' id='table' class='org.akaza.openclinica.web.bean.EntityBeanTable'/>
<jsp:useBean scope='request' id='message' class='java.lang.String'/>

<h1>
	<span class="first_level_header">
		<fmt:message key="administer_all_jobs" bundle="${resword}"/> 
		<a href="javascript:openDocWindow('help/6_4_administerJobs_Help.html')">
			<img src="images/bt_Help_Manage.gif" border="0" alt="<fmt:message key="help" bundle="${resword}"/>" title="<fmt:message key="help" bundle="${resword}"/>">
		</a>
	</span>
</h1>

<%-- <div class="homebox_bullets"><a href="ViewJob"><fmt:message key="view_all_export_data_jobs" bundle="${resword}"/></a></div> --%>
<%-- <div class="homebox_bullets"><a href="ViewImportJob"><fmt:message key="view_all_import_data_jobs" bundle="${resword}"/></a></div> --%>
<%-- <div  class="homebox_bullets"> <a title="View all Jobs" href='pages/listCurrentScheduledJobs' onclick="javascript:HighlightTab(1);"><fmt:message key="view_currently_executing_data_export_jobs" bundle="${resword}"/></a></div> --%>
<p></p>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>
<jsp:useBean id="now" class="java.util.Date" />
<P><I><fmt:message key="note_that_job_is_set" bundle="${resword}"/> <fmt:formatDate value="${now}" pattern="${dtetmeFormat}"/>.</I></P>

<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />
<input type="button" name="<fmt:message key="export_jobs" bundle="${resword}"/>" value="<fmt:message key="export_jobs" bundle="${resword}"/>" class="button_medium" onClick="window.location.href='ViewJob'"/>
<input type="button" name="<fmt:message key="import_jobs" bundle="${resword}"/>" value="<fmt:message key="import_jobs" bundle="${resword}"/>" class="button_medium" onClick="window.location.href='ViewImportJob'"/>
<input type="button" name="<fmt:message key="running_jobs" bundle="${resword}"/>" value="<fmt:message key="running_jobs" bundle="${resword}"/>" class="button_medium" onClick="window.location.href='pages/listCurrentScheduledJobs'"/>


 <jsp:include page="../include/footer.jsp"/>