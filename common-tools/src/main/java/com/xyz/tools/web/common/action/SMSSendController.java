package com.xyz.tools.web.common.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xyz.tools.common.bean.ResultModel;
import com.xyz.tools.common.utils.BaseConfig;
import com.xyz.tools.common.utils.LogUtils;
import com.xyz.tools.common.utils.RandomUtil;
import com.xyz.tools.common.utils.RegexUtil;
import com.xyz.tools.web.util.CommonServUtil;
import com.xyz.tools.web.util.UserLoginHelper;

/**
 * @comment
 * @author tujianjun
 * @time 2017年8月29日 上午9:35:22
 */

@Controller
@RequestMapping("/sms")
public class SMSSendController implements ApplicationContextAware {

	private UserLoginHelper userLoginHelper;

	@RequestMapping("/send")
	@ResponseBody
	public ResultModel send(HttpServletRequest request, String phone) {
		if (phone == null || !RegexUtil.isPhone(phone = phone.trim())) {
			return new ResultModel("ILLEGAL_PHONE", "手机号格式不正确");
		}
		String code = RandomUtil.randomNumCode(6);

		String appId = BaseConfig.getValue("sms.appid." + request.getServletPath());
		if (StringUtils.isBlank(appId)) {
			appId = BaseConfig.getValue("sms.appid");
		}
		String tmplId = BaseConfig.getValue("sms.tmplid." + request.getServletPath());
		if (StringUtils.isBlank(tmplId)) {
			tmplId = BaseConfig.getValue("sms.tmplid");
		}

		if (StringUtils.isBlank(appId) || StringUtils.isBlank(tmplId)) {
			return new ResultModel("CONFIG_ERR", "配置错误");
		}

		userLoginHelper.saveTmpSessionInfo(request, code, phone);

		return CommonServUtil.sendMsg(null, appId.trim(), phone, tmplId.trim(), code, "5");
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		try {
			userLoginHelper = context.getBean(UserLoginHelper.class);
		} catch (Exception e) {
			LogUtils.warn("not found bean for UserLoginHelper in ApplicationContext");
		}
	}

}
