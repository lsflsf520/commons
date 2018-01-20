package com.ujigu.secure.upfile.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.baidu.ueditor.ActionEnter;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.upfile.service.ProjUploadService;
import com.ujigu.secure.web.util.WebUtils;

@Controller
@RequestMapping("/ueditor")
public class UEditorController {
	
	private final static Logger LOG = LoggerFactory.getLogger(UEditorController.class);

	@RequestMapping(value="/ctrl")
	public void ctrl(HttpServletRequest request, HttpServletResponse response){
		response.addHeader("Access-Control-Allow-Origin", "*");  
        response.addHeader("Access-Control-Allow-Headers", "x-requested-with,X-Requested-With,X_Requested_With,x_requested_with");
        
		String rootPath = BaseConfig.getValue("ueditor.root.path");
		String result = new ActionEnter( request, rootPath ).exec();
		
//		WebUtils.writeJson(result, request, response);
		writeResult(response, result);
	}
	
	@RequestMapping(value="/upfile")
	@ResponseBody
	public Map<String, Object> upfile(String file, String base64Code){
		Map<String, Object> retMap = new HashMap<>();
		if(StringUtils.isBlank(file) || StringUtils.isBlank(base64Code)){
			retMap.put("code", "ILLEGAL_PARAM");
			retMap.put("errorMsg",  "参数不能为空");
//			return new ResultModel("ILLEGAL_PARAM", "参数不能为空");
			return retMap;
		}
		String baseDir = ProjUploadService.getBaseStoreDir();
		if(file.contains("\\")){
			file = file.replace("\\", "/");
		}
//		ResultModel resultModel = ResultModel.buildMapResultModel();
		String localfile = file.startsWith("/") ? baseDir + file : baseDir + "/" + file;
		byte[] bytes = Base64.decodeBase64(base64Code);
		try {
			File targetFile = new File(localfile);
			FileUtils.writeByteArrayToFile(targetFile, bytes);
			retMap.put("code", "SUCCESS");
			retMap.put("size", bytes.length);
			retMap.put("title", targetFile.getName());
			retMap.put("url", ProjUploadService.getAccessDomain() + file);
		} catch (IOException e) {
			LOG.error("file " + file + ",base64Code:" + base64Code + "errorMsg:" + e.getMessage(), e);
			retMap.put("code", "ERR_SAVE_FILE");
			retMap.put("errorMsg",  e.getMessage());
//			return new ResultModel("ERR_SAVE_FILE", e.getMessage());
			return retMap;
		}
		
		return retMap;
	}
	
	private void writeResult(HttpServletResponse response, String result){
		try {
			response.getWriter().write(result);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
	
}
