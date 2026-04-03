package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.scsdky.common.core.domain.entity.SysRole;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.common.utils.SecurityUtils;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.TunnelInfoAndDeviceDto;
import com.scsdky.web.domain.vo.KmlDataVo;
import com.scsdky.web.domain.vo.TunnelInfoAndDeviceVo;
import com.scsdky.web.domain.vo.TunnelNameResultVo;
import com.scsdky.web.mapper.TunnelNameResultMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.ConvertBit;
import com.scsdky.web.utils.KmlUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class TunnelNameResultServiceImpl extends ServiceImpl<TunnelNameResultMapper, TunnelNameResult> implements TunnelNameResultService{
    @Resource
    private KmlUtil kmlUtil;

    @Resource
    private TunnelLongitudeLatitudeService tunnelLongitudeLatitudeService;

    @Resource
    private UserTunnelService userTunnelService;

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelDeviceService tunnelDeviceService;

    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;

    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;

    @Resource
    private TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;

    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;

    @Resource
    private TTunnelDeviceBreakdownInfoService tTunnelDeviceBreakdownInfoService;

    @Resource
    private TunnelSyscmdService tunnelSyscmdService;

    @Override
    public List<TunnelNameResult> getTunnelName(Long userId) {
        List<TunnelNameResult> list = getTunnelNameFlatList(userId);
        return buildTreeStream(list,0L);
    }

    @Override
    public List<TunnelNameResult> getTunnelNameFlatList(Long userId) {
        if (SecurityUtils.isAdmin(userId)) {
            return list();
        }
        return this.getBaseMapper().getTunnelName(userId);
    }

    @Override
    public List<Long> getLevel4TunnelIdsForUser(Long userId) {
        if (SecurityUtils.isAdmin(userId)) {
            return list(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getLevel, 4))
                    .stream().map(TunnelNameResult::getId).collect(Collectors.toList());
        }
        List<Long> ids = this.getBaseMapper().selectLevel4TunnelIdsForUser(userId);
        return ids != null ? ids : Collections.emptyList();
    }

    @Override
    public List<Long> getLevel4TunnelIdsForDeviceStatus(Long userId) {
        if (SecurityUtils.isAdmin(userId)) {
            return list(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getLevel, 4))
                    .stream().map(TunnelNameResult::getId).collect(Collectors.toList());
        }
        List<Long> ids = this.getBaseMapper().selectLevel4TunnelIdsForDeviceStatus(userId);
        return ids != null ? ids : Collections.emptyList();
    }

    @Override
    public String highroadTunnel(Long parentId) {
        StringBuilder sb = new StringBuilder();
        TunnelNameResult tunnelName= getById(parentId);
        TunnelNameResult tunnelName1 = getById(tunnelName.getParentId());
        sb.append(tunnelName1.getTunnelName());
        sb.append("--").append(tunnelName.getTunnelName());
        return sb.toString();
    }

    @Override
    public String uploadKml(MultipartFile file) throws Exception {
        String subfix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
        if(!".kml".equals(subfix)){
            throw new BaseException("请上传kml文件!");
        }
        InputStream inputStream = file.getInputStream();
        kmlUtil.parseXmlWithDom4j(inputStream);
        return "success";
    }

    @Override
    public  List<KmlDataVo> longitudeLatitude(Long tunnelId, Integer isDown) {
        List<KmlDataVo> kmlDataVos = new ArrayList<>();
        List<Long> tunnelIds = new ArrayList<>();
        if(tunnelId == null ){
            Long userId = SecurityUtils.getUserId();
            List<UserTunnel> userTunnels = userTunnelService.list(new LambdaQueryWrapper<UserTunnel>().eq(UserTunnel::getUserId, userId));
            tunnelIds = userTunnels.stream().map(UserTunnel::getTunnelNameId).collect(Collectors.toList());
        }else{
            tunnelIds.add(tunnelId);
        }
        if(isDown == 1 ){
            //查询下级隧道
            List<TunnelNameResult> tunnelNames = tunnelNameResultService.list(new LambdaQueryWrapper<TunnelNameResult>()
                    .eq(TunnelNameResult::getParentId, tunnelId));
            List<Long> tunnelIdList = tunnelNames.stream().map(TunnelNameResult::getId).collect(Collectors.toList());
            tunnelIds.addAll(tunnelIdList);
        }
        List<TunnelLongitudeLatitude> tunnelLongitudeLatitudeList = tunnelLongitudeLatitudeService
                .list(new LambdaQueryWrapper<TunnelLongitudeLatitude>()
                        .in(CollectionUtils.isNotEmpty(tunnelIds),TunnelLongitudeLatitude::getTunnelId,tunnelIds));
        if(CollectionUtils.isNotEmpty(tunnelLongitudeLatitudeList)) {
            Map<Long, List<TunnelLongitudeLatitude>> tunnelNoticesByType = tunnelLongitudeLatitudeList.stream().collect(Collectors.groupingBy(TunnelLongitudeLatitude::getTunnelId));
            for (Map.Entry<Long, List<TunnelLongitudeLatitude>> longListEntry : tunnelNoticesByType.entrySet()) {
                KmlDataVo kmlDataVo = new KmlDataVo();
                List<List<BigDecimal>> result = new ArrayList<>();
                kmlDataVo.setTunnelId(longListEntry.getKey());
                TunnelNameResult tunnel = tunnelNameResultService.getById(longListEntry.getKey());
                kmlDataVo.setTunnelName(tunnel.getTunnelName());
                kmlDataVo.setMileage(tunnel.getTunnelMileage());
                longListEntry.getValue().forEach(tunnelLongitudeLatitude -> {
                    List<BigDecimal> xy = new ArrayList<>();
                    xy.add(new BigDecimal(tunnelLongitudeLatitude.getLongitude()));
                    xy.add(new BigDecimal(tunnelLongitudeLatitude.getLatitude()));
                    result.add(xy);
                });
                kmlDataVo.setXyList(result);
                kmlDataVos.add(kmlDataVo);
            }
        }

        return kmlDataVos;
    }

    @Override
    public Page<TunnelNameResultVo> getTunnelInfo(TunnelNameResult tunnelNameResult) {
        Page<TunnelNameResultVo> result = new Page<>();
        Long userId = SecurityUtils.getUserId();
        SysRole sysRole = SecurityUtils.getLoginUser().getUser().getRoles().get(0);
        //如果是超级管理员,查看全部的数据
        if("admin".equals(sysRole.getRoleKey())) {
            List<TunnelNameResult> tunnelNameResults = baseMapper.selectList(new LambdaQueryWrapper<TunnelNameResult>()
                    .eq(TunnelNameResult::getLevel, 4));
            tunnelNameResults.forEach(tunnelNameResult1 -> {
                //获取隧道信息
                TunnelNameResultVo tunnelNameResultVo = getTunnelnfoMethod(tunnelNameResult1);
                result.add(tunnelNameResultVo);
            });
            int count = tunnelNameResultService.count(new LambdaQueryWrapper<TunnelNameResult>()
                    .eq(TunnelNameResult::getLevel, 4));
            result.setTotal(count);


        }else{
            //不是超级管理员，查看自己分配的隧道
            List<TunnelNameResult> tunnelNameResults = baseMapper.getTunnelListByUserId(userId);
            tunnelNameResults.forEach(tunnelNameResult1 -> {
                //获取隧道信息
                TunnelNameResultVo tunnelNameResultVo = getTunnelnfoMethod(tunnelNameResult1);
                result.add(tunnelNameResultVo);
            });
            result.setTotal(tunnelNameResults.size());
        }

        return result;
    }

    @Override
    public TunnelInfoAndDeviceVo getTunnelDeviceInfoById(Long id) {
        TunnelInfoAndDeviceVo tunnelInfoAndDeviceVo = new TunnelInfoAndDeviceVo();
        TunnelNameResult tunnelNameResult1 = tunnelNameResultService.getById(id);
        //获取隧道信息
        TunnelNameResultVo tunnelNameResultVo = getTunnelnfoMethod(tunnelNameResult1);
        tunnelInfoAndDeviceVo.setTunnelNameResultVo(tunnelNameResultVo);
        //查询设备
        Map<String,List<TunnelDevice>> dataMap = new HashMap<>();
        tunnelInfoAndDeviceVo.setTunnelDevices(dataMap);
        return tunnelInfoAndDeviceVo;
    }

    private void setDeviceStatus(List<TunnelDevice> devices) {
        //查询设备代码表
        List<TTunnelDeviceBreakdownInfo> tunnelDeviceBreakdownInfoList = tTunnelDeviceBreakdownInfoService.list();
        devices.forEach(device -> {
            //安装了雷达
            if(device.getLdWhetherInstall() != null && device.getLdWhetherInstall() == 1) {
                String deviceNum = device.getLdDeviceId().substring(0, 2);
                String deviceBreakDown = device.getLdStatus();
                for (TTunnelDeviceBreakdownInfo tTunnelDeviceBreakdownInfo : tunnelDeviceBreakdownInfoList) {
                    if(tTunnelDeviceBreakdownInfo.getDeivceNum().equals(deviceNum) && tTunnelDeviceBreakdownInfo.getBreakdownNum().equals(deviceBreakDown)) {
                        device.setLdStatus(tTunnelDeviceBreakdownInfo.getBreakdownInfo());
                        break;
                    }
                }
            }
            String deviceNum = device.getDeviceId().substring(0, 2);
            String deviceBreakDown = device.getDeviceStatus();
            for (TTunnelDeviceBreakdownInfo tTunnelDeviceBreakdownInfo : tunnelDeviceBreakdownInfoList) {
                if(tTunnelDeviceBreakdownInfo.getDeivceNum().equals(deviceNum) && tTunnelDeviceBreakdownInfo.getBreakdownNum().equals(deviceBreakDown)) {
                    device.setDeviceStatus(tTunnelDeviceBreakdownInfo.getBreakdownInfo());
                    break;
                }
            }
        });
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTunnelDeviceInfoById(TunnelInfoAndDeviceDto tunnelInfoAndDeviceDto) throws JsonProcessingException {
        TunnelNameResultVo tunnelNameResultVo = tunnelInfoAndDeviceDto.getTunnelNameResultVo();
        //判断当前线路是否存在，存在则不添加
        TunnelNameResult tunnelLineName = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, tunnelNameResultVo.getLineName()));
        if(tunnelLineName != null && !tunnelLineName.getId().equals(tunnelNameResultVo.getLineId())) {
            throw new BaseException("当前线路已存在！请重新输入");
        }
        //判断当前隧道是否存在，存在则不添加
        TunnelNameResult tunnelName = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, tunnelNameResultVo.getTunnelName()));
        if(tunnelName != null && !tunnelName.getId().equals(tunnelNameResultVo.getTunnelId())) {
            throw new BaseException("当前隧道已存在！请重新输入");
        }

        //判断当前隧道方向是否存在，存在则不添加
        assert tunnelName != null;
        TunnelNameResult tunnelNameDirection = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>()
                .eq(TunnelNameResult::getTunnelName, tunnelNameResultVo.getDirection())
                .eq(TunnelNameResult::getParentId,tunnelName.getId()));
        if(tunnelNameDirection != null && !tunnelNameDirection.getId().equals(tunnelNameResultVo.getId())) {
            throw new BaseException("当前隧道方向已存在！请重新输入");
        }
        //更改线路
        TunnelNameResult tunnelNameResultLine = new TunnelNameResult();
        tunnelNameResultLine.setTunnelName(tunnelNameResultVo.getLineName());
        tunnelNameResultLine.setTunnelMileage(tunnelNameResultVo.getLineMileage());
        tunnelNameResultLine.setId(tunnelNameResultVo.getLineId());
        tunnelNameResultService.updateById(tunnelNameResultLine);
        //更改隧道
        TunnelNameResult tunnelNameResultTunnel = new TunnelNameResult();
        tunnelNameResultTunnel.setTunnelName(tunnelNameResultVo.getTunnelName());
        tunnelNameResultTunnel.setTunnelMileage(tunnelNameResultVo.getLineMileageTunnel());
        tunnelNameResultTunnel.setId(tunnelNameResultVo.getTunnelId());
        tunnelNameResultService.updateById(tunnelNameResultTunnel);

        //更改隧道方向
        TunnelNameResult tunnelNameResultDirection = new TunnelNameResult();
        BeanUtils.copyProperties(tunnelNameResultVo,tunnelNameResultDirection);
        tunnelNameResultDirection.setTunnelName(tunnelNameResultVo.getDirection());
        tunnelNameResultDirection.setTunnelMileage(tunnelNameResultVo.getTunnelMileage());
        tunnelNameResultService.updateById(tunnelNameResultDirection);

        //更改设备信息
        List<TunnelDevice> tunnelDevices = tunnelInfoAndDeviceDto.getTunnelDevices();

        //更改中间服务器线路信息
        TunnelDevice tunnelDevice2 = new TunnelDevice();
        tunnelDevice2.setTunnelName(tunnelNameResultVo.getDirection());
        tunnelDevice2.setTunnelId(tunnelNameResultVo.getId());
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getOneObject(tunnelNameResultDirection);
        BeanUtils.copyProperties(tunnelNameResultVo,tunnelEdgeComputingTerminal, "id");

        //将1+100转换为10进制
        tunnelEdgeComputingTerminal.setInMileageNum(ConvertBit.deviceNumConvertValue(tunnelNameResultVo.getInMileageNum()));
        tunnelEdgeComputingTerminal.setOutMileageNum(ConvertBit.deviceNumConvertValue(tunnelNameResultVo.getOutMileageNum()));
        tunnelEdgeComputingTerminal.setBasicStart(ConvertBit.deviceNumConvertValue(tunnelNameResultVo.getBasicStart()));
        tunnelEdgeComputingTerminal.setBasicEnd(ConvertBit.deviceNumConvertValue(tunnelNameResultVo.getBasicEnd()));

        tunnelEdgeComputingTerminalService.updateByIdObject(tunnelEdgeComputingTerminal);
        //更改中间服务器设备信息
        if(CollectionUtils.isNotEmpty(tunnelDevices)) {
            long deviceListId = 0L;
            for (TunnelDevice tunnelDevice : tunnelDevices) {
                if ("边缘控制器".equals(tunnelDevice.getDeviceType())) {
                    tunnelDevicelistService.updateObject(tunnelDevice);
                    deviceListId = Long.parseLong(tunnelDevice.getDeviceId());
                }
            }
            for (TunnelDevice tunnelDevice : tunnelDevices) {
                //更新唐总的灯具控制器
                if (!"边缘控制器".equals(tunnelDevice.getDeviceType())) {
                    if(tunnelDevice.getId() != null && "灯具控制器".equals(tunnelDevice.getDeviceType())) {
                        tunnelLampsTerminalService.updateObject(tunnelDevice);


                        //修改了灯具控制器，需要下发到指令交互表，更新灯具的数据
                        TunnelSyscmd  tunnelSyscmd = new TunnelSyscmd();
                        tunnelSyscmd.setUserid(SecurityUtils.getUserId());
                        tunnelSyscmd.setCmd("DownloadLampListEx");
                        //生成json操作命令
                        ObjectMapper mapper = new ObjectMapper();
                        // 创建一个Map模拟你的对象
                        Map<String, Object> obj = new LinkedHashMap<>();
                        obj.put("Action", "DownloadLampListEx");
                        obj.put("Ext", "");
                        obj.put("Param", "");
                        obj.put("Target", String.valueOf(deviceListId));

                        // 将Map序列化为JSON字符串
                        String json = mapper.writeValueAsString(obj);
                        tunnelSyscmd.setJson(json);
                        tunnelSyscmd.setAddtime(new Date());
                        tunnelSyscmd.setRspstate(0);
                        tunnelSyscmd.setTarget(deviceListId);
                        tunnelSyscmd.setIsrsp(0);
                        tunnelSyscmd.setReaded(0);
                        tunnelSyscmdService.save(tunnelSyscmd);
                    }else{
                        if("灯具控制器".equals(tunnelDevice.getDeviceType())){
                            //查询灯具终端的个数，不能超过256个
                            int count = tunnelLampsEdgeComputingService.count(new LambdaQueryWrapper<TunnelLampsEdgeComputing>()
                                    .eq(TunnelLampsEdgeComputing::getDevicelistId, deviceListId));
                            if(count < 160) {
                                //新增唐总的灯具控制器
                                TunnelLampsTerminal tunnelLampsTerminal = new TunnelLampsTerminal();
                                tunnelLampsTerminal.setDeviceId(Long.valueOf(tunnelDevice.getDeviceId()));
                                tunnelLampsTerminal.setDeviceProperty(tunnelDevice.getDeviceProperty());
                                tunnelLampsTerminalService.saveObject(tunnelLampsTerminal);
                                TunnelLampsEdgeComputing tunnelLampsEdgeComputing = new TunnelLampsEdgeComputing();
                                tunnelLampsEdgeComputing.setDevicelistId(deviceListId);
                                tunnelLampsEdgeComputing.setUniqueId(tunnelLampsTerminal.getUniqueId());
                                tunnelLampsEdgeComputingService.saveObject(tunnelLampsEdgeComputing);
                            }else{
                                throw new BaseException("灯具控制器不能超过256个！，请不要继续添加");
                            }
                        }
                    }
                }

            }

        }
        //保存自己的设备表
        tunnelDeviceService.saveOrUpdateBatch(tunnelDevices);
        //记录下发指令
        tunnelSyscmdService.setCmdData(tunnelInfoAndDeviceDto.getTunnelNameResultVo().getId(),"DownloadTunnelBaseConfig","","",1);
        return true;
    }

    @Override
    public List<TunnelNameResult> getAllTunnelNameTree() {
        List<TunnelNameResult> list = list();
        return buildTreeStream(list, 0L);
    }

    @Override
    public List<TunnelNameResult> getTunnelListByUserId(Long userId) {
        return baseMapper.getTunnelListByUserId(userId);
    }

    @Override
    public List<TunnelNameResult> listLevel1Companies() {
        return list(new LambdaQueryWrapper<TunnelNameResult>()
                .eq(TunnelNameResult::getLevel, 1)
                .orderByAsc(TunnelNameResult::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addLevel1Company(String tunnelName) {
        if (tunnelName == null || tunnelName.trim().isEmpty()) {
            throw new BaseException("公司名称不能为空");
        }
        String name = tunnelName.trim();
        TunnelNameResult dup = getOne(new LambdaQueryWrapper<TunnelNameResult>()
                .eq(TunnelNameResult::getTunnelName, name)
                .eq(TunnelNameResult::getLevel, 1), false);
        if (dup != null) {
            throw new BaseException("该公司名称已存在，请更换名称");
        }
        Date now = new Date();
        TunnelNameResult row = new TunnelNameResult();
        row.setTunnelName(name);
        row.setParentId(0L);
        row.setLevel(1);
        row.setStatus(0);
        row.setTunnelMileage(null);
        row.setCreateTime(now);
        row.setUpdateTime(now);
        save(row);
        return row.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addTunnelTreeNode(TunnelNameResult req) {
        if (req == null || req.getParentId() == null || req.getParentId() <= 0) {
            throw new BaseException("父级节点无效");
        }
        TunnelNameResult parent = getById(req.getParentId());
        if (parent == null) {
            throw new BaseException("父级隧道不存在");
        }
        if (parent.getLevel() == null || parent.getLevel() < 1 || parent.getLevel() > 3) {
            throw new BaseException("仅可在管理单位/高速公路/隧道群下新增子级");
        }
        int expectedLevel = parent.getLevel() + 1;
        if (req.getLevel() == null || !req.getLevel().equals(expectedLevel)) {
            throw new BaseException("层级与父级不匹配");
        }
        String name = req.getTunnelName() == null ? "" : req.getTunnelName().trim();
        if (name.isEmpty()) {
            throw new BaseException("隧道名称不能为空");
        }
        long dup = count(new LambdaQueryWrapper<TunnelNameResult>()
            .eq(TunnelNameResult::getParentId, req.getParentId())
            .eq(TunnelNameResult::getTunnelName, name));
        if (dup > 0) {
            throw new BaseException("同级下已存在同名节点");
        }
        Date now = new Date();
        TunnelNameResult row = new TunnelNameResult();
        row.setTunnelName(name);
        row.setParentId(req.getParentId());
        row.setLevel(req.getLevel());
        row.setTunnelMileage(req.getTunnelMileage());
        row.setStatus(req.getStatus() != null ? req.getStatus() : 0);
        row.setCreateTime(now);
        row.setUpdateTime(now);
        save(row);
        return row.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTunnelTreeNode(TunnelNameResult req) {
        if (req == null || req.getId() == null) {
            throw new BaseException("隧道ID不能为空");
        }
        TunnelNameResult existing = getById(req.getId());
        if (existing == null) {
            throw new BaseException("隧道不存在");
        }
        String name = req.getTunnelName() == null ? "" : req.getTunnelName().trim();
        if (name.isEmpty()) {
            throw new BaseException("隧道名称不能为空");
        }
        long dup = count(new LambdaQueryWrapper<TunnelNameResult>()
            .eq(TunnelNameResult::getParentId, existing.getParentId())
            .eq(TunnelNameResult::getTunnelName, name)
            .ne(TunnelNameResult::getId, req.getId()));
        if (dup > 0) {
            throw new BaseException("同级下已存在同名节点");
        }
        Integer newStatus = req.getStatus() != null ? req.getStatus() : existing.getStatus();
        boolean statusChanged = !Objects.equals(existing.getStatus(), newStatus);
        existing.setTunnelName(name);
        existing.setTunnelMileage(req.getTunnelMileage());
        existing.setStatus(newStatus);
        existing.setUpdateTime(new Date());
        if (!updateById(existing)) {
            return false;
        }
        if (statusChanged) {
            cascadeTunnelStatusDescendants(existing.getId(), newStatus);
        }
        return true;
    }

    private void cascadeTunnelStatusDescendants(Long parentId, int status) {
        List<TunnelNameResult> children = list(new LambdaQueryWrapper<TunnelNameResult>()
            .eq(TunnelNameResult::getParentId, parentId));
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        Date now = new Date();
        for (TunnelNameResult c : children) {
            c.setStatus(status);
            c.setUpdateTime(now);
            updateById(c);
            cascadeTunnelStatusDescendants(c.getId(), status);
        }
    }

    private TunnelNameResultVo getTunnelnfoMethod(TunnelNameResult tunnelNameResult1) {
        TunnelNameResult tunnelName = tunnelNameResultService.getById(tunnelNameResult1.getParentId());
        TunnelNameResult tunnelNameLine = tunnelNameResultService.getById(tunnelName.getParentId());

        //通过设备号查询隧道参数
        TunnelEdgeComputingTerminal tunnelEdgeComputingTerminal = tunnelEdgeComputingTerminalService.getById(tunnelNameResult1.getId());

        TunnelDevicelistTunnelinfo tunnelDevicelistTunnelinfo = tunnelDevicelistTunnelinfoService.getOne(new LambdaQueryWrapper<TunnelDevicelistTunnelinfo>()
                .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelNameResult1.getId())
                .eq(TunnelDevicelistTunnelinfo::getType,1));
        if(tunnelEdgeComputingTerminal != null && tunnelDevicelistTunnelinfo != null) {
            TunnelNameResultVo tunnelNameResultVo = new TunnelNameResultVo();
            BeanUtils.copyProperties(tunnelEdgeComputingTerminal,tunnelNameResultVo);
            tunnelNameResultVo.setLineName(tunnelNameLine.getTunnelName());
            tunnelNameResultVo.setLineId(tunnelNameLine.getId());
            tunnelNameResultVo.setLineMileage(tunnelNameLine.getTunnelMileage());
            tunnelNameResultVo.setTunnelName(tunnelName.getTunnelName());
            tunnelNameResultVo.setTunnelId(tunnelName.getId());
            tunnelNameResultVo.setLineMileageTunnel(tunnelName.getTunnelMileage());
            tunnelNameResultVo.setDirection(tunnelNameResult1.getTunnelName());
            tunnelNameResultVo.setDeviceListId(tunnelDevicelistTunnelinfo.getDevicelistId());

            tunnelNameResultVo.setInMileageNum(ConvertBit.bigAndSmall(tunnelEdgeComputingTerminal.getInMileageNum()));
            tunnelNameResultVo.setOutMileageNum(ConvertBit.bigAndSmall(tunnelEdgeComputingTerminal.getOutMileageNum()));
            tunnelNameResultVo.setBasicStart(ConvertBit.bigAndSmall(tunnelEdgeComputingTerminal.getBasicStart()));
            tunnelNameResultVo.setBasicEnd(ConvertBit.bigAndSmall(tunnelEdgeComputingTerminal.getBasicEnd()));
            return tunnelNameResultVo;
        }
        return new TunnelNameResultVo();
    }


    /**
     * 使用stream流转换树结构
     * @param treeList
     * @param id
     * @return
     */
    public static List<TunnelNameResult> buildTreeStream(List<TunnelNameResult> treeList, long id){
        return treeList.stream()
                //过滤父节点与传递的id相同的TreeNode对象
                .filter( treeNode -> treeNode.getParentId() == id )
                .peek(treeNode -> {
                    //递归设置孩子节点
                    treeNode.setChildren(buildTreeStream(treeList,treeNode.getId()));
                })
                .collect(Collectors.toList());
    }

}




