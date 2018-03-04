package com.xyz.tools.common.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author lsf
 * 
 */
public class NumberUtil {

	/** 定义数组存放数字对应的大写 */
	private final static String[] STR_NUMBER = { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖" };

	/** 定义数组存放位数的大写 */
	private final static String[] STR_MODIFY = { "", "拾", "佰", "仟", "万", "拾", "佰", "仟", "亿", "拾", "佰", "仟" };

	/**
	 * 转化整数部分
	 * 
	 * @param tempString
	 * @return 返回整数部分
	 */
	private static String getInteger(String tempString) {
		/** 用来保存整数部分数字串 */
		String strInteger = null;//
		/** 记录"."所在位置 */
		int intDotPos = tempString.indexOf(".");
		int intSignPos = tempString.indexOf("-");
		if (intDotPos == -1)
			intDotPos = tempString.length();
		/** 取出整数部分 */
		strInteger = tempString.substring(intSignPos + 1, intDotPos);
		strInteger = new StringBuffer(strInteger).reverse().toString();
		StringBuffer sbResult = new StringBuffer();
		for (int i = 0; i < strInteger.length(); i++) {
			sbResult.append(STR_MODIFY[i]);
			sbResult.append(STR_NUMBER[strInteger.charAt(i) - 48]);
		}

		sbResult = sbResult.reverse();
		replace(sbResult, "零拾", "零");
		replace(sbResult, "零佰", "零");
		replace(sbResult, "零仟", "零");
		replace(sbResult, "零万", "万");
		replace(sbResult, "零亿", "亿");
		replace(sbResult, "零零", "零");
		replace(sbResult, "零零零", "零");
		/** 这两句不能颠倒顺序 */
		replace(sbResult, "零零零零万", "");
		replace(sbResult, "零零零零", "");
		/** 这样读起来更习惯. */
		replace(sbResult, "壹拾亿", "拾亿");
		replace(sbResult, "壹拾万", "拾万");
		/** 删除个位上的零 */
		if (sbResult.charAt(sbResult.length() - 1) == '零' && sbResult.length() != 1)
			sbResult.deleteCharAt(sbResult.length() - 1);
		if (strInteger.length() == 2) {
			replace(sbResult, "壹拾", "拾");
		}
		/** 将结果反转回来. */
		return sbResult.toString();
	}

	/**
	 * 转化小数部分 例：输入22.34返回叁肆
	 * 
	 * @param tempString
	 * @return
	 */
	private static String getFraction(String tempString) {
		String strFraction = null;
		int intDotPos = tempString.indexOf(".");
		/** 没有点说明没有小数，直接返回 */
		if (intDotPos == -1)
			return "";
		strFraction = tempString.substring(intDotPos + 1);
		StringBuffer sbResult = new StringBuffer(strFraction.length());
		for (int i = 0; i < strFraction.length(); i++) {
			sbResult.append(STR_NUMBER[strFraction.charAt(i) - 48]);
		}
		return sbResult.toString();
	}

	/**
	 * 判断传入的字符串中是否有.如果有则返回点
	 * 
	 * @param tempString
	 * @return
	 */
	private static String getDot(String tempString) {
		return tempString.indexOf(".") != -1 ? "点" : "";
	}

	/**
	 * 判断传入的字符串中是否有-如果有则返回负
	 * 
	 * @param tempString
	 * @return
	 */
	private static String getSign(String tempString) {
		return tempString.indexOf("-") != -1 ? "负" : "";
	}

	/**
	 * 将一个数字转化为中文的大写金额
	 * 
	 * @param tempNumber
	 *            传入一个double的变量
	 * @return 返一个转换好的字符串
	 */
	public static String numberToChinese(double tempNumber) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.#########");
		String pTemp = String.valueOf(df.format(tempNumber));
		StringBuffer sbResult = new StringBuffer(
				getSign(pTemp) + getInteger(pTemp) + getDot(pTemp) + getFraction(pTemp));
		return sbResult.toString();
	}

	public static String numberToChinese(BigDecimal tempNumber) {
		return numberToChinese(tempNumber.doubleValue());
	}
	

	/**
	 * 替代字符
	 * 
	 * @param pValue
	 * @param pSource
	 * @param pDest
	 */
	private static void replace(StringBuffer pValue, String pSource, String pDest) {
		if (pValue == null || pSource == null || pDest == null)
			return;
		/** 记录pSource在pValue中的位置 */
		int intPos = 0;
		do {
			intPos = pValue.toString().indexOf(pSource);
			/** 没有找到pSource */
			if (intPos == -1)
				break;
			pValue.delete(intPos, intPos + pSource.length());
			pValue.insert(intPos, pDest);
		} while (true);
	}

	/**
	 * 
	 * @param data
	 *            需要被格式化的数据,可接收java的原生类型数据
	 * @param pattern
	 *            格式化pattern, 例如：0、0.00或0.00%
	 * @return 按照指定的pattern形式，格式化data后返回
	 */
	public static String format(double data, String pattern) {

		DecimalFormat formatter = new DecimalFormat(pattern);

		return formatter.format(data);
	}

	/**
	 * 
	 * @param data
	 *            需要被格式化的数据,可接收java的原生类型数据
	 * @return 按照指定的0.00形式，格式化data后返回
	 */
	public static String format(double data) {

		return format(data, "0.00");
	}

	/**
	 * 将百分比表达式的前一部分(%前边的数字) 乘以 100变成整数，存入数据库
	 * 
	 * @param percent
	 * @return
	 */
	public static int toDbPercent(float percent) {

		return (int) (percent * 100);
	}

	public static String fromDbPercent(long percent) {

		return format(percent / 100.0);
	}

	public static int roundByDbPercent(long percent) {

		return Math.round(percent / 10000.0f);
	}

	public static int floorByDbPercent(long percent) {

		return floor(percent / 10000.0f);
	}

	/**
	 * 
	 * @param data
	 *            需要被格式化的数据,可接收java的原生类型数据
	 * @param pattern
	 *            格式化pattern类型，例如：0、0.00或0.00%
	 * @return 需要被格式化的数据,可接收java的原生类型数据
	 */
	public static double formatToDouble(double data, String pattern) {

		return Double.valueOf(format(data, pattern));
	}

	/**
	 * 
	 * @param data
	 *            需要被格式化的数据,可接收java的原生类型数据
	 * @return 按照指定的0.00形式，格式化data后返回
	 */
	public static double formatToDouble(double data) {

		return Double.valueOf(format(data));
	}

	/**
	 * 
	 * <p>
	 * Title: formatToPecentByDefault
	 * </p>
	 * <p>
	 * Description:this is a method
	 * </p>
	 * 
	 * @param data
	 *            需要被格式化的数据
	 * @return 按照0.00%的形式格式化data数据后返回
	 */
	public static String formatToPecent(double data) {

		return format(data, "0.00%");
	}

	/**
	 * 实现一个BigInteger和int的求和
	 * 
	 * @param a
	 *            被加数，BigInteger对象
	 * @param b
	 *            加数，整形
	 * @return 返回相加后的结果
	 */
	public static BigInteger addBigInt(BigInteger a, int b) {

		return a.add(BigInteger.valueOf(b));
	}

	/**
	 * 实现一个BigInteger的自增1
	 * 
	 * @param a
	 *            被加数，BigInteger对象
	 * @return 返回a + 1 之后的结果
	 */
	public static BigInteger autoIncrement(BigInteger a) {

		return addBigInt(a, 1);
	}

	/**
	 * 
	 * @param v1
	 *            双精度被加数
	 * @param v2
	 *            双精度加数
	 * @return 返回 v1 + v2 之后的结果
	 */
	public static double add(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.add(b2).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            双精度被加数
	 * @param v2
	 *            双精度加数
	 * @param scale
	 *            对返回结果保留scale位小数
	 * @return v1 + v2 之后，保留scale位小数后返回
	 */
	public static double add(double v1, double v2, int scale) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.add(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            双精度被减数
	 * @param v2
	 *            双精度减数
	 * @return 返回 v1 - v2 之后的结果
	 */
	public static double subtract(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            双精度被减数
	 * @param v2
	 *            双精度减数
	 * @param scale
	 *            对返回结果保留scale位小数
	 * @return v1 - v2 之后，保留scale位小数后返回
	 */
	public static double subtract(double v1, double v2, int scale) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.subtract(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            双精度被乘数
	 * @param v2
	 *            双精度乘数
	 * @return 返回 v1 * v2 之后的结果
	 */
	public static double multiply(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.multiply(b2).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            双精度被乘数
	 * @param v2
	 *            双精度乘数
	 * @param scale
	 *            对返回结果保留scale位小数
	 * @return v1 * v2 之后，保留scale位小数后返回
	 */
	public static double multiply(double v1, double v2, int scale) {

		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            双精度被除数
	 * @param v2
	 *            双精度除数
	 * @return 返回 v1 / v2 之后的结果
	 */
	public static double divide(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.divide(b2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            双精度被除数
	 * @param v2
	 *            双精度除数
	 * @param scale
	 *            对返回结果保留scale位小数
	 * @return v1 / v2 之后，保留scale位小数后返回
	 */
	public static double divide(double v1, double v2, int scale) {

		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * 
	 * @param v1
	 *            被比较的双精度参数
	 * @param v2
	 *            用于比较的双精度参数
	 * @return 如果v1小于v2，则返回-1；如果v1大于v2，则返回1；如果相等，则返回0
	 */
	public static int compareTo(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.compareTo(b2);
	}

	/**
	 * 
	 * 功能描述: <br>
	 * 提供精确的类型转换(Float)
	 * 
	 * @param v
	 * @return
	 * @see [相关类/方法](可选)
	 * @since [产品/模块版本](可选)
	 */
	public static float convertsToFloat(double v) {

		BigDecimal b = new BigDecimal(v);
		return b.floatValue();
	}

	/**
	 * 
	 * 功能描述: <br>
	 * 提供精确的类型转换(Int)不进行四舍五入
	 * 
	 * @param v
	 * @return
	 * @see [相关类/方法](可选)
	 * @since [产品/模块版本](可选)
	 */
	public static int convertsToInt(double v) {

		BigDecimal b = new BigDecimal(v);
		return b.intValue();
	}

	/**
	 * 
	 * 功能描述: <br>
	 * 提供精确的类型转换(Long)
	 * 
	 * @param v
	 * @return
	 * @see [相关类/方法](可选)
	 * @since [产品/模块版本](可选)
	 */
	public static long convertsToLong(double v) {

		BigDecimal b = new BigDecimal(v);
		return b.longValue();
	}

	/**
	 * 
	 * 功能描述: <br>
	 * 返回两个数中大的一个值
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 * @see [相关类/方法](可选)
	 * @since [产品/模块版本](可选)
	 */
	public static double returnMax(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.max(b2).doubleValue();
	}

	/**
	 * 将double类型的数值取整
	 * 
	 * @param data
	 * @return
	 */
	public static int floor(double data) {
		return (int) data;
	}

	/**
	 * 
	 * 功能描述: <br>
	 * 返回两个数中小的一个值
	 * 
	 * @param v1
	 * @param v2
	 * @return
	 * @see [相关类/方法](可选)
	 * @since [产品/模块版本](可选)
	 */
	public static double returnMin(double v1, double v2) {

		BigDecimal b1 = new BigDecimal(v1);
		BigDecimal b2 = new BigDecimal(v2);
		return b1.min(b2).doubleValue();
	}

	/**
	 * 如果data为空，则返回0；否则返回data的值
	 * 
	 * @param data
	 * @return
	 */
	public static int getInt(Integer data) {
		return data == null ? 0 : data;
	}

	/**
	 * 从company端的id转成admin端的id
	 * 
	 * @param acId
	 * @param uniquId
	 * @return
	 */
	public static int fromUniqId(int acId, int uniquId) {
		int id = uniquId % (acId * 1000000);

		return id;
	}

	/**
	 * 从admin中的id转成company端的id
	 * 
	 * @param acId
	 * @param id
	 * @return
	 */
	public static int toUniqId(int acId, int id) {
		int uniqueId = acId * 1000000 + id;

		return uniqueId;
	}

	public static Set<Integer> toIntSet(String str) {
		Set<Integer> results = new LinkedHashSet<>();
		if (StringUtils.isNotBlank(str)) {
			String[] parts = str.split(",");
			for (String part : parts) {
				if (RegexUtil.isInt(part)) {
					results.add(Integer.valueOf(part));
				}
			}
		}

		return results;
	}

	public static List<Integer> toIntList(String str) {
		List<Integer> results = new ArrayList<>();
		if (StringUtils.isNotBlank(str)) {
			String[] parts = str.split(",");
			for (String part : parts) {
				if (RegexUtil.isInt(part)) {
					results.add(Integer.valueOf(part));
				}
			}
		}

		return results;
	}

	/**
	 * @param param
	 * @return
	 * @Decription 元转分
	 * @Author Administrator
	 * @Time 2017年7月13日上午11:13:59
	 * @Exception
	 */
	public static int yuan2Fen(String param) {
		if (param != null && !"".equals(param)) {
			BigDecimal bigDecimal = new BigDecimal(param);
			BigDecimal bd = new BigDecimal(100);
			int result = bigDecimal.multiply(bd).intValue();
			return result;
		} else {
			return 0;
		}
	}

	/**
	 * @param param
	 * @return
	 * @Decription 分转元
	 * @Author Administrator
	 * @Time 2017年7月13日上午11:14:15
	 * @Exception
	 */
	public static float fen2Yuan(int param) {
		BigDecimal bigDecimal = new BigDecimal(param);
		BigDecimal bd = new BigDecimal(100);
		float result = bigDecimal.divide(bd).floatValue();
		return result;
	}

	/**
	 * @param fee
	 * @return
	 * @Decription 银行家算法 四舍六入五考虑 五后非零就进一(1.1651 = 1.17) 五后皆零看奇偶 五前为偶应舍去(1.1650 =
	 *             1.16) 五前为奇要进一(1.1350 = 1.14)
	 * @Author Administrator
	 * @Time 2017年7月6日下午4:30:39
	 * @Exception
	 */
	public static String countFee(float fee) {
		String[] str = (fee + "").split("\\.");
		String result = fee + "";
		if (str[1].length() == 3) {
			int third = Integer.parseInt(str[1].substring(2, 3));
			int second = Integer.parseInt(str[1].substring(1, 2));
			int first = Integer.parseInt(str[1].substring(0, 1));
			int integral = Integer.parseInt(str[0]);
			if (third < 5) {
				result = integral + "." + str[1].substring(0, 2);
				return result;
			} else if (third > 5) {
				if (second < 9) {
					result = integral + "." + first + (second + 1);
				} else {
					if (first < 9) {
						result = integral + "." + (first + 1) + "0";
					} else {
						result = (integral + 1) + ".00";
					}
				}
				return result;// str[0] + "." + str[1].substring(0,
								// 1)+(second+1);
			} else {
				if (second % 2 == 0) {
					result = integral + "." + str[1].substring(0, 2);
					return result;
				} else {
					if (second < 9) {
						result = integral + "." + first + (second + 1);
					} else {
						if (first < 9) {
							result = integral + "." + (first + 1) + "0";
						} else {
							result = integral + ".00";
						}
					}
					return result;// str[0] + "." + str[1].substring(0,
									// 1)+(second+1);
				}
			}
		} else if (str[1].length() > 3) {
			int third = Integer.parseInt(str[1].substring(2, 3));
			int second = Integer.parseInt(str[1].substring(1, 2));
			int fouth = Integer.parseInt(str[1].substring(3, 4));
			int first = Integer.parseInt(str[1].substring(0, 1));
			int integral = Integer.parseInt(str[0]);
			if (third < 5) {
				result = integral + "." + str[1].substring(0, 2);
				return result;
			} else if (third > 5) {
				if (second < 9) {
					result = integral + "." + first + (second + 1);
				} else {
					if (first < 9) {
						result = integral + "." + (first + 1) + "0";
					} else {
						result = (integral + 1) + ".00";
					}
				}
				return result;// integral + "." + str[1].substring(0,
								// 1)+(second+1);
			} else {
				if (fouth > 0) {
					if (second < 9) {
						result = integral + "." + first + (second + 1);
					} else {
						if (first < 9) {
							result = integral + "." + (first + 1) + "0";
						} else {
							result = (integral + 1) + ".00";
						}
					}
					return result;
				} else {
					if (second % 2 == 0) {
						result = integral + "." + str[1].substring(0, 2);
						return result;
					} else {
						if (second < 9) {
							result = integral + "." + first + (second + 1);
						} else {
							if (first < 9) {
								result = integral + "." + (first + 1) + "0";
							} else {
								result = (integral + 1) + ".00";
							}
						}
						return result;// str[0] + "." + str[1].substring(0,
										// 1)+(second+1);
					}
				}
			}
		} else {
			return fee + "";
		}
	}
}
