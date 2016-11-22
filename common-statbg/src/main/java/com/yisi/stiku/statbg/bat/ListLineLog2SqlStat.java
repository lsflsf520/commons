package com.yisi.stiku.statbg.bat;

import java.util.List;
import java.util.Map;

import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.bat.reader.LineReader;
import com.yisi.stiku.statbg.bat.reader.LogListLineReader;

public class ListLineLog2SqlStat extends Sql2SqlStat {

	protected String logFileDir; // 日志目录
	protected String fileNameFilter; // 匹配日志文件的正则表达式，不设置的话，默认为logFileDir目录下所有的文件
	protected String lineRegex;
	protected String encoding;

	@Override
	protected LineReader getLineReader(Map<String, List<FlowData>> paramMap) {

		LogListLineReader lineReader = new LogListLineReader(paramMap);
		lineReader.setLogFileDir(logFileDir);
		lineReader.setFileNameFilter(fileNameFilter);
		lineReader.setLineRegex(lineRegex);
		lineReader.setEncoding(encoding);

		return lineReader;
	}

	public String getLogFileDir() {

		return logFileDir;
	}

	public void setLogFileDir(String logFileDir) {

		this.logFileDir = logFileDir;
	}

	public String getFileNameFilter() {

		return fileNameFilter;
	}

	public void setFileNameFilter(String fileNameFilter) {

		this.fileNameFilter = fileNameFilter;
	}

	public String getLineRegex() {

		return lineRegex;
	}

	public void setLineRegex(String lineRegex) {

		this.lineRegex = lineRegex;
	}

	public String getEncoding() {

		return encoding;
	}

	public void setEncoding(String encoding) {

		this.encoding = encoding;
	}

}
