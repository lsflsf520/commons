<%@ page language="java" pageEncoding="UTF-8" %>
<html>
  <head>
  
  </head>

  <body>
    <!-- common upload form template -->
    <form action="/image/upload.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="module" value="site">
      <input type="hidden" name="base64" value="true"> <!-- 可选 -->

      <input type="submit" value="传图片">
    </form>
    
    <form action="/image/upload.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="cutpx" value="20,20,100,100">
      <input type="hidden" name="module" value="cut">

      <input type="submit" value="图片裁切">
    </form>
    
    <form action="/image/upload.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="module" value="water">

      <input type="submit" value="加水印">
    </form>
    
    <form action="/file/upload.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="module" value="file">
      <input type="hidden" name="prefix" value="xxoo">

      <input type="submit" value="传文件">
    </form>
    
    <form action="/file/multi/upload.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile1">
      <input type="file" name="upfile2">
      <input type="hidden" name="module" value="file">

      <input type="submit" value="多文件上传">
    </form>


    <!-- head image upload form template -->
    <form action="/image/upload.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="module" value="primHeadImg">

      <input type="submit" value="传头像第一步">
    </form>

    <form action="/image/saveRuleImg.do" method="post">
      <!-- <mlcs:imgSec formName="form3" signStr="projectName=headImg,ruleId=123456"  /> -->
      primUri:<input type="text" name="primUri" value="">
      <!-- primSign:<input type="text" name="primSign" value=""> -->
      <input type="hidden" name="cutpx" value="20,20,100,100">
      <input type="hidden" name="module" value="headImg">
      <input type="hidden" name="ruleId" value="123456">

      <input type="submit" value="传头像第二步">
    </form>
    
  </body>
  
</html>