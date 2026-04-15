package com.scsdky.web.domain.dto;

import lombok.Data;

/**
 * INFORMATION_SCHEMA.COLUMNS 查询行（MyBatis 映射）。
 */
@Data
public class InformationSchemaColumnRow {

    private String columnName;
    private String columnComment;
    private String dataType;
    private Integer ordinalPosition;
}
