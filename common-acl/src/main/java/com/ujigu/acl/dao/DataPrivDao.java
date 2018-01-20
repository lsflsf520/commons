package com.ujigu.acl.dao;

import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.DataPriv;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface DataPrivDao extends IBaseDao<Integer, DataPriv> {
    Integer insertReturnPK(DataPriv dataPriv);
}