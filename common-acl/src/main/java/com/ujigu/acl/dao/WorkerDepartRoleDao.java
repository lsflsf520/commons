package com.ujigu.acl.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.WorkerDepartRole;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface WorkerDepartRoleDao extends IBaseDao<Integer, WorkerDepartRole> {
    Integer insertReturnPK(WorkerDepartRole workerDepartRole);
    
    int removeMyDepartIds(@Param("workerId") int workerId, @Param("departIds") Integer... departIds);
}