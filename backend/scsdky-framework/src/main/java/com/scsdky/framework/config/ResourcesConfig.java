package com.scsdky.framework.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.scsdky.common.config.ScsdkyConfig;
import com.scsdky.common.constant.Constants;
import com.scsdky.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 通用配置
 *
 * @author leomc
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer
{
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        /** 本地文件上传路径 */
        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations("file:" + ScsdkyConfig.getProfile() + "/");

        /** swagger配置 */
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter()
    {
        CorsConfiguration config = new CorsConfiguration();
        // ========== 原代码（已注释）==========
        // 原代码同时设置了 setAllowCredentials(true) 和 addAllowedOrigin("*")
        // 这在 Spring 中是不兼容的，会导致 CORS 错误
        // config.setAllowCredentials(true);
        // config.addAllowedOrigin("*");
        
        // ========== 新代码 ==========
        // Spring Boot 2.3.7 使用 Spring Framework 5.2.x，不支持 addAllowedOriginPattern
        // 解决方案：允许所有源，但不允许携带凭证（大多数情况下可以工作）
        // 如果需要携带凭证，需要指定具体的源地址，不能使用 "*"
        config.addAllowedOrigin("*");  // 允许所有源
        config.setAllowCredentials(false);  // 不允许携带凭证（与 "*" 兼容）
        
        // 如果需要携带凭证（如 Cookie、Authorization 头），需要指定具体的源地址：
        // config.addAllowedOrigin("http://8.130.167.111:8090");  // 前端地址
        // config.addAllowedOrigin("http://localhost:8090");  // 本地开发
        // config.setAllowCredentials(true);
        
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }
}
