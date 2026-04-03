package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity generator.domain.TunnelDevicelistTunnelinfo
 */
public interface TunnelDevicelistTunnelinfoMapper extends BaseMapper<TunnelDevicelistTunnelinfo> {

    /**
     * DISTINCT tunnel_id，避免拉全表行再在内存去重；建议 devicelist_id 有索引。
     */
    List<Long> listDistinctTunnelIdByDevicelistIdRange(@Param("minId") long minId, @Param("maxId") long maxId);

    /**
     * devicelist_id 十进制字符串以 9916 开头（含占位段及未来扩展位宽）；distinct tunnel_id。
     */
    List<Long> listDistinctTunnelIdByDevicelistId9916CharPrefix();
}




