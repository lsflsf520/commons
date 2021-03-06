package com.xyz.tools.common.constant;

import org.apache.commons.lang.StringUtils;

import com.xyz.tools.common.utils.BaseConfig;

public class GlobalConstant {

	public static final long SYS_USER_ID = -8888l;
	public static final String SYS_USER_NAME = "系统";
	
	//分配给代理公司的ID，系统中可以直接引用
	
	public static final String PROJECT_NAME = BaseConfig.getValue("project.name");
	
	public static final String PROJECT_NAME_SUFFIX = StringUtils.isBlank(PROJECT_NAME) || !PROJECT_NAME.contains("-") ? PROJECT_NAME : PROJECT_NAME.substring(PROJECT_NAME.indexOf("-") + 1, PROJECT_NAME.length());
	
	public static final boolean IS_ENV_ONLINE = "true".equalsIgnoreCase(BaseConfig.getValue("env.online", "false"));
	
	public static final boolean IS_MGR = "true".equalsIgnoreCase(BaseConfig.getValue("env.mgr_web", "false"));
	
    public static final String PROJECT_PC_DOMAIN = BaseConfig.getValue("pc.project.domain");
	
	public static final String PROJECT_H5_DOMAIN = BaseConfig.getValue("h5.project.domain"); 
	
	//静态资源访问域名
	public static final String STATIC_DOMAIN = BaseConfig.getValue("static.resource.domain", "http://static.csaimall.com");
	
	//静态资源版本号
	public static final String RES_VERSION = BaseConfig.getValue("static.resource.version", "1.0.0");
	
	//公共底层基础服务访问域名
	public static final String BASE_SERVICE_DOMAIN = BaseConfig.getValue("base.service.domain", "http://base.csaimall.com");
	
	//权限校验域名
	public static final String ACL_DOMAIN = BaseConfig.getValue("acl.domain", "http://acl.csaimall.com");
	
	public static final String SQL_FIELD_SPLITER = ",";
	
	public static final int PAGE_NO_LIMIT = -1; //不分页标识
	
}
