package com.scsdky.web.utils;

import com.github.dozermapper.core.Mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tubo
 * @date 2024/01/25
 */
public class DozerUtils {
    public static <T,S> List<T> mapList(final Mapper mapper, List<S> sourceList, Class<T> targetObjectClass){
        List<T> targetList=new ArrayList<T>();
        for(S s:sourceList){
            targetList.add(mapper.map(s,targetObjectClass));
        }
        return targetList;
    }
}
