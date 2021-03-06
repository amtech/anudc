<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="anu" uri="http://www.anu.edu.au/taglib"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

<c:set var="layoutstyle" value="doublenarrow" />
<c:if test="${empty it.sidepage}">
	<c:set var="layoutstyle" value="doublewide" />
</c:if>
<anu:content layout="${layoutstyle}">
	<sec:accesscontrollist hasPermission="READ,WRITE,REVIEW,PUBLISH,ADMINISTRATION" domainObject="${it.fedoraObject}">
		<c:if test="${not empty it.fedoraObject.reviewReady}">
			<anu:message type="info">
				Ready for Review
			</anu:message>
			<br />
		</c:if>
		<c:if test="${not empty it.fedoraObject.publishReady}">
			<anu:message type="info">
				Ready for Publishing
			</anu:message>
			<br />
		</c:if>
		<c:if test="${not empty it.fedoraObject.reviewReject}">
			<anu:message type="info">
				<strong>Rejection Reasons:</strong><br />
				${it.fedoraObject.reviewReject.reason}
			</anu:message>
			<br />
		</c:if>
	</sec:accesscontrollist>
<c:out escapeXml="false" value="${it.page}" />

<input type="hidden" readonly="readonly" name="itemType" id="itemType" value="${it.itemType}" />
</anu:content>