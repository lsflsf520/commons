package com.ujigu.secure.test;

import org.apache.commons.lang.StringEscapeUtils;

public class XssTest {
	
	public static void main(String[] args) {
		String script = StringEscapeUtils.escapeJavaScript("<script>alert(\"哈哈，笨蛋！\")</script>");
		
		System.out.println(script);
	}

}
