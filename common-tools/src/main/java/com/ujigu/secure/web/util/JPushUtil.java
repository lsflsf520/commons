package com.ujigu.secure.web.util;

import java.util.ArrayList;
import java.util.List;

import com.ujigu.secure.common.utils.LogUtils;

import cn.jiguang.common.ClientConfig;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;

/**
** @author Administrator
** @version 2017年11月9日上午11:00:51
** @Description
*/
public class JPushUtil {
	
	private static String masterSecret = "70362f2bcff2aaff4b108f09";
    private static String appKey = "a1613b0d2efc971135b933ae";
    private static final String ALERT = "推送信息"; 
	
	 /**
     * 生成极光推送对象PushPayload（采用java SDK）
     * @param alias
     * @param alert
     * @return PushPayload
     */
    public static PushPayload buildPushObject_android_ios_alias_alert(List<String> alias,String alert,String msgContent,String title){
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.alias(alias))
                .setNotification(Notification.newBuilder()
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .addExtra("type", "infomation")
                                .setAlert(alert)
                                .build())
                        .addPlatformNotification(IosNotification.newBuilder()
                                .addExtra("type", "infomation")
                                .setAlert(alert)
                                .build())
                        .build())
                .setMessage(Message.newBuilder()
                		.setMsgContent(msgContent)
                		.setTitle(title)
                		.build())
                .setOptions(Options.newBuilder()
                        .setApnsProduction(false)//true-推送生产环境 false-推送开发环境（测试使用参数）
                        //.setTimeToLive(90)//消息在JPush服务器的失效时间（测试使用参数）
                        .build())
                .build();
    }
    /**
     * 极光推送方法(采用java SDK)
     * @param alias
     * @param alert
     * @return PushResult
     */
    public static PushResult push(List<String> alias,String alert,String msgContent,String title){
        ClientConfig clientConfig = ClientConfig.getInstance();
        clientConfig.setPushHostName("https://api.jpush.cn");
        JPushClient jpushClient = new JPushClient(masterSecret, appKey, null, clientConfig);
        PushPayload payload = buildPushObject_android_ios_alias_alert(alias,alert,msgContent,title);
        try {
        	LogUtils.info("push message , title : %s", title);
            return jpushClient.sendPush(payload);
        } catch (APIConnectionException e) {
            LogUtils.error("Connection error. Should retry later. ", e);
            return null;
        } catch (APIRequestException e) {
        	LogUtils.error("Error response from JPush server. Should review and fix it. ", e);
        	LogUtils.info("HTTP Status: " + e.getStatus());
        	LogUtils.info("Error Code: " + e.getErrorCode());
        	LogUtils.info("Error Message: " + e.getErrorMessage());
        	LogUtils.info("Msg ID: " + e.getMsgId());
            return null;
        }    
    }
    
    public void jiguangPush(){
        //String alias = "123456";//声明别名
        List<String> alias = new ArrayList<>();
        alias.add("136");
        String msgContent = "weruy";
        String title = "testtest";
        LogUtils.info("对别名" + alias + "的用户推送信息");
        PushResult result = push(alias,ALERT,msgContent,title);
        if(result != null && result.isResultOK()){
        	LogUtils.info("针对别名" + alias + "的信息推送成功！");
        }else{
        	LogUtils.info("针对别名" + alias + "的信息推送失败！");
        }
    }
    
    public static void main(String[] args) {
		JPushUtil util = new JPushUtil();
		util.jiguangPush();
	}

}
