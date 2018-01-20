package com.ujigu.statbg.bat.reader;

import java.io.Serializable;
import java.util.Map;

/**
 * @author shangfeng
 *
 */
public interface LineReader {

	void open() throws Exception;

	/**
	 * 
	 * @return 如果返回null，则说明数据读取完毕
	 * @throws Exception
	 */
	Map<String, Serializable> nextLine() throws Exception;

	void close() throws Exception;

}
