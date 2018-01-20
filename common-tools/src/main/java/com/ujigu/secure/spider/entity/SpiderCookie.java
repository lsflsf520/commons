/**   
* @Title: SpiderCookie.java 
* @Package com.ujigu.secure.spider.entity 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年8月18日 上午10:44:08 
* @version V1.0   
*/
package com.ujigu.secure.spider.entity;

/**
 * @author user
 *
 */
public class SpiderCookie {

	private String key;
	private String value;
	private String host;
	
	
	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 0;
		result = host.hashCode() + key.hashCode() + value.hashCode();
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SpiderCookie)){
			SpiderCookie spiderCookie = (SpiderCookie)obj;
			try {
				if(host.equals(spiderCookie.getHost()) && key.equals(spiderCookie.getKey()) && value.equals(spiderCookie.getValue())){
					return true;
				}else{
					return false;
				}
			} catch (Exception e) {
				return false;
			}
		}else{
			return false;
		}
	}
	
}
