package com.xyz.tools.common.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;

public class CoderUtils {

	 public static char ascii2Char(int ASCII) {  
	        return (char) ASCII;  
	    }  
	  
	    public static int char2ASCII(char c) {  
	        return (int) c;  
	    }  
	  
	    public static String ascii2String(int[] ASCIIs) {  
	        StringBuffer sb = new StringBuffer();  
	        for (int i = 0; i < ASCIIs.length; i++) {  
	            sb.append((char) ascii2Char(ASCIIs[i]));  
	        }  
	        return sb.toString();  
	    }  
	  
	    public static String ascii2String(String ASCIIs) {  
	        String[] ASCIIss = ASCIIs.split(",");  
	        StringBuffer sb = new StringBuffer();  
	        for (int i = 0; i < ASCIIss.length; i++) {  
	            sb.append((char) ascii2Char(Integer.parseInt(ASCIIss[i])));  
	        }  
	        return sb.toString();  
	    }  
	  
	    public static int[] string2ASCII(String s) {// 字符串转换为ASCII码  
	        if (s == null || "".equals(s)) {  
	            return null;  
	        }  
	  
	        char[] chars = s.toCharArray();  
	        int[] asciiArray = new int[chars.length];  
	  
	        for (int i = 0; i < chars.length; i++) {  
	            asciiArray[i] = char2ASCII(chars[i]);  
	        }  
	        return asciiArray;  
	    }  
	    
	    //汉字与GBK16进制内码转换
	    private static String hexString = "0123456789ABCDEF";
	    
	    private static int hex2Dec(char ch) {
	        if (ch == '0')
	            return 0;
	        if (ch == '1')
	            return 1;
	        if (ch == '2')
	            return 2;
	        if (ch == '3')
	            return 3;
	        if (ch == '4')
	            return 4;
	        if (ch == '5')
	            return 5;
	        if (ch == '6')
	            return 6;
	        if (ch == '7')
	            return 7;
	        if (ch == '8')
	            return 8;
	        if (ch == '9')
	            return 9;
	        if (ch == 'a')
	            return 10;
	        if (ch == 'A')
	            return 10;
	        if (ch == 'B')
	            return 11;
	        if (ch == 'b')
	            return 11;
	        if (ch == 'C')
	            return 12;
	        if (ch == 'c')
	            return 12;
	        if (ch == 'D')
	            return 13;
	        if (ch == 'd')
	            return 13;
	        if (ch == 'E')
	            return 14;
	        if (ch == 'e')
	            return 14;
	        if (ch == 'F')
	            return 15;
	        if (ch == 'f')
	            return 15;
	        else
	            return -1;
	 
	    }
	 
	    public static String gbk16Decode(String hexStr, String charSpliter) throws UnsupportedEncodingException {
	        if (null == hexStr || "".equals(hexStr) || (hexStr.length()) % 2 != 0) {
	            return null;
	        }
	        if(StringUtils.isNotBlank(charSpliter)){
	        	hexStr = hexStr.replace(charSpliter, "");
	        }
	 
	        int byteLength = hexStr.length() / 2;
	        byte[] bytes = new byte[byteLength];
	 
	        int temp = 0;
	        for (int i = 0; i < byteLength; i++) {
	            temp = hex2Dec(hexStr.charAt(2 * i)) * 16 + hex2Dec(hexStr.charAt(2 * i + 1));
	            bytes[i] = (byte) (temp < 128 ? temp : temp - 256);
	        }
	        return new String(bytes,"GBK");
	    }
	 
	    /*
	     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
	     */
	    public static String gbk16Encode(String str, String charSpliter) throws UnsupportedEncodingException {
	        // 根据默认编码获取字节数组
	        byte[] bytes = str.getBytes("GBK");
	        StringBuilder sb = new StringBuilder(bytes.length * 2);
	        if(StringUtils.isBlank(charSpliter)){
	        	charSpliter = "";
	        }
	        // 将字节数组中每个字节拆解成2位16进制整数
	        for (int i = 0; i < bytes.length; i++) {
	        	sb.append(charSpliter);
	            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
	            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
	        }
	        return sb.toString();
	    }
	    
	    public static void main(String[] args) throws IOException {  
	  
	        //String s = "好好学习！天天向上！————笑的自然 2009年3月11日";  
	    	/*String s = "刘尚风";
	        int[] intArray = string2ASCII(s);
	        for (int i = 0; i < intArray.length; i++) {  
	            System.out.print(intArray[i] + " " + Integer.toHexString(intArray[i]) + ", ");  
	        }  
	        
	        System.out.println();  
	        System.out.println(ascii2String(string2ASCII(s)));  
	  
	        System.out.println(Integer.parseInt("C1F5", 16) + " " + Integer.toHexString(49653));*/
	        //createFile("c://console_ch.txt", getCHASCII(0, 50000));  
	    	String encodeStr = gbk16Encode("刘尚风", "%");
	    	System.out.println(encodeStr);
	    	
	    	System.out.println(gbk16Decode(encodeStr, "%"));
	    }  
	
}
