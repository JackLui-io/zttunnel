package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.scsdky.web.domain.TunnelEdgeComputingTerminal;
import com.scsdky.web.domain.dto.InformationSchemaColumnRow;
import com.scsdky.web.domain.vo.TunnelEdgeTerminalColumnMetaVo;
import com.scsdky.web.mapper.TunnelEdgeTerminalColumnMetaMapper;
import com.scsdky.web.service.TunnelEdgeTerminalMetaService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class TunnelEdgeTerminalMetaServiceImpl implements TunnelEdgeTerminalMetaService {

    private static final String TABLE_NAME = "tunnel_edge_computing_terminal";

    @Resource
    private TunnelEdgeTerminalColumnMetaMapper tunnelEdgeTerminalColumnMetaMapper;

    @Override
    public List<TunnelEdgeTerminalColumnMetaVo> listEdgeTerminalColumnMeta() {
        List<InformationSchemaColumnRow> schemaRows = tunnelEdgeTerminalColumnMetaMapper.selectColumnMeta(TABLE_NAME);
        Map<String, InformationSchemaColumnRow> byColumn = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (InformationSchemaColumnRow r : schemaRows) {
            if (r.getColumnName() != null) {
                byColumn.put(r.getColumnName(), r);
            }
        }

        List<TunnelEdgeTerminalColumnMetaVo> out = new ArrayList<>();
        for (Field field : TunnelEdgeComputingTerminal.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            TableField tf = field.getAnnotation(TableField.class);
            if (tf != null && !tf.exist()) {
                if ("preOnConfig".equals(field.getName())) {
                    out.add(buildVirtualPreOn());
                }
                continue;
            }

            String columnName = resolveDbColumnName(field);
            if (columnName == null) {
                continue;
            }

            InformationSchemaColumnRow row = byColumn.get(columnName);
            TunnelEdgeTerminalColumnMetaVo vo = new TunnelEdgeTerminalColumnMetaVo();
            vo.setPropertyName(field.getName());
            vo.setColumnName(columnName);
            if (row != null) {
                vo.setComment(trimToEmpty(row.getColumnComment()));
                vo.setDataType(row.getDataType());
                vo.setOrdinalPosition(row.getOrdinalPosition());
            } else {
                vo.setComment("");
                vo.setDataType("");
                vo.setOrdinalPosition(99999);
            }
            out.add(vo);
        }

        out.sort(Comparator.comparingInt(a -> a.getOrdinalPosition() == null ? Integer.MAX_VALUE : a.getOrdinalPosition()));
        return out;
    }

    private static String resolveDbColumnName(Field field) {
        TableField tf = field.getAnnotation(TableField.class);
        if (tf != null && StringUtils.isNotBlank(tf.value())) {
            return tf.value();
        }
        return StringUtils.camelToUnderline(field.getName());
    }

    private static String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private static TunnelEdgeTerminalColumnMetaVo buildVirtualPreOn() {
        TunnelEdgeTerminalColumnMetaVo vo = new TunnelEdgeTerminalColumnMetaVo();
        vo.setPropertyName("preOnConfig");
        vo.setColumnName(null);
        vo.setComment("预亮灯控制器配置（40 个数值：前 20 为等待时长、后 20 为持续时长，英文逗号分隔）");
        vo.setDataType("virtual");
        vo.setOrdinalPosition(1_000_000);
        return vo;
    }
}
