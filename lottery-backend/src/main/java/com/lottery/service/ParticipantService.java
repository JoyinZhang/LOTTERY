package com.lottery.service;

import com.lottery.dto.request.JoinActivityRequest;
import com.lottery.dto.response.JoinActivityResponse;
import com.lottery.dto.response.ParticipantStatusResponse;

/**
 * 参与服务接口
 *
 * @author lottery
 * @since 2024-12-16
 */
public interface ParticipantService {

    /**
     * 参与抽奖
     */
    JoinActivityResponse joinActivity(JoinActivityRequest request);

    /**
     * 查询参与状态
     */
    ParticipantStatusResponse getParticipantStatus(String activityCode, String openid);
}
