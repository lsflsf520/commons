package com.ujigu.statbg.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.func.FuncContext;

/**
 * @author shangfeng
 *
 */
@SuppressWarnings("all")
public class SqlUtil {

	private final static Logger LOG = LoggerFactory.getLogger(SqlUtil.class);

	public static String getPreparedSql(String sql, List<String> paramNames, Map<String, List<FlowData>> paramMap) {

		String preparedSql = sql;
		if (paramNames == null || paramNames.size() <= 0) {
			return preparedSql;
		}

		for (String paramName : paramNames) {
			String quesStr = "?";
			Object value = ParamFindUtil.getValue(paramName, paramMap, 0, false);
			if (value instanceof List) { // 如果参数的值是一个List，那么需要根据List的个数来设置好sql的预编译“？”的数量
				int size = ((List) value).size();
				for (int index = 1; index < size; index++) {
					quesStr += ",?";
				}
			}
			preparedSql = RegexHelperUtil.replaceParamName(preparedSql, paramName, quesStr);
		}

		return preparedSql;
	}

	public static String buildExecSql(String sql, List<String> paramNames, Map<String, Serializable> lineData,
			Map<String, List<FlowData>> paramMap,
			int lineIndex,
			boolean repeatOneline, Map<String, FuncContext> funcMap) {

		String execSql = sql;
		if (paramNames == null || paramNames.size() <= 0) {
			return execSql;
		}
		for (String paramName : paramNames) {
			Serializable value = getValue(paramName, lineData, paramMap, lineIndex, repeatOneline);

			execSql = RegexHelperUtil.replaceParamName(execSql, paramName,
					(Serializable) converVal(funcMap, paramName, value));
		}

		return execSql;
	}

	private static Serializable getValue(String paramName, Map<String, Serializable> lineData,
			Map<String, List<FlowData>> paramMap, int lineIndex,
			boolean repeatOneline) {

		Serializable val = lineData.get(paramName);
		if (val != null) {
			return buildValue(val);
		}

		Object value = ParamFindUtil.getValue(paramName, paramMap, lineIndex,
				repeatOneline);

		return buildValue(value);
	}

	private static Serializable buildValue(Object value) {

		if (value instanceof List) {
			throw new BaseRuntimeException("NOT_SUPPORT_VALUE", "value " + value
					+ " is not supported here");
		} else if (value instanceof String) {
			return "'" + value + "'";
		} else if (value instanceof Date) {
			return "'" + DateUtil.getDateTimeStr((Date) value) + "'";
		}

		return (Serializable) value;
	}

	/**
	 * 
	 * @param pstmt
	 * @param paramMap
	 *            上下文参数
	 * @param paramNames
	 *            解析到的sql中的变量
	 * @param execIndex
	 *            当前执行的数据行号。比如paramNames中有个参数的值为一个集合，那么execIndex为该集合中数据对象的索引，
	 *            从0开始
	 * @param nullReplaceMap
	 *            当参数中的值为null时，将会从nullReplaceMap中找对应的默认值，如果这里边也没有，
	 *            那么就将该参数的值设置为null
	 * @param repeatOneLine
	 *            在填充参数时，如果某个参数的值是List，并且其中只有一个元素，那么在循环执行sql的时候，是否需要反复用第一行的数据；
	 *            默认为true，即需要
	 * @throws SQLException
	 */
	public static void setSqlValue(PreparedStatement pstmt,
			Map<String, List<FlowData>> paramMap, List<String> paramNames, int execIndex,
			Map<String, Serializable> nullReplaceMap, boolean repeatOneLine, Map<String, FuncContext> funcMap)
			throws SQLException {

		if (paramNames != null && paramNames.size() > 0) {
			Map<String, Object> pvMap = new TreeMap<String, Object>();
			List<Object> vlist = new ArrayList<Object>();
			int index = 1;
			for (String paramName : paramNames) {
				Object value = getValue(paramMap, paramName, execIndex, repeatOneLine, nullReplaceMap);

				value = (value == null && nullReplaceMap != null ? nullReplaceMap.get(paramName) : value);

				if (value instanceof List) { // 如果参数的值是一个List，那么需要根据List的个数来设置好sql的预编译“？”的数量
					for (Object val : (List) value) {
						val = val == null && nullReplaceMap != null ? nullReplaceMap.get(paramName) : val;
						pstmt.setObject(index++, converVal(funcMap, paramName, val));
					}
				} else {
					pstmt.setObject(index++, converVal(funcMap, paramName, value));
				}

				pvMap.put(paramName, value);
				vlist.add(value);
			}
			LOG.debug("prepared params:" + vlist);
			LOG.debug("kv params:" + pvMap);
		}

	}
	
	public static Object getValue(Map<String, List<FlowData>> paramMap, String paramName, int execIndex, boolean repeatOneLine, Map<String, Serializable> nullReplaceMap){
		Object value = ParamFindUtil.getValue(paramName, paramMap, execIndex, repeatOneLine);

		value = (value == null && nullReplaceMap != null ? nullReplaceMap.get(paramName) : value);
		
		return value;
	}

	private static Object converVal(Map<String, FuncContext> funcMap, String paramName, Object value) {

		if (funcMap == null || funcMap.get(paramName) == null) {
			return value;
		}

		FuncContext context = funcMap.get(paramName);
		return context.getFunc().convert(value, context.getParamMap());
	}

}
