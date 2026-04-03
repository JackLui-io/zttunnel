package com.scsdky.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * ota文件管理
 * @TableName tunnel_ota_files
 */
@TableName(value ="tunnel_ota_files")
@Data
public class TunnelOtaFiles implements Serializable {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 老文件名
     */
    private String fileOldName;

    /**
     * 新文件名
     */
    private String fileNewName;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 上传时间
     */
    private Date uploadTime;

    /**
     * 文件上传目录
     */
    private String fileAddress;

    /**
     * 上传人
     */
    private String createBy;

    /**
     * 设备类型（字典值）
     */
    private String deviceType;

    /**
     * 版本号
     */
    private Long version;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
