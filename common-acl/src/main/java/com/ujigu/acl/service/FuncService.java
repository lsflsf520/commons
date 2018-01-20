package com.ujigu.acl.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ujigu.acl.dao.FuncDao;
import com.ujigu.acl.entity.Func;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.db.bean.PageData;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

@Service
public class FuncService extends AbstractBaseService<Integer, Func> {
	
    @Resource
    private FuncDao funcDao;

    @Override
    protected IBaseDao<Integer, Func> getBaseDao() {
        return funcDao;
    }

    public Integer insertReturnPK(Func func) {
        funcDao.insertReturnPK(func);
        return func.getPK();
    }

    public Integer doSave(Func func) {
    	EhCacheTool.remove(DefaultCacheNS.SYS_FUNC, func.getWebappId());
    	func.setLastUptime(new Date());
        if (func.getPK() == null) {
        	if(!CollectionUtils.isEmpty(func.getUris())){
        		Func query = new Func();
        		query.setWebappId(func.getWebappId());
        		query.setUris(func.getUris());
        		query.setStatus(CommonStatus.NORMAL);
        		
        		Func dbData = this.findOne(query);
        		if(dbData != null){
        			throw new BaseRuntimeException("DATA_CONFLICT", "该菜单/功能的链接已存在", "webappId:" + func.getWebappId() + ",uri:" + func.getUri() + ",status:" + CommonStatus.NORMAL);
        		}
        	}
        	
        	func.setStatus(CommonStatus.NORMAL);
        	func.setCreateTime(func.getLastUptime());
            return this.insertReturnPK(func);
        }
        this.update(func);
        return func.getPK();
    }
    
    @Override
    public PageData<Func> findByPage(Func t) {
    	List<Func> datas = this.findByEntity(t);

        DataTree<Integer, Func> dataTree = new DataTree<>(datas);
    	
    	return new PageData<>(dataTree.getRoots(), 1, datas.size(), datas.size(), 1);
    }
    
    public List<Func> loadFuncList(int webappId){
    	List<Func> funcs = EhCacheTool.getValue(DefaultCacheNS.SYS_FUNC, webappId);
    	
    	if(CollectionUtils.isEmpty(funcs)){
    		Func query = new Func();
    		query.setStatus(CommonStatus.NORMAL);
    		query.setWebappId(webappId);
    		
    		funcs = this.findByEntity(query); //, "parent_id.asc,priority.asc"
    		EhCacheTool.put(DefaultCacheNS.SYS_FUNC, webappId, funcs);
    	}
    	
    	return funcs;
    }
    
    /**
     * 将当前webapp中能与uri匹配的Func全部取出，便于后续权限校验
     * @param uri
     * @return
     */
    public List<Func> loadFuncsByUri(String uri, int webappId){
    	List<Func> funcs = loadFuncList(webappId);
    	
    	List<Func> matchedFuncs = new ArrayList<>();
    	for(Func func : funcs){
    		if(func.getUris() != null && func.getUris().contains(uri)){
    			matchedFuncs.add(func); 
    		}
    	}
    	
    	return matchedFuncs;
    }
    
    /**
     * 
     * @return 加载所有可用的功能信息，包括菜单
     */
    public DataTree<Integer, Func> loadFuncTree(int webappId){
    	List<Func> funcs = loadFuncList(webappId);
    	
    	return new DataTree<Integer, Func>(funcs);
    }
    
    /**
     * 
     * @return
     */
    public DataTree<Integer, Func> loadMenuTree(int webappId){
    	List<Func> menus = new ArrayList<Func>();
    	List<Func> funcs = loadFuncList(webappId);
    	for(Func func : funcs){
    		if(func.canShow()){
    			menus.add(func);
    		}
    	}
    	
    	return new DataTree<Integer, Func>(menus);
    }
}