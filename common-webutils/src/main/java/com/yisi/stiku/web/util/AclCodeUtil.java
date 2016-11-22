package com.yisi.stiku.web.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * 
 * @author shangfeng
 *
 */
public class AclCodeUtil {

	/**
	 * 
	 * @param aclCodeStr 格式为 areaId1[_schoolId1],areaId2[_schoolId2],areaIdn[_schoolIdn] ，下划线前边的为地址编码，下划线后边的为学校ID，如果没有下划线就是地址ID
	 * @param targetData 目标数据，用来检测 aclCodeStr 是否包含targetData，即是否有权限
	 * @return 如果有targetData数据的权限返回true；否则返回false
	 */
	public static boolean hasPermission(String aclCodeStr, String targetData){
		if(StringUtils.isBlank(aclCodeStr)){
			return false; //如果aclCodeStr为空，则直接返回false，即没有权限
		}
		
		return hasPermission(aclCodeStr.split(","), targetData);
	}
	
	/**
	 * 
	 * @param aclCodeArr 每个元素的格式为 格式为 areaId1[_schoolId1]，下划线前边的为地址编码，下划线后边的为学校ID，如果没有下划线就是地址ID
	 * @param targetData 目标数据，用来检测 aclCodeStr 是否包含targetData，即是否有权限
	 * @return 如果有targetData数据的权限返回true；否则返回false
	 */
	public static boolean hasPermission(String[] aclCodeArr, String targetData){
		if(aclCodeArr != null && aclCodeArr.length > 0 && StringUtils.isNotBlank(targetData)){
			for(String aclCode : aclCodeArr){
				if(aclCode.startsWith(targetData)){ //判断aclCode是否以targetData开头，如果是，则返回true；否则继续比对下一个aclCode
					return true;
				}
				//下边这段验证可能有bug，先不要了
//				else if(aclCode.contains("_")){ //如果为其它数据，则从第一个下划线的后边数据开始，只要跟targetData相等，则说明有权限
//					String[] parts = aclCode.split("_");
//					for(int i = 1; i < parts.length; i++){
//						if(targetData.equals(parts[i])){
//							return true;
//						}
//					}
//				}
			}
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param targetData 目标数据，用来检测 aclCodeStr 是否包含targetData，即是否有权限
	 * @return 当前登录用户如果有targetData数据的权限，则返回true；否则返回false
	 */
	public static boolean hasPermission(String targetData){
		String currAclCodeStr = getAclCodeForLoginUser();
		
		return hasPermission(currAclCodeStr, targetData);
	}
	
	/**
	 * 
	 * @param aclCodeName 如果是HQL语句，请传入aclCode的hql路径名(比如 "from School s where s.name = 'xxx' and :aclCodeHql", 那么这时的方法参数aclCodeName应为 "s.aclCode")；
	 *                    如果是sql语句，请传入aclCode的sql路径名(比如 "select * from school s where s.name = 'xxx' and :aclCodeSql"， 那么这时的aclCodeName应为 "s.acl_code");
	 * @return 根据当前用户的aclCode生成一条用于数据过滤的sql语句
	 */
	public static String genAclFilterSql(String aclCodeName){
		String currAclCodeStr = getAclCodeForLoginUser();
		
		if(StringUtils.isBlank(currAclCodeStr)){
			return "";
		}
		
		return genAclFilterSql(currAclCodeStr.split(","), aclCodeName);
	}
	
	/**
	 * 
	 * @return 返回当前用户的aclCode
	 */
	public static String getAclCodeForLoginUser(){
		return LoginSesionUtil.getAclCodeStr();
	}
	
	/**
	 * 
	 * @param aclCodeArr
	 * @return 解析aclCodeStr中的学校ID(如果绑定数据权限的时候没有绑定到学校这一级，将解析不出来)
	 */
	public static List<Long> parseSchoolIdList(String[] aclCodeArr){
		List<Long> schoolIdList = new ArrayList<Long>();
		if(aclCodeArr != null){
			for(String aclCode : aclCodeArr){
				String[] parts = null;
				if(StringUtils.isNotBlank(aclCode) && aclCode.contains("_") && (parts = aclCode.split("_")).length >= 2){
					Long schoolId = Long.valueOf(parts[1]);
					schoolIdList.add(schoolId);
				}
			}
		}
		
		return schoolIdList;
	}
	
	/**
	 * 
	 * @param aclCodeStr
	 * @return 解析aclCodeStr中的学校ID(如果绑定数据权限的时候没有绑定到学校这一级，将解析不出来)
	 */
	public static List<Long> parseSchoolIdList(String aclCodeStr){
		if(StringUtils.isBlank(aclCodeStr)){
			return new ArrayList<Long>();
		}
		
		return parseSchoolIdList(aclCodeStr.split(","));
	}
	
	/**
	 * 
	 * @return 返回当前用户绑定的学校数据(如果绑定数据权限的时候没有绑定到学校这一级，将解析不出来)
	 */
	public static List<Long> getSchoolIdListForCurrUser(){
		String aclCodeStr = getAclCodeForLoginUser();
		
		return parseSchoolIdList(aclCodeStr);
	}
	
	/**
	 * 
	 * @param aclCodeArr 当前用户的aclCode权限
	 * @param aclCodeName 如果是HQL语句，请传入aclCode的hql路径名(比如 "from School s where s.name = 'xxx' and :aclCodeHql", 那么这时的方法参数aclCodeName应为 "s.aclCode")；
	 *                    如果是sql语句，请传入aclCode的sql路径名(比如 "select * from school s where s.name = 'xxx' and :aclCodeSql"， 那么这时的aclCodeName应为 "s.acl_code");
	 * @return 返回一个根据aclCode进行过滤的sql条件语句
	 */
	public static String genAclFilterSql(String[] aclCodeArr, String aclCodeName){
		String filterSql = "";
		if(aclCodeArr != null && aclCodeArr.length > 0){
			filterSql = "(";
			for(String aclCode : aclCodeArr){
				filterSql += aclCodeName + " like '" + aclCode + "' or";
			}
			filterSql.substring(0, filterSql.length() - 2);
			filterSql += ")";
		}
		
		return filterSql;
	}
}
