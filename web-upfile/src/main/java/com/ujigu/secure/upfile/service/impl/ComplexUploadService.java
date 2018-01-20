package com.ujigu.secure.upfile.service.impl;

import java.awt.Dimension;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.util.ImageShellUtil;

public class ComplexUploadService extends CompressionUploadService {
	
	@Override
	public RetFileInfo handleReturnImg(String localFilePath,
			String module, HttpServletRequest request) {
		
		RetFileInfo returnImgInfo = super.handleReturnImg(localFilePath, module, request);
		String showSize = getShowSize(module);
		String convertName = handleForSize(localFilePath, showSize, module);
		if (StringUtils.isBlank(convertName)) {
			returnImgInfo.setAccessUri(getAccessUri(localFilePath, convertName));
		}
		
		return returnImgInfo;
	}
	
	@Override
	protected String handleForSize(String localFilePath, String sizeStr,
			String module) {
		String convertName = null;
		if (sizeStr.startsWith("x")) {
			convertName = handleH(localFilePath, sizeStr, module);
		}else if (StringUtils.split(sizeStr, "x").length == 2) {
			convertName = handleWH(localFilePath, sizeStr, module);
		} else {
			convertName = handleW(localFilePath, sizeStr, module);
		}
		
		return convertName;
	}
	
	private String handleWH(String localFilePath, String sizeStr,String module){
		return super.handleForSize(localFilePath, sizeStr, module);
	}
	
	private String handleH(String localFilePath, String sizeStr,String module){
		String convertName = null;
		if (sizeStr.startsWith("x") && RegexUtil.isInt(sizeStr.replace("x", ""))) {
			int height = Integer.valueOf(sizeStr.replace("x", ""));
			
			Dimension dim = ImageShellUtil.getDimension(localFilePath);
			if(compareHeight(dim, height) == 1){
				convertName = ImageShellUtil.ratioScaleImg(localFilePath, 0, height);
			}else{
				convertName = fillBg(localFilePath, module, (int)dim.getWidth(), height, String.valueOf(height));
			}
		}
		
		return convertName;
	}
	
	private String handleW(String localFilePath, String sizeStr,String module){
		String convertName = null;
		if (RegexUtil.isInt(sizeStr)) {
			int width = Integer.valueOf(sizeStr);
			
			Dimension dim = ImageShellUtil.getDimension(localFilePath);
			if(compareWidth(dim, width) == 1){
			    convertName = ImageShellUtil.ratioScaleImg(localFilePath, width, 0);
			}else{
				convertName = fillBg(localFilePath, module, width, (int)dim.getHeight(), String.valueOf(width));
			}
		}
		
		return convertName;
	}
}
