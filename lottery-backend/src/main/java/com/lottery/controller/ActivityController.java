package com.lottery.controller;

import com.lottery.common.response.Result;
import com.lottery.dto.request.CreateActivityRequest;
import com.lottery.dto.response.ActivityDetailResponse;
import com.lottery.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 活动控制器
 *
 * @author lottery
 * @since 2024-12-16
 */
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 创建活动
     */
    @PostMapping("/create")
    public Result<ActivityDetailResponse> create(@RequestBody @Valid CreateActivityRequest request) {
        ActivityDetailResponse response = activityService.createActivity(request);
        return Result.success(response);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/detail")
    public Result<ActivityDetailResponse> detail(@RequestParam String activityCode) {
        ActivityDetailResponse response = activityService.getActivityDetail(activityCode);
        return Result.success(response);
    }
}
