package com.scsdky.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.TunnelNameResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.TunnelNameResult
 */
public interface TunnelNameResultMapper extends BaseMapper<TunnelNameResult> {

    List<TunnelNameResult> getTunnelName(Long userId);

    List<TunnelNameResult> getTunnelListByUserId(Long userId);

    /**
     * 获取用户可见的 level-4 隧道 ID（含层级扩展：用户分配了父节点则包含其 level-4 子节点）
     * @param userId 用户 ID
     * @return level-4 隧道 ID 列表
     */
    List<Long> selectLevel4TunnelIdsForUser(@Param("userId") Long userId);

    /**
     * 设备状态分布专用：仅直接分配 + level-3 扩展，不扩展 level-2（避免同名隧道导致多统计）
     * @param userId 用户 ID
     * @return level-4 隧道 ID 列表
     */
    List<Long> selectLevel4TunnelIdsForDeviceStatus(@Param("userId") Long userId);
}




