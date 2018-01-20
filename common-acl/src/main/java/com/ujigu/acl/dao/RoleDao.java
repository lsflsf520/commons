package com.ujigu.acl.dao;

import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.Role;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface RoleDao extends IBaseDao<Integer, Role> {
    Integer insertReturnPK(Role role);
}