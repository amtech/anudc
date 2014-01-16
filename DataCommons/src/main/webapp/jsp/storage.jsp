<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="anu" uri="http://www.anu.edu.au/taglib"%>

<anu:header id="1998" title="${it.fo.object_id}" description="DESCRIPTION" subject="SUBJECT" respOfficer="Doug Moncur"
	respOfficerContact="doug.moncur@anu.edu.au" ssl="true">
	<!-- Possible bug in the ANU taglib. The following CSS should not be referenced here. Should be referenced in the taglib. -->
	<link href="//styles.anu.edu.au/_anu/3/style/anu-forms.css" rel="stylesheet" type="text/css" />
	<link href="<c:url value='/css/storage.css' />" rel="stylesheet" type="text/css" />
	<script src="//crypto-js.googlecode.com/svn/tags/3.1.2/build/rollups/md5.js"></script>
	<script src="//crypto-js.googlecode.com/svn/tags/3.1.2/build/components/lib-typedarrays-min.js"></script>
	<script type="text/javascript" src="<c:url value='/js/storage.js' />"></script>
	<script type="text/javascript">
		jQuery(document).ready(function() {
			documentReady();
		});
	</script>
</anu:header>

<jsp:include page="/jsp/header.jsp" />

<anu:content layout="doublewide" title="Data Files [${it.fo.object_id}]">
	<jsp:include page="/jsp/statusmessages.jsp">
		<jsp:param value="${it}" name="it" />
	</jsp:include>
	<img id="loading" src="<c:url value='/images/ajax-loader.gif' />" style="display: none"></img>

	<div id="tabs" class="pagetabs-nav nopadbottom">
		<ul>
			<li><a href="#files">Files</a></li>
			<li><a href="#info">Archive Info</a></li>
			<li><a href="#extRefs">External References</a></li>
			<sec:authorize access="isAuthenticated()">
				<sec:accesscontrollist hasPermission="WRITE,ADMINISTRATION" domainObject="${it.fo}">
					<li><a href="#uploadFiles">Upload Files</a></li>
				</sec:accesscontrollist>
			</sec:authorize>
		</ul>
	</div>
</anu:content>

<div class="doublewide nopadtop" id="files" class="list_view">
	<c:choose>
		<c:when test="${it.fileList != null}">
			<!-- Navigation Breadcrumbs -->
			<div class="left marginbottom" >
				<c:forEach var="iParent" items="${it.fileList.parents}" varStatus="stat">
					<a class="large" href="<c:url value='${iParent.uri}' />"><c:out value="${iParent.filename}" /></a>
					<c:if test="${!stat.last}">
						&nbsp;&gt;
					</c:if>
				</c:forEach>
			</div>
			
			<!-- Actions -->
			<div id="div-action-icons" class="right">
				<sec:authorize access="isAuthenticated()">
					<sec:accesscontrollist hasPermission="WRITE,ADMINISTRATION" domainObject="${it.fo}">
						<img id="action-create-folder" class="clickable-icon" src="<c:url value='/images/folder-new.png' />" onclick="createDir();"></img>
						<img id="action-del-selected" class="clickable-icon disabled" src="<c:url value='/images/delete_red.png' />" onclick="deleteSelected();"></img>
					</sec:accesscontrollist>
				</sec:authorize>
			</div>

			<table class="w-doublewide tbl-row-bdr noborder anu-long-area tbl-files">
				<tr class="anu-sticky-header">
					<th class="col-checkbox"><input type="checkbox" onchange="toggleCheckboxes(this);" /></th>
					<th class="col-filename">Name</th>
					<th class="col-filetype">Type</th>
					<th class="col-filesize">Size</th>
					<th class="col-action-icons">&nbsp;</th>
				</tr>
				
				<c:forEach var="iFile" items="${it.fileList.files}" varStatus="stat">
					<tr id="filerow-${stat.count}" class="file-row">
						<!-- Selection checkbox. -->
						<td class="col-checkbox"><input type="checkbox" value="${iFile.uri}" onclick="condEnableSelTasks()" /></td>
						
						<!-- Icon and filename as hyperlink -->
						<td class="col-filename"><a class="nounderline" href="<c:url value='${iFile.uri}' />"> <c:choose>
									<c:when test="${iFile.type == 'DIR'}">
										<img src="//styles.anu.edu.au/_anu/images/icons/web/folder.png" onmouseover="this.src='//styles.anu.edu.au/_anu/images/icons/web/folder-over.png'"
											onmouseout="this.src='//styles.anu.edu.au/_anu/images/icons/web/folder.png'" />
									</c:when>
									<c:when test="${iFile.type == 'FILE'}">
										<img src="//styles.anu.edu.au/_anu/images/icons/web/paper.png" onmouseover="this.src='//styles.anu.edu.au/_anu/images/icons/web/paper-over.png'"
											onmouseout="this.src='//styles.anu.edu.au/_anu/images/icons/web/paper.png'" />
									</c:when>
									<c:otherwise>
										<c:out value='${iFile.type}' />
									</c:otherwise>
								</c:choose>
						<c:out value="${iFile.filename}" /></a></td>

						<td class="col-filetype">
							<c:out value="${iFile.type}" />
						</td>
						
						<td class="col-filesize">
							<c:out value="${iFile.friendlySize}" />
						</td>
						
						<td class="col-action-icons">
							<!-- Delete icon -->
							<sec:authorize access="isAuthenticated()">
								<sec:accesscontrollist hasPermission="WRITE,ADMINISTRATION" domainObject="${it.fo}">
									<a href="javascript:void(0);" onclick="deleteFile('${iFile.uri}')">
										<img src="<c:url value='/images/delete_red.png' />" width="24" height="24" title="Delete ${iFile.filename}" />
									</a>
								</sec:accesscontrollist>
							</sec:authorize>
							
							<!-- Expand -->
							<span onclick="jQuery('#filerow-extra-${stat.count}').slideToggle();">Expand</span>
						</td>
					</tr>
					
					<!-- Additional file details -->
					<tr id="filerow-extra-${stat.count}" class="file-row-extra" style="display: none">
						<td colspan="0">Test</td>
					</tr>
				</c:forEach>
			</table>
		</c:when>
	</c:choose>

	<!-- Drag n Drop -->
	<div id="dragandrophandler" class="w-doublewide">Drag &amp; Drop Files Here</div>
</div>

<sec:authorize access="isAuthenticated()">
	<sec:accesscontrollist hasPermission="WRITE,ADMINISTRATION" domainObject="${it.fo}">
		<div class="doublewide nopadtop" id="uploadFiles" style="display: none;">
			<p class="msg-info">
				The Java upload applet below may take a few moments to display. When it does, either drag and drop files from your system into the applet, or click on the <em>Browse</em>
				button to select files from a dialog box.
			</p>
			<form class="anuform" name="uploadForm" id="idUploadForm" enctype="multipart/form-data" method="post" action="/">
				<applet code="wjhk.jupload2.JUploadApplet.class" name="JUpload" archive="<c:url value='/plugins/jupload-5.0.8.jar' />" width="680" height="500" mayscript
					alt="The java plugin must be installed.">
					<param name="postURL" value="<c:url value='${it.fileList.uri};jsessionid=${cookie.JSESSIONID.value}?src=jupload' />" />
					<param name="stringUploadSuccess" value="^SUCCESS$" />
					<param name="stringUploadError" value="^ERROR: (.*)$" />
					<param name="stringUploadWarning" value="^WARNING: (.*)$" />
					<param name="debugLevel" value="1" />
					<param name="maxChunkSize" value="10485760" />
					<param name="lang" value="en" />
					<param name="formdata" value="uploadForm" />
					<param name="showLogWindow" value="false" />
					<param name="showStatusBar" value="true" />
					<param name="sendMD5Sum" value="true" />
					<param name="readCookieFromNavigator" value="true" />
					<param name="type" value="application/x-java-applet;version=1.6">
					<param name="afterUploadURL" value="<c:url value='${it.fileList.uri}' />" />
					This Java Applet requires Java 1.6 or higher.
				</applet>
			</form>
		</div>
	</sec:accesscontrollist>
</sec:authorize>


<jsp:include page="/jsp/footer.jsp" />