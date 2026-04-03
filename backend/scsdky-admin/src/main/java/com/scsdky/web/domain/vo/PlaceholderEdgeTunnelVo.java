package com.scsdky.web.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 绑定列表：通过 {@code tunnel_devicelist_tunnelinfo} 中 {@code devicelist_id} 十进制以 9916 开头 的记录反查得到的四级隧道行，
 * 展示字段对齐原 zt_tunnel_web 隧道管理列表（线路名 / 隧道名 / 方向 / 创建时间）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceholderEdgeTunnelVo {

    /** 四级隧道 id，用于跳转隧道参数、设备管理 */
    private Long tunnelLevel4Id;

    /** level=2 高速公路名 */
    private String lineName;

    /** level=3 隧道群名 */
    private String tunnelName;

    /** level=4 左右线名称，作「隧道方向」展示 */
    private String direction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
