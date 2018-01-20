package com.ujigu.secure.db.aop;

import java.util.Set;

import com.ujigu.secure.mq.bean.ModifyMsg;

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
