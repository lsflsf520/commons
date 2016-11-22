package com.yisi.stiku.dbtest.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yisi.stiku.common.bean.EntityState;
import com.yisi.stiku.common.bean.GlobalResultCode;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.db.dao.impl.BaseDaoImpl;
import com.yisi.stiku.db.service.impl.BaseServiceImpl;
import com.yisi.stiku.dbtest.dao.TestUserDao;
import com.yisi.stiku.dbtest.dao.impl.TestUserDaoImpl;
import com.yisi.stiku.dbtest.entity.TestUser;

@Service
public class TestUserServiceImpl extends BaseServiceImpl<Integer, TestUser> {

	@Resource
	private TestUserDaoImpl testUserDaoImpl;

	@Resource
	private TestUserDao testUserDao;

	@Override
	protected BaseDaoImpl<Integer, TestUser> getBaseDaoImpl() {

		return testUserDaoImpl;
	}

	@Transactional(value = "testTransactionManager")
	public void testTransaction() {

		TestUser user = new TestUser();
		user.setNick("哈哈110");
		user.setState(EntityState.NORMAL.getDbCode());
		user.setCompany("搜狐");

		int pk = testUserDaoImpl.insertReturnPK(user);

		user.setNick("after insert name");
		int effectRows = testUserDaoImpl.updateByPK(user);

		System.out.println("pk:" + pk + ", effectRows:" + effectRows);
		throw new BaseRuntimeException(GlobalResultCode.DB_OPER_ERROR);

	}

	public TestUser loadBySid(int sid, boolean forceMaster) {

		return testUserDao.loadBySid(sid, forceMaster);
	}

	public void updateCompany(String company) {

		testUserDao.updateCompany(company);
	}
}