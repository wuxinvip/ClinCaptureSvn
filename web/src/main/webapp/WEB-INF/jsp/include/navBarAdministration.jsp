<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>

<c:set var="urlPrefix" value=""/>
<c:set var="requestFromSpringController" value="${param.isSpringController}" />
<c:set var="requestFromDoubleSpringController" value="${param.isDoubleSpringController}" />

<c:if test="${requestFromSpringController == 'true' }">
    <c:set var="urlPrefix" value="../"/>
</c:if>

<c:if test="${requestFromDoubleSpringController == 'true' }">
    <c:set var="urlPrefix" value="../../"/>
</c:if>

<br clear="all">
<div class="taskGroup"><fmt:message key="nav_administration" bundle="${resword}"/></div>
<div class="taskLeftColumn">
    <c:if test="${study.parentStudyId == 0 && userBean.name == 'root'}">
        <div class="taskLink"><a href="${urlPrefix}ListStudy"><fmt:message key="nav_studies" bundle="${resword}"/></a></div>
    </c:if>
    <div class="taskLink"><a href="${urlPrefix}ListSite"><fmt:message key="nav_sites" bundle="${resword}"/></a></div>
    <div class="taskLink"><a href="${urlPrefix}ListUserAccounts"><fmt:message key="nav_users" bundle="${resword}"/></a></div>
</div>
<div class="taskRightColumn">
	<c:if test="${!userRole.studySponsor}">
		<div class="taskLink"><a href="${urlPrefix}ViewAllJobs"><fmt:message key="nav_jobs" bundle="${resword}"/></a></div>
	</c:if>
	<div class="taskLink"><a href="${urlPrefix}ListSubject"><fmt:message key="nav_subjects" bundle="${resword}"/></a></div>
	<c:if test="${study.parentStudyId == 0 && (userBean.name == 'root' || (userRole.studyAdministrator && userBean.sysAdmin))}">
		<div class="taskLink"><a href="${urlPrefix}pages/EmailLog"><fmt:message key="email_log" bundle="${resword}"/></a></div>
	</c:if>
</div>
