package com.ujigu.acl.service;

import com.ujigu.acl.dao.WebappDao;
import com.ujigu.acl.entity.Webapp;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class WebappService extends AbstractBaseService<Integer, Webapp> {
    @Resource
    private WebappDao webappDao;
    
    @Resource
    private WorkerDepartRoleService workerDepartRoleService;

    @Override
    protected IBaseDao<Integer, Webapp> getBaseDao() {
        return webappDao;
    }

    public Integer insertReturnPK(Webapp webapp) {
        webappDao.insertReturnPK(webapp);
        return webapp.getPK();
    }

    public Integer doSave(Webapp webapp) {
    	EhCacheTool.remove(DefaultCacheNS.SYS_WEBAPP);
    	webapp.setLastUptime(new Date());
        if (webapp.getPK() == null) {
        	Webapp query = new Webapp();
        	query.setName(webapp.getName());
        	query.setDepartId(webapp.getDepartId());
        	
        	Webapp dbData = this.findOne(query);
        	if(dbData != null){
        		throw new BaseRuntimeException("DATA_CONFLICT", "该web应用名已存在", "webapp name:" + webapp.getName());
        	}
        	
        	webapp.setStatus(CommonStatus.NORMAL);
        	webapp.setCreateTime(webapp.getLastUptime());
            return this.insertReturnPK(webapp);
        }
        EhCacheTool.remove(DefaultCacheNS.SYS_WEBAPP, webapp.getPK());
        this.update(webapp);
        return webapp.getPK();
    }
    
    @Override
    public Webapp findById(Integer pk) {
    	Webapp webapp = EhCacheTool.getValue(DefaultCacheNS.SYS_WEBAPP, pk);
    	if(webapp == null){
    		webapp = getBaseDao().findByPK(pk);
    		
    		if(webapp != null){
    			EhCacheTool.put(DefaultCacheNS.SYS_WEBAPP, pk, webapp);
    		}
    	}
    	
    	return webapp;
    }
    
    /**
     * 判断workerId的工作人员是否有webappId的访问权限
     * @param departId
     * @return
     */
    public boolean hasWebAppPriv(int workerId, int webappId){
    	Webapp webapp = this.findById(webappId);
    	if(webapp == null){
    		return false;
    	}
    	
    	List<Integer> departIds = workerDepartRoleService.loadMyDepartIdIncludeParent(workerId);
    	
    	return !CollectionUtils.isEmpty(departIds) && departIds.contains(webapp.getDepartId());
    }
    
    public List<Webapp> loadWebappByDepartId(Integer... departIds){
    	if(departIds == null || departIds.length <= 0){
    		return new ArrayList<>();
    	}
    	boolean hasZeroDepartId = false; //判断departIds中是否有0的departId，如果有departId为0，则意味着直接查询出所有webapp即可
    	for(Integer departId : departIds){
    		if(departId == 0){
    			hasZeroDepartId = true;
    			break;
    		}
    	}
    	Webapp query = new Webapp();
    	if(hasZeroDepartId){
    		query.setStatus(CommonStatus.NORMAL);
    	} else {
    		query.addQueryParam("departIds", departIds);
    	}
    	
    	return this.findByEntity(query);
    }
    
    public Set<Integer> loadWebappIdByDepartId(Integer... departIds){
    	List<Webapp> webapps = loadWebappByDepartId(departIds);
    	
    	Set<Integer> webappIds = new HashSet<>();
    	if(!CollectionUtils.isEmpty(webapps)){
    		for(Webapp webapp : webapps){
    			webappIds.add(webapp.getId());
    		}
    	}
    	
    	return webappIds;
    }
    
    public DataTree<Integer, Webapp> loadWebappTree(){
    	List<Webapp> webapps = EhCacheTool.getValue(DefaultCacheNS.SYS_WEBAPP);
    	if(CollectionUtils.isEmpty(webapps)){
    		Webapp query = new Webapp();
    		query.setStatus(CommonStatus.NORMAL);

    		webapps = this.findByEntity(query);
    		
    		EhCacheTool.put(DefaultCacheNS.SYS_WEBAPP, webapps);
    	}
    	
    	return new DataTree<>(webapps);
    }
    
    public Integer loadDepartId(int webappId){
    	Webapp dbData = findById(webappId);
    	
    	return dbData == null ? null : dbData.getDepartId();
    }
    
    
}