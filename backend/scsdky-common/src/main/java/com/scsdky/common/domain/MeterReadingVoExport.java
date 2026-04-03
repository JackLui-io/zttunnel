package com.scsdky.common.domain;


/**
 * @author tubo
 * 电表度数vo
 * @date 2024/10/31
 */
public class MeterReadingVoExport {

    /**
     * 回路名称
     */
    private String loopName;

    /**
     * 电表度数
     */
    private String dataValue;

    public MeterReadingVoExport(String loopName, String dataValue) {
        this.loopName = loopName;
        this.dataValue = dataValue;
    }

    public String getLoopName() {
        return loopName;
    }

    public void setLoopName(String loopName) {
        this.loopName = loopName;
    }

    public String getDataValue() {
        return dataValue;
    }

    public void setDataValue(String dataValue) {
        this.dataValue = dataValue;
    }

}
