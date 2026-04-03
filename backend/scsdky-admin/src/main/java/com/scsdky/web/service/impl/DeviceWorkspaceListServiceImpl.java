package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scsdky.common.constant.HttpStatus;
import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.common.utils.poi.ExcelUtil;
import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.scsdky.web.domain.dto.DeviceWorkspaceQueryDto;
import com.scsdky.web.domain.vo.TunnelDevicelistVo;
import com.scsdky.web.enums.DeviceTypeEnum;
import com.scsdky.web.service.DeviceWorkspaceListService;
import com.scsdky.web.service.TunnelDevicelistService;
import com.scsdky.web.service.TunnelDeviceService;
import com.scsdky.web.service.TunnelLampsTerminalService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceWorkspaceListServiceImpl implements DeviceWorkspaceListService {

    private static final int EDGE_TYPE = 1;
    private static final int POWER_TYPE = 2;

    @Resource
    private TunnelDevicelistService tunnelDevicelistService;
    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;
    @Resource
    private TunnelDeviceService tunnelDeviceService;

    @Override
    public List<?> listMerged(DeviceWorkspaceQueryDto dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getTunnelIds())) {
            return Collections.emptyList();
        }
        String deviceType = dto.getDeviceType();
        String kw = dto.getKeyword();
        List<Long> tunnelIds = dto.getTunnelIds();

        if (DeviceTypeEnum.BYKZQ.getValue().equals(deviceType)) {
            return filterDevicelistVo(tunnelDevicelistService.listDevicelistByTunnelIds(tunnelIds, EDGE_TYPE), kw);
        }
        if (DeviceTypeEnum.DNZD.getValue().equals(deviceType)) {
            return filterDevicelistVo(tunnelDevicelistService.listDevicelistByTunnelIds(tunnelIds, POWER_TYPE), kw);
        }
        if (DeviceTypeEnum.DJZD.getValue().equals(deviceType)) {
            return tunnelLampsTerminalService.listDeviceLampByTunnelIds(tunnelIds, kw);
        }
        if (DeviceTypeEnum.LD.getValue().equals(deviceType) || DeviceTypeEnum.DWLD.getValue().equals(deviceType)) {
            return listTunnelDeviceByTunnels(tunnelIds, deviceType, kw);
        }
        throw new ServiceException("不支持的设备类型：" + deviceType);
    }

    @Override
    public TableDataInfo page(DeviceWorkspaceQueryDto dto) {
        List<?> all = listMerged(dto);
        int total = all.size();
        int pageNum = dto.getPageNum() == null ? 1 : dto.getPageNum();
        int pageSize = dto.getPageSize() == null ? 10 : dto.getPageSize();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        int to = Math.min(from + pageSize, total);
        List<?> slice = from >= total ? Collections.emptyList() : new ArrayList<>(all.subList(from, to));

        TableDataInfo rsp = new TableDataInfo();
        rsp.setCode(HttpStatus.SUCCESS);
        rsp.setMsg("查询成功");
        rsp.setRows(slice);
        rsp.setTotal(total);
        return rsp;
    }

    @Override
    public void export(HttpServletResponse response, DeviceWorkspaceQueryDto dto) {
        List<?> all = listMerged(dto);
        String deviceType = dto.getDeviceType();
        if (DeviceTypeEnum.BYKZQ.getValue().equals(deviceType) || DeviceTypeEnum.DNZD.getValue().equals(deviceType)) {
            @SuppressWarnings("unchecked")
            List<TunnelDevicelistVo> rows = (List<TunnelDevicelistVo>) all;
            ExcelUtil<TunnelDevicelistVo> util = new ExcelUtil<>(TunnelDevicelistVo.class);
            util.exportExcel(response, rows, deviceType + "列表");
            return;
        }
        if (DeviceTypeEnum.DJZD.getValue().equals(deviceType)) {
            @SuppressWarnings("unchecked")
            List<TunnelLampsTerminal> rows = (List<TunnelLampsTerminal>) all;
            ExcelUtil<TunnelLampsTerminal> util = new ExcelUtil<>(TunnelLampsTerminal.class);
            util.exportExcel(response, rows, deviceType + "列表");
            return;
        }
        if (DeviceTypeEnum.LD.getValue().equals(deviceType) || DeviceTypeEnum.DWLD.getValue().equals(deviceType)) {
            @SuppressWarnings("unchecked")
            List<TunnelDevice> rows = (List<TunnelDevice>) all;
            ExcelUtil<TunnelDevice> util = new ExcelUtil<>(TunnelDevice.class);
            util.exportExcel(response, rows, deviceType + "列表");
            return;
        }
        throw new ServiceException("不支持的设备类型：" + deviceType);
    }

    private List<TunnelDevice> listTunnelDeviceByTunnels(List<Long> tunnelIds, String deviceType, String keyword) {
        LambdaQueryWrapper<TunnelDevice> w = new LambdaQueryWrapper<>();
        w.in(TunnelDevice::getTunnelId, tunnelIds);
        w.eq(TunnelDevice::getDeviceType, deviceType);
        if (StringUtils.isNotEmpty(keyword)) {
            String k = keyword.trim();
            w.and(q -> q.like(TunnelDevice::getLoopNumber, k)
                .or().like(TunnelDevice::getDeviceNum, k)
                .or().like(TunnelDevice::getDeviceId, k)
                .or().like(TunnelDevice::getDeviceStatus, k));
        }
        return tunnelDeviceService.list(w);
    }

    private List<TunnelDevicelistVo> filterDevicelistVo(List<TunnelDevicelistVo> list, String keyword) {
        if (CollectionUtils.isEmpty(list)) {
            return list == null ? Collections.emptyList() : list;
        }
        if (StringUtils.isEmpty(keyword)) {
            return list;
        }
        String k = keyword.trim();
        return list.stream().filter(vo -> matchDevicelist(vo, k)).collect(Collectors.toList());
    }

    private boolean matchDevicelist(TunnelDevicelistVo vo, String k) {
        if (vo == null) {
            return false;
        }
        if (vo.getDeviceId() != null && vo.getDeviceId().toString().contains(k)) {
            return true;
        }
        if (vo.getNickName() != null && vo.getNickName().contains(k)) {
            return true;
        }
        if (vo.getLoopNumber() != null && vo.getLoopNumber().contains(k)) {
            return true;
        }
        if (vo.getDeviceNum() != null && vo.getDeviceNum().toString().contains(k)) {
            return true;
        }
        if (vo.getDeviceStatus() != null && vo.getDeviceStatus().contains(k)) {
            return true;
        }
        return false;
    }
}
