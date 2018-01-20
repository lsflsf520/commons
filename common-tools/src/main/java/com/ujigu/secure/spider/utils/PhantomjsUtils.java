/**   
* @Title: PhantomjsUtils.java 
* @Package com.ujigu.secure.spider.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年8月30日 上午10:19:56 
* @version V1.0   
*/
package com.ujigu.secure.spider.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.client.CookieStore;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;

/**
 * @author user
 *
 */
public class PhantomjsUtils {
	
	public static final int WAIT_TIMES = 10;
	
	public static WebDriver createDriver(){
		
		WebDriver driver = null;
		if("1".equals(BaseConfig.getValue("service.isdev"))){
		//if(false){
			System.setProperty("webdriver.gecko.driver", "D:\\phantomjs2.1\\geckodriver.exe");   
			driver = new FirefoxDriver();  
		}else{
			String Phantomjs_Path = BaseConfig.getValue("servcie.phantomjs.path");
			LogUtils.info("phantomjs.binary.path:%s", Phantomjs_Path);
			System.setProperty("phantomjs.binary.path", Phantomjs_Path);
		    //driver = new PhantomJSDriver();
			DesiredCapabilities desiredCapabilities = DesiredCapabilities.phantomjs();
			//设置参数
			
			
			desiredCapabilities.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0");
			desiredCapabilities.setCapability("phantomjs.page.customHeaders.User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0");		
			driver = new PhantomJSDriver(desiredCapabilities);
		}
		
		return driver;
	}
	
	
	/*
	 * 等待
	 */
	public static WebDriverWait getWebDriverWait(WebDriver driver){
		return new WebDriverWait(driver,WAIT_TIMES);
	}
	
	public static WebDriver configurePhantomJSDriver(){
		WebDriver driver = null;
		if("1".equals(BaseConfig.getValue("service.isdev"))){
			//开发环境
			System.setProperty("phantomjs.binary.path", "D:\\phantomjs2.1\\phantomjs.exe");
		}else{
			String Phantomjs_Path = BaseConfig.getValue("servcie.phantomjs.path");
			LogUtils.info("phantomjs.binary.path:%s", Phantomjs_Path);
			System.setProperty("phantomjs.binary.path", Phantomjs_Path);
		}
		DesiredCapabilities desiredCapabilities = DesiredCapabilities.phantomjs();
		//设置参数
		desiredCapabilities.setCapability("phantomjs.page.settings.userAgent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
		desiredCapabilities.setCapability("phantomjs.page.customHeaders.User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");		
		driver = new PhantomJSDriver(desiredCapabilities);
		return driver;
		
	}
	
	/**
	 * 
	* @Title: setWebDriverCookie 
	* @Description: 填写cookie
	* @param @param driver
	* @param @param url
	* @param @return
	* @return WebDriver 返回类型
	* @throws
	 */
	public static WebDriver setWebDriverCookie(WebDriver driver, String url ,String key){
		CookieStore	cookieStore = HttpCookieManager.getCookies(key);
		if(cookieStore != null && cookieStore.getCookies().size() > 0){
			List<org.apache.http.cookie.Cookie> cookiesList = cookieStore.getCookies();
			driver.get(url);
			org.openqa.selenium.Cookie[] cookisArr = new org.openqa.selenium.Cookie[driver.manage().getCookies().size()];
			driver.manage().getCookies().toArray(cookisArr);
			
			for (org.apache.http.cookie.Cookie httpCookie : cookiesList) {
	    		System.out.println("\""+httpCookie.getName()+"\",\""+httpCookie.getValue()+"\",\""+httpCookie.getDomain()+"\",\""+httpCookie.getPath()+"\",\""+httpCookie.getExpiryDate());
	    		String domain = HttpCookieManager.parseDomain(url);
	    		System.out.println("domain:"+domain);
	    		System.out.println("url:"+driver.getCurrentUrl());
	    		if(httpCookie.getDomain().equals(domain)){
	    			org.openqa.selenium.Cookie cookie = new org.openqa.selenium.Cookie(httpCookie.getName(), httpCookie.getValue(), cookisArr[0].getDomain(), cookisArr[0].getPath(), cookisArr[0].getExpiry());
	    			//org.openqa.selenium.Cookie cookie = ;
	    			driver.manage().addCookie(cookie);
	    		}
			}
		}
		return driver;
	}
	
	
	public static Date getNextYear(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.YEAR, 1);
		return calendar.getTime();
	}
	
	public static void main(String[] args) {
		String url = "https://icorepnbs.pingan.com.cn/icore_pnbs/mainCtrl.tpl?applicantPersonFlag=1&familyPrd=&bsDetailCode=3-4-J-B&usageAttributeCode=02&ownershipAttributeCode=03&insuranceType=1&agentSalerName=&businessCertificateNum=&empBusinessCertificateNum=&deptCodeText=2201705&secondLevelDepartmentCode=220&deptCode=2201705&employeeCodeText=2200042635&employeeCode=2200042635&channelCode=J&brokerCode=00009585&productCombineList=&autoInsurance=true&propertyInsurance=false&accidentInsurance=false&rateClassFlag=14&partnerWorknetPanel=&agentCode=&worknetCode=&conferVal=&agentNameLike=&agentCodeText=&agentName=&conferNo=&subConferNo=&dealerCode=&employeeName=%E7%A8%8B%E7%A7%91&saleGroupCode=22017052928&businessMode=&systemId=ICORE-PTS&applyApproach=";
		WebDriver driver = PhantomjsUtils.createDriver();
	    driver.get(url);
	    
		try {
		    driver.manage().deleteAllCookies();
			org.openqa.selenium.Cookie cookie1 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie2 = new org.openqa.selenium.Cookie("BIGipServericore-pts_http_ng_PrdPool","2339445932.62325.0000","icore-pts.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie3 = new org.openqa.selenium.Cookie("JSESSIONID","JPoxTBpS9a37Cm3cCi2z0G_k2X5_IzAI05S8DcQOxTMzW6CoQcod!997753761","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie4 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie5 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie6 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie7 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie8 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie9 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie10 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie11 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			org.openqa.selenium.Cookie cookie12 = new org.openqa.selenium.Cookie("BIGipServerICORE-PNBS_DMZ_PrdPool","2205228204.42357.0000","icorepnbs.pingan.com.cn","/",null);
			
			driver.manage().addCookie(cookie1);
			driver.manage().addCookie(cookie2);
			driver.manage().addCookie(cookie3);
			
			driver.get(url);
			
			System.out.println(driver.findElement(By.tagName("html")).getText());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		    driver.close();
		}
	}
}
