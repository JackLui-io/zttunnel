package com.scsdky.web.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author tubo
 * 洞内洞外照度
 * @date 2023/09/18
 */
@Data
public class InsideAndOutsideVo {

    @ApiModelProperty(value = "日期")
    @Excel(name = "日期")
    private String uploadTime;

    @ApiModelProperty(value = "小时")
    private Integer hour;

    @ApiModelProperty(value = "灯亮时长")
    private String lightUp;

    @ApiModelProperty(value = "灯暗时长")
    private String lightDown;

    @ApiModelProperty(value = "最大值-洞外")
    @Excel(name = "洞外亮度最大值")
    private Integer maxOutside;

    @ApiModelProperty(value = "最小值-洞外")
    @Excel(name = "洞外亮度最小值")
    private Integer minOutside;

    @ApiModelProperty(value = "平均值-洞外")
    @Excel(name = "洞外亮度平均值")
    private Integer avgOutside;
    @ApiModelProperty(value = "洞外亮度值")
    private Integer outsideLightValue;

    @ApiModelProperty(value = "最大值-调光比例")
    @Excel(name = "调光比例最大值")
    private Long maxDimmingRadio;

    @ApiModelProperty(value = "最小值-调光比例")
    @Excel(name = "调光比例最小值")
    private Long minDimmingRadio;

    @ApiModelProperty(value = "平均值-调光比例")
    @Excel(name = "调光比例平均值")
    private Long avgDimmingRadio;

    @ApiModelProperty(value = "调光比例具体值")
    private Integer dimmingRadioValue;


    @ApiModelProperty(value = "亮灯暗灯对比")
    @Excel(name = "亮灯暗灯对比")
    private String lightRadio;


    private Long devicelistId;
}
