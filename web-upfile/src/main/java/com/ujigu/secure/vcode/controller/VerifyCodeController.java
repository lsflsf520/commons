package com.ujigu.secure.vcode.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.RandomUtil;
import com.ujigu.secure.upfile.util.VerifyCodeTool;
import com.ujigu.secure.web.util.UserLoginUtil;
import com.ujigu.secure.web.util.WebUtils;

@Controller
@RequestMapping("/verify/code")
public class VerifyCodeController {
	
	private static final int IMG_WIDTH=90; //验证码图片的默认宽度
	
	private static final int IMG_HEIGHT=34; //验证码图片的默认高度
	
	private final static Map<String/*width+height*/, DefaultKaptcha> producerMap = new HashMap<>();
	
	@RequestMapping("gen")
	public void genCode(HttpServletRequest request, HttpServletResponse response, String key, Integer w, Integer h) {
		try {
			// set encoding
			request.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			// Verification code tool
			VerifyCodeTool vcTool = new VerifyCodeTool();
			BufferedImage image = vcTool.drawVerificationCodeImage(w == null || w <= 0 ? IMG_WIDTH : w, h == null || h <= 0 ? IMG_HEIGHT : h);
			// Verification code result
			int vcCode = vcTool.getXyresult();
			String vcValue = vcTool.getRandomString();
			// Set ban cache
			// Cache-control : Specify the request and response following
			// caching mechanism
			// no-cache: Indicates a request or response message cannot cache
			response.setHeader("Cache-Control", "no-cache");
			// Entity header field response expiration date and time are given
			response.setHeader("Pragma", "No-cache");
			response.setDateHeader("Expires", 0);
			// Set the output stream content format as image format
			response.setContentType("image/jpeg");
			// To the output stream output image
			
			key = StringUtils.isBlank(key) ? request.getSession().getId() : key;
			UserLoginUtil.saveImgCode(key, vcCode + "");
			
			ImageIO.write(image, "JPEG", response.getOutputStream());
			LogUtils.debug("获取验证码成功 :验证码:%s 验证码结果:%s key:%s", vcValue, vcCode, key);
		} catch (Exception e) {
			LogUtils.error("获取验证码失败", e);
		}
	}
	
	@RequestMapping("h/image")
	public void hardImage(HttpServletRequest request, HttpServletResponse response, String key, Integer w, Integer h){
		genImage(request, response, "h", key, w, h, "com.google.code.kaptcha.impl.WaterRipple", "com.google.code.kaptcha.impl.DefaultNoise");
	}
	
	@RequestMapping("m/image")
	public void middleImage(HttpServletRequest request, HttpServletResponse response, String key, Integer w, Integer h){
		genImage(request, response, "m", key, w, h, "com.google.code.kaptcha.impl.WaterRipple", null);
	}
	
	@RequestMapping("s/image")
	public void simpleImage(HttpServletRequest request, HttpServletResponse response, String key, Integer w, Integer h){
		genImage(request, response, "s", key, w, h, null, null);
	}
	
	@RequestMapping("r/image")
	public void randomImage(HttpServletRequest request, HttpServletResponse response, String key, Integer w, Integer h){
		try{
			int width = w == null || w <= 0 ? IMG_WIDTH : w;
			int height = h == null || h <= 0 ? IMG_HEIGHT : h;
			
			String[] whites = BaseConfig.getValueArr("vimage.width.height.whitelist");
			if(!Arrays.asList(whites).contains(width + "+" + height)){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "指定的宽("+width+")高("+height+")参数没有在白名单(vimage.width.height.whitelist)中配置");
			}
			
			request.setCharacterEncoding("utf-8");
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			response.setHeader("Pragma", "no-cache");
			response.setContentType("image/jpeg");
			
			key = StringUtils.isBlank(key) ? request.getSession().getId() : key;
			
			
			Producer producer = getRandomProducer(width, height);
			String capText = producer.createText();
			
	        BufferedImage image = producer.createImage(capText);
			
			UserLoginUtil.saveImgCode(key, "r" + capText);
			
			ImageIO.write(image, "JPEG", response.getOutputStream());
			LogUtils.debug("获取验证码成功 :验证码:%s key:%s", capText, key);
		} catch (Exception e) {
			LogUtils.error("获取验证码失败", e);
		}
	}
	
	private Producer getRandomProducer(int width, int height){
		String simpleProdKey = width + "+" + height + "+" + null + "+" + null;
		if(!producerMap.containsKey(simpleProdKey)){
			getProducer(width, height, null, null);
		}
		
		String middleProdKey = width + "+" + height + "+com.google.code.kaptcha.impl.WaterRipple+" + null;
		if(!producerMap.containsKey(middleProdKey)){
			getProducer(width, height, "com.google.code.kaptcha.impl.WaterRipple", null);
		}
		
		String highProdKey = width + "+" + height + "+com.google.code.kaptcha.impl.WaterRipple+com.google.code.kaptcha.impl.DefaultNoise";
		if(!producerMap.containsKey(highProdKey)){
			getProducer(width, height, "com.google.code.kaptcha.impl.WaterRipple", "com.google.code.kaptcha.impl.DefaultNoise");
		}
		
		int index = RandomUtil.rand(3);
		switch (index) {
		case 0:
			return producerMap.get(simpleProdKey);
		case 1:
			return producerMap.get(middleProdKey);
		
		default:
			return producerMap.get(highProdKey);
		}
		
	}
	
	private void genImage(HttpServletRequest request, HttpServletResponse response, String level, String key, Integer w, Integer h, String obscurificator, String noise){
		try{
			int width = w == null || w <= 0 ? IMG_WIDTH : w;
			int height = h == null || h <= 0 ? IMG_HEIGHT : h;
			
			String[] whites = BaseConfig.getValueArr("vimage.width.height.whitelist");
			if(!Arrays.asList(whites).contains(width + "+" + height)){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "指定的宽("+width+")高("+height+")参数没有在白名单(vimage.width.height.whitelist)中配置");
			}
			
			request.setCharacterEncoding("utf-8");
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			response.setHeader("Pragma", "no-cache");
			response.setContentType("image/jpeg");
			
			key = StringUtils.isBlank(key) ? request.getSession().getId() : key;
			
			Producer producer = getProducer(width, height, obscurificator, noise);
			String capText = producer.createText();
			
	        BufferedImage image = producer.createImage(capText);
			
			UserLoginUtil.saveImgCode(key, level + capText);
			
			ImageIO.write(image, "JPEG", response.getOutputStream());
			LogUtils.debug("获取验证码成功 :验证码:%s key:%s", capText, key);
		} catch (Exception e) {
			LogUtils.error("获取验证码失败", e);
		}
	}
	
	private synchronized Producer getProducer(int width, int height, String obscurificator, String noise){
		String prodKey = width + "+" + height + "+" + obscurificator + "+" + noise;
		DefaultKaptcha producer = producerMap.get(prodKey);
		if(producer != null){
			return producer;
		}
		Properties prop = new Properties();
		prop.setProperty("kaptcha.border", BaseConfig.getValue("kaptcha.border", "no"));
		prop.setProperty("kaptcha.textproducer.font.color", BaseConfig.getValue("kaptcha.textproducer.font.color", "black"));
		prop.setProperty("kaptcha.background.clear.from", BaseConfig.getValue("kaptcha.background.clear.from", "247,247,247"));
		prop.setProperty("kaptcha.background.clear.to", BaseConfig.getValue("kaptcha.background.clear.to", "247,247,247"));
		prop.setProperty("kaptcha.obscurificator.impl", StringUtils.isBlank(obscurificator) ? "com.google.code.kaptcha.impl.ShadowGimpy" : obscurificator);
		prop.setProperty("kaptcha.noise.impl", StringUtils.isBlank(noise) ? "com.google.code.kaptcha.impl.NoNoise" : noise);
		
		int fontsize = height - 8;
		int charlen = 4; //图片验证码字符个数
		int space = (width - fontsize * charlen) / (charlen + 2);
		if(space < 0 ){
			space = 5;
		}
		prop.setProperty("kaptcha.textproducer.font.size", fontsize + "");
		prop.setProperty("kaptcha.textproducer.char.length", "4");
		prop.setProperty("kaptcha.textproducer.char.space", space + "");

		prop.setProperty("kaptcha.image.width", width + "");
		prop.setProperty("kaptcha.image.height", height + "");
		
		Config config = new Config(prop);
		producer = new DefaultKaptcha();
		producer.setConfig(config);
		
		producerMap.put(prodKey, producer);
		
		return producer;
	}
	
	/**
	 * 此方法与上边的中文验证码图片一一对应
	 * @param request
	 * @param response
	 * @param key
	 * @param code
	 */
	@RequestMapping("check")
	public void verifyCode(HttpServletRequest request, HttpServletResponse response, String key, String code){
		key = StringUtils.isBlank(key) ? request.getSession().getId() : key;
		try{
			boolean result = UserLoginUtil.verifyImgCode(key, code);
			
			WebUtils.writeJson(new ResultModel(result), request, response);
		} catch (Exception e){
			LogUtils.error("occur exception when verify image code with key %s ",	e, key, code);
			WebUtils.writeJson(new ResultModel("SERV_ERR", "服务出现问题，请稍后重试！"), request, response);
		}
	}
	
	/**
	 * 此方法的验证与上边各难度等级对应
	 * @param request
	 * @param response
	 * @param level
	 * @param key
	 * @param code
	 */
	@RequestMapping("{level}/check")
	public void verifyCode(HttpServletRequest request, HttpServletResponse response, @PathVariable("level") String level, String key, String code){
		key = StringUtils.isBlank(key) ? request.getSession().getId() : key;
		try{
			level = StringUtils.isBlank(level) ? "" : level;
			boolean result = UserLoginUtil.verifyImgCode(key, level + code);
			
			WebUtils.writeJson(new ResultModel(result), request, response);
		} catch (Exception e){
			LogUtils.error("occur exception when verify image code with key %s ",	e, key, code);
			WebUtils.writeJson(new ResultModel("SERV_ERR", "服务出现问题，请稍后重试！"), request, response);
		}
	}

}
