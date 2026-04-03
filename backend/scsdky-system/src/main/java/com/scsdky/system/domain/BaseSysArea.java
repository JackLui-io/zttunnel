package com.scsdky.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.scsdky.common.core.domain.BaseEntity;

import java.util.List;

/**
 * @author wjs
 * @date 2022/7/27 9:29
 */
public class BaseSysArea extends BaseEntity {

    @TableId(type =IdType.AUTO)
    private Long areaId;

    private Long parentId;

    private String ancestors;

    private String areaname;

    private Long areacode;

    @TableField(exist = false)
    private List<BaseSysArea> children;

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getAncestors() {
        return ancestors;
    }

    public void setAncestors(String ancestors) {
        this.ancestors = ancestors;
    }

    public String getAreaname() {
        return areaname;
    }

    public void setAreaname(String areaname) {
        this.areaname = areaname;
    }

    public Long getAreacode() {
        return areacode;
    }

    public void setAreacode(Long areacode) {
        this.areacode = areacode;
    }

    public List<BaseSysArea> getChildren() {
        return children;
    }

    public void setChildren(List<BaseSysArea> children) {
        this.children = children;
    }
}
