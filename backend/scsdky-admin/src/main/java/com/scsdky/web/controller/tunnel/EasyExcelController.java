package com.scsdky.web.controller.tunnel;

import com.scsdky.web.service.IEasyExcelService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author tubo
 * @date 2024/01/25
 */
@RestController
@RequestMapping("/easyExcel")
public class EasyExcelController {

    @Resource
    private IEasyExcelService easyExcelService;

    @PostMapping("/excelInput")
    public void excelInput(MultipartFile file) throws Exception {
        easyExcelService.excelInput(file);
    }

}
