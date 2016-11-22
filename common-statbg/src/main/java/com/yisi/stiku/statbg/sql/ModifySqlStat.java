package com.yisi.stiku.statbg.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.data.ListData;

/**
 * 可以执行插入、修改和删除操作的sql语句
 * 
 * @author lsf
 *
 */
public class ModifySqlStat extends SqlStat {

	private final static Logger LOG = LoggerFactory.getLogger(ModifySqlStat.class);

	@Override
	protected List<FlowData> execute(PreparedStatement pstmt) throws SQLException {

		List<FlowData> results = new ArrayList<FlowData>();
		int effectRows = 0;
		try {
			effectRows = pstmt.executeUpdate();
		} catch (SQLException e) {
			LOG.error("execute query error, serverIP:" + IPUtil.getLocalIp(), e);
			throw e;
		}
		ListData flowData = new ListData();
		flowData.add(effectRows);

		results.add(flowData);

		return results;
	}

}
