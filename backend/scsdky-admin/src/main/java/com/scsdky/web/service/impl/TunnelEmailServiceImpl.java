package com.scsdky.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scsdky.web.domain.TunnelEmail;
import com.scsdky.web.mapper.TunnelEmailMapper;
import com.scsdky.web.service.TunnelEmailService;
import org.springframework.stereotype.Service;

/**
 * 邮件发送记录表 服务实现类
 * 
 * @author system
 */
@Service
public class TunnelEmailServiceImpl 
        extends ServiceImpl<TunnelEmailMapper, TunnelEmail>
        implements TunnelEmailService {
    
}

