<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<c:set var="dteFormat"><fmt:message key="date_format_string" bundle="${resformat}"/></c:set>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.DisplayStudyEventRow" />
<jsp:useBean scope="request" id="studySub" class="org.akaza.openclinica.bean.managestudy.StudySubjectBean"/>
   <tr>
     <td class="table_cell_left"><c:out value="${currRow.bean.studyEvent.studyEventDefinition.name}"/>
        <c:if test="${currRow.bean.studyEvent.studyEventDefinition.repeating}">
            (<c:out value="${currRow.bean.studyEvent.sampleOrdinal}"/>)
        </c:if>
     </td>
     <td class="table_cell"><fmt:formatDate value="${currRow.bean.studyEvent.dateStarted}" pattern="${dteFormat}"/>
     </td>

     <td class="table_cell"><c:out value="${currRow.bean.studyEvent.location}"/></td>
     <td class="table_cell" width="30"><c:out value="${currRow.bean.studyEvent.subjectEventStatus.name}"/></td>
     <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
		<tr>
		<td>
        <a href="EnterDataForStudyEvent?eventId=<c:out value="${currRow.bean.studyEvent.id}"/>"
		onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
		onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
		name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
		</td>
		</tr>
		<tr><td>&nbsp;</td></tr>
		
		<c:if test="${(studySub.status.name != 'removed' && studySub.status.name != 'auto-removed' && !currRow.bean.studyEvent.status.deleted && currRow.bean.studyEvent.editable) && (study.status.available)}">
        <tr>
		<td>
        <a href="UpdateStudyEvent?event_id=<c:out value="${currRow.bean.studyEvent.id}"/>&ss_id=<c:out value="${studySub.id}"/>"
		onMouseDown="javascript:setImage('bt_Edit1','images/bt_Edit_d.gif');"
		onMouseUp="javascript:setImage('bt_Edit1','images/bt_Edit.gif');"><img
		name="bt_Edit1" src="images/bt_Edit.gif" border="0" alt="<fmt:message key="edit" bundle="${resword}"/>" title="<fmt:message key="edit" bundle="${resword}"/>" align="left" hspace="6"></a>
		</td>
		</tr>
		<tr><td>&nbsp;</td></tr>
		</c:if>
		
		<tr>
		<td>
		<c:choose>
         <c:when test="${!currRow.bean.studyEvent.status.deleted}">
           <c:if test="${userRole.manageStudy && study.status.available}">
           <a href="RemoveStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
			onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
			name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
           </c:if>
         </c:when>
         <c:otherwise>
          <c:if test="${(userRole.manageStudy && studySub.status.name != 'removed' && studySub.status.name != 'auto-removed') && (study.status.available)}">
           <a href="RestoreStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
			onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
			onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img
			name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </c:if>
         </c:otherwise>
        </c:choose>

        <c:if test="${currRow.bean.studyEvent.subjectEventStatus.scheduled && userRole.manageStudy && study.status.available}">
            <tr><td>&nbsp;</td></tr>
            <tr><td>
                <a href="DeleteStudyEvent?action=confirm&id=<c:out value="${currRow.bean.studyEvent.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
                 onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
                 onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
                 name="bt_Remove1" src="images/bt_Delete.gif" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>" title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"></a>
            </td></tr>
        </c:if>


		</td>
       </tr>
      </table>
     </td>
     <td class="table_cell">

      <c:choose>

	  <c:when test="${empty currRow.bean.uncompletedCRFs && empty currRow.bean.displayEventCRFs}">
		<fmt:message key="no_CRFs" bundle="${resword}"/>
	  </c:when>

	  <c:otherwise>

      <table border="0" cellpadding="0" cellspacing="0">

      <c:forEach var="dedc" items="${currRow.bean.fullCrfList}">
        <c:choose>
        <c:when test="${dedc.class.name eq 'org.akaza.openclinica.bean.managestudy.DisplayEventDefinitionCRFBean'}">

		<c:set var="getQuery" value="eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}&eventCRFId=${dedc.eventCRF.id}&exitTo=ViewStudySubject?id=${studySub.id}" />
        <form name="startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" action="InitialDataEntry?<c:out value="${getQuery}"/>" method="POST">
				
			<tr valign="top">
				<td class="table_cell" width="180"><c:out value="${dedc.edc.crf.name}"/> 
					<c:if test="${dedc.edc.requiredCRF}">	
						<span style="color: orange">*</span>
					</c:if> 
					<c:if test="${dedc.edc.sourceDataVerification.code eq 1 or dedc.edc.sourceDataVerification.code eq 2}">
						<img src="images/sdv.png" style="border: none; margin: 0px; padding: 0px;"/>
					</c:if>
				</td>
				<td class="table_cell" width="50">

				<c:set var="versionCount" value="0"/>
				<c:set var="firstVersionId" value="0"/>
				<c:forEach var="version" items="${dedc.edc.versions}">
					<c:if test="${versionCount == 0}">
						<c:set var="firstVersionId" value="${version.id}"/>
					</c:if>
                    <c:set var="versionCount" value="${versionCount+1}"/>
                </c:forEach>
							
				<c:choose>
					<c:when test="${dedc.eventCRF.notStarted && dedc.eventCRF.id == 0}">
						<input type="hidden" name="crfVersionId" value="<c:out value="${firstVersionId}"/>">
					</c:when>
					<c:when test="${versionCount > 1 && dedc.eventCRF.notStarted && dedc.eventCRF.id > 0}">
						<input type="hidden" name="crfVersionId" value="<c:out value="${dedc.eventCRF.crfVersion.id}"/>">
					</c:when>
					<c:when test="${versionCount == 1 && dedc.eventCRF.notStarted && dedc.eventCRF.id > 0}">
						<input type="hidden" name="crfVersionId" value="<c:out value="${firstVersionId}"/>">
					</c:when>
					<c:otherwise>
						<input type="hidden" name="crfVersionId" value="<c:out value="${defaultVersionId}"/>">
					</c:otherwise>
				</c:choose>

				<c:choose>
					<c:when test="${versionCount<=1}">
						<c:forEach var="version" items="${dedc.edc.versions}">
							<c:out value="${version.name}"/>
						</c:forEach>
					</c:when>

					<c:when test="${dedc.eventCRF.notStarted || dedc.eventCRF.id == 0}">
						<select name="versionId<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>" onchange="javascript:changeQuery<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>();">
							<c:forEach var="version" items="${dedc.edc.versions}">
								<c:set var="getQuery" value="action=ide_s&eventDefinitionCRFId=${dedc.edc.id}&studyEventId=${currRow.bean.studyEvent.id}&subjectId=${studySub.subjectId}" />
								<c:choose>
									<c:when test="${(dedc.edc.defaultVersionId == version.id && dedc.eventCRF.id == 0) || (dedc.eventCRF.CRFVersionId == version.id && dedc.eventCRF.notStarted)}">
										<option value="<c:out value="${version.id}"/>" selected>
											<c:out value="${version.name}"/>
										</option>
									</c:when>
									<c:otherwise>
										<option value="<c:out value="${version.id}"/>">
											<c:out value="${version.name}"/>
										</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</select>

						<SCRIPT LANGUAGE="JavaScript">
							function changeQuery<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>() {
								var qer = document.startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.versionId<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.value;
								document.startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>.crfVersionId.value=qer;
							}
						</SCRIPT>
					</c:when>
					<c:otherwise>
						<c:out value="${dedc.eventCRF.crfVersion.name}"/>
					</c:otherwise>
				</c:choose>
				</td>
				
				<c:choose>
				<c:when test="${dedc.status.name=='locked'}">
					<td class="table_cell" bgcolor="#F5F5F5" align="center" width="20">
						<img src="images/icon_Locked.gif" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
					</td>
				</c:when>

                <c:when test="${studySubject.status.name != 'removed'&& studySubject.status.name != 'auto-removed'}">
                    <c:choose>
                        <c:when test="${dedc.eventCRF.id > 0 and !dedc.eventCRF.notStarted}">
                            <td class="table_cell" bgcolor="#F5F5F5" align="center" width="20"><img src="images/icon_InitialDE.gif" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>"></td>
                        </c:when>
                        <c:otherwise>
                            <td class="table_cell" bgcolor="#F5F5F5" align="center" width="20"><img src="images/icon_NotStarted.gif" alt="<fmt:message key="not_started" bundle="${resword}"/>" title="<fmt:message key="not_started" bundle="${resword}"/>"></td>
                        </c:otherwise>
                    </c:choose>
                </c:when>
				<c:otherwise>
					<td class="table_cell" bgcolor="#F5F5F5" align="center" width="20">
						<img src="images/icon_Invalid.gif" alt="<fmt:message key="invalid" bundle="${resword}"/>" title="<fmt:message key="invalid" bundle="${resword}"/>">
					</td>
				</c:otherwise>
				</c:choose>
				<td class="table_cell" width="110">&nbsp;&nbsp;</td>
				<td class="table_cell" width="220">
				<table cellspacing="0" cellpadding="0" border="0">
				<tr>
				<c:choose>
				 <c:when test="${studySub.status.name != 'removed' && studySub.status.name != 'auto-removed' && study.status.available && !currRow.bean.studyEvent.status.deleted && userRole.role.id ne 6}">
                    <td>
                    <a href="#" onclick="checkCRFLockedInitial('<c:out value="${dedc.eventCRF.id}"/>', document.startForm<c:out value="${currRow.bean.studyEvent.id}"/><c:out value="${dedc.edc.crf.id}"/>);"
                      onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
                      onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
                     <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="enter_data" bundle="${resword}"/>" title="<fmt:message key="enter_data" bundle="${resword}"/>" align="right" hspace="6">
                    </a>
                    </td>
				 </c:when>
				 <c:otherwise>
				 <td>
					<img name="itemForSpace" src="images/bt_EnterData.gif" border="0" style="visibility:hidden"  align="left" hspace="6">
				</td>
				 </c:otherwise>
				</c:choose>
				
				
		         <td>
                     <a href="ViewSectionDataEntry?eventDefinitionCRFId=<c:out value="${dedc.edc.id}"/>&crfVersionId=<c:out value="${dedc.edc.defaultVersionId}"/>&tabId=1&studySubjectId=<c:out value="${studySub.id}"/>&ecId=<c:out value="${dedc.eventCRF.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}"
                        onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			            onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');">
                     <img
		         name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view_data" bundle="${resword}"/>" title="<fmt:message key="view_data" bundle="${resword}"/>" align="left" hspace="6">

		         </a>
                 </td>

		         <td><a href="javascript:openDocWindow('PrintCRF?id=<c:out value="${dedc.edc.defaultVersionId}"/>')"
			     onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');"
			     onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><img
		         name="bt_Print1" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>" title="<fmt:message key="print" bundle="${resword}"/>" align="left" hspace="6"></a></td>

				<!-- added 12/2012 #121 clinovo -->
				<c:if test="${(dedc.eventCRF.id > 0 and !dedc.eventCRF.notStarted) && 
 (userBean.sysAdmin || (userRole.studyDirector || userRole.studyAdministrator)) &&
 (study.status.available || study.status.pending) 
&& !(currRow.bean.studyEvent.subjectEventStatus.locked || currRow.bean.studyEvent.subjectEventStatus.skipped)}">
   				<td>
				<a href="pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dedc.eventCRF.crf.id}" />&crfName=<c:out value="${dedc.eventCRF.crf.name}" />&crfversionId=<c:out value="${dedc.eventCRF.crfVersion.id}" />&crfVersionName=<c:out value="${dedc.eventCRF.crfVersion.name}" />&studySubjectLabel=<c:out value="${studySub.label}"/>&studySubjectId=<c:out value="${studySub.id}"/>&eventCRFId=<c:out value="${dedc.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dedc.edc.id}"/>"
   					onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
   					onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');">
					<img name="itemForSpace" src="images/bt_EnterData.gif" border="0" style="visibility:hidden"  align="left" hspace="6">
					<img name="itemForSpace" src="images/bt_EnterData.gif" border="0" style="visibility:hidden"  align="left" hspace="6">
					<img name="Reassign" src="images/bt_Reassign.gif" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></a>
    			</td>
   				</c:if>

				</tr></table>
				</td>
			</tr>
			</form>

    </c:when>
    <c:when test="${dedc.class.name eq 'org.akaza.openclinica.bean.submit.DisplayEventCRFBean'}">
    <c:set scope="request" var="hideCaseBookButton" value="false"/>
    <c:set var="dec" value="${dedc}"/>
	<tr>
		<td class="table_cell" width="180"><c:out value="${dec.eventCRF.crf.name}"/> <c:if test="${dec.eventDefinitionCRF.requiredCRF}"><span style="color: orange">*</span></c:if> <c:if test="${dec.eventDefinitionCRF.sourceDataVerification.code eq 1 or dec.eventDefinitionCRF.sourceDataVerification.code eq 2}"><img src="images/sdv.png" style="border: none; margin: 0px; padding: 0px;"/></c:if></td>
		<td class="table_cell" width="50"><c:out value="${dec.eventCRF.crfVersion.name}" /></td>
		<td class="table_cell" bgcolor="#F5F5F5" align="center" width="20">
		<c:choose>
		 <c:when test="${dec.stage.initialDE}">
		  <img src="images/icon_InitialDE.gif" alt="<fmt:message key="initial_data_entry" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.initialDE_Complete}">
		  <img src="images/icon_InitialDEcomplete.gif" alt="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="initial_data_entry_complete" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.doubleDE}">
		    <img src="images/icon_DDE.gif" alt="<fmt:message key="double_data_entry" bundle="${resword}"/>" title="<fmt:message key="double_data_entry" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.doubleDE_Complete}">
           <c:choose>
               <c:when test="${currRow.bean.studyEvent.subjectEventStatus.signed}">
                   <img src="images/icon_Signed.gif" alt="<fmt:message key="subjectEventSigned" bundle="${resword}"/>" title="<fmt:message key="subjectEventSigned" bundle="${resword}"/>">
               </c:when>
               <c:when test="${dec.eventCRF.sdvStatus}">
                   <img src="images/icon_DoubleCheck.gif" alt="<fmt:message key="sourceDataVerified" bundle="${resword}"/>" title="<fmt:message key="sourceDataVerified" bundle="${resword}"/>">
               </c:when>
               <c:otherwise>
                   <img src="images/icon_DEcomplete.gif" alt="<fmt:message key="data_entry_complete" bundle="${resword}"/>" title="<fmt:message key="data_entry_complete" bundle="${resword}"/>">
               </c:otherwise>
           </c:choose>
		 </c:when>
		 <c:when test="${dec.stage.admin_Editing}">
		    <img src="images/icon_AdminEdit.gif" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>">
		 </c:when>
		 <c:when test="${dec.stage.locked || dec.eventCRF.status.locked || dec.locked}">
		   <img src="images/icon_Locked.gif" alt="<fmt:message key="locked" bundle="${resword}"/>" title="<fmt:message key="locked" bundle="${resword}"/>">
		 </c:when>
		 <c:otherwise>
		    <img src="images/icon_Invalid.gif" alt="<fmt:message key="removed" bundle="${resword}"/>" title="<fmt:message key="removed" bundle="${resword}"/>">
		 </c:otherwise>
		</c:choose>

		</td>
		<td class="table_cell" width="110">
		<c:if test="${dec.eventCRF.updatedDate != null}">
		 <fmt:formatDate value="${dec.eventCRF.updatedDate}" pattern="${dteFormat}"/><br>
		</c:if>
		<c:choose>
		  <c:when test="${dec.eventCRF.updater.name == null}">
		    (<c:out value="${dec.eventCRF.owner.name}"/>)
		  </c:when>
		  <c:otherwise>
		   (<c:out value="${dec.eventCRF.updater.name}"/>)
		  </c:otherwise>
		 </c:choose>
		</td>

		<td class="table_cell" width="220">
		 <table border="0" cellpadding="0" cellspacing="0">
	     <tr valign="top">
	     <td>
		 <c:choose>
			<c:when test="${!dec.eventCRF.status.deleted && !dec.eventCRF.status.locked && study.status.available && !currRow.bean.studyEvent.status.deleted && userRole.role.id ne 6}">
			    <c:if test="${dec.continueInitialDataEntryPermitted}">
		           <a href="#"
				    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
				    onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', 'InitialDataEntry?eventCRFId=<c:out value="${dec.eventCRF.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}');">
                       <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6">
				    </a>
    		    </c:if>
	    		<c:if test="${dec.startDoubleDataEntryPermitted}">
    				<a href="#"
	    			onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
		    		onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', 'DoubleDataEntry?eventCRFId=<c:out value="${dec.eventCRF.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}');">
                        <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" title="<fmt:message key="begin_double_data_entry" bundle="${resword}"/>" align="left" hspace="6"></a>
    			</c:if>
	    		<c:if test="${dec.continueDoubleDataEntryPermitted}">
		            <a href="#"
				    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
				    onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', 'DoubleDataEntry?eventCRFId=<c:out value="${dec.eventCRF.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}');">
				    <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="continue_entering_data" bundle="${resword}"/>" title="<fmt:message key="continue_entering_data" bundle="${resword}"/>" align="left" hspace="6"></a>
    			</c:if>
    			<c:if test="${(dec.performAdministrativeEditingPermitted) &&(study.status.available)}">
        		    <a href="#"
				    onMouseDown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');"
				    onMouseUp="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');"
                    onclick="checkCRFLocked('<c:out value="${dec.eventCRF.id}"/>', 'AdministrativeEditing?eventCRFId=<c:out value="${dec.eventCRF.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}');">
				    <img name="bt_EnterData1" src="images/bt_EnterData.gif" border="0" alt="<fmt:message key="administrative_editing" bundle="${resword}"/>" title="<fmt:message key="administrative_editing" bundle="${resword}"/>" align="left" hspace="6">
				    </a>
                </c:if>
			<%-- locked status here --%>
    			<c:if test="${dec.locked || dec.eventCRF.status.locked || dec.stage.locked || currRow.bean.studyEvent.subjectEventStatus.locked}">
							<img name="itemForSpace" src="images/bt_EnterData.gif" border="0" style="visibility:hidden"  align="left" hspace="6">
		    	</c:if>
    		</c:when>
			<c:otherwise>
			<img name="itemForSpace" src="images/bt_EnterData.gif" border="0" style="visibility:hidden"  align="left" hspace="6">
			</c:otherwise>
		</c:choose>
		</td>
	     <td>

		 <a href="ViewSectionDataEntry?ecId=<c:out value="${dec.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>&tabId=1&studySubjectId=<c:out value="${studySub.id}"/>&exitTo=ViewStudySubject?id=${studySub.id}"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');"><img
		    name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>

		 </td>
		 <td>
		 <a href="javascript:openDocWindow('PrintDataEntry?ecId=<c:out value="${dec.eventCRF.id}"/>')"
			onMouseDown="javascript:setImage('bt_Print1','images/bt_Print_d.gif');"
			onMouseUp="javascript:setImage('bt_Print1','images/bt_Print.gif');"><img
		    name="bt_Print1" src="images/bt_Print.gif" border="0" alt="<fmt:message key="print" bundle="${resword}"/>" title="<fmt:message key="print" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td>
		<c:choose>
		<c:when test="${!dec.eventCRF.status.deleted}">
		 <c:if test="${userRole.manageStudy && study.status.available}">
		  <td><a href="RemoveEventCRF?action=confirm&id=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
			onMouseDown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');"
			onMouseUp="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><img
			name="bt_Remove1" src="images/bt_Remove.gif" border="0" alt="<fmt:message key="remove" bundle="${resword}"/>" title="<fmt:message key="remove" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td>
		 </c:if>
		</c:when>
		<c:otherwise>
		 <c:if test="${(userRole.manageStudy && currRow.bean.studyEvent.status.name!='auto-removed' && dec.eventCRF.status.name != 'auto-removed') && (study.status.available)}">
		  <td><a href="RestoreEventCRF?action=confirm&id=<c:out value="${dec.eventCRF.id}"/>&studySubId=<c:out value="${studySub.id}"/>"
			onMouseDown="javascript:setImage('bt_Restor3','images/bt_Restore_d.gif');"
			onMouseUp="javascript:setImage('bt_Restore3','images/bt_Restore.gif');"><img
			name="bt_Restore3" src="images/bt_Restore.gif" border="0" alt="<fmt:message key="restore" bundle="${resword}"/>" title="<fmt:message key="restore" bundle="${resword}"/>" align="left" hspace="6"></a>
		  </td>
		 </c:if>
		</c:otherwise>
		</c:choose>
         <c:if test="${(!(currRow.bean.studyEvent.status.name!='auto-removed' && dec.eventCRF.status.name != 'auto-removed') && (study.status.available))}">
		 <td>
		<img name="itemForSpace" src="images/bt_EnterData.gif" border="0" style="visibility:hidden"  align="left" hspace="6">
		</td>
		</c:if>
		<c:if test="${(userBean.sysAdmin) && (study.status.available) && (dec.eventCRF.status.name != 'completed')}">
		<td>
		 <a href="DeleteEventCRF?action=confirm&ssId=<c:out value="${studySub.id}"/>&ecId=<c:out value="${dec.eventCRF.id}"/>"
			onMouseDown="javascript:setImage('bt_Delete1','images/bt_Delete_d.gif');"
			onMouseUp="javascript:setImage('bt_Delete1','images/bt_Delete.gif');"><img
		    name="bt_Delete1" src="images/bt_Delete.gif" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>" title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"></a>
		 </td>
		 </c:if>
		 <c:if test="${(dedc.eventCRF.id>0 and !dedc.eventCRF.notStarted) && (userBean.sysAdmin || (userRole.studyDirector || userRole.studyAdministrator)) && (dec.eventCRF.status.name != 'not_started') &&
 (study.status.available || study.status.pending)
 && !(currRow.bean.studyEvent.subjectEventStatus.locked || currRow.bean.studyEvent.subjectEventStatus.skipped) }">
   		<td>
   		
    	<a href="pages/managestudy/chooseCRFVersion?crfId=<c:out value="${dec.eventCRF.crf.id}" />&crfName=<c:out value="${dec.eventCRF.crf.name}" />&crfversionId=<c:out value="${dec.eventCRF.crfVersion.id}" />&crfVersionName=<c:out value="${dec.eventCRF.crfVersion.name}" />&studySubjectLabel=<c:out value="${studySub.label}"/>&studySubjectId=<c:out value="${studySub.id}"/>&eventCRFId=<c:out value="${dec.eventCRF.id}"/>&eventDefinitionCRFId=<c:out value="${dec.eventDefinitionCRF.id}"/>"
   			onMouseDown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');"
   			onMouseUp="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><img
      		name="Reassign" src="images/bt_Reassign.gif" border="0" alt="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" title="<fmt:message key="reassign_crf_version" bundle="${resword}"/>" align="left" hspace="6"></a>
    	</td>
   		</c:if>
		 
		 
		</tr>
		</table>
	   </td>
	 </tr>
    </c:when>
    </c:choose>
	</c:forEach>
    </table>
	</c:otherwise>
    </c:choose>
        </td>

    </tr>