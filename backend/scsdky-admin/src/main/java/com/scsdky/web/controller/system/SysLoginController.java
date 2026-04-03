package com.scsdky.web.controller.system;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.scsdky.common.constant.Constants;
import com.scsdky.common.core.domain.AjaxResult;
import com.scsdky.common.core.domain.entity.SysMenu;
import com.scsdky.common.core.domain.entity.SysUser;
import com.scsdky.common.core.domain.model.LoginBody;
import com.scsdky.common.core.domain.model.LoginUser;
import com.scsdky.common.utils.SecurityUtils;
import com.scsdky.framework.web.service.SysLoginService;
import com.scsdky.framework.web.service.SysPermissionService;
import com.scsdky.system.service.ISysMenuService;
import com.scsdky.system.service.ISysUserService;

/**
 * 登录验证
 *
 * @author leomc
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private ISysUserService userService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser user = loginUser.getUser();
        // 若 Redis 反序列化后 user 为 null（如 test_dev 等用户），则按 userId 重新查询
        if (user == null && loginUser.getUserId() != null) {
            user = userService.selectUserById(loginUser.getUserId());
        }
        if (user == null) {
            return AjaxResult.error("用户信息已失效，请重新登录");
        }
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
