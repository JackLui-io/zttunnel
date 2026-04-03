package com.scsdky.web.service.impl;

import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.web.domain.TunnelApproachLampsTerminal;
import com.scsdky.web.domain.TunnelDevicelist;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelDeviceParam;
import com.scsdky.web.domain.TunnelEdgeComputingTerminal;
import com.scsdky.web.domain.TunnelLampsEdgeComputing;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.scsdky.web.domain.TunnelLampsTerminalNode;
import com.scsdky.web.domain.TunnelLongitudeLatitude;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.TunnelOutOfRadar;
import com.scsdky.web.domain.TunnelPowerEdgeComputing;
import com.scsdky.web.domain.dto.TunnelCopyRequestDto;
import com.scsdky.web.domain.vo.TunnelCopyResultVo;
import com.scsdky.web.service.TunnelApproachLampsTerminalService;
import com.scsdky.web.service.TunnelCopyService;
import com.scsdky.web.service.TunnelDevicelistService;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.service.TunnelDeviceParamService;
import com.scsdky.web.service.TunnelDeviceService;
import com.scsdky.web.service.TunnelEdgeComputingTerminalService;
import com.scsdky.web.service.TunnelLampsEdgeComputingService;
import com.scsdky.web.service.TunnelLampsTerminalNodeService;
import com.scsdky.web.service.TunnelLampsTerminalService;
import com.scsdky.web.service.TunnelLongitudeLatitudeService;
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.service.TunnelOutOfRadarService;
import com.scsdky.web.service.TunnelPowerEdgeComputingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 复制隧道群（level=3）：新 L3 + 新 L4，并逐表复制与 level-4 关联的设备与参数数据。
 * device_id 规则与 zttunnel-admin log.md「复制规则」一致：边缘/电能用 9916 十二位占位；灯具、洞外雷达、t_tunnel_device 保留源 device_id。
 * 不复制：tunnel_syscmd（保存指令副作用，由业务操作产生）。
 */
@Service
public class TunnelCopyServiceImpl implements TunnelCopyService {

    private static final int LEVEL_GROUP = 3;
    private static final int LEVEL_LINE = 4;
    private static final int DEVTYPE_EDGE = 1;
    private static final int DEVTYPE_POWER = 2;
    /** 与 log 约定一致：边缘/电能占位 device_id 991600000000～991699999999，共用去重 */
    private static final long PLACEHOLDER_9916_MIN = 991600000000L;
    private static final long PLACEHOLDER_9916_MAX = 991699999999L;
    private static final int MAX_ID_TRIES = 80;

    @Resource
    private TunnelNameResultService tunnelNameResultService;
    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;
    @Resource
    private TunnelDevicelistService tunnelDevicelistService;
    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;
    @Resource
    private TunnelDeviceParamService tunnelDeviceParamService;
    @Resource
    private TunnelPowerEdgeComputingService tunnelPowerEdgeComputingService;
    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;
    @Resource
    private TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;
    @Resource
    private TunnelLampsTerminalNodeService tunnelLampsTerminalNodeService;
    @Resource
    private TunnelOutOfRadarService tunnelOutOfRadarService;
    @Resource
    private TunnelDeviceService tunnelDeviceService;
    @Resource
    private TunnelApproachLampsTerminalService tunnelApproachLampsTerminalService;
    @Resource
    private TunnelLongitudeLatitudeService tunnelLongitudeLatitudeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TunnelCopyResultVo copyTunnelGroup(TunnelCopyRequestDto dto) {
        String newName = StringUtils.trim(dto.getNewTunnelName());
        if (StringUtils.isEmpty(newName)) {
            throw new ServiceException("新隧道名称不能为空");
        }

        TunnelNameResult source = tunnelNameResultService.getById(dto.getSourceTunnelGroupId());
        if (source == null) {
            throw new ServiceException("源隧道群不存在");
        }
        if (source.getLevel() == null || source.getLevel() != LEVEL_GROUP) {
            throw new ServiceException("只能复制隧道群（level=3）");
        }

        long sameName = tunnelNameResultService.lambdaQuery()
            .eq(TunnelNameResult::getParentId, source.getParentId())
            .eq(TunnelNameResult::getLevel, LEVEL_GROUP)
            .eq(TunnelNameResult::getTunnelName, newName)
            .count();
        if (sameName > 0) {
            throw new ServiceException("同级高速下已存在同名隧道群，请修改名称");
        }

        Date now = new Date();
        TunnelNameResult newGroup = TunnelNameResult.builder()
            .tunnelName(newName)
            .parentId(source.getParentId())
            .level(LEVEL_GROUP)
            .tunnelMileage(source.getTunnelMileage())
            .status(source.getStatus() != null ? source.getStatus() : 0)
            .createTime(now)
            .updateTime(now)
            .build();
        if (!tunnelNameResultService.save(newGroup)) {
            throw new ServiceException("保存新隧道群失败");
        }

        List<TunnelNameResult> children = tunnelNameResultService.lambdaQuery()
            .eq(TunnelNameResult::getParentId, source.getId())
            .eq(TunnelNameResult::getLevel, LEVEL_LINE)
            .orderByAsc(TunnelNameResult::getId)
            .list();

        List<Long> newLevel4Ids = new ArrayList<>();
        for (TunnelNameResult child : children) {
            TunnelNameResult nc = TunnelNameResult.builder()
                .tunnelName(child.getTunnelName())
                .parentId(newGroup.getId())
                .level(LEVEL_LINE)
                .tunnelMileage(child.getTunnelMileage())
                .status(child.getStatus() != null ? child.getStatus() : 0)
                .createTime(now)
                .updateTime(now)
                .build();
            if (!tunnelNameResultService.save(nc)) {
                throw new ServiceException("复制左右线节点失败");
            }
            newLevel4Ids.add(nc.getId());
            copyLevel4Detail(child.getId(), nc.getId(), child.getTunnelName(), now);
        }

        return TunnelCopyResultVo.builder()
            .newTunnelGroupId(newGroup.getId())
            .newLevel4TunnelIds(newLevel4Ids)
            .firstLevel4TunnelId(newLevel4Ids.isEmpty() ? null : newLevel4Ids.get(0))
            .build();
    }

    /**
     * 复制单条 level-4 下的：隧道参数主表、devicelist、灯具、洞外、电表行、tunnel_device、引道灯、经纬度。
     */
    private void copyLevel4Detail(Long oldTunnelId, Long newTunnelId, String lineName, Date now) {
        copyTunnelEdgeComputingTerminal(oldTunnelId, newTunnelId, lineName);
        Map<Long, Long> devOldToNew = copyDevicelistBlock(oldTunnelId, newTunnelId);
        copyDeviceParams(devOldToNew);
        copyPowerMeters(devOldToNew);
        Long oldEdge = findDevIdByType(oldTunnelId, DEVTYPE_EDGE);
        Long newEdge = oldEdge == null ? null : devOldToNew.get(oldEdge);
        if (oldEdge != null && newEdge != null) {
            copyLampsBlock(oldEdge, newEdge, now);
            copyOutOfRadarBlock(oldEdge, newEdge, now);
        }
        copyTunnelDevices(oldTunnelId, newTunnelId, lineName, now);
        copyApproachLamps(oldTunnelId, newTunnelId, now);
        copyLongitudeLatitudes(oldTunnelId, newTunnelId, lineName, now);
    }

    private void copyTunnelEdgeComputingTerminal(Long oldTunnelId, Long newTunnelId, String lineName) {
        TunnelEdgeComputingTerminal old = tunnelEdgeComputingTerminalService.getById(oldTunnelId);
        if (old == null) {
            return;
        }
        TunnelEdgeComputingTerminal n = new TunnelEdgeComputingTerminal();
        BeanUtils.copyProperties(old, n, "id", "tunnelId", "tunnelName");
        n.setId(newTunnelId);
        n.setTunnelId(newTunnelId);
        n.setTunnelName(lineName);
        tunnelEdgeComputingTerminalService.save(n);
    }

    /**
     * 复制 tunnel_devicelist + tunnel_devicelist_tunnelinfo，返回旧设备号 -> 新设备号。
     */
    private Map<Long, Long> copyDevicelistBlock(Long oldTunnelId, Long newTunnelId) {
        List<TunnelDevicelistTunnelinfo> links = tunnelDevicelistTunnelinfoService.lambdaQuery()
            .eq(TunnelDevicelistTunnelinfo::getTunnelId, oldTunnelId)
            .list();
        Map<Long, Long> map = new HashMap<>();
        for (TunnelDevicelistTunnelinfo link : links) {
            Long oldDid = link.getDevicelistId();
            if (oldDid == null || map.containsKey(oldDid)) {
                continue;
            }
            TunnelDevicelist oldDev = tunnelDevicelistService.getById(oldDid);
            if (oldDev == null) {
                continue;
            }
            TunnelDevicelist nd = new TunnelDevicelist();
            BeanUtils.copyProperties(oldDev, nd, "deviceId");
            long nid = nextDevicelistIdForCopy(oldDev);
            nd.setDeviceId(nid);
            nd.setLastUpdate(new Date());
            tunnelDevicelistService.save(nd);

            TunnelDevicelistTunnelinfo ni = new TunnelDevicelistTunnelinfo();
            ni.setTunnelId(newTunnelId);
            ni.setDevicelistId(nid);
            ni.setType(link.getType());
            tunnelDevicelistTunnelinfoService.save(ni);
            map.put(oldDid, nid);
        }
        return map;
    }

    private void copyDeviceParams(Map<Long, Long> devOldToNew) {
        for (Map.Entry<Long, Long> e : devOldToNew.entrySet()) {
            List<TunnelDeviceParam> list = tunnelDeviceParamService.lambdaQuery()
                .eq(TunnelDeviceParam::getDevicelistId, e.getKey())
                .list();
            for (TunnelDeviceParam p : list) {
                TunnelDeviceParam np = new TunnelDeviceParam();
                BeanUtils.copyProperties(p, np, "id", "devicelistId");
                np.setDevicelistId(e.getValue());
                tunnelDeviceParamService.save(np);
            }
        }
    }

    private void copyPowerMeters(Map<Long, Long> devOldToNew) {
        for (Map.Entry<Long, Long> e : devOldToNew.entrySet()) {
            TunnelDevicelist oldDev = tunnelDevicelistService.getById(e.getKey());
            if (oldDev == null || oldDev.getDeviceTypeId() == null || oldDev.getDeviceTypeId() != DEVTYPE_POWER) {
                continue;
            }
            Long newPowerId = e.getValue();
            List<TunnelPowerEdgeComputing> rows = tunnelPowerEdgeComputingService.lambdaQuery()
                .eq(TunnelPowerEdgeComputing::getDevicelistId, e.getKey())
                .list();
            for (TunnelPowerEdgeComputing row : rows) {
                TunnelPowerEdgeComputing n = new TunnelPowerEdgeComputing();
                BeanUtils.copyProperties(row, n, "uniqueId");
                n.setDevicelistId(newPowerId);
                tunnelPowerEdgeComputingService.save(n);
            }
        }
    }

    private Long findDevIdByType(Long tunnelId, int type) {
        TunnelDevicelistTunnelinfo one = tunnelDevicelistTunnelinfoService.lambdaQuery()
            .eq(TunnelDevicelistTunnelinfo::getTunnelId, tunnelId)
            .eq(TunnelDevicelistTunnelinfo::getType, type)
            .last("LIMIT 1")
            .one();
        return one == null ? null : one.getDevicelistId();
    }

    private void copyLampsBlock(Long oldEdgeDevId, Long newEdgeDevId, Date now) {
        List<TunnelLampsEdgeComputing> rels = tunnelLampsEdgeComputingService.lambdaQuery()
            .eq(TunnelLampsEdgeComputing::getDevicelistId, oldEdgeDevId)
            .list();
        for (TunnelLampsEdgeComputing rel : rels) {
            TunnelLampsTerminal lamp = tunnelLampsTerminalService.getById(rel.getUniqueId());
            if (lamp == null) {
                continue;
            }
            TunnelLampsTerminal nl = new TunnelLampsTerminal();
            // 约定：灯具复制保留源 device_id（全局冲突时由库约束暴露，人工处理）
            BeanUtils.copyProperties(lamp, nl, "uniqueId", "updateTime");
            nl.setUpdateTime(now);
            tunnelLampsTerminalService.save(nl);

            TunnelLampsEdgeComputing nrel = new TunnelLampsEdgeComputing();
            nrel.setUniqueId(nl.getUniqueId());
            nrel.setDevicelistId(newEdgeDevId);
            tunnelLampsEdgeComputingService.save(nrel);

            List<TunnelLampsTerminalNode> nodes = tunnelLampsTerminalNodeService.lambdaQuery()
                .eq(TunnelLampsTerminalNode::getUniqueId, rel.getUniqueId())
                .list();
            for (TunnelLampsTerminalNode node : nodes) {
                TunnelLampsTerminalNode nn = new TunnelLampsTerminalNode();
                BeanUtils.copyProperties(node, nn, "id", "uniqueId");
                nn.setUniqueId(nl.getUniqueId());
                tunnelLampsTerminalNodeService.save(nn);
            }
        }
    }

    private void copyOutOfRadarBlock(Long oldEdgeDevId, Long newEdgeDevId, Date now) {
        List<TunnelOutOfRadar> rows = tunnelOutOfRadarService.lambdaQuery()
            .eq(TunnelOutOfRadar::getDevicelistId, oldEdgeDevId)
            .list();
        for (TunnelOutOfRadar r : rows) {
            TunnelOutOfRadar nr = new TunnelOutOfRadar();
            BeanUtils.copyProperties(r, nr, "id", "devicelistId", "createTime");
            nr.setDevicelistId(newEdgeDevId);
            nr.setCreateTime(now);
            tunnelOutOfRadarService.save(nr);
        }
    }

    private void copyTunnelDevices(Long oldTunnelId, Long newTunnelId, String lineName, Date now) {
        List<TunnelDevice> rows = tunnelDeviceService.lambdaQuery()
            .eq(TunnelDevice::getTunnelId, oldTunnelId)
            .list();
        for (TunnelDevice d : rows) {
            TunnelDevice nd = new TunnelDevice();
            // 约定：t_tunnel_device 复制保留源 device_id
            BeanUtils.copyProperties(d, nd, "id", "tunnelId");
            nd.setTunnelId(newTunnelId);
            if (StringUtils.isNotEmpty(lineName)) {
                nd.setTunnelName(lineName);
            }
            nd.setCreateTime(now);
            nd.setUpdateTime(now);
            tunnelDeviceService.save(nd);
        }
    }

    private void copyApproachLamps(Long oldTunnelId, Long newTunnelId, Date now) {
        List<TunnelApproachLampsTerminal> rows = tunnelApproachLampsTerminalService.lambdaQuery()
            .eq(TunnelApproachLampsTerminal::getTunnelId, oldTunnelId)
            .list();
        for (TunnelApproachLampsTerminal a : rows) {
            TunnelApproachLampsTerminal na = new TunnelApproachLampsTerminal();
            BeanUtils.copyProperties(a, na, "id");
            na.setTunnelId(newTunnelId);
            na.setLastUpdate(LocalDateTime.now());
            tunnelApproachLampsTerminalService.save(na);
        }
    }

    private void copyLongitudeLatitudes(Long oldTunnelId, Long newTunnelId, String lineName, Date now) {
        List<TunnelLongitudeLatitude> rows = tunnelLongitudeLatitudeService.lambdaQuery()
            .eq(TunnelLongitudeLatitude::getTunnelId, oldTunnelId)
            .list();
        for (TunnelLongitudeLatitude row : rows) {
            TunnelLongitudeLatitude n = new TunnelLongitudeLatitude();
            BeanUtils.copyProperties(row, n, "id", "tunnelId", "tunnelName", "createTime", "updateTime");
            n.setTunnelId(newTunnelId);
            if (StringUtils.isNotEmpty(lineName)) {
                n.setTunnelName(lineName);
            }
            n.setCreateTime(now);
            n.setUpdateTime(now);
            tunnelLongitudeLatitudeService.save(n);
        }
    }

    /**
     * 边缘/电能：9916 十二位占位且库内未占用；其它 devicelist 类型：yy * 10^9 + [0,10^9)（log 仅规定 1/2 走 9916）。
     */
    private long nextDevicelistIdForCopy(TunnelDevicelist oldDev) {
        Integer tid = oldDev.getDeviceTypeId();
        if (tid != null && (tid == DEVTYPE_EDGE || tid == DEVTYPE_POWER)) {
            return nextUnique9916DevicelistId();
        }
        return nextUniqueLongDeviceId();
    }

    /** type 1/2：991600000000～991699999999，与 nextUnique9916DevicelistId 共用去重（主键即 deviceId） */
    private long nextUnique9916DevicelistId() {
        long span = PLACEHOLDER_9916_MAX - PLACEHOLDER_9916_MIN + 1;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < MAX_ID_TRIES; i++) {
            long candidate = PLACEHOLDER_9916_MIN + r.nextLong(0, span);
            if (tunnelDevicelistService.getById(candidate) == null) {
                return candidate;
            }
        }
        throw new ServiceException("9916 占位设备号生成失败，请重试");
    }

    /** 非 1/2 的 tunnel_devicelist.device_id：yy * 10^9 + [0,10^9) 且库内不存在 */
    private long nextUniqueLongDeviceId() {
        int yy = Year.now().getValue() % 100;
        long base = yy * 1_000_000_000L;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < MAX_ID_TRIES; i++) {
            long candidate = base + r.nextLong(0, 1_000_000_000L);
            if (tunnelDevicelistService.getById(candidate) == null) {
                return candidate;
            }
        }
        throw new ServiceException("生成 devicelist 设备号失败，请重试");
    }
}
