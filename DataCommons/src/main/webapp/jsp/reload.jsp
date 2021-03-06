<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="anu" uri="http://www.anu.edu.au/taglib"%>

<anu:header id="1998" title="ANU Data Commons - Reload" description="description" subject="subject" respOfficer="Doug Moncur" respOfficerContact="mailto:doug.moncur@anu.edu.au"
	ssl="true">

	<link rel="stylesheet" type="text/css" href="<c:url value='/css/jquery-ui-1.8.20.custom.css' />" />
	<link rel="stylesheet" type="text/css" href="<c:url value='/css/default.css' />" />
	<script type="text/javascript" src="<c:url value='/js/report.js' />"></script>
	<script type="text/javascript" src="<c:url value='/js/jquery-ui-1.8.20.custom.min.js' />"></script>
</anu:header>

<jsp:include page="/jsp/header.jsp" />

<anu:content layout="doublenarrow" title="Reports">
	<a href='<c:url value="/rest/report/reload" />'>Reload Reports</a><br/>
	<a href='<c:url value="/rest/report/auto/reload" />'>Reschedule All Automated Reports</a>
</anu:content>

<jsp:include page="/jsp/footer.jsp" />