package com.scsdky.web.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tubo
 * @date 2024/01/29
 */
public class MapConstants {

    public static Map<String,Long> mapValue = new HashMap<>();

    public static Long getMapData(String key){
        return mapValue.get(key);
    }
}
