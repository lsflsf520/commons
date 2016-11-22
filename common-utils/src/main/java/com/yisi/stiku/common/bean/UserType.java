package com.yisi.stiku.common.bean;

/**
 * 
 * @author shangfeng
 *
 */
public class UserType {

	public final static int SUPER_ADMIN = 1; //超级管理员
	public final static int TEACHER = 3; //老师
	public final static int AGENT = 4; //代理
	public final static int STUDENT = 5; //学生，由工作人员通过后台导入的学生
	public final static int REG_STUDENT = 8; //自己注册的学生
	public final static int ZIZHU_PRINT_STUDENT = 9; //有自助打印功能的学生类型
	public final static int ADMIN = 6; //管理员
	public final static int COACH = 7; //学习教练
	public final static int JIAOYAN = 10; //教研
	
	
//	SYS_ADMIN((byte)0, "系统管理员"),
//	STUDENT((byte)1, "学生"), 
//	TEACHER((byte)2, "老师"),
//	JIAOYAN((byte)3, "教研"),
//	AGENT((byte)4, "代理商"),
//	SALOR((byte)5, "销售"),
//	FINANCE((byte)6, "财务"),
//	PRINTER((byte)7, "打印员"),
//	;
//
//	private byte dbCode;
//	private String desc;
//	private UserType(byte dbCode, String desc){
//		this.dbCode = dbCode;
//		this.desc = desc;
//	}
//	
//	public byte getDbCode() {
//		return dbCode;
//	}
//	public String getDesc() {
//		return desc;
//	}
//	
//	public static UserType getByDbCode(byte dbCode){
//		for(UserType state : UserType.values()){
//			if(state.getDbCode() == dbCode){
//				return state;
//			}
//		}
//		
//		throw new IllegalArgumentException("NOT SUPPORTED PARAM");
//	}
	
}
