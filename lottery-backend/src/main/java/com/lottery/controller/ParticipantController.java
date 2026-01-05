package com.lottery.controller;

import com.lottery.common.response.Result;
import com.lottery.dto.request.JoinActivityRequest;
import com.lottery.dto.response.JoinActivityResponse;
import com.lottery.dto.response.ParticipantStatusResponse;
import com.lottery.service.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 参与控制器
 *
 * @author lottery
 * @since 2024-12-16
 */
@RestController
@RequestMapping("/api/participant")
public class ParticipantController {

    @Autowired
    private ParticipantService participantService;

    /**
     * 参与抽奖
     */
    @PostMapping("/join")
    public Result<JoinActivityResponse> join(@RequestBody @Valid JoinActivityRequest request) {
        JoinActivityResponse response = participantService.joinActivity(request);
        return Result.success(response);
    }

    /**
     * 查询参与状态
     */
    @GetMapping("/status")
    public Result<ParticipantStatusResponse> status(
            @RequestParam String activityCode,
            @RequestParam String openid) {
        ParticipantStatusResponse response = participantService.getParticipantStatus(activityCode, openid);
        return Result.success(response);
    }
}
