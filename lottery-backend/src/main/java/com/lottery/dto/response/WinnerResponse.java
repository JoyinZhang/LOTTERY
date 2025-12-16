package com.lottery.dto.response;

import lombok.Data;

/**
 * 中奖者响应DTO
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class WinnerResponse {

    /**
     * 中奖者ID
     */
    private Long id;

    /**
     * 中奖者昵称
     */
    private String nickname;

    /**
     * 中奖者头像
     */
    private String avatarUrl;

    /**
     * 中奖时间
     */
    private String winTime;
}
