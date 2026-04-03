package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scsdky.common.utils.SecurityUtils;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.TunnelSyscmd;
import com.scsdky.web.mapper.TunnelSyscmdMapper;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.service.TunnelSyscmdService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author tubo
 */
@Service
public class TunnelSyscmdServiceImpl extends ServiceImpl<TunnelSyscmdMapper, TunnelSyscmd> implements TunnelSyscmdService{

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;
    @Override
    public TunnelSyscmd setCmdData(Long tunnelIdOrDeviceListId, String action, String ext, String param,Integer type) throws JsonProcessingException {

        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = null;
        //边缘控制器
        if(type == 1) {
            //通过隧道id获取边缘控制器的id
            tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                    .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelIdOrDeviceListId)
                    .eq(TunnelDevicelistTunnelinfo::getType, 1));
        }
        TunnelSyscmd  tunnelSyscmd = new TunnelSyscmd();
        tunnelSyscmd.setUserid(SecurityUtils.getUserId());
        tunnelSyscmd.setCmd(action);
        //生成json操作命令
        ObjectMapper mapper = new ObjectMapper();
        // 创建一个Map模拟你的对象
        Map<String, Object> obj = new LinkedHashMap<>();
        obj.put("Action", action);
        obj.put("Ext", ext);
        obj.put("Param", param);
        obj.put("Target", type == 1 ? tunnelDevicelistTunnelinfo.getDevicelistId() : tunnelIdOrDeviceListId);

        // 将Map序列化为JSON字符串
        String json = mapper.writeValueAsString(obj);
        tunnelSyscmd.setJson(json);
        tunnelSyscmd.setAddtime(new Date());
        tunnelSyscmd.setRspstate(0);
        tunnelSyscmd.setTarget(type == 1 ? tunnelDevicelistTunnelinfo.getDevicelistId() : tunnelIdOrDeviceListId);
        tunnelSyscmd.setIsrsp(0);
        tunnelSyscmd.setReaded(0);
        save(tunnelSyscmd);
        return tunnelSyscmd;
    }
}




