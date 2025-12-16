package com.lottery.common.constant;

/**
 * 响应码枚举
 *
 * @author lottery
 * @since 2024-12-16
 */
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(0, "成功"),

    /**
     * 参数校验失败
     */
    PARAM_ERROR(1001, "参数校验失败"),

    /**
     * 活动不存在
     */
    ACTIVITY_NOT_FOUND(1002, "活动不存在"),

    /**
     * 活动已结束
     */
    ACTIVITY_FINISHED(1003, "活动已结束"),

    /**
     * 用户已参与过
     */
    ALREADY_PARTICIPATED(1004, "用户已参与过"),

    /**
     * 奖品已抽完
     */
    PRIZE_EXHAUSTED(1005, "奖品已抽完"),

    /**
     * 微信授权失败
     */
    WECHAT_AUTH_FAILED(2001, "微信授权失败"),

    /**
     * 系统繁忙
     */
    SYSTEM_BUSY(5001, "系统繁忙，请稍后重试"),

    /**
     * 系统内部错误
     */
    SYSTEM_ERROR(5000, "系统内部错误");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
