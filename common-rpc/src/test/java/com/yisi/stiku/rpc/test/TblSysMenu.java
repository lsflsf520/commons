package com.yisi.stiku.rpc.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.yisi.stiku.common.bean.BaseEntity;

public class TblSysMenu extends BaseEntity<String> {
    private String id;

    private String aclCode;

    private String aclType;

    private String createdBy;

    private Date createdDt;

    private String updatedBy;

    private Date updatedDt;

    private Integer version;

    private Integer childrenSize;

    private String code;

    private String description;

    private Boolean disabled;

    private Integer inheritLevel;

    private Boolean initOpen;

    private Integer orderRank;

    private String style;

    private String title;

    private String type;

    private String url;

    private String parentId;
    
    private transient List<TblSysMenu> children = new ArrayList<TblSysMenu>();
    
    /**
     * 
     * @param menu 添加一个子菜单
     */
    public void addChild(TblSysMenu menu){
    	children.add(menu);
    }
    
    /**
     * 
     * @return 返回子菜单列表
     */
    public List<TblSysMenu> getChildren(){
    	return Collections.unmodifiableList(children);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getAclCode() {
        return aclCode;
    }

    public void setAclCode(String aclCode) {
        this.aclCode = aclCode == null ? null : aclCode.trim();
    }

    public String getAclType() {
        return aclType;
    }

    public void setAclType(String aclType) {
        this.aclType = aclType == null ? null : aclType.trim();
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy == null ? null : createdBy.trim();
    }

    public Date getCreatedDt() {
        return createdDt;
    }

    public void setCreatedDt(Date createdDt) {
        this.createdDt = createdDt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy == null ? null : updatedBy.trim();
    }

    public Date getUpdatedDt() {
        return updatedDt;
    }

    public void setUpdatedDt(Date updatedDt) {
        this.updatedDt = updatedDt;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getChildrenSize() {
        return childrenSize;
    }

    public void setChildrenSize(Integer childrenSize) {
        this.childrenSize = childrenSize;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? null : code.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Integer getInheritLevel() {
        return inheritLevel;
    }

    public void setInheritLevel(Integer inheritLevel) {
        this.inheritLevel = inheritLevel;
    }

    public Boolean getInitOpen() {
        return initOpen;
    }

    public void setInitOpen(Boolean initOpen) {
        this.initOpen = initOpen;
    }

    public Integer getOrderRank() {
        return orderRank;
    }

    public void setOrderRank(Integer orderRank) {
        this.orderRank = orderRank;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style == null ? null : style.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type == null ? null : type.trim();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId == null ? null : parentId.trim();
    }

    @Override
    public String getPK() {
        return id;
    }
}