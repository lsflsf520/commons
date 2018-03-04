package com.xyz.tools.statbg.bat.reader;

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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.statbg.FlowData;
import com.xyz.tools.statbg.data.ListData;
import com.xyz.tools.statbg.log.SingleRegexListLogStat;

/**
 * @author shangfeng
 *
 */
public class LogListLineReader extends SingleRegexListLogStat implements LineReader {

	private final static Logger LOG = LoggerFactory.getLogger(LogListLineReader.class);

	private Map<String, List<FlowData>> paramMap;

	private Iterator<File> fileItr;
	private BufferedReader br;
	private File file;

	/**
	 * 
	 */
	public LogListLineReader(Map<String, List<FlowData>> paramMap) {

		this.paramMap = paramMap;
	}

	@Override
	public void open() throws Exception {

		List<File> fileList = this.getFiles(paramMap);
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
		ListData data = (ListData) resultObj;

		Map<String, Serializable> fieldMap = new HashMap<String, Serializable>();
		Iterator<Object> dataItr = data.getIterator();
		int index = 0;
		while (dataItr.hasNext()) {
			fieldMap.put("" + index, (Serializable) dataItr.next());

			index++;
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
