package com.yisi.stiku.conf;

public class ZkConstant {

	public final static String ZK_ROOT_NODE = "/csjy";
	
	public final static String PROJECT_NAME = BaseConfig.getValue("project.name");
	
	public final static String ALIAS_PROJECT_NAME = BaseConfig.getValue("project.name.alias", PROJECT_NAME);
	
	public final static String APP_ZK_PATH = ALIAS_PROJECT_NAME + "/application.properties";
	
}
