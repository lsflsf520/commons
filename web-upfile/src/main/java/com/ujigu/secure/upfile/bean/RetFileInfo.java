package com.ujigu.secure.upfile.bean;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.upfile.util.ImageShellUtil;



/**
 * 
 * @author lsf
 *
 */
public class RetFileInfo {

	private String accessDomain;
	private String accessUri;
	private String primUri;
	private String originName;
	private String accessUrl;
	private String extension;
	private String humanSize;
	private String base64Code;
	private long imgSize;
	private int width;
	private int height;
	
	
	public String getAccessDomain() {
		return accessDomain;
	}
	public void setAccessDomain(String accessDomain) {
//		this.accessDomain = MultiDomUrl.exec(accessDomain);
		this.accessDomain = accessDomain;
	}
	public String getAccessUrl() {
		String url = getAccessDomain() + getAccessUri() ;
		setAccessUrl(url);
		return this.accessUrl;
	}
	public String getAccessUri() {
		return accessUri;
	}
	public void setAccessUri(String accessUri) {
		this.accessUri = accessUri;
	}
//	public double getImgSize() {
//		return imgSize;
//	}
//	public void setImgSize(double imgSize) {
//		this.imgSize = imgSize;
//	}
	
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getOriginName() {
		return originName;
	}
	public void setOriginName(String originName) {
		this.originName = originName;
	}
	
	public String getBase64Code() {
		return base64Code;
	}
	public void setBase64Code(String base64Code) {
		this.base64Code = base64Code;
	}
	
	public String getExtension(){
		String ext = FilenameUtils.getExtension(getAccessUri());
		setExtension(ext);
		return this.extension;
	}
	
	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	
	public String getPrimUri() {
		String filename = FilenameUtils.getBaseName(getAccessUri());
		if(filename.contains(ImageShellUtil.GIF_TMP_FLAG)){
			filename = filename.replace(ImageShellUtil.GIF_TMP_FLAG, "");
		}
		
		String dir = FilenameUtils.getFullPath(getAccessUri());
		String accessUri = dir + filename.split("\\.")[0] + "." + getExtension();
		setPrimUri(accessUri);
		return this.primUri;
	}
	public void setPrimUri(String primUri) {
		this.primUri = primUri;
	}
	
	public long getImgSize() {
		String baseDir = BaseConfig.getValue("photo.local.storage.path");
		File file = new File(baseDir + getAccessUri());
		if(file.exists()){
			setImgSize(file.length());
		}
		return imgSize;
	}
	public void setImgSize(long imgSize) {
		this.imgSize = imgSize;
	}
	
	public String getHumanSize() {
		long size = getImgSize();
		setHumanSize(FileUtils.byteCountToDisplaySize(size));
		return humanSize;
	}
	public void setHumanSize(String humanSize) {
		this.humanSize = humanSize;
	}
	@Override
	public String toString() {
		return "{\"accessDomain\":\"" + accessDomain + "\",\"accessUri\":\"" + accessUri + "\",\"accessUrl\":\"" + getAccessUrl() + "\",\"originName\":\"" + originName + "\",\"extension\":\"" + getExtension() + "\",\"width\":" + width + ",\"height\":" + height;
	}
}
