package com.ujigu.secure.upfile.util;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.EncryptTools;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.upfile.bean.ImgConstant;

public class PathUtils {
	
	private final static Logger LOG = LoggerFactory.getLogger(PathUtils.class);
	
	public final static String SPLITER = "`";
	
	/**
	 * 检测目录名是否合法
	 * @return
	 */
	public static boolean isRightDir(String dirname){
		return RegexUtil.checkRegex("[\\w_-]+", dirname);
	}

	/**
	 * <Description>checkDirs: 检查savePath指定的路径是否存在，如果不存在，则创建之</Description>
	 * 
	 * @param savePath
	 *            文件路径
	 */
	public static void checkDirs(String savePath) {
		File file = new File(savePath);
		if (!file.exists()) {
			LOG.debug("image savePath(" + savePath
					+ ") not exists, then create dirs with this path");
			file.mkdirs();
		}
	}
	
	/**
	 * 将原图路径进行加密签名
	 * @param primUri
	 * @return
	 */
	public static String getEncryptPrimUri(String primUri){
		String secCode = BaseConfig.getValue(ImgConstant.SECURE_PROP_KEY);
		
		String primSign = EncryptTools.encryptBySHA1(primUri+secCode);
		LOG.debug(LOG.isDebugEnabled() ? "primUri:" + primUri + ",secCode:" + secCode + ",primSign:" + primSign : null);
		
		return primSign;
	}
	
	/**
	 * 
	 * @param primUri 解析出原图路径
	 * @return
	 */
	public static boolean checkPrimUri(String primUri, String primSign){
		if(StringUtils.isBlank(primUri) || StringUtils.isBlank(primSign)){
			return false;
		}
		
		String secCode = BaseConfig.getValue(ImgConstant.SECURE_PROP_KEY);
		String mySign = EncryptTools.encryptBySHA1(primUri+secCode);
		
		LOG.debug(LOG.isDebugEnabled() ? "primUri:" + primUri + ",secCode:" + secCode + ",mySign:" + mySign + ",primSign:" + primSign : null);
		
		return mySign.equals(primSign);
	}
	
}
