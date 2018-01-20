/**   
* @Title: BrowserType.java 
* @Package com.ujigu.secure.spider.enums 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年9月7日 下午3:21:07 
* @version V1.0   
*/
package com.ujigu.secure.spider.enums;

/**
 * @author user
 *
 */
public enum BrowserType {
	IE("IE浏览器"),
	FIREFOX("fireFox浏览器"),
	CHROME("chrome浏览器"),
	EDGE("adge浏览器"),
	OPERA("opera浏览器"),
	SAFARI("safari浏览器");
	
	private String value;
	
	BrowserType(String value) {
        this.value = value;
    }
	
	
    public String getValue() {
        return value;
    }
    
}



