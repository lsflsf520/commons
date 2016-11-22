package com.yisi.stiku.db.multi;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;

/**
 * 根据业务分库的多数据源路由策略工具
 * @author shangfeng
 *
 */
public class DSKeyHolder {

	public final static String PRIV_DS_KEY = "priv";
	public final static String BASEDATA_DS_KEY = "basedata";

	private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();

	public static void setDataSourceKey(String contextType) {
		contextHolder.set(contextType);
	}

	public static String getDataSourceKey() {
		return contextHolder.get();
	}

	public static void clearDataSourceKey() {
		contextHolder.remove();
	}

	public static String parseDsKey(String statement){
		String pkgPrefix = ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "mybatis.multi.ds.pkg.prefix", "com.yisi.stiku");
		pkgPrefix = pkgPrefix.endsWith(".") ? pkgPrefix : pkgPrefix + ".";
		if (StringUtils.isNotBlank(statement)
				&& statement.startsWith(pkgPrefix)) {
			String suffix = statement.replace(pkgPrefix, "");
			
			statement = suffix.substring(0, suffix.indexOf("."));
		}
		
		return statement;
	}
	
	public static void parseCurrDsKey(String statement) {
		String dsKey = parseDsKey(statement);
		
		setDataSourceKey(dsKey);
	}
}
