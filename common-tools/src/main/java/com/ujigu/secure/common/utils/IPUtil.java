package com.ujigu.secure.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IPUtil {

	private static final Logger LOG = LoggerFactory.getLogger(IPUtil.class);

	private static String LOCAL_IP = "";
	
    private static StringBuilder sb = new StringBuilder();
	
	private final static int INADDRSZ = 4;

	/**
	 * @return 获取本地的内网IP
	 */
	public static String getLocalIp() {

		if (StringUtils.isBlank(LOCAL_IP)) {
			try {
				for (Enumeration<NetworkInterface> ni = NetworkInterface.getNetworkInterfaces(); ni
						.hasMoreElements();) {
					NetworkInterface eth = ni.nextElement();
					for (Enumeration<InetAddress> add = eth.getInetAddresses(); add.hasMoreElements();) {
						InetAddress i = add.nextElement();
						if (i instanceof Inet4Address) {
							if (i.isSiteLocalAddress()) {
								LOCAL_IP = i.getHostAddress();

								return LOCAL_IP;
							}
						}
					}
				}
			} catch (SocketException e) {
				LOG.error("get local ip failure", e);
			}
		}
		return LOCAL_IP;
	}

	/**
	 * 
	 * @param iceConInfo
	 *            ice的连接信息
	 * @return 返回 localIP:xx.xx.xx.xx,clientIP:xx.xx.xx.xx 形式的字符串
	 */
	public static String parseIceConIP(String iceConInfo) {

		return iceConInfo.replaceAll(":", "_").replace("local address =", "localIP:")
				.replace("remote address =", "clientIP:").replace("\n", ",");
	}

	/**
	 * 
	 * @param iceConInfo
	 *            ice的连接信息
	 * @return 返回一个IP字符串数组，数组第一个元素为客户端IP；第二个元素为服务器本地IP
	 */
	public static String[] parseIceConfToIPArr(String iceConInfo) {

		return iceConInfo.replaceAll(":", "_").replace("local address =", "").replace("remote address =", "")
				.replace("\n", ",").split(",");
	}

	/**
	 * 从ip的字符串形式得到字节数组形式
	 * 
	 * @param ip
	 *            字符串形式的ip
	 * @return 字节数组形式的ip
	 */
	public static byte[] getIpByteArrayFromString(String ip) {
		byte[] ret = new byte[4];
		StringTokenizer st = new StringTokenizer(ip, ".");
		try {
			ret[0] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
			ret[1] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
			ret[2] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
			ret[3] = (byte) (Integer.parseInt(st.nextToken()) & 0xFF);
		} catch (Exception e) {
			// LogFactory.log("从ip的字符串形式得到字节数组形式报错", Level.ERROR, e);
		}
		return ret;
	}

	/**
	 * @param ip
	 *            ip的字节数组形式
	 * @return 字符串形式的ip
	 */
	public static String getIpStringFromBytes(byte[] ip) {
		sb.delete(0, sb.length());
		sb.append(ip[0] & 0xFF);
		sb.append('.');
		sb.append(ip[1] & 0xFF);
		sb.append('.');
		sb.append(ip[2] & 0xFF);
		sb.append('.');
		sb.append(ip[3] & 0xFF);
		return sb.toString();
	}
	
	public static int ip2Int(String ip){
		byte[] bytes = getIpByteArrayFromString(ip);
		
		return bytesToInt(bytes);
	}
	
	private static int bytesToInt(byte[] bytes) {
        int addr = bytes[3] & 0xFF;
        addr |= ((bytes[2] << 8) & 0xFF00);
        addr |= ((bytes[1] << 16) & 0xFF0000);
        addr |= ((bytes[0] << 24) & 0xFF000000);
        return addr;
    }
	
	/**
     * ipInt -> byte[]
     * @param ipInt
     * @return byte[]
     */
    private static byte[] intToBytes(int ipInt) {
        byte[] ipAddr = new byte[INADDRSZ];
        ipAddr[0] = (byte) ((ipInt >>> 24) & 0xFF);
        ipAddr[1] = (byte) ((ipInt >>> 16) & 0xFF);
        ipAddr[2] = (byte) ((ipInt >>> 8) & 0xFF);
        ipAddr[3] = (byte) (ipInt & 0xFF);
        return ipAddr;
    }
    
    public static String intToIP(int ipInt){
    	byte[] bytes = intToBytes(ipInt);
    	
    	return getIpStringFromBytes(bytes);
    }

	/**
	 * 根据某种编码方式将字节数组转换成字符串
	 * 
	 * @param b
	 *            字节数组
	 * @param offset
	 *            要转换的起始位置
	 * @param len
	 *            要转换的长度
	 * @param encoding
	 *            编码方式
	 * @return 如果encoding不支持，返回一个缺省编码的字符串
	 */
	public static String getString(byte[] b, int offset, int len,
			String encoding) {
		try {
			return new String(b, offset, len, encoding);
		} catch (UnsupportedEncodingException e) {
			return new String(b, offset, len);
		}
	}
	
}
