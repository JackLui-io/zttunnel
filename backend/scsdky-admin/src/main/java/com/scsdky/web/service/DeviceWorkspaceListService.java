package com.scsdky.web.service;

import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.web.domain.dto.DeviceWorkspaceQueryDto;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 设备列表工作台：多叶子隧道 + 设备类型 + 分页查询与导出
 */
public interface DeviceWorkspaceListService {

    /**
     * 合并后的全量列表（内存分页前），供导出使用。
     */
    List<?> listMerged(DeviceWorkspaceQueryDto dto);

    TableDataInfo page(DeviceWorkspaceQueryDto dto);

    void export(HttpServletResponse response, DeviceWorkspaceQueryDto dto);
}
