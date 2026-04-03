package com.scsdky.web.enums;

import com.scsdky.web.domain.vo.EnumVo;

import java.util.ArrayList;
import java.util.List;

/**
 * 通知公告枚举
 * @author tubo
 */

public enum NoticeTypeEnum {

    TFSG(1,"系统通知"),
    XLWH(2,"模式切换"),
    TGMS(3,"实时警报"),
    SBXG(4,"设备管理");


    private Integer typeNum;
    private String value;

    NoticeTypeEnum(Integer typeNum , String value){
        this.typeNum = typeNum;
        this.value = value;
    }

    public Integer getTypeNum(){
        return typeNum;
    }

    public String getValue(){
        return value;
    }

    public static List<EnumVo> getAllData(){
        NoticeTypeEnum[] values = NoticeTypeEnum.values();
        List<EnumVo> list = new ArrayList<>();
        for (NoticeTypeEnum noticeTypeEnum : values) {
            EnumVo enumVo = new EnumVo();
            enumVo.setTypeNum(noticeTypeEnum.getTypeNum());
            enumVo.setType(noticeTypeEnum.getValue());
            list.add(enumVo);
        }
        return list;
    }

    /**
     * 通过code 获取值
     * @param code
     * @return
     */
    public static String getEnumValue(Object code){
        NoticeTypeEnum[] values = NoticeTypeEnum.values();
        try {
            for(NoticeTypeEnum noticeTypeEnum : values){

                if(code.equals(noticeTypeEnum.getTypeNum())){
                    return noticeTypeEnum.getValue();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
