package com.yisi.stiku.statbg.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.Stat;
import com.yisi.stiku.statbg.func.FuncContext;
import com.yisi.stiku.statbg.util.RegexHelperUtil;
import com.yisi.stiku.statbg.util.SqlUtil;

/**
 * 
 * @author lsf
 * 
 */
abstract public class SqlStat extends Stat {

	private final static Logger LOG = LoggerFactory
			.getLogger(SqlStat.class);

	protected DataSource dataSource;
	protected String sql;
	protected boolean repeatOneLine = true; // 在填充参数时，如果某个参数的值是List，并且其中只有一个元素，那么在循环执行sql的时候，是否需要反复用第一行的数据；默认为true，即需要
	protected Map<String, Serializable> nullReplaceMap = new HashMap<String, Serializable>();
	protected boolean specialCharReplaced;
	protected int maxRetryCount = 5; // 当发生 No operations allowed after
										// statement

	// closed 异常的之后，需要重试的次数

	public DataSource getDataSource() {

		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {

		this.dataSource = dataSource;
	}

	public String getSql() {

		return sql;
	}

	public void setSql(String sql) {

		this.sql = sql;
	}

	public void setMaxRetryCount(int maxRetryCount) {

		this.maxRetryCount = maxRetryCount;
	}

	public Map<String, Serializable> getNullReplaceMap() {

		return nullReplaceMap;
	}

	public void setNullReplaceMap(Map<String, Serializable> nullReplaceMap) {

		this.nullReplaceMap = nullReplaceMap;
	}

	@Override
	public boolean isSpecialCharReplaced() {

		return specialCharReplaced;
	}

	public void setSpecialCharReplaced(boolean specialCharReplaced) {

		this.specialCharReplaced = specialCharReplaced;
	}

	public boolean isRepeatOneLine() {

		return repeatOneLine;
	}

	public void setRepeatOneLine(boolean repeatOneLine) {

		this.repeatOneLine = repeatOneLine;
	}

	@Override
	public List<FlowData> execute(Map<String, List<FlowData>> paramMap) {

		if (StringUtils.isBlank(sql)) {
			LOG.warn("No sql defined in stat of orderNo " + getOrderNO() + "," +
					IPUtil.getLocalIp());
			return null;
		}

		List<FlowData> results = new ArrayList<FlowData>();

		List<String> paramNames = RegexHelperUtil.getParamNames(sql); // 获取sql中的所有变量，以便后边进行替换
		Map<String, FuncContext> funcMap = RegexHelperUtil.getKey2FuncMap(sql);
		String preparedSql = SqlUtil.getPreparedSql(sql, paramNames, paramMap);
		LOG.debug("origin sql:" + sql);
		LOG.debug("prepared sql:" + preparedSql);
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(preparedSql);

			int maxLines = getMaxValueLines(paramMap, paramNames);
			for (int i = 0; i < maxLines; i++) {
				SqlUtil.setSqlValue(pstmt, paramMap, paramNames, i, nullReplaceMap, repeatOneLine, funcMap);
				int retryCount = maxRetryCount;
				boolean transactionCompleted = false;
				List<FlowData> currResults = null;
				do {
					try {
						currResults = execute(pstmt);
						transactionCompleted = true;
						retryCount = 0;
					} catch (SQLException e) {
						String sqlState = e.getSQLState();
						// 这个08S01就是这个异常No operations allowed after statement
						// closed的sql状态。单独处理手动重新链接就可以了。
						if ("08S01".equals(sqlState) || "40001".equals(sqlState)) {
							retryCount--;
							LOG.error("execute query error, serverIP:" + IPUtil.getLocalIp(), e);
						} else {
							retryCount = 0;
							throw new BaseRuntimeException("EXEC_ERROR", "execute query error, serverIP:"
									+ IPUtil.getLocalIp(), e);
						}
					}
				} while (!transactionCompleted && (retryCount > 0));

				if (currResults != null && currResults.size() > 0) {
					results.addAll(currResults);
				}

				pstmt.clearParameters();
				pstmt.clearWarnings();
			}
		} catch (SQLException e) {
			String errorMsg = "jdbc error occured. serverIP:" + IPUtil.getLocalIp();
			throw new BaseRuntimeException("EXEC_ERROR", errorMsg, e);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					LOG.error("pstmt closed error. serverIP:" + IPUtil.getLocalIp(), e);
				}
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error("conn closed error. serverIP:" + IPUtil.getLocalIp(), e);
				}
			}
		}

		return results;
	}

	abstract protected List<FlowData> execute(PreparedStatement pstmt) throws SQLException;

	protected int getMaxValueLines(Map<String, List<FlowData>> paramMap,
			List<String> paramNames) {

		int maxLines = 1;
		if (paramNames != null && paramNames.size() > 0) {

			for (String paramName : paramNames) {
				String part0 = paramName.split("\\.")[0];
				List<FlowData> listData = paramMap.get(part0);
				if (listData != null && listData.size() > maxLines) {
					maxLines = listData.size();
				}
			}
		}

		return maxLines;
	}

}
