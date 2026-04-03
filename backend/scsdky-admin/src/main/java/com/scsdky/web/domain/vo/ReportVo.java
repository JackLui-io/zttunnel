package com.scsdky.web.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class ReportVo {

    @ApiModelProperty(value = "路线")
    private String route;

    private String tunnelName;

    @ApiModelProperty(value = " 里程")
    private BigDecimal mileage;

    @ApiModelProperty(value = " 数据")
    private List<Map<String, Object>> list;


    @ApiModelProperty(value = "设备类型集合--报表异常事件使用")
    private List<String> deviceTypeList;

    @ApiModelProperty(value = "回路故障集合--报表异常事件使用")
    private List<String> loopTypeList;
}
