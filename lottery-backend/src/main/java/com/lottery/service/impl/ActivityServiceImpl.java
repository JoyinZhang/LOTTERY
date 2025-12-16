package com.lottery.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.lottery.common.constant.ActivityStatus;
import com.lottery.common.constant.DrawMode;
import com.lottery.common.constant.ResultCode;
import com.lottery.common.exception.BusinessException;
import com.lottery.dto.request.CreateActivityRequest;
import com.lottery.dto.response.ActivityDetailResponse;
import com.lottery.entity.Activity;
import com.lottery.mapper.ActivityMapper;
import com.lottery.mapper.ParticipantMapper;
import com.lottery.mapper.WinnerMapper;
import com.lottery.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 活动服务实现
 *
 * @author lottery
 * @since 2024-12-16
 */
@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private WinnerMapper winnerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityDetailResponse createActivity(CreateActivityRequest request) {
        // 参数校验
        validateCreateRequest(request);

        // 生成活动编码
        String activityCode = generateActivityCode();

        // 构建活动实体
        Activity activity = buildActivity(request, activityCode);

        // 保存到数据库
        activityMapper.insert(activity);

        log.info("创建活动成功，活动ID: {}, 活动编码: {}", activity.getId(), activityCode);

        // 返回响应
        return buildDetailResponse(activity);
    }

    @Override
    public ActivityDetailResponse getActivityDetail(String activityCode) {
        Activity activity = activityMapper.selectByCode(activityCode);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        ActivityDetailResponse response = buildDetailResponse(activity);

        // 查询参与人数
        int participantCount = participantMapper.countByActivityId(activity.getId());
        response.setParticipantCount(participantCount);

        // 查询剩余奖品数
        int winnerCount = winnerMapper.countByActivityId(activity.getId());
        response.setRemainPrizeCount(activity.getPrizeCount() - winnerCount);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateActivityStatus(Long activityId, Integer status) {
        activityMapper.updateStatus(activityId, status);
        log.info("更新活动状态成功，活动ID: {}, 状态: {}", activityId, status);
    }

    /**
     * 验证创建请求
     */
    private void validateCreateRequest(CreateActivityRequest request) {
        // 定时开奖验证
        if (DrawMode.TIMED.getValue().equals(request.getDrawMode())) {
            if (StrUtil.isBlank(request.getDrawTime())) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "定时开奖必须设置开奖时间");
            }
            LocalDateTime drawTime = LocalDateTime.parse(request.getDrawTime(), 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (drawTime.isBefore(LocalDateTime.now().plusMinutes(5))) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "开奖时间必须晚于当前时间5分钟");
            }
        }

        // 人数开奖验证
        if (DrawMode.COUNT.getValue().equals(request.getDrawMode())) {
            if (request.getTargetParticipantCount() == null || 
                request.getTargetParticipantCount() < request.getPrizeCount()) {
                throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "目标人数必须大于等于奖品份数");
            }
        }
    }

    /**
     * 生成活动编码
     */
    private String generateActivityCode() {
        return UUID.randomUUID().toString(true).substring(0, 16).toUpperCase();
    }

    /**
     * 构建活动实体
     */
    private Activity buildActivity(CreateActivityRequest request, String activityCode) {
        Activity activity = new Activity();
        activity.setActivityCode(activityCode);
        activity.setCreatorOpenid(request.getCreatorOpenid());
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setDrawMode(request.getDrawMode());
        activity.setPrizeCount(request.getPrizeCount());
        activity.setStatus(ActivityStatus.ONGOING.getValue());
        activity.setCreateTime(LocalDateTime.now());
        activity.setUpdateTime(LocalDateTime.now());

        if (DrawMode.TIMED.getValue().equals(request.getDrawMode())) {
            activity.setDrawTime(LocalDateTime.parse(request.getDrawTime(), 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        if (DrawMode.COUNT.getValue().equals(request.getDrawMode())) {
            activity.setTargetParticipantCount(request.getTargetParticipantCount());
        }

        return activity;
    }

    /**
     * 构建详情响应
     */
    private ActivityDetailResponse buildDetailResponse(Activity activity) {
        ActivityDetailResponse response = new ActivityDetailResponse();
        response.setActivityId(activity.getId());
        response.setActivityCode(activity.getActivityCode());
        response.setTitle(activity.getTitle());
        response.setDescription(activity.getDescription());
        response.setDrawMode(activity.getDrawMode());
        response.setPrizeCount(activity.getPrizeCount());
        response.setStatus(activity.getStatus());
        response.setTargetParticipantCount(activity.getTargetParticipantCount());
        
        if (activity.getDrawTime() != null) {
            response.setDrawTime(activity.getDrawTime().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        response.setShareUrl("/pages/activity/activity?code=" + activity.getActivityCode());
        return response;
    }
}
