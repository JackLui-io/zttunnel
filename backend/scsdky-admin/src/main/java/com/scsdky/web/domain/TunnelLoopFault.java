package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 回路故障
 * @author tubo
 * @TableName t_tunnel_loop_fault
 */
@TableName(value ="t_tunnel_loop_fault")
@Data
public class TunnelLoopFault implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 隧道名称
     */
    @ApiModelProperty(value = "隧道名称")
    private String tunnelName;

    /**
     * 隧道编号
     */
    @ApiModelProperty(value = "隧道编号")
    private Long tunnelId;

    /**
     * 所属区段
     */
    @Excel(name = "所属区段")
    @ApiModelProperty(value = "所属区段")
    private String zone;

    /**
     * 回路编号
     */
    @Excel(name = "回路编号")
    @ApiModelProperty(value = "回路编号")
    private String loopNumber;

    /**
     * 电压（v）
     */
    @Excel(name = "电压（v）")
    @ApiModelProperty(value = "电压（v）")
    private Integer voltage;

    /**
     * 电流（v）
     */
    @Excel(name = "电流（v）")
    @ApiModelProperty(value = "电流（v）")
    private Integer electric;

    /**
     * 发生时间
     */
    @Excel(name = "发生时间",dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "发生时间")
    private Date occurrenceTime;

    /**
     * 恢复时间（发生时间产生后，前恢复时间清零）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "恢复时间")
    @Excel(name = "恢复时间",dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date recoveryTime;

    /**
     * 故障类型
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "故障类型")
    private String breakdown;

    /**
     * 故障类型
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "是否启动")
    private String isStart;

    @ApiModelProperty(value = "预计故障个数")
    private Integer expectFailNum;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 上传时间
     */
    private Date uploadTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}
