package com.lottery.common.constant;

/**
 * 活动状态枚举
 *
 * @author lottery
 * @since 2024-12-16
 */
public enum ActivityStatus {

    /**
     * 进行中
     */
    ONGOING(1, "进行中"),

    /**
     * 已开奖
     */
    FINISHED(2, "已开奖"),

    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

    private final Integer value;
    private final String description;

    ActivityStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static ActivityStatus valueOf(Integer value) {
        for (ActivityStatus status : ActivityStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
