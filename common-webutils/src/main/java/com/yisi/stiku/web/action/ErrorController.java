package com.yisi.stiku.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yisi.stiku.log.LogUtil;
import com.yisi.stiku.web.util.OperationResult;
import com.yisi.stiku.web.util.WebUtils;

@Controller
@RequestMapping("/error")
public class ErrorController {

	@RequestMapping("/500")
	public String to500(HttpServletRequest request) {

		request.setAttribute("errorMsg", request.getAttribute("errorMsg"));
		request.setAttribute("messageId", request.getAttribute("messageId"));

		return "common/error500";
	}

	@RequestMapping("/404")
	public String to404() {

		return "common/error404";
	}

	@RequestMapping("/403")
	public String to403() {

		return "common/error403";
	}

	@RequestMapping("/503")
	public String to503() {

		return "common/error503";
	}

	@RequestMapping("/report")
	public void reportError(HttpServletRequest request, HttpServletResponse response, String msgId, String errorMsg) {

		LogUtil.reportError(msgId, errorMsg);
		WebUtils.writeJson(OperationResult.buildSuccessResult("感谢您的报告，我们会尽快处理！"), request, response);
	}

}
