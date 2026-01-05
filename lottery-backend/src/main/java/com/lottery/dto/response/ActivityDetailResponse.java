package com.lottery.dto.response;

import lombok.Data;

/**
 * 活动详情响应
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class ActivityDetailResponse {

    private Long activityId;
    private String activityCode;
    private String title;
    private String description;
    private Integer drawMode;
    private String drawTime;
    private Integer targetParticipantCount;
    private Integer prizeCount;
    private Integer remainPrizeCount;
    private Integer participantCount;
    private Integer status;
    private String shareUrl;
}
