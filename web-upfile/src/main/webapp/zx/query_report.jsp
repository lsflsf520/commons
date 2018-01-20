<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%

    request.setCharacterEncoding( "utf-8" );
	response.setHeader("Content-Type" , "text/html");
	
%>

<html>
   <head>
     <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
     <link href="http://static-test.csai.cn/common/plugins/layui/css/layui.css" rel="stylesheet" />
	 <script src="http://static-test.csai.cn/common/plugins/layui/layui.js"></script>
	 <script src="http://static-test.csai.cn/common/js/jquery-1.11.2.min.js"></script>
	 <script src="http://static-test.csai.cn/common/js/csai/csai-msg-util.js?_=12"></script>
	 <script src="http://static-test.csai.cn/common/js/csai/csai-net-util.js?_=12"></script>
     <title>个人征信报告查询</title>
   </head>

   <body>
      <!-- <script>alert("哈哈，笨蛋！")</script> -->
      <!-- "><script>alert("哈哈，笨蛋！")</script>"<br/ -->
      <!-- ';alert("哈哈，笨蛋！");var x=' -->
      <!--  -->
   
      <form id="dataForm" method="post">
                            验证码：<input name="tradeCode" id="tradeCode" type="text" >
        
        <input type="button" onclick="dosubmit();" value="查询">
        <input type="button" onclick="sendCode();" value="没收到验证码，重新发送">
      </form>
      
      <script type="text/javascript">
        $(document).ready(function(){
    		layui.use(['form'], function(){
    			form=layui.form();
    		});
        });
        
        function dosubmit(){
        	NetUtil.ajaxload("/tools/zhengxin/queryreport.do", "#dataForm", function(result){
        		alert(result.model);
        	});
        }
      </script>

   </body>
</html>