package com.scsdky.web.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * tunnel_edge_computing_terminal 字段元数据（与 INFORMATION_SCHEMA 及实体属性对齐）。
 */
@Data
@ApiModel("边缘计算终端表字段元数据")
public class TunnelEdgeTerminalColumnMetaVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Java/JSON 属性名（camelCase）")
    private String propertyName;

    @ApiModelProperty("数据库列名")
    private String columnName;

    @ApiModelProperty("列注释（COMMENT）")
    private String comment;

    @ApiModelProperty("数据类型，如 int、varchar；虚拟字段为 virtual")
    private String dataType;

    @ApiModelProperty("列顺序（虚拟字段置后）")
    private Integer ordinalPosition;
}
