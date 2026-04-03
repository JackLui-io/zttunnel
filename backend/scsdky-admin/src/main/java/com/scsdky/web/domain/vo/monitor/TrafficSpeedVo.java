package com.scsdky.web.domain.vo.monitor;

import lombok.Data;

import java.util.Date;

/**
 * 车速
 */
@Data
public class TrafficSpeedVo {
    /**
     * 上传数据所处的小时
     */
    private String uploadHour;

    /**
     * 上传的时间
     */
    private Date uploadTime;

    /**
     * 车速
     */
    private String speed;

}
