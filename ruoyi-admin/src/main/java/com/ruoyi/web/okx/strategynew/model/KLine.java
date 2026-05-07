package com.ruoyi.web.okx.strategynew.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * K线数据模型
 */
public class KLine {
    private LocalDateTime timestamp;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal volume;
    private String timeframe;

    public KLine() {
    }

    public KLine(LocalDateTime timestamp, BigDecimal open, BigDecimal high,
                 BigDecimal low, BigDecimal close, BigDecimal volume, String timeframe) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.timeframe = timeframe;
    }

    // 实体大小
    public BigDecimal getBodySize() {
        return close.subtract(open).abs();
    }

    // 上影线长度
    public BigDecimal getUpperShadow() {
        BigDecimal maxBody = open.max(close);
        return high.subtract(maxBody);
    }

    // 下影线长度
    public BigDecimal getLowerShadow() {
        BigDecimal minBody = open.min(close);
        return minBody.subtract(low);
    }

    // 是否阳线
    public boolean isBullish() {
        return close.compareTo(open) > 0;
    }

    // 是否阴线
    public boolean isBearish() {
        return close.compareTo(open) < 0;
    }

    // 全部范围（高-低）
    public BigDecimal getRange() {
        return high.subtract(low);
    }

    // Getters and Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public String getTimeframe() {
        return timeframe;
    }

    public void setTimeframe(String timeframe) {
        this.timeframe = timeframe;
    }
}
