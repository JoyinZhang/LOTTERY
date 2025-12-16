package com.lottery.service;

import com.lottery.dto.request.WechatLoginRequest;
import com.lottery.dto.response.WechatLoginResponse;

/**
 * 微信授权服务接口
 *
 * @author lottery
 * @since 2024-12-16
 */
public interface WechatAuthService {

    /**
     * 微信登录
     */
    WechatLoginResponse login(WechatLoginRequest request);

    /**
     * 验证token
     */
    String validateToken(String token);
}
