package com.ujigu.statbg.log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.Stat;

/**
 * 
 * @author lsf
 *
 * @param <T>
 */
abstract public class LogStat extends Stat {

	private final static Logger LOG = LoggerFactory.getLogger(LogStat.class);

	protected String encoding;
	protected boolean specialCharReplaced = true;

	@Override
	public List<FlowData> execute(Map<String, List<FlowData>> paramMap) throws BaseRuntimeException {

		List<FlowData> resultList = new ArrayList<FlowData>(200);
		List<File> fileList = getFiles(paramMap);
		if (fileList == null || fileList.size() <= 0) {
			LOG.warn("no log files found." + IPUtil.getLocalIp());
			return resultList;
		}

		for (File file : fileList) {
			try {
				LineIterator itr = FileUtils.lineIterator(file, getEncoding());
				if (itr != null) {
					while (itr.hasNext()) {
						addFields(itr.nextLine(), resultList, paramMap);
					}

				}
			} catch (IOException e) {
				String errorMsg = "log file '" + file.getAbsolutePath() + "',serverIP:" + IPUtil.getLocalIp();
				throw new BaseRuntimeException("EXEC_ERROR", errorMsg, e);
			}
		}
		return resultList;
	}

	/**
	 * 
	 * @param line
	 * @param resultList
	 * @param paramMap
	 */
	protected void addFields(String line, List<FlowData> resultList, Map<String, List<FlowData>> paramMap) {

		if (StringUtils.isBlank(line)) {
			return;
		}

		List<String> fieldList = extractFields(paramMap, line);
		if (fieldList != null && fieldList.size() > 0) {
			FlowData resultObj = buildValue(fieldList);
			if (resultObj != null) {
				resultList.add(resultObj);
			}
		}
	}

	/**
	 * 
	 * @return 获取指定的日志文件
	 */
	abstract protected List<File> getFiles(Map<String, List<FlowData>> paramMap);

	/**
	 * 
	 * @param line
	 *            日志文件中的行
	 * @return 如果line符合要求，则取出日志中的各个域；否则可以返回一个空得List对象或者null
	 */
	abstract protected List<String> extractFields(Map<String, List<FlowData>> paramMap, String line);

	/**
	 * 获取field名称，该名称的顺序和个数一定要和 extractFields方法返回的值一一对应
	 * 
	 * @return
	 */
	abstract protected FlowData buildValue(List<String> fieldList);

	public String getEncoding() {

		return StringUtils.isBlank(encoding) ? "UTF-8" : encoding;
	}

	public void setEncoding(String encoding) {

		this.encoding = encoding;
	}

	public boolean isSpecialCharReplaced() {

		return specialCharReplaced;
	}

	public void setSpecialCharReplaced(boolean specialCharReplaced) {

		this.specialCharReplaced = specialCharReplaced;
	}

}
