package com.yisi.stiku.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yisi.stiku.web.util.WebUtils;

@Controller
@RequestMapping("/ping")
public class PingPangController {

	@RequestMapping("/pang")
	public void ping(HttpServletRequest request, HttpServletResponse response){
		WebUtils.writeJson("PONG", request, response);
	}
	
}
