<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/> 
<fmt:setBundle basename="org.akaza.openclinica.i18n.terms" var="resterm"/>

<jsp:useBean scope="request" id="currRow" class="org.akaza.openclinica.web.bean.StudyUserRoleRow" />

<tr valign="top">   
      <td class="table_cell_left"><c:out value="${currRow.bean.userName}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.firstName}"/></td>  
      <td class="table_cell"><c:out value="${currRow.bean.lastName}"/></td>
      <td class="table_cell">
          <fmt:message key="${roleMap[currRow.bean.role.id] }" bundle="${resterm}"></fmt:message>
      </td>
      <td class="table_cell"><c:out value="${currRow.bean.studyName}"/></td>
      <td class="table_cell"><c:out value="${currRow.bean.status.name}"/></td>
      <td class="table_cell">
       <table border="0" cellpadding="0" cellspacing="0">
		<tr>
		 <td><a href="ViewStudyUser?name=<c:out value="${currRow.bean.userName}"/>&studyId=<c:out value="${currRow.bean.studyId}"/>"
			onMouseDown="javascript:setImage('bt_View1','images/bt_View_d.gif');"
			onMouseUp="javascript:setImage('bt_View1','images/bt_View.gif');">
			<img name="bt_View1" src="images/bt_View.gif" border="0" alt="<fmt:message key="view" bundle="${resword}"/>" title="<fmt:message key="view" bundle="${resword}"/>" align="left" hspace="6"></a>
		</td>      
 
        <c:if test="${!currRow.bean.status.deleted}">
        	<td>
        		<a href="SetStudyUserRole?action=confirm&name=<c:out value="${currRow.bean.userName}"/>&studyId=<c:out value="${currRow.bean.studyId}"/>"
		  			onMouseDown="javascript:setImage('bt_SetRole1','images/bt_SetRole_d.gif');"
		 			onMouseUp="javascript:setImage('bt_SetRole1','images/bt_SetRole.gif');">
		  			<img name="bt_SetRole1" src="images/bt_SetRole.gif" border="0" alt="<fmt:message key="set_role" bundle="${resword}"/>" title="<fmt:message key="set_role" bundle="${resword}"/>" align="left" hspace="6"></a>
			</td>
		</c:if>
		
		<c:if test="${currRow.bean.status.deleted || ((userRolesAvailableCountMap[currRow.bean.userName] > 1) and not ((currRow.bean.userName eq userBean.name) and (currRow.bean.studyId eq userBean.activeStudyId)))}">
			<c:choose>
				<c:when test="${not empty currRow.bean.studyName}">
					<c:set var="study" value="${currRow.bean.studyName}" />
				</c:when>
				<c:otherwise>
					<c:set var="study" value="Study ${currRow.bean.studyId}" />
				</c:otherwise>
			</c:choose>
			
			<c:set var="actionName">
				<fmt:message key="delete" bundle="${resword}"/>
			</c:set>
			
        	<c:set var="confirmQuestion">
            	<fmt:message key="are_you_want_to_the_role_for" bundle="${restext}">
                	<fmt:param value="${actionName}"/>
                    <fmt:param value="${currRow.bean.role.description}"/>
                    <fmt:param value="${study}"/>
               </fmt:message>
         	</c:set>
         	
           	<c:set var="onClick" value="return confirm('${confirmQuestion}');"/>
               
            <c:if test="${currRow.bean.status.deleted}">
            	<td>
            		<img name="spaceIcon" src="images/bt_Restore.gif" style="visibility:hidden;" border="0" align="left" hspace="6">
            	</td>
            </c:if>   
                                    
        	<td>
       		 <a href="DeleteStudyUserRole?studyId=<c:out value="${currRow.bean.studyId}" />&userName=<c:out value="${currRow.bean.userName}"/>&action=3" onClick="<c:out value="${onClick}" />"
                onMouseDown="javascript:setImage('bt_Delete1','images/bt_Delete_d.gif');"
                onMouseUp="javascript:setImage('bt_Delete1','images/bt_Delete.gif');">
                <img name="bt_Delete1" src="images/bt_Delete.gif" border="0" alt="<fmt:message key="delete" bundle="${resword}"/>" title="<fmt:message key="delete" bundle="${resword}"/>" align="left" hspace="6"></a>
        	</td>
		
		</c:if>
	
      </tr>
      </table>
      </td>
   </tr>
   
