package com.scsdky.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scsdky.common.config.ScsdkyConfig;
import com.scsdky.common.utils.StringUtils;

/**
 * 首页
 */
@RestController
public class SysIndexController
{
    /** 系统基础配置 */
    @Autowired
    private ScsdkyConfig scsdkyConfig;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index()
    {
        return "";
    }
}
