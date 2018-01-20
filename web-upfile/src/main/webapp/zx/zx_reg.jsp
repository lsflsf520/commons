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
        <input type="hidden" name="token" id="token">
	         姓名：<input name="name" id="name" placeholder="请输入您的真实姓名，例如：“张三”。" type="text" maxlength="30"> <br/>
	      证件类型： <select name="certType" id="certType">
	        </select> <br/>
	   证件号码：<input name="certNo" id="certNo" placeholder="请输入您的真实姓名，例如：“张三”。" type="text" maxlength="30"><br/>
	      验证码：  <input name="imgCode" id="valImgCode" type="text" > <img id="codeImg" src="" onclick="refreshImgCode();"/> <br/>
        <input type="button" onclick="dosubmit();" value="下一步">
        <!-- <input type="button" onclick="sendCode();" value="没收到验证码，重新发送"> -->
      </form>
      
      <script type="text/javascript">
       $(document).ready(function(){
  		  layui.use(['form'], function(){
  			form=layui.form();
  		  });
  		
	      NetUtil.ajaxload("/tools/zhengxin/regparam.do", function(result){
	      	$("#codeImg").attr("src", result.model.codeImg);
	      	$("#token").val(result.model.token);
	      	
	      	var options = "";
	      	$.each(result.model.certTypeMap, function(key, value){
	      		options += "<option value='" + key +"'>"+value+"</option>";
	      	});
	      	
	      	$("#certType").html(options);
	      });
       });
       
       function refreshImgCode(){
    	   NetUtil.ajaxload("/tools/zhengxin/reloadimgcode.do", function(result){
    	     $('#codeImg').attr('src', result.model);
    	   });
       }
        
        function dosubmit(){
        	NetUtil.ajaxload("/tools/zhengxin/doreg.do", "#dataForm", function(result){
        		location.href = "/zx/zx_reg_other.jsp?token=" + result.model.token;
        	}, function(flag, result){
        		if(!flag){
        			if(result.extraInfoMap){
        				if(result.extraInfoMap.token){
        					$("#token").val(result.extraInfoMap.token);
        				}
        				if(result.extraInfoMap.codeImg){
        					$("#codeImg").attr("src", result.extraInfoMap.codeImg);
        				}
        			}
        			
        			MsgUtil.error(result.resultMsg);
        		}
        	});
        }
      </script>

   </body>
</html>