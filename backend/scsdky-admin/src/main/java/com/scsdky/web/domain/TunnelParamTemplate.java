package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 隧道参数模板主表
 */
@TableName("tunnel_param_template")
@Data
public class TunnelParamTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateCode;
    private String templateName;
    private Integer templateSchemaVersion;
    /** 0草稿 1启用 2停用（逻辑删） */
    private Integer status;
    private String remark;
    private Long sourceTunnelGroupId;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
}
