<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="respage"/>

<c:choose>
<c:when test="${userBean.sysAdmin}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/managestudy-header.jsp"/>
</c:otherwise>
</c:choose>

<!-- *JSP* ${pageContext.page['class'].simpleName} -->
<jsp:include page="../include/sideAlert.jsp"/>
<!-- then instructions-->

<tr id="sidebar_Instructions_open" style="display: all">

		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open');
		leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		<div class="sidebar_tab_content">
		  <b><fmt:message key="create_CRF" bundle="${resword}"/> : </b>
		  <fmt:message key="br_create_new_CRF_entering" bundle="${respage}"/><br/><br/>
		  <b><fmt:message key="create_CRF_version" bundle="${resword}"/> : </b>
		  <fmt:message key="br_create_new_CRF_uploading" bundle="${respage}"/><br/><br/>
		  <b><fmt:message key="revise_CRF_version" bundle="${resword}"/> : </b>
		  <fmt:message key="br_if_you_owner_CRF_version" bundle="${respage}"/><br/><br/>
		  <b><fmt:message key="CRF_spreadsheet_template" bundle="${resword}"/> : </b>
		  <fmt:message key="br_download_blank_CRF_spreadsheet_from" bundle="${respage}"/><br/><br/>
		  <b><fmt:message key="example_CRF_br_spreadsheets" bundle="${resword}"/> : </b>
          <fmt:message key="br_download_example_CRF_instructions_from" bundle="${respage}"/><br/>
		  
		
		</div>

		</td>

	</tr>
	<tr id="sidebar_Instructions_closed" style="display: none">
		<td class="sidebar_tab">

		<a href="javascript:leftnavExpand('sidebar_Instructions_open');
		leftnavExpand('sidebar_Instructions_closed');">
            <img src="images/sidebar_expand.gif" border="0" align="right" hspace="10"></a>

		<b><fmt:message key="instructions" bundle="${resword}"/></b>

		</td>
  </tr>
<jsp:include page="../include/sideInfo.jsp"/>


<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>

<h1>
	<span class="first_level_header">
		 <c:choose>
		     <c:when test="${empty crfName || empty param.crfId}">
		         <fmt:message key="create_a_new_CRF_case_report_form" bundle="${resworkflow}"/>
		     </c:when>
		     <c:otherwise>
		        <fmt:message key="create_CRF_version" bundle="${resworkflow}"/> <c:out value="${crfName}"/>
		     </c:otherwise>
		 </c:choose>	
	</span>
</h1>

<script type="text/JavaScript" language="JavaScript">
 
 function myCancel() {

    cancelButton=document.getElementById('cancel');
    if ( cancelButton != null) {
    	confirmDialog({ message: '<fmt:message key="sure_to_cancel" bundle="${resword}"/>', height: 150, width: 500, redirectLink: 'ListCRF' });
      	return false;
     }
     return true;

  }

function submitform(){
    var crfUpload = document.getElementById('excel_file_path');
    //Does the user browse or select a file or not
    if (crfUpload.value =='' )
    {
    	alertDialog({ message: "Select a file to upload!", height: 150, width: 500 });
        return false;
    }
    return true;
}

</script>

<p><fmt:message key="can_download_blank_CRF_excel" bundle="${restext}"/><a href="DownloadVersionSpreadSheet?template=1"><b><fmt:message key="here" bundle="${resword}"/></b></a>.</p>

<div style="font-family: Tahoma, Arial, Helvetica, Sans-Serif;font-size:12px;">
    <p><fmt:message key="openclinica_excel_support" bundle="${restext}"/></p>

 </div>

<%--
<p><fmt:message key="also_download_set_example_CRFs" bundle="${restext}"/><a href="http://www.openclinica.org/entities/entity_details.php?eid=151" target="_blank"><fmt:message key="here" bundle="${resword}"/></a>.</p>
--%>



<form action="CreateCRFVersion?action=confirm&crfId=<c:out value="${version.crfId}"/>&name=<c:out value="${version.name}"/>" method="post" ENCTYPE="multipart/form-data">
<div style="width: 450px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center fileInputDivWrapper">
<table border="0" cellpadding="0" cellspacing="0" class="fileInputTableWrapper">

<tr>
<td class="formlabel"><div class="excelFileUploadWrapper"><fmt:message key="ms_excel_file_to_upload" bundle="${resword}"/>:</div></td>
<td><input type="file" name="excel_file" id="excel_file_path" onchange="javascript:changeIcon()">
<br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="excel_file"/></jsp:include></td>
</tr>
<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">


</table>

</div>

</div></div></div></div></div></div></div></div>
</div>

<br clear="all">
<table border="0" cellpadding="0" cellspacing="0">
<tr>
<td>
  <input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium medium_back"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />
</td>
<td>
  <input type="submit" onclick="return checkFileUpload('excel_file_path', '<fmt:message key="select_a_file_to_upload" bundle="${restext}"/>');" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium medium_continue">
</td>
<td>
  <img src="images/icon_UnchangedData.gif" style="visibility:hidden" title="You have not changed any data in this CRF section." alt="Data Status" name="DataStatus_bottom">
</td>
</tr></table>
</form>

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