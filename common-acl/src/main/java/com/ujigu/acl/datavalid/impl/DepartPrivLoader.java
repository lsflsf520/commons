package com.ujigu.acl.datavalid.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.ujigu.acl.datavalid.AbstractDataPrivLoader;
import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.acl.entity.Depart;
import com.ujigu.acl.service.DepartService;
import com.ujigu.acl.service.WorkerDepartRoleService;
import com.ujigu.secure.common.bean.DataTree;

/**
 * 
 * @author shangfeng
 *
 */
@Service
public class DepartPrivLoader extends AbstractDataPrivLoader<Integer, Depart>{
	
	@Resource
	private DepartService departService;
	
	@Resource
	private WorkerDepartRoleService workerDepartRoleService;

	@Override
	public String getKey() {
		return "depart";
	}

	@Override
	public String getCnName() {
		return "公司/部门";
	}
	
	@Override
	public Set<String> requiredParams() {
		return new HashSet<>(Arrays.asList("departId"));
	}

	@Override
	public DataTree<Integer, Depart> loadDataTree(PrivContext context) {
		return departService.loadDepartTree();
	}
	
	@Override
	public Set<Integer> loadMyDatas(PrivContext context) {
		List<Integer> departIds = workerDepartRoleService.loadMyDepartIds(context);
		
		return new HashSet<>(departIds);
	}

}
