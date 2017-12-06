<%@ page contentType="text/html; charset=UTF-8"%>
<%@page import="ext.yhzc.report.ExportSystemObject"%>
<%@ include file="/netmarkets/jsp/util/beginPopup.jspf"%>
<%
String path = ExportSystemObject.exportAllPart();
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    
    <title>ExportAllParts</title>
    
	

  </head>
  
  <body>
  	<center><a href="temp/<%=path%>">导出系统中所有部件信息</a></center>
    <br>
  </body>
</html>
<%@ include file="/netmarkets/jsp/util/end.jspf"%>