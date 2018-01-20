package com.ujigu.acl.datavalid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.acl.service.DataPrivService;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.bean.TreeBean;
import com.ujigu.secure.common.exception.BaseRuntimeException;
import com.ujigu.secure.common.utils.LogUtils;

public abstract class AbstractDataPrivLoader<K extends Serializable, E extends TreeBean<K, E>> implements DataPrivLoader<K, E> {
	
	@Resource
	protected DataPrivService dataPrivService;

	@Override
	public DataTree<K, E> loadMyDataTreeIncludeChild(PrivContext context) {
		checkContext(context);
		
		DataTree<K, E> dataTree = loadDataTree(context);
		
		Set<K> mydatas = loadMyDataIncludeChild(context);
		
		return dataTree.getTree(mydatas);
	}
	
	private void checkContext(PrivContext context){
		if(context == null || context.getWorkerId() == null){
			throw new BaseRuntimeException("ILLEGAL_CONTEXT", "上下文参数_workerId不能为空");
		}
	}
	
	@Override
	@SuppressWarnings("all")
	public Set<K> loadMyDataIncludeChild(PrivContext context){
		checkContext(context);
		Set<K> mydatas = loadMyDatas(context);
		
		DataTree<K, E> dataTree = loadDataTree(context);
		
		List<K> includeChildIds = dataTree.getNodeIdIncludeChild(mydatas);
		
		return new LinkedHashSet<>(includeChildIds);
	}
	
	@Override
	public boolean checkDataPriv(String requestUri, PrivContext context) {
		checkContext(context);
		
		Set<String> requiredParams = this.requiredParams();
		if(CollectionUtils.isEmpty(requiredParams)){
			LogUtils.warn("at least one param need for checker %s in requestUri %s", this.getKey(), requestUri);
			return false;
		}
		Set<K> mydatas = loadMyDataIncludeChild(context);
		Set<String> strDatas = convert2Strs(mydatas);
		if(strDatas.contains("0")){
			return true; //字符串0代表拥有所有权限，如果用户的数据权限中包含 0 这个特殊权限，则说明拥有所有此类型的数据权限
		}
		for(String paramName : requiredParams){
			String currData = context.getValue(paramName);
			if(StringUtils.isBlank(currData) || !mydatas.contains(currData)){
				currData = currData.trim();
				List<String> dataList = Arrays.asList(currData);
				if(currData.contains(",")){
					dataList = new ArrayList<>();
					String[] parts = currData.split(",");
					for(String part : parts){
						part = part.trim();
						if(StringUtils.isNotBlank(part)){
							dataList.add(part);
						}
					}
				}
				
				dataList = convertParamDatas(requestUri, context, paramName, dataList);
						
				for(String partData : dataList){
					if(StringUtils.isBlank(partData) || !strDatas.contains(partData.trim())){
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * 子类可以根据当前的参数，对需要校验的数值做一下转换，默认直接返回原参数列表
	 * @param paramName
	 * @param paramValues
	 * @return
	 */
	protected List<String> convertParamDatas(String requestUri, PrivContext context, String paramName, List<String> paramValues){
		return paramValues;
	}
	
	/**
	 * 如果数据权限是不是存储在data_priv表中的，即其它自定义数据权限，或者data_priv表中的数据字段不是整型的，则需要重写此方法，@see DepartPrivLoader
	 * @param workerId
	 * @return 返回与当前上下文数据完全匹配的数据权限
	 */
	public Set<K> loadMyDatas(PrivContext context){
		Set<Integer> mydatas = dataPrivService.loadMyIntDatas(context.getWorkerId(), this.getKey());
		Set<K> convertDatas = new LinkedHashSet<>();
		for(Integer data : mydatas){
			convertDatas.add((K)data);
		}
		
		return convertDatas;
	}
	
	private Set<String> convert2Strs(Set<K> mydatas){
		Set<String> strDatas = new LinkedHashSet<>();
		for(K d : mydatas){
			if(d != null){
				strDatas.add(d.toString());
			}
		}
		
		return strDatas;
	}
	
}
