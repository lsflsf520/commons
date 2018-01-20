package com.ujigu.acl.datavalid.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ujigu.acl.datavalid.AbstractDataPrivLoader;
import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.acl.entity.Webapp;
import com.ujigu.acl.service.WebappService;
import com.ujigu.acl.service.WorkerDepartRoleService;
import com.ujigu.secure.common.bean.DataTree;

@Service
public class WebappPrivLoader extends AbstractDataPrivLoader<Integer,Webapp>{
	
	@Resource
	private WebappService webappService;
	
	@Resource
	private WorkerDepartRoleService workerDepartRoleService;

	@Override
	public String getKey() {
		return "webapp";
	}

	@Override
	public String getCnName() {
		return "web应用";
	}

	@Override
	public Set<String> requiredParams() {
		return new HashSet<>(Arrays.asList("webappId"));
	}

	@Override
	public DataTree<Integer, Webapp> loadDataTree(PrivContext context) {
		
		return webappService.loadWebappTree();
	}
	
	@Override
	public Set<Integer> loadMyDatas(PrivContext context) {
		List<Integer> myDepartIds = workerDepartRoleService.loadMyDepartIdIncludeParent(context.getWorkerId());
		
		return webappService.loadWebappIdByDepartId(myDepartIds.toArray(new Integer[0]));
	}

}
