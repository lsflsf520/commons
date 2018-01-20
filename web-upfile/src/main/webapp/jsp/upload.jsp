<%@ page language="java" pageEncoding="UTF-8" %>
<html>
  <head>
  
  </head>

  <body>
    <!-- common upload form template -->
    <form action="/file/uploadImg.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="upfileElemName" value="upfile">
      <input type="hidden" name="module" value="admin">
      <input type="hidden" name="base64" value="true">

      <input type="submit" value="传图片">
    </form>
    
    <form action="/file/uploadfile.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="upfileElemName" value="upfile">
      <input type="hidden" name="module" value="admin">

      <input type="submit" value="传普通文件">
    </form>


    <!-- head image upload form template -->
    <form action="/file/uploadImg.do" method="post" enctype="multipart/form-data">
      <input type="file" name="upfile">
      <input type="hidden" name="upfileElemName" value="upfile">
      <input type="hidden" name="module" value="primHeadImg">

      <input type="submit" value="传头像第一步">
    </form>

    <form action="/file/saveRuleImg.do" method="post">
      <mlcs:imgSec formName="form3" signStr="projectName=headImg,ruleId=123456"  />
      primUri:<input type="text" name="primUri" value="">
      primSign:<input type="text" name="primSign" value="">
      <input type="hidden" name="cutpx" value="0,0,100,100">
      <input type="hidden" name="module" value="headImg">
      <input type="hidden" name="ruleId" value="123456">

      <input type="submit" value="传头像第二步">
    </form>
    
  </body>
  
</html>