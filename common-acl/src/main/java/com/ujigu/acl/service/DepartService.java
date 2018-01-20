package com.ujigu.acl.service;

import com.ujigu.acl.dao.DepartDao;
import com.ujigu.acl.entity.Depart;
import com.ujigu.secure.cache.constant.DefaultCacheNS;
import com.ujigu.secure.cache.eh.EhCacheTool;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.db.bean.PageData;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DepartService extends AbstractBaseService<Integer, Depart> {
    @Resource
    private DepartDao departDao;

    @Override
    protected IBaseDao<Integer, Depart> getBaseDao() {
        return departDao;
    }

    public Integer insertReturnPK(Depart depart) {
        departDao.insertReturnPK(depart);
        return depart.getPK();
    }

    public Integer doSave(Depart depart) {
    	EhCacheTool.remove(DefaultCacheNS.SYS_DEPART);
    	depart.setLastUptime(new Date());
        if (depart.getPK() == null) {
        	Depart query = new Depart();
        	query.setParentId(depart.getParentId());
        	query.setName(depart.getName());
        	
        	Depart dbData = this.findOne(query);
        	if(dbData != null){
        		throw new BaseRuntimeException("DATA_CONFLICT", "该公司/部门已存在", "departName:" + depart.getName() + ",parentId:" + depart.getParentId());
        	}
        	
        	depart.setStatus(CommonStatus.NORMAL);
        	depart.setCreateTime(depart.getLastUptime());
        	
            return this.insertReturnPK(depart);
        }
        this.update(depart);
        return depart.getPK();
    }
    
    @Override
    public PageData<Depart> findByPage(Depart t) {
    	List<Depart> datas = this.findByEntity(t);
    	
    	DataTree<Integer, Depart> dataTree = new DataTree<>(datas);
    	
    	return new PageData<>(dataTree.getRoots(), 1, datas.size(), datas.size(), 1);
    }
    
    public List<Depart> loadDeparts(){
    	List<Depart> departs = EhCacheTool.getValue(DefaultCacheNS.SYS_DEPART);
    	if(CollectionUtils.isEmpty(departs)){
    		Depart query = new Depart();
    		query.setStatus(CommonStatus.NORMAL);
    		
    		departs = this.findByEntity(query);
    		
    		EhCacheTool.put(DefaultCacheNS.SYS_DEPART, departs);
    	}
    	
    	return departs;
    }
    
    public DataTree<Integer, Depart> loadDepartTree(){
    	List<Depart> departs = this.loadDeparts();
    	
    	return new DataTree<>(departs);
    }
    
    public List<Integer> loadDepartIdsIncludeChild(int departId){
    	DataTree<Integer, Depart> departs = loadDepartTree();
    	
    	return departs.getNodeIdIncludeChild(Arrays.asList(departId));
    }
}