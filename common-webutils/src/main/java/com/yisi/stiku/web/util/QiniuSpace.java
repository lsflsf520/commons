package com.yisi.stiku.web.util;

import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.rs.Entry;
import com.qiniu.api.rs.PutPolicy;
import com.qiniu.api.rs.RSClient;

public class QiniuSpace {
	public static final String IMAG_SPACE = "daxue17-image";
	public static final String IMAG_SPACE_TEST = "problempreptest";
	public static final String IMAG_URL_START = "http://" + IMAG_SPACE
			+ ".qiniudn.com/";
	private static Mac mac = null;
	private static Mac mac_test = null;
	static {
		// 生产环境key值
		Config.ACCESS_KEY = "mR4tyKfVZcwq-VtJ7N7vovAN11QEDsOL6rOPmKTx";
		Config.SECRET_KEY = "Ptlc3iRZNvYwpVxdWuliWI5N8vixy9DOLyuNGnDC";
		//测试环境key值(秦武)
//		Config.ACCESS_KEY = "87FcSPDnJY8tEO-AKK8Ps0cQH0yumN8jpVIcPN1A";
//		Config.SECRET_KEY = "eYzz637qbNK5UR7ytWXUIRzKDEn7XqZPDF0apLAQ";
		// 测试环境key值
		//Config.ACCESS_KEY = "NPQ_6vzDRT3iaw7eVYmp5tpeCU1qm0fzNVVL2WV3";
		//Config.SECRET_KEY = "JAE14lV-bMl33nI1pDuFlul6d0NTRvFvPNXxGW6r";
		mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
		mac_test = new Mac("87FcSPDnJY8tEO-AKK8Ps0cQH0yumN8jpVIcPN1A", "eYzz637qbNK5UR7ytWXUIRzKDEn7XqZPDF0apLAQ");
	}

	public static boolean isImagOk(String key){
		RSClient client = new RSClient(mac);
		Entry statRet = client.stat(IMAG_SPACE, key);
		if (statRet.ok()) {
			return true;
		}
		return false;
	}
	
	public static boolean isImagOkInTest(String key){
		RSClient client = new RSClient(mac_test);
		Entry statRet = client.stat(IMAG_SPACE_TEST, key);
		if (statRet.ok()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 取token值
	 * 
	 * @author 梁顺
	 * @param key
	 * @date 2014-9-26 下午4:57:12
	 * @param tbProblem
	 * @return
	 */
	public static String token(String key) {

		// 请确保该bucket已经存在
		// String bucketName = "testworkspace";
		// String bucketName = "questionbank";
		String scope = IMAG_SPACE + ":" + key;
		PutPolicy putPolicy = new PutPolicy(scope);
		String uptoken = null;
		try {
			uptoken = putPolicy.token(mac);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return uptoken;
	}

}
