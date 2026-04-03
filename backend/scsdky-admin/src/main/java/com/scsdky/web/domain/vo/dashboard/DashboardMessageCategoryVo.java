package com.scsdky.web.domain.vo.dashboard;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * Dashboard 消息通知分类（RightSidebar 消息通知卡片）
 */
@Data
public class DashboardMessageCategoryVo {

    @ApiModelProperty(value = "分类名称：系统通知、其它通知")
    private String name;

    @ApiModelProperty(value = "该分类总数")
    private Integer count;

    @ApiModelProperty(value = "消息列表")
    private List<DashboardMessageItemVo> items;
}
