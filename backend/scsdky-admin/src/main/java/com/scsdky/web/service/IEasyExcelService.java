package com.scsdky.web.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author tubo
 * @date 2024/01/25
 */
public interface IEasyExcelService {
    /**
     * 导入excel 多级sheet
     * @param file 文件
     */
    void excelInput(MultipartFile file) throws Exception;
}
