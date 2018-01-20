package com.ujigu.secure.common.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.ujigu.secure.common.bean.BaseEntity;
import com.ujigu.secure.common.bean.Pair;
import com.ujigu.secure.common.exception.BaseRuntimeException;

public class LogUtils {
	
	private final static Logger LOG = LoggerFactory.getLogger(LogUtils.class);
	
	public static enum IntfType{
		IN, OUT
	}
	private final static Logger API_LOG = LoggerFactory.getLogger("apiLogger");
	private final static Logger XN_LOG = LoggerFactory.getLogger("xnLogger");
	private final static Logger OPER_LOG = LoggerFactory.getLogger("operLogger");
	
	public static void logIntf(IntfType type, String intfName, Map<String, Object> paramMap){
		StringBuilder builder = new StringBuilder();
		if(paramMap != null && !paramMap.isEmpty()){
			for(Entry<String, Object> entry : paramMap.entrySet()){
				builder.append(entry.getKey());
				builder.append(":");
				builder.append(entry.getValue());
				builder.append("&");
			}
		}
		API_LOG.info(type + " " + intfName + "(" + builder.toString() + ")");
	}
	
	public static void logIntf(IntfType type, String intfName, String format, Object... values){
		if(API_LOG.isInfoEnabled()){
			try{
				format = chkformat(format);
				API_LOG.info(type + " " + intfName + "(" + String.format(format, values) + ")");
			} catch (Exception e){
				LOG.error(type + " intfName:" + intfName + ",format:" + format + (values == null ? "" : ",values:" + Arrays.asList(values)), e);
			}
		}
	}
	
	public static void logXN(String intfName, long startTime){
		if(XN_LOG.isInfoEnabled()){
			XN_LOG.info(intfName + " in " + (System.currentTimeMillis() - startTime) + " milliseconds");
		}
	}
	
	public static void logOperLog(String intfName, Object preData, Object afterData){
		if(afterData == null){
//			throw new BaseRuntimeException("ILLEGAL_PARAM", "param afterData should not be null");
		    warn("not found after data for operation %s, preData: %s", intfName, preData);
		}
		
		Map<String, Object> preValMap = null;
		if(preData != null){
			if(preData.getClass() != afterData.getClass()){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "the class of preData and afterData should be equal.");
			}
			
			preValMap = new BeanMap(preData);
		}
		
		Map<String, Object> afterValMap = (afterData == null ? new HashMap<>() : new BeanMap(afterData));
		OperLog operLog = initOperLog(intfName);
		for(String field : afterValMap.keySet()){
			if("pageInfo".equals(field) || "ordseg".equals(field) || "queryParam".equals(field) || ("class".equals(field) && afterData instanceof BaseEntity)){
				//内置变量，无需记录
				continue;
			}
			Object preVal = preData;
			Object afterVal = afterData;
			if(!"class".equals(field)){
				preVal = preValMap == null ? null : preValMap.get(field);
				afterVal = afterValMap.get(field);
				if((preVal != null && !(preVal instanceof Serializable)) || (afterVal != null && !(afterVal instanceof Serializable))){
					LogUtils.warn("ignore for operlog with intfName:%s, field '%s', preVal '%s', afterVal:%s", intfName, field, preVal, afterVal);
					continue;
				}
			}
			operLog.addKV("class".equals(field) ? "val" : field, (Serializable)preVal, (Serializable)afterVal);
		}
		
		operLog.log();
	}
	
	public static OperLog initOperLog(String intfName){
		
		return new OperLog(intfName);
	}
	
	public static class OperLog{
		private String intfName;
		private Map<String/*key*/, Pair<Serializable, Serializable>> operPairMap = new HashMap<>();
		
		public OperLog(String intfName){
			if(StringUtils.isBlank(intfName)){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "param key not allow null");
			}
			
			this.intfName = intfName;
		}
		
		public OperLog addKV(String key, Serializable afterVal){
			
			return addKV(key, null, afterVal);
		}
		
		public OperLog addKV(String key, Serializable preVal, Serializable afterVal){
			if(StringUtils.isBlank(key)){
				throw new BaseRuntimeException("ILLEGAL_PARAM", "param key not allow null");
			}
			
			operPairMap.put(key, new Pair<Serializable, Serializable>(preVal, afterVal));
			
			return this;
		}
		
		public void log(){
            StringBuilder builder = new StringBuilder();
            builder.append("userId:" + ThreadUtil.getUserId());
            builder.append(",userName:" + ThreadUtil.getRealName());
            builder.append(",operTime:" + DateUtil.getCurrentDateTimeStr());
			
            builder.append("," + intfName + "(");
			if(!CollectionUtils.isEmpty(operPairMap)){
				for(String key : operPairMap.keySet()){
					Pair<Serializable, Serializable> pair = operPairMap.get(key);
					if(pair.first == null && pair.second == null){
						continue; //如果修改前后的数据均为null，则忽略该字段的打印
					}
					builder.append(key);
					builder.append(":");
					if(pair.first == null){
						builder.append(pair.second);
					} else {
						builder.append(pair);
					}
					builder.append(",");
				}
				
				builder.setLength(builder.length() - 1);
			}
			
			builder.append(")");
			OPER_LOG.info(builder.toString());	
		}
		
	}
	
	private final static Set<Character> formatChars = new HashSet<>();
	static{
		formatChars.add('s');
		formatChars.add('c');
		formatChars.add('b');
		formatChars.add('d');
		formatChars.add('x');
		formatChars.add('o');
		formatChars.add('f');
		formatChars.add('a');
		formatChars.add('e');
		formatChars.add('g');
		formatChars.add('h');
		formatChars.add('%');
		formatChars.add('n');
		formatChars.add('t');
	}
	
	/**
	 * 
	 * 转  换  符                               说    明                                                示    例
        %s          字符串类型                                   "mingrisoft"
        %c          字符类型                                       'm'
        %b          布尔类型                                       true
        %d         整数类型（十进制）                    99
        %x         整数类型（十六进制）                FF
        %o         整数类型（八进制）                    77
        %f         浮点类型                                          99.99
        %a        十六进制浮点类型                          FF.35AE
        %e         指数类型                                        9.38e+5
        %g   通用浮点类型（f和e类型中较短的）
        %h         散列码
        %%        百分比类型                                          ％
        %n         换行符
        %t[c/F/D/r/T/R]    日期与时间类型转换符
        
	 * String str=String.format("Hi,%s", "王力");   
    * System.out.println(str);  //Hi,王力 
    * str=String.format("Hi,%s:%s.%s", "王南","王力","王张");            
    * System.out.println(str);     //Hi,王南:王力.王张                       
    * System.out.printf("字母a的大写是：%c %n", 'A');  //字母a的大写是：A 
    * System.out.printf("3>7的结果是：%b %n", 3>7);  //3>7的结果是：false  
    * System.out.printf("100的一半是：%d %n", 100/2);  //100的一半是：50 
    * System.out.printf("100的16进制数是：%x %n", 100);  //100的16进制数是：64 
    * System.out.printf("100的8进制数是：%o %n", 100);  //100的8进制数是：144
    * System.out.printf("50元的书打8.5折扣是：%f 元%n", 50*0.85);  //50元的书打8.5折扣是：42.500000 元
    * System.out.printf("上面价格的16进制数是：%a %n", 50*0.85);  //上面价格的16进制数是：0x1.54p5
    * System.out.printf("上面价格的指数表示：%e %n", 50*0.85);  //上面价格的指数表示：4.250000e+01
    * System.out.printf("上面价格的指数和浮点数结果的长度较短的是：%g %n", 50*0.85);  //上面价格的指数和浮点数结果的长度较短的是：42.5000
    * System.out.printf("上面的折扣是%d%% %n", 85);  //上面的折扣是85% 
    * System.out.printf("字母A的散列码是：%h %n", 'A');  //字母A的散列码是：41
    *  Date date=new Date();                                                                    // 创建日期对象
    *  System.out.printf("全部日期和时间信息：%tc%n",date); // 星期一 五月 08 13:56:51 CST 2017
    *  System.out.printf("年-月-日格式：%tF%n",date);
    *  System.out.printf("月/日/年格式：%tD%n",date);
    *  System.out.printf("HH:MM:SS PM格式（12时制）：%tr%n",date);
    *  System.out.printf("HH:MM:SS格式（24时制）：%tT%n",date);
    *  System.out.printf("HH:MM格式（24时制）：%tR",date);
    *  System.out.printf("2位数字24时制的小时（不足2位前面补0）:%tH%n",date);
    *  System.out.printf("2位数字12时制的小时（不足2位前面补0）:%tI%n",date);
    *  System.out.printf("2位数字24时制的小时（前面不补0）:%tk%n",date);
    *  System.out.printf("2位数字12时制的小时（前面不补0）:%tl%n",date);
    *  System.out.printf("2位数字的分钟（不足2位前面补0）:%tM%n",date);
    *  System.out.printf("2位数字的秒（不足2位前面补0）:%tS%n",date);
    *  System.out.printf("3位数字的毫秒（不足3位前面补0）:%tL%n",date);
    *  System.out.printf("9位数字的毫秒数（不足9位前面补0）:%tN%n",date);
    *  String str=String.format(Locale.US,"小写字母的上午或下午标记(英)：%tp",date);
    *  System.out.println(str);                          // 输出字符串变量str的内容
    *  System.out.printf ("小写字母的上午或下午标记（中）：%tp%n",date);
    *  System.out.printf("相对于GMT的RFC822时区的偏移量:%tz%n",date);
    *  System.out.printf("时区缩写字符串:%tZ%n",date);
    *  System.out.printf("1970-1-1 00:00:00 到现在所经过的秒数：%ts%n",date);
    *  System.out.printf("1970-1-1 00:00:00 到现在所经过的毫秒数：%tQ%n",date);
	 * @param format
	 * @param values
	 * 
	 */
	public static void debug(String format, Object... values){
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		Logger clsLogger = LoggerFactory.getLogger(className);
		if(clsLogger.isDebugEnabled() && StringUtils.isNotBlank(format)){
			try{
				format = chkformat(format);
				clsLogger.debug(String.format(format, values));
			} catch (Exception e){
				LOG.error("format:" + format + (values == null ? "" : ",values:" + Arrays.asList(values)), e);
			}
		}
	}
	
	/**
	 * 如果百分号 % 后边的字符没有在格式化字符的白名单中，需要将该百分号转义，否则会抛异常
	 * @param format 
	 * @return
	 */
	private static String chkformat(String format){
		StringBuilder builder = new StringBuilder(format);
		int insertCnt = 0;
		int length = format.length();
		for(int i = 0; i < length; i++){
			char ch = format.charAt(i);
			if(ch == '%'){
				if((i + 1) == length || !formatChars.contains(format.charAt(i + 1))){
					builder.insert(i + insertCnt++, '%');
				}
			}
		}
		
		return builder.toString();
	}
	
	public static void info(String format, Object... values){
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		Logger clsLogger = LoggerFactory.getLogger(className);
		if(clsLogger.isInfoEnabled() && StringUtils.isNotBlank(format)){
			try{
				format = chkformat(format);
			    clsLogger.info(String.format(format, values));
			} catch (Exception e){
				LOG.error("format:" + format + (values == null ? "" : ",values:" + Arrays.asList(values)), e);
			}
		}
	}
	
	public static void warn(String format, Object... values){
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		Logger clsLogger = LoggerFactory.getLogger(className);
		if(clsLogger.isWarnEnabled() && StringUtils.isNotBlank(format)){
			try{
				format = chkformat(format);
			    clsLogger.warn(String.format(format, values));
			} catch (Exception e){
				LOG.error("format:" + format + (values == null ? "" : ",values:" + Arrays.asList(values)), e);
			}
		}
	}
	
	public static void warn(String format, Throwable throwable, Object... values){
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		Logger clsLogger = LoggerFactory.getLogger(className);
		if(clsLogger.isWarnEnabled() && StringUtils.isNotBlank(format)){
			try{
				format = chkformat(format);
			    clsLogger.warn(String.format(format, values), throwable);
			} catch (Exception e){
				LOG.error("format:" + format + (values == null ? "" : ",values:" + Arrays.asList(values)), e);
			}
		}
	}

	public static void error(String format, Throwable throwable, Object... values){
		String className = Thread.currentThread().getStackTrace()[2].getClassName();
		Logger clsLogger = LoggerFactory.getLogger(className);
		if(StringUtils.isNotBlank(format)){
			try{
				format = chkformat(format);
				clsLogger.error(String.format(format, values), throwable);
			} catch (Exception e){
				LOG.error("format:" + format + (values == null ? "" : ",values:" + Arrays.asList(values)), e);
			}
		}
	}
	
}
