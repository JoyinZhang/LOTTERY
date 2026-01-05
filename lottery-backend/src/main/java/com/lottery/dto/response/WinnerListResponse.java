package com.lottery.dto.response;

import lombok.Data;
import java.util.List;

/**
 * 中奖名单响应DTO
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class WinnerListResponse {

    /**
     * 活动标题
     */
    private String activityTitle;

    /**
     * 活动状态
     */
    private Integer activityStatus;

    /**
     * 中奖人数
     */
    private Integer winnerCount;

    /**
     * 中奖名单
     */
    private List<WinnerResponse> winners;
}
