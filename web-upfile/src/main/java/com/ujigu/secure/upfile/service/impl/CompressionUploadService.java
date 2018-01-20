package com.ujigu.secure.upfile.service.impl;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.ujigu.secure.common.utils.BeanUtils;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.service.CompositeHandler;
import com.ujigu.secure.upfile.service.ProjUploadService;
import com.ujigu.secure.upfile.util.ImageShellUtil;
import com.ujigu.secure.web.util.WebUtils;

/**
 * 
 * @author lsf
 * 
 */
public class CompressionUploadService extends ProjUploadService {

	private final static Logger LOG = LoggerFactory
			.getLogger(CompressionUploadService.class);

	private final static Map<String, CompositeHandler> compositeMap = new HashMap<String, CompositeHandler>();

	@Override
	public String getLocalFilePath(MultipartFile partFile, String prefix, String module) {
		String fileName = partFile.getOriginalFilename();
		String suffix = FilenameUtils.getExtension(fileName);

		Date currDate = new Date();
		String generationfileName = DateUtil.formatDate(currDate,
				"yyyyMMddHHmmss")
				+ new Random(System.currentTimeMillis()).nextInt(1000);

		String newFileName = generationfileName + (StringUtils.isNotBlank(suffix) ? "." + suffix : "");
		return getBaseStoreDir() + File.separator + (StringUtils.isNotBlank(prefix) ? prefix.trim() + File.separator : "") + module
				+ File.separator + DateUtil.getMonthStr(currDate)
				+ File.separator + currDate.getTime() % 1500 + File.separator
				+ newFileName;
	}

	@Override
	public RetFileInfo handleReturnImg(String localFilePath,
			String module, HttpServletRequest request) {
		RetFileInfo imgInfo = new RetFileInfo();

		imgInfo.setAccessDomain(getAccessDomain());

		int compRatio = getCompressRatio(localFilePath, module);
		//只有压缩比在28到100之间，才对图片进行压缩，否则视为无效的压缩比
		if (compRatio >= 28 && compRatio < 100) {
			String convertName = ImageShellUtil.compressionImg(localFilePath,
					compRatio); // 压缩为原来图片质量的75%

			String parentDir = FilenameUtils.getFullPath(localFilePath);
			String compFilePath = (parentDir.endsWith(File.separator) ? parentDir
					: parentDir + File.separator)
					+ convertName;
			File compFile = new File(compFilePath);
			if (compFile.exists() && convertName.contains("comp")) {
				try {
					LOG.debug(LOG.isDebugEnabled() ? "delete primative file "
							+ localFilePath : null);
					File primFile = new File(localFilePath);
					primFile.delete();
					LOG.debug(LOG.isDebugEnabled() ? "move file "
							+ compFile.getAbsolutePath() + " to file "
							+ localFilePath : null);
					FileUtils.moveFile(compFile, primFile);

				} catch (IOException ex) {
					LOG.error("localFilePath:" + localFilePath
							+ ",compFilePath:" + compFilePath +
							WebUtils.getIpAddr(request), ex);
				}
			}
		}

		if(!"gif".equalsIgnoreCase(FilenameUtils.getExtension(localFilePath))){
			// 如果存在图片合成的处理器，则先进行图片合成
			String handlerClz = getCompositeHandler(module);
			try {
				CompositeHandler compositeHandler = BeanUtils.getBean(compositeMap,
						module, handlerClz);
				if (compositeHandler != null) {
					compositeHandler.compositeImg(request, module,
							localFilePath);
				}
			} catch (Exception e) {
				LOG.error("init compositeHandler with class '" + handlerClz
						+ "' for module '" + module + "' failure" +
						WebUtils.getIpAddr(request), e);
			}
		}

		String accessUri = localFilePath.replace(getBaseStoreDir(), "");
		imgInfo.setAccessUri(accessUri);

		return imgInfo;
	}

	@Override
	protected String handleForSize(String localFilePath, String sizeStr,
			String module) {
		String convertName = null;
		String[] dimParts = sizeStr.split("x");
		if (dimParts.length == 2) {
			int width = Integer.valueOf(dimParts[0]);
			int height = Integer.valueOf(dimParts[1]);

			Dimension dim = ImageShellUtil.getDimension(localFilePath);
			if (compareWidth(dim, width) == 1
					&& compareHeight(dim, height) == 1) {
				convertName = ImageShellUtil.scaleImg(localFilePath, width,
						height);
			} else {
				convertName = fillBg(localFilePath, module, width, height);
			}

		}

		return convertName;
	}

	/**
	 * 
	 * @param dim
	 *            文件的实际尺寸对象
	 * @param scaleWidth
	 *            被缩放的宽度
	 * @return 如果文件的实际宽度大于scaleWidth，则返回1；如果相等，则返回0；否则返回-1；
	 */
	protected int compareWidth(Dimension dim, int scaleWidth) {
		if (dim == null || dim.getWidth() < scaleWidth) {
			return -1;
		}

		int fileWidth = (int) dim.getWidth();
		return fileWidth == scaleWidth ? 0 : 1;
	}

	/**
	 * 
	 * @param dim
	 *            文件的实际尺寸对象
	 * @param scaleHeight
	 *            被缩放的高度
	 * @return 如果文件的实际高度大于scaleHeight，则返回1；如果相等，则返回0；否则返回-1；
	 */
	protected int compareHeight(Dimension dim, int scaleHeight) {
		if (dim == null || dim.getHeight() < scaleHeight) {
			return -1;
		}

		int fileHeight = (int) dim.getHeight();
		return fileHeight == scaleHeight ? 0 : 1;
	}

}
