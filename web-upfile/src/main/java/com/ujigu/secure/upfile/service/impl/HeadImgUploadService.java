package com.ujigu.secure.upfile.service.impl;

import java.awt.Dimension;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.util.ImageShellUtil;
import com.ujigu.secure.upfile.util.PathUtils;
import com.ujigu.secure.web.util.WebUtils;

/**
 * 
 * @author lsf
 *
 */
public class HeadImgUploadService extends CompressionUploadService{
	
	private final static Logger LOG = LoggerFactory.getLogger(HeadImgUploadService.class);

	@Override
	public RetFileInfo handleReturnImg(String localFilePath,
			String module, HttpServletRequest request) {
		RetFileInfo imgInfo = new RetFileInfo();
		imgInfo.setAccessDomain(getAccessDomain());
		String showSizeStr = getShowSize(module);
		
		String cutpx = request.getParameter("cutpx");
		LOG.debug(LOG.isDebugEnabled() ? "recvd cutpx " + cutpx : null);
		if (StringUtils.isNotBlank(cutpx)) {
			String[] pxs = cutpx.split(",");
			File locaFile = new File(localFilePath);
			if (pxs.length < 4) {
				LOG.warn(
						"parameter cutpx should consist of four parts.",
						"cutpx:" + cutpx,
						WebUtils.getIpAddr(request));
			} else {
				final String ruleId = getFilenameByRulId(request.getParameter("ruleId"));
				String headDir = getBaseStoreDir() + File.separator + module + File.separator + parse2Dir(ruleId);
				
				String ext = FilenameUtils.getExtension(localFilePath);
				String filename = ruleId + "." + ext;
				String headImgPath = headDir + File.separator + filename;
				
				File targetFile = new File(headImgPath);
				String convertName = null;
				boolean firstScale = false;
				if(locaFile.exists()){
					double x = Double.valueOf(pxs[0]);
					double y = Double.valueOf(pxs[1]);
					double width = Double.valueOf(pxs[2]);
					double height = Double.valueOf(pxs[3]);
					Dimension dim = ImageShellUtil.getDimension(localFilePath);
					if(compareWidth(dim, (int)(x + width)) >= 0 && compareHeight(dim, (int)(y + height)) >= 0){
						convertName = ImageShellUtil.cutImg(localFilePath,
								(int)x, (int)y,
								 (int)width,  (int)height);
					}else{
						
						convertName = fillBg(localFilePath, module, (int)width, (int)height);
						
					}
					
					imgInfo.setAccessUri(getAccessUri(localFilePath, convertName));
					
					String prevPath = getBaseStoreDir() + imgInfo.getAccessUri();
					try {
						File prevFile = new File(prevPath);
						
						if(prevFile.exists() && targetFile.exists()){
							File[] files = new File(headDir).listFiles(new FilenameFilter() {
								
								@Override
								public boolean accept(File dir, String filename) {
									return ruleId.equals(filename.split("\\.")[0]);
								}
							});
							
							if(files != null && files.length > 0){
								for(File ruleIdFile : files){
									try{
									ruleIdFile.delete();
									}catch(Exception e){
										LOG.error("file '" + ruleIdFile.getAbsolutePath() + "' deleted failure" + WebUtils.getIpAddr(request), e);
									}
								}
							}
						}
						
						if(prevFile.exists()){
							PathUtils.checkDirs(headDir);
							org.apache.commons.io.FileUtils.moveFile(prevFile, targetFile);
							
							firstScale = true;
							
							locaFile.delete(); //把原图也删除
						}
					} catch (IOException e) {
						LOG.error("move file '" + prevPath + "' to target file '" + headImgPath + "' error" + WebUtils.getIpAddr(request), e);
					}
				} 
				
				if(targetFile.exists()){
					imgInfo.setAccessUri(getAccessUri(headImgPath, filename));
					
					//如果制定了showSize，则需要在裁切后的图片基础上，进一步缩放成回显尺寸
					if(showSizeStr.contains("x")){
						if(firstScale || !new File(headImgPath + showSizeStr + "." + ext).exists()){
							String[] parts = showSizeStr.split("x");
							int width = Integer.valueOf(parts[0]);
							int height = Integer.valueOf(parts[1]);
							
							convertName = ImageShellUtil.scaleImg(getBaseStoreDir() + imgInfo.getAccessUri(), width, height);
						}
						
						imgInfo.setAccessUri(getAccessUri(headImgPath,  filename + showSizeStr + "." + ext));
					}
					
				}
				
			} 
			
		}
		
		return imgInfo;
	}
	
	protected String parse2Dir(String ruleId) {
		// String proIdStr = String.valueOf(objId);
		ruleId = getFilenameByRulId(ruleId);

		String part1 = ruleId.substring(0, ruleId.length() - 6);
		String part2 = ruleId.substring(part1.length(), part1.length() + 3);
		String part3 = ruleId.substring(ruleId.length() - 3);
		// String part3 = objId.substring(objId.length() - 3);

		return part1 + File.separator + part2 + File.separator + part3;
	}
	
	protected String getFilenameByRulId(String ruleId){
		int diffNum = 8 - ruleId.length();
		if (diffNum > 0) {
			String zeroStr = "";
			for (int i = 0; i < diffNum; i++) {
				zeroStr += "0";
			}

			ruleId = zeroStr + ruleId;
		}
		
		return ruleId;
	}
	
}
