package com.yisi.stiku.db.multi;

import javax.sql.DataSource;

import com.yisi.stiku.common.exception.BaseRuntimeException;

/**
 * 多数据源灾备路由类
 * @author shangfeng
 *
 */
public class DBKey {

	private final static long NORMAL_TAG = -1l;
	
	private Object dbKey;
	private Object dataSource;
	private long lastLostTime = NORMAL_TAG; //上次连接获取失败的时间,以便用于计算何时可以对连接重试
	private int weight = 1;
	
	private DBKey nextKey; //在获取本dbKey的连接失败之后，用于尝试的下一个dbKey
	
	public DBKey(Object dbKey, Object dataSource, DBKey nextDbKey){
		if(dbKey == null || dataSource == null || !(dataSource instanceof DataSource)){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "neithor dbKey nor dataSource cannot null to create an instance of " + DBKey.class.getName());
		}
		this.dbKey = dbKey;
		this.dataSource = dataSource;
		this.nextKey = nextDbKey;
		
		//TODO 权重可以在dbKey属性上解析出来,暂时没用
	}
	
	public Object getDbKey() {
		return dbKey;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public DBKey getNextKey() {
		return nextKey;
	}
	
	public boolean isSameDataSource(Object dataSource){
		return this.dataSource.equals(dataSource);
	}

	/**
	 * 在确定本dbKey已经失败之后，需要调用此方法将该dbKey标记为fail
	 */
	public void tagFail(){
		lastLostTime = System.currentTimeMillis();
	}
	
	/**
	 * 在确定本dbKey可以正常连接之后，需要调用此方法将该dbKey标记为normal
	 */
	public void tagNormal(){
		lastLostTime = NORMAL_TAG;
	}
	
	/**
	 * 
	 * @return 如果当前dbKey被标记为正常，或者dbKey已被标记为失败，但当前时间距离上次获取连接的时间超过3分钟，则返回true； 否则返回false
	 */
	public boolean isAvail() {
		return lastLostTime == NORMAL_TAG 
				|| System.currentTimeMillis() - lastLostTime > 180 * 1000l
				;
	}
	
	@Override
	public String toString() {
		return "DBKey[dbKey:" + dbKey + ",lastLostTime:" + lastLostTime + ", weight:" + weight + "]" ;
	}
	
}
