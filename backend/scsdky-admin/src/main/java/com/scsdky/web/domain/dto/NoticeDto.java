package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class NoticeDto {

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "类型  1 系统通知 2 模式切换 3 灯具故障 4 设备管理")
    private Integer type;

    @ApiModelProperty(value = "隧道id")
    private Integer tunnelId;

    @ApiModelProperty(value = "开始时间")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "0未处理 1 已处理")
    private Integer process;

    @ApiModelProperty(value = "用户id")
    private Long userId;
}
