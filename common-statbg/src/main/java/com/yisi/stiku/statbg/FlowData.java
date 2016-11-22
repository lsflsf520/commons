package com.yisi.stiku.statbg;


/**
 * 数据行对象，这个对象可以代表数据库表中的一行数据，也可以是日志文件中的一行数据，还可以是特定的单个参数数据 用户也可以实现该接口，自定义自己的数据结构
 * 
 * @author lsf
 *
 */
public interface FlowData {

	/**
	 * 
	 * @param key
	 * @return
	 */
	Object getData(String key);

	/**
	 * 
	 * @param specialCharReplaced
	 *            是否需要替换特殊字符
	 */
	void setSpecialCharReplaced(boolean specialCharReplaced);

	/**
	 * 
	 * @param data
	 */
	void appendFlowData(FlowData data);
}
