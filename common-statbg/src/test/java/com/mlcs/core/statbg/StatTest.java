package com.mlcs.core.statbg;

import com.yisi.stiku.statbg.util.BootUtil;

/*
 @author shangfeng

 */
public class StatTest {

	/*
	 * @param args
	 */
	public static void main(String[] args) {

		BootUtil.execStat(new String[] { "-configFile",
				"context/spring-pagetime_log-stat.xml" });
		System.exit(0);

		// String sql =
		// "insert into test_user(sid, nick, state) values (165, null, 0),(166, null, 0),(168, null, 0)";
		// SqlUtil.exec(sql);

	}
}
