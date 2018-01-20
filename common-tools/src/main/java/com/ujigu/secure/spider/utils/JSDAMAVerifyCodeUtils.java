/**   
* @Title: JSDAMAVerifyCodeUtils.java 
* @Package com.ujigu.secure.spider.utils 
* @Description: TODO(用一句话描述该文件做什么) 
* @author A18ccms A18ccms_gmail_com   
* @date 2017年9月7日 上午9:55:36 
* @version V1.0   
*/
package com.ujigu.secure.spider.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.codec.binary.Base64;

import com.ujigu.secure.common.utils.ImageUtils;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;

/**
 * @author user
 *
 */
public class JSDAMAVerifyCodeUtils {
	
	public static final String str = "http://v1-http-api.jsdama.com/api.php?mod=php&act=upload";
	public static final String user_name = "csaibx";
	public static final String user_pw = "Csaibx2016";
	public static final String yzm_minlen = "4";
	public static final String yzm_maxlen = "4";
	public static final  String yzmtype_mark = "1001";
	public static final String zztool_token = "";
	
	
	/**
	 * 
	* @Title: getVerifyCode 
	* @Description: 获取验证码
	* @param @return
	* @return String 返回类型
	* @throws
	 */
	public static String getVerifyCode(String base64){
		//Base64解码
		byte[] b = Base64.decodeBase64(base64);
		for (int i = 0; i < b.length; ++i) {
			if (b[i] < 0) {// 调整异常数据
				b[i] += 256;
			}
		}
		InputStream input = new ByteArrayInputStream(b);
		return getVerifyCode(input);
	}
	
	
	/**
	 * 
	* @Title: getVerifyCode 
	* @Description: 获取验证码
	* @param @return
	* @return String 返回类型
	* @throws
	 */
	public static String getVerifyCode(InputStream input){
		String result = "";
		String BOUNDARY = "---------------------------68163001211748"; // boundary就是request头和上传文件内容的分隔符
		
		Map<String, String> paramMap = getParamMap();
		try {
			URL url = new URL(str);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("content-type", "multipart/form-data; boundary=" + BOUNDARY);
			connection.setConnectTimeout(30000);
			connection.setReadTimeout(30000);

			OutputStream out = new DataOutputStream(connection.getOutputStream());
			// 普通参数
			if (paramMap != null) {
				StringBuffer strBuf = new StringBuffer();
				Iterator<Entry<String, String>> iter = paramMap.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry<String, String> entry = iter.next();
					String inputName = entry.getKey();
					String inputValue = entry.getValue();
					strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
					strBuf.append("Content-Disposition: form-data; name=\"" + inputName + "\"\r\n\r\n");
					strBuf.append(inputValue);
				}
				out.write(strBuf.toString().getBytes());
			}

			
			String contentType = "image/jpeg";//这里看情况设置
			StringBuffer strBuf = new StringBuffer();
			strBuf.append("\r\n").append("--").append(BOUNDARY).append("\r\n");
			strBuf.append("Content-Disposition: form-data; name=\"" + "upload" + "\"; filename=\"" + Math.random()+ "\"\r\n");
			strBuf.append("Content-Type:" + contentType + "\r\n\r\n");
			out.write(strBuf.toString().getBytes());
			DataInputStream dataInputStream = new DataInputStream(input);
			int bytes = 0;
			byte[] bufferOut = new byte[1024];
			while ((bytes = dataInputStream.read(bufferOut)) != -1) {
				out.write(bufferOut, 0, bytes);
			}
			dataInputStream.close();
			
			byte[] endData = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(endData);
			out.flush();
			out.close();

			//读取URLConnection的响应
			InputStream in = connection.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			while (true) {
				int rc = in.read(buf);
				if (rc <= 0) {
					break;
				} else {
					bout.write(buf, 0, rc);
				}
			}
			in.close();
			//结果输出
			result = new String(bout.toByteArray());
			HashMap map = JsonUtil.create().fromJson(result, HashMap.class);
			if((Boolean)map.get("result")){
				Map<String, String> data = (Map<String, String>) map.get("data");
				result = data.get("val");
				LogUtils.info("成功识别验证码：%s", result);
			}else{
				LogUtils.info("验证码识别失败！%s", result);
				result = "";
			}
		} catch (Exception e) {
			LogUtils.error("验证码识别失败！", e);
		}
		return result.trim();
	}

	/**
	 * 参数信息
	 * 
	 * @return
	 */
	private static Map<String, String> getParamMap() {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("user_name", user_name);
		paramMap.put("user_pw", user_pw);
		paramMap.put("yzm_minlen", yzm_minlen);
		paramMap.put("yzm_maxlen", yzm_maxlen);
		paramMap.put("yzmtype_mark", yzmtype_mark);
		paramMap.put("zztool_token", zztool_token);
		return paramMap;
	}
	
	
	public static void main(String[] args) {
		String path = "C:\\Users\\user\\Desktop\\image\\getCaptchaImage.jpg";
		//System.out.println(ImageUtils.getImageStr(path));
		String result = JSDAMAVerifyCodeUtils.getVerifyCode(ImageUtils.getImageStr(path));
		System.out.println(result);
	}
}
