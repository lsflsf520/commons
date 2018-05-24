package com.xyz.tools.web.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.util.NestedServletException;

import com.xyz.tools.common.bean.ResultModel;
import com.xyz.tools.common.constant.ClientType;
import com.xyz.tools.common.constant.GlobalConstant;
import com.xyz.tools.common.constant.GlobalResultCode;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.BaseConfig;
import com.xyz.tools.common.utils.DateUtil;
import com.xyz.tools.common.utils.EncryptTools;
import com.xyz.tools.common.utils.IPUtil;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.RandomUtil;
import com.xyz.tools.common.utils.ThreadUtil;
import com.xyz.tools.web.util.UserLoginHelper;
import com.xyz.tools.web.util.WebUtils;

/**
 * 
 * @author shangfeng
 *
 */
public class CommonFilter implements Filter {

	private AntPathMatcher urlMatcher = new AntPathMatcher();

	private final Set<String> extendNames = new LinkedHashSet<String>();

	private final Set<String> noRedirect2MobileUris = new HashSet<>();

	private final Set<String> excludePatterns = new LinkedHashSet<>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		extendNames.add(".js");
		extendNames.add(".css");
		extendNames.add(".jpg");
		extendNames.add(".jpeg");
		extendNames.add(".png");
		extendNames.add(".swf");
		extendNames.add(".gif");
		extendNames.add(".ico");

		String excludeSuffixStr = filterConfig.getInitParameter("excludeSuffix");
		// excludeSuffix 的格式为以逗号分隔的文件后缀
		if (StringUtils.isNotBlank(excludeSuffixStr)) {
			String[] suffixes = excludeSuffixStr.split(",");
			for (String suffix : suffixes) {
				extendNames.add((suffix.startsWith(".") ? suffix : "." + suffix).trim());
			}
		}

		String noredirectUriStr = filterConfig.getInitParameter("noRedirect2MobileUris");
		if (StringUtils.isNotBlank(noredirectUriStr)) {
			String[] noredirectUris = noredirectUriStr.split(",");
			for (String uri : noredirectUris) {
				noRedirect2MobileUris.add(uri.trim());
			}
		}

		String excludePattern = filterConfig.getInitParameter("excludePattern");
		if (StringUtils.isNotBlank(excludePattern)) {
			String[] patterns = excludePattern.split(",");
			for (String pattern : patterns) {
				excludePatterns.add(pattern.trim());
			}
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String servletUri = httpRequest.getServletPath();
		if (WebUtils.isStaticRes(servletUri, extendNames) || isExcludeUrl(servletUri)) {
			// 如果为静态资源，则直接继续调用filter链进行处理即可
			chain.doFilter(request, response);
			return;
		}
		String currUrl = ThreadUtil.getCurrUrl(); // 这个当前的url有可能会被UrlRewrite重写，所以先记录一下
		// ThreadUtil.printContent();
		ThreadUtil.clear(); // 每次进入当前环境，先清空当前ThreadLocal中的数据
		if (StringUtils.isBlank(currUrl)) {
			currUrl = WebUtils.getCurrParamUrl(httpRequest);
		}
		ThreadUtil.setCurrUrl(currUrl);

		String token = httpRequest.getHeader(ThreadUtil.TOKEN_KEY);
		if (StringUtils.isBlank(token)) {
			token = UserLoginHelper.getToken(httpRequest);
			if ("true".equals(BaseConfig.getValue("api.web.flag"))) {
				ThreadUtil.setAppReqFlag();
			}
		} else {
			ThreadUtil.setAppReqFlag(); // 在此设置app请求标识
		}

		ClientType clientType = WebUtils.getClientType(httpRequest);
		ThreadUtil.setClientType(clientType);
		if(!ThreadUtil.isAppReq() && ClientType.isApp(clientType)) {
			ThreadUtil.setAppReqFlag(); // 在此设置app请求标识
		}
		String traceMsgId = httpRequest.getHeader(ThreadUtil.TRACE_MSG_ID);
		ThreadUtil.getTraceMsgId(traceMsgId);

		ThreadUtil.setCurrDomain(request.getServerName());
		int serverPort = request.getServerPort();
		ThreadUtil.setCurrPort(serverPort);
		String sid = getSid(httpRequest, (HttpServletResponse) response);
		ThreadUtil.setSid(sid);
		ThreadUtil.setToken(token);

		String srcProjectName = httpRequest.getHeader(ThreadUtil.SRC_PROJECT_KEY);
		ThreadUtil.setSrcProject(srcProjectName);
		ThreadUtil.setSrcIP(WebUtils.getClientIp(httpRequest));
		ThreadUtil.put(ThreadUtil.RESPONSE_KEY, (HttpServletResponse) response);
		ThreadUtil.put(ThreadUtil.REQUEST_KEY, httpRequest);
		ThreadUtil.setCurrUri(servletUri);
		ThreadUtil.setAppReq(WebUtils.isWxClient(httpRequest));
		ThreadUtil.setWxClient(WebUtils.isMobile(httpRequest));
		// ThreadUtil.putIfAbsent(ThreadUtil.IS_MOBILE_CLIENT,
		// WebUtils.isMobile(httpRequest));

		if (!ThreadUtil.isAppReq() && ThreadUtil.isMobileClient() && ThreadUtil.isPCDomain()
				&& "true".equals(BaseConfig.getValue("auto.pc.to.h5", "false"))) {
			boolean isNoRedirectUri = false; // 先检测一下是否是无需往m站跳转的uri
			for (String noRedirectUri : noRedirect2MobileUris) {
				if (urlMatcher.match(noRedirectUri, servletUri)) {
					isNoRedirectUri = true;
					break;
				}
			}

			if (!isNoRedirectUri) {
				String domainPrefix = ThreadUtil.getDomainPrefix();
				String h5url = buildH5Url(domainPrefix);
				LogUtils.info("redirect from %s to %s", ThreadUtil.getCurrUrl(), h5url);
				((HttpServletResponse) response).sendRedirect(h5url);

				return;
			}
		}

		long start = System.currentTimeMillis();
		String code = "200";
		try {
			httpRequest.setAttribute("BASE_URI", httpRequest.getContextPath());
			httpRequest.setAttribute("_SID_", sid);
			httpRequest.setAttribute("STATIC_DOMAIN", GlobalConstant.STATIC_DOMAIN);
			httpRequest.setAttribute("RES_VERSION", GlobalConstant.RES_VERSION);
			httpRequest.setAttribute("IS_WEB_ADMIN", GlobalConstant.IS_MGR);
			httpRequest.setAttribute("BASE_SERVICE_DOMAIN", GlobalConstant.BASE_SERVICE_DOMAIN);
			httpRequest.setAttribute("ACL_DOMAIN", GlobalConstant.ACL_DOMAIN);
			httpRequest.setAttribute("SERVLET_URI", servletUri);
			httpRequest.setAttribute("CURR_URL", currUrl);
			httpRequest.setAttribute("CURR_NO_PARAM_URL",
					StringUtils.isBlank(currUrl) ? null : currUrl.split("\\?")[0]);
			httpRequest.setAttribute("IS_WX_CLIENT", ThreadUtil.isWxClient());
			httpRequest.setAttribute("IS_MOBILE_CLIENT", ThreadUtil.isMobileClient());
			httpRequest.setAttribute("referer", httpRequest.getHeader("referer"));

			String userAgentStr = httpRequest.getHeader("user-agent");
			if (userAgentStr != null) {
				boolean isIos = userAgentStr.contains("iPhone");
				httpRequest.setAttribute("userAgent", isIos);
			}

			chain.doFilter(request, response);
		} catch (Throwable th) {
			code = "500";
			String resultCode = "SYS_ERR";
			String msg = "系统错误，请联系管理员!";
			Throwable realTh = th;
			if (th instanceof NestedServletException) {
				NestedServletException ne = (NestedServletException) th;
				realTh = ne.getCause();
			}
			if (realTh instanceof BaseRuntimeException) {
				msg = ((BaseRuntimeException) realTh).getFriendlyMsg();
				resultCode = ((BaseRuntimeException) realTh).getErrorCode();
			}

			String extraMsg = "time:" + DateUtil.getCurrentDateTimeStr() + ",userId:" + ThreadUtil.getUid()
					+ ",loginName:" + ThreadUtil.getShowName() + ",serverIP:" + IPUtil.getLocalIp() + ",params:"
					+ getParams(httpRequest);
			if ("NOT_LOGON".equals(resultCode)) {
				LogUtils.warn("not logon, context:%s", extraMsg);
			} else {
				LogUtils.error("uri: %s" + ",extraMsg: %s" + ",errMsg: %s", th, servletUri, extraMsg, th.getMessage());
			}

			HttpServletResponse httpResponse = (HttpServletResponse) response;
			if (WebUtils.isAjax(httpRequest) || ThreadUtil.isAppReq()) {
				WebUtils.writeJson(new ResultModel(resultCode, msg), httpRequest, httpResponse);
			} else {
				String errorUrl = BaseConfig.getValue("exception.redirect.url", "/error/500.do");
				if (GlobalResultCode.NO_PRIVILEGE.name().equals(resultCode)) {
					errorUrl = BaseConfig.getValue("nopriv.redirect.url", "/error/401.do");
				} else if ("NOT_LOGON".equals(resultCode)) {
					errorUrl = BaseConfig.getValue("project.loginpage.url", "/sys/port/loginPage.do");
				}
				if (th instanceof MaxUploadSizeExceededException
						|| th.getCause() instanceof MaxUploadSizeExceededException) {
					httpResponse.sendRedirect(errorUrl);
				} else {
					/*
					 * httpRequest.setAttribute("errorMsg", new BaseRuntimeException(resultCode,
					 * "uri:" + servletUri + "," + extraMsg, th));
					 * httpRequest.setAttribute("friendlyMsg", msg);
					 * httpRequest.setAttribute("messageId", ThreadUtil.getTraceMsgId());
					 * httpRequest.setAttribute("srcUrl", servletUri);
					 * httpRequest.getRequestDispatcher(errorUrl).forward(httpRequest,
					 * httpResponse);
					 */

					httpResponse.sendRedirect(errorUrl);
				}
			}

		} finally {
			LogUtils.logXN(WebUtils.getCurrParamUrl(httpRequest) + " " + code + " " + (WebUtils.isAjax(httpRequest) ? "ajax " : "http ")
				       + (StringUtils.isBlank(ThreadUtil.getSid()) ? "" : "sid(" + ThreadUtil.getSid() + ")")
	                   + (StringUtils.isBlank(ThreadUtil.getToken()) ? "" : "tk(" + ThreadUtil.getToken() + ")")
	                   + (ThreadUtil.getUid() == null ? "" : "uid(" + ThreadUtil.getUid() + ")" )
	                   + (ThreadUtil.getClientType() == null ? "" : "ctp(" + ThreadUtil.getClientType() + ")")
	                   + (StringUtils.isBlank(httpRequest.getHeader("referer")) ? "" : "ref(" + httpRequest.getHeader("referer") + ")"), start);
			ThreadUtil.clear();
		}

	}

	private boolean isExcludeUrl(String servletUri) {
		for (String pattern : excludePatterns) {
			if (urlMatcher.match(pattern, servletUri)) {
				return true;
			}
		}

		return false;
	}

	private String buildH5Url(String domainPrefix) {
		String currUrl = ThreadUtil.getCurrUrl();
		String currDomain = ThreadUtil.getCurrDomain();

		String h5domain = BaseConfig.getValue("h5.domain." + domainPrefix, BaseConfig.getValue("h5.domain"));
		if (StringUtils.isNotBlank(h5domain)) {
			return currUrl.replace(currDomain, h5domain);
		}

		// currDomain = currDomain.replace("wd.csai.cn", "csai.cn");
		if ("www".equals(domainPrefix) && currDomain.startsWith("www.")) {
			return currUrl.replace(currDomain, currDomain.replace("www.", "m."));
		}

		return currUrl.replace(currDomain, "m." + currDomain);
	}

	/**
	 * 返回当前会话的唯一ID
	 * 
	 * @param request
	 * @return
	 */
	private String getSid(HttpServletRequest request, HttpServletResponse response) {
		String sidName = "_sid_";
		String sid = WebUtils.getCookieValue(request, sidName);
		if (StringUtils.isBlank(sid)) {
			String clientIp = ThreadUtil.getSrcIP();
			ClientType clientType = ThreadUtil.getClientType();
			long currTime = System.currentTimeMillis();
			int randCode = RandomUtil.rand(100000);

			sid = EncryptTools.encryptByMD5(clientIp + clientType + currTime + randCode);

			WebUtils.setHttpOnlyCookie(response, sidName, sid, -1);
		}

		return sid;
	}

	/**
	 * 将request中的所有参数拼成字符串
	 * 
	 * @param request
	 * @return
	 */
	private String getParams(HttpServletRequest request) {

		StringBuilder builder = new StringBuilder();
		Enumeration<String> paramNames = request.getParameterNames();
		while (paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();

			String paramVal = null;
			String[] paramValues = request.getParameterValues(paramName);
			if (paramValues.length == 1) {
				String paramValue = paramValues[0];
				if (paramValue != null) {
					paramVal = paramValue;
				}
			} else if (paramValues.length > 1) {
				paramVal = paramValues[0];
				for (int index = 1; index < paramValues.length; index++) {
					paramVal += "," + paramValues[index];
				}
			}
			builder.append(";" + paramName + ":" + paramVal);
		}

		return builder.toString();
	}

	@Override
	public void destroy() {

		ThreadUtil.clear();
	}

}
