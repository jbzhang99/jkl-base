package com.jkinvest.jkl.base.log.properties;

import lombok.Data;
import lombok.NoArgsConstructor;

import static com.jkinvest.jkl.base.log.properties.OptLogProperties.PREFIX;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 操作日志配置类
 *
 * @author zuihou
 * @date 2020年03月09日15:00:47
 */
@ConfigurationProperties(prefix = PREFIX)
@Data
@NoArgsConstructor
public class OptLogProperties {
    public static final String PREFIX = "zuihou.log";

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 日志存储类型
     */
    private OptLogType type = OptLogType.DB;
}
