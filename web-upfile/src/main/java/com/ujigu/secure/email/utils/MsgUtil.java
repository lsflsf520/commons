package com.ujigu.secure.email.utils;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.ujigu.secure.cache.constant.DefaultJedisKeyNS;
import com.ujigu.secure.cache.redis.ShardJedisTool;
import com.ujigu.secure.common.bean.MsgType;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.secure.common.utils.LogUtils;

public class MsgUtil {
	
	public static void checkValve(String account, MsgType msgType, boolean isBatch){
		String value = null;
		if(!isBatch){ //批量发送不做单分钟内的总量检查
			String mTotalKey = buildMinuteTotalKey();
			value = ShardJedisTool.get(DefaultJedisKeyNS.msgvalve_m, mTotalKey);
			Integer minuteValve = BaseConfig.getInt(msgType.name().toLowerCase() + ".minute.total.valve");
			//验证1分钟内的总发送量是否超过设定的阀值
			if(StringUtils.isNotBlank(value) && minuteValve != null && Integer.valueOf(value) > minuteValve){
				LogUtils.warn("key %s for account %s has send %s times in one minute, max valve is %d", mTotalKey, account, value, minuteValve);
				throw new BaseRuntimeException("TOO_FREQUENCY", "发送太频繁,请检查是否有黑客攻击");
			}
		}
		
		String mkey = buildMinuteKey(account);
		value = ShardJedisTool.get(DefaultJedisKeyNS.msgvalve_m, mkey);
		//验证当前账号是否在一分钟内已经发送过
		if(StringUtils.isNotBlank(value) && Integer.valueOf(value) > 0){
			LogUtils.warn("key %s has send %s times in one minute, max valve is %d", mkey, value, 1);
			throw new BaseRuntimeException("TOO_FREQUENCY", "消息发送太频繁");
		}
		
		String dayKey = buildDayKey(account);
		value = ShardJedisTool.get(DefaultJedisKeyNS.msgvalve_day, dayKey);
		Integer dayValve = BaseConfig.getInt(msgType.name().toLowerCase() + ".day.valve");
		//验证当前账号当天的发送量是否超过总量
		if(StringUtils.isNotBlank(value) && dayValve != null && Integer.valueOf(value) > dayValve){
			LogUtils.warn("key %s for account %s has send %s times in one minute, max valve is %d", dayKey, account, value, dayValve);
			throw new BaseRuntimeException("TOO_TIMES", "当日发送量过多！");
		}
	}
	
	public static void incrSendNum(String account, boolean isBatch){
		if(!isBatch){//批量发送，不对单分钟内的总量做控制
			String mTotalKey = buildMinuteTotalKey();
			ShardJedisTool.incr(DefaultJedisKeyNS.msgvalve_m, mTotalKey);
		}
		
		String mkey = buildMinuteKey(account);
		ShardJedisTool.incr(DefaultJedisKeyNS.msgvalve_m, mkey);
		
		String dayKey = buildDayKey(account);
		ShardJedisTool.incr(DefaultJedisKeyNS.msgvalve_day, dayKey);
	}
	
	private static String buildMinuteKey(String account){
		return account + "_" + DateUtil.formatDate(new Date(), "HHmm");
	}
	
	private static String buildMinuteTotalKey(){
		return DateUtil.formatDate(new Date(), "HHmm");
	}
	
	private static String buildDayKey(String account){
		return account + "_" + DateUtil.formatDate(new Date(), "yyMMdd");
	}

}
