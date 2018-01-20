package com.ujigu.secure.email.dto;

import java.util.List;
import java.util.Map;

import com.ujigu.secure.mq.bean.MqMsg;

public class Email extends MqMsg{
	
	private String apiuser;
	private String module;
	private List<String> tos;
	private String replyTo;
	private String title;
	private String tmplId; //模板ID
	private String content; //content 和 tmplId二选一，如果两个字段都有值，则会抛出异常
	private List<String> attachments;
	
	/**
	 * 关于下边两个参数的说明：
	 * 假如邮件内容为  "<p>欢迎使用<a href='http://sendcloud.sohu.com'>SendCloud!</a> %email%  %name%</p>"
	 * 其中有 email 和 name这两个变量，那么subMap中需要有 "%email%" 和  "%name%" 这两个变量
	 * 即： 
	 *   Map<String, List<String>> subMap = new HashMap<String, List<String>>();
		 subMap.put("%email%", Arrays.asList("xxxx@126.com", "yyyy@163.com"));
		 subMap.put("%name%", new ArrayList<String>(Arrays.asList("name1 %num1%", "name2 %num2%")));
	 *
	 * 注意："%name%"变量的值中又有两个变量分别为："%num1%" 和  "%num2%"
	 * 这时就需要用到 sectionMap，也就是说 sectionMap 是用来替换subMap变量值中的变量的
	 * 即：
	 *   Map<String, String> sectionMap = new HashMap<String, String>();
		 sectionMap.put("%num1%", "1111111111111111111");
		 sectionMap.put("%num2%", "2222222222222222222");
	 *
	 *这样设置之后，假如两个收件人地址分别为 "xxxx@126.com", "yyyy@163.com"，那么他们分别会收到如下内容：
	 *  "xxxx@126.com"将收到：
	 *    "<p>欢迎使用<a href='http://sendcloud.sohu.com'>SendCloud!</a> %xxxx@126.com%  %name1 1111111111111111111%</p>"
	 *
	 *  "yyyy@163.com"将收到：
	 *    "<p>欢迎使用<a href='http://sendcloud.sohu.com'>SendCloud!</a> %yyyy@163.com%  %name2 2222222222222222222%</p>"
	 *
	 */
	private Map<String, List<String>> subMap; //变量
	private Map<String, String> sectionMap;//变量中的变量
	
	public String getApiuser() {
		return apiuser;
	}
	public void setApiuser(String apiuser) {
		this.apiuser = apiuser;
	}
	
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public List<String> getTos() {
		return tos;
	}
	public void setTos(List<String> tos) {
		this.tos = tos;
	}
	public String getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTmplId() {
		return tmplId;
	}
	public void setTmplId(String tmplId) {
		this.tmplId = tmplId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<String> getAttachments() {
		return attachments;
	}
	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}
	public Map<String, List<String>> getSubMap() {
		return subMap;
	}
	public void setSubMap(Map<String, List<String>> subMap) {
		this.subMap = subMap;
	}
	public Map<String, String> getSectionMap() {
		return sectionMap;
	}
	public void setSectionMap(Map<String, String> sectionMap) {
		this.sectionMap = sectionMap;
	}
	
}
