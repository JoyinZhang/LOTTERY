package com.lottery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 参与抽奖请求
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class JoinActivityRequest {

    /**
     * 活动编码
     */
    @NotBlank(message = "活动编码不能为空")
    private String activityCode;

    /**
     * 用户OpenID
     */
    @NotBlank(message = "用户OpenID不能为空")
    private String openid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;
}
