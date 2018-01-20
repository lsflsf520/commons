package com.ujigu.acl.dao;

import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.Webapp;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface WebappDao extends IBaseDao<Integer, Webapp> {
    Integer insertReturnPK(Webapp webapp);
}