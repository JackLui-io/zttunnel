package com.scsdky.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.system.domain.BaseArea;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wjs
 * @date 2022/4/1 14:57
 */
public interface BaseAreaMapper extends BaseMapper<BaseArea> {

    @Select("select code_id, parent_id, city_name from base_area where parent_id = #{parentId}")
    public List<BaseArea> selectBaseArea(@Param("parentId") Integer parentId);

    @Select("select * from base_area")
    public List<BaseArea> selectList();
    @Select("select city_name from base_area where parent_id = #{parentId}")
    public List<String> selectBaseAreaCotyName(@Param("parentId") Integer parentId);

    @Select("select city_name from base_area where LENGTH(parent_id) = 2")
    List<String> getCityNameList();

    @Select("select code_id, parent_id, city_name from base_area")
    List<BaseArea> getAreaList();

    @Select("select code_id, parent_id, city_name from base_area where city_name like concat('%',#{cityName},'%') and parent_id < 10")
    List<BaseArea> selectByProvinceName(@Param("cityName") String cityName);

    @Select("select code_id, parent_id, city_name from base_area where city_name  = #{provinceName} and parent_id < 10 limit 1")
    BaseArea selectProvinceByName(@Param("provinceName") String provinceName);

    @Select("select code_id, parent_id, city_name from base_area where city_name  = #{cityName} and LENGTH(parent_id) = 2 limit 1")
    BaseArea selectCityByName(@Param("cityName") String cityName);

    @Select("select code_id, parent_id, city_name from base_area where city_name like concat('%',#{cityName},'%') and parent_id = #{parentId}")
    List<BaseArea> selectByCityName(@Param("cityName") String cityName, @Param("parentId") Integer parentId);

    @Select("select * from base_area where parent_id < 10 ")
    List<BaseArea> getProvinces();

    @Select("select * from base_area where parent_id =  #{provinceId}")
    List<BaseArea> getCities(@Param("provinceId") Long provinceId);

    @Select("select code_id, parent_id, city_name from base_area where city_name like concat(#{cityName},'%') and type = #{type}")
    List<BaseArea> getAreaByCityNameAndType(@Param("cityName") String cityName,@Param("type") Integer type);

    @Select("select code_id, parent_id, city_name from base_area where code_id = #{codeId}")
    BaseArea getAreaByCodeId(Integer codeId);
}
