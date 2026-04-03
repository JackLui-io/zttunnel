package com.scsdky.web.enums;

import java.util.ArrayList;
import java.util.List;

/**
 *  设备区段
 * @author tubo
 */
public enum DeviceTypeEnum {
    BYKZQ("边缘控制器"),
    DNZD("电能终端"),
    LD("洞外雷达"),
    DWLD("洞外亮度传感器"),
    DJZD("灯具终端");


    private String value;

    DeviceTypeEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public static List<String> getAllData(){
        DeviceTypeEnum[] values = DeviceTypeEnum.values();
        List<String> list = new ArrayList<>();
        for (DeviceTypeEnum noticeTypeEnum : values) {
            list.add(noticeTypeEnum.getValue());
        }
        return list;
    }

}
