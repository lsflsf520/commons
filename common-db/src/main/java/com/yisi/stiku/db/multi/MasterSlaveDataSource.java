package com.yisi.stiku.db.multi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.yisi.stiku.common.utils.RandomUtil;

/**
 * 
 * @author shangfeng
 *
 */
public class MasterSlaveDataSource extends AbstractRoutingDataSource{
	
	private List<Object> slaveDSKeys;

	@Override
	protected Object determineCurrentLookupKey() {
		if(MSDSKeyHolder.isForceMaster() || slaveDSKeys == null || slaveDSKeys.isEmpty()){
			return null; //由于setMasterDataSource方法设置的默认的主库，所以这里返回null就说明使用主库，具体可以看父类AbstractRoutingDataSource的逻辑
		}
		int randomIndex = RandomUtil.rand(slaveDSKeys.size());
		return randomIndex >= slaveDSKeys.size() ? null : slaveDSKeys.get(randomIndex);
	}

	public void setSlaveDataSources(Map<Object, Object> slaveDataSources) {
		setTargetDataSources(slaveDataSources);
		if(slaveDataSources != null && !slaveDataSources.isEmpty()){
			Set<Object> keys = slaveDataSources.keySet();
			slaveDSKeys = new ArrayList<Object>();
			for(Object key : keys){
				slaveDSKeys.add(key);
			}
		}
	}

	public void setMasterDataSource(Object masterDataSource) {
		setDefaultTargetDataSource(masterDataSource);
	}
	
}
