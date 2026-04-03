package com.scsdky.system.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import org.springframework.data.annotation.Id;

import java.util.List;

/**
 * @author wjs
 * @date 2022/4/1 14:54
 */
public class BaseArea {

    /**
     * 城市编码
     */
    @TableId
    public Integer codeId;

    /**
     * 城市父类
     */
    public Integer parentId;

    /**
     * 城市名称
     */
    public String cityName;

    /**
     * 类型 1 国内 2国外
     */
    public Integer type;
    /**
     * 子集
     */
    @TableField(exist = false)
    public List<BaseArea> children;

    public Integer getCodeId() {
        return codeId;
    }

    public void setCodeId(Integer codeId) {
        this.codeId = codeId;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<BaseArea> getChildren() {
        return children;
    }

    public void setChildren(List<BaseArea> children) {
        this.children = children;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
