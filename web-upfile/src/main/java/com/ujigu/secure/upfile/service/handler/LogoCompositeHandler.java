package com.ujigu.secure.upfile.service.handler;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.upfile.bean.ImgPosition;
import com.ujigu.secure.upfile.service.CompositeHandler;
import com.ujigu.secure.upfile.util.ImageShellUtil;
import com.ujigu.secure.web.util.WebUtils;

public class LogoCompositeHandler implements CompositeHandler {
	

	@Override
	public void compositeImg(HttpServletRequest request, String module,
			String localFilePath) {
		String watermarkPath = getWatermarkImg(module);
		if(StringUtils.isNotBlank(watermarkPath)){
			if(new File(watermarkPath).exists()){
				ImgPosition position = ImgPosition.southeast;
				
				String positionStr = getWatermarkPosition(module);
				if(StringUtils.isNotBlank(positionStr)){
					try{
					    position = ImgPosition.valueOf(positionStr);
					}catch(Exception e){
						LogUtils.error("module.watermark.position:%s is invalid， ipaddr:%s", e, positionStr, WebUtils.getIpAddr(request));
					}
				}
				
				String offsetStr = getWatermarkOffset(module);
				ImageShellUtil.compositeImg(localFilePath, watermarkPath, position, offsetStr);
			}
		}

	}
	
	protected String getWatermarkImg(String module){
		return BaseConfig.getValue(module + ".watermark.path");
	}
	
	protected String getWatermarkPosition(String module){
		return BaseConfig.getValue(module + ".watermark.position");
	}
	
	protected String getWatermarkOffset(String module){
		return BaseConfig.getValue(module + ".watermark.offset", "+15+15");
	}

}
