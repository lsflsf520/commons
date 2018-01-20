package com.ujigu.acl.datavalid.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.acl.service.WorkerDepartRoleService;

/**
 * 校验当前登陆用户有没有操作参数中的workerId的账号信息的权限
 * @author lsf
 *
 */
@Service
public class WorkerRolePrivLoader extends RolePrivLoader {
	
	
	@Resource
	private WorkerDepartRoleService workerDepartRoleService;
	
	@Override
	public String getKey() {
		return "worker_role";
	}

	@Override
	public String getCnName() {
		return "员工角色";
	}

	@Override
	public Set<String> requiredParams() {
		return new HashSet<>(Arrays.asList("workerId"));
	}

	@Override
	protected List<String> convertParamDatas(String requestUri, PrivContext context, String paramName, List<String> paramValues) {
		List<String/*roleIdStr*/> roleIds = new ArrayList<String>();
		for(String val : paramValues){
			List<Integer> workerRoleIds = workerDepartRoleService.loadMyRoleIds(new PrivContext(Integer.valueOf(val)));
			for(Integer roleId : workerRoleIds){
				roleIds.add(roleId + "");
			}
		}
		return roleIds;
	}

}
