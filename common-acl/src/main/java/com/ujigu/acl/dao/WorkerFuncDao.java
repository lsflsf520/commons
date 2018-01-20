package com.ujigu.acl.dao;

import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.WorkerFunc;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface WorkerFuncDao extends IBaseDao<Integer, WorkerFunc> {
    Integer insertReturnPK(WorkerFunc workerFunc);
}