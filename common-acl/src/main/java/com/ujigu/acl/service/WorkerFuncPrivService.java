package com.ujigu.acl.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ujigu.acl.constant.RoleEnum;
import com.ujigu.acl.datavalid.DataPrivLoader;
import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.acl.datavalid.impl.RolePrivLoader;
import com.ujigu.acl.entity.Func;
import com.ujigu.acl.entity.Func.DataPrivConfig;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.bean.GlobalResultCode;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;

@Service
public class WorkerFuncPrivService implements ApplicationContextAware{
	
	@Resource
	private FuncService funcService;
	
	@Resource
	private RoleService roleService;
	
	@Resource
	private WorkerDepartRoleService workerDepartRoleService;
	
	@Resource
	private WorkerFuncService userFuncService;
	
	@Resource
	private RoleFuncService roleFuncService;
	
	@Resource
	private WebappService webappService;
	
	@Resource
	private RolePrivLoader rolePrivLoader;
	
	private Map<String/*loader key*/, DataPrivLoader> key2loaderMap = new HashMap<>();
	
	/**
	 * 返回指定用户拥有的菜单
	 * @param workerId
	 * @return
	 */
	public List<Func> loadMyMenus(int workerId, int webappId){
		
//		List<Integer> roleIds = workerDepartRoleService.loadMyRoleIdIncludeChild(workerId, webappId);
		Integer departId = webappService.loadDepartId(webappId);
		if(departId == null){
			throw new BaseRuntimeException("NOT_EXIST", "系统错误", "not exist departId for webappId " + webappId);
		}
		
		Set<Integer> roleIds = rolePrivLoader.loadMyDataIncludeChild(new PrivContext(workerId).putIfAbsentDepartId(departId));
		
		if(!roleService.hasSuperAdmin(roleIds)){ //对于非超级管理员， 需要判断当前用户是否有本app的权限，当前用户必须要在本app所属的depart中才行
			boolean hasWebappPriv = webappService.hasWebAppPriv(workerId, webappId);
			if(!hasWebappPriv){ 
				throw new BaseRuntimeException(GlobalResultCode.NO_PRIVILEGE);
			}
		}
		
		DataTree<Integer, Func> menuTree = funcService.loadMenuTree(webappId);
		
		List<Func> menus = null;
		List<Integer> funcIds = null;
		//超级管理员和公司管理员，无需校验权限，直接拥有所有菜单权限
		if(!roleService.hasSuperAdmin(roleIds) && !roleService.hasDepartAdmin(roleIds)){
			funcIds = loadMyFuncIds(workerId, webappId, roleIds);
			menus = menuTree.getRoots(funcIds);
		} else {
			menus = menuTree.getRoots();
		}
		
		return menus;
	}
	
	/**
	 * 剔除掉没有权限的菜单
	 * @param menus
	 * @param myFuncIds
	 */
	/*private void filterMenus(List<Func> menus, List<Integer> myFuncIds){
		Iterator<Func> itr = menus.iterator();
		while(itr.hasNext()){
			Func menu = itr.next();
			if(menu.hasChild()){
				filterMenus(menu.getChildren(), myFuncIds);
			} 
			
			if(!myFuncIds.contains(menu.getId()) && !menu.hasChild()){
				menus.remove(menu);
			}
		}
	}*/
	
	/**
	 * 
	 * @return 返回当前系统中存在的数据权限集合
	 */
	public Map<String, DataPrivLoader> getDataPrivLoaders(){
		
		return Collections.unmodifiableMap(this.key2loaderMap);
	}
	
	/**
	 * 校验当前登陆用户有没有权限操作当前的uri
	 * @param request
	 * @param workerId
	 * @param uri
	 * @return
	 */
	public boolean isPermit(HttpServletRequest request, int workerId, String uri, int webappId){
//		List<Integer> roleIds = workerDepartRoleService.loadMyRoleIdIncludeChild(workerId, webappId);
		Integer departId = webappService.loadDepartId(webappId);
		if(departId == null){
			throw new BaseRuntimeException("NOT_EXIST", "系统错误", "not exist departId for webappId " + webappId);
		}
		
		Set<Integer> roleIds = rolePrivLoader.loadMyDataIncludeChild(new PrivContext(workerId).putIfAbsentDepartId(departId));
		if(roleService.hasSuperAdmin(roleIds)){
			LogUtils.debug("no need priv check for workerId %d with super admin", workerId);
			return true;
		}
		
		List<Func> matchedFuncs = funcService.loadFuncsByUri(uri, webappId);
		if(CollectionUtils.isEmpty(matchedFuncs)){ //能匹配到对应的uri，则说明可以
			LogUtils.warn("not found any func for uri %s", uri);
			return false;
		}
		
		if(roleService.hasDepartAdmin(roleIds)){ //公司管理员只校验数据权限，默认拥有此webapp的所有功能权限
			for(Func func : matchedFuncs){
				if(!CollectionUtils.isEmpty(func.getDataPrivConfigs())){
					boolean result = checkDataPriv(request, func, workerId, uri, departId);
					if(!result){
						return false;
					}
				}
			}
			
			return true;
		}
		
		List<Integer> funcIds = loadMyFuncIds(workerId, webappId, roleIds);
		for(Func func : matchedFuncs){
			if(funcIds.contains(func.getId())){
				if(!CollectionUtils.isEmpty(func.getDataPrivConfigs())){
					//有一个功能权限校验通过的前提下，还需要进一步校验数据权限
					return checkDataPriv(request, func, workerId, uri, departId);
				}
				
				return true;
			}
		}
		
		LogUtils.warn("curr worker %d has no privilege with uri %s", workerId, uri);
		return false;
	}
	
	public List<Func> loadMyFuncTree(int workerId, int webappId){
//		List<Integer> roleIds = workerDepartRoleService.loadMyRoleIdIncludeChild(workerId, webappId);
		Integer departId = webappService.loadDepartId(webappId);
		if(departId == null){
			throw new BaseRuntimeException("NOT_EXIST", "系统错误", "not exist departId for webappId " + webappId);
		}
		Set<Integer> roleIds = rolePrivLoader.loadMyDataIncludeChild(new PrivContext(workerId).putIfAbsentDepartId(departId));
		
		DataTree<Integer, Func> funcTree = funcService.loadFuncTree(webappId);
		if(roleService.hasSuperAdmin(roleIds) || roleService.hasDepartAdmin(roleIds)){
			 return funcTree.getRoots();
		}
		
		List<Integer> funcIds = loadMyFuncIds(workerId, webappId, roleIds);
		
		return funcTree.getRoots(funcIds);
	}
	
	@SuppressWarnings("all")
	private boolean checkDataPriv(HttpServletRequest request, Func func, int workerId, String uri, int departId){
		//如果此功能需要数据权限校验，则还需要进一步校验完数据权限才行放行
		for(DataPrivConfig config : func.getDataPrivConfigs()){
			if(!CollectionUtils.isEmpty(config.getIgnoreUris()) && config.getIgnoreUris().contains(uri)){
				LogUtils.debug("ignore data priv check for uri %s, funcId:%d", uri, func.getId());
				continue;
			}
			if(StringUtils.isBlank(config.getLoaderKey())){
				LogUtils.warn("loaderKey is blank for uri %s, funcId:%d", uri, func.getId());
				continue;
			}
			DataPrivLoader loader = key2loaderMap.get(config.getLoaderKey());
			if(loader == null){
				throw new BaseRuntimeException("NOT_EXIST", "数据权限校验器未成功加载，请联系管理员", "data priv loader '" + config.getLoaderKey() + "' not exists.");
			}
//			Map<String, String> requirdParamMap = new HashMap<>();
			
			PrivContext context = new PrivContext(workerId).putIfAbsentDepartId(departId);
			for(String paramName : (Set<String>)loader.requiredParams()){
				String aliasName = paramName;
				if(!CollectionUtils.isEmpty(config.getAliasParamMap())){
					String tempName = config.getAliasParamMap().get(paramName);
					if(StringUtils.isNotBlank(tempName)){
						aliasName = tempName;
					}
				}
				context.put(paramName, request.getParameter(aliasName));
//				requirdParamMap.put(paramName, request.getParameter(aliasName));
			}
			boolean result = loader.checkDataPriv(uri, context);
			if(!result){
				LogUtils.warn("no data privilege for workerId %d, requestUri:%s, paramMap:%s", workerId, uri, JsonUtil.create().toJson(context));
				return false;
			}
		}
		
		return true;
	}
	
	private List<Integer/*funcId*/> loadMyFuncIds(int workerId, int webappId, Set<Integer> roleIds){
		List<Integer> funcIds = userFuncService.loadMyFuncIds(workerId, webappId);
		
		if(!CollectionUtils.isEmpty(roleIds)){
			roleIds.add(RoleEnum.ANON.getCode()); //给每个用户添加匿名角色
			for(Integer roleId : roleIds){
				List<Integer> rfuncIds = roleFuncService.loadFuncIdsByRoleId(roleId, webappId);
				if(!CollectionUtils.isEmpty(rfuncIds)){
					funcIds.addAll(rfuncIds);
				}
			}
		}
		
		return funcIds;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		Map<String, DataPrivLoader> loaderMap = context.getBeansOfType(DataPrivLoader.class);
		if(!CollectionUtils.isEmpty(loaderMap)){
			for(DataPrivLoader loader : loaderMap.values()){
				key2loaderMap.put(loader.getKey(), loader);
			}
		}
	}

}
