package com.xyz.tools.db.aop;

import java.util.Set;

import com.xyz.tools.mq.bean.ModifyMsg;

public interface DataModifyListener {
	
	/**
	 * 处理修改后的数据
	 * @param modifyMsg
	 */
	void receivedModifyData(ModifyMsg modifyMsg);

	/**
	 * 
	 * @return 返回敢兴趣的表名
	 */
	Set<String> interestTables();

}
