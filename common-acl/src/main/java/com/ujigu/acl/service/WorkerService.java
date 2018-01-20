package com.ujigu.acl.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.ujigu.acl.dao.WorkerDao;
import com.ujigu.acl.entity.Worker;
import com.ujigu.acl.vo.WorkerVO;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.EncryptTools;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.db.bean.PageData;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;

@Service
public class WorkerService extends AbstractBaseService<Integer, Worker> {
    @Resource
    private WorkerDao workerDao;
    
    @Resource
    private DepartService departService;

    @Override
    protected IBaseDao<Integer, Worker> getBaseDao() {
        return workerDao;
    }

    public Integer insertReturnPK(Worker worker) {
        workerDao.insertReturnPK(worker);
        return worker.getPK();
    }

    public Integer doSave(Worker worker) {
    	worker.setLastUptime(new Date());
        if (worker.getPK() == null) {
        	if(StringUtils.isNotBlank(worker.getPhone())){
        		Worker dbData = this.findByPhone(worker.getPhone());
        		if(dbData != null){
        			throw new BaseRuntimeException("DATA_EXIST", "手机号已存在", "phone:" + worker.getPhone());
        		}
        	}
        	
        	if(StringUtils.isNotBlank(worker.getEmail())){
        		Worker dbData = this.findByEmail(worker.getEmail());
        		if(dbData != null){
        			throw new BaseRuntimeException("DATA_EXIST", "邮箱已存在", "email:" + worker.getEmail());
        		}
        	}
        	
        	if(StringUtils.isNotBlank(worker.getPasswd())){
        		worker.setPasswd(encryptPasswd(worker.getPasswd().trim()));
        	}
        	
        	worker.setStatus(CommonStatus.NORMAL);
        	worker.setCreateTime(worker.getLastUptime());
            return this.insertReturnPK(worker);
        }
        
        Worker dbData = this.findById(worker.getPK());
        if(dbData == null){
        	throw new BaseRuntimeException("DATA_EXIST", "数据有误", "worker id:" + worker.getPK());
        }
        if(StringUtils.isNotBlank(worker.getEmail()) && !worker.getEmail().equalsIgnoreCase(dbData.getEmail())){
        	Worker dbEmailData = this.findByEmail(worker.getEmail());
    		if(dbEmailData != null && dbEmailData.getPK() != worker.getPK()){
    			throw new BaseRuntimeException("DATA_EXIST", "邮箱已被占用", "email:" + worker.getEmail());
    		}
        }
        
        if(StringUtils.isNotBlank(worker.getPhone()) && !worker.getPhone().equals(dbData.getPhone())){
        	Worker dbPhoneData = this.findByPhone(worker.getPhone());
    		if(dbPhoneData != null && dbPhoneData.getPK() != worker.getPK()){
    			throw new BaseRuntimeException("DATA_EXIST", "手机号已被占用", "phone:" + worker.getPhone());
    		}
        }
        //密码重置必须通过专门的接口处理
        if(StringUtils.isNotBlank(worker.getPasswd())){
        	worker.setPasswd(null);
        }
        
        this.update(worker);
        return worker.getPK();
    }
    
    public PageData<WorkerVO> findWorkerVOByPage(Worker t, Integer departId) {
    	if(departId == null || departId <= 0){
    		return new PageData<WorkerVO>(new ArrayList<WorkerVO>(), 1, 15, 0, 0);
    	}
    	List<Integer> departIds = departService.loadDepartIdsIncludeChild(departId);
    	
    	PageBounds bounds = parsePageBounds(t);
    	List<WorkerVO> datas = workerDao.findWorkerVOByPage(t, departIds, bounds);
    	 
    	return new PageData<WorkerVO>((PageList<WorkerVO>)datas);
    }
    
    /**
     * 修改密码
     * @param uid
     * @param passwd
     * @return
     */
    public boolean updateRawPasswd(int uid, String passwd){
    	Worker modifier = new Worker();
    	modifier.setId(uid);
    	modifier.setPasswd(encryptPasswd(passwd));
    	
    	return this.update(modifier);
    }
    
    /**
     * 根据登陆名称来查询用户信息
     * @param loginName
     * @return
     */
    public Worker findByLoginName(String loginName){
    	if(RegexUtil.isPhone(loginName)){
    		return this.findByPhone(loginName);
    	}else if(RegexUtil.isEmail(loginName)){
    		return this.findByEmail(loginName);
    	} else if("admin".equals(loginName)){
    		return this.findByName(loginName);
    	}
    	
    	throw new BaseRuntimeException("NOT_SUPPORT", "目前只支持手机号和邮箱号登陆", "loginName:" + loginName);
    }
    
    public Worker findByPhone(String phone){
    	Worker query = new Worker();
    	query.setPhone(phone);
    	
    	return this.findOne(query);
    }
    
    public Worker findByEmail(String email){
    	Worker query = new Worker();
    	query.setEmail(email);
    	
    	return this.findOne(query);
    }
    
    public Worker findByName(String name){
    	Worker query = new Worker();
    	query.setName(name);
    	
    	return this.findOne(query);
    }
    
    public String encryptPasswd(String passwd){
    	
    	return EncryptTools.encrypt(passwd + getSalt(passwd));
    }
    
    private String getSalt(String passwd){
    	if(StringUtils.isBlank(passwd)){
    		return "";
    	}
    	
    	if(passwd.length() < 3){
    		return passwd;
    	}
    	
    	return passwd.substring(0, 1) + passwd.substring(passwd.length() - 2);
    }
}