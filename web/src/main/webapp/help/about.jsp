<%@ page contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<jsp:useBean id="date" class="java.util.Date" />
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.buildNumber" var="resbuildnumber"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en">

<head>
<link rel="icon" href="<c:url value='${faviconUrl}'/>" />
<link rel="shortcut icon" href="<c:url value='${faviconUrl}'/>" />
<meta http-equiv="content-type" content="text/html; charset=iso-8859-1" />

<title>ClinCapture About</title>
<link rel="stylesheet" href="../includes/styles_hf.css?r=${revisionNumber}" type="text/css" media="screen" />
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="colorTheme.js?r=${revisionNumber}"></script>
</head>

<body>

<!--main container-->
<div id="main">

<!--header container-->
<div id="header">
	
</div>
<!--end header-->

<!--content container, on the right side-->
<div id="about-content">
		<!-- clinovo icon should be here -->
		<img src="${'..'}${systemLogoUrl}" alt="Clinovo Inc."/>
		<P>ClinCapture&#0153; <fmt:message key="Version_release" bundle="${resword}"/>&nbsp;&nbsp;<fmt:message key="buildNumber" bundle="${resbuildnumber}"/></P>
		<P>Licensed under LGPLv2.1.  The program is free software; you can redistribute it and modify it under the terms of the GNU Lesser General Public License version 2.1 as published by the Free Software Foundation.</P>
		<p> &copy; 2004-2012 OpenClinica, LLC and collaborators. The OpenClinica			<a href="http://www.openclinica.com" target="_blank">software for clinical research</a>
			is provided AS IS, without warranty. Licensed under LGPLv2.1, you can redistribute it and/or modify it under the terms of the
			<a href="http://www.openclinica.com/gnu-lgpl-open-source-license" target="_blank">GNU Lesser General Public License</a>
			version 2.1 as published by the Free Software Foundation. OpenClinica is a
			<a href="http://www.openclinica.com/trademark" target="_blank">trademark</a>
			of OpenClinica, LLC.</p>
		<p>ClinCapture is made possible by the OpenClinica open source project and other open source software.</p>
		<p>ClinCapture&#0153; is a Trademark of ClinCapture Inc. <a href="http://www.clincapture.com/" target="_blank">www.clincapture.com</a></p>
		<p>Copyright 2010-<fmt:formatDate value="${date}" pattern="yyyy"/> ClinCapture Inc. All rights reserved.</p>
		<br /><input class="button_medium" type="submit" id="CloaseWindow" name="BTN_Close" value="Close Window" onclick="javascript:window.close()"/> 
</div>
<!--end content-->

<!--container for footer-->
<div id="footer">
	
</div>
<!--end footer-->

</div>
<!--end main container-->

</body>
</html>