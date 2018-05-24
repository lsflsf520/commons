package com.ujigu.utils;


import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.google.gson.Gson;
import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.BaseConfig;
import com.xyz.tools.common.utils.DateUtil;
import com.xyz.tools.common.utils.HttpClientUtil;

import cn.binarywang.wx.miniapp.api.WxMaMsgService;
import cn.binarywang.wx.miniapp.api.WxMaQrcodeService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.WxMaUserService;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.api.WxMpInRedisConfigStorage;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;
import cn.binarywang.wx.miniapp.bean.WxMaTemplateMessage;
import cn.binarywang.wx.miniapp.config.WxMaInMemoryConfig;
import me.chanjar.weixin.common.exception.WxErrorException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class WxTool {

    private final static Logger LOG = LoggerFactory.getLogger(WxTool.class);

    private static WxMpService wxMpService;

    private static WxPayService wxPayService;

    private static WxMaUserService wxMaUserService;

    private static WxMaMsgService wxMaMsgService;

    private static WxMaQrcodeService wxMaQrcodeService;

    static {
        String appId = getAppId();
        String appSecret = getAppSecret();
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
            throw new BaseRuntimeException("CONFIG_ERR", "配置错误", "wx.appid or wx.secret not config in application.properties");
        }
        appId = appId.trim();
        appSecret = appSecret.trim();

        String redisHost = BaseConfig.getValue("redis.host");
        String redisPort = BaseConfig.getValue("redis.port");
        String redisPass = BaseConfig.getValue("redis.auth.password");
        String soTimeout = BaseConfig.getValue("redis.soTimeout", "30000");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(50);
        poolConfig.setMinIdle(5);
        poolConfig.setMaxIdle(20);
        poolConfig.setMaxWaitMillis(1000L);
        JedisPool pool = new JedisPool(poolConfig, redisHost, Integer.valueOf(redisPort), Integer.valueOf(soTimeout), redisPass);

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
        payConfig.setKeyPath(BaseConfig.getValue("wx.pay.cert.path", "/data/www/wx/cert/apiclient_cert.p12"));

        wxPayService = new WxPayServiceImpl();
        wxPayService.setConfig(payConfig);


        WxMaInMemoryConfig wxMaConfig = new WxMaInMemoryConfig();
        wxMaConfig.setAppid(appId);
        wxMaConfig.setSecret(appSecret);
        WxMaService wxMaService = new WxMaServiceImpl();
        wxMaService.setWxMaConfig(wxMaConfig);

        wxMaUserService = wxMaService.getUserService();
        wxMaMsgService = wxMaService.getMsgService();
        wxMaQrcodeService = wxMaService.getQrcodeService();
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
    public static  String getWxUserInfo(String openid) {

        String retStr = "";
        String accessToken = getShopWxAccessToken();
        try {
            long time1 = System.currentTimeMillis();
            String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + accessToken + "&openid=" + openid + "&lang=zh_CN";
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
     * @param toOpenId   接收者openId
     * @param templateId 模板id
     * @param formId     表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的 prepay_id
     * @param page       跳转页
     * @param dataMap    数据 <keyword,value>
     * @return 是否发送成功
     */
    public static boolean sendMaTmplMsg(String toOpenId, String templateId,String formId, String page, Map<String, String> dataMap) {

        try {
            WxMaTemplateMessage templateMessage = WxMaTemplateMessage.builder().toUser(toOpenId).templateId(templateId).formId(formId).page(page).build();
            for (Map.Entry<String, String> data : dataMap.entrySet()) {
                templateMessage.addData(new WxMaTemplateMessage.Data(data.getKey(), data.getValue()));
            }
            wxMaMsgService.sendTemplateMsg(templateMessage);
            return true;
        } catch (Exception e) {
            LOG.error("sendMaTmplMsg|ERROR|" + toOpenId + "|" + templateId + "|" + formId + "|" + page + "|" + new Gson().toJson(dataMap), e);
        }
        return false;
    }

    /**
     * 生成小程序二唯码
     *
     * @param wxUid
     * @param scene
     * @param page
     * @return 图片地址
     */
    public static String getWxCodeLimit(int wxUid, String scene, String page) {

        if (StringUtils.isNotBlank(scene) && scene.length() > 32) {
            throw new BaseRuntimeException("DATA_TOO_LONG", "二维码场景值长度不能超过32位", "scene: " + scene);
        }
        String localDir = BaseConfig.getValue("local.file.dir", "/data/www/static/files");
        String filePath = "/wxqrcode/" + DateUtil.formatDate(new Date(), "MMdd/HH/") + wxUid + ".jpg";
        String fileUrl = "";
        try {
            File file = wxMaQrcodeService.createWxCodeLimit(scene, page, 123, true, new WxMaCodeLineColor("0","0","0"));
            FileInputStream fileInputStream = new FileInputStream(file);

            File localFile = new File(localDir + filePath);
            FileUtils.copyInputStreamToFile(fileInputStream, localFile);
            fileInputStream.close();

            fileUrl = WebUtils.wellformUrl(filePath);
            LOG.info("getWxCodeLimit|SUCC|" + wxUid + "|" + scene + "|" + page + "|" + fileUrl);
        } catch (Exception e) {
            LOG.error("getWxCodeLimit|ERROR|" + scene + "|" + page, e);
        }
        return fileUrl;
    }
}
