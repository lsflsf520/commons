package com.xyz.tools.common.bean;

import org.apache.commons.lang.StringUtils;

public class TKD {
	
	private String title = null;
	private String kword = null;
	private String descp = null;
	private String classification = null;
	private String author = null;
	
	public TKD(String title){
		this.title = title;
	}
	
	public TKD(String title, String kword) {
		this(title);
		this.kword = kword;
	}
	
	public TKD(String title, String kword, String descp){
		this(title, kword);
		this.descp = descp;
	}
	
	public TKD(String title, String kword, String descp, String classification){
		this(title, kword, descp);
		this.classification = classification;
	}
	
	public String getTitle() {
		return title;
	}
	public String getKword() {
		return kword;
	}
	public String getDescp() {
		return descp;
	}
	
	public String getClassification() {
		return classification;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean isEmpty(){
		return StringUtils.isBlank(title) && StringUtils.isBlank(kword) && StringUtils.isBlank(descp) && StringUtils.isBlank(classification);
	}

}
