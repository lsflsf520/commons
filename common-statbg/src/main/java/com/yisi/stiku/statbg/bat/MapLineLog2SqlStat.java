package com.yisi.stiku.statbg.bat;

import java.util.List;
import java.util.Map;

import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.bat.reader.LineReader;
import com.yisi.stiku.statbg.bat.reader.LogMapLineReader;

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
