package com.ujigu.acl.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ujigu.acl.constant.RoleEnum;
import com.ujigu.acl.dao.WorkerDepartRoleDao;
import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.acl.entity.Depart;
import com.ujigu.acl.entity.Role;
import com.ujigu.acl.entity.WorkerDepartRole;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

@Service
public class WorkerDepartRoleService extends AbstractBaseService<Integer, WorkerDepartRole> {
    @Resource
    private WorkerDepartRoleDao workerDepartRoleDao;
    
    @Resource
    private DepartService departService;
    
    @Resource
    private RoleService roleService;
    
    @Resource
    private WebappService webappService;
    
    @Override
    protected IBaseDao<Integer, WorkerDepartRole> getBaseDao() {
        return workerDepartRoleDao;
    }

    public Integer insertReturnPK(WorkerDepartRole workerDepartRole) {
        workerDepartRoleDao.insertReturnPK(workerDepartRole);
        return workerDepartRole.getPK();
    }

    public Integer doSave(WorkerDepartRole workerDepartRole) {
    	WorkerDepartRole query = new WorkerDepartRole();
    	query.setWorkerId(workerDepartRole.getWorkerId());
    	query.setDepartId(workerDepartRole.getDepartId());
    	
    	//同一个worker在同一个departId下只能有一个角色
    	WorkerDepartRole dbData = this.findOne(query);
    	if(dbData == null){
        	workerDepartRole.setCreateTime(new Date());
            return this.insertReturnPK(workerDepartRole);
        }
    	dbData.setRoleId(workerDepartRole.getRoleId());
        this.update(dbData);
        return dbData.getPK();
    }
    
    public boolean removeMyDepartIds(int workerId, Integer... departIds){
    	if(departIds == null || departIds.length <= 0){
//    		return false;
    		return workerDepartRoleDao.removeMyDepartIds(workerId) >= 0;
    	}
    	return workerDepartRoleDao.removeMyDepartIds(workerId, departIds) >= 0;
    }
    
    public int loadRoleId(int departId, int workerId){
    	WorkerDepartRole query = new WorkerDepartRole();
    	query.setDepartId(departId);
    	query.setWorkerId(workerId);
    	
    	WorkerDepartRole dbData = this.findOne(query);
    	
    	return dbData == null ? 0 : dbData.getRoleId();
    }
    
    public List<WorkerDepartRole> loadMyWdrs(int workerId){
    	List<WorkerDepartRole> wdrs = EhCacheTool.getValue(DefaultCacheNS.WORKER_DEPART_ROLE, workerId);
    	if(CollectionUtils.isEmpty(wdrs)){
    		WorkerDepartRole query = new WorkerDepartRole();
    		query.setWorkerId(workerId);
    		
    		wdrs = this.findByEntity(query);
    		
    		EhCacheTool.put(DefaultCacheNS.WORKER_DEPART_ROLE, workerId, wdrs);
    	}
    	
    	return wdrs;
    }
    
    public boolean isSuperAdmin(int workerId){
    	List<Integer> roleIds = loadMyRoleIds(workerId);
    	
    	return roleIds.contains(RoleEnum.SUPER_ADMIN.getCode());
    }
    
    public boolean isDepartAdmin(int workerId){
    	List<Integer> roleIds = loadMyRoleIds(workerId);
    	
    	return roleIds.contains(RoleEnum.DEPART_ADMIN.getCode());
    }
    
    private List<Integer/*roleId*/> loadMyRoleIds(int workerId){
    	List<Integer> roleIds = new ArrayList<>();
    	List<WorkerDepartRole> wdrs = loadMyWdrs(workerId);
		for(WorkerDepartRole wdr : wdrs){
			roleIds.add(wdr.getRoleId());
		}
    	
    	return roleIds;
    }
    
    public List<Integer/*roleId*/> loadMyRoleIds(PrivContext context){
    	Boolean hasSuperAdmin = context.getValue("hasSuperAdmin");
    	hasSuperAdmin = hasSuperAdmin == null ? hasSuperAdmin(context.getWorkerId()) : hasSuperAdmin;
    	if(hasSuperAdmin){
    		Integer departId = context.getDepartId();
    		if(departId == null){
    			throw new BaseRuntimeException("ILLEGAL_CONTEXT", "不支持的操作", "departId must be provided");
    		}
    		DataTree<Integer, Role> dataTree = roleService.loadRoleTree(departId);
    		return dataTree.getNodeIds();
    	}
    	
    	return loadMyRoleIds(context.getWorkerId());
    }
    
    /**
     * 
     * @return
     */
    /*public List<IntegerroleId> loadMyRoleIdIncludeChild(int workerId, int webappId){
    	List<Integer> roleIds = loadMyRoleIds(workerId);
    	
    	Webapp webapp = webappService.findById(webappId);
    	if(webapp == null){
    		throw new BaseRuntimeException("NOT_EXIST", "数据访问出错", "not exist webapp for id " + webappId);
    	}
    	
    	DataTree<Integer, Role> roleTree = roleService.loadRoleTree(webapp.getDepartId());
    	
    	return roleTree.getNodeIdIncludeChild(roleIds);
    }*/
    
    
    /**
     * 查询出工作人员归属的直接部门或公司ID列表
     * @param workerId
     * @return
     */
    private List<Integer/*departId*/> loadMyDepartIds(int workerId){
    	List<Integer> departIds = new ArrayList<>();
    	List<WorkerDepartRole> wdrs = loadMyWdrs(workerId);
		for(WorkerDepartRole wdr : wdrs){
			departIds.add(wdr.getDepartId());
		}
    	
    	return departIds;
    }
    
    public boolean hasSuperAdmin(int workerId){
//    	List<Integer> roleIds = loadMyRoleIdIncludeChild(workerId, webappId);
    	List<Integer> roleIds = loadMyRoleIds(workerId);
    	
    	return roleService.hasSuperAdmin(new HashSet<>(roleIds));
    }
    
    public List<Integer/*departId*/> loadMyDepartIds(PrivContext context){
    	Boolean hasSuperAdmin = context.getValue("hasSuperAdmin");
    	hasSuperAdmin = hasSuperAdmin == null ? hasSuperAdmin(context.getWorkerId()) : hasSuperAdmin;
    	if(hasSuperAdmin){
    		DataTree<Integer, Depart> departTree = departService.loadDepartTree();
    		return departTree.getNodeIds();
    	}
    	
        return loadMyDepartIds(context.getWorkerId());
    }
    
    /**
     * 查询出工作人员归属的直接部门或公司ID，以及部门或公司的所有父级节点ID，在校验当前用户时候有本webapp的访问权限的时候有用到
     * @param workerId
     * @return
     */
    public List<Integer/*departId*/> loadMyDepartIdIncludeParent(int workerId){
    	DataTree<Integer, Depart> departTree = departService.loadDepartTree();
    	if(hasSuperAdmin(workerId)){
    		return departTree.getNodeIds();
    	}
    	List<Integer> myDepartIds = loadMyDepartIds(workerId);
    	
    	return departTree.getNodeIdIncludeParent(myDepartIds);
    }
}