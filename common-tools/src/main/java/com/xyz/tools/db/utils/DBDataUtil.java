package com.xyz.tools.db.utils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.RegexUtil;

public class DBDataUtil {

	private final static Logger LOG = LoggerFactory.getLogger(DBDataUtil.class);
	
	/**
	 * 在数据源中执行指定的sql语句
	 * @param dataSource 
	 * @param sql
	 * @return
	 */
	public static boolean execUpdate(DataSource dataSource,
			String sql){
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			
			LogUtils.debug("will exec sql %s", sql);
			stmt.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			LOG.error("sql:" + e.getMessage(), e);
			throw new BaseRuntimeException("DB_ERR", "sql执行失败", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		
	}

	/**
	 * 
	 * @param dataSource
	 *            数据源
	 * @param sql
	 *            查询语句
	 * @return
	 */
	public static List<Map<String/* fieldName */, Serializable/* value */>> loadData(DataSource dataSource,
			String sql) {
		
		LogUtils.debug("will exec sql %s", sql);
		List<Map<String, Serializable>> tableDataList = new ArrayList<>();

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = dataSource.getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Map<String/* fieldName */, Serializable/* value */> lineData = new HashMap<>();
				ResultSetMetaData metaData = rs.getMetaData();
				for (int index = 1; index <= metaData.getColumnCount(); index++) {
					String columnName = metaData.getColumnLabel(index);
					lineData.put(columnName, parseVal(rs.getString(columnName)));
				}

				tableDataList.add(lineData);
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		return tableDataList;
	}

	public static Serializable parseVal(String val) {
//		long longVal = 0l;
		if (RegexUtil.isInt(val) && !RegexUtil.isPhone(val) && ("0".equals(val = val.trim()) || !val.startsWith("0")) 
//				&& ( (!val.startsWith("-") && "2147483647".compareTo(val.startsWith("+") ? val.substring(1) : val)  >= 0) || (val.startsWith("-") && "2147483648".compareTo(val.substring(1)) >= 0) )
//				&& ( val.length() < 12 && (longVal = Long.valueOf(val)) >= Integer.MIN_VALUE && longVal <= Integer.MAX_VALUE)
				&& val.length() < String.valueOf(Long.MAX_VALUE).length()
				) {
			return Long.valueOf(val);
		}
		return val;
	}
	
	public static void main(String[] args) {
		System.out.println(parseVal("12345678901"));
	}

}

