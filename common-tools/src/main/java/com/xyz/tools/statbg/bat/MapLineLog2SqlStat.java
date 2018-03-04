package com.xyz.tools.statbg.bat;

import java.util.List;
import java.util.Map;

import com.xyz.tools.statbg.FlowData;
import com.xyz.tools.statbg.bat.reader.LineReader;
import com.xyz.tools.statbg.bat.reader.LogMapLineReader;

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
