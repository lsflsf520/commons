package com.xyz.tools.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

/**
** @author Administrator
** @version 2017年7月20日下午6:02:26
** @Description
*/
public class SignUtil {
	
	/**
	 * @param text 需要签名的字符串
	 * @param sign 签名结果
	 * @param key	秘钥
	 * @param input_charset 编码格式
	 * @return
	 * @Decription 验证签名
	 * @Author Administrator
	 * @Time 2017年7月20日下午6:07:58
	 * @Exception
	 */
	public static boolean verify(String text, String sign, String key, String input_charset) {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
       return mysign.equals(sign);
    }
	
	/**
	 * @param content
	 * @param charset
	 * @return
	 * @Decription 根据编码格式获取字节
	 * @Author Administrator
	 * @Time 2017年7月20日下午6:09:33
	 * @Exception
	 */
	private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("获取字节,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
	
	/**
	 * @param text 需要签名的字符串
	 * @param key	秘钥
	 * @param input_charset 编码格式
	 * @return
	 * @Decription 签名字符串
	 * @Author Administrator
	 * @Time 2017年7月20日下午6:10:07
	 * @Exception
	 */
	public static String sign(String text, String key, String input_charset) {
        text = text + key;
        return DigestUtils.md5Hex(getContentBytes(text, input_charset));
    }
	
	/**
	 * @param params
	 * @return
	 * @Decription 拼接参数
	 * @Author Administrator
	 * @Time 2017年7月20日下午6:02:54
	 * @Exception
	 */
	public static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);
        String prestr = "";
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            if (i == keys.size() - 1) { //拼接时，不包括最后一个&字符
                prestr = prestr + key + "=" + value;
            } else {
                prestr = prestr + key + "=" + value + "&";
            }
        }
        return prestr;
    }
	
	/**
	 * @param sArray
	 * @return
	 * @Decription 过滤参数，键值为空的不参与拼接，签名sign参数不参与拼接
	 * @Author Administrator
	 * @Time 2017年7月20日下午6:04:50
	 * @Exception
	 */
	public static Map<String, String> paraFilter(Map<String, String> sArray) {
        Map<String, String> result = new HashMap<String, String>();
        if (sArray == null || sArray.size() <= 0) {
            return result;
        }
        for (String key : sArray.keySet()) {
            if (key == null || key.equals("") || key.equalsIgnoreCase("sign")) {
                continue;
            }
            String value = sArray.get(key);
            if (value == null || value.equals("")) {
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

}
