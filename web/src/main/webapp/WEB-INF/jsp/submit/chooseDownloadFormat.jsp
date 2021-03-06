<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<html>
<head><title>Choose a format for downloading notes</title></head>
<link rel="icon" href="<c:url value='${faviconUrl}'/>" />
<link rel="shortcut icon" href="<c:url value='${faviconUrl}'/>" />
<link rel="stylesheet" href="includes/styles.css?r=${revisionNumber}" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-1.3.2.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js?r=${revisionNumber}"></script><%--<c:set var="subjectId" value="${requestScope[subjectId]}" />

/discrepancyNoteReport.csv--%>
<body style="margin: 5px">
<h2><fmt:message key="choose_download_format" bundle="${resword}"/></h2>
<!-- *JSP* submit/chooseDownloadFormat.jsp -->
<div id="downloadDiv">
    <form action="DiscrepancyNoteOutputServlet" name="downloadForm">
        <fmt:message key="format" bundle="${resword}"/>: <select id="fmt" name="fmt">
           <option value="csv">comma separated values</option>
           <option value="pdf">portable document format</option>
        </select><br /><br />

        <input type="hidden" name="list" value="y"/>
        <input type="hidden" name="subjectId" value="${subjectId}"/>
        <input type="hidden" name="fileName" value="dnotes${subjectId}_${studyIdentifier}"/>
        <input type="hidden" name="studyIdentifier" value="${studyIdentifier}"/>        
        <input type="hidden" name="eventId" value="${param.eventId}"/>
        <input type="hidden" name="resolutionStatus" value="${param.resolutionStatus}"/>
        <input type="hidden" name="discNoteType" value="${param.discNoteType}"/>

        <%--
            ClinCapture #71
            field to save filters
        --%>
        <input type="hidden" name="filters" value="${param}" />

        <input type="submit" name="submitFormat" value="<fmt:message key="download_notes" bundle="${resword}"/>" class=
                                  "button_medium" />

        <br />
         <input type="button" name="clsWin" value="<fmt:message key="close_window" bundle="${resword}"/>" class=
                                  "button_medium" onclick="window.close()"/>
    </form>


</div>

</body>
</html>