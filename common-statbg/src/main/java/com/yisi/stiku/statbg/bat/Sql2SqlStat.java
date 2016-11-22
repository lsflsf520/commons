package com.yisi.stiku.statbg.bat;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.statbg.FlowData;
import com.yisi.stiku.statbg.bat.reader.LineReader;
import com.yisi.stiku.statbg.bat.reader.SqlMapLineReader;
import com.yisi.stiku.statbg.bat.writer.BatWriter;
import com.yisi.stiku.statbg.bat.writer.SqlBatWriter;

public class Sql2SqlStat extends BatStat {

	protected DataSource srcDataSource;
	protected DataSource targetDataSource;

	protected String selectSql;
	protected String insertPreSql;
	protected String insertSuffixSql;
	protected String updateSql;

	@Override
	protected LineReader getLineReader(Map<String, List<FlowData>> paramMap) {

		return new SqlMapLineReader(srcDataSource, selectSql, paramMap);
	}

	@Override
	protected BatWriter getBatchWriter(Map<String, List<FlowData>> paramMap) {

		return new SqlBatWriter(targetDataSource, insertPreSql, insertSuffixSql, paramMap, repeatOneLine, updateSql);
	}

	public DataSource getSrcDataSource() {

		return srcDataSource;
	}

	public void setSrcDataSource(DataSource srcDataSource) {

		this.srcDataSource = srcDataSource;
	}

	public DataSource getTargetDataSource() {

		return targetDataSource;
	}

	public void setTargetDataSource(DataSource targetDataSource) {

		this.targetDataSource = targetDataSource;
	}

	public String getSelectSql() {

		return selectSql;
	}

	public void setSelectSql(String selectSql) {

		this.selectSql = selectSql;
	}

	public void setInsertSql(String insertSql) {

		String[] parts = insertSql.split("\\s+(?i)values\\s*");

		if (parts == null || parts.length < 2) {
			throw new BaseRuntimeException("NOT_SUPPORT_SQL", insertSql + " is not an insert sql for orderNO "
					+ this.getOrderNO());
		}

		insertPreSql = parts[0].trim();

		insertSuffixSql = parts[1].trim();
	}

	public String getUpdateSql() {

		return updateSql;
	}

	public void setUpdateSql(String updateSql) {

		this.updateSql = updateSql;
	}

}
