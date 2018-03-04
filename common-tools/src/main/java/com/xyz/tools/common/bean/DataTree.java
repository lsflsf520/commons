package com.xyz.tools.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.util.CollectionUtils;

import com.xyz.tools.common.exception.BaseRuntimeException;
import com.xyz.tools.common.utils.BeanUtils;
import com.xyz.tools.common.utils.LogUtils;

@SuppressWarnings("all")
public class DataTree<PK extends Serializable, E extends TreeBean<PK, E>> {
	
	private List<E> beanTree = new CopyOnWriteArrayList<>();
	
	private transient Map<PK, E> pk2BeanMap = new HashMap<>();
	
	/**
	 * 
	 * @param beans 一个已经根据 parent_id.asc,priority.asc 排好序的集合，一般从数据库中先按这样的顺序排序查出来
	 */
	public DataTree(List<E> beans){
		if(CollectionUtils.isEmpty(beans)){
//			throw new BaseRuntimeException("ILLEGAL_PARAM", "参数不能为空集合");
			LogUtils.warn("param is empty for DataTree");
			return ;
		}
		
		pk2BeanMap = BeanUtils.buildPK2BeanMap(beans);
		
		//先按parentId和priority升序排列
		Collections.sort(beans, new Comparator<E>() {

			@Override
			public int compare(E o1, E o2) {
				if(o1 == null){
					return -1;
				} else if(o2 == null){
					return 1;
				}
				PK k1 = o1.getParentId();
				PK k2 = o2.getParentId();
				int result = 0;
				if(k1 == null){
					result = -1;
				}else if(k2 == null){
					result = 1;
				}
				
				if(k1 instanceof Number){
					Long kval1 = ((Number)k1).longValue();
					result = kval1.compareTo(((Number)k2).longValue());
				}else{
					result = k1.toString().compareTo(k2.toString());
				}
				
				if(result == 0){
					Integer ordNo1 = o1.getPriority() == null ? 0 : o1.getPriority();
					Integer ordNo2 = o2.getPriority() == null ? 0 : o2.getPriority();
					
					result = ordNo1.compareTo(ordNo2);
				}
				return result;
			}
		});
		
		for(E bean : beans){
			if(bean == null || bean.getPK() == null){
				continue;
			}
			if(bean.isRoot()){
				beanTree.add(bean);
			}else{
				TreeBean<PK, E> parentBean = pk2BeanMap.get(bean.getParentId());
//				LogUtils.warn("parentId %d, currId:%d", bean.getParentId(), bean.getPK());
				if(parentBean == null){
					LogUtils.warn("not found parent for pk %s with parentId %s", bean.getPK(), bean.getParentId());
				}else{
					parentBean.addChild(bean);
				}
			}
		}
		
	}
	
	/*public List<E> getRootsWithoutChild(){
		List<E> roots = new ArrayList<>();
		for(E root : beanTree){
			roots.add((E)root.copyWithoutChild());
		}
		
		return roots;
	}
	
	public E getBeanWithoutChild(K pk){
		TreeBean<PK> bean = this.pk2BeanMap.get(pk);
		
		return bean != null ? (E)bean.copyWithoutChild() : null;
	}*/
	
	/**
	 * 只返回树的第一层
	 * @return
	 */
	public List<E> getRootOnly(){
		List<E> roots = new ArrayList<>();
		for(E e : this.beanTree){
			e.clearChild();
			roots.add(e);
		}
		
		return roots;
	}
	
	/**
	 * 根据指定的节点id集合，返回包含指定节点的根节点集合，根节点下的子节点将会被全部剔除
	 * @param pks
	 * @return
	 */
	public List<E> getRootOnly(Collection<PK> pks){
		List<E> roots = new ArrayList<>();
		List<E> rootTree = getRoots(pks);
		for(E e : this.beanTree){
			e.clearChild();
			roots.add(e);
		}
		
		return roots;
	}
	
	public List<E> getRoots(){
		return this.beanTree;
	}
	
	/**
	 * 根据指定的节点id集合，返回包含指定节点的根节点集合，也是一颗树，只不过第一层是根节点列表
	 * @param pks 指定的节点id
	 * @return
	 */
	public List<E> getRoots(Collection<PK> pks){

		filterTree(this.beanTree, pks); //先将树中id不在pks中的节点剔除掉
		
		return this.beanTree;
	}
	
	/**
	 * 返回树中包含指定pk参数节点的根节点
	 * @param pk
	 * @return
	 */
	public E getRootNode(PK pk){
		for(E rootNode : beanTree){
			boolean result = hasContainNodeId(rootNode, pk);
			if(result){
				return rootNode;
			}
		}
		
		return null;
	}
	
	/**
	 * 返回树中包含指定pk参数节点的根节点 所代表的树对象
	 * @param pk
	 * @return
	 */
	public DataTree<PK, E> getRootNodeTree(PK pk){
		 E rootNode = getRootNode(pk);
		 
		 return new DataTree<>(Arrays.asList(rootNode));
	}
	
	private boolean hasContainNodeId(E node, PK pk){
		if(node.getPK().equals(pk)){
			return true;
		}
		if(!CollectionUtils.isEmpty(node.getChildren())){
			for(E child : node.getChildren()){
				boolean result = hasContainNodeId(child, pk);
				if(result){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 返回某个id代表的node所属的根节点的ID
	 * @param pk
	 * @return
	 */
	public PK getRootNodeId(PK pk){
		E node = getRootNode(pk);
		
		return node == null ? null : node.getPK();
	}
	
	/**
	 * 将树上的节点拉平后返回
	 * @return
	 */
	public List<E> getFlatNodes(){
		
		return flatNodes(this.beanTree);
	}
	
	/**
	 * 根据指定的节点id集合，生成一颗包含指定节点及其父节点的树，然后将该树拉平后返回
	 * @param pks
	 * @return
	 */
	public List<E> getFlatNodes(Collection<PK> pks){
		List<E> nodes = getRoots(pks);
		
		return flatNodes(nodes);
	}
	
	private List<E> flatNodes(List<E> roots){
		List<E> nodes = new ArrayList<>();
		for(E node : roots){
			nodes.add(node);
			if(!CollectionUtils.isEmpty(node.getChildren())){
				List<E> childs = flatNodes(node.getChildren());
				nodes.addAll(childs);
			}
		}
		
		return nodes;
	}
	
	/**
	 * 返回包含指定pks节点及其所有父节点的id集合
	 * @param pks
	 * @return
	 */
	public List<PK> getNodeIdIncludeParent(Collection<PK> pks){
		List<E> roots = getRoots(pks);
		
		return getNodeIds(roots);
	}
	
	/**
	 * 返回所有节点的主键ID集合
	 * @return
	 */
	public List<PK> getNodeIds(){
		
		return getNodeIds(beanTree);
	}
	
	/**
	 * 返回包含指定pks节点及其所有子节点的id集合
	 * @param pks
	 * @return
	 */
	public List<PK> getNodeIdIncludeChild(Collection<PK> pks){
		List<PK> klist = null;
		
		List<E> nodes = new ArrayList<>();
		for(PK pk : pks){
			E node = getNode(pk);
			if(node != null){
				nodes.add(node);
			} else {
				LogUtils.warn("not found node for pk %s", pk);
			}
		}
		if(!CollectionUtils.isEmpty(nodes)){
			klist = getNodeIds(nodes);
		}
		
		return klist == null ? new ArrayList<PK>() : klist;
	}
	
	/**
	 * 将树中各节点的Id搜集到一起返回
	 * @param roots
	 * @return
	 */
	private List<PK> getNodeIds(List<E> roots){
		List<PK> nodePks = new ArrayList<>();
		for(E root : roots){
			if(!nodePks.contains(root.getPK())){
				nodePks.add(root.getPK());
			}
			if(root.hasChild()){
				List<E> children = root.getChildren();
				
				List<PK> childPks = getNodeIds(children);
				
				for(PK pk : childPks){
					if(!nodePks.contains(pk)){
						nodePks.add(pk);
					}
				}
			}
		}
		
		return nodePks;
	}
	
	/**
	 * 根据指定的节点id集合，返回一颗包含这些节点的树
	 * @param pks
	 * @return
	 */
	public DataTree<PK, E> getTree(Collection<PK> pks){
		filterTree(this.beanTree, pks); //先将树中id不在pks中的节点剔除掉
		
		return this;
	}
	
	/**
	 * 从roots中剔除掉id不在pks中的节点，但需要保留pks中的父节点，一直到根
	 * @param roots
	 * @param pks
	 */
	private void filterTree(List<E> roots, Collection<PK> pks){
		if(!CollectionUtils.isEmpty(pks)){
			Iterator<E> itr = roots.iterator();
			while(itr.hasNext()){
				E item = itr.next();
				if(item.hasChild()){
					List<E> childs = item.getChildren();
					filterTree(childs, pks);
				} 
				
				if(!pks.contains(item.getPK()) && !item.hasChild()){
					roots.remove(item);
				}
			}
		} else {
			roots.clear();
		}
	}
	
	public E getNode(PK pk){
		return this.pk2BeanMap.get(pk);
	}
	
	public void removeNode(PK pk){
		E node = getNode(pk);
		E parentNode = getNode(node.getParentId());
		if(parentNode != null){
			parentNode.removeChild(pk);
		} else {
			this.beanTree.remove(pk);
		}
		
		this.pk2BeanMap.remove(pk);
	}
	

}
