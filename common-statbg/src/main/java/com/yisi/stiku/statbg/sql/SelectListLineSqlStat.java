package com.yisi.stiku.statbg.sql;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.data.ListData;

/**
 * 
 * @author lsf
 *
 */
public class SelectListLineSqlStat extends SqlStat {

	private final static Logger LOG = LoggerFactory.getLogger(SelectListLineSqlStat.class);

	@Override
	protected List<FlowData> execute(PreparedStatement pstmt) throws SQLException {

		ResultSet rs = null;
		List<FlowData> results = new ArrayList<FlowData>();
		try {
			rs = pstmt.executeQuery();
			ResultSetMetaData meta = rs.getMetaData();
			while (rs.next()) {
				ListData lineData = new ListData();
				for (int i = 1; i <= meta.getColumnCount(); i++) {
					lineData.add((Serializable) rs.getObject(i));
				}

				results.add(lineData);
			}
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

		return results;
	}

}
