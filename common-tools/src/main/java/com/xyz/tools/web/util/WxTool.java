package com.xyz.tools.web.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.google.gson.Gson;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.BaseConfig;
import com.xyz.tools.common.utils.HttpClientUtil;

import cn.binarywang.wx.miniapp.api.WxMaMsgService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaTemplateMessage;
import cn.binarywang.wx.miniapp.config.WxMaInMemoryConfig;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpInRedisConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class WxTool {

	private final static Logger LOG = LoggerFactory.getLogger(WxTool.class);

	private static WxMpService wxMpService;

	private static WxPayService wxPayService;

	private static WxMaUserService wxMaUserService;

	private static WxMaMsgService wxMaMsgService;

	static {
		String appId = getAppId();
		String appSecret = getAppSecret();
		if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
			throw new BaseRuntimeException("CONFIG_ERR", "配置错误",
					"wx.appid or wx.secret not config in application.properties");
		}
		appId = appId.trim();
		appSecret = appSecret.trim();

		String redisHost = BaseConfig.getValue("redis.hostName");
		String redisPort = BaseConfig.getValue("redis.port");
		String redisPass = BaseConfig.getValue("redis.password");
		String soTimeout = BaseConfig.getValue("redis.timeout", "30000");

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxTotal(50);
		poolConfig.setMinIdle(5);
		poolConfig.setMaxIdle(20);
		poolConfig.setMaxWaitMillis(1000L);
		JedisPool pool = new JedisPool(poolConfig, redisHost, Integer.valueOf(redisPort), Integer.valueOf(soTimeout),
				redisPass);

		WxMpInRedisConfigStorage config = new WxMpInRedisConfigStorage(pool);
		config.setAppId(appId); // 设置微信公众号的appid
		config.setSecret(appSecret); // 设置微信公众号的app corpSecret

		config.setToken(BaseConfig.getValue("wx.chat.token")); // 设置微信公众号的token
		config.setAesKey(BaseConfig.getValue("wx.chat.aeskey"));// 设置微信公众号的EncodingAESKey

		wxMpService = new WxMpServiceImpl();
		wxMpService.setWxMpConfigStorage(config);

		WxPayConfig payConfig = new WxPayConfig();
		payConfig.setAppId(appId);
		payConfig.setMchId(BaseConfig.getValue("wx.pay.mchid").trim());
		payConfig.setMchKey(BaseConfig.getValue("wx.pay.key").trim());
		payConfig.setKeyPath(BaseConfig.getValue("wx.pay.cert"));

		wxPayService = new WxPayServiceImpl();
		wxPayService.setConfig(payConfig);

		WxMaInMemoryConfig wxMaConfig = new WxMaInMemoryConfig();
		wxMaConfig.setAppid(appId);
		wxMaConfig.setSecret(appSecret);
		WxMaService wxMaService = new WxMaServiceImpl();
		wxMaService.setWxMaConfig(wxMaConfig);

		wxMaUserService = wxMaService.getUserService();
		wxMaMsgService = wxMaService.getMsgService();
	}

	public static String getAppId() {

		return BaseConfig.getValue("wx.appid");
	}

	public static String getAppSecret() {

		return BaseConfig.getValue("wx.secret");
	}

	public static WxPayService getWxPayService() {

		return wxPayService;
	}

	public static WxMpService getWxMpService() {

		return wxMpService;
	}

	public static WxMaUserService getWxMaUserService() {

		return wxMaUserService;
	}

	/**
	 * 获取微信用户的信息
	 *
	 * @param openid
	 * @return
	 */
	public static String getWxUserInfo(String openid) {

		String retStr = "";
		String accessToken = getShopWxAccessToken();
		try {
			long time1 = System.currentTimeMillis();
			String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken + "&openid=" + openid
					+ "&lang=zh_CN";
			retStr = HttpClientUtil.getInstance().doGet(url);
			LOG.info("getWxUserInfo|SUCC|" + (System.currentTimeMillis() - time1) + "|" + retStr);
		} catch (Exception exp) {
			LOG.error("getWxUserInfo|ERROR|", exp);
		}
		return retStr;
	}

	/**
	 * 获取微信公众号令牌
	 *
	 * @returng
	 */
	public static String getShopWxAccessToken() {
		try {
			return WxTool.getWxMpService().getAccessToken();
		} catch (WxErrorException e) {
			throw new BaseRuntimeException("get wx access token error", "微信服务异常");
		}
	}

	/**
	 * 小程序推送消息消息
	 *
	 * @param toOpenId
	 *            接收者openId
	 * @param templateId
	 *            模板id
	 * @param page
	 *            跳转页
	 * @param dataMap
	 *            数据 <keyword,value>
	 * @return 是否发送成功
	 */
	public static boolean sendMaTmplMsg(String toOpenId, String templateId, String page, Map<String, String> dataMap) {

		WxMaTemplateMessage templateMessage = WxMaTemplateMessage.builder().toUser(toOpenId).templateId(templateId)
				.page(page).build();
		for (Map.Entry<String, String> data : dataMap.entrySet()) {
			templateMessage.getData().add(new WxMaTemplateMessage.Data(data.getKey(), data.getValue()));
		}
		try {
			wxMaMsgService.sendTemplateMsg(templateMessage);
			return true;
		} catch (Exception e) {
			LOG.error("sendMaTmplMsg|ERROR|" + toOpenId + "|" + templateId + "|" + page + "|"
					+ new Gson().toJson(dataMap), e);
		}
		return false;
	}

	/**
	 * 小程序推送消息消息
	 *
	 * @param toOpenId
	 *            接收者openId
	 * @param templateId
	 *            模板id
	 * @param page
	 *            跳转页
	 * @param dataList
	 *            数据 Data(keyword,value,color)
	 * @return 是否发送成功
	 */
	public static boolean sendMaTmplMsg(String toOpenId, String templateId, String page,
			List<WxMaTemplateMessage.Data> dataList) {

		WxMaTemplateMessage templateMessage = WxMaTemplateMessage.builder().toUser(toOpenId).templateId(templateId)
				.page(page).build();
		templateMessage.getData().addAll(dataList);
		try {
			wxMaMsgService.sendTemplateMsg(templateMessage);
			return true;
		} catch (Exception e) {
			LOG.error("sendMaTmplMsg|ERROR|" + toOpenId + "|" + templateId + "|" + page + "|"
					+ new Gson().toJson(dataList), e);
		}
		return false;
	}
}
