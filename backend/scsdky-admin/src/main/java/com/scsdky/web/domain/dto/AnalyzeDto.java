package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * @author tubo
 * 分析参数
 * @date 2023/09/20
 */
@Data
public class AnalyzeDto {

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;


    @ApiModelProperty("时间 yyyy-MM-dd,,求某天中每个小时的数量")
    private String time;

    @ApiModelProperty("隧道id")
    @NotNull(message = "隧道id不能为空")
    private Long tunnelId;

    @ApiModelProperty("是否按小时统计，小时统计要传入时间")
    private Integer isHour;

    @ApiModelProperty("边缘计算终端设备号")
    private Long deviceListId;

    @ApiModelProperty("时间,年份---针对首页统计")
    private String year;

    @ApiModelProperty(value = "入库车流量限值（5min）", example = "100")
    private Integer entryTrafficLimit;

    @ApiModelProperty(value = "入库车速限值（km/h）", example = "50")
    private Integer entrySpeedLimit;

    private List<Long> tunnelIds;

}
