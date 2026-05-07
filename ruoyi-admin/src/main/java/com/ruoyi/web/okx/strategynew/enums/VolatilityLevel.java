package com.ruoyi.web.okx.strategynew.enums;

/**
 * 波动率级别
 */
public enum VolatilityLevel {
    LOW("低波"),
    MID("中波"),
    HIGH("高波"),
    EXTREME("极端高波");

    private final String description;

    VolatilityLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
