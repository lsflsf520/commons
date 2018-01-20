package com.ujigu.secure.upfile.service.handler;

import java.awt.Dimension;
import java.io.File;

import org.apache.commons.io.FilenameUtils;

import com.ujigu.secure.upfile.service.SpecialSizeHandler;
import com.ujigu.secure.upfile.util.ImageShellUtil;

/**
 * 
 * @author lsf
 *
 */
public class CutImgSizeHandler extends SpecialSizeHandler {
	
	@Override
	protected void handleSpecialSize(String localFilePath, String module,
			String sizeStr) {
		String[] dimParts = sizeStr.split("x");
		if (dimParts.length == 2) {
			int width = Integer.valueOf(dimParts[0]);
			int height = Integer.valueOf(dimParts[1]);

			Dimension dim = ImageShellUtil.getDimension(localFilePath);
			double imgRatio = dim.getWidth() / dim.getHeight();
			double aimRatio = width / (height * 1.0);
			String baseDir = FilenameUtils.getFullPath(localFilePath);
			String aimFilePath = baseDir + FilenameUtils.getName(localFilePath) + width + ImageShellUtil.W_H_SPLITER + height + "." + FilenameUtils.getExtension(localFilePath);
			
			int scaleWidth = width;
			int scaleHeight = height;
			if(imgRatio > aimRatio){
				scaleWidth = (int)((height / dim.getHeight()) * dim.getWidth());
			} else if (imgRatio < aimRatio){
				scaleHeight = (int)((width / dim.getWidth()) * dim.getHeight());
			} 
			
			String convertName = ImageShellUtil.scaleImg(localFilePath, scaleWidth, scaleHeight);
			String baseImgPath = baseDir + convertName;
			
			if(width != scaleWidth || height != scaleHeight){
				ImageShellUtil.cutImg(baseImgPath, aimFilePath, 0, 0, width, height);
				
				new File(baseImgPath).delete();
			}
		}

	}
	
//	protected String getSafeBaseFilePath(String localFilePath, String sizeStr){
//		String baseImgPath = localFilePath;
//		String ext = FilenameUtils.getExtension(localFilePath);
//		File checkFile = new File(localFilePath + sizeStr + "." + ext);
//		if(checkFile.exists()){
//			baseImgPath += "bs." + ext;
//			try {
//				FileUtils.copyFile(new File(localFilePath), new File(baseImgPath));
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return baseImgPath;
//	}

}
