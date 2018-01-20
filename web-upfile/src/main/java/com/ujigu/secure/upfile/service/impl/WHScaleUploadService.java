package com.ujigu.secure.upfile.service.impl;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.upfile.bean.RetFileInfo;

/**
 * 
 * @author lsf
 *
 */
public class WHScaleUploadService extends CompressionUploadService{

	@Override
	public RetFileInfo handleReturnImg(String localFilePath,
			String module, HttpServletRequest request) {
		RetFileInfo imgInfo = super.handleReturnImg(localFilePath, module, request);
		String showSizeStr = getShowSize(module);
		
		String convertName = super.handleForSize(localFilePath, showSizeStr, module);
		if(StringUtils.isNotBlank(convertName)){
			imgInfo.setAccessUri(getAccessUri(localFilePath, convertName));
		}
		
		return imgInfo;
	}
	
}
