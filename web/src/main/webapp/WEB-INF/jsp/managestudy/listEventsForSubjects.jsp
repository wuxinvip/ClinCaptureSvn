<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>


<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>


<jsp:include page="../include/submit-header.jsp"/>


<!-- *JSP* ${pageContext.page['class'].simpleName} -->
<jsp:include page="../include/sideAlert.jsp"/>

<link rel="stylesheet" href="includes/jmesa/jmesa.css?r=${revisionNumber}" type="text/css">
<!--script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery-1.3.2.min.js"></script-->
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js?r=${revisionNumber}"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js?r=${revisionNumber}"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js?r=${revisionNumber}"></script>

<script type="text/javascript">
    function onInvokeAction(id,action) {
        if(id.indexOf('listEventsForSubject') == -1)  {
        setExportToLimit(id, '');
        }
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        location.href = '${pageContext.request.contextPath}/ListEventsForSubjects? + module=manage&defId=' + '${defId}&' + parameterString;
    }
    
    jQuery(window).load(function(){

    	highlightLastAccessedObject();
    });

</script>

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
		<fmt:message key="view_subjects_in" bundle="${restext}"/> <c:out value="${study.name}"/>
	</span>
</h1>

<div id="popupShadowWrapper"><div class="box_T_a"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="popupShadow"></div></div></div></div></div></div></div></div></div></div>

<div id="findSubjectsDiv">
    <form  action="${pageContext.request.contextPath}/ListEventsForSubjects">
        <input type="hidden" name="module" value="submit">
        <input type="hidden" name="defId" value="${defId}">
        ${listEventsForSubjectsHtml}
    </form>
</div>


<br>

<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage" value="<fmt:message key="back" bundle="${resword}"/>" class="button_medium medium_back" onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');"/>

<c:import url="../include/workflow.jsp">
   <c:param name="module" value="submit"/>
</c:import>
<input id="accessAttributeName" type="hidden" value="data-cc-subjectMatrixId">
<jsp:include page="../include/footer.jsp"/>
