package com.ujigu.secure.email.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.web.util.RestClientUtil;

@Controller
@RequestMapping("/msg/test")
public class MsgTestController {
	
	@RequestMapping("email")
	@ResponseBody
	public ResultModel testEmail(String email){
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", "bx_reg");
		formParams.put("apiuser", "postmaster@passport-shangxueba.sendcloud.org");
		formParams.put("tos", StringUtils.isBlank(email) ? "lsflsf520@126.com" : email);
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
		
		String result = RestClientUtil.doPost(GlobalConstant.UPFILE_DOMAIN + "/email/tmpl/send.do", formParams, String.class);
		
		return new ResultModel(result);
	}
	
	@RequestMapping("sms")
	@ResponseBody
	public ResultModel testSms(String phone){
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", "bx_reg");
		formParams.put("appId", "8aaf07085b228d32015b22a763c50005");
		formParams.put("phone", StringUtils.isBlank(phone) ? "17773132069" : phone);
		formParams.put("tmplId", "164388");
		formParams.put("params", "尚方宝剑,2");
		
//		String result = RestClientUtil.doPost("http://upfile-test.baoxianjie.net/sms/send.do", formParams, String.class);
		String result = RestClientUtil.doPost(GlobalConstant.UPFILE_DOMAIN + "/sms/asyncsend.do", formParams, String.class);
		
		return new ResultModel(result);
	}

}
