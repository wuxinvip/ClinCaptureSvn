<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<jsp:include page="../include/managestudy-header.jsp"/>
<script type="text/javascript" language="javascript">
    jQuery(window).load(function(){

    	highlightLastAccessedObject();
    });
    
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
<jsp:useBean scope='request' id='definition' class='org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean'/>
<jsp:useBean scope='request' id='eventDefinitionCRFs' class='java.util.ArrayList'/>
<jsp:useBean scope='request' id='defSize' type='java.lang.Integer'/>

<h1>
	<span class="first_level_header">
		<fmt:message key="view_event_definition" bundle="${resword}"/> 
	</span>
</h1>

<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">   
  <tr valign="top"><td class="table_header_column"><fmt:message key="name" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.name}"/>
   </td></tr>
    <tr valign="top"><td class="table_header_column"><fmt:message key="oid" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.oid}"/>
   </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="description" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.description}"/>&nbsp;
  </td></tr>
 
 <tr valign="top"><td class="table_header_column"><fmt:message key="repeating" bundle="${resword}"/>:</td><td class="table_cell">
  <c:choose>
   <c:when test="${definition.repeating == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
   <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
  </c:choose>
  </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="type" bundle="${resword}"/>:</td><td class="table_cell">
    <c:out value="${definition.type}"/>
   </td></tr>
  
  <tr valign="top"><td class="table_header_column"><fmt:message key="category" bundle="${resword}"/>:</td><td class="table_cell">  
  <c:out value="${definition.category}"/>&nbsp;
  </td></tr>
  </table>
  </div>
</div></div></div></div></div></div></div></div>

</div>
<br>
<c:if test="${!empty eventDefinitionCRFs}">
<div class="table_title_manage">
  <fmt:message key="CRFs" bundle="${resword}"/>
</div>
<%--<p><fmt:message key="click_the_up_down_arrow_icons" bundle="${restext}"/></p>--%>
<div style="min-width: 700px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">


<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%"> 
 <tr valign="top"> 
    <td class="table_header_row"><fmt:message key="name" bundle="${resword}"/></td>   
    <td valign="top" class="table_header_row"><fmt:message key="required" bundle="${resword}"/></td>     
    <td valign="top" class="table_header_row"><fmt:message key="double_data_entry" bundle="${resword}"/></td>         
    <td valign="top" class="table_header_row"><fmt:message key="password_required" bundle="${resword}"/></td>
    <!-- <td valign="top" class="table_header_row"><fmt:message key="enforce_decision_conditions" bundle="${restext}"/></td>-->
    <td valign="top" class="table_header_row"><fmt:message key="default_version" bundle="${resword}"/></td>
     <td valign="top" class="table_header_row"><fmt:message key="hidden_crf" bundle="${resword}"/></td>
     <td valign="top" class="table_header_row"><fmt:message key="sdv_option" bundle="${resword}"/></td>
    <td valign="top" class="table_header_row"><fmt:message key="status" bundle="${resword}"/></td>
    <td valign="top" class="table_header_row"><fmt:message key="actions" bundle="${resword}"/></td>
  </tr>             
 
  <c:set var="prevCrf" value=""/>
  <c:set var="count" value="0"/>
  <c:set var="last" value="${defSize-1}"/>
 <c:forEach var ="crf" items="${eventDefinitionCRFs}" varStatus="status">
  <c:choose>
    <c:when test="${count == last}">
      <c:set var="nextCrf" value="${eventDefinitionCRFs[count]}"/>
    </c:when>  
    <c:otherwise> 
     <c:set var="nextCrf" value="${eventDefinitionCRFs[count+1]}"/>
    </c:otherwise>
  </c:choose>
   <tr valign="top">
    <td class="table_cell"><c:out value="${crf.crfName}"/></td> 
    <td class="table_cell">
    <c:choose>
    <c:when test="${crf.requiredCRF == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
     <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
    </c:choose>
   </td>
     
    <td class="table_cell">
     <c:choose>
      <c:when test="${crf.doubleEntry == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>         

    <td class="table_cell">
     <c:choose>
      <c:when test="${crf.electronicSignature == true}"> <fmt:message key="yes" bundle="${resword}"/> </c:when>
      <c:otherwise> <fmt:message key="no" bundle="${resword}"/> </c:otherwise>
     </c:choose>
    </td>

   <td class="table_cell">   
    <c:out value="${crf.defaultVersionName}"/>     
   </td>
   <td class="table_cell">
    <c:out value="${crf.hideCrf}"/>
   </td>      
  <td class="table_cell"><fmt:message key="${crf.sourceDataVerification.description}" bundle="${resterm}"/></td> 
  <c:choose>
		<c:when test="${crf.status.available}">
			<td class="table_cell aka_green_highlight"><c:out value="${crf.status.name}"/></td>	
		</c:when>
		<c:when test="${crf.status.deleted || crf.status.locked}">
			<td class="table_cell aka_red_highlight"><c:out value="${crf.status.name}"/></td>	
		</c:when>
		<c:otherwise>
			<td class="table_cell"><c:out value="${crf.status.name}"/></td>	
		</c:otherwise>
	</c:choose>
   <td class="table_cell">
     <table border="0" cellpadding="0" cellspacing="0">
	  <tr class="innerTable">
        <td>
            <a href="ViewCRF?crfId=<c:out value="${crf.crfId}"/>"
                 onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
                 onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"
				data-cc-eventDefinitionReadonlyCrfId="${crf.crfId}"
				onclick="setAccessedObjected(this);"><img
                name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
        </td>
	  </tr>
	 </table>
   </td>
   </tr>
   <c:set var="prevCrf" value="${crf}"/>
   <c:set var="count" value="${count+1}"/>
 </c:forEach>
 
</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
</c:if>
<p>
	<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium medium_back"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />
	
</p>
 
 <c:import url="../include/workflow.jsp">
   <c:param name="module" value="manage"/> 
 </c:import>
 
<input id="accessAttributeName" type="hidden" value="data-cc-eventDefinitionReadonlyCrfId">
<jsp:include page="../include/footer.jsp"/>
