package com.scsdky.web.domain.vo;

import com.scsdky.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author tubo
 * 能碳数据Vo
 * @date 2023/09/20
 */
@Data
public class EnergyCarbonVo {

    @ApiModelProperty("日期")
    @Excel(name = "日期")
    private LocalDate uploadTime;

    @ApiModelProperty("小时")
    private Integer hour;

    @ApiModelProperty(value = "理论节电率")
    private BigDecimal theoreticalPowerSavingRate;

    @ApiModelProperty("理论节电量")
    private BigDecimal theoreticalPowerSavings;

    @ApiModelProperty("当日耗电量")
    @Excel(name = "当日耗电量")
    private Double dailyPowerConsumption;

    @ApiModelProperty("总节电量")
    private Integer totalPowerSavings;

    @ApiModelProperty("碳减排量/当量媒")
    private String ratio;

    @ApiModelProperty("等效种树量")
    private String plantTrees;

    @ApiModelProperty("当日节电率")
    private String dailyPowerSavingRate;

    @ApiModelProperty("累计耗电量")
    @Excel(name = "累计耗电量")
    private Double cumulativePowerConsumption;

    @ApiModelProperty("单个电表累计耗电量")
    private BigDecimal cumulativeSimplePowerConsumption;

    @ApiModelProperty("电表读数")
    private List<String> meterReadings;

    @ApiModelProperty("电表读数")
    @Excel(name = "电表读数",length = 6)
    private List<MeterReadingVo> meterReadingVos;

    private String powerDataValue;

    private Long devicelistId;

}
