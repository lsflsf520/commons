package com.ujigu.acl.aop;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ujigu.acl.service.WorkerFuncPrivService;
import com.ujigu.acl.utils.ConfigUtil;
import com.ujigu.secure.common.bean.GlobalResultCode;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.web.filter.AbstractInterceptor;
import com.ujigu.secure.web.util.LogonUtil.SessionUser;

/**
 * 
 * @author shangfeng
 *
 */
public class ACLInterceptor extends AbstractInterceptor {
	
	@Resource
	private WorkerFuncPrivService workerFuncPrivService;

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {

        SessionUser suser = ThreadUtil.getUserInfo();
        int webappId = ConfigUtil.getWebappId();
        
       /* String queryStr = StringUtils.isNotBlank(request.getQueryString()) ? "?"
				+ request.getQueryString()
				: ""; *///追加url后的参数
		boolean result = workerFuncPrivService.isPermit(request, suser.getUidInt(), requestUri //+ queryStr
				, webappId);
		if(!result){
			throw new BaseRuntimeException(GlobalResultCode.NO_PRIVILEGE);
		}
		
		return true;
	}

}
