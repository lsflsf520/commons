package com.ujigu.secure.upfile.service.strategy;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.upfile.bean.ImgPosition;
import com.ujigu.secure.upfile.service.FillBgStrategy;
import com.ujigu.secure.upfile.util.ImageShellUtil;

/**
 * 
 * @author lsf
 *
 */
public class ColorFillBgStrategy implements FillBgStrategy {
	
	@Override
	public String fillBg(String fromImg, String module, int width, int height) {
		
		return ImageShellUtil.fillBgWithColor(fromImg, getFillPosition(module), getFillBgColor(module), width, height);
	}
	
	protected ImgPosition getFillPosition(String module){
		String position = BaseConfig.getValue(module + ".fillBg.position", "center");
		
		try{
			return ImgPosition.valueOf(position);
		} catch (Exception e){
			LogUtils.error("position %s in zk is invalid", e, position);
		}
		
		return ImgPosition.center;
	}
	
	protected String getFillBgColor(String module){
		return BaseConfig.getValue(module + ".fillBg.color", "white");
	}

}
