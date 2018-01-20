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
   
      <form id="dataForm" action="/tools/zhengxin/dologon.do" method="post">
        <!--<input name="cookie" id="cookie" type="hidden" > -->
         <input name="token" id="token" type="hidden" >
        <input name="date" id="date" type="hidden" >
        登录名：<input name="loginname" id="loginname" type="text" >
       密码： <input name="password" id="password" type="password" >
      验证码：  <input name="imgCode" id="valImgCode" type="text" > <img id="codeImg" src="" />
        <input type="button" onclick="dosubmit();" value="去查询">
      </form>
      
      <script type="text/javascript">
      $(document).ready(function(){
    		layui.use(['form'], function(){
    			form=layui.form();
    		});
    		
	        NetUtil.ajaxload("/tools/zhengxin/loginparam.do", function(result){
	        	//$("#cookie").val(result.model.cookie);
	        	$("#date").val(result.model.date);
	        	$("#token").val(result.model.token);
	        	$("#codeImg").attr("src", result.model.codeImg);
	        });
      });
        
        function dosubmit(){
        	NetUtil.ajaxload("/tools/zhengxin/dologon.do", "#dataForm", function(result){
        		if(result.extraInfoMap && result.extraInfoMap.hasReport){
        			location.href = "/zx/query_report.jsp";
        			return;
        		}
        		if (result.extraInfoMap && result.extraInfoMap.hasApply){
        			MsgUtil.success("申请已提交，请等待央行的短信验证码！");
        		}else {
        			MsgUtil.success("登陆成功，去申请报告");
        		}
        	});
        }
      </script>

   </body>
</html>