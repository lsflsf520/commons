package com.ujigu.secure.upfile.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.BeanUtils;
import com.ujigu.secure.common.utils.RegexUtil;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.util.ImageShellUtil;

public abstract class ProjUploadService {

	private final static Logger LOG = LoggerFactory
			.getLogger(ProjUploadService.class);

	protected final static Map<String, FillBgStrategy> fillBgMap = new HashMap<String, FillBgStrategy>();
	protected final static Map<String, SpecialSizeHandler> specialSizeHandlerMap = new HashMap<String, SpecialSizeHandler>();

	/**
	 * 
	 * @param partFile
	 *            web端文件流对象
	 * @param module
	 *            模块名
	 * @return 将文件流对象保存到服务器本地之后，返回本地文件的绝对路径
	 */
	abstract public String getLocalFilePath(MultipartFile partFile, String prefix,
			String module);

	/**
	 * 
	 * @param localFilePath
	 *            服务器文件的绝对路径
	 * @param module
	 *            模块名
	 * @param request
	 *            request对象
	 * @return 将图片做相应的处理之后，返回一个ReturnImgInfo对象
	 */
	abstract public RetFileInfo handleReturnImg(String localFilePath,
			String module, HttpServletRequest request);

	public RetFileInfo handleTooMuchFrameGIF(String localFilePath,
			String module, HttpServletRequest request){
		String frameIndexStr =  BaseConfig.getValue(module + ".temp.return.frameIndex", "0");
		
		String frameImgPath = ImageShellUtil.renameGifByFrameIndex(localFilePath, Integer.valueOf(frameIndexStr));
		
		RetFileInfo imgInfo = handleReturnImg(frameImgPath, module, request);
		
		return imgInfo;
	}
	
	/**
	 * 
	 * @param localFilePath
	 *            服务器文件的绝对路径
	 * @param sizeStr
	 *            被缩放的尺寸字符串，格式为 宽x高、宽、x高 三种格式，举例：100x200，150，x230
	 * @param module
	 *            模块名
	 * @return 将文件localFilePath按照sizeStr进行缩放后，返回缩放后的文件名
	 */
	abstract protected String handleForSize(String localFilePath,
			String sizeStr, String module);

	public void handleOtherSizes(String localFilePath, String module) {
		String[] sizeRuleArr = BaseConfig.getValueArr(module + ".sizeRule");

		if (sizeRuleArr != null && sizeRuleArr.length > 0) {
			for (String part : sizeRuleArr) {
				if (StringUtils.isNotBlank(part)) {
					handleForSize(localFilePath, part, module);
				}
			}

			String specialSizeHandlerClz = BaseConfig.getValue(module + ".specialSize.handler");
			SpecialSizeHandler specialSizeHandler = null;
			try {
				specialSizeHandler = BeanUtils.getBean(specialSizeHandlerMap,
						module, specialSizeHandlerClz);
			} catch (Exception e) {
				LOG.error("get specialSizeHandler failure for project '"
						+ module + "'", e);
			}

			if (specialSizeHandler != null) {
				specialSizeHandler
						.handleSpecialSize(localFilePath, module);
			}

		}
	}
	
	public static String getBaseStoreDir() {
		return BaseConfig.getValue("photo.local.storage.path", "/data/www/upimgs");
	}
	
	/**
	 * 获取gif图片的最大帧数，以便后边的处理
	 * @param module
	 * @return
	 */
	public int getMaxFrames(String module){
		String maxFrames = BaseConfig.getValue(module + ".gif.max.frames", "10");
		
		return Integer.valueOf(maxFrames);
	}

	/**
	 * 
	 * @param fromImg
	 * @param module
	 * @param width
	 * @param height
	 * @return
	 * @throws Exception
	 */
	protected String fillBg(String fromImg, String module, int width,
			int height) {
		String fillBgClz = BaseConfig.getValue(module + ".fillBg.strategy",
				"com.mlcs.mop.upimg.service.strategy.DefaultFillBgStrategy");

		FillBgStrategy fillBgStrategy;
		try {
			fillBgStrategy = BeanUtils.getBean(fillBgMap, module,
					fillBgClz);
			return fillBgStrategy.fillBg(fromImg, module, width, height);
		} catch (Exception e) {
			LOG.error("get fillBgStrategy for project '" + module + "'",
					e);
		}

		return FilenameUtils.getName(fromImg);
	}

	/**
	 * 
	 * @param fromImg
	 * @param module
	 * @param width
	 * @param height
	 * @param fileSuffixName 
	 * @return 如果 fileSuffixName 不为空，则需要返回以 fileSuffixName 为后缀的文件名
	 */
	protected String fillBg(String fromImg, String module, int width,
			int height, String fileSuffixName) {
		String convertName = fillBg(fromImg, module, width, height);
		if (!StringUtils.isBlank(fileSuffixName)) {
			String aimFilePath = fromImg + fileSuffixName + "."
					+ FilenameUtils.getExtension(convertName);
			String aimFileName = FilenameUtils.getName(aimFilePath);
			if (!aimFileName.equals(convertName)) {
				String convertFilePath = FilenameUtils.getFullPath(fromImg)
						+ convertName;
				try {
					File convertFile = new File(convertFilePath);
					FileUtils.copyFile(convertFile,
							new File(aimFilePath));
					convertFile.delete();
				} catch (IOException e) {
					LOG.error("fail copy '" + convertFilePath + "' to '"
							+ aimFilePath + "' for project '" + module
							+ "'", e);
				}
			}
			
			convertName = aimFileName;
		}

		return convertName;
	}

	protected String getAccessUri(String localFilePath, String convertName) {
		String parentDir = FilenameUtils.getFullPath(localFilePath);
		String convertPath = (parentDir.endsWith(File.separator) ? parentDir
				: parentDir + File.separator) + convertName;
		return convertPath.replace(getBaseStoreDir(), "");
	}

	public static String getAccessDomain() {
		return BaseConfig.getValue(
				"photo.access.domain");
	}

	protected int getCompressRatio(String localFilePath, String module) {
		String ratio = BaseConfig.getValue(module + ".img.compress.ratio", "");
		if(RegexUtil.isInt(ratio.trim())){
			return Integer.valueOf(ratio.trim());
		}
		
		File file = new File(localFilePath);
		if(file.exists() && file.isFile()){
			long bytes = file.length();
			
			long kb = bytes / 1024;
			if(kb < 100 ){
				return 100;
			} else if(kb < 200){
				return 90;
			} else if(kb < 500){
				return 80;
			} else if(kb < 1024){
				return 60;
			}
			
			return 40;
		}
		
		return 78;
	}

	protected String getShowSize(String module) {
		return BaseConfig.getValue(module + ".showSize");
	}

	protected String getCompositeHandler(String module) {
		return BaseConfig.getValue(module + ".composite.handler.class");
	}
}
