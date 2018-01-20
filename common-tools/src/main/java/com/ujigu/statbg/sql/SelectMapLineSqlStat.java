package com.ujigu.statbg.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.IPUtil;
import com.ujigu.statbg.FlowData;
import com.ujigu.statbg.data.MapData;

/**
 * 
 * @author lsf
 *
 */
public class SelectMapLineSqlStat extends SqlStat {

	private final static Logger LOG = LoggerFactory.getLogger(SelectMapLineSqlStat.class);

	@Override
	protected List<FlowData> execute(PreparedStatement pstmt) throws SQLException {

		ResultSet rs = null;

		try {
			rs = pstmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();

			return handleResultSet(meta, rs);
		} catch (SQLException e) {
			LOG.error("execute query error, serverIP:" + IPUtil.getLocalIp(), e);
			throw e;
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error("close ResultSet error, serverIP:" + IPUtil.getLocalIp(), e);
				}
			}
		}

	}

	protected List<FlowData> handleResultSet(ResultSetMetaData meta, ResultSet rs) throws SQLException {

		List<FlowData> results = new ArrayList<FlowData>();
		while (rs.next()) {
			MapData mapData = new MapData();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				mapData.put(meta.getColumnLabel(i), rs.getObject(i));
			}

			results.add(mapData);
		}

		return results;
	}

}
