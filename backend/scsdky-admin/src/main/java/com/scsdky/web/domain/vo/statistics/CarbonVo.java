package com.scsdky.web.domain.vo.statistics;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author tubo
 * @date 2023/09/21
 */
@Data
public class CarbonVo {

    @ApiModelProperty(value = "日期")
    private String uploadTime;

    @ApiModelProperty(value = "实际碳排放量")
    private BigDecimal actualCarbonEmission;

    @ApiModelProperty(value = "原设计碳排放量")
    private BigDecimal originalCarbonEmission;
}
