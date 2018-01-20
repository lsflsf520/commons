package com.ujigu.secure.test;

import java.util.HashMap;
import java.util.Map;

import com.ujigu.secure.upfile.util.PathUtils;
import com.ujigu.secure.web.util.RestClientUtil;

public class EmailTest {

	public static void main(String[] args) {
		/*Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", "bx_reg");
		formParams.put("apiuser", "postmaster@passport-shangxueba.sendcloud.org");
		formParams.put("tos", "lsflsf520@126.com");
		formParams.put("title", "您有待支付的保险订单");
		formParams.put("tmplId", "csaimaibaoxian_tixing");
		
		formParams.put("subMap['%username%']", "尚方宝剑");
		formParams.put("subMap['%orderdate%']", "2017-6-9 12:12:12");
		formParams.put("subMap['%productname%']", "安心无忧 平安出行保险");
		formParams.put("subMap['%paymoney%']", "18.8");
		formParams.put("subMap['%expdate%']", "2017-7-9 12:12:12");
		formParams.put("subMap['%premium%']", "18.8");

//		String result = RestClientUtil.doPost("http://upfile-test.baoxianjie.net/email/tmpl/send.do", formParams, String.class);
		
//		Map<String, String> headers = new HashMap<>();
//		headers.put("X-Requested-With", "XMLHttpRequest");
		
		String result = RestClientUtil.doPost("http://upfile-test.baoxianjie.net/email/tmpl/send.do", formParams, String.class);
//		String result = RestClientUtil.doPost("http://localhost:8282/email/asyncsend.do", formParams, String.class);

		System.out.println(result);*/
		
		
		
		/*Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", "bx_find_passwd");
		formParams.put("apiuser", "postmaster@passport-shangxueba.sendcloud.org");
		formParams.put("tos", "lsflsf520@126.com");
		formParams.put("title", "密码找回邮件");
		formParams.put("content", "<p>您好，%username%, 请点击如下链接重置密码：<a href='%resetpwdurl%'>%resetpwdurl%</a></p>");
		
		formParams.put("subMap['%username%']", "尚方宝剑");
		formParams.put("subMap['%resetpwdurl%']", "http://baoxian.csaimall.com/resetpwd.html?id=%userId%");
		formParams.put("sectionMap['%userId%']", "123456");
		
//		String result = RestClientUtil.doPost("http://localhost:8282/email/content/send.do", formParams, String.class);
		String result = RestClientUtil.doPost("http://localhost:8282/email/asyncsend.do", formParams, String.class);

		System.out.println(result);*/

		System.out.println(PathUtils.isRightDir("aB45-d_c"));

	}

}
