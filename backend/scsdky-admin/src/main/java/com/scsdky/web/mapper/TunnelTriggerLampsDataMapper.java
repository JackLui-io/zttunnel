package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelLampsEdgeComputing;
import com.scsdky.web.domain.TunnelTriggerLampsData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @Entity generator.domain.TunnelTriggerLampsData
 */
public interface TunnelTriggerLampsDataMapper extends BaseMapper<TunnelTriggerLampsData> {

    /**
     * 根据灯具终端ID查询灯具状态
     * @return
     */
    List<Map<String, Object>> selectLampsStatus(@Param("tunnelLampsEdgeComputings") List<TunnelLampsEdgeComputing> tunnelLampsEdgeComputings);
    
    /**
     * 批量查询灯具终端的最新记录（按id倒序，每个设备一条）
     * 
     * @param uniqueIds 设备ID列表
     * @return 每个设备的最新记录（unique_id, upload_time）
     */
    List<TunnelTriggerLampsData> selectLatestByUniqueIds(@Param("uniqueIds") List<Long> uniqueIds);
}




