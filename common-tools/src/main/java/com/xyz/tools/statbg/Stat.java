package com.xyz.tools.statbg;

import java.util.List;
import java.util.Map;

import com.xyz.tools.common.exception.BaseRuntimeException;


/**
 * 
 * @author lsf
 *
 */
abstract public class Stat {

	protected String desc; // 对bean的简短描述
	protected int orderNO; // 获得该统计组件在本次统计中的位置
	protected String outputKey; // 返回一个key，和 execute方法的返回值组成一个键值对
	protected boolean exitWhenException = true; // 当发生异常的时候，是否决定退出数据统计。如果为true，则发生异常时退出；否则继续下边的数据统计

	/**
	 * @param paramMap
	 *            执行程序所要用到的上下文参数
	 * @return 执行相应的操作，返回一个指定类型的结果
	 */
	abstract public List<FlowData> execute(Map<String, List<FlowData>> paramMap) throws BaseRuntimeException;

	/**
	 * 是否需要替换掉特殊字符
	 * 
	 * @return
	 */
	abstract public boolean isSpecialCharReplaced();

	public String getDesc() {

		return desc;
	}

	public void setDesc(String desc) {

		this.desc = desc;
	}

	public void setOrderNO(int orderNO) {

		this.orderNO = orderNO;
	}

	public void setOutputKey(String outputKey) {

		this.outputKey = outputKey;
	}

	public int getOrderNO() {

		return orderNO;
	}

	public String getOutputKey() {

		return outputKey;
	}

	public boolean isExitWhenException() {

		return exitWhenException;
	}

	public void setExitWhenException(boolean exitWhenException) {

		this.exitWhenException = exitWhenException;
	}

}
