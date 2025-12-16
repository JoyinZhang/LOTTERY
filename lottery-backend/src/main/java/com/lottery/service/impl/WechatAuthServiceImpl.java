package com.lottery.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lottery.common.constant.ResultCode;
import com.lottery.common.exception.BusinessException;
import com.lottery.config.WechatProperties;
import com.lottery.dto.request.WechatLoginRequest;
import com.lottery.dto.response.WechatLoginResponse;
import com.lottery.service.WechatAuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 微信授权服务实现
 *
 * @author lottery
 * @since 2024-12-16
 */
@Slf4j
@Service
public class WechatAuthServiceImpl implements WechatAuthService {

    @Autowired
    private WechatProperties wechatProperties;

    private static final String WECHAT_AUTH_URL = "https://api.weixin.qq.com/sns/jscode2session";
    private static final String JWT_SECRET = "lottery_secret_key_for_jwt_token_generation_32chars";

    @Override
    public WechatLoginResponse login(WechatLoginRequest request) {
        // 调用微信接口获取openid
        String openid = getOpenidFromWechat(request.getCode());
        
        if (StrUtil.isBlank(openid)) {
            throw new BusinessException(ResultCode.WECHAT_AUTH_FAILED);
        }

        // 生成JWT token
        String token = generateToken(openid);

        // 构建响应
        WechatLoginResponse response = new WechatLoginResponse();
        response.setOpenid(openid);
        response.setToken(token);
        response.setNickname(request.getNickname());
        response.setAvatarUrl(request.getAvatarUrl());

        return response;
    }

    @Override
    public String validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            log.error("Token验证失败", e);
            return null;
        }
    }

    /**
     * 从微信服务器获取openid
     */
    private String getOpenidFromWechat(String code) {
        try {
            String url = String.format("%s?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    WECHAT_AUTH_URL, wechatProperties.getAppid(), wechatProperties.getSecret(), code);

            String result = HttpUtil.get(url);
            JSONObject json = JSONUtil.parseObj(result);

            if (json.containsKey("openid")) {
                return json.getStr("openid");
            } else {
                log.error("微信授权失败: {}", result);
                return null;
            }
        } catch (Exception e) {
            log.error("调用微信接口失败", e);
            return null;
        }
    }

    /**
     * 生成JWT token
     */
    private String generateToken(String openid) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Date expireDate = new Date(System.currentTimeMillis() + wechatProperties.getTokenExpireTime() * 1000);

        return Jwts.builder()
                .subject(openid)
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }
}
