package com.ujigu.secure.upfile.service.impl;


import java.awt.Dimension;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.util.ImageShellUtil;

/**
 * 
 * @author lsf
 *
 */
public class HRatioScaleUploadService extends CompressionUploadService{
	
	@Override
	public RetFileInfo handleReturnImg(String localFilePath,
			String module, HttpServletRequest request) {
		RetFileInfo imgInfo = super.handleReturnImg(localFilePath, module, request);
		String showSizeStr = getShowSize(module);
		
		String convertName = handleForSize(localFilePath, showSizeStr, module);
		if(StringUtils.isNotBlank(convertName)){
			imgInfo.setAccessUri(getAccessUri(localFilePath, convertName));
		}
		
		return imgInfo;
	}
	
	@Override
	protected String handleForSize(String localFilePath, String sizeStr,
			String module) {
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
	
}
