package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.TunnelPowerSupplyCircuit;
import com.scsdky.web.domain.dto.TunnelPowerSupplyCircuitDto;
import com.scsdky.web.domain.dto.TunnelPowerSupplyCircuitQueryDto;
import com.scsdky.web.service.TunnelPowerSupplyCircuitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 直流照明-供电回路相关接口
 *
 * @author makejava
 * @since 2025-08-25 13:54:03
 */
@RestController
@RequestMapping("tunnelPowerSupplyCircuit")
public class TunnelPowerSupplyCircuitController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private TunnelPowerSupplyCircuitService tunnelPowerSupplyCircuitService;

    /**
     * 查询所有数据
     *
     * @param dto 查询实体
     * @return 所有数据
     */
    @GetMapping("list")
    public AjaxResult selectAll(TunnelPowerSupplyCircuitQueryDto dto) {
        return AjaxResult.success(this.tunnelPowerSupplyCircuitService.selectAll(dto));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public AjaxResult selectOne(@PathVariable Serializable id) {
        return AjaxResult.success(this.tunnelPowerSupplyCircuitService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param dto 实体对象
     * @return 新增结果
     */
    @PostMapping("save")
    public AjaxResult insert(@RequestBody TunnelPowerSupplyCircuitDto dto) {
        return AjaxResult.success(this.tunnelPowerSupplyCircuitService.insert(dto));
    }

    /**
     * 修改数据
     *
     * @param dto 实体对象
     * @return 修改结果
     */
    @PostMapping("update")
    public AjaxResult updateData(@RequestBody TunnelPowerSupplyCircuitDto dto) {
        return AjaxResult.success(this.tunnelPowerSupplyCircuitService.updateData(dto));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public AjaxResult delete(@RequestParam("idList") List<Long> idList) {
        return AjaxResult.success(this.tunnelPowerSupplyCircuitService.removeByIds(idList));
    }
}

