package com.ujigu.secure.upfile.service;

/**
 * 
 * @author lsf
 *
 */
public interface FillBgStrategy {

	/**
	 * 
	 * @param fromImg 
	 * @param width 
	 * @param height 
	 * @return 
	 */
	String fillBg(String fromImg, String module, int width, int height);
	
}
