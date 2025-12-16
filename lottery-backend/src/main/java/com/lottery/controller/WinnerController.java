package com.lottery.controller;

import com.lottery.common.domain.Result;
import com.lottery.dto.response.WinnerListResponse;
import com.lottery.service.WinnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 中奖管理控制器
 *
 * @author lottery
 * @since 2024-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/winner")
@Tag(name = "中奖管理", description = "中奖名单查询接口")
public class WinnerController {

    @Autowired
    private WinnerService winnerService;

    /**
     * 查询中奖名单
     */
    @GetMapping("/list")
    @Operation(summary = "查询中奖名单", description = "根据活动编码查询中奖名单")
    public Result<WinnerListResponse> getWinnerList(@RequestParam String activityCode) {
        log.info("查询中奖名单，活动编码: {}", activityCode);
        WinnerListResponse response = winnerService.getWinnerList(activityCode);
        return Result.success(response);
    }
}
