package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.io.Serializable;

/**
 * 隧道灯具终端节点表(TunnelLampsTerminalNode)表实体类
 *
 * @author makejava
 * @since 2025-09-25 14:54:47
 */
@TableName(value ="tunnel_lamps_terminal_node")
@Data
public class TunnelLampsTerminalNode extends Model<TunnelLampsTerminalNode> {
    //主键ID
    private Long id;
    //灯具终端id
    private Long uniqueId;
    //设备号
    private String deviceNo;
    //关联类型（0=负，1=正）
    private Integer associationType;
    //参数（字符串）
    private String params;
    //直联状态（0=离线，1=在线）
    private Integer directConnectionStatus;
    //节点类型（0=子节点，1=主节点）
    private Integer nodeType;
    }

