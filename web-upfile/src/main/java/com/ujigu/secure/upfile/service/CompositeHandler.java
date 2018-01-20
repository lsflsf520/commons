package com.ujigu.secure.upfile.service;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author lsf
 *
 */
public interface CompositeHandler {

	public void compositeImg(HttpServletRequest request, String module, String localFilePath);
	
}
