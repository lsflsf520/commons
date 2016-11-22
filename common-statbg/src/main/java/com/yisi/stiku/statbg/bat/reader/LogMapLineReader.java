package com.yisi.stiku.statbg.bat.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.data.MapData;
import com.yisi.stiku.statbg.log.SingleRegexMapLogStat;

/**
 * @author shangfeng
 *
 */
public class LogMapLineReader extends SingleRegexMapLogStat implements LineReader {

	private final static Logger LOG = LoggerFactory.getLogger(LogMapLineReader.class);

	private Map<String, List<FlowData>> paramMap;

	private Iterator<File> fileItr;
	private BufferedReader br;
	private File file;

	/**
	 * 
	 */
	public LogMapLineReader(Map<String, List<FlowData>> paramMap) {

		this.paramMap = paramMap;
	}

	@Override
	public void open() throws Exception {

		List<File> fileList = getFiles(paramMap);
		fileItr = fileList == null ? null : fileList.iterator();
	}

	@Override
	public Map<String, Serializable> nextLine() throws Exception {

		if (file == null && fileItr.hasNext()) {
			nextFile();
		}

		if (br == null) {
			throw new BaseRuntimeException("FILE_NOT_FOUND", "没有找到任何匹配的日志文件");
		}

		String line = br.readLine();
		if (StringUtils.isBlank(line) && fileItr.hasNext()) {
			nextFile();

			line = br.readLine();
		}

		if (StringUtils.isBlank(line)) {
			return null;
		}

		List<String> fieldList = extractFields(paramMap, line);
		if (fieldList == null || fieldList.size() <= 0) {
			return nextLine(); // 这里需要递归调用，直到找到匹配的行
		}

		FlowData resultObj = buildValue(fieldList);
		MapData data = (MapData) resultObj;

		Map<String, Serializable> fieldMap = new HashMap<String, Serializable>();
		for (Entry<String, Object> entry : data.getEntries()) {
			fieldMap.put(entry.getKey(), (Serializable) entry.getValue());
		}

		return fieldMap;
	}

	private void nextFile() throws FileNotFoundException {

		file = fileItr.next();
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				LOG.error("close file input stream error, message: " + e.getMessage(), e);
			}
		}

		br = new BufferedReader(new FileReader(file));
	}

	@Override
	public void close() throws Exception {

		if (br != null) {
			br.close();
		}

	}

}
