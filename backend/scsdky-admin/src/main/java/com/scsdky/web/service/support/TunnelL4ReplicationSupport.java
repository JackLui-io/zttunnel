package com.scsdky.web.service.support;

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
import com.scsdky.web.domain.TunnelOutOfRadar;
import com.scsdky.web.domain.TunnelPowerEdgeComputing;
import com.scsdky.web.domain.dto.TunnelParamTemplatePayloadV1;
import com.scsdky.web.service.TunnelApproachLampsTerminalService;
import com.scsdky.web.service.TunnelDevicelistService;
import com.scsdky.web.service.TunnelDevicelistTunnelinfoService;
import com.scsdky.web.service.TunnelDeviceParamService;
import com.scsdky.web.service.TunnelDeviceService;
import com.scsdky.web.service.TunnelEdgeComputingTerminalService;
import com.scsdky.web.service.TunnelLampsEdgeComputingService;
import com.scsdky.web.service.TunnelLampsTerminalNodeService;
import com.scsdky.web.service.TunnelLampsTerminalService;
import com.scsdky.web.service.TunnelLongitudeLatitudeService;
import com.scsdky.web.service.TunnelOutOfRadarService;
import com.scsdky.web.service.TunnelPowerEdgeComputingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Level-4 隧道关联数据的「从现网复制」与「从模板快照落库」共用逻辑（与 {@code TunnelCopyServiceImpl} 约定一致）。
 * <p>
 * {@code tunnel_devicelist} 主键为设备号，全局唯一。从现网复制或<strong>应用模板创建新隧道</strong>时，
 * 边缘控制器（type=1）与电能终端（type=2）一律分配<strong>未占用的 9916 开头十二位数字段</strong>
 * （{@code 991600000000}～{@code 991699999999}），与占位/绑定规则一致；快照或源表中的旧 deviceId 仅用于关联映射。
 * </p>
 */
@Component
public class TunnelL4ReplicationSupport {

    private static final int DEVTYPE_EDGE = 1;
    private static final int DEVTYPE_POWER = 2;
    private static final long PLACEHOLDER_9916_MIN = 991600000000L;
    private static final long PLACEHOLDER_9916_MAX = 991699999999L;
    private static final int MAX_ID_TRIES = 80;

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

    /**
     * 从现网 L4 复制到目标 L4（与历史 {@code copyLevel4Detail} 行为一致）。
     */
    public void replicateFromLive(Long oldTunnelId, Long newTunnelId, String lineName, Date now) {
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

    /**
     * 从模板快照 JSON 反序列化后的对象写入目标 L4。
     * 边缘/电能 {@link TunnelDevicelist} 重新生成 9916 段设备号并建立 devicelistId 映射，再写入参数、电表、灯具关联等。
     */
    public void replicateFromPayload(TunnelParamTemplatePayloadV1 payload, Long newTunnelId, String lineName, Date now) {
        if (payload == null) {
            throw new ServiceException("模板快照为空");
        }
        copyTunnelEdgeComputingTerminalFromPayload(payload.getEdgeTerminal(), newTunnelId, lineName);
        Map<Long, Long> devOldToNew = copyDevicelistBlockFromPayload(payload, newTunnelId);
        copyDeviceParamsFromPayload(payload.getDeviceParams(), devOldToNew);
        copyPowerMetersFromPayload(payload.getPowerEdgeComputing(), devOldToNew);
        Long oldEdge = findDevIdByTypeFromPayload(payload, DEVTYPE_EDGE);
        Long newEdge = oldEdge == null ? null : devOldToNew.get(oldEdge);
        if (oldEdge != null && newEdge != null) {
            copyLampsBlockFromPayload(payload, oldEdge, newEdge, now);
            copyOutOfRadarFromPayload(payload.getOutOfRadars(), oldEdge, newEdge, now);
        }
        copyTunnelDevicesFromPayload(payload.getTunnelDevices(), newTunnelId, lineName, now);
        copyApproachLampsFromPayload(payload.getApproachLampsTerminals(), newTunnelId, now);
        copyLongitudeLatitudesFromPayload(payload.getLongitudes(), newTunnelId, lineName, now);
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

    private void copyTunnelEdgeComputingTerminalFromPayload(TunnelEdgeComputingTerminal old, Long newTunnelId, String lineName) {
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
            Integer resolvedType = resolveDevicelistTypeForNewId(oldDev, link);
            TunnelDevicelist nd = new TunnelDevicelist();
            BeanUtils.copyProperties(oldDev, nd, "deviceId");
            if (nd.getDeviceTypeId() == null && resolvedType != null) {
                nd.setDeviceTypeId(resolvedType);
            }
            long nid = nextDevicelistIdForCopy(resolvedType);
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

    private Map<Long, Long> copyDevicelistBlockFromPayload(TunnelParamTemplatePayloadV1 payload, Long newTunnelId) {
        List<TunnelDevicelistTunnelinfo> links = payload.getDevicelistTunnelinfos();
        if (links == null) {
            links = Collections.emptyList();
        }
        Map<Long, TunnelDevicelist> byOldId = new HashMap<>();
        if (payload.getDevicelists() != null) {
            for (TunnelDevicelist d : payload.getDevicelists()) {
                if (d != null && d.getDeviceId() != null) {
                    byOldId.put(d.getDeviceId(), d);
                }
            }
        }
        Map<Long, Long> map = new HashMap<>();
        for (TunnelDevicelistTunnelinfo link : links) {
            Long oldDid = link.getDevicelistId();
            if (oldDid == null || map.containsKey(oldDid)) {
                continue;
            }
            TunnelDevicelist oldDev = byOldId.get(oldDid);
            if (oldDev == null) {
                continue;
            }
            Integer resolvedType = resolveDevicelistTypeForNewId(oldDev, link);
            TunnelDevicelist nd = new TunnelDevicelist();
            BeanUtils.copyProperties(oldDev, nd, "deviceId");
            if (nd.getDeviceTypeId() == null && resolvedType != null) {
                nd.setDeviceTypeId(resolvedType);
            }
            long nid = nextDevicelistIdForCopy(resolvedType);
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

    /**
     * 生成新设备号时：优先用 {@link TunnelDevicelist#getDeviceTypeId()}；为空时用关联 {@link TunnelDevicelistTunnelinfo#getType()}
     *（1=边缘，2=电能），避免模板快照缺字段时误走非 9916 规则。
     */
    private Integer resolveDevicelistTypeForNewId(TunnelDevicelist oldDev, TunnelDevicelistTunnelinfo link) {
        Integer tid = oldDev != null ? oldDev.getDeviceTypeId() : null;
        if (tid != null) {
            return tid;
        }
        return link != null ? link.getType() : null;
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

    private void copyDeviceParamsFromPayload(List<TunnelDeviceParam> params, Map<Long, Long> devOldToNew) {
        if (params == null) {
            return;
        }
        for (TunnelDeviceParam p : params) {
            if (p == null || p.getDevicelistId() == null) {
                continue;
            }
            Long nw = devOldToNew.get(p.getDevicelistId());
            if (nw == null) {
                continue;
            }
            TunnelDeviceParam np = new TunnelDeviceParam();
            BeanUtils.copyProperties(p, np, "id", "devicelistId");
            np.setDevicelistId(nw);
            tunnelDeviceParamService.save(np);
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

    private void copyPowerMetersFromPayload(List<TunnelPowerEdgeComputing> rows, Map<Long, Long> devOldToNew) {
        if (rows == null) {
            return;
        }
        for (TunnelPowerEdgeComputing row : rows) {
            if (row == null || row.getDevicelistId() == null) {
                continue;
            }
            Long newPowerId = devOldToNew.get(row.getDevicelistId());
            if (newPowerId == null) {
                continue;
            }
            TunnelDevicelist probe = tunnelDevicelistService.getById(newPowerId);
            if (probe == null || probe.getDeviceTypeId() == null || probe.getDeviceTypeId() != DEVTYPE_POWER) {
                continue;
            }
            TunnelPowerEdgeComputing n = new TunnelPowerEdgeComputing();
            BeanUtils.copyProperties(row, n, "uniqueId");
            n.setDevicelistId(newPowerId);
            tunnelPowerEdgeComputingService.save(n);
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

    private Long findDevIdByTypeFromPayload(TunnelParamTemplatePayloadV1 payload, int type) {
        List<TunnelDevicelistTunnelinfo> links = payload.getDevicelistTunnelinfos();
        if (links == null) {
            return null;
        }
        for (TunnelDevicelistTunnelinfo link : links) {
            if (link.getType() != null && link.getType() == type) {
                return link.getDevicelistId();
            }
        }
        return null;
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

    private void copyLampsBlockFromPayload(TunnelParamTemplatePayloadV1 payload, Long oldEdgeDevId, Long newEdgeDevId, Date now) {
        List<TunnelLampsEdgeComputing> rels = payload.getLampsEdgeComputings();
        if (rels == null) {
            return;
        }
        Map<Long, TunnelLampsTerminal> lampById = new HashMap<>();
        if (payload.getLampsTerminals() != null) {
            for (TunnelLampsTerminal l : payload.getLampsTerminals()) {
                if (l != null && l.getUniqueId() != null) {
                    lampById.put(l.getUniqueId(), l);
                }
            }
        }
        for (TunnelLampsEdgeComputing rel : rels) {
            if (rel == null || rel.getDevicelistId() == null || !rel.getDevicelistId().equals(oldEdgeDevId)) {
                continue;
            }
            TunnelLampsTerminal lamp = lampById.get(rel.getUniqueId());
            if (lamp == null) {
                continue;
            }
            TunnelLampsTerminal nl = new TunnelLampsTerminal();
            BeanUtils.copyProperties(lamp, nl, "uniqueId", "updateTime");
            nl.setUpdateTime(now);
            tunnelLampsTerminalService.save(nl);

            TunnelLampsEdgeComputing nrel = new TunnelLampsEdgeComputing();
            nrel.setUniqueId(nl.getUniqueId());
            nrel.setDevicelistId(newEdgeDevId);
            tunnelLampsEdgeComputingService.save(nrel);

            if (payload.getLampsTerminalNodes() != null) {
                for (TunnelLampsTerminalNode node : payload.getLampsTerminalNodes()) {
                    if (node == null || node.getUniqueId() == null || !node.getUniqueId().equals(rel.getUniqueId())) {
                        continue;
                    }
                    TunnelLampsTerminalNode nn = new TunnelLampsTerminalNode();
                    BeanUtils.copyProperties(node, nn, "id", "uniqueId");
                    nn.setUniqueId(nl.getUniqueId());
                    tunnelLampsTerminalNodeService.save(nn);
                }
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

    private void copyOutOfRadarFromPayload(List<TunnelOutOfRadar> rows, Long oldEdgeDevId, Long newEdgeDevId, Date now) {
        if (rows == null) {
            return;
        }
        for (TunnelOutOfRadar r : rows) {
            if (r == null || r.getDevicelistId() == null || !r.getDevicelistId().equals(oldEdgeDevId)) {
                continue;
            }
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

    private void copyTunnelDevicesFromPayload(List<TunnelDevice> rows, Long newTunnelId, String lineName, Date now) {
        if (rows == null) {
            return;
        }
        for (TunnelDevice d : rows) {
            if (d == null) {
                continue;
            }
            TunnelDevice nd = new TunnelDevice();
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

    private void copyApproachLampsFromPayload(List<TunnelApproachLampsTerminal> rows, Long newTunnelId, Date now) {
        if (rows == null) {
            return;
        }
        for (TunnelApproachLampsTerminal a : rows) {
            if (a == null) {
                continue;
            }
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

    private void copyLongitudeLatitudesFromPayload(List<TunnelLongitudeLatitude> rows, Long newTunnelId, String lineName, Date now) {
        if (rows == null) {
            return;
        }
        for (TunnelLongitudeLatitude row : rows) {
            if (row == null) {
                continue;
            }
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
     * 边缘（1）、电能（2）→ 未占用的 9916xxxxxxxxxx；其余或类型未知 → 年份前缀随机段（与历史兜底一致）。
     */
    private long nextDevicelistIdForCopy(Integer deviceTypeId) {
        if (deviceTypeId != null && (deviceTypeId == DEVTYPE_EDGE || deviceTypeId == DEVTYPE_POWER)) {
            return nextUnique9916DevicelistId();
        }
        return nextUniqueLongDeviceId();
    }

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
