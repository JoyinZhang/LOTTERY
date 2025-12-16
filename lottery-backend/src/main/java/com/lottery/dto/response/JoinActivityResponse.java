package com.lottery.dto.response;

import lombok.Data;

/**
 * 参与抽奖响应
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class JoinActivityResponse {

    /**
     * 参与记录ID
     */
    private Long participantId;

    /**
     * 是否中奖（仅即抽即中模式返回）
     */
    private Boolean isWinner;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 活动状态
     */
    private Integer activityStatus;
}
