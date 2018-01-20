package com.ujigu.secure.tools.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ujigu.secure.common.bean.GlobalConstant;
import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.ThreadUtil;
import com.ujigu.secure.web.util.RestClientUtil;
import com.ujigu.secure.web.util.WebUtils;

@Controller
@RequestMapping("tools/zhengxin")
public class ZhengXinController {
	
	private final static String IMG_CODE_DIR = BaseConfig.getValue("img.code.tmp.dir", "/data/www/static/download/");
//	private final static String IMG_CODE_DIR = BaseConfig.getValue("img.code.tmp.dir", "D:/data/www/static/download/");
	private final static String ACCESS_PREFIX = BaseConfig.getValue("img.code.access.prefix", GlobalConstant.STATIC_DOMAIN + "/dl/");
	
	private final static String COOKIE_KEY = "_ck_";
	
	private final static Map<String, String> certTypeMap = new LinkedHashMap<>();
	static{
		//注册的时候要用到的证件类型
		certTypeMap.put("0", "身份证");
		certTypeMap.put("1", "户口簿");
		certTypeMap.put("2", "护照");
		certTypeMap.put("3", "军官证");
		certTypeMap.put("4", "士兵证");
		certTypeMap.put("5", "港澳居民来往内地通行证");
		certTypeMap.put("6", "台湾同胞来往内地通行证");
		certTypeMap.put("7", "临时身份证");
		certTypeMap.put("8", "外国人居留证");
		certTypeMap.put("9", "警官证");
		certTypeMap.put("A", "香港身份证");
		certTypeMap.put("B", "澳门身份证");
		certTypeMap.put("C", "台湾身份证");
		certTypeMap.put("X", "其他证件");
	}
	
	@RequestMapping("regparam")
	@ResponseBody
	public ResultModel loadRegParam(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String token = getRegToken(request, response);
		
		String codeImg = getImgCodeUrl(request, response);
		
		return ResultModel.buildMapResultModel().put("certTypeMap", certTypeMap).put("codeImg", codeImg).put("token", token);
	}
	
	private String getRegToken(HttpServletRequest request, HttpServletResponse response) throws Exception{
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("method", "initReg");
		Document doc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/userReg.do", paramMap, Method.POST, "https://ipcrs.pbccrc.org.cn/page/login/loginreg.jsp");
		LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/userReg.do?method=initReg", doc.toString());
		
		String token = doc.select("input[name=org.apache.struts.taglib.html.TOKEN]").val();
		return token;
	}
	
	@RequestMapping("doreg")
	@ResponseBody
	public ResultModel doreg(HttpServletRequest request, HttpServletResponse response, String token, String name, String certType, String certNo, String imgCode) throws Exception{
		if(StringUtils.isBlank(token) || StringUtils.isBlank(name) || StringUtils.isBlank(certType) || StringUtils.isBlank(certNo) || StringUtils.isBlank(imgCode)){
			LogUtils.debug("token:%s,name:%s,certType:%s,certNo:%s,imgCode:%s", token, name, certType, certNo, imgCode);
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("1", "on");
		paramMap.put("org.apache.struts.taglib.html.TOKEN", token);
		paramMap.put("method", "checkIdentity");
		paramMap.put("userInfoVO.name", name);
		paramMap.put("userInfoVO.certType", certType);
		paramMap.put("userInfoVO.certNo", certNo);
		paramMap.put("_@IMGRC@_", imgCode);
		
		
		Document doc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/userReg.do", paramMap, Method.POST, "https://ipcrs.pbccrc.org.cn/userReg.do");
		LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/userReg.do?method=checkIdentity", doc.toString());
		Element errorElem = doc.select("#_error_field_").first();
		if(errorElem != null){
			String html = errorElem.html();
			return new ResultModel("REG_ERR", html);
		} else if(doc.select("div.error").first() != null){
			WebUtils.deleteCookie(request, response, COOKIE_KEY);
			token = getRegToken(request, response);
			String codeImg = getImgCodeUrl(request, response);
			
			ResultModel resultModel = new ResultModel("REG_ERR", "回话信息已过期，请刷新后重试");
			resultModel.addExtraInfo("token", token);
			resultModel.addExtraInfo("codeImg", codeImg);
			
			return resultModel;
		} else if(doc.select("#loginname").first() != null){
			//校验成功，则读取表单中的token信息
			String newToken = doc.select("input[name=org.apache.struts.taglib.html.TOKEN]").val();
			
			return ResultModel.buildMapResultModel().put("token", newToken);
		}
		
		return new ResultModel("UNKNOWN_ERR", "当前请求发生未知错误");
	}
	
	@RequestMapping("sendphonecode")
	@ResponseBody
	public ResultModel sendPhoneCode(HttpServletRequest request, HttpServletResponse response, String mobileTel) throws Exception{
		if(StringUtils.isBlank(mobileTel)){
			return new ResultModel("ILLEGAL_PARAM", "手机号不能为空");
		}
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("method",	"getAcvitaveCode");
		paramMap.put("mobileTel",	mobileTel);
		Document doc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/userReg.do", paramMap, Method.POST, "https://ipcrs.pbccrc.org.cn/userReg.do");
	
		LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/userReg.do?method=getAcvitaveCode", doc.toString());
		
		String tcId = doc.select("body").html();
		tcId = StringUtils.isBlank(tcId) ? "" : tcId.trim();
		System.out.println("tcId:" + tcId);
		
	    return new ResultModel(tcId);
	}
	
	@RequestMapping("doregother")
	@ResponseBody
	public ResultModel doRegOtherInfo(HttpServletRequest request, HttpServletResponse response, String token, String tcId, String loginname, String password,
			                 String cfpasswd, String email, String mobileTel, String verifyCode, String smsrcvtimeflag) throws Exception{
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("method",	"saveUser");
		paramMap.put("counttime", "1");
		paramMap.put("tcId", tcId);
		paramMap.put("org.apache.struts.taglib.html.TOKEN", token);
		paramMap.put("userInfoVO.loginName",	loginname);
		paramMap.put("userInfoVO.password",	password);
		paramMap.put("userInfoVO.confirmpassword",	cfpasswd);
		paramMap.put("userInfoVO.email",	email);
		paramMap.put("userInfoVO.mobileTel",	mobileTel);
		paramMap.put("userInfoVO.verifyCode",	verifyCode);
		paramMap.put("userInfoVO.smsrcvtimeflag",	smsrcvtimeflag);
		
		Document doc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/userReg.do", paramMap, Method.POST, "https://ipcrs.pbccrc.org.cn/userReg.do");
		LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/userReg.do?method=saveUser", doc.toString());
		Element errElem = doc.select("#_error_field_").first();
		if(errElem != null){
			token = doc.select("input[name=org.apache.struts.taglib.html.TOKEN]").val();
			
			ResultModel resultModel = new ResultModel("REQ_ERR", errElem.html());
			resultModel.addExtraInfo("token", token);
			
			return resultModel;
		} else if(doc.select("div.error").first() != null){
			WebUtils.deleteCookie(request, response, COOKIE_KEY);
			String codeImg = getImgCodeUrl(request, response);
			
			ResultModel resultModel = new ResultModel("REG_ERR", "回话信息已过期，请刷新后重试");
			resultModel.addExtraInfo("codeImg", codeImg);
			
			return resultModel;
		} else if(doc.select("p.padding_top_10").first() != null){
			
			return new ResultModel("您在个人信用信息平台已注册成功，即将前往登陆页面");
		}
		
		return new ResultModel("REQ_ERR", "当前请求发生未知错误，请刷新后重试！");
	}
	
	private Document remoteRequest(HttpServletRequest request, HttpServletResponse response, String url, Map<String, String> paramMap, Method method, String referer) throws Exception{
		
		Map<String, String> cookieMap = getCookies(request, response);
		
		Response reqResp = remoteRequest(url, paramMap, cookieMap, method, referer);
		
		return Jsoup.parse(reqResp.body(), "GBK");
	}
	
	private Map<String, String> getCookies(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String cookieVal = initCookieVal(request, response);
		Map<String, String> cookieMap = new HashMap<>();
		if(StringUtils.isNotBlank(cookieVal)){
			String[] parts = cookieVal.split(":::");
			for(String part : parts){
				if(StringUtils.isNotBlank(part) && part.contains("::")){
					String[] kv = part.split("::");
					cookieMap.put(kv[0], kv[1]);
				}
			}
		}
		
		return cookieMap;
	}
	
	private String initCookieVal(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String cookieVal = WebUtils.getCookieValue(request, COOKIE_KEY);
		if(StringUtils.isBlank(cookieVal)){
			cookieVal = ThreadUtil.get("CURR_COOKIE");
			if(StringUtils.isBlank(cookieVal)){
				Response initResp = remoteRequest("https://ipcrs.pbccrc.org.cn", new HashMap<String, String>(), new HashMap<String, String>(), Method.GET, null);
				Map<String, String> cookieMap = initResp.cookies();
				if(!CollectionUtils.isEmpty(cookieMap)){
					cookieVal = "";
					for(String key : cookieMap.keySet()){
						String value = cookieMap.get(key);
						if(StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)){
							cookieVal += key + "::" + cookieMap.get(key) + ":::";
						}
					}
					
					ThreadUtil.putIfAbsent("CURR_COOKIE", cookieVal);
					
					String encCookie = WebUtils.urlEncode(cookieVal);
					WebUtils.setCookieValue(response, COOKIE_KEY, encCookie, -1);
				}
			}
		} else {
			if(!cookieVal.contains(":::")){
				cookieVal = WebUtils.urlDecode(cookieVal);
			}
		}
		
		return cookieVal;
	}
	
	private Response remoteRequest(String url, Map<String, String> paramMap, Map<String, String> cookieMap, Method method, String referer) throws Exception{
		trustAllHttpsCertificates();
		HttpsURLConnection.setDefaultHostnameVerifier(hv);
		
		paramMap = (paramMap == null ? new HashMap<String, String>() : paramMap);
		cookieMap = (cookieMap == null ? new HashMap<String, String>() : cookieMap);
		
		LogUtils.debug("url:%s, paramMap:%s, cookieMap:%s, method:%s, referer:%s", url, paramMap, cookieMap, method, referer);
		Response response = Jsoup.connect(url).data(paramMap)
                .cookies(cookieMap)
//                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
//                .header("Accept-Encoding", "gzip, deflate, br")
//                .header("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2")
                .header("Referer", StringUtils.isBlank(referer) ? "" : referer)
                .header("Upgrade-Insecure-Requests", "1")
                .header("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .method(method).timeout(30000).ignoreContentType(true).execute();
		
		return response;
	}
	
	@RequestMapping("reloadimgcode")
	@ResponseBody
	public ResultModel getImgCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
		String codeImg = getImgCodeUrl(request, response);
		
		return new ResultModel(codeImg);
	}
	
	private String getImgCodeUrl(HttpServletRequest request, HttpServletResponse response) throws Exception{
		Map<String, String> cookieMap = getCookies(request, response);
		
		Response reqResp = remoteRequest("https://ipcrs.pbccrc.org.cn/imgrc.do?" + System.currentTimeMillis(), new HashMap<String, String>(), cookieMap, Method.GET, "https://ipcrs.pbccrc.org.cn/page/login/loginreg.jsp");
		
		String codeImg = RestClientUtil.saveFile(reqResp.bodyAsBytes(), IMG_CODE_DIR + (IMG_CODE_DIR.endsWith("/") || IMG_CODE_DIR.endsWith("\\") ? "" : "/") + "imgcode", System.currentTimeMillis() + ".jpg");
		
		String accessUrl = ACCESS_PREFIX + codeImg.replace(IMG_CODE_DIR, "");
		
		return accessUrl;
	}

	@RequestMapping("loginparam")
	@ResponseBody
	public ResultModel loadLoginParam(HttpServletRequest request, HttpServletResponse response){
		
		ResultModel resultModel = ResultModel.buildMapResultModel();
		try {
			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("method", "initLogin");
			Document doc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/login.do", paramMap, Method.GET, "https://ipcrs.pbccrc.org.cn/index1.do");
			
			LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/login.do?method=initLogin", doc.toString());
			
			//String loginAction = doc.select("form[name=loginForm]").attr("action");
			//String method = doc.select("input[name=method]").val();
			String date = doc.select("input[name=date]").val();
			String token = doc.select("input[name=org.apache.struts.taglib.html.TOKEN]").val();
			
			String accessUrl = getImgCodeUrl(request, response);
			
//			resultModel.put("action", "https://ipcrs.pbccrc.org.cn" + loginAction);
			resultModel.put("token", token);
			//resultModel.put("method", method);
			resultModel.put("date", date);
//			resultModel.put("loginname", "loginname");
//			resultModel.put("password", "password");
			resultModel.put("codeImg", accessUrl);  //_@IMGRC@_
			
		} catch (MalformedURLException e) {
			LogUtils.error(e.getMessage(), e);
			return new ResultModel("NET_ERR", "央行征信接口请求出错，请重试！");
		} catch (IOException e) {
			LogUtils.error(e.getMessage(), e);
			return new ResultModel("NET_ERR", "央行征信接口请求出错，请重试！");
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return new ResultModel("NET_ERR", "央行征信接口请求出错，请重试！");
		}
		
		return resultModel;
	}
	
	@RequestMapping("dologon")
	@ResponseBody
	public ResultModel doLogon(HttpServletRequest request, HttpServletResponse response, String token, 
			  String date, String loginname, String password, String imgCode){
		if(StringUtils.isBlank(token) || StringUtils.isBlank(loginname) || StringUtils.isBlank(password) || StringUtils.isBlank(imgCode) || StringUtils.isBlank(date)){
			LogUtils.debug("token:%s,date:%s,loginname:%s,password:%s,imgCode:%s", token, date, loginname, password, imgCode);
			
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}
		Map<String, String> pageParam = new HashMap<String, String>();
		pageParam.put("method", "login");
		pageParam.put("date", date);	
		pageParam.put("loginname", loginname);
		pageParam.put("password", password);
		pageParam.put("_@IMGRC@_", imgCode);
		pageParam.put("org.apache.struts.taglib.html.TOKEN", token);
		
		String errorMsg = "登陆失败，请重试！";
		try {
			Document doc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/login.do", pageParam, Method.POST, "https://ipcrs.pbccrc.org.cn/page/login/loginreg.jsp");
			
			LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/login.do?method=login", doc.toString());
			
			String mainSrc = doc.select("#mainFrame").attr("src");
			if("https://ipcrs.pbccrc.org.cn/welcome.do".equals(mainSrc)){
				Document welcomeDoc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/welcome.do", new HashMap<String, String>(), Method.GET, null);
				
				LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/welcome.do", welcomeDoc.toString());
				
				Map<String, Boolean> infoStatMap = new HashMap<>();
				
				boolean hasReport = false;
				
				Elements pElems = doc.select(".span-grey2");
				if(pElems != null && pElems.size() > 0){
					for(Element elem : pElems){
						String text = elem.text();
						if(StringUtils.isNotBlank(text)){
							text = text.trim();
							if(text.contains("您的个人信用信息提示") || text.contains("您的个人信用信息概要") || text.contains("您的个人信用报告") ){
								boolean reportReady = "button".equals(elem.select("input.earn_yl").attr("type"));
								if(reportReady){
									hasReport = true;
								}
								
								infoStatMap.put(text, reportReady);
							}
						}
					}
				}
				
				/*if(welcomeDoc.select("#id_getinfo").first() != null){
					return ResultModel.buildMapResultModel().put("status", "COMPLETE"); //请输入央行发送的手机验证码查看报告！
				} else if(welcomeDoc.select("").first() != null){
					return ResultModel.buildMapResultModel().put("status", "WAIT"); //申请已提交，请耐心等待
				}*/
				
				ResultModel resultModel = new ResultModel(infoStatMap);
				resultModel.addExtraInfo("hasReport", hasReport + "");
				resultModel.addExtraInfo("hasApply", (infoStatMap.size() > 0) + "");
				
				return resultModel;
			} else if(StringUtils.isNotBlank(doc.select(".p4").text())){
				errorMsg = doc.select(".p4").text();
			}
		} catch (IOException e) {
			LogUtils.error(e.getMessage(), e);
			return new ResultModel("NET_ERR", "央行征信接口请求出错，请重试！");
		} catch (Exception e) {
			LogUtils.error(e.getMessage(), e);
			return new ResultModel("NET_ERR", "央行征信接口请求出错，请重试！");
		}
		
		return new ResultModel("LOGIN_ERR", errorMsg);
	}
	
	@RequestMapping("cardauth")
	@ResponseBody
	public ResultModel creditCardAuth( ){
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("accessType", "0");
		paramMap.put("backUrl", "http://www.specialUrl.com");
		paramMap.put("bizType", "000000");
		paramMap.put("certId", "0");
		paramMap.put("channelType", "07");
		paramMap.put("customerIp", ThreadUtil.getSrcIP());
		paramMap.put("encoding", "UTF-8");
		paramMap.put("frontUrl", "http://www.95516.com/");
		paramMap.put("accessType", "0");
		paramMap.put("accessType", "0");
		paramMap.put("accessType", "0");
		paramMap.put("accessType", "0");
		paramMap.put("accessType", "0");
		
		
		return new ResultModel(true);
	}
	
	@RequestMapping("queryreport")
	@ResponseBody
	public ResultModel queryReport(HttpServletRequest request, HttpServletResponse response, String tradeCode) throws Exception{
		if(StringUtils.isBlank(tradeCode)){
			return new ResultModel("ILLEGAL_PARAM", "参数错误");
		}
		Map<String, String> queryParam = new HashMap<>();
		queryParam.put("counttime", "");
		queryParam.put("reportformat", "25"); //25：个人信用信息提示；24：个人信用信息概要；21：个人信用报告
		queryParam.put("tradeCode", tradeCode); //央行发送的查看信用报告的验证码
		
		Document doc = remoteRequest(request, response, "https://ipcrs.pbccrc.org.cn/reportAction.do?method=viewReport", queryParam, Method.POST, "https://ipcrs.pbccrc.org.cn/reportAction.do?method=queryReport");
		
		String content = doc.toString();
		LogUtils.debug("url:%s, document:%s", "https://ipcrs.pbccrc.org.cn/reportAction.do?method=viewReport", content);
		if(content.contains("由于您长时间未进行任何操作，系统已退出，如需继续使用请您重新登录。")){
			return new ResultModel("NOT_LOGON", "由于您长时间未进行任何操作，系统已退出，如需继续使用请您重新登录。");
		} else {
			Element td = doc.select("tbody > tr > td").first();
			if(td != null){
				String info = td.html();
				
				return new ResultModel(info);
			}
			
		}
		
		return new ResultModel("QUERY_ERR", "查询出错");
	}
	
	HostnameVerifier hv = new HostnameVerifier() {
		
		@Override
		public boolean verify(String hostname, SSLSession session) {
			LogUtils.debug("Warning: URL Host: %s vs. %s", hostname, session.getPeerHost());
			return false;
		}
	};

    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
            return;
        }
    }
	
	public static void main(String[] args) throws Exception {
//		ResultModel resultModel = new ZhengXinController().loadLoginParam();
//		
//		System.out.println(JsonUtil.create().toJson(resultModel));
		
		/*ResultModel resultModel = new ZhengXinController().doLogon("https://ipcrs.pbccrc.org.cn/login.do", "G7d3hdLBc11yswqkJMHfWy261nd4cRTw3t1yJvGGcjjzhqZRb2QD!-626646733", 
//				 "afaec9e756bdde15ef233841801c1522", 
				 "login", "1516097659408", "17773132069", "lsf335869", "yf9prb");
		
		System.out.println(JsonUtil.create().toJson(resultModel));*/
		
		Map<String, String> cookieMap = new HashMap<>();
		cookieMap.put("TSf75e5b", "84a1dffa321bae58f8ca792d0313d7379fa28fc9f920e1aa5a62bd94");
		cookieMap.put("BIGipServerpool_ipcrs_web", "NMq2Bh9fWv5bJAAvb+H7Of3zy4BZ/9ImdwreXXOd8p3P620xxieoJhGii5lwJ1wbLRkjWN9EDwnP");
		cookieMap.put("BIGipServerpool_ipcrs_app", "KS5Tb/NS5mUN+Vkvb+H7Of3zy4BZ/1tIGA9/MRdwB+9mHQcul2AeM+MgDdlCteQajEP4l9YtCV8yd69gC4E+8TU8Nv4TiQk8rgmWMHoLmip0M8zg9rlmb7hswB7Al7tOdZXi5n0eE3IQGbUIJVu6LxkGAPCxhw==");
		cookieMap.put("JSESSIONID", "h8hQhv7V2ZJxc87TLbXbsdGwx1TYjTPLyVmfNp6cv84bgrT5SXFf!1926714625");
		
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("method", "getunionpaycode");
		Response response = new ZhengXinController().remoteRequest("https://ipcrs.pbccrc.org.cn/unionpayAction.do", paramMap, cookieMap, Method.POST, "https://ipcrs.pbccrc.org.cn/reportAction.do");
		System.out.println(response.body());
		Document doc = Jsoup.parse(response.body());
		String action = doc.select("#sform").attr("action");
		Map<String, String> currParamMap = new HashMap<>();
		for(Element elem : doc.select("input")){
			currParamMap.put(elem.attr("name"), elem.val());
		}
		
	    System.out.println(action + " " + currParamMap);
	    Response currResp = Jsoup.connect(action).data(currParamMap).cookies(response.cookies()).method(Method.POST).timeout(30000).execute();
	    
	    System.out.println(currResp.body());
	    doc = Jsoup.parse(currResp.body());
	    action = doc.select("#defaultForm").attr("action");
	    currParamMap.clear();
	    for(Element elem : doc.select("input")){
			currParamMap.put(elem.attr("name"), elem.val());
		}
	    currResp = Jsoup.connect(action).data(currParamMap).cookies(currResp.cookies()).method(Method.POST).timeout(30000).execute();
	    System.out.println(currResp.body());
	    
	    doc = Jsoup.parse(currResp.body());
	    action = doc.select("#defaultForm").attr("action");
	    currParamMap.clear();
	    for(Element elem : doc.select("input")){
			currParamMap.put(elem.attr("name"), elem.val());
		}
	    currResp = Jsoup.connect(action).data(currParamMap).cookies(currResp.cookies()).method(Method.POST).timeout(30000).execute();
	    System.out.println(currResp.body());
	    
	    doc = Jsoup.parse(currResp.body());
	    String errorMsg = doc.select(".sub_word").text();
	    if(StringUtils.isNotBlank(errorMsg)){
	    	System.out.println("ERROR\n" + errorMsg );
	    }
		
	    /*Map<String, String> paramMap = new HashMap<>();
		paramMap.put("method", "initReg");
		
		Response response = new ZhengXinController().remoteRequest("https://ipcrs.pbccrc.org.cn/userReg.do", paramMap, cookieMap, Method.POST, "https://ipcrs.pbccrc.org.cn/page/login/loginreg.jsp");
		System.out.println(response.body());*/
		
		/*Map<String, String> paramMap = new HashMap<>();
		paramMap.put("method", "initLogin");
		Response response = new ZhengXinController().remoteRequest("https://ipcrs.pbccrc.org.cn/page/login/loginreg.jsp", paramMap, cookieMap, Method.GET, "https://ipcrs.pbccrc.org.cn/index1.do");
		System.out.println(response.body());*/
		
		
		/*Map<String, String> paramMap = new HashMap<>();
		paramMap.put("1", "on");
		paramMap.put("org.apache.struts.taglib.html.TOKEN", "bfdaf41ded35be7f4c315943cf3a5d8c");
		paramMap.put("method", "checkIdentity");
//		paramMap.put("userInfoVO.name", URLEncoder.encode("涂建军", "GBK"));
		paramMap.put("userInfoVO.name", "涂建军");
//		paramMap.put("userInfoVO.name", "%CD%BF%BD%A8%BE%FC");
		paramMap.put("userInfoVO.certType", "0");
		paramMap.put("userInfoVO.certNo", "430726199110090813");
		paramMap.put("_@IMGRC@_", "9bx8qg");
		
		Response response = new ZhengXinController().remoteRequest("https://ipcrs.pbccrc.org.cn/userReg.do", paramMap, cookieMap, Method.POST, "https://ipcrs.pbccrc.org.cn/userReg.do");
	
	    System.out.println(response.body());*/
		
		/*Map<String, String> paramMap = new HashMap<>();
		paramMap.put("method",	"getAcvitaveCode");
		paramMap.put("mobileTel", "17773132069");
		Response response = new ZhengXinController().remoteRequest("https://ipcrs.pbccrc.org.cn/userReg.do", paramMap, cookieMap, Method.POST, "https://ipcrs.pbccrc.org.cn/userReg.do");
	
		System.out.println(response.body());*/
	}
}
