package com.scsdky.common.utils;

import com.github.pagehelper.PageHelper;
import com.scsdky.common.core.page.PageDomain;
import com.scsdky.common.core.page.TableSupport;
import com.scsdky.common.utils.sql.SqlUtil;

import java.util.Optional;

/**
 * 分页工具类
 *
 * @author leomc
 */
public class PageUtils extends PageHelper
{
    /**
     * 设置请求分页数据
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        PageHelper.startPage(Optional.ofNullable(pageNum).orElse(1), Optional.ofNullable(pageSize).orElse(10), orderBy);

    }
}
