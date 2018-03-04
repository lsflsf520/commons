package com.xyz.tools.common.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
/**
 * 说明：windows时，必须拷贝tessdata文件夹到工程下。
 * */
public class ImageOcrUtils {

	private static final String linuxDataPath = BaseConfig.getValue("service.ocr.linux.dataPath");
	private static final String linuxImagePath = BaseConfig.getValue("service.ocr.linux.imagePath");
	private static final String winImagePath = BaseConfig.getValue("service.ocr.windows.imagePath");
	
	//linux下Base64图片识别(需要生成临时文件,jpg格式)
	public static String getBase64LinuxOCR(String base64Str){
		String result = "";
		String path = linuxImagePath + getRandomPic();
		if(ImageUtils.generateImage(base64Str, path)){
			result = ocrImage(path,2);
			File file = new File(path);
			if(file.exists()){
				file.delete();
			}
		}
		return result;
	}
	
	//windows下Base64图片识别(需要生成临时文件,jpg格式)
	public static String getBase64WindowsOCR(String base64Str){
		String result = "";
		String path = winImagePath + getRandomPic();
		if(ImageUtils.generateImage(base64Str, path)){
			result = ocrImage(path,1);
			File file = new File(path);
			if(file.exists()){
				file.delete();
			}
		}
		return result;
	}
	
	//linux图片识别(图片地址已知,支持多格式)
	public static String getPath2LinuxOCR(String path){
		String result = "";
		result = ocrImage(path,2);
		return result;
	}
	
	//windows图片识别(图片地址已知,支持多格式)
	public static String getPath2WindowsOCR(String path){
		String result = "";
		result = ocrImage(path,1);
		return result;
	}
	
	//获取随机数生成图片地址
	public static String getRandomPic(){
		Date date = new Date();
		SimpleDateFormat sd = new SimpleDateFormat("yyyyMMddHHmmss");
		int num = (int)(Math.random()*900000)+100000;
		String result = "/"+sd.format(date)+num+".jpg";
		return result;
	}
	
	//windows图片识别主代码(key 1---windows;2---linux)
	public static String ocrImage(String filePath,Integer key){
		File imageFile = new File(filePath); 
		ITesseract instance = new Tesseract();  // JNA Interface Mapping
		if(key == 2){
			instance.setDatapath(linuxDataPath);
			//instance.setDatapath("/usr/local/tessdata");
		}		
		String result = "";
		try {  
            result = instance.doOCR(imageFile);
        } catch (TesseractException e) { 
        	LogUtils.error("验证码解析异常", e, ""); 
        } 
		return result.trim();
	}
	
	//jpg图片识别（老方法）
	public static String ocrImage(String filePath){
		File imageFile = new File(filePath); 
		ITesseract instance = new Tesseract();  // JNA Interface Mapping
		instance.setDatapath("/usr/local/tessdata");
		String result = "";
		try {  
            result = instance.doOCR(imageFile);
            //System.out.println(result);  
        } catch (TesseractException e) { 
        	LogUtils.error("验证码解析异常", e, ""); 
        } 
		return result.trim();
	}
	
	//linux中，base64图片验证码识别（老方法）
	public static String getStrOCR(String base64Str){
		String result = "";
		if(ImageUtils.generateImage(base64Str, "/data/www/static/ocr/1.jpg")){
			result = ocrImage("/data/www/static/ocr/1.jpg",2);
			System.out.println(result);
		}
		return result;
	}
	
}
