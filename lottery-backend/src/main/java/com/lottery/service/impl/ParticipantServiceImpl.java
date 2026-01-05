package com.lottery.service.impl;

import com.lottery.common.constant.ActivityStatus;
import com.lottery.common.constant.DrawMode;
import com.lottery.common.constant.ResultCode;
import com.lottery.common.exception.BusinessException;
import com.lottery.common.util.RedisUtil;
import com.lottery.dto.request.JoinActivityRequest;
import com.lottery.dto.response.JoinActivityResponse;
import com.lottery.dto.response.ParticipantStatusResponse;
import com.lottery.entity.Activity;
import com.lottery.entity.Participant;
import com.lottery.mapper.ActivityMapper;
import com.lottery.mapper.ParticipantMapper;
import com.lottery.service.LotteryService;
import com.lottery.service.ParticipantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 参与服务实现
 *
 * @author lottery
 * @since 2024-12-16
 */
@Slf4j
@Service
public class ParticipantServiceImpl implements ParticipantService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private LotteryService lotteryService;

    @Autowired
    private RedisUtil redisUtil;

    private static final String PARTICIPANT_COUNT_KEY = "lottery:participant:count:";
    private static final String USER_PARTICIPATED_KEY = "lottery:user:participated:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public JoinActivityResponse joinActivity(JoinActivityRequest request) {
        // 1. 查询活动
        Activity activity = activityMapper.selectByCode(request.getActivityCode());
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        // 2. 检查活动状态
        if (!ActivityStatus.ONGOING.getValue().equals(activity.getStatus())) {
            throw new BusinessException(ResultCode.ACTIVITY_FINISHED);
        }

        // 3. 检查是否已参与（Redis缓存 + 数据库双重检查）
        String userKey = USER_PARTICIPATED_KEY + request.getOpenid();
        Boolean hasParticipated = redisUtil.hasKey(userKey);
        
        if (Boolean.TRUE.equals(hasParticipated)) {
            // Redis中存在，直接返回
            throw new BusinessException(ResultCode.ALREADY_PARTICIPATED);
        }

        // 4. 创建参与记录
        Participant participant = new Participant();
        participant.setActivityId(activity.getId());
        participant.setOpenid(request.getOpenid());
        participant.setNickname(request.getNickname());
        participant.setAvatarUrl(request.getAvatarUrl());
        participant.setParticipateTime(LocalDateTime.now());
        participant.setIsWinner(0);

        try {
            participantMapper.insert(participant);
        } catch (DuplicateKeyException e) {
            // 唯一索引冲突，说明已参与
            throw new BusinessException(ResultCode.ALREADY_PARTICIPATED);
        }

        // 5. 缓存用户参与记录
        redisUtil.set(userKey, "1", 30, java.util.concurrent.TimeUnit.DAYS);

        // 6. 增加参与人数计数器
        String countKey = PARTICIPANT_COUNT_KEY + activity.getId();
        Long currentCount = redisUtil.increment(countKey);

        log.info("用户参与成功，活动ID: {}, OpenID: {}, 当前参与人数: {}", 
            activity.getId(), request.getOpenid(), currentCount);

        // 7. 构建响应
        JoinActivityResponse response = new JoinActivityResponse();
        response.setParticipantId(participant.getId());
        response.setActivityStatus(activity.getStatus());

        // 8. 根据开奖模式处理
        if (DrawMode.INSTANT.getValue().equals(activity.getDrawMode())) {
            // 即抽即中模式，立即执行抽奖
            boolean isWinner = lotteryService.executeInstantDraw(activity.getId(), participant.getId());
            response.setIsWinner(isWinner);
            response.setMessage(isWinner ? "恭喜中奖！" : "很遗憾，未中奖");
        } else if (DrawMode.COUNT.getValue().equals(activity.getDrawMode())) {
            // 人数开奖模式，检查是否达到目标人数
            if (currentCount >= activity.getTargetParticipantCount()) {
                // 异步触发开奖
                lotteryService.executeCountDraw(activity.getId());
            }
            response.setMessage("参与成功，等待开奖");
        } else {
            // 定时开奖模式
            response.setMessage("参与成功，等待开奖");
        }

        return response;
    }

    @Override
    public ParticipantStatusResponse getParticipantStatus(String activityCode, String openid) {
        // 1. 查询活动
        Activity activity = activityMapper.selectByCode(activityCode);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        // 2. 查询参与记录
        Participant participant = participantMapper.selectByActivityAndOpenid(activity.getId(), openid);

        // 3. 构建响应
        ParticipantStatusResponse response = new ParticipantStatusResponse();
        response.setActivityStatus(activity.getStatus());
        
        if (participant != null) {
            response.setHasParticipated(true);
            response.setIsWinner(participant.getIsWinner() == 1);
            response.setParticipateTime(participant.getParticipateTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            response.setHasParticipated(false);
            response.setIsWinner(false);
        }

        return response;
    }
}
