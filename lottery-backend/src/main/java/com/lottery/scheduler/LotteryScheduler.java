package com.lottery.scheduler;

import com.lottery.common.constant.ActivityStatus;
import com.lottery.common.constant.DrawMode;
import com.lottery.entity.Activity;
import com.lottery.mapper.ActivityMapper;
import com.lottery.service.LotteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时开奖调度器
 *
 * @author lottery
 * @since 2024-12-16
 */
@Slf4j
@Component
public class LotteryScheduler {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private LotteryService lotteryService;

    /**
     * 定时扫描待开奖的活动
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanPendingDrawActivities() {
        log.info("开始扫描待开奖活动，当前时间: {}", LocalDateTime.now());

        try {
            // 查询待开奖的定时活动
            List<Activity> activities = activityMapper.selectPendingTimedActivities(LocalDateTime.now());

            if (activities == null || activities.isEmpty()) {
                log.debug("暂无待开奖活动");
                return;
            }

            log.info("发现 {} 个待开奖活动", activities.size());

            // 逐个执行开奖
            for (Activity activity : activities) {
                try {
                    if (DrawMode.TIMED.getValue().equals(activity.getDrawMode())) {
                        log.info("执行定时开奖，活动ID: {}, 活动标题: {}", 
                            activity.getId(), activity.getTitle());
                        lotteryService.executeTimedDraw(activity.getId());
                    }
                } catch (Exception e) {
                    log.error("定时开奖失败，活动ID: {}", activity.getId(), e);
                }
            }

        } catch (Exception e) {
            log.error("扫描待开奖活动异常", e);
        }
    }

    /**
     * 清理过期数据
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        log.info("开始清理过期数据，当前时间: {}", LocalDateTime.now());
        // TODO: 清理30天前的已结束活动数据（可选）
    }
}
