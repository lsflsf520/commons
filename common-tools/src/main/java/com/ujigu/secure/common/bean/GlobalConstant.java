package com.ujigu.secure.common.bean;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.utils.BaseConfig;

public class GlobalConstant {

	public static final long SYS_USER_ID = -8888l;
	public static final String SYS_USER_NAME = "系统";
	
	//分配给代理公司的ID，系统中可以直接引用
	public static final int AGENT_COMPANY_ID = Integer.valueOf(BaseConfig.getValue("agent.company.id", "0"));
	
	public static final String PROJECT_NAME = BaseConfig.getValue("project.name");
	
	public static final String PROJECT_NAME_SUFFIX = StringUtils.isBlank(PROJECT_NAME) || !PROJECT_NAME.contains("-") ? PROJECT_NAME : PROJECT_NAME.substring(PROJECT_NAME.indexOf("-") + 1, PROJECT_NAME.length());
	
	public static final boolean IS_WEB_ADMIN = "web-admin".equals(GlobalConstant.PROJECT_NAME) || "web-cms-admin".equals(GlobalConstant.PROJECT_NAME);
	
	public static final boolean IS_WEB_H5 = "web-h5".equals(GlobalConstant.PROJECT_NAME) || "web-cms-h5".equals(GlobalConstant.PROJECT_NAME);
	
	public static final boolean IS_WEB_COMPANY = "web-company".equals(GlobalConstant.PROJECT_NAME);
	
	public static final boolean IS_WEB_AGENT = "web-agent".equals(GlobalConstant.PROJECT_NAME);
	
	public static final boolean IS_WEB_PC = IS_WEB_AGENT || "web-cms".equals(GlobalConstant.PROJECT_NAME);
	
	public static final boolean IS_WEB_APP = "web-app".equals(GlobalConstant.PROJECT_NAME);
	
	public static final boolean IS_ENV_ONLINE = "true".equalsIgnoreCase(BaseConfig.getValue("env.online", "false"));
	
	public static final String STATIC_DOMAIN = "http://" + BaseConfig.getValue("static.resource.domain", "");
	
	public static final String RES_VERSION = BaseConfig.getValue("static.resource.version", "1.0.0");
	
	public static final String IMG_DOMAIN = "http://" + BaseConfig.getValue("static.img.domain", "img-test.baoxianjie.net");
	
	public static final String UPFILE_DOMAIN = "http://" + BaseConfig.getValue("static.upfile.domain", "upfile-test.baoxianjie.net");
	
	public static final String APP_DOMAIN = BaseConfig.getValue("app.domain");
	
	public static final String ACL_DOMAIN = "http://" + BaseConfig.getValue("csai.acl.domain", "mgr.csaimall.com");
	
	public static final String SQL_FIELD_SPLITER = ",";
	
	public static final int PAGE_NO_LIMIT = -1; //不分页标识
	
}
