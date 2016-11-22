package com.yisi.stiku.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.bean.BaseEntity;
import com.yisi.stiku.common.bean.DbEnum;

public class BeanUtils {

	private final static Logger LOG = LoggerFactory.getLogger(BeanUtils.class);

	public static <PK extends Serializable, T extends BaseEntity<PK>> Map<PK, T> buildPK2BeanMap(List<T> tList) {

		Map<PK, T> pk2BeanMap = new HashMap<PK, T>();

		if (tList != null && !tList.isEmpty()) {
			for (T t : tList) {
				pk2BeanMap.put(t.getPK(), t);
			}
		}

		return pk2BeanMap;
	}

	/**
	 * 
	 * @param enumType
	 * @param dbCode
	 * @return
	 */
	@SuppressWarnings("all")
	public static <T extends Enum<T>, CType extends Serializable> T getByDbCode(Class<T> enumType, CType dbCode) {

		try {
			Method method = enumType.getMethod("values", null);
			T[] tList = (T[]) method.invoke(null, null);
			for (T t : tList) {
				DbEnum<CType> dbEnum = (DbEnum<CType>) t;
				if (dbCode.equals(dbEnum.getDbCode())
						|| (dbCode instanceof Integer && dbCode.equals(Integer.valueOf(dbEnum.getDbCode() + "")))) {
					return t;
				}
			}
			throw new IllegalArgumentException("cannot find a instance in " + enumType + " with dbCode " + dbCode);
		} catch (Exception e) {
			throw new IllegalArgumentException(
					"NOT SUPPORTED PARAM. enumType should be a Enum class and inherited from com.yisi.stiku.common.bean.DbEnum, dbCode should inherited from java.io.Serializable");
		}

	}

	public static Object deepClone(Object src) {

		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;

		Object clonedObj = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(src);

			bais = new ByteArrayInputStream(baos.toByteArray());
			ois = new ObjectInputStream(bais);

			clonedObj = ois.readObject();
		} catch (IOException e) {
			throw new IllegalStateException("serialize obj error", e);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("serialize obj error", e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (bais != null) {
				try {
					bais.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}

		}

		return clonedObj;
	}

	/**
	 * 复制两个实体相同属性名称的值
	 * 
	 * @param dest
	 *            目标实体
	 * @param org
	 *            源
	 */
	public static void copyProperties(Object dest, Object orgin) {

		ConvertUtils.register(new Converter() {

			@Override
			public Object convert(Class type, Object value) {

				if (value instanceof Date) {
					return DateUtil.parseDateTime(DateUtil.getDateTimeStr((Date) value));
				}
				if (value == null || !(value instanceof String)) {
					return null;
				}
				String str = (String) value;
				if ("".equals(str.trim())) {
					return null;
				}
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
				try {
					return df.parse(str);
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		}, Date.class);
		try {
			org.apache.commons.beanutils.BeanUtils.copyProperties(dest, orgin);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param objList
	 * @return 将objList强转成 List<T> 并返回
	 */
	@SuppressWarnings("all")
	public static <T> List<T> convert2ObjList(List objList) {

		return convert2ObjList(objList, new ObjConvertor<T>() {

			@Override
			public T convert2Obj(Object obj) {

				return (T) obj;
			}

		});
	}

	@SuppressWarnings("all")
	public static <T> List<T> convert2ObjList(List objList, ObjConvertor<T> objConvertor) {

		List<T> tList = new ArrayList<T>();
		if (objList != null && !objList.isEmpty()) {
			for (Object obj : objList) {
				T t = objConvertor.convert2Obj(obj);

				tList.add(t);
			}
		}

		return tList;
	}

	/**
	 * 
	 * @param objList
	 *            对象集合
	 * @param convertor
	 *            属性值转换器，负责将对象的属性值按一定的顺序添加到List集合中并返回
	 * @return
	 */
	public static <T> List<List<String>> convert2StrList(List<T> objList, Convertor<T> convertor) {

		List<List<String>> valList = new ArrayList<List<String>>();
		if (objList != null && !objList.isEmpty()) {
			for (T t : objList) {
				List<String> vals = convertor.convert2Str(t);

				valList.add(vals);
			}
		}
		return valList;
	}

	public static interface ObjConvertor<T> {

		public T convert2Obj(Object obj);

	}

	public static interface Convertor<T> {

		/**
		 * 
		 * @param obj
		 * @return
		 */
		public List<String> convert2Str(T obj);

	}

	// public static void main(String[] args) {
	//
	// Person p = new Person();
	// // p.setBirthday(new Date());
	// p.setName("liangzheng");
	//
	// Person p1 = new Person();
	// BeanUtils.copyProperties(p1, p);
	// System.out.println(p1.getName() + "," + p1.getBirthday());
	// }
}
