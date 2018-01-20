package com.ujigu.secure.upfile.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.utils.BaseConfig;

/**
 * 
 * @author lsf
 *
 */
public abstract class SpecialSizeHandler {

	/**
	 * 
	 * @param localFilePath
	 * @param module
	 */
	public void handleSpecialSize(String localFilePath, String module){
		String[] sizeRuleArr = getSpecialSizes(module);
		if(sizeRuleArr != null && sizeRuleArr.length > 0){
			String convertName = preHandle(localFilePath, module);
			String baseImgPath = localFilePath;
			if(StringUtils.isNotBlank(convertName)){
				baseImgPath = convertName;
				String baseDir = FilenameUtils.getFullPath(localFilePath);
				if(!convertName.startsWith(baseDir)){
					baseImgPath = baseDir + convertName;
				}
			}
			
			for(String sizeStr : sizeRuleArr){
				handleSpecialSize(baseImgPath, module, sizeStr);
			}
			
			postHandle(baseImgPath, module);
		}
	}
	
	protected String preHandle(String localFilePath, String module){
		//do nothing by default
		return null;
	}
	
	protected void postHandle(String baseImgPath, String module){
		//do nothing by default
	}
	
	/**
	 * 
	 * @param baseImgPath 有可能会携带尺寸后缀
	 * @param module
	 * @param sizeStr
	 */
	abstract protected void handleSpecialSize(String baseImgPath, String module, String sizeStr);
	
	private String[] getSpecialSizes(String module){
		return BaseConfig.getValueArr(module + ".special.sizeRule");
	}
 	
}
