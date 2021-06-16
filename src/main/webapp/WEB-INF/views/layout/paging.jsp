<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:if test="${not empty paging.search }">
	<c:set var="searchParam" value="&search=${paging.search }" />
</c:if>

<c:if test="${not empty paging.category }">
	<c:set var="categoryParam" value="&category=${paging.category }" />
</c:if>

<c:if test="${not empty categoryArr }">
	<c:set var="mycategoryParam">
		<c:forEach var="mycategory" items="${categoryArr }" varStatus="mycStatus">
			&category=${categoryArr[mycStatus.index] }
		</c:forEach>
	</c:set>
</c:if>



<div class="paging text-center">
	<ul class="pagination">

		<%-- 처음 페이지 버튼 --%>
		<%-- 첫 페이지가 아닐 때 버튼 노출 --%>
		<c:if test="${paging.curPage ne 1 }">
		  <li><a href="${contextPath}?curPage=1${categoryParam}${searchParam }${mycategoryParam }"><span>&larr;</span></a></li>
		</c:if>





		<%-- 이전 페이지 버튼 --%>
		<%-- 첫 페이지면 금지 표시 --%>
		<c:if test="${paging.curPage ne 1 }">
		   <li><a href="${contextPath}?curPage=${paging.curPage-1 }${categoryParam}${searchParam}${mycategoryParam }">&laquo;</a></li>
		</c:if>
		<c:if test="${paging.curPage eq 1 }">
			<li class="disabled"><span>&laquo;</span></li>
		</c:if>





		<%-- 페이징 번호 표시 --%>
		<%-- 현재 페이지 번호는 active 클래스 부여 -> 파랑 바탕 버튼 --%>
		<c:forEach begin="${paging.startPage }" end="${paging.endPage }" var="page">
			<c:if test="${paging.curPage eq page }">
				<li class="active"><a href="${contextPath}?curPage=${page }${categoryParam}${searchParam }${mycategoryParam }">${page }</a></li>
			</c:if>
			<c:if test="${paging.curPage ne page }">
				<li><a href="${contextPath}?curPage=${page }${categoryParam}${searchParam }${mycategoryParam }">${page }</a></li>
			</c:if>
		</c:forEach>





		<%-- 다음 페이지 버튼 --%>
		<%-- 마지막 페이지면 동작 안함 --%>
		<c:if test="${paging.curPage ne paging.totalPage }">
			<li><a href="${contextPath}?curPage=${paging.curPage+1 }${categoryParam}${searchParam }${mycategoryParam }"><span>&raquo;</span></a></li>
		</c:if>
		<c:if test="${paging.curPage eq paging.totalPage }">
			<li class="disabled"><span>&raquo;</span></li>
		</c:if>
		
		
		
		
		<%-- 마지막 페이지 버튼 --%>
		<%-- 마지막 페이지가 아닐 때 버튼 노출 --%>
		<c:if test="${paging.curPage ne paging.totalPage }">
			<li><a href="${contextPath}?curPage=${paging.totalPage }${categoryParam}${searchParam }${mycategoryParam }"><span>&rarr;</span></a></li>
		</c:if>
	</ul>
</div>