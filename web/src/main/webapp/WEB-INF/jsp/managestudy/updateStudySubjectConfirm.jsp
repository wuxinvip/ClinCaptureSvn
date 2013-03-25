<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<c:choose>
<c:when test="${userBean.sysAdmin || userBean.techAdmin || userRole.manageStudy}">
	<jsp:include page="../include/managestudy-header.jsp"/>
</c:when>
<c:otherwise>
	<jsp:include page="../include/home-header.jsp"/>
</c:otherwise>
</c:choose>
<%-- <jsp:include page="../include/managestudy-header.jsp"/> --%>


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

<jsp:useBean scope="session" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>

<h1><span class="title_manage">
<fmt:message key="confirm_study_subject_updates" bundle="${resword}"/>
</span></h1>

<form action="UpdateStudySubject" method="post">
<input type="hidden" name="action" value="submit">
<input type="hidden" name="id" value="<c:out value="${studySub.id}"/>">
 <div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="tablebox_center">
<table border="0" cellpadding="0" cellspacing="0" width="100%">

  <fmt:message key="study_subject_ID" bundle="${resword}" var="studySubjectLabel"/>
  <c:if test="${study ne null}">
      <c:set var="studySubjectLabel" value="${study.studyParameterConfig.studySubjectIdLabel}"/>
  </c:if>
  <tr valign="bottom">
      <td class="table_header_column">${studySubjectLabel}:</td><td class="table_cell"><c:out value="${studySub.label}"/></td>
  </tr>

  <c:set var="secondaryIdShow" value="${true}"/>
  <fmt:message key="secondary_ID" bundle="${resword}" var="secondaryIdLabel"/>
  <c:if test="${study ne null}">
      <c:set var="secondaryIdShow" value="${!(study.studyParameterConfig.secondaryIdRequired == 'not_used')}"/>
      <c:set var="secondaryIdLabel" value="${study.studyParameterConfig.secondaryIdLabel}"/>
  </c:if>
  <c:if test="${secondaryIdShow}">
      <tr valign="bottom">
          <td class="table_header_column">${secondaryIdLabel}:</td><td class="table_cell"><c:out value="${studySub.secondaryLabel}"/>&nbsp;</td>
      </tr>
  </c:if>

  <c:set var="enrollmentDateShow" value="${true}"/>
  <fmt:message key="enrollment_date" bundle="${resword}" var="enrollmentDateLabel"/>
  <c:if test="${study ne null}">
      <c:set var="enrollmentDateShow" value="${!(study.studyParameterConfig.dateOfEnrollmentForStudyRequired == 'not_used')}"/>
      <c:set var="enrollmentDateLabel" value="${study.studyParameterConfig.dateOfEnrollmentForStudyLabel}"/>
  </c:if>
  <c:if test="${enrollmentDateShow}">
      <tr valign="bottom">
          <td class="table_header_column">${enrollmentDateLabel}:</td><td class="table_cell"><fmt:formatDate value="${studySub.enrollmentDate}" pattern="${dteFormat}"/></td>
      </tr>
  </c:if>

  <tr valign="top"><td class="table_header_column"><fmt:message key="created_by" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${studySub.owner.name}"/></td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_created" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${studySub.createdDate}" pattern="${dteFormat}"/>
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="last_updated_by" bundle="${resword}"/>:</td><td class="table_cell"><c:out value="${studySub.updater.name}"/>&nbsp;
  </td></tr>
  <tr valign="top"><td class="table_header_column"><fmt:message key="date_updated" bundle="${resword}"/>:</td><td class="table_cell"><fmt:formatDate value="${studySub.updatedDate}" pattern="${dteFormat}"/>&nbsp;
  </td></tr>

</table>
</div>
</div></div></div></div></div></div></div></div>

</div>
<c:if test="${(!empty groups)}">
<br>
<div style="width: 600px">
<div class="box_T"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR">

<div class="textbox_center">
<table border="0" cellpadding="0">

  <tr valign="top">
	<td class="table_header_column"><fmt:message key="subject_group_class" bundle="${resword}"/>:
	<td class="table_cell">

	<table border="0" cellpadding="0">
	  <c:forEach var="group" items="${groups}">
	  <tr valign="top">
	   <td><b><c:out value="${group.name}"/></b></td>
	   <td><c:out value="${group.studyGroupName}"/></td></tr>
	    <tr valign="top">
	      <td><fmt:message key="notes" bundle="${resword}"/>:</td>
	      <td><c:out value="${group.groupNotes}"/></td>
	    </tr>
	  </c:forEach>
	  </table>
	</td>
  </tr>



</table>
</div>

</div></div></div></div></div></div></div></div>

</div>
</c:if>
<br>
  <%-- <input id="GoBackToSubjectList" class="button_medium" type="button" name="BTN_Back" title="<fmt:message key="go_back_to_subject_matrix" bundle="${resword}"/>" value="<fmt:message key="back" bundle="${resword}"/>" onclick="window.location.href=('ListStudySubjects');"/>--%>
  <input type="button" name="BTN_Back" id="GoToPreviousPage" value="<fmt:message key="back" bundle="${resword}"/>" class="button_medium" onClick="javascript: history.go(-1);"/>
 
  <input type="submit" name="Submit" value="<fmt:message key="submit" bundle="${resword}"/>" class="button_medium">

  <input type="button" name="BTN_Cancel" id="Cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" class="button_medium" onClick="javascript: history.go(-2);"/>  
  
  <%-- <input id="Cancel" class="button_medium" type="button" name="BTN_Cancel" title="<fmt:message key="go_back_to_update_study_subject_details" bundle="${resword}"/>" value="<fmt:message key="cancel" bundle="${resword}"/>" onclick="window.location.href=('ViewStudySubject?id=${studySub.id}');"/>--%>
<br>
</form>
<jsp:include page="../include/footer.jsp"/>
