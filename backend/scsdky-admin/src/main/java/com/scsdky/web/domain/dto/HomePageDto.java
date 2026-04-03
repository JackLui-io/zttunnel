package com.scsdky.web.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author tubo
 * 分析参数
 * @date 2023/09/20
 */
@Data
public class HomePageDto {

    @ApiModelProperty("年份")
    private String year;


    @ApiModelProperty("隧道id")
    @NotNull(message = "隧道id不能为空")
    private Long tunnelId;

    private List<Long> tunnelIds;

}
