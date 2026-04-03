package com.scsdky.web.common;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.dto.AnalyzeDto;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.utils.DateUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.ParseException;


@Component
public class ParamBuild {

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;
    /**
     * 构建analyzeDto参数
     * @param analyzeDto 参数源对象
     * @throws ParseException 转换异常
     */
    public void setAnalyzeDto(AnalyzeDto analyzeDto) throws ParseException {
        //通过隧道id查询边缘控制器的设备号，因为设备数据是根据边缘计算终端设备号绑定的
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, analyzeDto.getTunnelId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        analyzeDto.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());
        //排除当天的
        if(analyzeDto.getEndTime().equals(DateUtils.getDate())) {
            String endTime = DateUtils.getDay(-1, analyzeDto.getEndTime());
            analyzeDto.setEndTime(endTime);
        }
    }
}
