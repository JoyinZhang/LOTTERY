package com.lottery.dto.response;

import lombok.Data;

/**
 * 参与状态响应
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class ParticipantStatusResponse {

    /**
     * 是否已参与
     */
    private Boolean hasParticipated;

    /**
     * 是否中奖
     */
    private Boolean isWinner;

    /**
     * 参与时间
     */
    private String participateTime;

    /**
     * 活动状态
     */
    private Integer activityStatus;
}
