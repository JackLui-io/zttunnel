package com.scsdky.web.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class LightCountVo {

    @ApiModelProperty(value = "区段")
    private Double avgLight;

    private Double avgOutside;
}
