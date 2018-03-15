package com.xyz.tools.web.common.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xyz.tools.common.bean.ResultModel;
import com.xyz.tools.common.constant.GlobalResultCode;
import com.xyz.tools.web.util.WebUtils;

@Controller
@RequestMapping("/error")
public class ErrorController {

	@RequestMapping("/500")
	public String to500(HttpServletRequest request) {

		request.setAttribute("errorMsg", request.getAttribute("errorMsg"));
		request.setAttribute("messageId", request.getAttribute("messageId"));
		request.setAttribute("friendlyMsg", request.getAttribute("friendlyMsg"));
		request.setAttribute("srcUrl", request.getAttribute("srcUrl"));

		return "common/error500";
	}
	
	@RequestMapping("/500/json")
	@ResponseBody
	public ResultModel to500Json() {
		return new ResultModel("500", "服务器发生未知错误");
	}

	@RequestMapping("/404")
	public String to404() {

		return "common/error404";
	}
	
	@RequestMapping("/404/json")
	@ResponseBody
	public ResultModel to404Json() {
		return new ResultModel("404", "服务器没找到当前链接");
	}

	@RequestMapping("/403")
	public String to403() {

		return "common/error403";
	}
	
	@RequestMapping("/403/json")
	@ResponseBody
	public ResultModel to403Json() {
		return new ResultModel("403", "禁止访问");
	}

	@RequestMapping("/401")
	public String to401() {

		return "common/error401";
	}
	
	@RequestMapping("/401/json")
	@ResponseBody
	public ResultModel to401Json() {
		return new ResultModel("401", "未授权的访问");
	}
	
	@RequestMapping("/503")
	public String to503() {

		return "common/error503";
	}
	
	@RequestMapping("/503/json")
	@ResponseBody
	public ResultModel to503Json() {
		return new ResultModel("503", "服务暂时不可用，请稍后重试！");
	}

	@RequestMapping("/report")
	public void reportError(HttpServletRequest request, HttpServletResponse response, String msgId, String errorMsg) {
		WebUtils.writeJson(new ResultModel(GlobalResultCode.SUCCESS), request, response);
	}

}
