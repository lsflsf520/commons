package com.xyz.tools.common.utils;



import java.security.DigestException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Base64Utils;

public final class EncryptTools {

	/**
	 * 
	 * @param strSrc 需要被加密的字符串
	 * @param encName 加密方式，有 MD5、SHA-1和SHA-256 这三种加密方式
	 * @return 返回加密后的字符串
	 */
	private static String encryptStr(String strSrc, String encName) {
		// parameter strSrc is a string will be encrypted,
		// parameter encName is the algorithm name will be used.
		// encName dafault to "MD5"
		MessageDigest md = null;
		String strDes = null;

		byte[] bt = strSrc.getBytes();
		try {
			if (encName == null || encName.equals("")) {
				encName = "MD5";
			}
			md = MessageDigest.getInstance(encName);
			md.update(bt);
			strDes = bytes2Hex(md.digest()); // to HexString
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Invalid algorithm.");
			return null;
		}
		return strDes;
	}

	/**
	 * 
	 * @param str 需要被加密的字符串
	 * @return 对字符串str进行MD5加密后，将加密字符串返回
	 * 
	 */
	public static String encryptByMD5(String str) {
		return encryptStr(str, "MD5");
	}
	
	/**
	 * 该方法主要用于验证学生端的md5密码
	 * 
	 * @param s 
	 *       the string want to encode
	 * @return
	 *       the encoded String
	 */
	public static String to_MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 
	 * @param str 需要被加密的字符串
	 * @return 对字符串str进行SHA-1加密后，将加密字符串返回
	 * 
	 */
	public static String encryptBySHA1(String str) {
		return encryptStr(str, "SHA-1");
	}

	/**
	 * 
	 * @param str 需要被加密的字符串
	 * @return 对字符串str进行SHA-256加密后，将加密字符串返回
	 * 
	 */
	public static String encryptBySHA256(String str) {
		return encryptStr(str, "SHA-256");
	}

	/**
	 * 
	 * @param bts
	 * @return
	 */
	private static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}
	
	/**
	 * 
	 * @param str
	 * @param key
	 * @return
	 */
	public static String union(String str,String key) {
        int strLen = str.length();
        int keyLen = key.length();
        Character[] s = new Character[strLen+keyLen];

        boolean flag= true;
        int strIdx=0;
        int keyIdx=0;
        for(int i=0;i<s.length;i++){
            if(flag){
                if(strIdx<strLen){
                    s[i] = str.charAt(strIdx);
                    strIdx++;
                }
                if(keyIdx<keyLen){
                    flag=false;
                }

            }else{
                if(keyIdx<keyLen){
                    s[i]=key.charAt(keyIdx);
                    keyIdx++;
                }
                if(strIdx<strLen){
                    flag=true;
                }

            }
        }
        return StringUtils.join(s);
    }
	
	 /**
     *  加密str
     *
     * @param str 要加密的字符串
     * @param key 加密的key
     * @return
     */
    public static String encrypt(String str,String key){

        if( str==null || str.length()==0 || StringUtils.isBlank(key)){
            return encrypt(str);
        }

        return encrypt(union(str, key));

    }




    /**
     * 先将str进行一次MD5加密，加密后再取加密后的字符串的第1、3、5个字符追加到加密串，再拿这个加密串进行加密
     * @param str
     * @return
     * @throws NoSuchAlgorithmException
     * @throws DigestException
     */
    public static String encrypt(String str)   {
        String encryptStr = encryptByMD5(str);
        if(encryptStr!=null ){
            encryptStr = encryptStr + encryptStr.charAt(0)+encryptStr.charAt(2)+encryptStr.charAt(4);
            encryptStr = encryptByMD5(encryptStr);
        }
        return encryptStr;
    }

//	public static void main(String[] args) {
//		String strSrc = "可以加密汉字.Oh,and english";
//		System.out.println("Source String:" + strSrc);
//		System.out.println("Encrypted String:");
//		System.out.println("Use Def:" + EncryptTools.Encrypt(strSrc, null));
//		System.out.println("Use MD5:" + EncryptTools.Encrypt(strSrc, "MD5"));
//		System.out.println("Use SHA:" + EncryptTools.Encrypt(strSrc, "SHA-1"));
//		System.out.println("Use SHA-256:" + EncryptTools.Encrypt(strSrc, "SHA-256"));
//	}
    
  //加密
    public static String encryptByBase64 (String data,String key) {
    	byte[] bt=encryptByBase64(data.getBytes(),key.getBytes());
    	String strs = Base64.encodeBase64String(bt);
        return strs;
    }
    //解密
    public static String decryptByBase64(String data,String key) {
        if(data==null)
        return null;
        
        byte[] buf = Base64.decodeBase64(data);
        byte[] bt = decryptByBase64(buf,key.getBytes());
        return new String(bt);
    }
     
    //加密方法
    public static byte[] encryptByBase64(byte[] data,byte[] key) {
        DESKeySpec dks = null;
        Cipher cipher = null;
        byte[] result = null;
		try {
			dks = new DESKeySpec(key);
			SecretKeyFactory keyFactory=SecretKeyFactory.getInstance("DES");
	        SecretKey secretkey=keyFactory.generateSecret(dks);
	        cipher=Cipher.getInstance("DES/CBC/PKCS5Padding");
	        IvParameterSpec iv = new IvParameterSpec(key);
	        cipher.init(Cipher.ENCRYPT_MODE, secretkey,iv);
	        
	        result = cipher.doFinal(data);
		} catch (Exception e) {
			LogUtils.warn("加密参数有误：加密字符串为：%s， clientId：%s", e,new String(data), new String(key));
		}
        
        return result;
    }
     
    //解密方法
    public static byte[] decryptByBase64(byte[] data,byte[] key) {
        DESKeySpec dks = null;
        Cipher cipher = null;
        byte[] result = null;
		try {
			dks =new DESKeySpec(key);
			SecretKeyFactory keyFactory=SecretKeyFactory.getInstance("DES");
	        SecretKey secretkey=keyFactory.generateSecret(dks);
	        cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
	        IvParameterSpec iv = new IvParameterSpec(key);
	        cipher.init(Cipher.DECRYPT_MODE, secretkey,iv);
	        
	        result = cipher.doFinal(data);
		} catch (Exception e) {
			LogUtils.warn("解密参数有误：加密字符串为：%s， clientId：%s", e,new String(data), new String(key));
		}
		return result;
    }
    
    //AES加密
    public static String encryptByAES (String data,String key) {
    	byte[] bt = encryptByAES(data.getBytes(),key.getBytes());
    	String strs = Base64.encodeBase64String(bt);
        return strs;
    }
    //AES解密
    public static String decryptByAES(String data,String key) {
        if(data==null)
        return null;
        
        byte[] bt = decryptByAES(Base64Utils.decodeFromString(data), key.getBytes());
        return new String(bt);
    }
    
    //AES加密
    public static byte[] encryptByAES(byte[] content,byte[] keyBytes) {  
        byte[] encryptedText=null;  
        KeyGenerator keyGen = null;  
        Cipher cipher = null;
        try {  
        	keyGen=KeyGenerator.getInstance("AES");
        	keyGen.init(128);  
        	cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
        	
        	Key key=new SecretKeySpec(keyBytes,"AES");  
            cipher.init(Cipher.ENCRYPT_MODE, key); 
              
            encryptedText=cipher.doFinal(content);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return encryptedText;  
    }  
      
    //解密为byte[]  
    public static byte[] decryptByAES(byte[] content,byte[] keyBytes) {  
        byte[] originBytes=null;  
        KeyGenerator keyGen = null;  
        Cipher cipher = null;
        try {  
        	keyGen=KeyGenerator.getInstance("AES");
        	keyGen.init(128);  
        	cipher=Cipher.getInstance("AES/ECB/PKCS5Padding");
        	
        	Key key=new SecretKeySpec(keyBytes,"AES"); 
        	cipher.init(Cipher.DECRYPT_MODE, key);
        	
            originBytes=cipher.doFinal(content);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }
          
        return originBytes;  
    }
}
