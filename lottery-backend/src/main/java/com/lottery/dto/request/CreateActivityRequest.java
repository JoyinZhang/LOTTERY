package com.lottery.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 创建活动请求
 *
 * @author lottery
 * @since 2024-12-16
 */
@Data
public class CreateActivityRequest {

    /**
     * 创建者OpenID
     */
    @NotBlank(message = "创建者OpenID不能为空")
    private String creatorOpenid;

    /**
     * 活动标题
     */
    @NotBlank(message = "活动标题不能为空")
    @Size(min = 1, max = 24, message = "活动标题长度为1-24个字符")
    private String title;

    /**
     * 抽奖说明
     */
    @Size(max = 200, message = "抽奖说明不能超过200字")
    private String description;

    /**
     * 开奖模式：1-定时，2-人数，3-即抽即中
     */
    @NotNull(message = "开奖模式不能为空")
    @Min(value = 1, message = "开奖模式值错误")
    @Max(value = 3, message = "开奖模式值错误")
    private Integer drawMode;

    /**
     * 定时开奖时间
     */
    private String drawTime;

    /**
     * 目标参与人数
     */
    private Integer targetParticipantCount;

    /**
     * 奖品份数
     */
    @NotNull(message = "奖品份数不能为空")
    @Min(value = 1, message = "奖品份数必须大于0")
    @Max(value = 999, message = "奖品份数不能超过999")
    private Integer prizeCount;
}
