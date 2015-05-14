<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<%@ taglib uri="/WEB-INF/tlds/format/date/date-time-format.tld" prefix="cc-fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.audit_events" var="resaudit"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<html>
<head>
    <link rel="icon" href="<c:url value='/images/favicon.ico'/>" />
    <link rel="shortcut icon" href="<c:url value='/images/favicon.ico'/>" />
    <link rel="stylesheet" href="includes/styles.css?r=${revisionNumber}" type="text/css">
    <script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-1.3.2.min.js"></script>
    <script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js?r=${revisionNumber}"></script>
    <ui:theme/>
</head>

<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>
<c:set var="dtetmeFormat"><fmt:message key="date_time_format_string" bundle="${resformat}"/></c:set>

<body>
<a name="root"></a>
<h1>
	<span class="first_level_header">
    <c:out value="${studySub.label}"/> <fmt:message key="audit_logs" bundle="${resword}"/>    </span>
</h1>

<!-- Excel Export Button --><form action="ExportExcelStudySubjectAuditLog">
    <input type="hidden" value="<c:out value="${id}"/>" name="id"/><br>
    <input type="submit" value="Export to Excel" class="button_xlong"/><br>
   </form><!-- End Excel Export Button -->


<fmt:message key="study_subject_ID" bundle="${resword}" var="studySubjectLabel"/><c:if test="${study ne null}">
    <c:set var="studySubjectLabel" value="${study.studyParameterConfig.studySubjectIdLabel}"/>
</c:if>
<c:set var="secondaryIdShow" value="${true}"/>
<fmt:message key="secondary_subject_ID" bundle="${resword}" var="secondaryIdLabel"/>
<c:if test="${study ne null}">    <c:set var="secondaryIdShow" value="${!(study.studyParameterConfig.secondaryIdRequired == 'not_used')}"/>
    <c:set var="secondaryIdLabel" value="${study.studyParameterConfig.secondaryIdLabel}"/>
</c:if>
<%-- Subject Summary --%>
<table border="0" cellpadding="0" cellspacing="0" width="650" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
	<tr>
        <td class="table_header_column_top" style="color: #789EC5"><b>${studySubjectLabel}</b></td>
        <c:if test="${secondaryIdShow}">
            <td class="table_header_column_top" style="color: #789EC5"><b>${secondaryIdLabel}</b></td>
        </c:if>
		<td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_of_birth" bundle="${resword}"/></b></td>
		<td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="person_ID" bundle="${resword}"/></b></td>
		<td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="created_by" bundle="${resword}"/></b></td>
		<td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="status" bundle="${resword}"/></b></td>
	</tr>

	<tr>
        <td class="table_header_column"><c:out value="${studySub.label}"/></td>
        <c:if test="${secondaryIdShow}">
            <td class="table_header_column"><c:out value="${studySub.secondaryLabel}"/>&nbsp</td>
        </c:if>
        <td class="table_header_column"><fmt:formatDate value="${subject.dateOfBirth}" pattern="${dteFormat}"/>&nbsp</td>
        <td class="table_header_column"><c:out value="${subject.uniqueIdentifier}"/>&nbsp;</td>
        <td class="table_header_column"><c:out value="${studySub.owner.name}"/>&nbsp;</td>
        <td class="table_header_column"><c:out value="${studySub.status.name}"/></td>

    </tr>
</table><br><br>

		<!-- excel encoding -->
	
<%-- Subject Audit Events --%>
<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
    <tr>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="local_date_time" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
    </tr>
    <c:forEach var="studySubjectAudit" items="${studySubjectAudits}">
        <tr>
            <td class="table_header_column"><fmt:message key="${studySubjectAudit.auditEventTypeName}" bundle="${resaudit}"/>&nbsp;</td>
            <!-- YW 12-06-2007, use dateStyle and timeStyle to display datetime -->
            <td class="table_header_column">
				<cc-fmt:formatDate value="${studySubjectAudit.auditDate}" pattern="${dtetmeFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
				&nbsp;
			</td>
            <td class="table_header_column"><c:out value="${studySubjectAudit.userName}"/>&nbsp;</td>
            <td class="table_header_column"><c:out value="${studySubjectAudit.entityName}"/>&nbsp;</td>
            <td class="table_header_column"><c:out value="${studySubjectAudit.oldValue}"/>&nbsp;</td>
            <td class="table_header_column"><c:out value="${studySubjectAudit.newValue}"/>&nbsp;</td>

        </tr>
    </c:forEach>
</table>
<br>
<%-- Study Events--%>
<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
    <tr>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="study_events" bundle="${resword}"/></b><br></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="location" bundle="${resword}"/></b><br></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date" bundle="${resword}"/></b><br></td>
        <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="occurrence_number" bundle="${resword}"/></b><br></td>
    </tr>
    <c:forEach var="event" items="${events}">
        <tr>
			<!-- Link to Dynamic Anchor -->
			<td class="table_header_column"><a href="#<c:out value="${event.studyEventDefinition.name}"/><c:out value="${event.sampleOrdinal}"/>"><c:out value="${event.studyEventDefinition.name}"/>&nbsp;</a></td>
            <td class="table_header_column"><c:out value="${event.location}"/>&nbsp;</td>
            <c:choose>
                <c:when test="${event.startTimeFlag=='false'}">
                    <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>
                </c:when>
                <c:otherwise>
                    <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" type="both" pattern="${dteFormat}" timeStyle="short"/>&nbsp;</td>
                </c:otherwise>
            </c:choose>
            <td class="table_header_column"><c:out value="${event.sampleOrdinal}"/>&nbsp;</td>
        </tr>
    </c:forEach>
</table>
<br>
<c:forEach var="event" items="${events}">
<%-- Study Event Summary --%>
<!-- Embedded Anchor -->
<a name="<c:out value="${event.studyEventDefinition.name}"/><c:out value="${event.sampleOrdinal}"/>"></a>
<table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
<tr>
    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="name" bundle="${resword}"/></b></td>
    <td class="table_header_column_top" style="color: #789EC5"><b><c:out value="${event.studyEventDefinition.name}"/></b>&nbsp;</td>
</tr>
<tr>
    <td class="table_header_column"><c:out value="Location"/></td>
    <td class="table_header_column"><c:out value="${event.location}"/>&nbsp;</td>
</tr>
<tr>
    <td class="table_header_column"><c:out value="Start Date"/></td>
    <c:choose>
        <c:when test="${event.startTimeFlag=='false'}">
            <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" pattern="${dteFormat}"/>&nbsp;</td>
        </c:when>
        <c:otherwise>
            <td class="table_header_column"><fmt:formatDate value="${event.dateStarted}" type="both" pattern="${dteFormat}" timeStyle="short"/>&nbsp;</td>
        </c:otherwise>
    </c:choose>
</tr>
<tr>
    <td class="table_header_column"><c:out value="Status"/></td>
    <td class="table_header_column"><c:out value="${event.subjectEventStatus.name}"/>&nbsp;</td>
</tr>
<tr>
    <td class="table_header_column"><fmt:message key="occurrence_number" bundle="${resword}"/></td>
    <td class="table_header_column"><c:out value="${event.sampleOrdinal}"/>&nbsp;</td>
</tr>
<tr><td colspan="2" class="table_header_column" style="border-style: solid; border-right-width: 0px;">&nbsp;</td></tr>

<tr>
    <td colspan="2">
            <%--Audit for deleted event crfs --%>
        <table border="0"><tr><td width="20">&nbsp;</td><td><%-- Margin --%>
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="name" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="version" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="deleted_by" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="delete_date" bundle="${resword}"/></b></td>
                </tr>
                <c:forEach var="deletedEventCRF" items="${allDeletedEventCRFs}">
                    <c:if test="${deletedEventCRF.studyEventId==event.id}">

                        <tr>
                            <td class="table_header_column"><c:out value="${deletedEventCRF.crfName}"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${deletedEventCRF.crfVersion}"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${deletedEventCRF.deletedBy}"/>&nbsp;</td>
                            <td class="table_header_column">
								<cc-fmt:formatDate value="${deletedEventCRF.deletedDate}" pattern="${dteFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
								&nbsp;
							</td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table><%-- Event CRFs Audit Events --%>
        </td></tr></table><%-- Margin --%>

    </td>
</tr>

<tr><td colspan="2">&nbsp;</td></tr>
<tr>
    <td colspan="2">
            <%--Audit Events for Study Event --%>
        <table border="0"><tr><td width="20">&nbsp;</td><td><%-- Margin --%>
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="local_date_time" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
                </tr>

                <c:forEach var="studyEventAudit" items="${studyEventAudits}">
                    <c:if test="${studyEventAudit.entityId==event.id}">
                        <tr>
                            <td class="table_header_column"><fmt:message key="${studyEventAudit.auditEventTypeName}" bundle="${resaudit}"/>&nbsp;</td>
                            <td class="table_header_column">
								<cc-fmt:formatDate value="${studyEventAudit.auditDate}" pattern="${dtetmeFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
								&nbsp;
							</td>
                            <td class="table_header_column"><c:out value="${studyEventAudit.userName}"/>&nbsp;</td>
                            <td class="table_header_column"><c:out value="${studyEventAudit.entityName}"/>&nbsp;</td>
                            <td class="table_header_column">
                                        <c:choose>
                                            <c:when test="${studyEventAudit.oldValue eq '0'}"><fmt:message key="invalid" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '1'}"><fmt:message key="scheduled" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '2'}"><fmt:message key="not_scheduled" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '3'}"><fmt:message key="data_entry_started" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '4'}"><fmt:message key="completed" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '5'}"><fmt:message key="stopped" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '6'}"><fmt:message key="skipped" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '7'}"><fmt:message key="locked" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '8'}"><fmt:message key="signed" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '9'}"><fmt:message key="source_data_verified" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.oldValue eq '10'}"><fmt:message key="deleted" bundle="${resterm}"/></c:when>
                                            <c:otherwise><c:out value="${studyEventAudit.oldValue}"/></c:otherwise>
                                        </c:choose>
                                &nbsp;</td>
                            <td class="table_header_column">
                                <c:choose>
                                    <%-- A removed Study Event ...--%>
                                    <c:when test="${studyEventAudit.newValue eq '5'}">
                                        <c:choose>
                                            <c:when test="${studyEventAudit.newValue eq '0'}"><fmt:message key="invalid" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '1'}"><fmt:message key="available" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '2'}"><fmt:message key="pending" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '3'}"><fmt:message key="private" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '4'}"><fmt:message key="completed" bundle="${resword}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '5'}"><fmt:message key="removed" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '6'}"><fmt:message key="locked" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '7'}"><fmt:message key="auto-removed" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '8'}"><fmt:message key="signed" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '9'}"><fmt:message key="source_data_verified" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '10'}"><fmt:message key="deleted" bundle="${resterm}"/></c:when>
                                            <c:otherwise><c:out value="${studyEventAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${studyEventAudit.newValue eq '0'}"><fmt:message key="invalid" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '1'}"><fmt:message key="scheduled" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '2'}"><fmt:message key="not_scheduled" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '3'}"><fmt:message key="data_entry_started" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '4'}"><fmt:message key="completed" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '5'}"><fmt:message key="stopped" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '6'}"><fmt:message key="skipped" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '7'}"><fmt:message key="locked" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '8'}"><fmt:message key="signed" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '9'}"><fmt:message key="source_data_verified" bundle="${resterm}"/></c:when>
                                            <c:when test="${studyEventAudit.newValue eq '10'}"><fmt:message key="deleted" bundle="${resterm}"/></c:when>
                                            <c:otherwise><c:out value="${studyEventAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                                &nbsp;</td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table><%-- Event CRFs Audit Events --%>
        </td></tr></table><%-- Margin --%>

    </td>
</tr>

    <%-- Event CRFs for this Study Event --%>
<c:forEach var="eventCRF" items="${event.eventCRFs}">
    <tr>
        <td colspan="2">
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="name" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="version" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="date_interviewed" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="interviewer_name" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="owner" bundle="${resword}"/></b></td>
                </tr>
                <tr>
                    <td class="table_header_column"><c:out value="${eventCRF.crf.name}"/>&nbsp;</td>
                    <td class="table_header_column"><c:out value="${eventCRF.crfVersion.name}"/>&nbsp;</td>
                    <td class="table_header_column">
						<fmt:formatDate value="${eventCRF.dateInterviewed}" type="both" pattern="${dteFormat}" timeStyle="short"/>&nbsp;
					</td>
                    <td class="table_header_column"><c:out value="${eventCRF.interviewerName}"/>&nbsp;</td>
                    <td class="table_header_column"><c:out value="${eventCRF.owner.name}"/>&nbsp;</td>
                </tr>
            </table>
        </td>
    </tr>
    <tr>
    <tr><td colspan="2">&nbsp;</td></tr>
    <td colspan="2">

            <%-- Event CRFs Audit Events --%>
        <table border="0"><tr><td width="20">&nbsp;</td><td><%-- Margin --%>
            <table border="0" cellpadding="0" cellspacing="0" width="550" style="border-style: solid; border-width: 1px; border-color: #CCCCCC;">
                <tr>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="audit_event" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="local_date_time" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="user" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="value_type" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="old" bundle="${resword}"/></b></td>
                    <td class="table_header_column_top" style="color: #789EC5"><b><fmt:message key="new" bundle="${resword}"/></b></td>
                </tr>

                <c:forEach var="eventCRFAudit" items="${eventCRFAudits}">
                    <c:if test="${eventCRFAudit.eventCRFId==eventCRF.id}">
                        <tr>
                            <td class="table_header_column"><fmt:message key="${eventCRFAudit.auditEventTypeName}" bundle="${resaudit}"/>&nbsp;</td>
                            <td class="table_header_column">
								<cc-fmt:formatDate value="${eventCRFAudit.auditDate}" pattern="${dtetmeFormat}" dateTimeZone="${userBean.userTimeZoneId}"/>
								&nbsp;
							</td>
                            <td class="table_header_column"><c:out value="${eventCRFAudit.userName}"/>&nbsp;</td>
                            <td class="table_header_column">
								<c:choose>
									<c:when test="${eventCRFAudit.itemId != 0}">
										<a href="javascript: openDocWindow('ViewItemDetail?itemId=${eventCRFAudit.itemId}')" title="<c:out value="${eventCRFAudit.itemDescription}"/>">
											<c:out value="${eventCRFAudit.entityName}"/> (<c:out value="${eventCRFAudit.ordinal}"/>)
										</a>
									</c:when>
									<c:otherwise>
											<c:out value="${eventCRFAudit.entityName}"/> (<c:out value="${eventCRFAudit.ordinal}"/>)
									</c:otherwise>
								</c:choose>
							</td>
                            <td class="table_header_column">
                                <c:choose>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 12 or eventCRFAudit.entityName eq "Status"}'>
                                        <c:if test="${eventCRFAudit.oldValue eq '0'}"><fmt:message key="invalid" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '1'}"><fmt:message key="available" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '2'}"><fmt:message key="unavailable" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '3'}"><fmt:message key="private" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '4'}"><fmt:message key="pending" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '5'}"><fmt:message key="removed" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '6'}"><fmt:message key="locked" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.oldValue eq '7'}"><fmt:message key="auto-removed" bundle="${resterm}"/></c:if>
                                    </c:when>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 32}' >
                                    	<c:choose>
                                    	<c:when test="${eventCRFAudit.oldValue eq '1'}">TRUE</c:when>
                                    	<c:when test="${eventCRFAudit.oldValue eq '0'}">FALSE</c:when>
                                    	<c:otherwise><c:out value="${eventCRFAudit.oldValue}"/></c:otherwise>
                                    	</c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${eventCRFAudit.itemDataTypeId == 11}">
                                                <c:set var="path" value="${eventCRFAudit.oldValue}"/>
                                                <c:set var="sep" value="\\"/>
                                                <c:set var="sep2" value="\\\\"/>
                                                <a href="DownloadAttachedFile?eventCRFId=<c:out value="${eventCRFAudit.eventCRFId}"/>&fileName=${fn:replace(fn:replace(path,'+','%2B'),sep,sep2)}"><c:out value="${eventCRFAudit.oldValue}"/></a>
                                            </c:when>
                                            <c:otherwise><c:out value="${eventCRFAudit.oldValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                                &nbsp;</td>
                            <td class="table_header_column">
                                <c:choose>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 12 or eventCRFAudit.entityName eq "Status"}'>
                                        <c:if test="${eventCRFAudit.newValue eq '0'}"><fmt:message key="invalid" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '1'}"><fmt:message key="available" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '2'}"><fmt:message key="completed" bundle="${resword}"/></c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '3'}"><fmt:message key="private" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '4'}"><fmt:message key="pending" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '5'}"><fmt:message key="removed" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '6'}"><fmt:message key="locked" bundle="${resterm}"/></c:if>
                                        <c:if test="${eventCRFAudit.newValue eq '7'}"><fmt:message key="auto-removed" bundle="${resterm}"/></c:if>
                                    </c:when>
                                    <c:when test='${eventCRFAudit.auditEventTypeId == 32}' >
                                    	<c:choose>
                                    	<c:when test="${eventCRFAudit.newValue eq '1'}">TRUE</c:when>
                                    	<c:when test="${eventCRFAudit.newValue eq '0'}">FALSE</c:when>
                                    	<c:otherwise><c:out value="${eventCRFAudit.newValue}"/></c:otherwise>
                                    	</c:choose>
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${eventCRFAudit.itemDataTypeId == 11}">
                                                <c:set var="path" value="${eventCRFAudit.newValue}"/>
                                                <c:set var="sep" value="\\"/>
                                                <c:set var="sep2" value="\\\\"/>
                                                <a href="DownloadAttachedFile?eventCRFId=<c:out value="${eventCRFAudit.eventCRFId}"/>&fileName=${fn:replace(fn:replace(path,'+','%2B'),sep,sep2)}"><c:out value="${eventCRFAudit.newValue}"/></a>
                                            </c:when>
                                            <c:otherwise><c:out value="${eventCRFAudit.newValue}"/></c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                                &nbsp;</td>
                        </tr>
                    </c:if>
                </c:forEach>
            </table><%-- Event CRFs Audit Events --%>
        </td></tr></table><%-- Margin --%>
    </td>
    </tr>
    <!-- Return to Root -->
    <tr><td colspan="2" class="table_header_column_top" style="color: #789EC5"><a href="#root"><fmt:message key="return_to_top" bundle="${resword}"/></a>&nbsp;</td></tr>
</c:forEach>
</table>
<input id="CloaseViewStudySubjectAuditWindow" class="button_medium" type="submit" onclick="javascript:window.close()" value="<fmt:message key="close_window" bundle="${resword}"/>" name="BTN_Close_Window"/>
<br>
</c:forEach>
<jsp:include page="../include/changeTheme.jsp"/>
<hr>
</body>