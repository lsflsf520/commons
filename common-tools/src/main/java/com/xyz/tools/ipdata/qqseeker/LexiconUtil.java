package com.xyz.tools.ipdata.qqseeker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 词库加载工具
 * @author lsf
 *
 */
public class LexiconUtil {

	private static final List<String> countryList ;
	private static final List<String> provinceList ;
	private static final List<String> cityList;
	
	private static final Map<String, String[]>  unitMap = new HashMap<String, String[]>();
	
//	private static final List<String> cityList ;
	public static final String UNKNOWN = "未知";
	
	static {
		countryList = initLexicon("country.txt");
		provinceList = initLexicon("province.txt");
		cityList = initLexicon("city.txt");
		List<String> unitLines = initLexicon("unit.txt");
		initUnitMap(unitLines);
	}
	
	private static List<String> initLexicon(String fileName){
		String filePath = Message.DATA_BASE_DIR + (Message.DATA_BASE_DIR.endsWith("/") ? "" : "/") + fileName;
		if(!new File(filePath).exists()){
			filePath = LexiconUtil.class.getResource("/" + fileName).getFile();
		}
		
		try {
			return FileUtils.readLines(new File(filePath), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<String>();
	}
	
	private static void initUnitMap(List<String> lines){
		for(String line : lines){
			if(StringUtils.isNotBlank(line)){
				String[] parts = line.split("\\s+");
				if(parts.length == 3){
					unitMap.put(parts[0], new String[]{parts[1], parts[2]});
				}
			}
		}
	}
	
	/**
	 * 获取国家名
	 * @param addr
	 * @return
	 */
	public static String getCountry(String addr){
		if(StringUtils.isBlank(addr)){
			return null;
		}
		
		for(String country : countryList){
			if(addr.startsWith(country)){
				return country;
			}
		}
		
		return UNKNOWN;
	}
	
	/**
	 * 获取省份
	 * @param country
	 * @param addr
	 * @return
	 */
	public static String getProvince(String country, String addr){
		if(StringUtils.isBlank(addr)){
			return null;
		}
		if(StringUtils.isNotBlank(country) && !isUnknown(country)){
			addr = addr.replace(country, "");
		}
		for(String provice : provinceList){
			if(addr.startsWith(provice)){
				return provice;
			}
		}
		
		return UNKNOWN;
	}
	
	/**
	 * 获取地级市或地级市一级行政区县
	 * @param country
	 * @param province
	 * @param addr
	 * @return
	 */
	public static String getCity(String country, String province, String addr){
		if(StringUtils.isBlank(addr)){
			return null;
		}
		if(StringUtils.isNotBlank(country) && !isUnknown(country)){
			addr = addr.replace(country, "");
		}
		if(StringUtils.isNotBlank(province) && !isUnknown(province)){
			addr = addr.replace(province, "");
		}
		
		for(String city : cityList){
			if(addr.startsWith(city)){
				return city;
			}
		}
		
		return UNKNOWN;
	}
	
	/**
	 * 获取市级以下的详细信息
	 * @param country
	 * @param province
	 * @param city
	 * @param addr
	 * @return
	 */
	public static String getDetail(String country, String province, String city, String addr){
		if(StringUtils.isBlank(addr)){
			return null;
		}
		if(StringUtils.isNotBlank(country) && !isUnknown(country)){
			addr = addr.replace(country, "");
		}
		if(StringUtils.isNotBlank(province) && !isUnknown(province)){
			addr = addr.replace(province, "");
		}
		if(StringUtils.isNotBlank(city) && !isUnknown(city)){
			addr = addr.replace(city, "");
		}
		
		if(StringUtils.isNotBlank(addr)){
			return addr;
		}
		
		return UNKNOWN;
	}
	
	/**
	 * 为给定的位置信息获取市级和省级行政单位的名称
	 * @param detail
	 * @return
	 */
	public static String[] getUnitInfo(String detail){
		for(Entry<String, String[]> entry : unitMap.entrySet()){
			String key = entry.getKey();
			if(detail.startsWith(key)){
				return entry.getValue();
			}
		}
		
		return null;
	}
	
	public static boolean isProvince(String str){
		return provinceList.contains(str);
	}
	
	public static boolean isCity(String str){
		return cityList.contains(str);
	}
	
	public static boolean isUnknown(String addr){
		return UNKNOWN.equals(addr);
	}
	
}
