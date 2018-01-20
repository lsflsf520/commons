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
        <input type="hidden" name="token" id="token" value="<%=request.getParameter("token") %>">
         <input type="hidden" name="tcId" id="tcId" >
	         登录名：<input name="loginname" id="loginname" placeholder="登录名由6-16位数字、字母、“_”、“-”、“/”组成，不含特殊字符，如：%、@、#、空格等" type="text" maxlength="16"> <br/>
	      密码： <input type="password" name="password" id="pwd" maxlength="20"/> <br/>
	      确认密码： <input type="password" name="cfpasswd" id="cfpwd" maxlength="20"/> <br/>
	  电子 邮箱：<input name="email" id="email" type="text" maxlength="50"><br/>
	   手机号码：<input name="mobileTel" id="mobileTel" type="text" maxlength="11"><br/>
	      验证码：  <input name="verifyCode" id="verifyCode" type="text" maxlength="6"> <input type="button" onclick="sendMsg();" value="获取动态码"> <br/>
	 短信接收时段：<input type="radio" name="smsrcvtimeflag" checked="checked" value="1" />仅在非休息时间发送（7时-23时） <br/>
	       <input class="radio_type2" value="2" name="smsrcvtimeflag" style="margin-left:155px; _margin-left:5px;" type="radio">全天均可发送（0时-24时）
        <input type="button" onclick="dosubmit();" value="下一步">
        <!-- <input type="button" onclick="sendCode();" value="没收到验证码，重新发送"> -->
      </form>
      
      <script type="text/javascript">
       $(document).ready(function(){
  		  layui.use(['form'], function(){
  			form=layui.form();
  		  });
       });
       
       function sendMsg(){
    	   var mobileTel = $("#mobileTel").val();
    	   if(!mobileTel){
    		   MsgUtil.warn("手机号不能为空");
    		   return;
    	   }
    	   NetUtil.ajaxload("/tools/zhengxin/sendphonecode.do?mobileTel=" + mobileTel, function(result){
    	     $('#tcId').val(result.model);
    	   });
       }
        
       function dosubmit(){
        	NetUtil.ajaxload("/tools/zhengxin/doregother.do", "#dataForm", function(result){
        		MsgUtil.success(result.model);
        		setTimeout('location.href="/zx/zx_login.jsp"', 2000);
        	}, function(flag, result){
	       		if(!flag){
	    			if(result.extraInfoMap){
	    				if(result.extraInfoMap.token){
	    					$("#token").val(result.extraInfoMap.token);
	    				}
	    			}
	    			
	    			MsgUtil.error(result.resultMsg);
	    		}
    	   });
       }
      </script>

   </body>
</html>