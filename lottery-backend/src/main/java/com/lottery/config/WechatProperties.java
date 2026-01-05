package com.lottery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信配置属性
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
@Component
@ConfigurationProperties(prefix = "wechat")
public class WechatProperties {

    /**
     * 小程序AppID
     */
    private String appid;

    /**
     * 小程序AppSecret
     */
    private String secret;

    /**
     * Token过期时间（秒）
     */
    private Long tokenExpireTime;
}
