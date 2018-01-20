package com.ujigu.secure.upfile.service.impl;

import java.awt.Dimension;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.upfile.bean.RetFileInfo;
import com.ujigu.secure.upfile.util.ImageShellUtil;
import com.ujigu.secure.web.util.WebUtils;

/**
 * 
 * @author lsf
 *
 */
public class CutUploadService extends CompressionUploadService{
	
	private final static Logger LOG = LoggerFactory.getLogger(CutUploadService.class);

	@Override
	public RetFileInfo handleReturnImg(String localFilePath,
			String module, HttpServletRequest request) {
		RetFileInfo imgInfo = super.handleReturnImg(localFilePath, module, request);
		String showSizeStr = BaseConfig.getValue(module + ".showSize");
		
		String cutpx = request.getParameter("cutpx");
		LOG.debug(LOG.isDebugEnabled() ? "recvd cutpx " + cutpx : null);
		if (StringUtils.isNotBlank(cutpx)) {
			String[] pxs = cutpx.split(",");
			if (pxs.length < 4) {
				LOG.warn(
						"parameter cutpx should consist of four parts.",
						"cutpx:" + cutpx,
						WebUtils.getIpAddr(request));
			} else {
				String convertName = FilenameUtils.getName(localFilePath);
				
				double x = Double.valueOf(pxs[0]);
				double y = Double.valueOf(pxs[1]);
				double width = Double.valueOf(pxs[2]);
				double height = Double.valueOf(pxs[3]);
				
				Dimension dim = ImageShellUtil.getDimension(localFilePath);
				if(compareWidth(dim, (int)(x + width)) >= 0 && compareHeight(dim, (int)(y + height)) >= 0){
					convertName = ImageShellUtil.cutImg(localFilePath,
							Double.valueOf(pxs[0]).intValue(), Double.valueOf(pxs[1]).intValue(),
							Double.valueOf(pxs[2]).intValue(), Double.valueOf(pxs[3]).intValue());
					
					imgInfo.setAccessUri(getAccessUri(localFilePath, convertName));
				}
				
				//如果制定了showSize，则需要在裁切后的图片基础上，进一步缩放成回显尺寸
				if(showSizeStr.contains("x")){
					String[] parts = showSizeStr.split("x");
					int showWidth = Integer.valueOf(parts[0]);
					int showHeight = Integer.valueOf(parts[1]);
					
					convertName = ImageShellUtil.scaleImg(getBaseStoreDir() + imgInfo.getAccessUri(), showWidth, showHeight);
					
					imgInfo.setAccessUri(getAccessUri(localFilePath, convertName));
				}
				
			}
			
		}
		
		return imgInfo;
	}
	
}
