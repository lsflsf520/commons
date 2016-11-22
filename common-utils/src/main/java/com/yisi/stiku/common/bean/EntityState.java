package com.yisi.stiku.common.bean;

public enum EntityState implements DbEnum<Byte>{
	
	NORMAL((byte)0, "正常"), 
	INVALID((byte)-1, "数据已作废，不可再次恢复"),
	FREEZE((byte)-2, "已冻结，数据还可以再次恢复")
	;

	private byte dbCode;
	private String desc;
	private EntityState(byte dbCode, String desc){
		this.dbCode = dbCode;
		this.desc = desc;
	}
	
	public Byte getDbCode() {
		return dbCode;
	}
	public String getDesc() {
		return desc;
	}

//	@Override
//	public EntityState[] getValues() {
//		return EntityState.values();
//	}
	
	public static EntityState getByDbCode(byte dbCode){
		for(EntityState state : EntityState.values()){
			if(state.getDbCode() == dbCode){
				return state;
			}
		}
		
		throw new IllegalArgumentException("NOT SUPPORTED PARAM");
	}
	
}
