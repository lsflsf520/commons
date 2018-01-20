package com.ujigu.secure.web.constant;

public enum ThirdChannel {
	//注意，各枚举值的首字母必须不一样
	qq, wx, sina, alipay, csai;
	
	public String getShort(){
		return this.name().substring(0, 1);
	}

	public static ThirdChannel getByShort(String shortName){
		ThirdChannel[] channels = ThirdChannel.values();
		for(ThirdChannel channel : channels){
			if(channel.name().startsWith(shortName)){
				return channel;
			}
		}
		
		return null;
	}
}
