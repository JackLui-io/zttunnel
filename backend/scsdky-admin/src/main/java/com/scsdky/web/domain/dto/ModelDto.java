package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tubo
 * 分析参数
 * @date 2023/09/20
 */
@Data
public class ModelDto {

    @ApiModelProperty("隧道id")
    @NotNull(message = "隧道id不能为空")
    private Long tunnelId;

    /**
     * 开始时间-针对固定功率和无极调光模式
     */
    @ApiModelProperty("开始时间-针对固定功率和无极调光模式")
    private Date startTime;

    /**
     * 截止时间-针对固定功率和无极调光模式
     */
    @ApiModelProperty("截止时间-针对固定功率和无极调光模式")
    private Date endTime;

    /**
     * 入口段1功率值
     */
    @ApiModelProperty("入口段1功率值")
    private Short th1PowerValue;

    /**
     * 入口段2功率值
     */
    @ApiModelProperty("入口段2功率值")
    private Short th2PowerValue;

    /**
     * 过渡段1功率值
     */
    @ApiModelProperty("过渡段1功率值")
    private Short tr1PowerValue;

    /**
     * 过渡段2功率值
     */
    @ApiModelProperty("过渡段2功率值")
    private Short tr2PowerValue;

    /**
     * 基本段功率值
     */
    @ApiModelProperty("基本段功率值")
    private Short midPowerValue;

    /**
     * 出口段功率值
     */
    @ApiModelProperty("出口段功率值")
    private Short ex1PowerValue;

    /**
     * 紧急停车带功率值
     */
    @ApiModelProperty("紧急停车带功率值")
    private Short emPowerValue;

    /**
     * 1固定功率模式 2 无极调光 3智慧调光
     */
    @ApiModelProperty("1固定功率模式 2 无极调光 3智慧调光")
    private Integer mode;

   /* @ApiModelProperty("模式 日历 无极调光、固定功率使用")
    private List<String> timeList;

    @ApiModelProperty("路段和功率")
    private Map<String,Object> roadAndPower;*/

}
