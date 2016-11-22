package com.yisi.stiku.web.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.NestedServletException;

import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.DateUtil;
import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.common.utils.ThreadUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;
import com.yisi.stiku.log.LogUtil;
import com.yisi.stiku.web.constant.WebConstant;
import com.yisi.stiku.web.util.LoginSesionUtil;
import com.yisi.stiku.web.util.OperationResult;
import com.yisi.stiku.web.util.WebUtils;

/**
 * 
 * @author shangfeng
 *
 */
public class CommonFilter implements Filter {

	private final static Logger LOG = LoggerFactory.getLogger(CommonFilter.class);

	private final Set<String> extendNames = new HashSet<String>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		extendNames.add(".js");
		extendNames.add(".css");
		extendNames.add(".jpg");
		extendNames.add(".png");
		extendNames.add(".gif");
		extendNames.add(".ico");

		String excludeSuffixStr = filterConfig.getInitParameter("excludeSuffix");
		// excludeSuffix 的格式为以逗号分隔的文件后缀
		if (StringUtils.isNotBlank(excludeSuffixStr)) {
			String[] suffixes = excludeSuffixStr.split(",");
			for (String suffix : suffixes) {
				extendNames.add(suffix.startsWith(".") ? suffix : "." + suffix);
			}
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String servletUri = httpRequest.getServletPath();
		if (isStaticRes(servletUri)) {
			// 如果为静态资源，则直接继续调用filter链进行处理即可
			chain.doFilter(request, response);
			return;
		}
		String token = httpRequest.getParameter("_token_");
		if (StringUtils.isBlank(token)) {
			token = LoginSesionUtil.getToken(httpRequest);
		} else {
			ThreadUtil.setAppReqFlag(); // 在此设置app请求标识
		}

		// if (!WebUtils.PC.equals(WebUtils.getEquipType(httpRequest))) {
		// ThreadUtil.setAppReqFlag(); // 在此设置app请求标识
		// }
		ThreadUtil.setToken(token);
		ThreadUtil.genTraceMsgId();
		ThreadUtil.setSrcIP(WebUtils.getClientIp(httpRequest));
		ThreadUtil.setSrcProject(ZkConstant.PROJECT_NAME);

		WebConstant.setCommonParam2Request(httpRequest, null);

		long start = System.currentTimeMillis();
		try {
			chain.doFilter(request, response);
		} catch (Throwable th) {
			String msg = "系统错误，请联系管理员!";
			Throwable realTh = th;
			if (th instanceof NestedServletException) {
				NestedServletException ne = (NestedServletException) th;
				realTh = ne.getCause();
			}
			if (realTh instanceof BaseRuntimeException) {
				msg = ((BaseRuntimeException) realTh).getFriendlyMsg();
			}

			String extraMsg = "time:" + DateUtil.getCurrentDateTimeStr() + ",userId:" + ThreadUtil.getUserId()
					+ ",loginName:" + ThreadUtil.get(ThreadUtil.SIGN_NAME)
					+ ",serverIP:" + IPUtil.getLocalIp() + ",params:" + getParams(httpRequest);
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			if (WebUtils.isAjax(httpRequest)) {
				WebUtils.writeJson(OperationResult.buildFailureResult(msg), httpRequest,
						httpResponse);
			} else {
				String errorUrl = ConfigOnZk.getValue(ZkConstant.APP_ZK_PATH, "exception.redirect.url", "/error/500");
				httpRequest.setAttribute("errorMsg", new BaseRuntimeException("SYS_ERROR", "uri:" + servletUri + ","
						+ extraMsg, th));
				httpRequest.setAttribute("messageId", ThreadUtil.getTraceMsgId());
				httpRequest.getRequestDispatcher(errorUrl).forward(httpRequest, httpResponse);
				// httpResponse.sendRedirect( + "?message=" + msg);
			}

			if (!(realTh instanceof BaseRuntimeException) || realTh.getCause() != null) {
				LOG.error("uri:" + servletUri + "," + extraMsg + "," + th.getMessage(), th);
			} else {
				LOG.warn("uri:" + servletUri + "," + extraMsg + "," + th.getMessage());
			}
		} finally {
			LogUtil.xnLog(LogUtil.HTTP_TYPE, servletUri, System.currentTimeMillis() - start);
			ThreadUtil.clear();
		}

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

	private boolean isStaticRes(String uri) {

		for (String suffix : extendNames) {
			if (uri.endsWith(suffix)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void destroy() {

		ThreadUtil.clear();
	}

}
