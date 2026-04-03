package com.scsdky.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scsdky.web.domain.TunnelEmail;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件发送记录表 Mapper接口
 * 
 * @author system
 */
@Mapper
public interface TunnelEmailMapper extends BaseMapper<TunnelEmail> {
    
}

