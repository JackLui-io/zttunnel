package com.scsdky.web.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TunnelCopyResultVo {

    /** 新隧道群（level=3）节点 ID */
    private Long newTunnelGroupId;

    /** 新建的全部 level=4 隧道 ID，顺序与源子节点一致 */
    private List<Long> newLevel4TunnelIds;

    /** 打开隧道参数页时默认选中的 level=4 ID（取第一个） */
    private Long firstLevel4TunnelId;
}
