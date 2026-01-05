package com.lottery.service.impl;

import com.lottery.common.constant.ActivityStatus;
import com.lottery.common.constant.ResultCode;
import com.lottery.common.exception.BusinessException;
import com.lottery.common.util.RedisUtil;
import com.lottery.entity.Activity;
import com.lottery.entity.Participant;
import com.lottery.entity.Winner;
import com.lottery.mapper.ActivityMapper;
import com.lottery.mapper.ParticipantMapper;
import com.lottery.mapper.WinnerMapper;
import com.lottery.service.LotteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 抽奖服务实现
 *
 * @author lottery
 * @since 2024-12-16
 */
@Slf4j
@Service
public class LotteryServiceImpl implements LotteryService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ParticipantMapper participantMapper;

    @Autowired
    private WinnerMapper winnerMapper;

    @Autowired
    private RedisUtil redisUtil;

    private static final String LOTTERY_LOCK_KEY = "lottery:lock:";
    private static final String PRIZE_STOCK_KEY = "lottery:prize:stock:";
    private static final Random random = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean executeInstantDraw(Long activityId, Long participantId) {
        log.info("开始执行即抽即中抽奖，活动ID: {}, 参与者ID: {}", activityId, participantId);

        // 1. 查询活动信息
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
        }

        // 2. 检查活动状态
        if (!ActivityStatus.ONGOING.getValue().equals(activity.getStatus())) {
            log.warn("活动已结束，无法抽奖，活动ID: {}", activityId);
            return false;
        }

        // 3. 获取Redis中的奖品库存
        String stockKey = PRIZE_STOCK_KEY + activityId;
        Long stock = redisUtil.get(stockKey, Long.class);
        
        if (stock == null) {
            // 初始化库存
            int currentWinnerCount = winnerMapper.countByActivityId(activityId);
            stock = (long) (activity.getPrizeCount() - currentWinnerCount);
            redisUtil.set(stockKey, stock, 1, TimeUnit.DAYS);
        }

        // 4. 检查库存
        if (stock <= 0) {
            log.info("奖品已发完，活动ID: {}", activityId);
            return false;
        }

        // 5. 计算中奖概率并抽奖
        int totalParticipants = participantMapper.countByActivityId(activityId);
        double winProbability = stock.doubleValue() / totalParticipants;
        
        // 保证概率在合理范围内
        if (winProbability > 1.0) {
            winProbability = 1.0;
        }

        boolean isWinner = random.nextDouble() < winProbability;

        if (isWinner) {
            // 6. 扣减库存（使用Redis原子操作）
            Long remainStock = redisUtil.decrement(stockKey);
            
            if (remainStock < 0) {
                // 库存不足，回滚
                redisUtil.increment(stockKey);
                log.warn("库存扣减失败，活动ID: {}, 参与者ID: {}", activityId, participantId);
                return false;
            }

            // 7. 记录中奖信息
            Participant participant = participantMapper.selectById(participantId);
            if (participant == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR);
            }

            Winner winner = new Winner();
            winner.setActivityId(activityId);
            winner.setParticipantId(participantId);
            winner.setOpenid(participant.getOpenid());
            winner.setNickname(participant.getNickname());
            winner.setAvatarUrl(participant.getAvatarUrl());
            winner.setWinTime(LocalDateTime.now());
            winnerMapper.insert(winner);

            // 8. 更新参与记录的中奖状态
            participantMapper.updateWinnerStatus(participantId, 1);

            log.info("即抽即中成功，活动ID: {}, 参与者ID: {}, 剩余库存: {}", 
                activityId, participantId, remainStock);

            // 9. 检查是否所有奖品已发完
            if (remainStock == 0) {
                activity.setStatus(ActivityStatus.FINISHED.getValue());
                activityMapper.updateStatus(activityId, ActivityStatus.FINISHED.getValue());
                log.info("奖品已全部发完，活动结束，活动ID: {}", activityId);
            }

            return true;
        }

        log.info("即抽即中未中奖，活动ID: {}, 参与者ID: {}", activityId, participantId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeCountDraw(Long activityId) {
        log.info("开始执行人数开奖，活动ID: {}", activityId);

        // 1. 获取分布式锁，防止重复开奖
        String lockKey = LOTTERY_LOCK_KEY + activityId;
        boolean lockSuccess = redisUtil.setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        
        if (!lockSuccess) {
            log.warn("获取开奖锁失败，可能正在开奖中，活动ID: {}", activityId);
            return;
        }

        try {
            // 2. 查询活动信息
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
            }

            // 3. 检查活动状态
            if (!ActivityStatus.ONGOING.getValue().equals(activity.getStatus())) {
                log.warn("活动已结束，无法开奖，活动ID: {}", activityId);
                return;
            }

            // 4. 检查是否已开奖
            int existWinnerCount = winnerMapper.countByActivityId(activityId);
            if (existWinnerCount > 0) {
                log.warn("活动已开奖，活动ID: {}", activityId);
                return;
            }

            // 5. 执行抽奖
            executeDraw(activity);

        } finally {
            // 释放锁
            redisUtil.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeTimedDraw(Long activityId) {
        log.info("开始执行定时开奖，活动ID: {}", activityId);

        // 1. 获取分布式锁，防止重复开奖
        String lockKey = LOTTERY_LOCK_KEY + activityId;
        boolean lockSuccess = redisUtil.setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        
        if (!lockSuccess) {
            log.warn("获取开奖锁失败，可能正在开奖中，活动ID: {}", activityId);
            return;
        }

        try {
            // 2. 查询活动信息
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                throw new BusinessException(ResultCode.ACTIVITY_NOT_FOUND);
            }

            // 3. 检查活动状态
            if (!ActivityStatus.ONGOING.getValue().equals(activity.getStatus())) {
                log.warn("活动已结束，无法开奖，活动ID: {}", activityId);
                return;
            }

            // 4. 检查是否已开奖
            int existWinnerCount = winnerMapper.countByActivityId(activityId);
            if (existWinnerCount > 0) {
                log.warn("活动已开奖，活动ID: {}", activityId);
                return;
            }

            // 5. 检查是否到达开奖时间
            if (activity.getDrawTime() != null && LocalDateTime.now().isBefore(activity.getDrawTime())) {
                log.warn("未到开奖时间，活动ID: {}, 开奖时间: {}", activityId, activity.getDrawTime());
                return;
            }

            // 6. 执行抽奖
            executeDraw(activity);

        } finally {
            // 释放锁
            redisUtil.delete(lockKey);
        }
    }

    /**
     * 执行抽奖（Fisher-Yates洗牌算法）
     */
    private void executeDraw(Activity activity) {
        Long activityId = activity.getId();
        
        // 1. 查询所有参与者
        List<Participant> participants = participantMapper.selectByActivityId(activityId);
        
        if (participants == null || participants.isEmpty()) {
            log.warn("没有参与者，无法开奖，活动ID: {}", activityId);
            return;
        }

        int participantCount = participants.size();
        int prizeCount = activity.getPrizeCount();

        // 奖品数不能超过参与人数
        if (prizeCount > participantCount) {
            prizeCount = participantCount;
            log.warn("奖品数超过参与人数，调整为: {}, 活动ID: {}", prizeCount, activityId);
        }

        // 2. 使用Fisher-Yates洗牌算法随机抽取中奖者
        List<Participant> winners = fisherYatesShuffle(participants, prizeCount);

        // 3. 批量保存中奖记录
        List<Winner> winnerRecords = new ArrayList<>();
        List<Long> winnerParticipantIds = new ArrayList<>();
        LocalDateTime winTime = LocalDateTime.now();

        for (Participant participant : winners) {
            Winner winner = new Winner();
            winner.setActivityId(activityId);
            winner.setParticipantId(participant.getId());
            winner.setOpenid(participant.getOpenid());
            winner.setNickname(participant.getNickname());
            winner.setAvatarUrl(participant.getAvatarUrl());
            winner.setWinTime(winTime);
            winnerRecords.add(winner);
            winnerParticipantIds.add(participant.getId());
        }

        if (!winnerRecords.isEmpty()) {
            winnerMapper.batchInsert(winnerRecords);
            participantMapper.batchUpdateWinnerStatus(winnerParticipantIds);
        }

        // 4. 更新活动状态为已开奖
        activityMapper.updateStatus(activityId, ActivityStatus.FINISHED.getValue());

        log.info("开奖完成，活动ID: {}, 参与人数: {}, 中奖人数: {}", 
            activityId, participantCount, winners.size());
    }

    /**
     * Fisher-Yates洗牌算法
     * 时间复杂度O(n)，空间复杂度O(1)
     * 
     * @param participants 参与者列表
     * @param count 需要抽取的数量
     * @return 中奖者列表
     */
    private List<Participant> fisherYatesShuffle(List<Participant> participants, int count) {
        int n = participants.size();
        List<Participant> shuffled = new ArrayList<>(participants);

        // Fisher-Yates洗牌算法
        for (int i = 0; i < count; i++) {
            // 从[i, n-1]范围内随机选择一个索引
            int randomIndex = i + random.nextInt(n - i);
            
            // 交换元素
            Participant temp = shuffled.get(i);
            shuffled.set(i, shuffled.get(randomIndex));
            shuffled.set(randomIndex, temp);
        }

        // 返回前count个元素作为中奖者
        return shuffled.subList(0, count);
    }
}
