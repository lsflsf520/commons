package com.ujigu.acl.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.reflect.TypeToken;
import com.ujigu.secure.common.bean.AbstractTreeBean;
import com.ujigu.secure.common.bean.Bool;
import com.ujigu.secure.common.bean.CommonStatus;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.StringUtil;

public class Func extends AbstractTreeBean<Integer, Func>{
    private Integer id;

    private String name;

    private Set<String> uris;

    private Integer parentId;

    private Bool display;

    private Integer webappId;

//    private String dataPrivConfig;
    private List<DataPrivConfig> dataPrivConfigs;
//    private Set<String> dataPrivLoaders;

    private Integer priority;

    private CommonStatus status;

    private Date createTime;

    private Date lastUptime;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getUri() {
        return StringUtils.join(this.getUris(), ",");
    }

    public void setUri(String uri) {
    	setUris(StringUtil.toSet(uri));
    }
    
    public Set<String> getUris() {
		return uris;
	}

	public void setUris(Set<String> uris) {
		if(!CollectionUtils.isEmpty(uris)){
			this.uris = new LinkedHashSet<>();
			for(String uri : uris){
				if(StringUtils.isNotBlank(uri)){
					this.uris.add(uri.trim());
				}
			}
		}
	}

	public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Bool getDisplay() {
        return display;
    }
    
    @JsonIgnore
    public boolean canShow(){
    	return Bool.Y.equals(this.getDisplay());
    }

    public void setDisplay(Bool display) {
        this.display = display;
    }

    public Integer getWebappId() {
        return webappId;
    }

    public void setWebappId(Integer webappId) {
        this.webappId = webappId;
    }
    
    public String getDataPrivConfig() {
		return this.getDataPrivConfigs() == null ? "" : JsonUtil.create().toJson(this.getDataPrivConfigs());
	}

    @SuppressWarnings("all")
	public void setDataPrivConfig(String dataPrivConfig) {
		if(StringUtils.isNotBlank(dataPrivConfig)){
			List<DataPrivConfig> configs = JsonUtil.create().fromJson(dataPrivConfig, new TypeToken<List<DataPrivConfig>>() {}.getType());
			this.setDataPrivConfigs(configs);
		}
	}

	public List<DataPrivConfig> getDataPrivConfigs() {
		return dataPrivConfigs;
	}

	public void setDataPrivConfigs(List<DataPrivConfig> dataPrivConfigs) {
		this.dataPrivConfigs = dataPrivConfigs;
	}

	public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public CommonStatus getStatus() {
        return status;
    }

    public void setStatus(CommonStatus status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getLastUptime() {
        return lastUptime;
    }

    public void setLastUptime(Date lastUptime) {
        this.lastUptime = lastUptime;
    }

    @Override
    public Integer getPK() {
        return id;
    }
    
    public static class DataPrivConfig implements Serializable{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private String loaderKey;
    	private Set<String> ignoreUris;
    	private Map<String, String> aliasParamMap;
    	
		public String getLoaderKey() {
			return loaderKey;
		}
		public void setLoaderKey(String loaderKey) {
			this.loaderKey = loaderKey;
		}
		public Set<String> getIgnoreUris() {
			return ignoreUris;
		}
		public void setIgnoreUris(Set<String> ignoreUris) {
			this.ignoreUris = ignoreUris;
		}
		public Map<String, String> getAliasParamMap() {
			return aliasParamMap;
		}
		public void setAliasParamMap(Map<String, String> aliasParamMap) {
			this.aliasParamMap = aliasParamMap;
		}
    }

	/*@Override
	public boolean isRoot() {
		return this.getParentId() == null || this.getParentId() <= 0;
	}
	
	@Override
	public void addChild(TreeBean<Integer> child) {
		this.children.add((Func)child);
	}
	
	@Override
	public boolean hasChild() {
		return !CollectionUtils.isEmpty(this.children);
	}
	
	*/
	
	/*@Override
	public TreeBean<Integer> copyWithoutChild() {
		Func copybean = new Func();
		copybean.setId(this.getId());
		copybean.setName(this.getName());
		copybean.setUri(this.getUri());
		copybean.setParentId(this.getParentId());
		copybean.setPriority(this.getPriority());
		copybean.setDisplay(this.getDisplay());
		copybean.setDataPrivLoaders(this.getDataPrivLoaders());
		copybean.setCreateTime(this.getCreateTime());
		copybean.setLastUptime(this.getLastUptime());
		copybean.setStatus(this.getStatus());
		copybean.setWebappId(this.getWebappId());
		
		return copybean;
	}*/
	
	/*@Override
	public void removeChild(Integer k) {
		Func temp = null;
	    for(Func f : this.children){
	    	if(f.getPK().equals(k)){
	    		temp = f;
	    		break;
	    	}
	    }
		
	    if(temp != null){
	    	this.children.remove(temp);
	    }
	}*/
}