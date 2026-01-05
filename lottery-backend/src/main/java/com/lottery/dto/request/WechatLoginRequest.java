package com.lottery.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class WechatLoginRequest {

    /**
     * 微信登录code
     */
    @NotBlank(message = "登录code不能为空")
    private String code;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;
}
