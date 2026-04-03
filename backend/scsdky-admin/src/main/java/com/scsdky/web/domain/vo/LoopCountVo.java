package com.scsdky.web.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author tubo
 * @date 2023/09/07
 */
@Data
public class LoopCountVo {

    @ApiModelProperty(value = "区段")
    private String zone;

    private Long num;
}
