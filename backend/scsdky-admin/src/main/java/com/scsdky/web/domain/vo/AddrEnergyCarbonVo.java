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
public class AddrEnergyCarbonVo {
    /**
     * 电表读数
     */
    private BigDecimal powerValue;

    /**
     * 地址号
     */
    private Integer addr;

    /**
     * 上传时间
     */
    private String uploadTime;

    /**
     * 回路名称
     */
    private String loopName;

    /**
     * 边缘计算终端设备号
     */
    private Long devicelistId;
}
