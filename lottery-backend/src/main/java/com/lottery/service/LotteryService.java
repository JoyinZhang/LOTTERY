package com.lottery.service;

/**
 * 抽奖服务接口
 *
 * @author lottery
 * @since 2024-12-16
 */
public interface LotteryService {

    /**
     * 执行即抽即中抽奖
     * 
     * @param activityId 活动ID
     * @param participantId 参与者ID
     * @return 是否中奖
     */
    boolean executeInstantDraw(Long activityId, Long participantId);

    /**
     * 执行人数开奖
     * 
     * @param activityId 活动ID
     */
    void executeCountDraw(Long activityId);

    /**
     * 执行定时开奖
     * 
     * @param activityId 活动ID
     */
    void executeTimedDraw(Long activityId);
}
