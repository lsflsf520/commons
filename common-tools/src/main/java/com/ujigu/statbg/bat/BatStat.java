package com.ujigu.statbg.bat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.Stat;
import com.ujigu.statbg.bat.reader.LineReader;
import com.ujigu.statbg.bat.writer.BatWriter;

/**
 * 
 * @author lsf
 *
 */
abstract public class BatStat extends Stat {

	private final static Logger LOG = LoggerFactory
			.getLogger(BatStat.class);

	protected int batchSize = 2000;
	protected boolean repeatOneLine = true; // 在填充参数时，如果某个参数的值是List，并且其中只有一个元素，那么在循环执行sql的时候，是否需要反复用第一行的数据；默认为true，即需要
	protected Map<String, Serializable> nullReplaceMap = new HashMap<String, Serializable>();
	protected boolean specialCharReplaced;

	@Override
	public List<FlowData> execute(Map<String, List<FlowData>> paramMap) throws BaseRuntimeException {

		int rowCount = 0;
		List<Map<String, Serializable>> batchValues = new ArrayList<Map<String, Serializable>>(batchSize);
		boolean hasException = false;
		Map<String, Serializable> lineData = null;
		LineReader lineReader = getLineReader(paramMap);
		BatWriter batWriter = getBatchWriter(paramMap);
		try {
			lineReader.open();
			batWriter.open();

			while ((lineData = lineReader.nextLine()) != null) {
				if (lineData.isEmpty()) {
					continue;
				}

				buildOutputPrefix(lineData);

				batchValues.add(lineData);
				++rowCount;

				if (rowCount % batchSize == 0) {
					try {
						batWriter.execBatch(batchValues);
					} catch (Exception e) {
						String errorMsg = "execBatch failure, rowCount:" + rowCount + ", stopWhenBatchException:"
								+ isExitWhenException() + ",serverIP:" + IPUtil.getLocalIp() + ", batch datas:\n"
								+ batchValues;
						if (isExitWhenException()) {
							throw new BaseRuntimeException("ERROR_EXEC", errorMsg, e);
						} else {
							LOG.error(errorMsg, e);
						}
					} finally {
						batchValues.clear();
					}
				}
			}

			if (batchValues.size() > 0 && !hasException) {
				try {
					batWriter.execBatch(batchValues);
				} catch (Exception e) {
					String errorMsg = "execBatch failure, rowCount:" + rowCount + ", stopWhenBatchException:"
							+ isExitWhenException() + ",serverIP:" + IPUtil.getLocalIp() + ", batch datas:\n"
							+ batchValues;
					throw new BaseRuntimeException("ERROR_EXEC", errorMsg, e);
				} finally {
					batchValues.clear();
				}
			}
		} catch (Exception e1) {
			throw new BaseRuntimeException("EXEC_ERROR", e1.getMessage(), e1);
		} finally {
			try {
				lineReader.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}

			try {
				batWriter.close();
			} catch (Exception e) {
				LOG.error(e.getMessage(), e);
			}
		}

		return null;
	}

	/**
	 * 将lineData中的key，加上outputkey，便于统一使用变量的格式
	 * 
	 * @param lineData
	 * @param prefix
	 */
	private void buildOutputPrefix(Map<String, Serializable> lineData) {

		if (StringUtils.isBlank(getOutputKey())) {
			return;
		}
		Map<String, Serializable> prefixMap = new HashMap<String, Serializable>();
		for (String key : lineData.keySet()) {
			if (!key.startsWith(getOutputKey())) {
				prefixMap.put(getOutputKey() + "." + key, lineData.get(key));
			}
		}

		lineData.putAll(prefixMap);
	}

	/**
	 * 
	 * @return 从源数据返回下一行数据
	 */
	abstract protected LineReader getLineReader(Map<String, List<FlowData>> paramMap);

	/**
	 * 
	 * @param paramMap
	 * @return
	 */
	abstract protected BatWriter getBatchWriter(Map<String, List<FlowData>> paramMap);

	@Override
	public boolean isSpecialCharReplaced() {

		return specialCharReplaced;
	}

	public void setSpecialCharReplaced(boolean specialCharReplaced) {

		this.specialCharReplaced = specialCharReplaced;
	}

	public int getBatchSize() {

		return batchSize;
	}

	public void setBatchSize(int batchSize) {

		this.batchSize = batchSize;
	}

	public boolean isRepeatOneLine() {

		return repeatOneLine;
	}

	public void setRepeatOneLine(boolean repeatOneLine) {

		this.repeatOneLine = repeatOneLine;
	}

	public Map<String, Serializable> getNullReplaceMap() {

		return nullReplaceMap;
	}

	public void setNullReplaceMap(Map<String, Serializable> nullReplaceMap) {

		this.nullReplaceMap = nullReplaceMap;
	}

}
