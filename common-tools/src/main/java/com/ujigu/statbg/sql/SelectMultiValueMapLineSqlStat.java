package com.ujigu.statbg.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.data.MapData;

/**
 * 本组件是将查出来的每行数据，变为列式数据，具体举例如下：
 *   表格数据如下：
 *     user_id user_name age
 *       1       lsf      25
 *       2       xqc      20
 *       3       yc       23
 *   查出之后的结果为：
 *       user_id [1,2,3]
 *       user_name [lsf, xqc, yc]
 *       age [25,20,23]
 * @author shangfeng
 *
 */
public class SelectMultiValueMapLineSqlStat extends SelectMapLineSqlStat {

	@Override
	protected List<FlowData> handleResultSet(ResultSetMetaData meta, ResultSet rs) throws SQLException {

		Map<String, List<Object>> valueMap = new TreeMap<String, List<Object>>();
		while (rs.next()) {
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				String colName = meta.getColumnLabel(i);
				List<Object> valList = valueMap.get(colName);
				if (valList == null) {
					valList = new ArrayList<Object>();

					valueMap.put(colName, valList);
				}

				valList.add(rs.getObject(i));
			}
		}

		MapData data = new MapData();
		for (Entry<String, List<Object>> entry : valueMap.entrySet()) {
			data.put(entry.getKey(), entry.getValue());
		}

		List<FlowData> dataList = new ArrayList<FlowData>();
		dataList.add(data);

		return dataList;
	}
}
