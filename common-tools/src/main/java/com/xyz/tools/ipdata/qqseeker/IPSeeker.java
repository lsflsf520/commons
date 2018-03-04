package com.xyz.tools.ipdata.qqseeker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xyz.tools.common.utils.BaseConfig;

public class IPSeeker {

	private final static Logger LOG = LoggerFactory.getLogger(IPSeeker.class);
	
	private RandomAccessFile ipFile;

	// 起始地区的开始和结束的绝对偏移
	private long ipBegin, ipEnd;

	// 用来做为cache，查询一个ip时首先查看cache，以减少不必要的重复查找
	private Map<String, IPLocation> ipCache;

	// 一些固定常量，比如记录长度等等
	private static final int IP_RECORD_LENGTH = 7;
	private static final byte REDIRECT_MODE_1 = 0x01;
	private static final byte REDIRECT_MODE_2 = 0x02;
	
	private static IPSeeker seeker;

	private IPSeeker(String ipfilePath) {
		try {
			ipFile = new RandomAccessFile(ipfilePath, "r");
		} catch (FileNotFoundException e) {
			LOG.error("access to file " + ipfilePath + " error. errorMsg:" + e.getMessage(), e);
		}
		// 如果打开文件成功，读取文件头信息
		if (ipFile != null) {
			try {
				ipBegin = readLong4(0);
				ipEnd = readLong4(4);
				if (ipBegin == -1 || ipEnd == -1) {
					ipFile.close();
					ipFile = null;
				}

				ipCache = new HashMap<String, IPLocation>();
			} catch (IOException e) {
				// LogFactory.log("IP地址信息文件格式有错误，IP显示功能将无法使用",Level.ERROR,e);
				ipFile = null;
			}
		}
	}
	
	public static IPSeeker getInstance(){
		if(seeker == null){
			synchronized (IPSeeker.class) {
				if(seeker == null){
					seeker = new IPSeeker(Message.DATA_BASE_DIR + (Message.DATA_BASE_DIR.endsWith("/") ? "" : "/") +"qqwry.dat");
				}
			}
		}
		
		return seeker;
	}

	/**
	 * 根据IP得到国家名
	 * 
	 * @param ip
	 *            IP的字符串形式
	 * @return 国家名字符串
	 */
	public IPLocation getAddr(String ip) {
		IPLocation loc = getAddr(Util.getIpByteArrayFromString(ip));
		if(loc != null){
			String[] unitInfo = LexiconUtil.getUnitInfo(loc.getDetail());
			if((StringUtils.isBlank(loc.getCity()) || LexiconUtil.isUnknown(loc.getCity())) && unitInfo != null && unitInfo.length == 2){
				loc.setCity(unitInfo[0]);
			}
			if((StringUtils.isBlank(loc.getProvince()) || LexiconUtil.isUnknown(loc.getProvince())) && unitInfo != null && unitInfo.length == 2){
				loc.setProvince(unitInfo[1]);
			}
			if(LexiconUtil.isUnknown(loc.getCountry()) && LexiconUtil.isProvince(loc.getProvince())){
				loc.setCountry("中国");
			}
		}
		return loc;
	}

	/**
	 * 根据IP得到国家名
	 * 
	 * @param ip
	 *            ip的字节数组形式
	 * @return 国家名字符串
	 */
	private IPLocation getAddr(byte[] ip) {
		// 检查ip地址文件是否正常
		if (ipFile == null){
			IPLocation info = new IPLocation(LexiconUtil.UNKNOWN, LexiconUtil.UNKNOWN,
					LexiconUtil.UNKNOWN, LexiconUtil.UNKNOWN);
			info.setArea(LexiconUtil.UNKNOWN);
			
			return info;
		}
		
		// 保存ip，转换ip字节数组为字符串形式
		String ipStr = Util.getIpStringFromBytes(ip);
		// 先检查cache中是否已经包含有这个ip的结果，没有再搜索文件
		if (ipCache.containsKey(ipStr)) {
			return ipCache.get(ipStr);
		}

		IPLocation ipLoc = getIPLocation(ip);
		ipCache.put(ipStr, ipLoc);

		return ipLoc;
	}

	/**
	 * 根据ip搜索ip信息文件，得到IPLocation结构，所搜索的ip参数从类成员ip中得到
	 * 
	 * @param ip
	 *            要查询的IP
	 * @return IPLocation结构
	 */
	private IPLocation getIPLocation(byte[] ip) {
		IPLocation info = null;
		long offset = locateIP(ip);
		if (offset != -1) {
			info = getIPLocation(offset);
		}

		if (info == null) {
			info = new IPLocation(LexiconUtil.UNKNOWN, LexiconUtil.UNKNOWN,
					LexiconUtil.UNKNOWN, LexiconUtil.UNKNOWN);
			info.setArea(LexiconUtil.UNKNOWN);
		}
		return info;
	}

	/**
	 * 根据IP得到地区名
	 * 
	 * @param ip
	 *            ip的字节数组形式
	 * @return 地区名字符串
	 */
	// private String getArea(byte[] ip) {
	// // 检查ip地址文件是否正常
	// if (ipFile == null) {
	// return Message.bad_ip_file;
	// }
	//
	// // 保存ip，转换ip字节数组为字符串形式
	// String ipStr = Util.getIpStringFromBytes(ip);
	// // 先检查cache中是否已经包含有这个ip的结果，没有再搜索文件
	// if (ipCache.containsKey(ipStr)) {
	// IPLocation ipLoc = ipCache.get(ipStr);
	// return ipLoc.getArea();
	// } else {
	// IPLocation ipLoc = getIPLocation(ip);
	// ipCache.put(ipStr, ipLoc.getCopy());
	// return ipLoc.getArea();
	// }
	// }

	/**
	 * 根据IP得到地区名
	 * 
	 * @param ip
	 *            IP的字符串形式
	 * @return 地区名字符串
	 */
	// private String getArea(String ip) {
	// if (StringUtils.isEmpty(ip)) {
	// return Message.bad_ip_addr;
	// }
	// return getArea(Util.getIpByteArrayFromString(ip));
	// }

	/**
	 * 从offset位置读取4个字节为一个long，因为java为big-endian格式，所以没办法 用了这么一个函数来做转换
	 * 
	 * @param offset
	 * @return 读取的long值，返回-1表示读取文件失败
	 */
	private long readLong4(long offset) {
		long ret = 0;
		try {
			ipFile.seek(offset);
			ret |= (ipFile.readByte() & 0xFF);
			ret |= ((ipFile.readByte() << 8) & 0xFF00);
			ret |= ((ipFile.readByte() << 16) & 0xFF0000);
			ret |= ((ipFile.readByte() << 24) & 0xFF000000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	/**
	 * 这个方法将根据ip的内容，定位到包含这个ip国家地区的记录处，返回一个绝对偏移 方法使用二分法查找。
	 * 
	 * @param ip
	 *            要查询的IP
	 * @return 如果找到了，返回结束IP的偏移，如果没有找到，返回-1
	 */
	private long locateIP(byte[] ip) {
		long m = 0;
		int r;
		byte[] b4 = new byte[4];
		// 比较第一个ip项
		readIP(ipBegin, b4);
		r = compareIP(ip, b4);
		if (r == 0)
			return ipBegin;
		else if (r < 0)
			return -1;
		// 开始二分搜索
		for (long i = ipBegin, j = ipEnd; i < j;) {
			m = getMiddleOffset(i, j);
			readIP(m, b4);
			r = compareIP(ip, b4);
			// log.debug(Utils.getIpStringFromBytes(b));
			if (r > 0)
				i = m;
			else if (r < 0) {
				if (m == j) {
					j -= IP_RECORD_LENGTH;
					m = j;
				} else
					j = m;
			} else
				return readLong3(m + 4);
		}
		// 如果循环结束了，那么i和j必定是相等的，这个记录为最可能的记录，但是并非
		// 肯定就是，还要检查一下，如果是，就返回结束地址区的绝对偏移
		m = readLong3(m + 4);
		readIP(m, b4);
		r = compareIP(ip, b4);
		if (r <= 0){
			return m;
		}
		
		return -1;
	}

	/**
	 * 得到begin偏移和end偏移中间位置记录的偏移
	 * 
	 * @param begin
	 * @param end
	 * @return
	 */
	private long getMiddleOffset(long begin, long end) {
		long records = (end - begin) / IP_RECORD_LENGTH;
		records >>= 1;
		if (records == 0){
			records = 1;
		}
		return begin + records * IP_RECORD_LENGTH;
	}

	/**
	 * 从offset位置读取3个字节为一个long，因为java为big-endian格式，所以没办法 用了这么一个函数来做转换
	 * 
	 * @param offset
	 *            整数的起始偏移
	 * @return 读取的long值，返回-1表示读取文件失败
	 */
	private long readLong3(long offset) {
		long ret = 0;
		byte[] b3 = new byte[3];
		try {
			ipFile.seek(offset);
			ipFile.readFully(b3);
			ret |= (b3[0] & 0xFF);
			ret |= ((b3[1] << 8) & 0xFF00);
			ret |= ((b3[2] << 16) & 0xFF0000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	/**
	 * 从当前位置读取3个字节转换成long
	 * 
	 * @return 读取的long值，返回-1表示读取文件失败
	 */
	private long readLong3() {
		long ret = 0;
		byte[] b3 = new byte[3];
		try {
			ipFile.readFully(b3);
			ret |= (b3[0] & 0xFF);
			ret |= ((b3[1] << 8) & 0xFF00);
			ret |= ((b3[2] << 16) & 0xFF0000);
			return ret;
		} catch (IOException e) {
			return -1;
		}
	}

	/**
	 * 从offset位置读取四个字节的ip地址放入ip数组中，读取后的ip为big-endian格式，但是
	 * 文件中是little-endian形式，将会进行转换
	 * 
	 * @param offset
	 * @param ip
	 */
	private void readIP(long offset, byte[] ip) {
		try {
			ipFile.seek(offset);
			ipFile.readFully(ip);
			byte temp = ip[0];
			ip[0] = ip[3];
			ip[3] = temp;
			temp = ip[1];
			ip[1] = ip[2];
			ip[2] = temp;
		} catch (IOException e) {
			// LogFactory.log("",Level.ERROR,e);
		}
	}

	/**
	 * 把类成员ip和beginIp比较，注意这个beginIp是big-endian的
	 * 
	 * @param ip
	 *            要查询的IP
	 * @param beginIp
	 *            和被查询IP相比较的IP
	 * @return 相等返回0，ip大于beginIp则返回1，小于返回-1。
	 */
	private int compareIP(byte[] ip, byte[] beginIp) {
		for (int i = 0; i < 4; i++) {
			int r = compareByte(ip[i], beginIp[i]);
			if (r != 0){
				return r;
			}
		}
		return 0;
	}

	/**
	 * 把两个byte当作无符号数进行比较
	 * 
	 * @param b1
	 * @param b2
	 * @return 若b1大于b2则返回1，相等返回0，小于返回-1
	 */
	private int compareByte(byte b1, byte b2) {
		if ((b1 & 0xFF) > (b2 & 0xFF)){ // 比较是否大于
			return 1;
		}else if ((b1 ^ b2) == 0){// 判断是否相等
			return 0;
		}
		
		return -1;
	}

	/**
	 * 从offset偏移处读取一个以0结束的字符串
	 * 
	 * @param offset
	 *            字符串起始偏移
	 * @return 读取的字符串，出错返回空字符串
	 */
	private String readString(long offset) {
		try {
			byte[] buf = new byte[100];
			ipFile.seek(offset);
			int i;
			for (i = 0, buf[i] = ipFile.readByte(); buf[i] != 0; buf[++i] = ipFile
					.readByte())
				;
			if (i != 0){
				return Util.getString(buf, 0, i, "GBK");
			}
		} catch (IOException e) {
			// LogFactory.log("",Level.ERROR,e);
		}
		return "";
	}

	/**
	 * 从offset偏移开始解析后面的字节，读出一个地区名
	 * 
	 * @param offset
	 *            地区记录的起始偏移
	 * @return 地区名字符串
	 * @throws IOException
	 */
	private String readArea(long offset) throws IOException {
		ipFile.seek(offset);
		byte b = ipFile.readByte();
		if (b == REDIRECT_MODE_1 || b == REDIRECT_MODE_2) {
			long areaOffset = readLong3(offset + 1);
			if (areaOffset == 0){
				return Message.unknown_area;
			}
			
			return readString(areaOffset);
		}
		
		return readString(offset);
	}

	/**
	 * 给定一个ip国家地区记录的偏移，返回一个IPLocation结构
	 * 
	 * @param offset
	 *            国家记录的起始偏移
	 * @return IPLocation对象
	 */
	private IPLocation getIPLocation(long offset) {
		try {
			// 跳过4字节ip
			ipFile.seek(offset + 4);
			// 读取第一个字节判断是否标志字节
			byte b = ipFile.readByte();

			String addr = null;
			String area = null;
			if (b == REDIRECT_MODE_1) {
				// 读取国家偏移
				long countryOffset = readLong3();
				// 跳转至偏移处
				ipFile.seek(countryOffset);
				// 再检查一次标志字节，因为这个时候这个地方仍然可能是个重定向
				b = ipFile.readByte();
				if (b == REDIRECT_MODE_2) {
					addr = readString(readLong3());
					ipFile.seek(countryOffset + 4);
				} else {
					addr = readString(countryOffset);
				}

				// 读取地区标志
				area = readArea(ipFile.getFilePointer());
			} else if (b == REDIRECT_MODE_2) {
				addr = readString(readLong3());
				area = readArea(offset + 8);
			} else {
				addr = readString(ipFile.getFilePointer() - 1);
				area = readArea(ipFile.getFilePointer());
			}

			if (StringUtils.isBlank(addr)) {
				return null;
			}

			String country = LexiconUtil.getCountry(addr);
			String province = LexiconUtil.getProvince(country, addr);
			String city = LexiconUtil.getCity(country, province, addr);
			String detail = LexiconUtil
					.getDetail(country, province, city, addr);

			IPLocation loc = new IPLocation(country, province, city, detail);
			loc.setArea(area);
			return loc;
		} catch (IOException e) {
			return null;
		}
	}

}
