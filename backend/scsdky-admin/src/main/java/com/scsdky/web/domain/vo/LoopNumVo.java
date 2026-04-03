package com.scsdky.web.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class LoopNumVo {

    @ApiModelProperty(value = "设备总数")
    private Long totalNum;

    @ApiModelProperty(value = "故障数")
    private Long loopNum;
}
