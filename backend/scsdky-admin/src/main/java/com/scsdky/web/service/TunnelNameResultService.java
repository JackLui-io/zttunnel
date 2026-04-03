package com.scsdky.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.Page;
import com.scsdky.web.domain.TunnelName;
import com.scsdky.web.domain.TunnelNameResult;
import com.scsdky.web.domain.dto.TunnelInfoAndDeviceDto;
import com.scsdky.web.domain.vo.KmlDataVo;
import com.scsdky.web.domain.vo.TunnelInfoAndDeviceVo;
import com.scsdky.web.domain.vo.TunnelNameResultVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 *
 * @author tubo
 */
public interface TunnelNameResultService extends IService<TunnelNameResult> {
    /**
     * 获取树状结构的隧道结构
     * @param userId
     * @return
     */
    List<TunnelNameResult> getTunnelName(Long userId);

    /**
     * 获取用户可见隧道的扁平节点列表（与 getTunnelName 同源，用于统计）
     * admin 返回全部，非 admin 返回 t_user_tunnel 中的节点
     * @param userId
     * @return
     */
    List<TunnelNameResult> getTunnelNameFlatList(Long userId);

    /**
     * 获取用户可见的 level-4 隧道 ID（含层级扩展：父节点分配则包含子节点）
     * @param userId 用户 ID
     * @return level-4 隧道 ID 列表
     */
    List<Long> getLevel4TunnelIdsForUser(Long userId);

    /**
     * 设备状态分布专用：仅直接分配 + level-3 扩展，不扩展 level-2（避免同名隧道导致多统计）
     * @param userId 用户 ID
     * @return level-4 隧道 ID 列表
     */
    List<Long> getLevel4TunnelIdsForDeviceStatus(Long userId);

    /**
     * 获取公路--隧道名称
     * @param parentId
     * @return
     */
    String highroadTunnel(Long parentId);

    /**
     * 上传kml文件并解析经纬度
     * @param file
     * @return
     * @throws Exception
     */
    String uploadKml(MultipartFile file) throws Exception;

    /**
     * 提供隧道的经纬度
     * @param tunnelId
     * @return
     */
    List<KmlDataVo> longitudeLatitude(Long tunnelId, Integer isDown);

    /**
     * 分页查询
     * @param tunnelNameResult
     * @return
     */
    Page<TunnelNameResultVo> getTunnelInfo(TunnelNameResult tunnelNameResult);

    /**
     * 通过id获取设备和隧道信息
     * @param id
     * @return
     */
    TunnelInfoAndDeviceVo getTunnelDeviceInfoById(Long id);

    /**
     * 编辑隧道和设备信息
     * @param tunnelInfoAndDeviceDto
     * @return
     */
    boolean updateTunnelDeviceInfoById(TunnelInfoAndDeviceDto tunnelInfoAndDeviceDto) throws JsonProcessingException;

    /**
     * 隧道树状结构
     * @return
     */
    List<TunnelNameResult>  getAllTunnelNameTree();

    /**
     * 获取用户下的隧道信息
     * @param userId
     * @return
     */
    List<TunnelNameResult> getTunnelListByUserId(Long userId);

    /**
     * 公司列表：t_tunnel_name_result 中 level = 1
     */
    List<TunnelNameResult> listLevel1Companies();

    /**
     * 新增公司（level=1，parent_id=0），名称在 level=1 节点中不可重复
     */
    Long addLevel1Company(String tunnelName);

    /**
     * 隧道树：在父节点下新增子节点（父为 L1～L3，子为 L2～L4）
     */
    Long addTunnelTreeNode(TunnelNameResult node);

    /**
     * 隧道树：更新节点名称、里程、状态（状态变更时递归同步子节点）
     */
    boolean updateTunnelTreeNode(TunnelNameResult node);
}
