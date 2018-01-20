package com.ujigu.acl.service;

import com.ujigu.acl.dao.DataPrivDao;
import com.ujigu.acl.entity.DataPriv;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DataPrivService extends AbstractBaseService<Integer, DataPriv> {
    @Resource
    private DataPrivDao dataPrivDao;

    @Override
    protected IBaseDao<Integer, DataPriv> getBaseDao() {
        return dataPrivDao;
    }

    public Integer insertReturnPK(DataPriv dataPriv) {
        dataPrivDao.insertReturnPK(dataPriv);
        return dataPriv.getPK();
    }

    public Integer doSave(DataPriv dataPriv) {
        if (dataPriv.getPK() == null) {
            return this.insertReturnPK(dataPriv);
        }
        this.update(dataPriv);
        return dataPriv.getPK();
    }
    
    /**
     * 
     * @param workerId 工作人员ID
     * @param loaderKey 数据权限加载器的唯一键
     * @return
     */
    public Set<String> loadMyDatas(int workerId, String loaderKey){
    	Set<String> mydatas = EhCacheTool.getValue(DefaultCacheNS.DATA_PRIV, workerId + "_" + loaderKey);
    	if(CollectionUtils.isEmpty(mydatas)){
    		DataPriv query = new DataPriv();
    		query.setLoader(loaderKey);
    		query.setWorkerId(workerId);
    		
    		DataPriv mydataPriv = this.findOne(query);
    		
    		mydatas = mydataPriv.getWorkerDatas();
    		
    		EhCacheTool.put(DefaultCacheNS.DATA_PRIV, workerId + "_" + loaderKey, mydatas);
    	}
    	
    	return mydatas;
    }
    
    public Set<Integer> loadMyIntDatas(int workerId, String checkerKey){
    	Set<String> mydatas = loadMyDatas(workerId, checkerKey);
    	Set<Integer> myintDatas = new LinkedHashSet<>();
    	for(String data : mydatas){
    		myintDatas.add(Integer.valueOf(data));
    	}
    	
    	return myintDatas;
    }
}