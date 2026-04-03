package com.scsdky.web.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * ota文件管理
 * @TableName tunnel_ota_files
 */
@Data
public class TunnelOtaFilesDto implements Serializable {

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 版本号
     */
    private Long version;

}
