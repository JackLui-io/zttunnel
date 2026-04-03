package com.scsdky.system.service;


import com.scsdky.system.domain.BaseArea;

import java.util.List;

/**
 * 地区服务
 *
 * @author wjs
 * @date 2022/4/1 15:34
 */
public interface IBaseAreaService {

    /**
     * 获取区域
     *
     * @param parentId
     * @return
     */
    List<BaseArea> getArea(Integer parentId);


    /**
     * 获取区域列表
     */
    List<BaseArea> getList();

    List<String> getAreaCityName(Integer parentId);

    List<String> getCityList();

    /**
     * 根据名称和类型查询
     * @param cityName
     * @param type
     * @return
     */
    List<BaseArea> getAreaByCityNameAndType(String cityName,Integer type);

    /**
     * 根据codeId查询地址信息
     * @param codeId
     * @return
     */
    BaseArea getAreaByCodeId(Integer codeId);

    // List<SysDeptArea> getTreeAreas();
}
