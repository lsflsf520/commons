package com.ujigu.secure.upfile.service;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.upfile.bean.RetFileInfo;

/**
 * 针对不同的上传组件，可能对返回格式有一定的要求，可以实现这个接口来对返回值做一些处理
 * @author lsf
 *
 */
public interface RetInfoHandler {

	/**
	 * 
	 * @param errorCode 错误代码
	 * @param imgInfo 返回的图片对象
	 * @return
	 */
	void buildRetInfo(ResultModel resultModel, RetFileInfo imgInfo);
	
}
