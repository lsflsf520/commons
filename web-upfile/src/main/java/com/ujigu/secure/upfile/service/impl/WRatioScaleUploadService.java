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
public class WRatioScaleUploadService extends CompressionUploadService{
	
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
