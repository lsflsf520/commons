package com.ujigu.acl.service;

import com.ujigu.acl.dao.WorkerFuncDao;
import com.ujigu.acl.entity.WorkerFunc;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class WorkerFuncService extends AbstractBaseService<Integer, WorkerFunc> {
    @Resource
    private WorkerFuncDao workerFuncDao;

    @Override
    protected IBaseDao<Integer, WorkerFunc> getBaseDao() {
        return workerFuncDao;
    }

    public Integer insertReturnPK(WorkerFunc workerFunc) {
        workerFuncDao.insertReturnPK(workerFunc);
        return workerFunc.getPK();
    }

    public Integer doSave(WorkerFunc workerFunc) {
        if (workerFunc.getPK() == null) {
            return this.insertReturnPK(workerFunc);
        }
        this.update(workerFunc);
        return workerFunc.getPK();
    }
    
    public List<WorkerFunc> loadMyUserFuncs(int workerId, int webappId){
    	List<WorkerFunc> userFuncs = EhCacheTool.getValue(DefaultCacheNS.WORKER_FUNC, workerId + "_" + webappId);
    	if(CollectionUtils.isEmpty(userFuncs)){
    		WorkerFunc query = new WorkerFunc();
    		query.setWorkerId(workerId);
    		query.setWebappId(webappId);
    		
    		userFuncs = this.findByEntity(query);
    		
    		EhCacheTool.put(DefaultCacheNS.WORKER_FUNC, workerId + "_" + webappId, userFuncs);
    	}
    	
    	return userFuncs;
    }
    
    public List<Integer/*funcId*/> loadMyFuncIds(int workerId, int webappId){
    	List<WorkerFunc> userFuncs = loadMyUserFuncs(workerId, webappId);
    	List<Integer> funcIds = new ArrayList<>();
    	for(WorkerFunc uf : userFuncs){
    		funcIds.add(uf.getFuncId());
    	}
    	
    	return funcIds;
    }
}