package com.ujigu.statbg.bat.writer;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.func.FuncContext;
import com.ujigu.statbg.util.RegexHelperUtil;
import com.ujigu.statbg.util.SqlUtil;

/**
 * @author shangfeng
 *
 */
public class SqlBatWriter implements BatWriter {

	private final static Logger LOG = LoggerFactory.getLogger(SqlBatWriter.class);

	private DataSource dataSource;
	private Connection conn;
	private Statement stmt;
	private String insertPreSql;
	private String insertSuffixSql;
	private String updateSql;
	private Map<String, List<FlowData>> paramMap;
	private boolean repeatOneLine;

	private List<String> paramNames = null;
	private Map<String, FuncContext> funcMap = null;

	/**
	 * 
	 */
	public SqlBatWriter(DataSource dataSource, String insertPreSql, String insertSuffixSql,
			Map<String, List<FlowData>> paramMap, boolean repeatOneLine, String updateSql) {

		this.dataSource = dataSource;
		this.insertPreSql = insertPreSql;
		this.insertSuffixSql = insertSuffixSql;
		this.paramMap = paramMap;
		this.repeatOneLine = repeatOneLine;
		this.updateSql = updateSql;
	}

	@Override
	public void open() throws Exception {

		conn = dataSource.getConnection();
		stmt = conn.createStatement();
	}

	@Override
	public void execBatch(List<Map<String, Serializable>> batchValues) throws Exception {

		if (StringUtils.isNotBlank(updateSql)) {
			for (int row = 0; row < batchValues.size(); row++) {
				if (paramNames == null) {
					paramNames = RegexHelperUtil.getParamNames(updateSql); // 获取sql中的所有变量，以便后边进行替换
					funcMap = RegexHelperUtil.getKey2FuncMap(updateSql);
				}
				String execSql = SqlUtil.buildExecSql(updateSql, paramNames, batchValues.get(row), paramMap, row,
						repeatOneLine, funcMap);

				stmt.executeUpdate(execSql);
			}
		} else {
			if (paramNames == null) {
				paramNames = RegexHelperUtil.getParamNames(insertSuffixSql); // 获取sql中的所有变量，以便后边进行替换
				funcMap = RegexHelperUtil.getKey2FuncMap(insertSuffixSql);
			}

			StringBuilder builder = new StringBuilder(insertPreSql + " values ");
			for (int row = 0; row < batchValues.size(); row++) {
				String execSql = SqlUtil.buildExecSql(insertSuffixSql, paramNames, batchValues.get(row), paramMap, row,
						repeatOneLine, funcMap);

				builder.append(execSql);
				if (row < batchValues.size() - 1) {
					builder.append(",");
				}
			}

			String sql = builder.toString();
			LOG.debug(LOG.isDebugEnabled() ? "batch sql: " + sql : null);

			stmt.executeUpdate(sql);
		}
	}

	@Override
	public void close() throws Exception {

		if (stmt != null) {
			stmt.close();
		}

		if (conn != null) {
			conn.close();
		}

	}

}
