<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.ptc.com/windchill/taglib/components" prefix="jca"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@taglib prefix="wctags" tagdir="/WEB-INF/tags" %>
<%@include file="/netmarkets/jsp/util/begin.jspf"%>

<%-->Set the table parameters<--%>
<jsp:include page="${mvc:getComponentURL('TZXZBHTable')}"/>

<%--<mvc:tableContainer compId="TZXZBHTable" height="600" />--%>
<table>
	<tr>驳回原因:</tr>
	<tr>
		<textarea name="bhyy" style="width:360px;height:80px;"></textarea>
	</tr>
</table>

<%@ include file="/netmarkets/jsp/util/end.jspf"%>