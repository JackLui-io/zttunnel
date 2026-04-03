package com.scsdky.web.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 管理员配置类
 * 支持从多个路径读取管理员邮箱配置：
 * 1. Linux 固定路径：/var/www/java/admin-config.properties
 * 2. 项目目录：./admin-config.properties
 * 3. jar包同级目录：{user.dir}/admin-config.properties
 * 4. classpath：classpath:admin-config.properties
 * 
 * @author system
 */
@Slf4j
@Data
@Component
public class AdminConfig {

    /**
     * 管理员邮箱列表
     */
    private List<String> adminEmails = new ArrayList<>();

    /**
     * 管理员手机号列表（可选）
     */
    private List<String> adminPhones = new ArrayList<>();

    /**
     * 配置文件路径（固定路径，Ubuntu 22.04系统）
     */
    private static final String CONFIG_FILE_PATH_LINUX = "/var/www/java/admin-config.properties";
    
    /**
     * 配置文件名称
     */
    private static final String CONFIG_FILE_NAME = "admin-config.properties";

    /**
     * 初始化配置，从多个可能的路径读取配置文件
     */
    @PostConstruct
    public void init() {
        loadConfig();
    }

    /**
     * 加载配置文件
     * 按以下顺序尝试加载：
     * 1. Linux 固定路径：/var/www/java/admin-config.properties
     * 2. 项目目录：./admin-config.properties
     * 3. jar包同级目录：../admin-config.properties
     * 4. classpath：classpath:admin-config.properties
     */
    public void loadConfig() {
        List<String> configPaths = new ArrayList<>();
        
        // 1. Linux 固定路径（生产环境）
        configPaths.add(CONFIG_FILE_PATH_LINUX);
        
        // 2. 当前工作目录（开发环境，mvn spring-boot:run 时通常是项目根目录）
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            // 项目根目录
            configPaths.add(userDir + File.separator + CONFIG_FILE_NAME);
            // scsdky-admin 子目录（如果配置文件在子目录中）
            configPaths.add(userDir + File.separator + "scsdky-admin" + File.separator + CONFIG_FILE_NAME);
            // 相对路径
            configPaths.add("./" + CONFIG_FILE_NAME);
            configPaths.add("./scsdky-admin/" + CONFIG_FILE_NAME);
        }
        
        // 3. classpath（打包后可以从jar包内读取）
        configPaths.add("classpath:" + CONFIG_FILE_NAME);
        
        // 尝试从各个路径加载配置
        for (String configPath : configPaths) {
            if (loadConfigFromPath(configPath)) {
                log.info("[管理员配置] 成功从路径加载配置: {}", configPath);
                return;
            }
        }
        
        // 所有路径都失败，尝试使用默认邮箱
        log.warn("[管理员配置] 所有配置文件路径都未找到，尝试使用默认配置");
        String defaultEmail = System.getProperty("admin.email");
        if (defaultEmail != null && !defaultEmail.trim().isEmpty()) {
            adminEmails.add(defaultEmail.trim());
            log.info("[管理员配置] 使用默认邮箱: {}", defaultEmail);
        }
    }
    
    /**
     * 从指定路径加载配置文件
     * 
     * @param configPath 配置文件路径
     * @return 是否成功加载
     */
    private boolean loadConfigFromPath(String configPath) {
        try {
            InputStream inputStream = null;
            
            if (configPath.startsWith("classpath:")) {
                // 从 classpath 加载
                String resourcePath = configPath.substring("classpath:".length());
                inputStream = AdminConfig.class.getClassLoader().getResourceAsStream(resourcePath);
                if (inputStream == null) {
                    log.debug("[管理员配置] classpath 中未找到配置文件: {}", resourcePath);
                    return false;
                }
                log.info("[管理员配置] 尝试从 classpath 加载配置文件: {}", resourcePath);
            } else {
                // 从文件系统加载
                File configFile = new File(configPath);
                log.debug("[管理员配置] 尝试从文件系统加载配置文件: {}", configPath);
                
                if (!configFile.exists() || !configFile.isFile()) {
                    log.debug("[管理员配置] 配置文件不存在: {}", configPath);
                    return false;
                }
                
                inputStream = new FileInputStream(configFile);
            }
            
            // 读取配置文件
            Properties props = new Properties();
            try (InputStream is = inputStream) {
                props.load(is);
            }
            
            // 清空之前的配置
            adminEmails.clear();
            adminPhones.clear();
            
            // 读取管理员邮箱（支持多个，用逗号分隔）
            String emails = props.getProperty("admin.emails", "").trim();
            if (!emails.isEmpty()) {
                String[] emailArray = emails.split(",");
                for (String email : emailArray) {
                    email = email.trim();
                    if (!email.isEmpty() && email.contains("@")) {
                        adminEmails.add(email);
                        log.info("[管理员配置] 加载管理员邮箱: {}", email);
                    }
                }
            }

            // 读取管理员手机号（可选，支持多个，用逗号分隔）
            String phones = props.getProperty("admin.phones", "").trim();
            if (!phones.isEmpty()) {
                String[] phoneArray = phones.split(",");
                for (String phone : phoneArray) {
                    phone = phone.trim();
                    if (!phone.isEmpty()) {
                        adminPhones.add(phone);
                        log.info("[管理员配置] 加载管理员手机号: {}", phone);
                    }
                }
            }

            log.info("[管理员配置] 配置加载完成 - 邮箱数量: {}, 手机号数量: {}", 
                    adminEmails.size(), adminPhones.size());
            
            if (adminEmails.isEmpty()) {
                log.warn("[管理员配置] 未配置管理员邮箱，设备掉线通知将无法发送");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.debug("[管理员配置] 从路径加载配置失败: {}, 错误: {}", configPath, e.getMessage());
            return false;
        }
    }

    /**
     * 重新加载配置（支持热更新）
     */
    public void reload() {
        adminEmails.clear();
        adminPhones.clear();
        loadConfig();
    }
}

