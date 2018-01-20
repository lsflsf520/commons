package com.ujigu.secure.email.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.email.dto.Email;
import com.ujigu.secure.email.service.EmailHttpService;
import com.ujigu.secure.email.service.EmailSmtpService;

@Controller
@RequestMapping("email")
public class EmailController {
	
	@Resource
	private EmailSmtpService emailSmtpService;
	
	@Resource
	private EmailHttpService emailHttpService;
	
	@RequestMapping(value = "content/send", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel sendContentEmail(Email email)  throws MessagingException, UnsupportedEncodingException{
		email.setSrcIP(ThreadUtil.getSrcIP());
		return emailSmtpService.send(email);
	}

	@RequestMapping(value = "tmpl/send", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel sendTemplateEmail(Email email)  throws Throwable{
		email.setSrcIP(ThreadUtil.getSrcIP());
		return emailHttpService.send(email);
	}
	
	@RequestMapping(value = "asyncsend", method = RequestMethod.POST)
	@ResponseBody
	public ResultModel asyncsendEmail(Email email){
		email.setSrcIP(ThreadUtil.getSrcIP());
		return emailSmtpService.asyncSend(email);
	}
}
