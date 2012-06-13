<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="anu" uri="http://www.anu.edu.au/taglib"%>

<anu:header id="1998" title="Search" description="description" subject="subject" respOfficer="Doug Moncur" respOfficerContact="mailto:doug.moncur@anu.edu.au"
	ssl="true">
	<!-- Possible bug in the ANU taglib. The following CSS should not be referenced here. Should be referenced in the taglib. -->
	<link href="http://styles.anu.edu.au/_anu/3/style/anu-forms.css" rel="stylesheet" type="text/css" />
</anu:header>

<jsp:include page="/jsp/header.jsp" />

<anu:content layout="doublewide" title="Search">
	<div id="divBasicSearch">
		<jsp:include page="/jsp/searchbox.jsp" />
	</div>
	<div id="divSearchResults">
		<c:if test="${it.resultSet != null}">
			<hr />
			${it.resultSet.numFound} results found <br /><br />
			<c:forEach items="${it.resultSet.documentList}" var="row">
				<a href="<c:url value="/rest/display?layout=def:display&item=${row['id']}" />"><c:out value="${row['unpublished.name']}" /></a> <br />
				${row['unpublished.briefDesc'][0]} <br />
			</c:forEach>
			<br />
			
			<fmt:bundle basename='global'>
				<fmt:message var="searchItemsPerPage" key='search.resultsPerPage' />
			</fmt:bundle>
			<c:set var="curPage" value="${(param.offset == null ? 0 : param.offset) / searchItemsPerPage + 1}" />
			<p class="text-centre">
				<c:if test="${it.resultSet.numFound > 0}">
					Pages&nbsp;
					
					<c:forEach begin="0" end="${((it.resultSet.numFound - 1) / searchItemsPerPage) - (((it.resultSet.numFound - 1) / searchItemsPerPage) % 1)}" var="i">
						<c:url var="searchURL" value='/rest/search'>
							<c:param name='q' value='${param.q}' />
							<c:param name='offset' value='${i * searchItemsPerPage}' />
							<c:param name='limit' value='${searchItemsPerPage}' />
						</c:url>
						<a class="nounderline"
							href="${searchURL}">
							<c:if test="${i == curPage - 1}">
								<strong>
							</c:if>
							<c:out value="[ ${i + 1} ]" />
							<c:if test="${i == curPage - 1}">
								</strong>
							</c:if>
						</a>
					</c:forEach>
				</c:if>
			</p>
		</c:if>
	</div>
</anu:content>

<jsp:include page="/jsp/footer.jsp" />
