package com.ujigu.acl.dao;

import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.Depart;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface DepartDao extends IBaseDao<Integer, Depart> {
    Integer insertReturnPK(Depart depart);
}