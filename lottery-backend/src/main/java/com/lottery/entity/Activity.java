package com.lottery.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 活动实体
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class Activity {

    /**
     * 活动ID
     */
    private Long id;

    /**
     * 活动唯一编码
     */
    private String activityCode;

    /**
     * 创建者OpenID
     */
    private String creatorOpenid;

    /**
     * 活动标题
     */
    private String title;

    /**
     * 抽奖说明
     */
    private String description;

    /**
     * 开奖模式：1-定时，2-人数，3-即抽即中
     */
    private Integer drawMode;

    /**
     * 定时开奖时间
     */
    private LocalDateTime drawTime;

    /**
     * 目标参与人数
     */
    private Integer targetParticipantCount;

    /**
     * 奖品份数
     */
    private Integer prizeCount;

    /**
     * 活动状态：1-进行中，2-已开奖，3-已取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
