package com.lottery.service;

import com.lottery.common.constant.ActivityStatus;
import com.lottery.common.constant.DrawMode;
import com.lottery.common.constant.ResultCode;
import com.lottery.common.exception.BusinessException;
import com.lottery.dto.request.CreateActivityRequest;
import com.lottery.dto.response.ActivityDetailResponse;
import com.lottery.entity.Activity;
import com.lottery.mapper.ActivityMapper;
import com.lottery.service.impl.ActivityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 活动服务测试
 *
 * @author lottery
 * @since 2024-12-16
 */
@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private ActivityServiceImpl activityService;

    private CreateActivityRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateActivityRequest();
        createRequest.setCreatorOpenid("test_creator");
        createRequest.setTitle("测试活动");
        createRequest.setDescription("这是一个测试活动");
        createRequest.setPrizeCount(10);
    }

    @Test
    void testCreateActivity_TimedMode_Success() {
        // 准备数据：定时开奖模式
        createRequest.setDrawMode(DrawMode.TIMED.getValue());
        LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
        createRequest.setDrawTime(futureTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        when(activityMapper.insert(any(Activity.class))).thenReturn(1);

        // 执行测试
        ActivityDetailResponse response = activityService.createActivity(createRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("测试活动", response.getTitle());
        assertEquals(DrawMode.TIMED.getValue(), response.getDrawMode());
        assertEquals(ActivityStatus.ONGOING.getValue(), response.getStatus());
        assertNotNull(response.getActivityCode());
        verify(activityMapper, times(1)).insert(any(Activity.class));
    }

    @Test
    void testCreateActivity_CountMode_Success() {
        // 准备数据：人数开奖模式
        createRequest.setDrawMode(DrawMode.COUNT.getValue());
        createRequest.setTargetParticipantCount(20);

        when(activityMapper.insert(any(Activity.class))).thenReturn(1);

        // 执行测试
        ActivityDetailResponse response = activityService.createActivity(createRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(DrawMode.COUNT.getValue(), response.getDrawMode());
        assertEquals(20, response.getTargetParticipantCount());
        verify(activityMapper, times(1)).insert(any(Activity.class));
    }

    @Test
    void testCreateActivity_InstantMode_Success() {
        // 准备数据：即抽即中模式
        createRequest.setDrawMode(DrawMode.INSTANT.getValue());

        when(activityMapper.insert(any(Activity.class))).thenReturn(1);

        // 执行测试
        ActivityDetailResponse response = activityService.createActivity(createRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals(DrawMode.INSTANT.getValue(), response.getDrawMode());
        verify(activityMapper, times(1)).insert(any(Activity.class));
    }

    @Test
    void testCreateActivity_InvalidTitle() {
        // 准备数据：标题为空
        createRequest.setTitle("");
        createRequest.setDrawMode(DrawMode.INSTANT.getValue());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.createActivity(createRequest);
        });

        assertTrue(exception.getMessage().contains("标题"));
        verify(activityMapper, never()).insert(any(Activity.class));
    }

    @Test
    void testCreateActivity_InvalidPrizeCount() {
        // 准备数据：奖品数量为0
        createRequest.setPrizeCount(0);
        createRequest.setDrawMode(DrawMode.INSTANT.getValue());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.createActivity(createRequest);
        });

        assertTrue(exception.getMessage().contains("奖品份数"));
        verify(activityMapper, never()).insert(any(Activity.class));
    }

    @Test
    void testCreateActivity_TimedMode_DrawTimeInPast() {
        // 准备数据：定时开奖时间在过去
        createRequest.setDrawMode(DrawMode.TIMED.getValue());
        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        createRequest.setDrawTime(pastTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.createActivity(createRequest);
        });

        assertTrue(exception.getMessage().contains("开奖时间"));
        verify(activityMapper, never()).insert(any(Activity.class));
    }

    @Test
    void testCreateActivity_CountMode_InvalidTargetCount() {
        // 准备数据：目标人数小于奖品数
        createRequest.setDrawMode(DrawMode.COUNT.getValue());
        createRequest.setTargetParticipantCount(5);
        createRequest.setPrizeCount(10);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.createActivity(createRequest);
        });

        assertTrue(exception.getMessage().contains("目标人数"));
        verify(activityMapper, never()).insert(any(Activity.class));
    }

    @Test
    void testGetActivityDetail_Success() {
        // 准备数据
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setActivityCode("TEST123");
        activity.setTitle("测试活动");
        activity.setDescription("测试描述");
        activity.setDrawMode(DrawMode.INSTANT.getValue());
        activity.setPrizeCount(10);
        activity.setStatus(ActivityStatus.ONGOING.getValue());

        when(activityMapper.selectByCode("TEST123")).thenReturn(activity);

        // 执行测试
        ActivityDetailResponse response = activityService.getActivityDetail("TEST123");

        // 验证结果
        assertNotNull(response);
        assertEquals("TEST123", response.getActivityCode());
        assertEquals("测试活动", response.getTitle());
        verify(activityMapper, times(1)).selectByCode("TEST123");
    }

    @Test
    void testGetActivityDetail_NotFound() {
        // 准备数据
        when(activityMapper.selectByCode("NOTEXIST")).thenReturn(null);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            activityService.getActivityDetail("NOTEXIST");
        });

        assertEquals(ResultCode.ACTIVITY_NOT_FOUND, exception.getCode());
    }
}
