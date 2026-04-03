package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 隧道能碳每日数据
 * @TableName tunnel_carbon_day
 */
@TableName(value ="tunnel_carbon_day")
@Data
public class TunnelCarbonDay {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 累计耗电量
     */
    private Double cumulativePowerConsumption;

    /**
     * 当时耗电量
     */
    private Double dailyPowerConsumption;

    /**
     * 隧道电表数据
     */
    private String powerDataValue;

    /**
     * 理论节电率
     */
    private BigDecimal theoreticalPowerSavingRate;

    /**
     * 理论节电量
     */
    private BigDecimal theoreticalPowerSavings;


    private String meterReadingVos;
    /**
     * 电能终端id
     */
    private Long devicelistId;

    /**
     * 隧道id，这里必须存隧道id，因为跑出来的数据，你只有电能终端id的话是不行的，因为现在电能终端包含左右线，我只查右线，就会识别不了
     */
    private Long tunnelId;

    /**
     * 上传时间
     */
    private String uploadTime;



}