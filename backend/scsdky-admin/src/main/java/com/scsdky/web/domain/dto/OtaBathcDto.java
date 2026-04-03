package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tubo
 * @date 2024/04/15
 */
@Data
public class OtaBathcDto {

    @ApiModelProperty("文件id")
    private Long fileId;

    @ApiModelProperty("设备id")
    private List<Long> deviceIds;
}
