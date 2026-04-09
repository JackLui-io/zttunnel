package com.scsdky.web.controller.tunnel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.core.redis.RedisCache;
import com.scsdky.web.config.websocket.WebSocketServer;
import com.scsdky.web.domain.*;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.ConvertBit;
import com.scsdky.web.utils.DateUtils;
import io.swagger.annotations.Api;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author tubo
 * @date 2023/09/25
 */
@RestController
@RequestMapping("/push")
@Api(value = "推送模块", tags = {"DIRECTOR 1.8：推送模块"})
@Component
public class PushController extends BaseController {

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelNoticeService tunnelNoticeService;

    @Resource
    private TunnelEdgeComputeDataService tunnelEdgeComputeDataService;

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Resource
    private TunnelTriggerLampsDataService tunnelTriggerLampsDataService;

    @Resource
    private TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;

    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @Resource
    private TunnelEdgeTriggerDataService tunnelEdgeTriggerDataService;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private RedisCache redisCache;

    @Scheduled(cron = "0 0/5 * * * ? ")
    public AjaxResult push() throws IOException {

        //查询所有的边缘控制器，方便推送
        List<TunnelDevicelist> devicelists = tunnelDevicelistService.list(new LambdaQueryWrapper<TunnelDevicelist>().eq(TunnelDevicelist::getDeviceTypeId,1));

        for (TunnelDevicelist devicelist : devicelists) {
            //模拟每20秒推送一个数据
            LambdaQueryWrapper<TunnelEdgeComputeData> tunnelEdgeComputeDataLambdaQueryWrapper = new LambdaQueryWrapper<>();
            tunnelEdgeComputeDataLambdaQueryWrapper.eq(TunnelEdgeComputeData::getDevicelistId,devicelist.getDeviceId());
            tunnelEdgeComputeDataLambdaQueryWrapper.orderByDesc(TunnelEdgeComputeData::getUploadTime);
            tunnelEdgeComputeDataLambdaQueryWrapper.last("limit 1");
            TunnelEdgeComputeData tunnelEdgeComputeData = tunnelEdgeComputeDataService.getOne(tunnelEdgeComputeDataLambdaQueryWrapper);
            // 原先用全局 long id 与 edge 表主键比较，去重「同一条记录只推一次」；若现场长时间没有新行，
            // 定时任务再也不会推送，大屏 WS Messages 会一直空白。现改为每 5 分钟按当前最新一行推送一次。
            if (tunnelEdgeComputeData != null) {
                // ========== 原代码（已注释）==========
                // 原代码直接使用 tunnelDevicelistTunnelinfo，但如果查询结果为空（返回null）会导致 NullPointerException
                // TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                //         .eq(TunnelDevicelistTunnelinfo::getDevicelistId, tunnelEdgeComputeData.getDevicelistId())
                //         .eq(TunnelDevicelistTunnelinfo::getType, 1));
                // ajaxResult.put("tunnelId", tunnelDevicelistTunnelinfo.getTunnelId());
                // TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(tunnelDevicelistTunnelinfo.getTunnelId());
                // ajaxResult.put("flag", tunnelEdgeComputeData.getTrafficFlow() * tunnelEdgeComputingTerminal.getUmax());
                // WebSocketServer.sendInfo(ajaxResult, String.valueOf(3));
                // id = tunnelEdgeComputeData.getId();
                
                // ========== 新代码 ==========
                // 添加 null 检查，避免 NullPointerException
                TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                        .eq(TunnelDevicelistTunnelinfo::getDevicelistId, tunnelEdgeComputeData.getDevicelistId())
                        .eq(TunnelDevicelistTunnelinfo::getType, 1));
                if (tunnelDevicelistTunnelinfo != null) {
                    TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(tunnelDevicelistTunnelinfo.getTunnelId());
                    if (tunnelEdgeComputingTerminal != null) {
                        // WebSocket 编码器为 HashMapEncoder，且 AjaxResult 基类 put 不写入字段；
                        // 必须用 HashMap 否则前端收不到 tunnelId/flag，随车动画无法触发。
                        int flow = tunnelEdgeComputeData.getTrafficFlow() != null ? tunnelEdgeComputeData.getTrafficFlow() : 0;
                        int umax = tunnelEdgeComputingTerminal.getUmax() != null ? tunnelEdgeComputingTerminal.getUmax() : 1;
                        HashMap<String, Object> wsPayload = new HashMap<>(4);
                        wsPayload.put("tunnelId", tunnelDevicelistTunnelinfo.getTunnelId());
                        wsPayload.put("flag", flow * (long) umax);
                        WebSocketServer.sendInfo(wsPayload, String.valueOf(3));
                    }
                }
            }
        }
        return AjaxResult.success();
    }

    /**
     * 联调：手动向 sid=3 推送一车流帧，不依赖定时任务与边缘库是否有新行。
     */
    @GetMapping("/test/traffic-ws")
    public AjaxResult testTrafficWebSocket(
            @RequestParam Long tunnelId,
            @RequestParam(defaultValue = "800") int flag) throws IOException {
        if (flag <= 0) {
            return AjaxResult.error("flag 须大于 0");
        }
        HashMap<String, Object> wsPayload = new HashMap<>(4);
        wsPayload.put("tunnelId", tunnelId);
        wsPayload.put("flag", flag);
        WebSocketServer.sendInfo(wsPayload, "3");
        return AjaxResult.success(wsPayload);
    }

    //@Scheduled(cron = "0 0/5 * * * ? ")
    //@Scheduled(cron = "0 0 0/2 * * ? ")
    public AjaxResult pushMonitor() throws IOException {

        //模拟每5分钟推送一个数据
        Integer[] num = {3, 4, 10,11,12,13,127,128,132};
        String content = "";
        Random random = new Random();

        int count = random.nextInt(3) + 2;
        Integer tunnelId = num[random.nextInt(num.length)];
        //隧道方向
        TunnelNameResult tunnelDirection = tunnelNameResultService.getById(tunnelId);
        //隧道
        TunnelNameResult tunnel = tunnelNameResultService.getById(tunnelDirection.getParentId());
        //高速公路
        TunnelNameResult tunnelLine = tunnelNameResultService.getById(tunnel.getParentId());
        if (count == 2) {
            String prefix = "[模式切换]";
            content = prefix +  tunnelLine.getTunnelName() + tunnel.getTunnelName() + "("+tunnelDirection.getTunnelName() + ")因检修启动固定调光模式，时间已超过2小时，请确认是否完成检修，完成后请及时切换至智慧调光模式。";
        }
        if (count == 3) {
            String prefix = "[灯具故障]";
            content = prefix + "平台监测到" + tunnelLine.getTunnelName()  + tunnel.getTunnelName()  + "r1-01回路失压故障，请及时安排相关人员处理。";

        }
        if (count == 4) {
            String prefix = "[设备故障]";
            content = prefix + tunnelLine.getTunnelName()  + tunnel.getTunnelName() + "("+tunnelDirection.getTunnelName() + ")" + "已于"+ com.scsdky.common.utils.DateUtils.getYYYYMMDDHHMM() +"检测到桩号为K141的雷达设备故障，请联系相关人员处理。";
        }
        TunnelNotice tunnelNotice = new TunnelNotice();
        tunnelNotice.setContent(content);
        tunnelNotice.setState(count);
        tunnelNotice.setType(count);
        tunnelNotice.setTunnelId(tunnelLine.getId());
        tunnelNoticeService.save(tunnelNotice);
        AjaxResult ajaxResult = new AjaxResult();
        ajaxResult.put("data", content);
        ajaxResult.put("type", count);
        ajaxResult.put("state", count);
        ajaxResult.put("road", tunnelLine.getTunnelName() + tunnel.getTunnelName());
        WebSocketServer.sendInfo(ajaxResult, "all");
        return AjaxResult.success();
    }


    /**
     * 每分钟监测灯具是否有异常停车
     * @return
     * @throws IOException
     */
    @Scheduled(cron = "0 0/1 * * * ? ")
    public AjaxResult getLampsStatus() throws IOException {

        //查询各个边缘下有几个灯具，每个灯具都要检查是否有异常停车及是否连接超时
        List<TunnelLampsEdgeComputing> tunnelLampsEdgeComputings = tunnelLampsEdgeComputingService.list();
        Map<Long, List<TunnelLampsEdgeComputing>> groupedByDevicelistId = tunnelLampsEdgeComputings.stream()
                .collect(Collectors.groupingBy(TunnelLampsEdgeComputing::getDevicelistId));

        for (Map.Entry<Long, List<TunnelLampsEdgeComputing>> longListEntry : groupedByDevicelistId.entrySet()) {
            //通过边缘控制器ID查询隧道ID
            // ========== 原代码（已注释）==========
            // 原代码直接使用 tunnelDevicelistTunnelinfo，但如果查询结果为空（返回null）会导致 NullPointerException
            // TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
            //         .eq(TunnelDevicelistTunnelinfo::getDevicelistId,  longListEntry.getKey())
            //         .eq(TunnelDevicelistTunnelinfo::getType, 1));
            // Long tunnelId = tunnelDevicelistTunnelinfo.getTunnelId();
            // TunnelNameResult tunnelDirection = tunnelNameResultService.getById(tunnelId);
            // TunnelNameResult tunnel = tunnelNameResultService.getById(tunnelDirection.getParentId());
            // TunnelNameResult tunnelLine = tunnelNameResultService.getById(tunnel.getParentId());
            
            // ========== 新代码 ==========
            // 添加 null 检查，避免 NullPointerException
            TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                    .eq(TunnelDevicelistTunnelinfo::getDevicelistId,  longListEntry.getKey())
                    .eq(TunnelDevicelistTunnelinfo::getType, 1));
            if (tunnelDevicelistTunnelinfo == null) {
                continue; // 如果查询结果为空，跳过本次循环
            }
            Long tunnelId = tunnelDevicelistTunnelinfo.getTunnelId();
            //隧道方向
            TunnelNameResult tunnelDirection = tunnelNameResultService.getById(tunnelId);
            if (tunnelDirection == null) {
                continue; // 如果隧道方向为空，跳过本次循环
            }
            //隧道
            TunnelNameResult tunnel = tunnelNameResultService.getById(tunnelDirection.getParentId());
            if (tunnel == null) {
                continue; // 如果隧道为空，跳过本次循环
            }
            //高速公路
            TunnelNameResult tunnelLine = tunnelNameResultService.getById(tunnel.getParentId());
            if (tunnelLine == null) {
                continue; // 如果高速公路为空，跳过本次循环
            }

            List<Map<String,Object>> datas = tunnelTriggerLampsDataService.selectLampsStatus(longListEntry.getValue());
            for (Map<String, Object> data : datas) {
                Integer status = (Integer) data.get("lamps_status");
                //通信状态,如果五分钟后都是异常，那就推送消息
                int communicationState = ConvertBit.computerBit(status, 0);
                if(communicationState != 0 && (Integer) data.get("communication_send") != 1) {
                    //将灯具状态存放在redis中，如果五分钟后还是异常，就发送通知
                    Integer num = redisCache.getCacheObject("lamps:" + data.get("unique_id"));
                    //第一次进来，没有异常，要重新赋值
                    if(num == null){
                        redisCache.setCacheObject("lamps:" + data.get("unique_id"),0);
                    }else if (num == 4) {
                        buildData(data,tunnelDirection,tunnel,tunnelLine,"[灯具异常]","区段灯具终端长时间未连接，请及时检查!",Long.valueOf(data.get("unique_id").toString()),1);
                        TunnelTriggerLampsData tunnelTriggerLampsData = new TunnelTriggerLampsData();
                        tunnelTriggerLampsData.setId((Long) data.get("id"));
                        tunnelTriggerLampsData.setCommunicationSend(1);
                        tunnelTriggerLampsDataService.updateById(tunnelTriggerLampsData);
                        //重置缓存
                        redisCache.setCacheObject("lamps:" + data.get("unique_id"),0);
                    } else{
                        redisCache.setCacheObject("lamps:" + data.get("unique_id"),num + 1);
                    }

                }else{
                    redisCache.setCacheObject("lamps:" + data.get("unique_id"),0);
                }
                //异常提车
                int num = ConvertBit.computerBit(status, 3);
                //如果不等于0，就说明是异常停车，并且判断这条消息是否已经被下发过，没有下发则需要下发到消息列表
                if(num != 0 && (Integer) data.get("message_send") != 1){
                    buildData(data,tunnelDirection,tunnel,tunnelLine,"[异常停车]","区段异常停车，请及时检查!",Long.valueOf(data.get("unique_id").toString()),2);
                    //更新推送状态，防止二次推送
                    TunnelTriggerLampsData tunnelTriggerLampsData = new TunnelTriggerLampsData();
                    tunnelTriggerLampsData.setId((Long) data.get("id"));
                    tunnelTriggerLampsData.setMessageSend(1);
                    tunnelTriggerLampsDataService.updateById(tunnelTriggerLampsData);
                }
            }
        }
        return AjaxResult.success();
    }


    private void buildData(Map<String, Object> data,TunnelNameResult tunnelDirection,TunnelNameResult tunnel,
                           TunnelNameResult tunnelLine,
                           String prefix,
                           String suffix,Long uniqueId,Integer exceptionStatus){
        //通过灯具id查询灯具的里程桩号
        TunnelLampsTerminal tunnelLampsTerminal = tunnelLampsTerminalService.getById((Long) data.get("unique_id"));
        //将数值转为大桩号+小桩号
        Integer deviceNum = tunnelLampsTerminal.getDeviceNum();
        String bigAndSmall = ConvertBit.bigAndSmall(deviceNum);

        TunnelNotice tunnelNotice = new TunnelNotice();
        //检测该消息是否存在
        Long id = selectNoticeExist(uniqueId, exceptionStatus);
        tunnelNotice.setId(id);
        String content = prefix + " 检测到" + bigAndSmall +  suffix;
        tunnelNotice.setContent(content);
        tunnelNotice.setState(3);
        tunnelNotice.setType(3);
        tunnelNotice.setTunnelId(tunnelDirection.getId());
        tunnelNotice.setUniqueIdentification(uniqueId);
        tunnelNotice.setExceptionStatus(exceptionStatus);
        tunnelNotice.setProcess(0);
        tunnelNoticeService.saveOrUpdate(tunnelNotice);
    }


    /**
     * 每两分钟检测边缘是否有异常情况
     * @return
     * @throws IOException
     */
    @Scheduled(cron = "0 0/2 * * * ? ")
    public AjaxResult getEdgeStatus() throws IOException {

        TunnelEdgeTriggerData tunnelEdgeTriggerData = tunnelEdgeTriggerDataService.getOne(new LambdaQueryWrapper<TunnelEdgeTriggerData>()
                .orderByDesc(TunnelEdgeTriggerData::getUploadTime).last("limit 1"));
        if (tunnelEdgeTriggerData == null) {
            return AjaxResult.success(); // 如果没有数据，直接返回
        }
        //通过边缘控制器ID查询隧道ID
        // ========== 原代码（已注释）==========
        // 原代码直接使用 tunnelDevicelistTunnelinfo，但如果查询结果为空（返回null）会导致 NullPointerException
        // TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
        //         .eq(TunnelDevicelistTunnelinfo::getDevicelistId,  tunnelEdgeTriggerData.getDevicelistId())
        //         .eq(TunnelDevicelistTunnelinfo::getType, 1));
        // Long tunnelId = tunnelDevicelistTunnelinfo.getTunnelId();
        // TunnelNameResult tunnelDirection = tunnelNameResultService.getById(tunnelId);
        // TunnelNameResult tunnel = tunnelNameResultService.getById(tunnelDirection.getParentId());
        // TunnelNameResult tunnelLine = tunnelNameResultService.getById(tunnel.getParentId());
        
        // ========== 新代码 ==========
        // 添加 null 检查，避免 NullPointerException
        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getDevicelistId,  tunnelEdgeTriggerData.getDevicelistId())
                .eq(TunnelDevicelistTunnelinfo::getType, 1));
        if (tunnelDevicelistTunnelinfo == null) {
            return AjaxResult.success(); // 如果查询结果为空，直接返回
        }
        Long tunnelId = tunnelDevicelistTunnelinfo.getTunnelId();
        //隧道方向
        TunnelNameResult tunnelDirection = tunnelNameResultService.getById(tunnelId);
        if (tunnelDirection == null) {
            return AjaxResult.success(); // 如果隧道方向为空，直接返回
        }
        //隧道
        TunnelNameResult tunnel = tunnelNameResultService.getById(tunnelDirection.getParentId());
        if (tunnel == null) {
            return AjaxResult.success(); // 如果隧道为空，直接返回
        }
        //高速公路
        TunnelNameResult tunnelLine = tunnelNameResultService.getById(tunnel.getParentId());
        if (tunnelLine == null) {
            return AjaxResult.success(); // 如果高速公路为空，直接返回
        }

        //有六位字节，每位都要表示具体的状态
        String content;
        for (int i = 0; i < 7; i++) {
            //异常提车
            int num = ConvertBit.computerBit(tunnelEdgeTriggerData.getHardwareStatus(), i);
            if(num != 0 && tunnelEdgeTriggerData.getMessageSend()!= 1) {
                TunnelNotice tunnelNotice = new TunnelNotice();
                if(i == 6) {
                    content = "[串口状态]" +  tunnelLine.getTunnelName() + tunnel.getTunnelName() +
                            "("+tunnelDirection.getTunnelName() + ")检测到边缘控制器设备号为:" + tunnelEdgeTriggerData.getDevicelistId() + "的串口状态发现异常，请及时检查!";
                    tunnelNotice.setExceptionStatus(4);
                    Long id = selectNoticeExist(tunnelEdgeTriggerData.getDevicelistId(), 4);
                    tunnelNotice.setId(id);
                }else{
                    content = "[模组状态]" + " 检测到边缘控制器设备号为:" + tunnelEdgeTriggerData.getDevicelistId() + "的"+(i + 1)+"号模组发现异常，请及时检查!";
                    tunnelNotice.setExceptionStatus(3);
                    Long id = selectNoticeExist(tunnelEdgeTriggerData.getDevicelistId(), 3);
                    tunnelNotice.setId(id);
                }
                tunnelNotice.setContent(content);
                tunnelNotice.setState(3);
                tunnelNotice.setType(4);
                tunnelNotice.setTunnelId(tunnelDirection.getId());
                tunnelNotice.setUniqueIdentification(tunnelEdgeTriggerData.getDevicelistId());
                tunnelNotice.setProcess(0);
                tunnelNoticeService.saveOrUpdate(tunnelNotice);
            }
        }
        //更新推送状态，防止二次推送
        tunnelEdgeTriggerData.setMessageSend(1);
        tunnelEdgeTriggerDataService.updateById(tunnelEdgeTriggerData);
        return AjaxResult.success();
    }


    private Long selectNoticeExist(Long uniqueIdentification, Integer exceptionStatus){
        //查询当前边缘是否有消息，没有的话则添加，有则更新
        TunnelNotice notice = tunnelNoticeService.getOne(new LambdaQueryWrapper<TunnelNotice>()
                .eq(TunnelNotice::getUniqueIdentification, uniqueIdentification)
                .eq(TunnelNotice::getExceptionStatus, exceptionStatus).last("limit 1"));
        if(notice != null ){
            return notice.getId();
        }
        return null;
    }




    /**
     * 每五分钟检测边缘是否在线，不在线设置为离线
     * @return
     * @throws IOException
     */
    @Scheduled(cron = "0 0/5 * * * ? ")
    public AjaxResult checkEdgeOnline() throws IOException {

        List<TunnelDevicelist> devicelists = tunnelDevicelistService.list(new LambdaQueryWrapper<TunnelDevicelist>().eq(TunnelDevicelist::getDeviceTypeId,1));

        for (TunnelDevicelist devicelist : devicelists) {
            //如果当前时间和最后一次更新的时间
            TunnelEdgeComputeData  tunnelEdgeComputeData = tunnelEdgeComputeDataService.getOne(new LambdaQueryWrapper<TunnelEdgeComputeData>()
                    .eq(TunnelEdgeComputeData::getDevicelistId, devicelist.getDeviceId())
                    .orderByDesc(TunnelEdgeComputeData::getUploadTime).last("limit 1"));

            if(DateUtils.getSecond(tunnelEdgeComputeData.getUploadTime(),new Date()) > 5) {
                devicelist.setOnline(0);
                tunnelDevicelistService.updateById(devicelist);
            }
        }
        return AjaxResult.success();
    }
}