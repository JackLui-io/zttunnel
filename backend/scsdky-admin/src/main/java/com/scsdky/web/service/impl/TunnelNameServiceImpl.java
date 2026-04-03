package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.common.exception.base.BaseException;
import com.scsdky.common.utils.SecurityUtils;
import com.scsdky.web.domain.*;
import com.scsdky.web.domain.dto.TunnelInfoAndDeviceDto;
import com.scsdky.web.domain.dto.TunnelNameResultExcel;
import com.scsdky.web.domain.vo.KmlDataVo;
import com.scsdky.web.domain.vo.TunnelInfoAndDeviceVo;
import com.scsdky.web.domain.vo.TunnelNameResultVo;
import com.scsdky.web.mapper.TunnelDeviceMapper;
import com.scsdky.web.service.*;
import com.scsdky.web.utils.KmlUtil;
import com.scsdky.web.mapper.TunnelNameMapper;
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
 * @author tubo
 */
@Service
public class TunnelNameServiceImpl extends ServiceImpl<TunnelNameMapper, TunnelName> implements TunnelNameService {

    @Resource
    private KmlUtil kmlUtil;

    @Resource
    private TunnelLongitudeLatitudeService tunnelLongitudeLatitudeService;

    @Resource
    private UserTunnelService userTunnelService;

    @Resource
    private TunnelNameService tTunnelNameService;

    @Resource
    private TunnelNameResultService tunnelNameResultService;

    @Resource
    private TunnelDeviceService tunnelDeviceService;

    @Override
    public List<TunnelName> getTunnelName(Long userId) {
        List<TunnelName> list = this.getBaseMapper().getTunnelName(userId);
        return buildTreeStream(list,0L);
    }

    @Override
    public String highroadTunnel(Long parentId) {
        StringBuilder sb = new StringBuilder();
        TunnelName tunnelName= getById(parentId);
        TunnelName tunnelName1 = getById(tunnelName.getParentId());
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
    public  List<KmlDataVo> longitudeLatitude(Long tunnelId,Integer isDown) {
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
            List<TunnelName> tunnelNames = tTunnelNameService.list(new LambdaQueryWrapper<TunnelName>()
                    .eq(TunnelName::getParentId, tunnelId));
            List<Long> tunnelIdList = tunnelNames.stream().map(TunnelName::getId).collect(Collectors.toList());
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
                TunnelName tunnel = tTunnelNameService.getById(longListEntry.getKey());
                kmlDataVo.setTunnelName(tunnel.getTunnelName());
                //kmlDataVo.setMileage(tunnel.getMileage());
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
    public List<TunnelNameResultVo> getTunnelInfo(TunnelNameResult tunnelNameResult) {
        List<TunnelNameResultVo> result = new ArrayList<>();
        List<TunnelNameResult> tunnelNameResults = tunnelNameResultService.list(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getLevel, 4));
        tunnelNameResults.forEach(tunnelNameResult1 -> {
            //获取隧道信息
            TunnelNameResultVo tunnelNameResultVo = getTunnelnfoMethod(tunnelNameResult1);
            result.add(tunnelNameResultVo);
        });
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
        //1.边缘控制器
        List<TunnelDevice> byDevice = tunnelDeviceService.list(new LambdaQueryWrapper<TunnelDevice>().eq(TunnelDevice::getTunnelId, id).eq(TunnelDevice::getDeviceType, "边缘控制器"));
        dataMap.put("bianyuan",byDevice);
        List<TunnelDevice> ldDevice = tunnelDeviceService.list(new LambdaQueryWrapper<TunnelDevice>().eq(TunnelDevice::getTunnelId, id).likeRight(TunnelDevice::getDeviceType, "洞外雷达"));
        dataMap.put("leida",ldDevice);
        List<TunnelDevice> dbDevice = tunnelDeviceService.list(new LambdaQueryWrapper<TunnelDevice>().eq(TunnelDevice::getTunnelId, id).likeRight(TunnelDevice::getDeviceType, "电表"));
        dataMap.put("dianbiao",dbDevice);
        List<TunnelDevice> djDevice = tunnelDeviceService.list(new LambdaQueryWrapper<TunnelDevice>().eq(TunnelDevice::getTunnelId, id).likeRight(TunnelDevice::getDeviceType, "灯具控制器"));
        dataMap.put("dengju",djDevice);
        tunnelInfoAndDeviceVo.setTunnelDevices(dataMap);
        return tunnelInfoAndDeviceVo;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTunnelDeviceInfoById(TunnelInfoAndDeviceDto tunnelInfoAndDeviceDto) {
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
        TunnelNameResult tunnelNameDirection = tunnelNameResultService.getOne(new LambdaQueryWrapper<TunnelNameResult>().eq(TunnelNameResult::getTunnelName, tunnelNameResultVo.getDirection()));
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
        tunnelDeviceService.saveOrUpdateBatch(tunnelDevices);
        return true;
    }





    private TunnelNameResultVo getTunnelnfoMethod(TunnelNameResult tunnelNameResult1) {
        TunnelNameResult tunnelName = tunnelNameResultService.getById(tunnelNameResult1.getParentId());
        TunnelNameResult tunnelNameLine = tunnelNameResultService.getById(tunnelName.getParentId());

        TunnelNameResultVo tunnelNameResultVo = new TunnelNameResultVo();
        BeanUtils.copyProperties(tunnelNameResult1,tunnelNameResultVo);
        tunnelNameResultVo.setLineName(tunnelNameLine.getTunnelName());
        tunnelNameResultVo.setLineId(tunnelNameLine.getId());
        tunnelNameResultVo.setLineMileage(tunnelNameLine.getTunnelMileage());
        tunnelNameResultVo.setTunnelName(tunnelName.getTunnelName());
        tunnelNameResultVo.setTunnelId(tunnelName.getId());
        tunnelNameResultVo.setLineMileageTunnel(tunnelName.getTunnelMileage());
        tunnelNameResultVo.setDirection(tunnelNameResult1.getTunnelName());
        return tunnelNameResultVo;
    }


    /**
     * 使用stream流转换树结构
     * @param treeList
     * @param id
     * @return
     */
    public static List<TunnelName> buildTreeStream(List<TunnelName> treeList, long id){
        List<TunnelName> list = treeList.stream()
                //过滤父节点与传递的id相同的TreeNode对象
                .filter( treeNode -> treeNode.getParentId() == id )
                .map( treeNode -> {
                    //递归设置孩子节点
                    treeNode.setChildren(buildTreeStream(treeList,treeNode.getId()));
                    return treeNode;
                })
                .collect(Collectors.toList());
        return list;
    }
}




