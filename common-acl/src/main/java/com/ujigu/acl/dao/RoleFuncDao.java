package com.ujigu.acl.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ujigu.acl.entity.RoleFunc;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface RoleFuncDao extends IBaseDao<Integer, RoleFunc> {
    Integer insertReturnPK(RoleFunc roleFunc);
    
    void remove(@Param("roleId") int roleId, @Param("webappId") int webappId);
}