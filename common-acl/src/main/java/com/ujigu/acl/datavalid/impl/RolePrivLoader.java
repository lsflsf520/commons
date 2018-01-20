package com.ujigu.acl.datavalid.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ujigu.acl.datavalid.AbstractDataPrivLoader;
import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.acl.entity.Role;
import com.ujigu.acl.service.RoleService;
import com.ujigu.acl.service.WebappService;
import com.ujigu.acl.service.WorkerDepartRoleService;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.exception.BaseRuntimeException;

@Service
public class RolePrivLoader extends AbstractDataPrivLoader<Integer,Role> {
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private WorkerDepartRoleService workerDepartRoleService;
	
	@Resource
	private WebappService webappService;

	@Override
	public String getKey() {
		return "role";
	}

	@Override
	public String getCnName() {
		return "角色";
	}

	@Override
	public Set<String> requiredParams() {
		return new HashSet<>(Arrays.asList("roleId"));
	}

	@Override
	public DataTree<Integer, Role> loadDataTree(PrivContext context) {
		Integer departId = context.getDepartId();
		if(departId == null){
    		throw new BaseRuntimeException("ILLEGAL_CONTEXT", "不支持的操作", "departId must be provided");
		}
		
		return roleService.loadRoleTree(departId);
	}
	
	@Override
	public Set<Integer> loadMyDatas(PrivContext context) {
		List<Integer> myRoleIds = workerDepartRoleService.loadMyRoleIds(context);
		
		return new HashSet<>(myRoleIds);
	}

}
