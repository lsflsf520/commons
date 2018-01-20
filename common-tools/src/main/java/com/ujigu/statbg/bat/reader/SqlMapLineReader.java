package com.ujigu.statbg.bat.reader;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

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
public class SqlMapLineReader implements LineReader {

	private final static Logger LOG = LoggerFactory
			.getLogger(SqlMapLineReader.class);

	private DataSource dataSource;
	private Connection conn;
	private PreparedStatement pstmt;
	private ResultSet rs;
	private String selectSql;
	private Map<String, List<FlowData>> paramMap;

	/**
	 * @throws SQLException
	 * 
	 */
	public SqlMapLineReader(DataSource dataSource, String selectSql, Map<String, List<FlowData>> paramMap)
	{

		this.dataSource = dataSource;
		this.selectSql = selectSql;
		this.paramMap = paramMap;

	}

	@Override
	public void open() throws Exception {

		List<String> paramNames = RegexHelperUtil.getParamNames(selectSql); // 获取sql中的所有变量，以便后边进行替换
		Map<String, FuncContext> funcMap = RegexHelperUtil.getKey2FuncMap(selectSql);
		String preparedSql = SqlUtil.getPreparedSql(selectSql, paramNames, paramMap);
		LOG.debug(LOG.isDebugEnabled() ? "origin sql:" + selectSql : null);
		LOG.debug(LOG.isDebugEnabled() ? "prepared sql:" + preparedSql : null);

		conn = dataSource.getConnection();
		pstmt = conn.prepareStatement(preparedSql);

		SqlUtil.setSqlValue(pstmt, paramMap, paramNames, 0, null, true, funcMap);

		rs = pstmt.executeQuery();

	}

	@Override
	public Map<String, Serializable> nextLine() throws SQLException {

		if (rs == null || !rs.next()) {
			return null;
		}
		Map<String, Serializable> lineMap = new HashMap<String, Serializable>();
		ResultSetMetaData meta = rs.getMetaData();
		for (int i = 1; i <= meta.getColumnCount(); i++) {
			lineMap.put(meta.getColumnLabel(i), (Serializable) rs.getObject(i));
		}

		return lineMap;
	}

	@Override
	public void close() throws Exception {

		if (conn != null) {
			conn.close();
		}

		if (pstmt != null) {
			pstmt.close();
		}

		if (rs != null) {
			rs.close();
		}

	}
}
