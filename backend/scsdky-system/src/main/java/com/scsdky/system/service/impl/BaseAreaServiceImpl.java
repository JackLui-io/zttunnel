package com.scsdky.system.service.impl;

import com.scsdky.system.domain.BaseArea;
import com.scsdky.system.mapper.BaseAreaMapper;
import com.scsdky.system.service.IBaseAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author wjs
 * @date 2022/4/1 15:36
 */
@Service
public class BaseAreaServiceImpl implements IBaseAreaService {

    @Autowired
    BaseAreaMapper baseAreaMapper;

    @Override
    public List<BaseArea> getArea(Integer parentId) {
        return baseAreaMapper.selectBaseArea(parentId);
    }

    public BaseArea getAreaByProvinceName(String cityName){
        List<BaseArea> baseAreas = baseAreaMapper.selectByProvinceName(cityName);
        if(baseAreas.size() == 0){
            return null;
        } else {
            return baseAreas.get(0);
        }
    }
    public BaseArea getAreaByCityName(String cityName, Integer parentId){
        List<BaseArea> baseAreas = baseAreaMapper.selectByCityName(cityName,parentId);
        if(baseAreas.size() == 0){
            return null;
        } else {
            return baseAreas.get(0);
        }
    }

    public List<BaseArea> getProvinces(){
        return baseAreaMapper.getProvinces();
    }
    public List<BaseArea> getCities(Long provinceId){
        return baseAreaMapper.getCities(provinceId);
    }
    @Override
    public List<String> getAreaCityName(Integer parentId) {
        return baseAreaMapper.selectBaseAreaCotyName(parentId);
    }
    @Override
    public List<BaseArea> getList() {
        return baseAreaMapper.selectList();
    }

    @Override
    public List<String> getCityList() {
        return baseAreaMapper.getCityNameList();
    }

    @Override
    public List<BaseArea> getAreaByCityNameAndType(String cityName,Integer type) {
        return baseAreaMapper.getAreaByCityNameAndType(cityName,type);
    }

    @Override
    public BaseArea getAreaByCodeId(Integer codeId) {
        return baseAreaMapper.getAreaByCodeId(codeId);
    }


    public List<BaseArea> selectAreaTree()
    {
        List<BaseArea> baseAreas = baseAreaMapper.selectList();
        return getChildPerms(baseAreas, 0);
    }


    public List<BaseArea> getChildPerms(List<BaseArea> list, int parentId)
    {
        List<BaseArea> returnList = new ArrayList<BaseArea>();
        for (Iterator<BaseArea> iterator = list.iterator(); iterator.hasNext();)
        {
            BaseArea t = (BaseArea) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId)
            {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }
    private void recursionFn(List<BaseArea> list, BaseArea t)
    {
        // 得到子节点列表
        List<BaseArea> childList = getChildList(list, t);
        t.setChildren(childList);
        for (BaseArea tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }
    private List<BaseArea> getChildList(List<BaseArea> list, BaseArea t)
    {
        List<BaseArea> tlist = new ArrayList<BaseArea>();
//        if(t.getCodeId() > 1000 && t.getCodeId() < 10000){
//            return tlist;
//        }

        if(t.getCodeId() > 10){
            return tlist;
        }

        Iterator<BaseArea> it = list.iterator();
        while (it.hasNext())
        {
            BaseArea n = (BaseArea) it.next();
            if (n.getParentId().longValue() == t.getCodeId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<BaseArea> list, BaseArea t)
    {
        return getChildList(list, t).size() > 0 ? true : false;
    }
}
