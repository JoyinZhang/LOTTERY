package com.lottery.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 参与记录实体
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class Participant {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 用户OpenID
     */
    private String openid;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 参与时间
     */
    private LocalDateTime participateTime;

    /**
     * 是否中奖：0-未中奖，1-已中奖
     */
    private Integer isWinner;
}
