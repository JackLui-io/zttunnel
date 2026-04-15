package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.web.domain.TunnelApproachLampsTerminal;
import com.scsdky.web.domain.TunnelDevicelist;
import com.scsdky.web.domain.TunnelDevicelistTunnelinfo;
import com.scsdky.web.domain.TunnelDevice;
import com.scsdky.web.domain.TunnelDeviceParam;
import com.scsdky.web.domain.TunnelLampsEdgeComputing;
import com.scsdky.web.domain.TunnelLampsTerminal;
import com.scsdky.web.domain.TunnelLampsTerminalNode;
import com.scsdky.web.domain.TunnelLongitudeLatitude;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.TunnelOutOfRadar;
import com.scsdky.web.domain.TunnelParamTemplate;
import com.scsdky.web.domain.TunnelParamTemplateDirection;
import com.scsdky.web.domain.TunnelPowerEdgeComputing;
import com.scsdky.web.domain.dto.TunnelParamTemplateApplyDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateDirectionUpdateDto;
import com.scsdky.web.domain.dto.TunnelParamTemplatePayloadV1;
import com.scsdky.web.domain.dto.TunnelParamTemplateSaveFromGroupDto;
import com.scsdky.web.domain.dto.TunnelParamTemplateUpdateDto;
import com.scsdky.web.domain.vo.TunnelParamTemplateApplyResultVo;
import com.scsdky.web.domain.vo.TunnelParamTemplateDetailVo;
import com.scsdky.web.mapper.TunnelParamTemplateDirectionMapper;
import com.scsdky.web.mapper.TunnelParamTemplateMapper;
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
import com.scsdky.web.service.TunnelNameResultService;
import com.scsdky.web.service.TunnelOutOfRadarService;
import com.scsdky.web.service.TunnelParamTemplateService;
import com.scsdky.web.service.TunnelPowerEdgeComputingService;
import com.scsdky.web.service.support.TunnelL4ReplicationSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TunnelParamTemplateServiceImpl implements TunnelParamTemplateService {

    private static final int LEVEL_GROUP = 3;
    private static final int LEVEL_LINE = 4;
    private static final int STATUS_DELETED = 2;
    private static final int DEVTYPE_EDGE = 1;
    private static final int DEVTYPE_POWER = 2;

    @Resource
    private TunnelParamTemplateMapper tunnelParamTemplateMapper;
    @Resource
    private TunnelParamTemplateDirectionMapper tunnelParamTemplateDirectionMapper;
    @Resource
    private TunnelNameResultService tunnelNameResultService;
    @Resource
    private TunnelL4ReplicationSupport tunnelL4ReplicationSupport;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private TunnelEdgeComputingTerminalService tunnelEdgeComputingTerminalService;
    @Resource
    private TunnelDevicelistTunnelinfoService tunnelDevicelistTunnelinfoService;
    @Resource
    private TunnelDevicelistService tunnelDevicelistService;
    @Resource
    private TunnelDeviceParamService tunnelDeviceParamService;
    @Resource
    private TunnelPowerEdgeComputingService tunnelPowerEdgeComputingService;
    @Resource
    private TunnelLampsEdgeComputingService tunnelLampsEdgeComputingService;
    @Resource
    private TunnelLampsTerminalService tunnelLampsTerminalService;
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
    public List<TunnelParamTemplate> listTemplates() {
        return tunnelParamTemplateMapper.selectList(
            new LambdaQueryWrapper<TunnelParamTemplate>()
                .ne(TunnelParamTemplate::getStatus, STATUS_DELETED)
                .orderByDesc(TunnelParamTemplate::getId));
    }

    @Override
    public TunnelParamTemplateDetailVo getDetail(Long id) {
        TunnelParamTemplate t = tunnelParamTemplateMapper.selectById(id);
        if (t == null || Objects.equals(t.getStatus(), STATUS_DELETED)) {
            throw new ServiceException("模板不存在");
        }
        List<TunnelParamTemplateDirection> dirs = tunnelParamTemplateDirectionMapper.selectList(
            new LambdaQueryWrapper<TunnelParamTemplateDirection>()
                .eq(TunnelParamTemplateDirection::getTemplateId, id)
                .orderByAsc(TunnelParamTemplateDirection::getSortOrder)
                .orderByAsc(TunnelParamTemplateDirection::getId));
        return TunnelParamTemplateDetailVo.builder()
            .template(t)
            .directions(dirs)
            .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFromGroup(TunnelParamTemplateSaveFromGroupDto dto, String username) {
        String name = StringUtils.trim(dto.getTemplateName());
        if (StringUtils.isEmpty(name)) {
            throw new ServiceException("模板名称不能为空");
        }
        TunnelNameResult source = tunnelNameResultService.getById(dto.getSourceTunnelGroupId());
        if (source == null) {
            throw new ServiceException("来源隧道群不存在");
        }
        if (source.getLevel() == null || source.getLevel() != LEVEL_GROUP) {
            throw new ServiceException("只能从隧道群（level=3）存为模板");
        }

        String code = StringUtils.trim(dto.getTemplateCode());
        if (StringUtils.isNotEmpty(code)) {
            Integer exists = tunnelParamTemplateMapper.selectCount(
                new LambdaQueryWrapper<TunnelParamTemplate>().eq(TunnelParamTemplate::getTemplateCode, code));
            if (exists != null && exists > 0) {
                throw new ServiceException("模板编码已存在");
            }
        } else {
            code = null;
        }

        List<TunnelNameResult> children = tunnelNameResultService.lambdaQuery()
            .eq(TunnelNameResult::getParentId, source.getId())
            .eq(TunnelNameResult::getLevel, LEVEL_LINE)
            .orderByAsc(TunnelNameResult::getId)
            .list();
        if (children == null || children.isEmpty()) {
            throw new ServiceException("源隧道群下无左右线（L4），无法存为模板");
        }

        Date now = new Date();
        TunnelParamTemplate head = new TunnelParamTemplate();
        head.setTemplateCode(code);
        head.setTemplateName(name);
        head.setTemplateSchemaVersion(TunnelParamTemplatePayloadV1.SCHEMA_VERSION);
        head.setStatus(1);
        head.setRemark(StringUtils.isNotEmpty(dto.getRemark()) ? dto.getRemark().trim() : null);
        head.setSourceTunnelGroupId(source.getId());
        head.setCreateBy(username);
        head.setCreateTime(now);
        head.setUpdateBy(username);
        head.setUpdateTime(now);
        tunnelParamTemplateMapper.insert(head);

        int idx = 0;
        for (TunnelNameResult child : children) {
            TunnelParamTemplatePayloadV1 snap = buildSnapshotForL4(child.getId());
            String json;
            try {
                json = objectMapper.writeValueAsString(snap);
            } catch (JsonProcessingException e) {
                throw new ServiceException("序列化模板快照失败");
            }
            TunnelParamTemplateDirection row = new TunnelParamTemplateDirection();
            row.setTemplateId(head.getId());
            row.setSortOrder(idx);
            row.setDirection(inferDirectionLine(child, idx));
            row.setLineDisplayName(child.getTunnelName());
            row.setLineTunnelMileage(child.getTunnelMileage());
            row.setLineStatus(child.getStatus() != null ? child.getStatus() : 0);
            row.setPayloadJson(json);
            row.setCreateTime(now);
            row.setUpdateTime(now);
            tunnelParamTemplateDirectionMapper.insert(row);
            idx++;
        }
        return head.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTemplate(Long id, TunnelParamTemplateUpdateDto dto, String username) {
        TunnelParamTemplate t = tunnelParamTemplateMapper.selectById(id);
        if (t == null || Objects.equals(t.getStatus(), STATUS_DELETED)) {
            throw new ServiceException("模板不存在或已删除");
        }
        if (dto.getTemplateName() != null) {
            String n = StringUtils.trim(dto.getTemplateName());
            if (StringUtils.isEmpty(n)) {
                throw new ServiceException("模板名称不能为空");
            }
            t.setTemplateName(n);
        }
        if (dto.getTemplateCode() != null) {
            String c = StringUtils.trim(dto.getTemplateCode());
            if (StringUtils.isEmpty(c)) {
                t.setTemplateCode(null);
            } else {
                Integer cnt = tunnelParamTemplateMapper.selectCount(
                    new LambdaQueryWrapper<TunnelParamTemplate>()
                        .eq(TunnelParamTemplate::getTemplateCode, c)
                        .ne(TunnelParamTemplate::getId, id));
                if (cnt != null && cnt > 0) {
                    throw new ServiceException("模板编码已存在");
                }
                t.setTemplateCode(c);
            }
        }
        if (dto.getStatus() != null) {
            t.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            t.setRemark(dto.getRemark());
        }
        t.setUpdateBy(username);
        t.setUpdateTime(new Date());
        tunnelParamTemplateMapper.updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDirection(Long templateId, Long directionId, TunnelParamTemplateDirectionUpdateDto dto, String username) {
        if (username == null) {
            throw new ServiceException("未登录");
        }
        TunnelParamTemplate head = tunnelParamTemplateMapper.selectById(templateId);
        if (head == null || Objects.equals(head.getStatus(), STATUS_DELETED)) {
            throw new ServiceException("模板不存在或已删除");
        }
        TunnelParamTemplateDirection row = tunnelParamTemplateDirectionMapper.selectById(directionId);
        if (row == null || !Objects.equals(row.getTemplateId(), templateId)) {
            throw new ServiceException("模板方向行不存在");
        }
        if (dto.getDirection() != null) {
            row.setDirection(dto.getDirection());
        }
        if (dto.getLineDisplayName() != null) {
            row.setLineDisplayName(StringUtils.trim(dto.getLineDisplayName()));
        }
        if (dto.getLineTunnelMileage() != null) {
            row.setLineTunnelMileage(dto.getLineTunnelMileage());
        }
        if (dto.getLineStatus() != null) {
            row.setLineStatus(dto.getLineStatus());
        }
        if (dto.getSortOrder() != null) {
            row.setSortOrder(dto.getSortOrder());
        }
        if (dto.getPayloadJson() != null) {
            validatePayload(dto.getPayloadJson(), head.getTemplateSchemaVersion());
            row.setPayloadJson(dto.getPayloadJson());
        }
        row.setUpdateTime(new Date());
        tunnelParamTemplateDirectionMapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLogical(Long id, String username) {
        TunnelParamTemplate t = tunnelParamTemplateMapper.selectById(id);
        if (t == null || Objects.equals(t.getStatus(), STATUS_DELETED)) {
            throw new ServiceException("模板不存在");
        }
        t.setStatus(STATUS_DELETED);
        t.setUpdateBy(username);
        t.setUpdateTime(new Date());
        tunnelParamTemplateMapper.updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TunnelParamTemplateApplyResultVo apply(TunnelParamTemplateApplyDto dto) {
        TunnelNameResult target = tunnelNameResultService.getById(dto.getTargetTunnelGroupId());
        if (target == null) {
            throw new ServiceException("目标隧道群不存在");
        }
        if (target.getLevel() == null || target.getLevel() != LEVEL_GROUP) {
            throw new ServiceException("只能应用到隧道群（level=3）");
        }
        long childCount = tunnelNameResultService.lambdaQuery()
            .eq(TunnelNameResult::getParentId, target.getId())
            .eq(TunnelNameResult::getLevel, LEVEL_LINE)
            .count();
        if (childCount > 0) {
            throw new ServiceException("目标隧道群下已存在左右线，仅能在无 L4 子节点时应用模板");
        }

        TunnelParamTemplate head = tunnelParamTemplateMapper.selectById(dto.getTemplateId());
        if (head == null || Objects.equals(head.getStatus(), STATUS_DELETED)) {
            throw new ServiceException("模板不存在或已删除");
        }
        List<TunnelParamTemplateDirection> dirs = tunnelParamTemplateDirectionMapper.selectList(
            new LambdaQueryWrapper<TunnelParamTemplateDirection>()
                .eq(TunnelParamTemplateDirection::getTemplateId, head.getId())
                .orderByAsc(TunnelParamTemplateDirection::getSortOrder)
                .orderByAsc(TunnelParamTemplateDirection::getId));
        if (dirs == null || dirs.isEmpty()) {
            throw new ServiceException("模板无方向数据");
        }

        Date now = new Date();
        List<Long> newIds = new ArrayList<>();
        for (TunnelParamTemplateDirection dir : dirs) {
            String lineName = StringUtils.isNotEmpty(dir.getLineDisplayName()) ? dir.getLineDisplayName() : "线";
            TunnelNameResult nc = TunnelNameResult.builder()
                .tunnelName(lineName)
                .parentId(target.getId())
                .level(LEVEL_LINE)
                .tunnelMileage(dir.getLineTunnelMileage())
                .status(dir.getLineStatus() != null ? dir.getLineStatus() : 0)
                .createTime(now)
                .updateTime(now)
                .build();
            if (!tunnelNameResultService.save(nc)) {
                throw new ServiceException("创建左右线节点失败");
            }
            TunnelParamTemplatePayloadV1 payload = parsePayload(dir.getPayloadJson(), head.getTemplateSchemaVersion());
            tunnelL4ReplicationSupport.replicateFromPayload(payload, nc.getId(), nc.getTunnelName(), now);
            newIds.add(nc.getId());
        }
        return TunnelParamTemplateApplyResultVo.builder()
            .newLevel4TunnelIds(newIds)
            .build();
    }

    private void validatePayload(String json, Integer schemaVersion) {
        if (StringUtils.isEmpty(json)) {
            throw new ServiceException("payload_json 不能为空");
        }
        parsePayload(json, schemaVersion);
    }

    private TunnelParamTemplatePayloadV1 parsePayload(String json, Integer schemaVersion) {
        if (schemaVersion == null || schemaVersion != TunnelParamTemplatePayloadV1.SCHEMA_VERSION) {
            throw new ServiceException("不支持的模板结构版本: " + schemaVersion);
        }
        try {
            TunnelParamTemplatePayloadV1 p = objectMapper.readValue(json, TunnelParamTemplatePayloadV1.class);
            int sv = p.getSchemaVersion();
            if (sv == 0) {
                sv = TunnelParamTemplatePayloadV1.SCHEMA_VERSION;
            }
            if (sv != TunnelParamTemplatePayloadV1.SCHEMA_VERSION) {
                throw new ServiceException("快照内 schemaVersion 与模板头不一致");
            }
            return p;
        } catch (JsonProcessingException e) {
            throw new ServiceException("解析模板快照失败: " + e.getMessage());
        }
    }

    private int inferDirectionLine(TunnelNameResult l4, int index) {
        String n = l4.getTunnelName();
        if (n != null) {
            if (n.contains("右")) {
                return 1;
            }
            if (n.contains("左")) {
                return 2;
            }
        }
        return index == 0 ? 1 : 2;
    }

    private TunnelParamTemplatePayloadV1 buildSnapshotForL4(Long l4Id) {
        TunnelParamTemplatePayloadV1 p = new TunnelParamTemplatePayloadV1();
        p.setSchemaVersion(TunnelParamTemplatePayloadV1.SCHEMA_VERSION);
        p.setEdgeTerminal(tunnelEdgeComputingTerminalService.getById(l4Id));

        List<TunnelDevicelistTunnelinfo> links = tunnelDevicelistTunnelinfoService.lambdaQuery()
            .eq(TunnelDevicelistTunnelinfo::getTunnelId, l4Id)
            .list();
        p.setDevicelistTunnelinfos(links == null ? Collections.emptyList() : new ArrayList<>(links));

        Map<Long, TunnelDevicelist> byId = new HashMap<>();
        if (links != null) {
            for (TunnelDevicelistTunnelinfo link : links) {
                if (link.getDevicelistId() == null) {
                    continue;
                }
                TunnelDevicelist d = tunnelDevicelistService.getById(link.getDevicelistId());
                if (d != null) {
                    byId.put(d.getDeviceId(), d);
                }
            }
        }
        p.setDevicelists(new ArrayList<>(byId.values()));

        List<TunnelDeviceParam> allParams = new ArrayList<>();
        for (Long did : byId.keySet()) {
            allParams.addAll(tunnelDeviceParamService.lambdaQuery()
                .eq(TunnelDeviceParam::getDevicelistId, did)
                .list());
        }
        p.setDeviceParams(allParams);

        List<TunnelPowerEdgeComputing> powerRows = new ArrayList<>();
        for (Long did : byId.keySet()) {
            TunnelDevicelist d = byId.get(did);
            if (d != null && d.getDeviceTypeId() != null && d.getDeviceTypeId() == DEVTYPE_POWER) {
                powerRows.addAll(tunnelPowerEdgeComputingService.lambdaQuery()
                    .eq(TunnelPowerEdgeComputing::getDevicelistId, did)
                    .list());
            }
        }
        p.setPowerEdgeComputing(powerRows);

        Long edgeId = findDevIdByTypeInLinks(links, DEVTYPE_EDGE);
        if (edgeId != null) {
            List<TunnelLampsEdgeComputing> rels = tunnelLampsEdgeComputingService.lambdaQuery()
                .eq(TunnelLampsEdgeComputing::getDevicelistId, edgeId)
                .list();
            p.setLampsEdgeComputings(rels == null ? Collections.emptyList() : new ArrayList<>(rels));
            List<TunnelLampsTerminal> lamps = new ArrayList<>();
            List<TunnelLampsTerminalNode> nodes = new ArrayList<>();
            if (rels != null) {
                for (TunnelLampsEdgeComputing rel : rels) {
                    if (rel.getUniqueId() == null) {
                        continue;
                    }
                    TunnelLampsTerminal lamp = tunnelLampsTerminalService.getById(rel.getUniqueId());
                    if (lamp != null) {
                        lamps.add(lamp);
                    }
                    nodes.addAll(tunnelLampsTerminalNodeService.lambdaQuery()
                        .eq(TunnelLampsTerminalNode::getUniqueId, rel.getUniqueId())
                        .list());
                }
            }
            p.setLampsTerminals(lamps);
            p.setLampsTerminalNodes(nodes);
            p.setOutOfRadars(tunnelOutOfRadarService.lambdaQuery()
                .eq(TunnelOutOfRadar::getDevicelistId, edgeId)
                .list());
        } else {
            p.setLampsEdgeComputings(Collections.emptyList());
            p.setLampsTerminals(Collections.emptyList());
            p.setLampsTerminalNodes(Collections.emptyList());
            p.setOutOfRadars(Collections.emptyList());
        }

        p.setTunnelDevices(tunnelDeviceService.lambdaQuery().eq(TunnelDevice::getTunnelId, l4Id).list());
        p.setApproachLampsTerminals(tunnelApproachLampsTerminalService.lambdaQuery()
            .eq(TunnelApproachLampsTerminal::getTunnelId, l4Id)
            .list());
        p.setLongitudes(tunnelLongitudeLatitudeService.lambdaQuery()
            .eq(TunnelLongitudeLatitude::getTunnelId, l4Id)
            .list());
        return p;
    }

    private Long findDevIdByTypeInLinks(List<TunnelDevicelistTunnelinfo> links, int type) {
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
}
