package com.scsdky.web.controller.tunnel;

import com.scsdky.common.core.controller.BaseController;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.core.page.TableDataInfo;
import com.scsdky.common.utils.poi.ExcelUtil;
import com.scsdky.web.domain.TunnelNotice;
import com.scsdky.web.domain.dto.NoticeDto;
import com.scsdky.web.domain.vo.notice.NoticeVo;
import com.scsdky.web.enums.NoticeTypeEnum;
import com.scsdky.web.service.TunnelNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * @author tubo
 * 后台管理--通知公告
 * @date 2023/09/11
 */

@RestController
@RequestMapping("/notice")
@Api(value = "通知公告", tags = {"DIRECTOR 1.4：通知公告"})
public class TunnelNoticeController extends BaseController {

    @Resource
    private TunnelNoticeService tunnelNoticeService;

    @ApiOperation("通知公告列表")
    @PostMapping(value = "/list",produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "通知公告", response = NoticeVo.class)
    })
    public TableDataInfo getNoticeListByPage(@RequestBody @Valid NoticeDto noticeDto) {
        startPage();
        return getDataTable(tunnelNoticeService.getNoticeListByPage(noticeDto));
    }


    @ApiOperation("通知公告列表--excel导出")
    @PostMapping("/list/export")
    public void getDeviceListExport(HttpServletResponse response, NoticeDto noticeDto) {
        List<TunnelNotice> tTunnelDevice = tunnelNoticeService.getNoticeListByPage(noticeDto);
        ExcelUtil<TunnelNotice> util = new ExcelUtil<>(TunnelNotice.class);
        util.exportExcel(response, tTunnelDevice, "通知公告列表");
    }


    @ApiOperation("通知公告数量统计")
    @PostMapping(value = "/countByTunnel",produces = {MediaType.APPLICATION_JSON_VALUE})
    public AjaxResult countByTunnel(@RequestBody NoticeDto noticeDto) {
        return AjaxResult.success(tunnelNoticeService.countByTunnel(noticeDto));
    }

    @ApiOperation("通过id查询类型个数")
    @GetMapping(value = "/countTypeByTunnelId",produces = {MediaType.APPLICATION_JSON_VALUE})
    public AjaxResult countTypeByTunnelId(@RequestParam Integer tunnelId) {
        return AjaxResult.success(tunnelNoticeService.countTypeByTunnelId(tunnelId));
    }


    @ApiOperation("新增或修改通知列表--id修改必传")
    @PostMapping("/saveOrUpdate")
    public AjaxResult save(@RequestBody TunnelNotice tunnelNotice) {
        return AjaxResult.success(tunnelNoticeService.saveOrUpdate(tunnelNotice));
    }

    @ApiOperation("删除公告")
    @GetMapping("/delete")
    public AjaxResult delete(@RequestParam Long id) {
        return AjaxResult.success(tunnelNoticeService.removeById(id));
    }

    @ApiOperation("获取类型")
    @GetMapping("/getNoticeType")
    public AjaxResult getNoticeType() {
        return AjaxResult.success(NoticeTypeEnum.getAllData());
    }
}
