package com.lottery.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 中奖记录实体
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class Winner {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 参与记录ID
     */
    private Long participantId;

    /**
     * 中奖者OpenID
     */
    private String openid;

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
    private LocalDateTime winTime;
}
