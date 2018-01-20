package com.ujigu.statbg.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.data.ListData;
import com.ujigu.statbg.util.RegexHelperUtil;

public class SingleRegexListLogStat extends LogStat {

	private final static Logger LOG = LoggerFactory
			.getLogger(SingleRegexListLogStat.class);

	protected String logFileDir; // 日志目录
	protected String fileNameFilter; // 匹配日志文件的正则表达式，不设置的话，默认为logFileDir目录下所有的文件
	protected String lineRegex;
	protected Pattern pattern;

	@Override
	protected List<File> getFiles(Map<String, List<FlowData>> paramMap) {

		List<File> files = new ArrayList<File>();
		if (StringUtils.isBlank(logFileDir)) {
			LOG.warn("property '" + logFileDir + "' must be defined,logFileDir:" + logFileDir + "," + IPUtil.getLocalIp());
			return files;
		}

		String currLogFileDir = RegexHelperUtil.replaceParams(logFileDir, paramMap);

		File logDir = new File(currLogFileDir);
		if (!logDir.exists()) {
			LOG.warn("directory '" + currLogFileDir + "' does not exist,logFileDir:" + currLogFileDir + ","
					+ IPUtil.getLocalIp());
			return files;
		}

		if (logDir.isFile()) {
			files.add(logDir);
			return files;
		}

		IOFileFilter fileFilter = TrueFileFilter.INSTANCE;
		String currFileNameFilter = "";
		if (StringUtils.isNotBlank(fileNameFilter)) {
			currFileNameFilter = RegexHelperUtil.replaceParams(
					fileNameFilter, paramMap);

			LOG.debug("filter log files with fileName '" + currFileNameFilter
					+ "' in directory " + logDir.getAbsolutePath());
			currFileNameFilter = currFileNameFilter.replaceAll("\\\\", "/");
			if (currFileNameFilter.contains("/")) {
				int index = currFileNameFilter.lastIndexOf("/");
				String fileName = currFileNameFilter.substring(index + 1);
				fileFilter = new WildcardFileFilter(fileName);
			} else {
				fileFilter = new WildcardFileFilter(currFileNameFilter);
			}
		}

		Collection<File> logFiles = listFiles(logDir, fileFilter);

		if (logFiles != null && logFiles.size() > 0) {
			currFileNameFilter = currFileNameFilter.replaceAll("\\*", "\\.*");
			String filePattern = currFileNameFilter.startsWith("/") ? ".*"
					+ currFileNameFilter : ".*/" + currFileNameFilter;
			for (File f : logFiles) {
				if (f.getAbsolutePath().replaceAll("\\\\", "/").matches(filePattern)) {
					files.add(f);
				}
			}
		}

		return files;
	}

	private static Collection<File> listFiles(File logDir, IOFileFilter fileFilter) {

		Collection<File> logFiles = FileUtils.listFiles(logDir, fileFilter,
				null);

		File[] files = logDir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					Collection<File> childFiles = listFiles(file, fileFilter);

					logFiles.addAll(childFiles);
				}
			}
		}

		return logFiles;
	}

	@Override
	protected List<String> extractFields(Map<String, List<FlowData>> paramMap,
			String line) {

		if (pattern == null) {
			synchronized (this) {
				if (pattern == null) {
					pattern = Pattern.compile(lineRegex);
				}
			}
		}

		return RegexHelperUtil.extractGroups(pattern, line);
	}

	@Override
	protected FlowData buildValue(List<String> fieldList) {

		ListData fieldData = new ListData();
		if (fieldList != null && fieldList.size() > 0) {
			for (String field : fieldList) {
				fieldData.add(field);
			}
		}
		return fieldData;
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

}
