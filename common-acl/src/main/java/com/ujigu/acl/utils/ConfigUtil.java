package com.ujigu.acl.utils;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;

public class ConfigUtil {
	
	/**
	 * 
	 * @return 返回当前web的appId
	 */
	public static int getWebappId(){
		Integer webappId = BaseConfig.getInt("sys.webapp.id");
		if(webappId == null){
			throw new BaseRuntimeException("NOT_CONFIG", "sys.webapp.id 没有在application.properties中配置");
		}
		
		return webappId;
	}

}
