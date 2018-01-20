package com.ujigu.acl.datavalid;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.ujigu.acl.datavalid.bean.PrivContext;
import com.ujigu.secure.common.bean.DataTree;
import com.ujigu.secure.common.bean.TreeBean;

/**
 * 
 * @author 数据权限校验器
 *
 */
public interface DataPrivLoader<K extends Serializable, E extends TreeBean<K, E>> {
	
	/**
	 * 
	 * @return 返回数据权限校验器的唯一标识
	 */
	String getKey();
	
	/**
	 * 
	 * @return 返回数据权限校验器的中文名，用于在管理端展示
	 */
	String getCnName();
	
	/**
	 * 如果当前校验器为属性校验器，则无需管理员再次给用户赋权。例如用户所属公司/部门ID，用户的手机号之类的，因为在添加用户的时候就需要指定这些属性，所以无需再数据权限赋权界面再次给该用户赋权
	 * @return 如果当前权限校验器是根据用户的某个属性去校验的，那么返回true；否则返回false
	 */
//	boolean isUserProp();
	
	/**
	 * 根据单一职责原则，每个校验器最好只针对一个参数做校验，特殊情况除外
	 * @return 此校验器执行校验时，需要uri请求携带的参数列表。校验器会自动从当前request中根据此参数列表获取对应的值，然后进行数据权限校验
	 */
	Set<String> requiredParams();
	
	/**
	 * 
	 * @param workerId 当前登陆用户的ID
	 * @param requestUri 当期请求的uri
	 * @param webappId requestUri所在的webappId
	 * @param paramMap 根据requiredParams()提供的参数列表，从当前request中取出对应的键值对，传入此paramMap中，让校验器执行相应的校验
	 * @param context 用户权限校验的辅助参数
	 * @return
	 */
	boolean checkDataPriv(String requestUri, PrivContext context);
	
	/**
	 * 获取当前用户在当前校验器下所拥有的数据权限ID
	 * @param workerId
	 * @param webappId requestUri所在的webappId
	 * @return
	 */
	Set<K> loadMyDataIncludeChild(PrivContext context);
	
	/**
	 * 
	 * @param workerId 工作人员的ID
	 * @param webappId requestUri所在的webappId
	 * @return 返回id为 workerId 的工作人员在当前权限校验器下拥有的数据树，用于权限校验
	 */
	DataTree<K, E> loadMyDataTreeIncludeChild(PrivContext context);
	
	/**
	 * 
	 * @return 返回当前权限校验器下一颗完整的权限树，用于管理端给用户赋权
	 */
	DataTree<K, E> loadDataTree(PrivContext context);
	
}
