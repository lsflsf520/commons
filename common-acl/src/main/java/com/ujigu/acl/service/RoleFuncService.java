package com.ujigu.acl.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ujigu.acl.dao.RoleFuncDao;
import com.ujigu.acl.entity.RoleFunc;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

@Service
public class RoleFuncService extends AbstractBaseService<Integer, RoleFunc> {
    @Resource
    private RoleFuncDao roleFuncDao;

    @Override
    protected IBaseDao<Integer, RoleFunc> getBaseDao() {
        return roleFuncDao;
    }

    public Integer insertReturnPK(RoleFunc roleFunc) {
        roleFuncDao.insertReturnPK(roleFunc);
        return roleFunc.getPK();
    }

    public Integer doSave(RoleFunc roleFunc) {
        if (roleFunc.getPK() == null) {
            return this.insertReturnPK(roleFunc);
        }
        this.update(roleFunc);
        return roleFunc.getPK();
    }
    
    @Transactional(value="privilegeTransactionManager")
    public void updateRoleFuncs(int roleId, Integer[] funcIds, int webappId){
    	EhCacheTool.remove(DefaultCacheNS.ROLE_FUNC, roleId + "_" + webappId);
    	roleFuncDao.remove(roleId, webappId);
    	if(funcIds != null && funcIds.length > 0){
    		List<RoleFunc> roleFuncs = new ArrayList<>();
    		for(Integer funcId : funcIds){
    			RoleFunc roleFunc = new RoleFunc();
    			roleFunc.setRoleId(roleId);
    			roleFunc.setWebappId(webappId);
    			roleFunc.setFuncId(funcId);
    			roleFunc.setCreateTime(new Date());
    			
    			roleFuncs.add(roleFunc);
    		}
    		this.insertBatch(roleFuncs);
    	}
    	
    }
    
    public List<RoleFunc> loadByRoleId(int roleId, int webappId){
    	List<RoleFunc> roleFuncs = EhCacheTool.getValue(DefaultCacheNS.ROLE_FUNC, roleId + "_" + webappId);
    	if(CollectionUtils.isEmpty(roleFuncs)){
    		RoleFunc query = new RoleFunc();
    		query.setRoleId(roleId);
    		
    		roleFuncs = this.findByEntity(query);
    		
    		EhCacheTool.put(DefaultCacheNS.ROLE_FUNC, roleId + "_" + webappId, roleFuncs);
    	}
    	
    	return roleFuncs;
    }
    
    public List<Integer/*funcId*/> loadFuncIdsByRoleId(int roleId, int webappId){
    	List<RoleFunc> roleFuncs = this.loadByRoleId(roleId, webappId);
    	List<Integer> funcIds = new ArrayList<>();
    	for(RoleFunc rf : roleFuncs){
    		funcIds.add(rf.getFuncId());
    	}
    	
    	return funcIds;
    }
}