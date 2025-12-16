package com.lottery.service;

import com.lottery.dto.response.WinnerListResponse;

/**
 * 中奖管理服务接口
 *
 * @author lottery
 * @since 2024-12-16
 */
public interface WinnerService {

    /**
     * 查询中奖名单
     * 
     * @param activityCode 活动编码
     * @return 中奖名单
     */
    WinnerListResponse getWinnerList(String activityCode);
}
