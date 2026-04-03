package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 蓝牙关联节点
 * @TableName tunnel_blue_relation_node
 */
@TableName(value ="tunnel_blue_relation_node")
@Data
public class TunnelBlueRelationNode {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联关系
     */
    private String relationJson;

    /**
     * 灯具id
     */
    private Long deviceListId;

    /**
     * 主节点
     */
    private Integer masterNode;


    /**
     * 创建时间
     */
    private Date createTime;
}