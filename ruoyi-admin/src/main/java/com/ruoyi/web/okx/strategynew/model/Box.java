package com.ruoyi.web.okx.strategynew.model;

import java.math.BigDecimal;

/**
 * 箱体/中枢模型
 */
public class Box {
    private BigDecimal upper;
    private BigDecimal lower;
    private BigDecimal middle;
    private int klineCount;
    private boolean stable;
    private String timeframe;

    public Box() {
    }

    public Box(BigDecimal upper, BigDecimal lower, int klineCount, String timeframe) {
        this.upper = upper;
        this.lower = lower;
        this.middle = upper.add(lower).divide(BigDecimal.valueOf(2));
        this.klineCount = klineCount;
        this.timeframe = timeframe;
        this.stable = klineCount >= 18;
    }

    public BigDecimal getHeight() {
        return upper.subtract(lower);
    }

    public boolean isInside(BigDecimal price) {
        return price.compareTo(lower) >= 0 && price.compareTo(upper) <= 0;
    }

    public boolean isBreakoutUp(BigDecimal price, int ticks) {
        BigDecimal threshold = upper.add(BigDecimal.valueOf(ticks * 0.0001));
        return price.compareTo(threshold) > 0;
    }

    public boolean isBreakoutDown(BigDecimal price, int ticks) {
        BigDecimal threshold = lower.subtract(BigDecimal.valueOf(ticks * 0.0001));
        return price.compareTo(threshold) < 0;
    }

    // Getters and Setters
    public BigDecimal getUpper() {
        return upper;
    }

    public void setUpper(BigDecimal upper) {
        this.upper = upper;
        if (lower != null) {
            this.middle = upper.add(lower).divide(BigDecimal.valueOf(2));
        }
    }

    public BigDecimal getLower() {
        return lower;
    }

    public void setLower(BigDecimal lower) {
        this.lower = lower;
        if (upper != null) {
            this.middle = upper.add(lower).divide(BigDecimal.valueOf(2));
        }
    }

    public BigDecimal getMiddle() {
        return middle;
    }

    public int getKlineCount() {
        return klineCount;
    }

    public void setKlineCount(int klineCount) {
        this.klineCount = klineCount;
        this.stable = klineCount >= 18;
    }

    public boolean isStable() {
        return stable;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }
}
