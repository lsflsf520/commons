package com.xyz.tools.common.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.constant.GlobalConstant;

public class ImageUtils {

	private final static Logger LOG = LoggerFactory.getLogger(ImageUtils.class);
	private static HttpURLConnection httpUrl = null;  

	// 图片转化成base64字符串
	public static String getImageStr(String imgFilePath) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
		InputStream in = null;
		byte[] data = null;
		//读取图片字节数组
		try {
			in = new FileInputStream(imgFilePath);
			data = new byte[in.available()];
			in.read(data);
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		// 对字节数组Base64编码
		return Base64.encodeBase64String(data);
	}

	// base64字符串转化成图片
	public static boolean generateImage(String imgStr, String toImgFilePath) { // 对字节数组字符串进行Base64解码并生成图片
		if (StringUtils.isBlank(imgStr)) { // 图像数据为空
			return false;
		}
		OutputStream out = null;
		try {
			// Base64解码
			byte[] b = Base64.decodeBase64(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
			// 生成jpeg图片
			out = new FileOutputStream(toImgFilePath);
			out.write(b);
			out.flush();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
			return false;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		return true;
	}
	
	/** 
     * 从URL中读取图片,转换成流形式. 
     * @param destUrl 
     * @return 
     */  
    public static InputStream saveToFile(String destUrl){  
          
        URL url = null;  
        InputStream in = null;   
        try{  
            url = new URL(destUrl);  
            httpUrl = (HttpURLConnection) url.openConnection();  
            httpUrl.connect();  
            httpUrl.getInputStream();  
            in = httpUrl.getInputStream();            
            return in;  
        }catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
    
    /** 
     * 读取输入流,转换为Base64字符串 
     * @param input 
     * @return 
     */  
    public static String GetImageStrByInPut(InputStream input) {  
        byte[] data = null;  
        // 读取图片字节数组  
        try {  
            data = new byte[input.available()];  
            input.read(data);  
            input.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }          
        return Base64.encodeBase64String(data);// 返回Base64编码过的字节数组字符串  
    }  
    
    //读取输入流,转换为Base64字符 
    public static String urlToBase64(String url){
    	InputStream in = saveToFile(url);
    	String str = GetImageStrByInPut(in);  //读取输入流,转换为Base64字符  
    	httpUrl.disconnect(); 
    	return str;
    }
    
    public static void main(String args[]){
    	System.out.println(urlToBase64("http://img-test.baoxianjie.net/2017/3/13/img186491820434419_0.jpeg"));
    	
    }

	
	public static String buildWellImgUrl(String imgUri){
		if(StringUtils.isBlank(imgUri)){
			return null;
		}
		if(imgUri.startsWith("http://") || imgUri.startsWith("https://")){
			return imgUri;
		}
		
		return imgUri.startsWith("/") ? GlobalConstant.STATIC_DOMAIN + imgUri : GlobalConstant.STATIC_DOMAIN + "/" + imgUri;
	}
	
}
