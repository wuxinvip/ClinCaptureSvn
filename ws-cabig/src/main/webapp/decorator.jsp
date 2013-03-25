<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator" %>
<%@ taglib uri="com.akazaresearch.viewtags" prefix="view" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html lang="en">
<head>
    <meta http-equiv="content-type" content="text/html; charset=utf-8">
    <meta name="gwt:property" content="locale=${pageContext.request.locale}">
    <title><decorator:title default="OpenClinica" /></title>
    <script type="text/javascript" language="javascript" src="../gwt/GwtMenu/org.akaza.openclinica.gwt.GwtMenu.nocache.js"></script>
    <script type="text/javascript" language="javascript" src="../includes/prototype.js"></script>
    <script type="text/javascript" language="javascript" src="../includes/global_functions_javascript.js"></script>
    <script type="text/javascript" language="javascript" src="../includes/Tabs.js"></script>
    <!-- Added for the new Calender -->

    <link rel="stylesheet" type="text/css" media="all" href="../includes/new_cal/skins/aqua/theme.css" title="Aqua" />
    <script type="text/javascript" src="../includes/new_cal/calendar.js"></script>
    <script type="text/javascript" src="../includes/new_cal/lang/<fmt:message key="jscalendar_language_file" bundle="${resformat}"/>"></script>
    <script type="text/javascript" src="../includes/new_cal/calendar-setup.js"></script>
    <!-- End -->
    <link rel="stylesheet" href="../includes/styles_updated.css" type="text/css">
    <link rel="stylesheet" href="../includes/proto_styles.css" type="text/css">
    <link rel="stylesheet" href="../gwt/GwtMenu/GwtMenu.css" type="text/css">
    <decorator:head />
</head>
<body>
<div id="headerDiv">
    <div id="logoDiv"><img src="../images/Logo.gif" alt="Akaza logo"/></div>
    <!-- the sub-menu, or alternative menu, displays if JavaScript is disabled-->
    <div id="menuContainer">
        <noscript>
            <span class="noscript">
                <a href="MainMenu">Home</a> | <a href="ListStudySubjectsSubmit">Submit Data</a> | <a href="ExtractDatasetsMain">Extract Data</a> | <a href="ManageStudy">ManageStudy</a> | <a href="AdminSystem">Business Admin</a>
            </span>
        </noscript>

    </div>

    <div id="reportIssueDiv">
        <a href="javascript:reportBug()"> <span class="originalFont"><fmt:message key="openclinica_report_issue" bundle="${resword}"/></span> </a> | <a href="javascript:openDocWindow('http://www.openclinica.org/OpenClinica/2.2/support/')"><span class="originalFont"><fmt:message key="openclinica_feedback" bundle="${resword}"/></span></a>
    </div>
    <div id="userBoxDiv" class="userbox">
        <view:userbox />
    </div>
</div>
<%-- this element must be designed to optionally include/exclude its internal DIVs --%>
<view:sidebar />

<div id="bodyDiv">
    <decorator:body />
    <div id="workflowDiv">
        <view:workflow />
    </div>
</div>

<div id="footerDiv">
    <view:footer />
</div>
</body>
</html>
