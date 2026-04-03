package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.web.domain.TunnelApproachLampsTerminal;
import com.scsdky.web.domain.dto.TunnelApproachLampsTerminalDto;
import com.scsdky.web.service.TunnelApproachLampsTerminalService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;

/**
 * 引道灯控制器
 *
 * @author makejava
 * @since 2025-09-29 10:13:53
 */
@RestController
@RequestMapping("approachLamps")
public class TunnelApproachLampsTerminalController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private TunnelApproachLampsTerminalService tunnelApproachLampsTerminalService;

    /**
     * 根据隧道id查询所有引道灯控制器数据
     * @param tunnelId 隧道id
     * @return 所有数据
     */
    @GetMapping("list")
    public AjaxResult<List<TunnelApproachLampsTerminalDto>> selectAll(@RequestParam("tunnelId") Long tunnelId) {
        return AjaxResult.success(this.tunnelApproachLampsTerminalService.selectAll(tunnelId));
    }

    /**
     * 通过主键查询单条引道灯控制器数据
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("info/{id}")
    public AjaxResult<TunnelApproachLampsTerminal> selectOne(@PathVariable Serializable id) {
        return AjaxResult.success(this.tunnelApproachLampsTerminalService.getById(id));
    }

    /**
     * 新增引道灯控制器
     * @param tunnelApproachLampsTerminal 实体对象
     * @return 新增结果
     */
    @PostMapping("add")
    public AjaxResult<Boolean> insert(@RequestBody TunnelApproachLampsTerminal tunnelApproachLampsTerminal) {
        return AjaxResult.success(this.tunnelApproachLampsTerminalService.save(tunnelApproachLampsTerminal));
    }

    /**
     * 修改引道灯控制器
     * @param tunnelApproachLampsTerminal 实体对象
     * @return 修改结果
     */
    @PostMapping("update")
    public AjaxResult<Boolean> update(@RequestBody TunnelApproachLampsTerminal tunnelApproachLampsTerminal) {
        return AjaxResult.success(this.tunnelApproachLampsTerminalService.updateById(tunnelApproachLampsTerminal));
    }

    /**
     * 批量删除引道灯控制器
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping("delete")
    public AjaxResult<Boolean> delete(@RequestParam("idList") List<Long> idList) {
        return AjaxResult.success(this.tunnelApproachLampsTerminalService.removeByIds(idList));
    }
}

