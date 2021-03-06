<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/tlds/ui/ui.tld" prefix="ui" %>

<ui:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.workflow" var="resworkflow"/>
<ui:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<c:choose>
<c:when test="${userBean.sysAdmin && module=='admin'}">
 <c:import url="../include/admin-header.jsp"/>
</c:when>
<c:otherwise>
 <c:import url="../include/submit-header.jsp"/>
</c:otherwise>
</c:choose>

<script type="text/javascript" language="JavaScript">
  
  function checkOverwriteStatus() {
      return confirm('<fmt:message key="you_will_overwrite_event_CRFs_continue" bundle="${resword}"/>');
  }
 
</script>

<!-- *JSP* submit/verifyImport.jsp -->
<!-- *JSP* ${pageContext.page['class'].simpleName} -->
<jsp:include page="../include/sideAlert.jsp"/>

<!-- then instructions-->
<tr id="sidebar_Instructions_open" style="display: all">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_collapse.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${restext}"/></b>
		<div class="sidebar_tab_content">
			<fmt:message key="import_side_bar_instructions" bundle="${restext}"/>
		</div>
	</td>
</tr>

<tr id="sidebar_Instructions_closed" style="display: none">
	<td class="sidebar_tab">
		<a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');">
			<img src="images/sidebar_expand.gif" border="0" align="right" hspace="10">
		</a>
		<b><fmt:message key="instructions" bundle="${restext}"/></b>
	</td>
</tr>

<jsp:include page="../include/sideInfo.jsp"/>

<jsp:useBean scope='session' id='version' class='org.akaza.openclinica.bean.submit.CRFVersionBean'/>
<jsp:useBean scope='session' id='userBean' class='org.akaza.openclinica.bean.login.UserAccountBean'/>
<jsp:useBean scope='session' id='importedData' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='subjectData' class='java.util.ArrayList'/>
<jsp:useBean scope='session' id='validationErrors' class='java.util.HashMap'/>
<jsp:useBean scope='session' id='hardValidationErrors' class='java.util.HashMap'/>
<jsp:useBean scope='session' id='summaryStats' class='org.akaza.openclinica.bean.submit.crfdata.SummaryStatsBean'/>
<jsp:useBean scope='session' id='crfName' class='java.lang.String'/>


<%-- <c:out value="${crfName}"/> --%>

<h1>
	<span class="first_level_header">
		<fmt:message key="import_crf_data" bundle="${resworkflow}"/>
	</span>
</h1>

<p><fmt:message key="import_instructions" bundle="${restext}"/></p>

<div style="width: 300px">
<table class="table_shadow_bottom table_horizontal" width="100%">
	<tr valign="top">
		<td class="table_header_row"><fmt:message key="summary_statistics" bundle="${resword}"/>:</td>
	</tr>
	<tr valign="top">
    	<td class="table_cell_left"><fmt:message key="subjects_affected" bundle="${resword}"/>: <c:out value="${summaryStats.studySubjectCount}" /></td>
	</tr>
	<tr valign="top">
    	<td class="table_cell_left"><fmt:message key="event_CRFs_affected" bundle="${resword}"/>: <c:out value="${summaryStats.eventCrfCount}" /></td>
	</tr>
	<tr valign="top">
    	<td class="table_cell_left"><fmt:message key="validation_rules_generated" bundle="${resword}"/>: <c:out value="${summaryStats.discNoteCount}" /></td>
	</tr>
</table>
</div>
<br/>

<c:if test="${fn:length(summaryStats.unavailableCRFVersionOIDs) > 0}">
	<div style="width: 300px">
		<table class="table_shadow_bottom table_horizontal" width="100%">
			<tr valign="top">
				<td class="table_cell_left">
					<fmt:message key="unavailable_crf_version_oids" bundle="${resword}"/>:
				</td>
			</tr>
			<c:forEach items="${summaryStats.unavailableCRFVersionOIDs}" var="crfVersionOID">
				<tr valign="top">
					<td class="table_cell_left">
						<c:out value="${crfVersionOID}"/>
					</td>
				</tr>
			</c:forEach>
		</table>
	</div>
	<br/>
</c:if>

<%-- hard validation errors here --%>
<%-- if we have hard validation errors here, we stop and don't generate the other two tables --%>
<c:choose>
	<c:when test="${not empty hardValidationErrors}">
	<fmt:message key="hard_validation_error_checks" bundle="${resword}"/>
	<div style="width: 100%">
	<table class="table_shadow_bottom" border="0" cellpadding="0" cellspacing="0" width="90%">

		<c:forEach var="subjectDataBean" items="${subjectData}" >
			<tr valign="top">
				<td class="table_header_row" colspan="4"><fmt:message key="study_subject" bundle="${resword}"/>: <c:out value="${subjectDataBean.subjectOID}"/></td>
			</tr>
			<c:forEach var="studyEventData" items="${subjectDataBean.studyEventData}">
				<tr valign="top">
		    		<td class="table_header_row"><fmt:message key="event_CRF_OID" bundle="${resword}"/></td>
		    		<td class="table_header_row" colspan="3"></td>
				</tr>
				<tr valign="top">
		    		<td class="table_cell_left"><c:out value="${studyEventData.studyEventOID}"/>
		    		<c:choose>
		    			<c:when test="${studyEventData.studyEventRepeatKey != null}">
		    				(<fmt:message key="repeated_key" bundle="${resword}">
		    					<fmt:param><c:out value="${studyEventData.studyEventRepeatKey}"/></fmt:param>
		    				</fmt:message>)
		    				<c:set var="studyEventRepeatKey" value="${studyEventData.studyEventRepeatKey}"/>
		    			</c:when>
		    			<c:otherwise>
		    				<c:set var="studyEventRepeatKey" value="${1}"/>
		    			</c:otherwise>
		    			</c:choose>
		    		</td>
		    		<td class="table_cell" colspan="3"></td>
				</tr>
				<c:forEach var="formData" items="${studyEventData.formData}">
					<tr valign="top">
			    		<td class="table_header_row"></td>
			    		<td class="table_header_row"><fmt:message key="CRF_version_OID" bundle="${resword}"/></td>
			    		<td class="table_header_row" colspan="2"></td>
					</tr>
					<tr valign="top">
		    			<td class="table_cell_left"></td>
		    			<td class="table_cell"><c:out value="${formData.formOID}"/></td>
		    			<td class="table_cell" colspan="2"></td>
					</tr>
					<c:forEach var="itemGroupData" items="${formData.itemGroupData}">
						<tr valign="top">
				    		<td class="table_header_row"></td>
				    		<td class="table_header_row"></td>
				    		<td class="table_header_row" colspan="2"><c:out value="${itemGroupData.itemGroupOID}"/>
				    		<c:choose>
				    			<c:when test="${itemGroupData.itemGroupRepeatKey != null}">
				    				(<fmt:message key="repeated_key" bundle="${resword}">
		    						 	<fmt:param><c:out value="${itemGroupData.itemGroupRepeatKey}"/></fmt:param>
		    						 </fmt:message> )
				    				<c:set var="groupRepeatKey" value="${itemGroupData.itemGroupRepeatKey}"/>
				    			</c:when>
				    			<c:otherwise>
				    				<c:set var="groupRepeatKey" value="${1}"/>
				    			</c:otherwise>
				    		</c:choose>
				    		</td>
				    		<%-- add repeat key here? --%>
						</tr>
						<c:forEach var="itemData" items="${itemGroupData.itemData}">
						<c:set var="oidKey" value="${itemData.itemOID}_${studyEventRepeatKey}_${groupRepeatKey}_${subjectDataBean.subjectOID }"/>
						<c:if test="${not empty hardValidationErrors[oidKey]}">
							<tr valign="top">
					    		<td class="table_cell_left"></td>
					    		<td class="table_cell"></td>
					    		<td class="table_cell"><font color="red"><c:out value="${itemData.itemOID}"/></font></td>
					    		<%-- or add it here? --%>
					    		<td class="table_cell">
					    			<c:out value="${itemData.value}"/><br/>
					    			<c:out value="${hardValidationErrors[oidKey]}"/>
					    		</td>
							</tr>
						</c:if>
						</c:forEach>
					</c:forEach>
				</c:forEach>
			</c:forEach>
		</c:forEach>
	</table>
	</div>
	<br/>
	<%-- place form here, so that user can go back --%>
	<form action="ImportCRFData?action=confirm&crfId=<c:out value="${version.crfId}"/>&name=<c:out value="${version.name}"/>" method="post" ENCTYPE="multipart/form-data">

	<p><fmt:message key="import_instructions" bundle="${restext}"/></p>
	<div style="width: 400px">
	<table class="table_shadow_bottom" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td class="formlabel"><fmt:message key="xml_file_to_upload" bundle="${resterm}"/>:</td>
		<td>
			<input type="file" name="xml_file" > 
			<br><jsp:include page="../showMessage.jsp"><jsp:param name="key" value="xml_file"/></jsp:include>
		</td>
	</tr>
	</table>
		<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>">
	</div>

	<br clear="all">
	<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
               value="<fmt:message key="back" bundle="${resword}"/>"
               class="button_medium medium_back"
               onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />
	
	<input type="submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium medium_continue">

	</form>
	</c:when>

	<c:otherwise>
		<%-- place everything else here --%>
		<%-- validation errors here --%>
		<c:if test="${not empty validationErrors}">
			<fmt:message key="validation_error_generated" bundle="${resword}" />

			<div style="width: 100%">
			<table class="table_shadow_bottom" border="0" cellpadding="0" cellspacing="0" width="100%">

				<c:forEach var="subjectDataBean" items="${subjectData}">
					<tr valign="top">
						<td class="table_header_row" colspan="4"><fmt:message
							key="study_subject" bundle="${resword}" />: <c:out
							value="${subjectDataBean.subjectOID}" /></td>
					</tr>
					<c:forEach var="studyEventData"
						items="${subjectDataBean.studyEventData}">
						<tr valign="top">
							<td class="table_header_row"><fmt:message
								key="event_CRF_OID" bundle="${resword}" /></td>
							<td class="table_header_row" colspan="3"></td>
						</tr>
						<tr valign="top">
							<td class="table_cell_left"><c:out
								value="${studyEventData.studyEventOID}" /> <c:choose>
								<c:when test="${studyEventData.studyEventRepeatKey != null}">
			    				(<fmt:message key="repeated_key" bundle="${resword}">
										<fmt:param>
											<c:out value="${studyEventData.studyEventRepeatKey}" />
										</fmt:param>
									</fmt:message> )
			    				<c:set var="studyEventRepeatKey"
										value="${studyEventData.studyEventRepeatKey}" />
								</c:when>
								<c:otherwise>
									<c:set var="studyEventRepeatKey" value="${1}" />
								</c:otherwise>
							</c:choose></td>
							<td class="table_cell" colspan="3"></td>
						</tr>
						<c:forEach var="formData" items="${studyEventData.formData}">
							<tr valign="top">
								<td class="table_header_row"></td>
								<td class="table_header_row"><fmt:message
									key="CRF_version_OID" bundle="${resword}" /></td>
								<td class="table_header_row" colspan="2"></td>
							</tr>
							<tr valign="top">
								<td class="table_cell_left"></td>
								<td class="table_cell"><c:out value="${formData.formOID}" /></td>
								<td class="table_cell" colspan="2"></td>
							</tr>
							<c:forEach var="itemGroupData" items="${formData.itemGroupData}">
								<tr valign="top">
									<td class="table_header_row"></td>
									<td class="table_header_row"></td>
									<td class="table_header_row" colspan="2"><c:out
										value="${itemGroupData.itemGroupOID}" /> <c:choose>
										<c:when test="${itemGroupData.itemGroupRepeatKey != null}">
					    				(<fmt:message key="repeated_key" bundle="${resword}">
												<fmt:param>
													<c:out value="${itemGroupData.itemGroupRepeatKey}" />
												</fmt:param>
											</fmt:message> )
					    				<c:set var="groupRepeatKey"
												value="${itemGroupData.itemGroupRepeatKey}" />
										</c:when>
										<c:otherwise>
											<c:set var="groupRepeatKey" value="${1}" />
										</c:otherwise>
									</c:choose></td>
									<%-- add repeat key here? --%>
								</tr>
								<c:forEach var="itemData" items="${itemGroupData.itemData}">
									<c:set var="oidKey"
										value="${itemData.itemOID}_${studyEventRepeatKey}_${groupRepeatKey}_${subjectDataBean.subjectOID}" />
									<c:if test="${not empty validationErrors[oidKey]}">
										<tr valign="top">
											<td class="table_cell_left"></td>
											<td class="table_cell"></td>
											<td class="table_cell"><font color="red"><c:out
												value="${itemData.itemOID}" /></font></td>
											<%-- or add it here? --%>
											<td class="table_cell"><c:out value="${itemData.value}" /><br />
											<c:out value="${validationErrors[oidKey]}" /></td>
										</tr>
									</c:if>
								</c:forEach>
							</c:forEach>
						</c:forEach>
					</c:forEach>
				</c:forEach>
			</table>
			</div>
		</c:if>
		<br />

        <c:set var="subjectSpan" value="4"/>
        <c:set var="formOidSpan" value="2"/>
        <c:set var="eventCrfOidSpan" value="3"/>
        <c:set var="studyEventOidSpan" value="3"/>
        <c:set var="crfVersionOidSpan" value="3"/>
        <c:if test="${study.studyParameterConfig.replaceExisitingDataDuringImport == 'no'}">
            <c:set var="subjectSpan" value="5"/>
            <c:set var="formOidSpan" value="3"/>
            <c:set var="eventCrfOidSpan" value="4"/>
            <c:set var="studyEventOidSpan" value="4"/>
            <c:set var="crfVersionOidSpan" value="3"/>
        </c:if>

		<!--  valid data section, show all valid data -->
		<fmt:message key="valid_data_imported" bundle="${resword}"/>
		<c:set var="notificationColumnRequired" scope="request"
			   value="${fn:length(summaryStats.unavailableCRFVersionOIDs) > 0
					|| study.studyParameterConfig.replaceExisitingDataDuringImport == 'no'}"/>

			<div style="width: 100%">
			<table class="table_shadow_bottom" border="0" cellpadding="0" cellspacing="0" width="100%">

				<c:forEach var="subjectDataBean" items="${subjectData}">
					<tr valign="top">
						<td class="table_header_row" colspan="${subjectSpan}"><fmt:message
							key="study_subject" bundle="${resword}" />: <c:out
							value="${subjectDataBean.subjectOID}" /></td>
					</tr>
					<c:forEach var="studyEventData"
						items="${subjectDataBean.studyEventData}">
						<tr valign="top">
							<td class="table_header_row"><fmt:message
								key="event_CRF_OID" bundle="${resword}" /></td>
							<td class="table_header_row" colspan="${eventCrfOidSpan}"></td>
						</tr>
						<tr valign="top">
							<td class="table_cell_left"><c:out
								value="${studyEventData.studyEventOID}" /> <c:choose>
								<c:when test="${studyEventData.studyEventRepeatKey != null}">
			    				(<fmt:message key="repeated_key" bundle="${resword}">
										<fmt:param>
											<c:out value="${studyEventData.studyEventRepeatKey}" />
										</fmt:param>
									</fmt:message>)
			    				<c:set var="studyEventRepeatKey"
										value="${studyEventData.studyEventRepeatKey}" />
								</c:when>
								<c:otherwise>
									<c:set var="studyEventRepeatKey" value="${1}" />
								</c:otherwise>
							</c:choose></td>
							<td class="table_cell" colspan="${studyEventOidSpan}"></td>
						</tr>
						<c:forEach var="formData" items="${studyEventData.formData}">
							<tr valign="top">
								<td class="table_header_row"></td>
								<td class="table_header_row"><fmt:message
									key="CRF_version_OID" bundle="${resword}" /></td>
								<td class="table_header_row" colspan="${crfVersionOidSpan}"></td>
							</tr>
							<tr valign="top">
								<td class="table_cell_left"></td>
								<td class="table_cell"><c:out value="${formData.formOID}" /></td>
								<td class="table_cell" colspan="2"></td>
							</tr>
							<c:forEach var="itemGroupData" items="${formData.itemGroupData}">
								<tr valign="top">
									<td class="table_header_row"></td>
									<td class="table_header_row"></td>
									<td class="table_header_row" colspan="${formOidSpan}"><c:out
										value="${itemGroupData.itemGroupOID}" /> <c:choose>
										<c:when test="${itemGroupData.itemGroupRepeatKey != null}">
					    				(<fmt:message key="repeated_key" bundle="${resword}">
												<fmt:param>
													<c:out value="${itemGroupData.itemGroupRepeatKey}" />
												</fmt:param>
											</fmt:message> )
					    				<c:set var="groupRepeatKey"
												value="${itemGroupData.itemGroupRepeatKey}" />
										</c:when>
										<c:otherwise>
											<c:set var="groupRepeatKey" value="${1}" />
										</c:otherwise>
									</c:choose></td>
								</tr>
								<c:forEach var="itemData" items="${itemGroupData.itemData}">
									<c:set var="oidKey"
										value="${itemData.itemOID}_${studyEventRepeatKey}_${groupRepeatKey}_${subjectDataBean.subjectOID}" />
									<c:if test="${(empty validationErrors[oidKey]) and itemData.autoAdded == false}">
										<tr valign="top">
											<td class="table_cell_left"></td>
											<td class="table_cell"></td>
											<td class="table_cell"><c:out value="${itemData.itemOID}" /></td>
											<td class="table_cell"><c:out value="${itemData.value}" /></td>
                                            <c:if test="${notificationColumnRequired}">
												<c:set var="crfVersionUnavailable" value="false" scope="request" />
												<c:forEach items="${summaryStats.unavailableCRFVersionOIDs}" var="value">
													<c:if test="${value == formData.formOID}">
														<c:set var="crfVersionUnavailable" value="true" scope="request" />
													</c:if>
												</c:forEach>
                                                <td class="table_cell">
													<c:if test="${itemData.skip}">
														<fmt:message key="will_be_skipped" bundle="${resword}"/>
													</c:if>
													<c:if test="${crfVersionUnavailable}">
														<fmt:message key="will_be_skipped" bundle="${resword}"/>
														<br/><fmt:message key="crf_version_is_unavailable" bundle="${resword}"/>
													</c:if>
                                                </td>
                                            </c:if>
										</tr>
									</c:if>
								</c:forEach>
							</c:forEach>
						</c:forEach>
					</c:forEach>
				</c:forEach>
			</table>
			</div>
			<br />
			
<form action="VerifyImportedCRFData?action=save" method="POST">

	<input type="hidden" name="crfId" value="<c:out value="${version.crfId}"/>"> 
	<c:set var="overwriteCount" value="${0}" /> 
	<c:forEach
		var="displayItemBeanWrapper" items="${importedData}">
		<c:if test="${displayItemBeanWrapper.overwrite}">
			<c:set var="overwriteCount" value="${overwriteCount + 1}" />
		</c:if>
	</c:forEach> 			
			 
	<table>
		<td>
			<input type="button" name="BTN_Smart_Back" id="GoToPreviousPage"
					value="<fmt:message key="back" bundle="${resword}"/>"
					class="button_medium medium_back"
					onClick="javascript: goBackSmart('${navigationURL}', '${defaultURL}');" />		
		</td>
		<td>
			<c:if test="${overwriteCount == 0}">
				<input type="submit" value="<fmt:message key="continue" bundle="${resword}"/>" class="button_medium medium_continue">
			</c:if> 
			<c:if test="${overwriteCount > 0 }">
				<input type="submit"
					value="<fmt:message key="continue" bundle="${resword}"/>"
					class="button_medium medium_continue" 
					onClick="return confirmSubmit({ message: '<fmt:message key="you_will_overwrite_event_CRFs_continue" bundle="${resword}"/>', height: 150, width: 500, submit: this });">
			</c:if>	
		</td>
			<%-- added an alert above --%>
		<td>
			<input id="Cancel" class="button_medium medium_cancel" type="button" name="BTN_Cancel" value="<fmt:message key="cancel" bundle="${resword}"/>" onclick="window.location.href=('ListStudySubjects');"/>
		</td>	
	</table>
</form>				
			<%-- end of the other loop --%>
	</c:otherwise>
</c:choose>

<c:choose>
  <c:when test="${userBean.sysAdmin && module=='admin'}">
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