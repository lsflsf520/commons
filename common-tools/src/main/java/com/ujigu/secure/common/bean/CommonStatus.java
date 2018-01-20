package com.ujigu.secure.common.bean;

public enum CommonStatus implements DbEnum<Byte>{
	
	NORMAL((byte)0, "正常"), 
	FREEZE((byte)-1, "已冻结"),
	INVALID((byte)-3, "已失效"),
	CLOSED((byte)-4, "已关闭"),
	DELETED((byte)-2, "已删除"),
	;

	private byte dbCode;
	private String descp;
	private CommonStatus(byte dbCode, String descp){
		this.dbCode = dbCode;
		this.descp = descp;
	}
	
	public Byte getDbCode() {
		return dbCode;
	}
	public String getDescp() {
		return descp;
	}

	public static CommonStatus getByDbCode(byte dbCode){
		for(CommonStatus state : CommonStatus.values()){
			if(state.getDbCode() == dbCode){
				return state;
			}
		}
		
		throw new IllegalArgumentException("NOT SUPPORTED PARAM");
	}
	
}
