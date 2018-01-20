package com.ujigu.acl.dao;

import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.Func;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface FuncDao extends IBaseDao<Integer, Func> {
    Integer insertReturnPK(Func func);
}