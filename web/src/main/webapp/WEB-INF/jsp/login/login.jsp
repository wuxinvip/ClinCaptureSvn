<%@page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="/WEB-INF/jsp/taglibs.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <link rel="icon" href="<c:url value='/images/favicon.ico'/>" />
    <link rel="shortcut icon" href="<c:url value='/images/favicon.ico'/>" />

    <title>ClinCapture</title>

    <meta http-equiv="Content-type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" href="<c:url value='/includes/styles.css'/>" type="text/css"/>
    <link rel="stylesheet" href="<c:url value='/includes/styles2.css'/>" type="text/css" />
    <link rel="stylesheet" href="<c:url value='/includes/NewLoginStyles.css'/>" type="text/css"/>
    <script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery-1.3.2.min.js'/>"></script>
    <script type="text/javascript" language="JavaScript" src="<c:url value='/includes/jmesa/jquery.blockUI.js'/>"></script>
    <script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/global_functions_javascript2.js'/>"></script>
    <script type="text/JavaScript" language="JavaScript" src="<c:url value='/includes/global_functions_javascript.js'/>"></script>

</head>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>


<body class="login_BG" onLoad="document.getElementById('username').focus();">
<div class="login_BG">
    <center>

        <!-- ClinCapture logo -->
        <%String ua = request.getHeader( "User-Agent" );
            String temp = "";
            String iev = "";
            if( ua != null && ua.indexOf( "MSIE" ) != -1 ) {
                temp = ua.substring(ua.indexOf( "MSIE" ),ua.length());
                iev = temp.substring(4, temp.indexOf(";"));
                iev = iev.trim();
            }
            if(iev.length() > 1 && Double.valueOf(iev)<7) {%>
        <div ID="OClogoIE6">&nbsp;</div>
        <%} else {%>
        <div ID="OClogo">&nbsp;</div>
        <%}%>
        <!-- end ClinCapture logo -->

        <span class='dbTitle'> <jsp:include page="../login-include/login-dbtitle.jsp"/> </span> <br><br><br>

        <table border="0" cellpadding="0" cellspacing="0" class="loginBoxes">
            <tr>
                <td class="loginBox_T">&nbsp;</td>
            </tr>
            <tr>
                <td class="loginBox" align="center">
                    <div ID="loginBox" align="center">
                        <!-- Login box contents -->
                        <div ID="login" align="left">
                            <form action="<c:url value='/j_spring_security_check'/>" method="post">
                                <h1><fmt:message key="login" bundle="${resword}"/></h1>
                                <b><fmt:message key="user_name" bundle="${resword}"/></b>
                                <div class="formfieldM_BG">
                                    <input type="text" id="username" name="j_username" class="formfieldM">
                                </div>

                                <b><fmt:message key="password" bundle="${resword}"/></b>
                                <div class="formfieldM_BG">
                                    <input type="password" id="j_password" name="j_password"  class="formfieldM">
                                </div>
                                <input type="submit" name="submit" value="<fmt:message key='login' bundle='${resword}'/>" class="loginbutton" />
                                <div style="display:inline; position:absolute;"><a style="" href="#" id="requestPassword"> <fmt:message key="forgot_password" bundle="${resword}"/></a></div>
                            </form>
                            <br/><jsp:include page="../login-include/login-alertbox.jsp"/>
                            <%-- <a href="<c:url value="/RequestPassword"/>"> <fmt:message key="forgot_password" bundle="${resword}"/></a> --%>
                        </div>
                        <!-- End Login box contents -->
                    </div>
                </td>
            </tr>
			<!-- Clinovo Ticket #178
            <script type="text/javascript">
                if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent)){
                    var ffversion=new Number(RegExp.$1)
                    if (!(ffversion>=3)){
                        document.write("<tr> <td align='justify' colspan=2 style='padding-left: 20px; text-indent: 25px;' >"+
                                " <fmt:message key="choose_browser" bundle="${restext}"/>"+
                                "</td> </tr>");
                    }
                } else if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){
                    var ieversion=new Number(RegExp.$1)
                    if (ieversion!=8 && ieversion!=7){
                        document.write("<tr> <td align='justify' colspan=2 style='padding-left: 20px;' >"+
                                "<div style='width: 310px; text-indent: 25px;' align='justify'> <fmt:message key="choose_browser" bundle="${restext}"/> </div>"+
                                "</td> </tr>");
                    }
                }else{
                    document.write("<tr> <td align='justify' colspan=2 style='padding-left: 20px; text-indent: 25px;' >"+
                            " <fmt:message key="choose_browser" bundle="${restext}"/>"+
                            "</td> </tr>");
                }
            </script>
			-->
        </table>

    </center>

    <script type="text/javascript">
        document.getElementById('username').setAttribute( 'autocomplete', 'off' );
        document.getElementById('j_password').setAttribute( 'autocomplete', 'off' );

        jQuery(document).ready(function() {

            /*$.get("../../RssReader", function(data){
                //alert("Data Loaded: " + data);
                $("#newsBox").html(data);
            });*/


            jQuery('#requestPassword').click(function() {
                jQuery.blockUI({ message: jQuery('#requestPasswordForm'), css:{left: "200px", top:"180px" } });
            });

            jQuery('#cancel').click(function() {
                jQuery.unblockUI();
                return false;
            });
        });

    </script>

    <div id="requestPasswordForm" style="display:none;">
        <c:import url="requestPasswordPop.jsp">
        </c:import>
    </div>

    <!-- Footer -->
    <!-- End Main Content Area -->
<jsp:include page="../login-include/login-footer.jsp"/>