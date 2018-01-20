/**   
* @Title: SeleniumHtmlUnitUtils.java 
* @Package com.ujigu.secure.spider.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年9月6日 上午10:21:34 
* @version V1.0   
*/
package com.ujigu.secure.spider.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * @author user
 *
 */
public class SeleniumHtmlUnitUtils {
	
	public static final int WAIT_TIMES = 10;
	
	/**
	 * 
	* @Title: createDriver 
	* @Description: 获得驱动，带浏览器标识
	* @param @param browserVersion
	* @param @return
	* @return HtmlUnitDriver 返回类型
	* @throws
	 */
	public static HtmlUnitDriver createDriver(BrowserVersion browserVersion) {
		if(browserVersion == null){
			browserVersion = BrowserVersion.FIREFOX_38;
		}
		//IE8 、允许js
		HtmlUnitDriver driver = new HtmlUnitDriver(browserVersion, true);
		//接受所有证书
		driver.setAcceptSslCertificates(true);
		return driver;
	}
	
	
	/**
	 * 
	* @Title: createDriver 
	* @Description: 获得驱动，默认firefox_38
	* @param @return
	* @return HtmlUnitDriver 返回类型
	* @throws
	 */
	public static HtmlUnitDriver createDriver() {
		return createDriver(null);
	}
	
	
	/*
	 * 等待
	 */
	public static WebDriverWait getWebDriverWait(WebDriver driver){
		return new WebDriverWait(driver,WAIT_TIMES);
	}
}
