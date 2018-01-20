package com.ujigu.acl.service;

import com.ujigu.acl.constant.RoleEnum;
import com.ujigu.acl.dao.RoleDao;
import com.ujigu.acl.entity.Role;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.db.bean.PageData;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class RoleService extends AbstractBaseService<Integer, Role> {
	
//	private final static int ROLE_SUPER_ADMIN_ID = 1; //超级管理员
//	private final static int ROLE_DEPART_ADMIN_ID = 2; //公司管理员
//	public final static int ROLE_ANON_ID = 3;
	
    @Resource
    private RoleDao roleDao;
    
    @Override
    protected IBaseDao<Integer, Role> getBaseDao() {
        return roleDao;
    }

    public Integer insertReturnPK(Role role) {
        roleDao.insertReturnPK(role);
        return role.getPK();
    }

    public Integer doSave(Role role) {
    	EhCacheTool.remove(DefaultCacheNS.SYS_ROLE, role.getDepartId());
    	role.setLastUptime(new Date());
        if (role.getPK() == null) {
        	Role query = new Role();
        	query.setParentId(role.getParentId());
        	query.setName(role.getName());
        	
        	Role dbData = this.findOne(role);
        	if(dbData != null){
        		throw new BaseRuntimeException("DATA_CONFLICT", "该角色已存在", "roleName:" + role.getName() + ",parentId:" + role.getParentId());
        	}
        	
        	role.setStatus(CommonStatus.NORMAL);
        	role.setCreateTime(role.getLastUptime());
            return this.insertReturnPK(role);
        }
        this.update(role);
        return role.getPK();
    }
    
    /*@Override
	public PageData<Role> findByPage(Role t) {
		List<Role> datas = this.findByEntity(t);
		
		DataTree<Integer, Role> dataTree = new DataTree<>(datas);
		
		return new PageData<>(dataTree.getRoots(), 1, datas.size(), datas.size(), 1);
	}*/
    
    /**
     * 判断是否拥有超级管理员角色
     * @param roleId
     * @return
     */
    public boolean hasSuperAdmin(Set<Integer> roleIds){
    	return !CollectionUtils.isEmpty(roleIds) && roleIds.contains(RoleEnum.SUPER_ADMIN.getCode());
    }
    
    /**
     * 判断是否拥有公司管理员角色
     * @param roleId
     * @return
     */
    public boolean hasDepartAdmin(Set<Integer> roleIds){
    	return !CollectionUtils.isEmpty(roleIds) && roleIds.contains(RoleEnum.DEPART_ADMIN.getCode());
    }
    
    /**
     * 
     * @param departId
     * @return
     */
    public List<Role> loadRoles(int departId){
    	List<Role> roles = EhCacheTool.getValue(DefaultCacheNS.SYS_ROLE, departId);
    	if(CollectionUtils.isEmpty(roles)){
    		Role query = new Role();
    		query.setStatus(CommonStatus.NORMAL);
    		query.setDepartId(departId);
    		
    		roles = this.findByEntity(query);
    		
    		EhCacheTool.put(DefaultCacheNS.SYS_ROLE, departId, roles);
    	}
    	
    	return roles;
    }
    
    /**
     * 加载某个公司或部门下的角色树
     * @param departId
     * @return
     */
    public DataTree<Integer, Role> loadRoleTree(int departId){
    	List<Role> roles = this.loadRoles(departId);
    	
    	return new DataTree<>(roles);
    }
    
}