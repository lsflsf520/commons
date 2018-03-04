package com.xyz.tools.common.utils;

public class IDCardUtil {
	
	final static int[] wi = {7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2,1};
	final static String[] vi = {"1","0","X","9","8","7","6","5","4","3","2"};
	private static int[] ai = new int[18];
	
	/**
	 * 获取第18位校验码
	 * @param eighteenCardId
	 * @return
	 */
	public static String getVerify(String eighteenCardId) throws Exception{
		int remaining = 0;
		if(eighteenCardId.length() == 18){
			eighteenCardId = eighteenCardId.substring(0,17);
		}
		if(eighteenCardId.length() == 17){
			int sum = 0;
			for(int i = 0 ; i < 17 ; i++){
				String k = eighteenCardId.substring(i,i+1);
				ai[i] = Integer.parseInt(k);
			}
			for(int i = 0 ; i < 17 ; i++){
				sum = sum+wi[i]*ai[i];
			}
			remaining = sum%11;
		}
		return vi[remaining];
	}
	
	/**
	 * 将15位身份证号码转成18位
	 * @param fifteenCardId
	 * @return
	 */
	public static String upToEighteen(String fifteenCardId) throws Exception{
		String eighteenCardId = fifteenCardId.substring(0,6);
		eighteenCardId = eighteenCardId + "19";
		eighteenCardId = eighteenCardId + fifteenCardId.substring(6,15);
		eighteenCardId = eighteenCardId + getVerify(eighteenCardId);
		return eighteenCardId;
	}
	
	/**
	 * 判断身份证号码是否合法
	 * @param cardId
	 * @return
	 */
	public static boolean verify(String cardId){
		try {
			if(cardId.length() == 15){
				cardId = upToEighteen(cardId);
			}
			if(cardId.length() != 18){
				return false;
			}
			String verify = cardId.substring(17,18);
			if(verify.equalsIgnoreCase(getVerify(cardId))){
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 填17位数字得到身份证号码,用于测试
	 * @param args
	 */
	public static void main(String[] args) {
		String str = "43042120140712432";
		try {
			System.out.println(str.substring(12));
			System.out.println(verify(str));
			str = str+getVerify(str);
			System.out.println(str);
			str = str.substring(6, 14);
			System.out.println("birthday:"+str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
