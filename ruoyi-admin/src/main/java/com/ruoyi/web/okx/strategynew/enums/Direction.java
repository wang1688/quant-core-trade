package com.ruoyi.web.okx.strategynew.enums;

/**
 * 交易方向
 */
public enum Direction {
    LONG("做多"),
    SHORT("做空");

    private final String description;

    Direction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Direction reverse() {
        return this == LONG ? SHORT : LONG;
    }
}
