package com.ujigu.secure.db.typeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.ujigu.secure.common.bean.CommonStatus;

public class CommonStatusHandler extends BaseTypeHandler<CommonStatus>{

	@Override
	public CommonStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
		Byte type = rs.getByte(columnName);
		if(rs.wasNull()){
			return null;
		}
		return CommonStatus.getByDbCode(type);
	}

	@Override
	public CommonStatus getNullableResult(ResultSet rs, int i) throws SQLException {
		Byte type = rs.getByte(i);
		if(rs.wasNull()){
			return null;
		}
		return CommonStatus.getByDbCode(type);
	}

	@Override
	public CommonStatus getNullableResult(CallableStatement cs, int i) throws SQLException {
		Byte type = cs.getByte(i);
		if(cs.wasNull()){
			return null;
		}
		return CommonStatus.getByDbCode(type);
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, CommonStatus param, JdbcType jdbcType)
			throws SQLException {
		ps.setByte(i, param.getDbCode());
		
	}

	
	
}
