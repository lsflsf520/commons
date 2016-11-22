package com.yisi.stiku.dbtest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import com.yisi.stiku.common.bean.EntityState;
import com.yisi.stiku.dbtest.entity.TestUser;
import com.yisi.stiku.dbtest.service.impl.TestUserServiceImpl;

@ContextConfiguration(locations = { "conf/spring/dal-spring.xml" })
public class DemoTest extends AbstractTestNGSpringContextTests {

	public static void main(String[] args) {

		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "conf/spring/dal-spring.xml" });

		TestUserServiceImpl userService = (TestUserServiceImpl) context.getBean("testUserService");

		TestUser user = new TestUser();
		user.setSid(110);
		user.setNick("哈哈110");
		user.setCompany("csjy");
		user.setState(EntityState.NORMAL.getDbCode());
		//
		// TestUser user2 = new TestUser();
		// user2.setSid(0);
		// user2.setNick("哈哈2");
		// user2.setCompany("csjy");
		// user2.setDbstate(EntityState.FREEZE);
		//
		// userService.insertBatch(Arrays.asList(user, user2));;

		// userService.insert(user);

		// int userId = 7;
		// // System.out.println("userId:" + userId);

		// for(int i=0; i< 100; i++){
		// TestUser entity = userService.findById(110);
		// System.out.println(entity);

		TestUser entity = userService.loadBySid(110, true);
		System.out.println(entity);

		// userService.updateCompany("创数教育");

		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }

		// user.setNick("haha");
		// userService.update(user);

		// List<TestUser> userList = userService.findAll();
		// System.out.println(userList);

		// Page<TestUser> pager = userService.findByPage(user, 1,20);
		// System.out.println(pager);

		// int result = userService.deleteById(9);
		// System.out.println("delete rows : " + result);

		// int sid = userService.insertReturnPK(user);
		//
		// System.out.println(sid);

		// List<TestUser> userList = userService.findByEntity(user,
		// " order by sid desc ");
		// System.out.println(userList);

		// userService.testTransaction();
	}

}
