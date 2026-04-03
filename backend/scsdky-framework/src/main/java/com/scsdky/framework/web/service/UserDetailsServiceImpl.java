package com.scsdky.framework.web.service;

import com.scsdky.system.mapper.SysUserPostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.scsdky.common.core.domain.entity.SysUser;
import com.scsdky.common.core.domain.model.LoginUser;
import com.scsdky.framework.datasource.DynamicDataSourceContextHolder;
import com.scsdky.common.enums.UserStatus;
import com.scsdky.common.exception.ServiceException;
import com.scsdky.common.utils.StringUtils;
import com.scsdky.system.service.ISysUserService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户验证处理
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService
{
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private SysUserPostMapper sysUserPostMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        String dsType = DynamicDataSourceContextHolder.getDataSourceType();
        log.info("[登录调试] loadUserByUsername: username={}, dataSourceType={}",
                username, dsType != null ? dsType : "DEFAULT(MASTER)");
        SysUser user = userService.selectUserByUserName(username);
        if (StringUtils.isNull(user))
        {
            log.info("登录用户：{} 不存在.", username);
            throw new ServiceException("登录用户：" + username + " 不存在");
        }
        List<String> postNames = sysUserPostMapper.getPostNameByUserId(user.getUserId());
        user.setPostName(postNames.stream().collect(Collectors.joining(",")));
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            log.info("登录用户：{} 已被删除.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            log.info("登录用户：{} 已被停用.", username);
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUser user)
    {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
