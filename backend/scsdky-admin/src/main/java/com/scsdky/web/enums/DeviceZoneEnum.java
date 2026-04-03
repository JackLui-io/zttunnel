package com.scsdky.web.enums;

import com.scsdky.web.domain.dto.EnumDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  设备区段
 * @author tubo
 */
@Getter
public enum DeviceZoneEnum {
    IN(1,"入口段1"),
    RK2(2,"入口段2"),
    GD1(3,"过渡段1"),
    GD2(4,"过渡段2"),
    JB(5,"基本段"),
    CK(6,"出口段"),
    JJTCD(7,"紧急停车带"),
    DW(8,"洞外雷达开灯区段"),
    JK(9,"进口引道"),
    CKYD(10,"出口引道"),
    DW_RK1(11,"洞外雷达开灯 (入口1)"),
    BACKUP1(12,"备用1"),
    BACKUP2(13,"备用2"),
    BACKUP3(14,"备用3"),
    BACKUP4(15,"备用4");
    // RKD1(101,"入口段1(无级)"),
    // RKD(102,"入口段2(无级)"),
    // GDD1(103,"过渡段1(无级)"),
    // GGD(104,"过渡段2(无级)"),
    // JBD(105,"基本段(无级)"),
    // CKD(106,"出口段(无级)"),
    // JJTCDW(107,"紧急停车带(无级)"),
    // DWLDK(108,"洞外雷达开灯区段(无级)"),
    // JKYD(109,"进口引道(无级)"),
    // CKYDW(110,"出口引道(无级)");



    private Integer typeNum;
    private String value;

    DeviceZoneEnum(Integer typeNum , String value){
        this.typeNum = typeNum;
        this.value = value;
    }

    public static List<String> getAllData(){
        DeviceZoneEnum[] values = DeviceZoneEnum.values();
        List<String> list = new ArrayList<>();
        for (DeviceZoneEnum noticeTypeEnum : values) {
            list.add(noticeTypeEnum.getValue());
        }
        return list;
    }

    /**
     * 通过code 获取值
     * @param code
     * @return
     */
    public static String getEnumValue(Object code){
        DeviceZoneEnum[] values = DeviceZoneEnum.values();
        try {
            for(DeviceZoneEnum noticeTypeEnum : values){

                if(code.equals(String.valueOf(noticeTypeEnum.getTypeNum()))){
                    return noticeTypeEnum.getValue();
                }
            }
            Integer bitCode = Integer.valueOf(String.valueOf(code));
            Integer realCode = getEnumCodeByBit(bitCode);
            if(realCode != null && !code.equals(realCode)){
                for(DeviceZoneEnum noticeTypeEnum : values){
                    if(realCode.equals(noticeTypeEnum.getTypeNum())){
                        return noticeTypeEnum.getValue();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * value 获取code
     * @param value
     * @return
     */
    public static Integer getEnumCode(Object value){
        DeviceZoneEnum[] values = DeviceZoneEnum.values();
        try {
            for(DeviceZoneEnum noticeTypeEnum : values){

                if(value.equals(String.valueOf(noticeTypeEnum.getValue()))){
                    return noticeTypeEnum.getTypeNum();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过低4bit值获取真实的code
     * @param bitCode 低4bit值 (1-15)
     * @return enum code (直接返回1-15的值，如果是无级类型则返回101-110)
     */
    public static Integer getEnumCodeByBit(Integer bitCode){
        if(bitCode == null){
            return null;
        }
        // 如果值在1-15范围内，直接返回（匹配基础区段）
        DeviceZoneEnum[] values = DeviceZoneEnum.values();
        for(DeviceZoneEnum noticeTypeEnum : values){
            // 优先匹配1-15范围内的基础区段
            if(noticeTypeEnum.getTypeNum() >= 1 && noticeTypeEnum.getTypeNum() <= 15){
                if(noticeTypeEnum.getTypeNum().equals(bitCode)){
                    return noticeTypeEnum.getTypeNum();
                }
            }
        }
        // 如果没有匹配到基础区段，尝试匹配无级类型（低4bit匹配）
        for(DeviceZoneEnum noticeTypeEnum : values){
            if((noticeTypeEnum.getTypeNum() & 0x0F) == bitCode){
                return noticeTypeEnum.getTypeNum();
            }
        }
        // 如果都没有匹配到，直接返回原值
        return bitCode;
    }

    /**
     * 获取所有区段枚举，按照 typeNum 排序（1-15顺序）
     * @return 按顺序排列的区段枚举集合
     */
    public static Set<EnumDto> getAllEnums() {
        DeviceZoneEnum[] values = DeviceZoneEnum.values();
        // 使用 LinkedHashSet 保持顺序，并按照 typeNum 排序
        return java.util.Arrays.stream(values)
                .filter(e -> e.getTypeNum() >= 1 && e.getTypeNum() <= 15) // 只返回1-15的基础区段
                .sorted((a, b) -> Integer.compare(a.getTypeNum(), b.getTypeNum())) // 按照 typeNum 排序
                .map(e -> {
                    EnumDto enumDto = new EnumDto();
                    enumDto.setNodeCode(e.getTypeNum());
                    enumDto.setNodeName(e.getValue());
                    return enumDto;
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
