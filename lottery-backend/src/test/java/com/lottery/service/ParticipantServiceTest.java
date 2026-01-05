package com.lottery.service;

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
import com.lottery.service.impl.ParticipantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 参与服务测试
 *
 * @author lottery
 * @since 2024-12-16
 */
@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {

    @Mock
    private ActivityMapper activityMapper;

    @Mock
    private ParticipantMapper participantMapper;

    @Mock
    private LotteryService lotteryService;

    @Mock
    private RedisUtil redisUtil;

    @InjectMocks
    private ParticipantServiceImpl participantService;

    private Activity activity;
    private JoinActivityRequest joinRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试活动
        activity = new Activity();
        activity.setId(1L);
        activity.setActivityCode("TEST123");
        activity.setTitle("测试活动");
        activity.setStatus(ActivityStatus.ONGOING.getValue());
        activity.setDrawMode(DrawMode.TIMED.getValue());
        activity.setPrizeCount(10);
        activity.setTargetParticipantCount(20);

        // 初始化参与请求
        joinRequest = new JoinActivityRequest();
        joinRequest.setActivityCode("TEST123");
        joinRequest.setOpenid("test_openid");
        joinRequest.setNickname("测试用户");
        joinRequest.setAvatarUrl("http://test.com/avatar.jpg");
    }

    @Test
    void testJoinActivity_Success() {
        // 准备数据
        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(participantMapper.insert(any(Participant.class))).thenReturn(1);
        doNothing().when(redisUtil).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(redisUtil.increment(anyString())).thenReturn(1L);

        // 执行测试
        JoinActivityResponse response = participantService.joinActivity(joinRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(ActivityStatus.ONGOING.getValue(), response.getActivityStatus());
        assertEquals("参与成功，等待开奖", response.getMessage());

        // 验证方法调用
        verify(participantMapper, times(1)).insert(any(Participant.class));
        verify(redisUtil, times(1)).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        verify(redisUtil, times(1)).increment(anyString());
    }

    @Test
    void testJoinActivity_AlreadyParticipated() {
        // 准备数据：Redis中已有参与记录
        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);
        when(redisUtil.hasKey(anyString())).thenReturn(true);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.joinActivity(joinRequest);
        });

        assertEquals(ResultCode.ALREADY_PARTICIPATED, exception.getCode());
        verify(participantMapper, never()).insert(any(Participant.class));
    }

    @Test
    void testJoinActivity_DuplicateKey() {
        // 准备数据：数据库唯一索引冲突
        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(participantMapper.insert(any(Participant.class))).thenThrow(new DuplicateKeyException(""));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.joinActivity(joinRequest);
        });

        assertEquals(ResultCode.ALREADY_PARTICIPATED, exception.getCode());
    }

    @Test
    void testJoinActivity_ActivityFinished() {
        // 准备数据：活动已结束
        activity.setStatus(ActivityStatus.FINISHED.getValue());
        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.joinActivity(joinRequest);
        });

        assertEquals(ResultCode.ACTIVITY_FINISHED, exception.getCode());
    }

    @Test
    void testJoinActivity_InstantMode() {
        // 准备数据：即抽即中模式
        activity.setDrawMode(DrawMode.INSTANT.getValue());
        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(participantMapper.insert(any(Participant.class))).thenReturn(1);
        doNothing().when(redisUtil).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(redisUtil.increment(anyString())).thenReturn(1L);
        when(lotteryService.executeInstantDraw(anyLong(), anyLong())).thenReturn(true);

        // 执行测试
        JoinActivityResponse response = participantService.joinActivity(joinRequest);

        // 验证结果
        assertNotNull(response);
        assertTrue(response.getIsWinner());
        assertEquals("恭喜中奖！", response.getMessage());
        verify(lotteryService, times(1)).executeInstantDraw(anyLong(), anyLong());
    }

    @Test
    void testJoinActivity_CountMode_ReachTarget() {
        // 准备数据：人数开奖模式，达到目标人数
        activity.setDrawMode(DrawMode.COUNT.getValue());
        activity.setTargetParticipantCount(10);
        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);
        when(redisUtil.hasKey(anyString())).thenReturn(false);
        when(participantMapper.insert(any(Participant.class))).thenReturn(1);
        doNothing().when(redisUtil).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        when(redisUtil.increment(anyString())).thenReturn(10L);
        doNothing().when(lotteryService).executeCountDraw(anyLong());

        // 执行测试
        JoinActivityResponse response = participantService.joinActivity(joinRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("参与成功，等待开奖", response.getMessage());
        verify(lotteryService, times(1)).executeCountDraw(1L);
    }

    @Test
    void testGetParticipantStatus_Participated() {
        // 准备数据
        Participant participant = new Participant();
        participant.setId(1L);
        participant.setActivityId(1L);
        participant.setOpenid("test_openid");
        participant.setIsWinner(1);
        participant.setParticipateTime(LocalDateTime.now());

        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);
        when(participantMapper.selectByActivityAndOpenid(1L, "test_openid")).thenReturn(participant);

        // 执行测试
        ParticipantStatusResponse response = participantService.getParticipantStatus("TEST123", "test_openid");

        // 验证结果
        assertNotNull(response);
        assertTrue(response.getHasParticipated());
        assertTrue(response.getIsWinner());
        assertEquals(ActivityStatus.ONGOING.getValue(), response.getActivityStatus());
    }

    @Test
    void testGetParticipantStatus_NotParticipated() {
        // 准备数据
        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);
        when(participantMapper.selectByActivityAndOpenid(1L, "test_openid")).thenReturn(null);

        // 执行测试
        ParticipantStatusResponse response = participantService.getParticipantStatus("TEST123", "test_openid");

        // 验证结果
        assertNotNull(response);
        assertFalse(response.getHasParticipated());
        assertFalse(response.getIsWinner());
    }

    @Test
    void testGetParticipantStatus_ActivityNotFound() {
        // 准备数据
        when(activityMapper.selectByCode("TEST123")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            participantService.getParticipantStatus("TEST123", "test_openid");
        });

        assertEquals(ResultCode.ACTIVITY_NOT_FOUND, exception.getCode());
    }
}
