package com.scsdky.web.service;

import com.scsdky.web.domain.TunnelCarbonDayPush;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.vo.AddrEnergyCarbonVo;
import com.scsdky.web.domain.vo.EnergyCarbonVo;

import java.util.List;

/**
 * @author 涂波
 * @description 针对表【tunnel_carbon_day_push(隧道能碳每日数据)】的数据库操作Service
 * @createDate 2025-06-23 10:52:20
 */
public interface TunnelCarbonDayPushService extends IService<TunnelCarbonDayPush> {

    List<EnergyCarbonVo> getEnergyCarbonVo(List<Long> devicelistId, List<Long> addrIds, AnalyzeDto analyzeDto);

    List<AddrEnergyCarbonVo> getEnergyCarbonVo4(List<Long> devicelistId, List<Long> addrIds, AnalyzeDto analyzeDto);

    List<AddrEnergyCarbonVo> getLatestDataByAddrIds(List<Long> devicelistId, List<Long> addrIds,AnalyzeDto analyzeDto);
}
