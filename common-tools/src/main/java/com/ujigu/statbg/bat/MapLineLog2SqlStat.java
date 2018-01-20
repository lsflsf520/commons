package com.ujigu.statbg.bat;

import java.util.List;
import java.util.Map;

import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.bat.reader.LineReader;
import com.ujigu.statbg.bat.reader.LogMapLineReader;

public class MapLineLog2SqlStat extends ListLineLog2SqlStat {

	@Override
	protected LineReader getLineReader(Map<String, List<FlowData>> paramMap) {

		LogMapLineReader lineReader = new LogMapLineReader(paramMap);
		lineReader.setLogFileDir(logFileDir);
		lineReader.setFileNameFilter(fileNameFilter);
		lineReader.setLineRegex(lineRegex);
		lineReader.setEncoding(encoding);

		return lineReader;
	}

}
