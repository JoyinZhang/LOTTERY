package com.lottery.service.impl;

import com.lottery.common.constant.ResultCode;
import com.lottery.common.exception.BusinessException;
import com.lottery.dto.response.WinnerListResponse;
import com.lottery.dto.response.WinnerResponse;
import com.lottery.entity.Activity;
import com.lottery.entity.Winner;
import com.lottery.mapper.ActivityMapper;
import com.lottery.mapper.WinnerMapper;
import com.lottery.service.WinnerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 中奖管理服务实现
 *
 * @author lottery
 * @since 2024-12-16
 */
@Slf4j
@Service
public class WinnerServiceImpl implements WinnerService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private WinnerMapper winnerMapper;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public WinnerListResponse getWinnerList(String activityCode) {
        log.info("查询中奖名单，活动编码: {}", activityCode);

        // 1. 查询活动信息
        Activity activity = activityMapper.selectByCode(activityCode);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        // 2. 查询中奖名单
        List<Winner> winners = winnerMapper.selectByActivityId(activity.getId());

        // 3. 构建响应
        WinnerListResponse response = new WinnerListResponse();
        response.setActivityTitle(activity.getTitle());
        response.setActivityStatus(activity.getStatus());
        response.setWinnerCount(winners != null ? winners.size() : 0);

        List<WinnerResponse> winnerList = new ArrayList<>();
        if (winners != null && !winners.isEmpty()) {
            for (Winner winner : winners) {
                WinnerResponse winnerResponse = new WinnerResponse();
                winnerResponse.setId(winner.getId());
                winnerResponse.setNickname(winner.getNickname());
                winnerResponse.setAvatarUrl(winner.getAvatarUrl());
                winnerResponse.setWinTime(winner.getWinTime().format(formatter));
                winnerList.add(winnerResponse);
            }
        }
        response.setWinners(winnerList);

        return response;
    }
}
