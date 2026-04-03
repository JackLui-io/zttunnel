package com.scsdky.web.enums;

import lombok.Getter;

@Getter
public enum FileDeviceTypeEnum {

    /**
     * 边缘控制器（dict_value=1，dict_sort=1）
     */
    EDGE_CONTROLLER("1", "边缘控制器"),

    /**
     * 电能终端（dict_value=2，dict_sort=2）
     */
    ELECTRICITY_TERMINAL("2", "电能终端"),

    /**
     * 中继器（dict_value=3，dict_sort=3）
     */
    REPEATER("3", "中继器"),

    /**
     * 灯具控制器（dict_value=4，dict_sort=4）
     */
    LAMP_CONTROLLER("4", "灯具控制器"),

    /**
     * 引道灯控制器（dict_value=5，dict_sort=5）
     */
    APPROACH_LAMP_CONTROLLER("5", "引道灯控制器");

    /**
     * 字典值（对应sys_dict_data.dict_value）
     */
    private final String value;

    /**
     * 字典标签（对应sys_dict_data.dict_label）
     */
    private final String label;

    FileDeviceTypeEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * 根据字典值获取枚举实例
     * @param value 字典值（sys_dict_data.dict_value）
     * @return 对应的枚举实例，若未匹配则返回null
     */
    public static FileDeviceTypeEnum getByValue(String value) {
        for (FileDeviceTypeEnum type : FileDeviceTypeEnum.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 根据字典值获取标签（用于展示）
     * @param value 字典值
     * @return 标签文本，若未匹配则返回空字符串
     */
    public static String getLabelByValue(String value) {
        FileDeviceTypeEnum type = getByValue(value);
        return type != null ? type.label : "";
    }
}
    