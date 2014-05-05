<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="respage"/>


<c:choose>
    <c:when test="${userBean.sysAdmin}">
        <c:import url="../include/admin-header.jsp"/>
    </c:when>
    <c:otherwise>
        <c:import url="../include/managestudy-header.jsp"/>
    </c:otherwise>
</c:choose>

<link rel="stylesheet" href="includes/jmesa/jmesa.css" type="text/css">
<!--script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-1.3.2.min.js"></script-->
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js"></script>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('studies') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ViewCRF?crfId=' + '${crf.id}&' + parameterString;
    }
</script>

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
<jsp:useBean scope='request' id='crf' class='org.akaza.openclinica.bean.admin.CRFBean'/>

<h1>
	<span class="first_level_header">
		<fmt:message key="view_CRF_details" bundle="${resword}"/>
	</span>
</h1>
<div style="width: 600px">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top"><td class="table_header_column_top"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${crf.name}"/>
                </td></tr>
                <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${crf.description}"/>
                </td></tr>
                <tr valign="top"><td class="table_header_column"><fmt:message key="OID" bundle="${resword}"/>:</td><td class="table_cell">
                    <c:out value="${crf.oid}"/>
                </td></tr>
            </table>
        </div>
    </div></div></div></div></div></div></div></div>

</div>
<br>
<c:choose>
    <c:when test="${userBean.sysAdmin}">
        <span class="table_title_Admin">
    </c:when>
    <c:otherwise>
        <span class="table_title_Manage">
    </c:otherwise>
</c:choose>
<fmt:message key="versions" bundle="${resword}"/></span>
<div style="width: 100%">
    <div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

        <div class="tablebox_center">
            <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr valign="top">
                    <td class="table_header_row_left"><fmt:message key="version_name" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="oid" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="description" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="revision_notes" bundle="${resword}"/></td>
                    <td class="table_header_row"><fmt:message key="action" bundle="${resword}"/></td>
                </tr>
                <c:forEach var ="version" items="${crf.versions}">
                <tr valign="top">
                    <td class="table_cell_left"><c:out value="${version.name}"/></td>
                    <td class="table_cell"><c:out value="${version.oid}"/></td>
                    <td class="table_cell"><c:out value="${version.description}"/></td>
                    <td class="table_cell"><c:out value="${version.status.name}"/></td>
                    <td class="table_cell"><c:out value="${version.revisionNotes}"/></td>
                    <td class="table_cell">
                        <table border="0" cellpadding="0" cellspacing="0">
                            <tr>
                                <td>
                                    <a href="ViewSectionDataEntry?crfId=<c:out value="${crf.id}"/>&crfVersionId=<c:out value="${version.id}"/>&tabId=1"
                                       onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                                       onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
                                            name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
                                </td>
                                <td>
                                    <a href="javascript:openDocWindow('PrintCRF?id=<c:out value="${version.id}"/>')"
                                       onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');"
                                       onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><img
                                            name="bt_Print1" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>" title="<fmt:message key="print" bundle="${resword}"/>" align="left" hspace="6"></a>

                                </td>
                                <td>
                                    <a href="ViewCRFVersion?id=<c:out value="${version.id}"/>"><img
                                            name="bt_Metadata" src="images/bt_Metadata.gif" border="0" alt="Metadata" title="Metadata" align="left" hspace="6"></a>

                                </td>
                            </tr>
                        </table>
                        </c:forEach>

            </table>
        </div>
    </div></div></div></div></div></div></div></div>

</div>
<br/>
<span class="table_title_Admin"><fmt:message key="studies_using_crf" bundle="${resword}"/></span>

<div id="studiesDiv">
    <form  action="${pageContext.request.contextPath}/ViewCRF">
        <input type="hidden" name="crfId" value="${crf.id}">
        ${studiesTableHTML}
    </form>
</div>
 <br/>

<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />
<input type="button" name="<fmt:message key="view_crf_rules" bundle="${resword}"/>" value="<fmt:message key="view_crf_rules" bundle="${resword}"/>" class="button_long" onClick="window.location.href='ViewRuleAssignment?ruleAssignments_f_crfName=<c:out value="${crfName}"/>'"/>
<input type="button" name="<fmt:message key="run_crf_rules" bundle="${resword}"/>" value="<fmt:message key="run_crf_rules" bundle="${resword}"/>" class="button_long" onClick="window.location.href='RunRule?crfId=<c:out value="${crf.id}"/>&action=dryRun'"/>
<c:if test="${userRole.role.id eq 1}">
    <input type="button" name="<fmt:message key="delete" bundle="${resword}"/>" value="<fmt:message key="delete" bundle="${resword}"/>" class="button_medium_red" onClick="return confirmDialog({ message: '<fmt:message key="are_you_sure_want_to_delete" bundle="${respage}"/>', height: 165, width: 500, redirectLink: 'CompleteCrfDelete?crfId=<c:out value="${crf.id}"/>'});"/>
</c:if>

<jsp:include page="../include/footer.jsp"/>