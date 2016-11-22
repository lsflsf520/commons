package com.yisi.stiku.statbg.bat.writer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author shangfeng
 *
 */
public interface BatWriter {

	void open() throws Exception;

	/**
	 * 
	 * @param batchValues
	 *            由BatStat控制批次数据的量，然后将这一批次的数据作为execBatch的参数执行相应的处理
	 */
	void execBatch(List<Map<String, Serializable>> batchValues)
			throws Exception;

	void close() throws Exception;

}
