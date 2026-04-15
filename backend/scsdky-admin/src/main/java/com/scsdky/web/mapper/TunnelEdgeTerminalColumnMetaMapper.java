package com.scsdky.web.mapper;

import com.scsdky.web.domain.dto.InformationSchemaColumnRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 读取当前库表列注释（仅 SELECT INFORMATION_SCHEMA）。
 */
public interface TunnelEdgeTerminalColumnMetaMapper {

    @Select("SELECT COLUMN_NAME AS columnName, COLUMN_COMMENT AS columnComment, DATA_TYPE AS dataType, "
        + "ORDINAL_POSITION AS ordinalPosition FROM INFORMATION_SCHEMA.COLUMNS "
        + "WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = #{tableName} "
        + "ORDER BY ORDINAL_POSITION")
    List<InformationSchemaColumnRow> selectColumnMeta(@Param("tableName") String tableName);
}
