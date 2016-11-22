package com.yisi.stiku.dbtest.dao.impl;

import com.yisi.stiku.db.dao.BaseDao;
import com.yisi.stiku.db.dao.impl.BaseDaoImpl;
import com.yisi.stiku.dbtest.dao.TestUserDao;
import com.yisi.stiku.dbtest.entity.TestUser;
import javax.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class TestUserDaoImpl extends BaseDaoImpl<Integer, TestUser> {
    @Resource
    private TestUserDao testUserDao;

    @Override
    protected BaseDao<Integer, TestUser> getProxyBaseDao() {
        return testUserDao;
    }
}