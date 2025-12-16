package com.lottery.common.constant;

/**
 * 开奖模式枚举
 *
 * @author lottery
 * @since 2024-12-16
 */
public enum DrawMode {

    /**
     * 定时开奖
     */
    TIMED(1, "定时开奖"),

    /**
     * 人数开奖
     */
    COUNT(2, "人数开奖"),

    /**
     * 即抽即中
     */
    INSTANT(3, "即抽即中");

    private final Integer value;
    private final String description;

    DrawMode(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static DrawMode valueOf(Integer value) {
        for (DrawMode mode : DrawMode.values()) {
            if (mode.getValue().equals(value)) {
                return mode;
            }
        }
        return null;
    }
}
