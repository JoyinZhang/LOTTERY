package com.lottery.controller;

import com.lottery.common.response.Result;
import com.lottery.dto.request.WechatLoginRequest;
import com.lottery.dto.response.WechatLoginResponse;
import com.lottery.service.WechatAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 微信授权控制器
 *
 * @author lottery
 * @since 2024-12-16
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private WechatAuthService wechatAuthService;

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public Result<WechatLoginResponse> login(@RequestBody @Valid WechatLoginRequest request) {
        WechatLoginResponse response = wechatAuthService.login(request);
        return Result.success(response);
    }
}
