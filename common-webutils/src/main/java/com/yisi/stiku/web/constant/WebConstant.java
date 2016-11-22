package com.yisi.stiku.web.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.common.utils.UserInfoUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.web.util.AclCodeUtil;
import com.yisi.stiku.web.util.LoginSesionUtil;

public class WebConstant {

	private final static String INDEX_URL = "http://www.17daxue.com/web-student";

	private final static String MODIFY_PRIV_INFO_URL = "/priv/info/edit";

	private final static String DEFAULT_LOGIN_PAGE_URL = "/user/login/loginPage";

	public final static String COMMON_ZK_NODE = "common/common.properties";

	public final static String PROBLEM_SERVER_PATH_KEY = "problem.server.path";

	// 17大学题目服务器的路径
	public final static String VIRTUAL_PATH_17_KEY = "17.problem.server.path";

	public final static String OOS_PATH_KEY = "17daxue.oos.url";

	// 每个考点出几个题目
	public final static String PER_POINT_PROBLEM_COUNT_KEY = "per.point.problem.num";

	public final static String MAGAZINE_SCORE_KEY = "magazine.score";

	public final static String PING_PANG_URI = "/ping/pang";

	public final static String STUDNET_PROJECT_NAME = "web-student";
	public final static String MS_PROJECT_NAME = "web-ms";
	public final static String TEACHER_PROJECT_NAME = "web-teacher";

	// 每个考点出几个题目
	// public static final int PER_POINT_PROBLEM_COUNT =
	// Integer.valueOf(ConfigOnZk.getValue(COMMON_ZK_NODE,
	// PER_POINT_PROBLEM_COUNT_KEY, "3"));
	//
	// // 题目服务器的路径
	// public static final String VIRTUAL_PATH =
	// ConfigOnZk.getValue(COMMON_ZK_NODE, PROBLEM_SERVER_PATH_KEY,
	// "http://7punek.com1.z0.glb.clouddn.com/");
	//
	// // 17大学题目服务器的路径
	// public static final String VIRTUAL_PATH_17 =
	// ConfigOnZk.getValue(COMMON_ZK_NODE, VIRTUAL_PATH_17_KEY);
	//
	// // 生成导学案参数
	// public static final String MAGAZINE_SCORE =
	// ConfigOnZk.getValue(COMMON_ZK_NODE, MAGAZINE_SCORE_KEY);
	//
	// public static final String OOS_PATH = ConfigOnZk.getValue(COMMON_ZK_NODE,
	// OOS_PATH_KEY,
	// "http://17daxue-magazine.oss-cn-hangzhou.aliyuncs.com/");

	// 登录页面
	// public final static String LOGIN_PAGE_URL =
	// ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "login.page.url",
	// ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE +
	// "/web-passport/application.properties", "login.page.url",
	// DEFAULT_LOGIN_PAGE_URL));
	//
	// //登录之需要跳转的默认页面
	// public final static String LOGON_URL =
	// ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "logon.url",
	// ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE +
	// "/web-passport/application.properties", "logon.url", INDEX_URL));
	//
	// //第一次登录之后跳转的页面
	// public final static String FIRST_LOGON_URL =
	// ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "first.logon.url",
	// ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE +
	// "/web-passport/application.properties", "first.logon.url",
	// MODIFY_PRIV_INFO_URL));

	/**
	 * 
	 * @return 返回当前环境中的passport的域名
	 */
	public final static String getPassportDomain() {

		return ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "passport.web.domain", ConfigOnZk.getValue(
				ZkConstant.ZK_ROOT_NODE + "/web-passport/application.properties", "passport.web.domain",
				"http://passport.17daxue.com"));
	}

	/**
	 * 
	 * @return 返回共用静态资源的域名
	 */
	public final static String getCommonResDomain() {

		return ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "common.res.domain", ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE
				+ "/web-passport/application.properties", "common.res.domain", "http://res.17daxue.com"));
	}

	public final static String getBaseDataDomain() {

		return ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "base.data.domain", ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE
				+ "/web-passport/application.properties", "base.data.domain", "http://ms.17daxue.com/web-ms"));
	}

	/**
	 * 
	 * @return 返回工程静态资源的域名，到
	 */
	public final static String getProjectResDomain() {

		return ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "project.res.domain", getCommonResDomain());
	}

	public final static String getStudentDomain(List<Long> schoolIds) {

		return getWebAppDomain(STUDNET_PROJECT_NAME, schoolIds);
	}

	/**
	 * 
	 * @return 返回管理后台的域名
	 */
	public final static String getMsDomain(List<Long> schoolIds) {

		return getWebAppDomain(MS_PROJECT_NAME, schoolIds);
	}

	public final static String getWebAppDomain(String userTypeProjectName, List<Long> schoolIds) {

		String key = getUserTypeProjectName(userTypeProjectName) + ".web.domain";
		String schoolDomain = getSchoolAppDomain(key, schoolIds);
		if (StringUtils.isNotBlank(schoolDomain)) {
			return schoolDomain;
		}
		return ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, key,
				ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE + "/web-passport/application.properties", key, INDEX_URL));
	}

	private final static String getUserTypeProjectName(String userTypeProjectName) {

		return StringUtils.isNotBlank(userTypeProjectName) ? userTypeProjectName : MS_PROJECT_NAME;
	}

	private final static String getSchoolAppDomain(String key, List<Long> schoolIds) {

		if (schoolIds != null && !schoolIds.isEmpty()) {
			for (Long schoolId : schoolIds) {
				String domain = ConfigOnZk.getValue(
						ZkConstant.APP_ZK_PATH,
						key + "." + schoolId,
						ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE + "/web-passport/application.properties", key + "."
								+ schoolId));
				if (StringUtils.isNotBlank(domain)) {
					return domain;
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * @return 登录页面
	 */
	public final static String getLoginPageUrl(String contextPath) {

		String passportDomain = getPassportDomain();
		String key = StringUtils.isBlank(contextPath) || !contextPath.startsWith("/web-")
				|| "/web-passport".equals(contextPath) ? "login.page.url" : contextPath.replace("/web-", "")
				+ ".login.page.url";
		return passportDomain
				+ ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, key, ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE
						+ "/web-passport/application.properties", key, DEFAULT_LOGIN_PAGE_URL));
	}

	/**
	 * @return 登录之需要跳转的默认页面
	 */
	public final static String getLogonUrl(String userTypeProjectName, List<Long> schoolIds) {

		String stdDomain = getWebAppDomain(userTypeProjectName, schoolIds);
		String key = getUserTypeProjectName(userTypeProjectName) + ".logon.url";
		return stdDomain
				+ ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, key,
						ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE + "/web-passport/application.properties", key, ""));
	}

	/**
	 * 
	 * @return 第一次登录之后跳转的页面
	 */
	public final static String getFirstLogonUrl(String userTypeProjectName) {

		String passportDomain = getPassportDomain();
		String key = getUserTypeProjectName(userTypeProjectName) + ".first.logon.url";
		return passportDomain
				+ ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, key, ConfigOnZk.getValue(ZkConstant.ZK_ROOT_NODE
						+ "/web-passport/application.properties", key, MODIFY_PRIV_INFO_URL));
	}

	public final static List<Long> getSchoolIdsForCurrUser(int userType) {

		if (UserInfoUtil.isStudent(userType)) {
			Long schoolId = LoginSesionUtil.getSchoolId();
			if (schoolId != null) {
				return Arrays.asList(schoolId);
			} else {
				return new ArrayList<Long>();
			}
		}

		return AclCodeUtil.getSchoolIdListForCurrUser();
	}

	public final static void setCommonParam2Request(HttpServletRequest request, List<Long> schoolIds) {

		request.setAttribute("studentIndex", WebConstant.getLogonUrl(STUDNET_PROJECT_NAME, schoolIds));
		request.setAttribute("studentDomain", WebConstant.getStudentDomain(schoolIds));
		request.setAttribute("teacherDomain", WebConstant.getWebAppDomain(TEACHER_PROJECT_NAME, schoolIds));
		request.setAttribute("passportDomain", WebConstant.getPassportDomain());
		request.setAttribute("msDomain", WebConstant.getMsDomain(schoolIds));
		request.setAttribute("sigmaDomain", WebConstant.getWebAppDomain("web-sigma", schoolIds));
		request.setAttribute("jyDomain", WebConstant.getWebAppDomain("web-jy", schoolIds));
		request.setAttribute("statDomain", WebConstant.getWebAppDomain("web-stat", schoolIds));
		request.setAttribute("loginUrl", WebConstant.getLoginPageUrl(request.getContextPath()));
		request.setAttribute("commonResDomain", WebConstant.getCommonResDomain());
		request.setAttribute("projectResDomain", WebConstant.getProjectResDomain());
		request.setAttribute("baseDataDomain", WebConstant.getBaseDataDomain());
		request.setAttribute("base", request.getContextPath());
		request.setAttribute("buildVersion", ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "static.build.version", "1.0.0"));
	}

}
