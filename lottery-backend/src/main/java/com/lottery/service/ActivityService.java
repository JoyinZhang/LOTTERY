package com.lottery.service;

import com.lottery.dto.request.CreateActivityRequest;
import com.lottery.dto.response.ActivityDetailResponse;

/**
 * 活动服务接口
 *
 * @author lottery
 * @since 2024-12-16
 */
public interface ActivityService {

    /**
     * 创建活动
     */
    ActivityDetailResponse createActivity(CreateActivityRequest request);

    /**
     * 获取活动详情
     */
    ActivityDetailResponse getActivityDetail(String activityCode);

    /**
     * 更新活动状态
     */
    void updateActivityStatus(Long activityId, Integer status);
}
