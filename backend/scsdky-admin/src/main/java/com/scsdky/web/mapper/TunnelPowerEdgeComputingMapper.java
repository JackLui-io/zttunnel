package com.scsdky.web.mapper;

import com.scsdky.web.domain.TunnelPowerEdgeComputing;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Entity generator.domain.TunnelPowerEdgeComputing
 */
public interface TunnelPowerEdgeComputingMapper extends BaseMapper<TunnelPowerEdgeComputing> {

    @Select("select a.*,b.last_time from tunnel_power_edge_computing a \n" +
            "\n" +
            "LEFT JOIN tunnel_carbon_day_push_base b \n" +
            "\n" +
            "on a.devicelist_id = b.devicelist_id and a.address = b.addr\n" +
            "\n" +
            "where a.devicelist_id = #{deviceListId}")
    List<TunnelPowerEdgeComputing> powerList(Long deviceListId);

    /**
     * 四级隧道下全部电能终端（type=2）关联的电表；与 admin 隧道参数页 tunnelId 入参一致。
     */
    @Select("select a.*, b.last_time from tunnel_power_edge_computing a " +
            "LEFT JOIN tunnel_carbon_day_push_base b " +
            "ON a.devicelist_id = b.devicelist_id AND a.address = b.addr " +
            "WHERE a.devicelist_id IN (" +
            "  SELECT devicelist_id FROM tunnel_devicelist_tunnelinfo " +
            "  WHERE tunnel_id = #{tunnelId} AND type = 2" +
            ") " +
            "ORDER BY a.devicelist_id, a.meter_index")
    List<TunnelPowerEdgeComputing> powerListByTunnelId(Long tunnelId);
}




