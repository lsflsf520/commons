package com.ujigu.secure.email.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.RegexUtil;

public class EmailConfig {

	private static final String SENDCLOUD_SMTP_HOST = "smtp.sendcloud.net";
	private static final int SENDCLOUD_SMTP_PORT = 25;
	
	public final static String TEMPLATE_API = BaseConfig.getValue("email.send_template_api", "http://api.sendcloud.net/apiv2/mail/sendtemplate");
	
	private final static String ATTACH_BASE_DIR = BaseConfig.getValue("email.attach.basedir", "/data/www/static/files/attachment/");
	private final static Properties props = System.getProperties();
	private final static Map<String, SenderInfo> senderMap = new HashMap<>();
	static{
		// 配置javamail
		props.setProperty("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", SENDCLOUD_SMTP_HOST);
		props.put("mail.smtp.port", SENDCLOUD_SMTP_PORT);
		props.setProperty("mail.smtp.auth", "true");
		props.put("mail.smtp.connectiontimeout", 2000); //毫秒为单位
		props.put("mail.smtp.timeout", 6000); //毫秒为单位
		props.setProperty("mail.mime.encodefilename", "true");
		
		Map<String, String> kvMap = BaseConfig.getKvMap("sendcloud.email");
		if(!CollectionUtils.isEmpty(kvMap)){
			for(String key : kvMap.keySet()){
				String apiuser = key.substring(0, key.lastIndexOf(".")).replace("sendcloud.email.", "").trim();
				SenderInfo sender = senderMap.get(apiuser);
				if(sender == null){
					String apikey = kvMap.get("sendcloud.email." + apiuser + ".apikey");
					String fromdomain = kvMap.get("sendcloud.email." + apiuser + ".fromdomain");
					String fromname = kvMap.get("sendcloud.email." + apiuser + ".fromname");
					String senderType = kvMap.get("sendcloud.email." + apiuser + ".type");
					String replyTo = kvMap.get("sendcloud.email." + apiuser + ".replyTo");
					
					if(StringUtils.isBlank(apikey) || StringUtils.isBlank(fromdomain) || StringUtils.isBlank(fromname)){
						throw new BaseRuntimeException("ILLEGAL_CONFIG", "参数配置有误", "apiuser:"+apiuser+",apikey:" + apikey + ",fromdomain:" + fromdomain + ",fromname:" + fromname);
					}
					
					senderMap.put(apiuser, new SenderInfo(apiuser, apikey.trim(), fromdomain.trim(), fromname.trim(), StringUtils.isBlank(senderType) ? SenderType.EVENT : SenderType.valueOf(senderType.trim()), replyTo));
				}
			}
		}
	}
	
	public static SenderInfo getSenderInfo(String apiuser){
		return senderMap.get(apiuser);
	}
	
	public static Properties getMailProps(){
		return props;
	}
	
	public static String getAttachBaseDir(){
		return ATTACH_BASE_DIR + (ATTACH_BASE_DIR.endsWith("/") || ATTACH_BASE_DIR.endsWith("\\") ? "" : "/");
	}
	
	public static boolean isWhiteModule(String module){
		String[] whiteModules = BaseConfig.getValueArr("email.white.module");
		
		return StringUtils.isNotBlank(module) && whiteModules != null && whiteModules.length > 0 && Arrays.asList(whiteModules).contains(module);
	}
	
	public static ResultModel checkEmail(List<String> emails){
		if(CollectionUtils.isEmpty(emails)){
			return new ResultModel("ILLEGAL_PARAM", "目标邮件地址不能为空");
		}
		
		for(String email : emails){
			if(!RegexUtil.isEmail(email) || email.length() > 64){
				return new ResultModel("ILLEGAL_PARAM", "目标邮件地址("+email+")格式错误！");
			}
		}
		
		return new ResultModel(true);
	}
	
	private enum SenderType{
		EVENT, BATCH
	}
	
	public static class SenderInfo{
		private String apiuser;
		private String apikey;
		private String fromdomain;
		private String fromname;
		private SenderType senderType;
		private String replyTo;
		
		public SenderInfo(String apiuser, String apikey, String fromdomain, String fromname, SenderType senderType, String replyTo) {
			this.apiuser = apiuser;
			this.apikey = apikey;
			this.fromdomain = fromdomain;
			this.fromname = fromname;
			this.senderType = senderType;
			this.replyTo = replyTo;
		}
		
		public String getApiuser() {
			return apiuser;
		}
		public String getApikey() {
			return apikey;
		}
		public String getFromdomain() {
			return fromdomain;
		}
		public String getFromname() {
			return fromname;
		}
		public boolean isEventType() {
			return SenderType.EVENT.equals(this.senderType);
		}
		public String getReplyTo() {
			return replyTo;
		}
		
	}
	
}
