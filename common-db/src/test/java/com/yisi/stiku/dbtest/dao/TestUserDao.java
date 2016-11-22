package com.yisi.stiku.dbtest.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.yisi.stiku.db.dao.BaseDao;
import com.yisi.stiku.dbtest.entity.TestUser;

@Repository
public interface TestUserDao extends BaseDao<Integer, TestUser> {

	public TestUser loadBySid(@Param("sid") int sid, @Param("forceMaster") boolean forceMaster);

	public void updateCompany(String company);

}