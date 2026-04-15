package com.scsdky.web.domain.dto;

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
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 隧道参数模板单方向 L4 快照结构（与 {@code TunnelL4ReplicationSupport#replicateFromLive} 落库范围对齐）。
 */
@Data
public class TunnelParamTemplatePayloadV1 implements Serializable {

    public static final int SCHEMA_VERSION = 1;

    private static final long serialVersionUID = 1L;

    /** 与库表 template_schema_version 一致 */
    private int schemaVersion = SCHEMA_VERSION;

    private TunnelEdgeComputingTerminal edgeTerminal;
    private List<TunnelDevicelist> devicelists;
    private List<TunnelDevicelistTunnelinfo> devicelistTunnelinfos;
    private List<TunnelDeviceParam> deviceParams;
    private List<TunnelPowerEdgeComputing> powerEdgeComputing;
    private List<TunnelLampsTerminal> lampsTerminals;
    private List<TunnelLampsEdgeComputing> lampsEdgeComputings;
    private List<TunnelLampsTerminalNode> lampsTerminalNodes;
    private List<TunnelOutOfRadar> outOfRadars;
    private List<TunnelDevice> tunnelDevices;
    private List<TunnelApproachLampsTerminal> approachLampsTerminals;
    private List<TunnelLongitudeLatitude> longitudes;
}
