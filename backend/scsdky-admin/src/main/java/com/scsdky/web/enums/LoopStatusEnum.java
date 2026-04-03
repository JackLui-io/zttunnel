package com.scsdky.web.enums;

import java.util.ArrayList;
import java.util.List;

/**
 *  回路状态
 * @author tubo
 */
public enum LoopStatusEnum {

    GY("过压"),
    SY("失压");


    private String value;

    LoopStatusEnum(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public static List<String> getAllData(){
        LoopStatusEnum[] values = LoopStatusEnum.values();
        List<String> list = new ArrayList<>();
        for (LoopStatusEnum noticeTypeEnum : values) {
            list.add(noticeTypeEnum.getValue());
        }
        return list;
    }

}
