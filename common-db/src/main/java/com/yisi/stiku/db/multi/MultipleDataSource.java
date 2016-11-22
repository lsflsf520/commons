package com.yisi.stiku.db.multi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.yisi.stiku.common.exception.BaseRuntimeException;

/**
 * 
 * @author shangfeng
 *
 */
public class MultipleDataSource extends AbstractRoutingDataSource{

	private final static Logger LOG = LoggerFactory.getLogger(MultipleDataSource.class);
	
	private List<DBKey> dsKeys = new ArrayList<DBKey>();
	private ThreadLocal<DBKey> currDBKey = new ThreadLocal<DBKey>();
	
	@Override
	protected Object determineCurrentLookupKey() {
		DBKey dbKey = currDBKey.get();
		if(dbKey == null){
			dbKey = selectDBKey();
		}else if(!dbKey.isAvail()){
			dbKey = dbKey.getNextKey();
			if(dbKey == null || !dbKey.isAvail()){
				dbKey = selectDBKey();
			}
		}
		
		return dbKey == null ? null : dbKey.getDbKey();
	}
	
	private DBKey selectDBKey(){
		DBKey dbKey = null;
		int i = 0;
		while(i++ < 10){
			int randomIndex = RandomUtils.nextInt(dsKeys.size());
			dbKey = dsKeys.get(randomIndex);
			if(dbKey.isAvail()){
				break;
			}
		}
		
		if(dbKey == null || !dbKey.isAvail()){
			throw new BaseRuntimeException("NO_AVAIL_DATASOURCE", "没有可用的数据源！");
		}
		
		currDBKey.set(dbKey);
		
		return dbKey;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return selectAvailConn(null, null);
	}
	
	private Connection selectAvailConn(String username, String password){
		Connection conn = null;
		int tryTimes = 0;
		while(tryTimes++ < 3){
			conn = selectConn(null, null);
			if(conn != null){
				DBKey dbKey = currDBKey.get();
				currDBKey.remove();
				
				LOG.debug("select avail db connection with dbKey '" + dbKey.getDbKey() + "'");
//				System.out.println("select avail db connection with dbKey '" + dbKey.getDbKey() + "'");
				break;
			}
		}
		
		if(conn == null){
			currDBKey.remove();
			throw new BaseRuntimeException("DB_CONN_ERROR", "数据库连接获取失败！");
		}
		
		return conn;
	}
	
	private Connection selectConn(String username, String password){
		Connection conn = null;
		DataSource ds = null;
		try{
			ds = determineTargetDataSource();
		}catch(IllegalStateException e){
			LOG.warn("get dataSource with dbKey "+(currDBKey.get() == null ? "unknown" : currDBKey.get().getDbKey())+" failure", e);
		}
		
		DBKey dbKey = currDBKey.get();
		
		if(ds != null){
			int tryTimes = 0;
			while(tryTimes++ < 3){ //在获取连接失败后，最多允许尝试3次
				try{
					if(StringUtils.isNotBlank(username)){
						conn = ds.getConnection(username, password);
					}else{
						conn = ds.getConnection();
					}
					break; //链接获取正常，则跳出循环
				}catch(SQLException e){
					LOG.warn("getConnection failure for dbKey '" + dbKey.getDbKey() + "' for " + tryTimes + " times.", e);
				}
				
				try {
					Thread.sleep(400);
				} catch (InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		
		if(conn == null || (dbKey != null && !dbKey.isSameDataSource(ds))){
			LOG.warn("dataSource with dbKey "+(dbKey == null ? "unknown" : dbKey.getDbKey())+" has lost connection");
			dbKey.tagFail();
		}else{
			dbKey.tagNormal();
		}
		
		return conn;
	}
	
	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return selectAvailConn(username, password);
	}

	public void setTargetDataSources(Map<Object, Object> targetDataSources) {
		super.setTargetDataSources(targetDataSources);
		DBKey nextDbKey = null;
		for(Object dbKeyObj : targetDataSources.keySet()){
			DBKey dbKey = new DBKey(dbKeyObj, targetDataSources.get(dbKeyObj), nextDbKey);
			
			nextDbKey = dbKey; //在这记录数据库的key，用于建立一条dbKey的链表，在获取链接失败之后便于故障转移
			int weight = dbKey.getWeight();
			for(int i = 0; i < weight; i++){
				dsKeys.add(dbKey);
			}
		}
	}

}
