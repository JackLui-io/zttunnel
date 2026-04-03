package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dashboard 隧道能耗排行
 */
@Data
@ApiModel("隧道能耗排行")
public class DashboardEnergyRankVo {

    @ApiModelProperty("排行列表，最多 5 条")
    private List<RankItem> list;

    @Data
    @ApiModel("排行项")
    public static class RankItem {
        @ApiModelProperty("排名 1-5")
        private Integer rank;
        @ApiModelProperty("展示文本，如「老君顶隧道 3月」")
        private String month;
        @ApiModelProperty("占比 0-100，用于进度条")
        private Integer percent;
        @ApiModelProperty("耗电量，如「159.52 kWh」")
        private String value;
        @ApiModelProperty("样式类型 orange/yellow/green")
        private String type;
    }
}
