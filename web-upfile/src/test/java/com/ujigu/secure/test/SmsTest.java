package com.ujigu.secure.test;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.web.util.RestClientUtil;

public class SmsTest {
	public static void main(String[] args) {
		/*Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", "bx_reg");
		formParams.put("appId", "8aaf07085b228d32015b22a763c50005");
		formParams.put("phone", "17773132069");
		formParams.put("tmplId", "164388");
		formParams.put("params", "尚方宝剑,2");
		
//		String result = RestClientUtil.doPost("http://upfile-test.baoxianjie.net/sms/send.do", formParams, String.class);
		String result = RestClientUtil.doPost("http://localhost:8282/sms/send.do", formParams, String.class);

		System.out.println(result);*/
		
		/*Map<String, Object> formParams = new HashMap<>();
		formParams.put("smsList[0].module", "bx_find_passwd");
		formParams.put("smsList[0].appId", "8aaf07085b228d32015b22a763c50005");
		formParams.put("smsList[0].phone", "17773132069");
		formParams.put("smsList[0].tmplId", "164388");
		formParams.put("smsList[0].params", "尚方宝剑,2");
		
		formParams.put("smsList[1].module", "bx_find_passwd");
		formParams.put("smsList[1].appId", "8aaf07085b228d32015b22a763c50005");
		formParams.put("smsList[1].phone", "15173131671");
		formParams.put("smsList[1].tmplId", "164388");
		formParams.put("smsList[1].params", "王维琦,2");
		
//		String result = RestClientUtil.doPost("http://localhost:8282/sms/batchSend.do", formParams, String.class);
//		String result = RestClientUtil.doPost("http://upfile-test.baoxianjie.net/sms/batchSend.do", formParams, String.class);

		System.out.println(result);*/
		
		/*Map<String, Object> formParams = new HashMap<>();
		formParams.put("module", "bx_reg");
		formParams.put("appId", "8aaf07085b228d32015b22a763c50005");
		formParams.put("phone", "17773132069");
		formParams.put("tmplId", "164388");
		formParams.put("params", "尚方宝剑,2");
		
//		String result = RestClientUtil.doPost("http://upfile-test.baoxianjie.net/sms/send.do", formParams, String.class);
		String result = RestClientUtil.doPost("http://upfile-test.baoxianjie.net/sms/asyncsend.do", formParams, String.class);

		System.out.println(result);*/
		
		/*RetFileInfo fileInfo = new RetFileInfo();
		fileInfo.setAccessDomain("http://upfile-test.baoxianjie.net");
		fileInfo.setAccessUri("/company/xxx/uuu.jpg");
		fileInfo.setWidth(100);
//		ResultModel resultModel = new ResultModel(fileInfo);
//		resultModel.addExtraInfo("base64", "UUUEO*U(");
		
		ResultModel resultModel = ResultModel.buildMapResultModel();
		resultModel.put("isY", false);
		String str = JsonUtil.create().toJson(resultModel);
		System.out.println(str);
		
		ResultModel dr = JsonUtil.create().fromJson(str, ResultModel.class);
//		fileInfo = dr.convertModel2Bean(RetFileInfo.class);
//		System.out.println(fileInfo.getWidth());
		Boolean bool = dr.getBool("isY");
		System.out.println(bool);*/
		
		System.out.println("xx|yy".split("\\|")[0]);
	}

}
