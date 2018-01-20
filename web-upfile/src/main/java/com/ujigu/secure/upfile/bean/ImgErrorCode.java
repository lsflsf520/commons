package com.ujigu.secure.upfile.bean;

/**
 * 
 * @author lsf
 *
 */
public enum ImgErrorCode {

	SUCCESS("成功"),
	NO_FILE_ELEM("没有可用的文件域"),
	NO_SUCH_MODULE("不支持的模块名"),
	ORIGIN_FILENAME_ILLEGAL("非法的文件名"),
	NOT_SUPPORT("不支持的文件格式"),
	SAVE_IO_EXCEPTION("保存文件到本地时发生异常"),
	SECURE_CHECK_NO_PASS("安全校验不通过"),
	ILLEGAL_RULE_ID("参数ruleId不能为空"),
	HANDLER_INSTANCE_ERROR("图片处理类实例化错误"),
	NO_SUCH_FONT_FILE("字体文件不存在"),
	IMG_COMPOSITE_ERROR("图片合成失败"),
	PROHIBITED_IMG("图片为违禁图片"),
	
	;
	
	private String msg;
	
	private ImgErrorCode(String msg){
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}
	
}
