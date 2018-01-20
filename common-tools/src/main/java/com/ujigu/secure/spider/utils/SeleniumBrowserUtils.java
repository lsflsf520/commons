/**   
* @Title: SeleniumIEUtils.java 
* @Package com.ujigu.secure.spider.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年9月7日 下午3:16:48 
* @version V1.0   
*/
package com.ujigu.secure.spider.utils;

import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.spider.enums.BrowserType;

/**
 * @author user
 *
 */
public class SeleniumBrowserUtils {

	
	public static final int WAIT_TIMES = 10;
	
	
	/**
	 * 
	* @Title: createDriver 
	* @Description: 获得不同类型的浏览器驱动 
	* @param @param browserType
	* @param @return
	* @return WebDriver 返回类型
	* @throws
	 */
	public static WebDriver createDriver(BrowserType browserType){
		WebDriver driver = null;
		String path = "";
		switch (browserType) {
		case IE:
			path = BaseConfig.getValue("selenium.ie");
			System.setProperty("webdriver.ie.driver",path);
			driver=new InternetExplorerDriver();
			break;
		case FIREFOX:
			path = BaseConfig.getValue("selenium.firefox");
			System.setProperty("webdriver.gecko.driver", path);
			driver = new FirefoxDriver();
			break;
		default:
			LogUtils.error("创建驱动失败，暂不支持的浏览器：%s", null, browserType.getValue());
			break;
		}
		return driver;
	}
	
	
	/*
	 * 等待
	 */
	public static WebDriverWait getWebDriverWait(WebDriver driver){
		return new WebDriverWait(driver,WAIT_TIMES);
	}
	
	/**
	 * 切换窗口
	 * */
	public static boolean switchToWindow(String windowTitle,WebDriver dr){    
        boolean flag = false;    
        try {   
            //将页面上所有的windowshandle放在入set集合当中  
            String currentHandle = dr.getWindowHandle();    
            Set<String> handles = dr.getWindowHandles();    
            for (String s : handles) {    
                if (s.equals(currentHandle))    
                    continue;    
                else {    
                    dr.switchTo().window(s);  
            //和当前的窗口进行比较如果相同就切换到windowhandle  
            //判断title是否和handles当前的窗口相同  
                    if (dr.getTitle().contains(windowTitle)) {    
                        flag = true;    
                        System.out.println("Switch to window: "    
                                + windowTitle + " successfully!");    
                        break;    
                    } else    
                        continue;    
                }    
            }    
        } catch (Exception e) {    
            System.out.printf("Window: " + windowTitle    
                    + " cound not found!", e.fillInStackTrace());    
            flag = false;    
        }    
        return flag;    
    }   
}
