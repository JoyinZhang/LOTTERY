package com.lottery.dto.response;

import lombok.Data;

/**
 * 微信登录响应
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class WechatLoginResponse {

    /**
     * 用户OpenID
     */
    private String openid;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;
}
