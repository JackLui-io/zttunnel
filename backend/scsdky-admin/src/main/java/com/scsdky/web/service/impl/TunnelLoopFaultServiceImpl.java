package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.scsdky.web.domain.TunnelLoopFault;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.domain.dto.LoopDto;
import com.scsdky.web.domain.vo.LoopCountVo;
import com.scsdky.web.domain.vo.LoopNumVo;
import com.scsdky.web.enums.LoopStatusEnum;
import com.scsdky.web.mapper.TunnelLoopFaultMapper;
import com.scsdky.web.service.TunnelLampsTerminalService;
import com.scsdky.web.service.TunnelLoopFaultService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tubo
 */
@Service
public class TunnelLoopFaultServiceImpl extends ServiceImpl<TunnelLoopFaultMapper, TunnelLoopFault> implements TunnelLoopFaultService {

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @Override
    public List<TunnelLoopFault> getLoopListByPage(LoopDto loopDto) {
        List<TunnelLoopFault> loopList = null;
        if (1 == loopDto.getType()) {
            loopList = this.getBaseMapper().getLoopList(loopDto);
            loopList.forEach(loop ->{
                if(loop.getVoltage() > 240) {
                    loop.setBreakdown(LoopStatusEnum.GY.getValue());
                }else if(loop.getVoltage() < 200) {
                    loop.setBreakdown(LoopStatusEnum.SY.getValue());
                }else{
                    loop.setBreakdown("正常");
                }
                //根据电流值判断
                if(loop.getElectric() == 0) {
                    loop.setIsStart("未启动");
                }
            });
        }
        if (2 == loopDto.getType()) {
            loopList = this.getBaseMapper().getLoopListByPage(loopDto);
            if(StringUtils.isNotBlank(loopDto.getLoopStatus())) {
                //过压
                if(loopDto.getLoopStatus().equals(LoopStatusEnum.GY.getValue())){
                    loopList = loopList.stream().filter(tunnelLoopFault -> tunnelLoopFault.getVoltage() > 240).collect(Collectors.toList());
                }
                //失压
                if(loopDto.getLoopStatus().equals(LoopStatusEnum.SY.getValue())){
                    loopList = loopList.stream().filter(tunnelLoopFault -> tunnelLoopFault.getVoltage() < 200).collect(Collectors.toList());
                }
            }
        }
        return loopList;
    }

    @Override
    public LoopNumVo countByTunnelId(Integer tunnelId) {
        int total = tunnelLampsTerminalService.count();
        LoopNumVo loopNumVo = new LoopNumVo();
        LoopDto loopDto = new LoopDto();
        loopDto.setTunnelId(Long.valueOf(tunnelId));
        int gzNum = tunnelLampsTerminalService.count(new LambdaQueryWrapper<TunnelLampsTerminal>().ne(TunnelLampsTerminal::getDeviceStatus,"00"));
        loopNumVo.setTotalNum((long) total);
        loopNumVo.setLoopNum((long) gzNum);
        return loopNumVo;
    }

    @Override
    public List<LoopCountVo> getCountByZone(AnalyzeDto analyzeDto) {
        return this.getBaseMapper().getCountByZone(analyzeDto);
    }
}




