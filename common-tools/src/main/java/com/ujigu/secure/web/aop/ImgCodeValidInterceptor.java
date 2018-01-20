package com.ujigu.secure.web.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.web.filter.AbstractInterceptor;
import com.ujigu.secure.web.util.UserLoginUtil;

/**
 * 根据imgKey或者当前sessionId来验证imgCode是否正确
 * @author lsf
 *
 */
public class ImgCodeValidInterceptor extends AbstractInterceptor{

	@Override
	protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			String requestUri) throws Exception {
		String imgcode = request.getParameter("imgCode");
//		String imgKey = request.getParameter("imgKey");
		if(StringUtils.isBlank(imgcode)){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "图片验证码不能为空");
		}
		boolean result = UserLoginUtil.verifyImgCode(request, "r" + imgcode);
		if(!result){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "图片验证码不正确");
		}
		return result;
	}
	

}
