<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='study' class='org.akaza.openclinica.bean.managestudy.StudyBean'/>
<jsp:useBean scope='session' id='userRole' class='org.akaza.openclinica.bean.login.StudyUserRoleBean'/>

<jsp:include page="include/home-header.jsp"/>
<jsp:include page="include/sideAlert.jsp"/>


<link rel="stylesheet" href="includes/jmesa/jmesa.css?r=${revisionNumber}" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jquery.jmesa.js?r=${revisionNumber}"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/jmesa/jmesa.js?r=${revisionNumber}"></script>
<script type="text/javascript" language="JavaScript" src="includes/jmesa/jquery.blockUI.js?r=${revisionNumber}"></script>
<script type="text/JavaScript" language="JavaScript" src="includes/global_functions_javascript.js?r=${revisionNumber}"></script>
<style type="text/css">

  .graph {
    position: relative; /* IE is dumb */
    width: 100px;
    border: 1px solid #3876C1;
    padding: 2px;
  }

  .graph .bar {
    display: block;
    position: relative;
    background: #E8D28C;
    text-align: center;
    color: #333;
    height: 1em;
    line-height: 1em;
  }

  .graph .bar span {
    position: absolute;
    left: 1em;
  }
</style>

<script type="text/JavaScript" language="JavaScript">
	//alignment of headers and icons

	$(document).ready(function () {
		$("div[id^='Event_']").parent().parent().parent().parent().parent().attr("align", "center");
		$("tr.header").attr("align", "center");
		checkCookiesDialog();
	});
</script>

<!-- then instructions-->
<div id="box" class="dialog">
	<span id="mbm"><br>
		<fmt:message key="study_frozen_locked_note" bundle="${restext}"/>
	</span><br>
	<div style="text-align:center; width:100%;">
		<button onclick="hm('box');">OK</button>
	</div>
</div>
<tr id="sidebar_Instructions_open" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
		<div class="sidebar_tab_content">
			<fmt:message key="may_change_request_access" bundle="${restext}"/>
		</div>
	</td>
</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_expand.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${resword}"/></b>
	</td>
</tr>

<jsp:include page="include/sideInfo.jsp"/>


<h1> 
    <span class="first_level_header" style="line-height:5px;">
        <fmt:message key="welcome_to" bundle="${restext}"/>
            <c:choose>
              <c:when test='${study.parentStudyId > 0}'>
                <c:out value='${study.parentStudyName}'/>: <c:out value='${study.name}'/>
              </c:when>
              <c:otherwise>
                <c:out value='${study.name}'/>
              </c:otherwise>
            </c:choose>
    </span>
</h1>

<c:set var="roleName" value=""/>
<c:if test="${userRole != null && !userRole.invalid}">
  <c:set var="roleName" value="${userRole.role.name}"/>

  <c:set var="studyidentifier">
    <span class="alert"><c:out value="${study.identifier}"/></span>
  </c:set>

</c:if>
<span class="table_title_Admin" style="line-height:15px;">
	<a href="ViewNotes?module=submit&listNotes_f_discrepancyNoteBean.user=<c:out value='${userBean.name}' />&listNotes_f_discrepancyNoteBean.resolutionStatus=<fmt:message key="Not_Closed" bundle="${resterm}"/>">
	  <fmt:message key="unresolved_discrepancies_assigned_to_me" bundle="${restext}"/><span>${assignedDiscrepancies}</span>&nbsp;
	</a><br/><br/>
</span>


<!--Old home page layout-->
<c:if test="${displayPageVersion=='old'}">
	<span class="old_home_page">
	<c:if test="${userRole.investigator || userRole.clinicalResearchCoordinator}">
	
	<div id="findSubjectsDiv">
		<script type="text/javascript">
		function onInvokeAction(id, action) {
	        if (id.indexOf('findSubjects') == -1) {
	          setExportToLimit(id, '');
	        }
	        createHiddenInputFieldsForLimitAndSubmit(id);
		}
		function onInvokeExportAction(id) {
			var parameterString = createParameterStringForLimit(id);
			location.href = '${pageContext.request.contextPath}/MainMenu?' + parameterString;
		}
	    </script>
        <div id="popupShadowWrapper"><div class="box_T_a"><div class="box_L"><div class="box_R"><div class="box_B"><div class="box_TL"><div class="box_TR"><div class="box_BL"><div class="box_BR"><div class="popupShadow"></div></div></div></div></div></div></div></div></div></div>
	    <form action="${pageContext.request.contextPath}/ListStudySubjects">
	      <input type="hidden" name="module" value="admin">
	        ${findSubjectsHtml}
	    </form>
	  </div>
	</c:if>
	<c:if test="${userRole.sysAdmin || userRole.studyAdministrator || userRole.studyDirector}">	
	  <script type="text/javascript">
	    function onInvokeAction(id, action) {
	      if (id.indexOf('studySiteStatistics') == -1) {
	        setExportToLimit(id, '');
	      }
	      if (id.indexOf('subjectEventStatusStatistics') == -1) {
	        setExportToLimit(id, '');
	      }
	      if (id.indexOf('studySubjectStatusStatistics') == -1) {
	        setExportToLimit(id, '');
	      }
	      createHiddenInputFieldsForLimitAndSubmit(id);
	    }
	  </script>
	
	  <table>
	    <tr>
	      <td valign="top">
	        <form action="${pageContext.request.contextPath}/MainMenu">
	            ${studySiteStatistics}
	        </form>
	      </td>
	      <td valign="top">
	        <form action="${pageContext.request.contextPath}/MainMenu">
	            ${studyStatistics}
	        </form>
	      </td>
	    </tr>
	  </table>
	
	
	  <table>
	    <tr>
	      <td valign="top">
	        <form action="${pageContext.request.contextPath}/MainMenu">
	            ${subjectEventStatusStatistics}
	        </form>
	      </td>
	
	      <td valign="top">
	        <form action="${pageContext.request.contextPath}/MainMenu">
	            ${studySubjectStatusStatistics}
	        </form>
	      </td>
	    </tr>
	  </table>
	
	</c:if>
	
	<c:if test="${userRole.role.id eq 6}">
	
	
	  <script type="text/javascript">
	    function onInvokeAction(id, action) {
	      setExportToLimit(id, '');
	      createHiddenInputFieldsForLimitAndSubmit(id);
	    }
	    function onInvokeExportAction(id) {
	      var parameterString = createParameterStringForLimit(id);
	    }
	    function prompt(formObj, crfId) {
	    	
	    	formObj.action = '${pageContext.request.contextPath}/pages/handleSDVRemove';
	        formObj.crfId.value = crfId;
	        
	        confirmSubmit({
	        	message: "<fmt:message key="uncheck_sdv" bundle="${resmessages}"/>",
	        	height: 150,
	        	width: 500,
	        	form: formObj
	        });
	    }
	  </script>
	
	  <div id="searchFilterSDV">
	    <table border="0" cellpadding="0" cellspacing="0">
	      <tr>
	        <td valign="bottom" id="Tab1'">
	          <div id="Tab1NotSelected">
	            <div class="tab_BG">
	              <div class="tab_L">
	                <div class="tab_R">
	                  <a class="tabtext" title="<fmt:message key="view_by_event_CRF" bundle="${resword}"/>"
	                     href='pages/viewAllSubjectSDVtmp?studyId=${studyId}'
	                     onclick="javascript:HighlightTab(1);"><fmt:message key="view_by_event_CRF"
	                                                                        bundle="${resword}"/></a></div>
	              </div>
	            </div>
	          </div>
	          <div id="Tab1Selected" style="display:none">
	            <div class="tab_BG_h">
	              <div class="tab_L_h">
	                <div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_event_CRF"
	                                                                        bundle="${resword}"/></span></div>
	              </div>
	            </div>
	          </div>
	        </td>
	
	        <td valign="bottom" id="Tab2'">
	          <div id="Tab2Selected">
	            <div class="tab_BG">
	              <div class="tab_L">
	                <div class="tab_R">
	                  <a class="tabtext" title="<fmt:message key="view_by_studysubjectID" bundle="${resword}"/>"
	                     href='pages/viewSubjectAggregate?studyId=${studyId}'
	                     onclick="javascript:HighlightTab(2);"><fmt:message key="view_by_studysubjectID"
	                                                                        bundle="${resword}"/></a></div>
	              </div>
	            </div>
	          </div>
	          <div id="Tab2NotSelected" style="display:none">
	            <div class="tab_BG_h">
	              <div class="tab_L_h">
	                <div class="tab_R_h"><span class="tabtext"><fmt:message key="view_by_studysubjectID"
	                                                                        bundle="${resword}"/></span></div>
	              </div>
	            </div>
	          </div>
	        </td>
	
	      </tr>
	    </table>
	    <script language="JavaScript">
	      HighlightTab(1);
	    </script>
	  </div>
	  <div id="subjectSDV">
	    <form name='sdvForm' action="${pageContext.request.contextPath}/pages/viewAllSubjectSDVtmp">
	      <input type="hidden" name="studyId" value="${study.id}">
	      <input type="hidden" name=imagePathPrefix value="">
	        <%--This value will be set by an onclick handler associated with an SDV button --%>
	      <input type="hidden" name="crfId" value="0">
	        <%-- the destination JSP page after removal or adding SDV for an eventCRF --%>
	      <input type="hidden" name="redirection" value="viewAllSubjectSDVtmp">
	        <%--<input type="hidden" name="decorator" value="mydecorator">--%>
	        ${sdvMatrix}
	      <br/>
	      <input type="button" name="BTN_Back_Smart" id="GoToPreviousPage"
	             value="<fmt:message key="back" bundle="${resword}"/>" class="button_medium medium_back"
	             onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');"/>
	      <input type="submit" name="sdvAllFormSubmit" class="button_medium"
	             value="<fmt:message key="sdv_all_checked" bundle="${resword}"/>"
	             onclick="this.form.method='POST';this.form.action='${pageContext.request.contextPath}/pages/handleSDVPost';this.form.submit();"/>
	    </form>
	
	  </div>
	</c:if>
	<c:if test="${userRole.studyCoder}">
	
	  <script type="text/javascript">
	
	    $.ajax({
	      type: "POST",
	      url: "pages/codedItems"
	    })
	
	  </script>
	</c:if>
	</span>
	<!--//Old home page layout-->
</c:if>

<c:if test="${displayPageVersion=='new'}">
	<!--New home page layout-->
	<span class="new_home_page">
		<input type="hidden" id="userId" name="userId" value="${userBean.id}"/>
		<input type="hidden" id="studyId" name="studyId" value="${study.id}"/>
		<input type="hidden" id="isSponsor" name="isSponsor" value="${userRole.role.id == 10}"/>
		<input type="hidden" id="sponsorAccessMessage" value="<fmt:message bundle="${resword}" key="sponsor_no_privileges_for_chart"/>"/>
	
	<c:if test="${!empty dispayWidgetsLayout}">
		<table class="widgets_container">
			<tr>
				<td>
					<c:forEach var="widget" items="${dispayWidgetsLayout}">
						<c:if test="${widget.ordinal ne 0 and widget.ordinal%2 ne 0 and not widget.twoColumnWidget}">
							<div class="widget">
								<c:catch var="e">
									<c:import url="widgets/${widget.widgetName}" />
								</c:catch>
								<c:if test="${!empty e}">
									<div class="widget_error_message">
										<fmt:message key="widget_error_message_cannot_load_widget" bundle="${restext}">
											<fmt:param>
												<c:out value="${widget.widgetName}" />
											</fmt:param>
										</fmt:message>
									</div>
								</c:if>
							</div>
						</c:if>
					</c:forEach>
				</td>
				<td>
					<c:forEach var="widget" items="${dispayWidgetsLayout}">
						<c:if test="${widget.ordinal ne 0 and widget.ordinal%2 eq 0 and not widget.twoColumnWidget}">
							<div class="widget">
								<c:catch var="e">
									<c:import url="widgets/${widget.widgetName}" />
								</c:catch>
								<c:if test="${!empty e}">
									<div class="widget_error_message">
										<fmt:message key="widget_error_message_cannot_load_widget" bundle="${restext}">
											<fmt:param>
												<c:out value="${widget.widgetName}" />
											</fmt:param>
										</fmt:message>
									</div>
								</c:if>
							</div>
						</c:if>
					</c:forEach>
				</td>
			</tr>
			<tr>
				<td colspan="2">
					<c:forEach var="widget" items="${dispayWidgetsLayout}">
						<c:if test="${widget.ordinal ne 0 and widget.twoColumnWidget}">
							<div class="widget_big">
								<c:catch var="e">
									<c:import url="widgets/${widget.widgetName}" />
								</c:catch>
								<c:if test="${!empty e}">
									<div class="widget_error_message">
										<fmt:message key="widget_error_message_cannot_load_widget" bundle="${restext}">
											<fmt:param>
												<c:out value="${widget.widgetName}" />
											</fmt:param>
										</fmt:message>
									</div>
								</c:if>
							</div>
						</c:if>
					</c:forEach>
				</td>
			</tr>
		</table>
	</c:if>
	</span>
	<!--//New home page layout-->
</c:if>


 <br>
 	<table>
 		<tr>
 			<c:if test="${((userRole.role.id ne 6 && displayPageVersion=='old') || (displayPageVersion=='new'))}">
	 		<td>
	 			<input type="button" name="BTN_Back_Smart" id="GoToPrevisusPage" value="<fmt:message key="back" bundle="${resword}"/>" class="button_medium medium_back" onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');"/> 
	 		</td>
	 		</c:if>
	 		<c:if test="${displayPageVersion=='new'}">
				<td class="new_home_page">
					<input id="ConfigueHomePage" class="button_long" type="button" name="BTN_Config" value="<fmt:message key="customize_home_page" bundle="${resword}"/>" onClick="window.location.href=('pages/configureHomePage');"/>
				</td>
			</c:if>
		</tr>
 	</table> 
<br>
<c:if test="${redirectAfterLogin ne null}">
<div class="hidden">
	<iframe id="downloadFileIframe" src=""></iframe>
	<script>
		var redirectAfterLogin = "${redirectAfterLogin}";
		redirectAfterLogin = location.href.indexOf("https") >= 0 ? redirectAfterLogin.replace("http:", "https:") : redirectAfterLogin;
		$("#downloadFileIframe").attr("src", redirectAfterLogin);
	</script>
</div>
</c:if>
<jsp:include page="include/footer.jsp"/>
