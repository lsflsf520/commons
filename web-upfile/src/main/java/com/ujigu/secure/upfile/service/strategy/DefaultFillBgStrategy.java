package com.ujigu.secure.upfile.service.strategy;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.upfile.service.FillBgStrategy;
import com.ujigu.secure.upfile.util.ImageShellUtil;

/**
 * 
 * @author lsf
 *
 */
public class DefaultFillBgStrategy implements FillBgStrategy {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultFillBgStrategy.class);

	@Override
	public String fillBg(String fromImg, String module, int width, int height) {
		String convertName = FilenameUtils.getName(fromImg);
		String aimFile = fromImg + width
				+ ImageShellUtil.W_H_SPLITER + height + "."
				+ FilenameUtils.getExtension(fromImg);
		try {
			FileUtils.copyFile(new File(fromImg), new File(
					aimFile));

			convertName = FilenameUtils.getName(aimFile);
		} catch (IOException e) {
			LOG.error("copy file '" + fromImg + "' to file '"
					+ aimFile + "' error", e);
		}
		return convertName;
	}

}
