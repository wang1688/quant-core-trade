package com.ruoyi.web.okx.strategynew.backtest;

import com.ruoyi.web.okx.strategynew.enums.Direction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单笔交易结果
 */
public class TradeResult {
    private String symbol;
    private String strategyName;
    private Direction direction;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal quantity;
    private BigDecimal pnl;
    private String exitReason;
    private int holdingBars;

    public TradeResult() {
    }

    public TradeResult(String symbol, String strategyName, Direction direction,
                       LocalDateTime entryTime, BigDecimal entryPrice, BigDecimal quantity) {
        this.symbol = symbol;
        this.strategyName = strategyName;
        this.direction = direction;
        this.entryTime = entryTime;
        this.entryPrice = entryPrice;
        this.quantity = quantity;
    }

    /**
     * 平仓
     */
    public void close(LocalDateTime exitTime, BigDecimal exitPrice, String exitReason, int holdingBars) {
        this.exitTime = exitTime;
        this.exitPrice = exitPrice;
        this.exitReason = exitReason;
        this.holdingBars = holdingBars;

        calculatePnL();
    }

    /**
     * 计算盈亏
     */
    private void calculatePnL() {
        BigDecimal priceDiff;
        if (direction == Direction.LONG) {
            priceDiff = exitPrice.subtract(entryPrice);
        } else {
            priceDiff = entryPrice.subtract(exitPrice);
        }

        this.pnl = priceDiff.multiply(quantity);
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public LocalDateTime getExitTime() {
        return exitTime;
    }

    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getExitPrice() {
        return exitPrice;
    }

    public void setExitPrice(BigDecimal exitPrice) {
        this.exitPrice = exitPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPnl() {
        return pnl;
    }

    public void setPnl(BigDecimal pnl) {
        this.pnl = pnl;
    }

    public String getExitReason() {
        return exitReason;
    }

    public void setExitReason(String exitReason) {
        this.exitReason = exitReason;
    }

    public int getHoldingBars() {
        return holdingBars;
    }

    public void setHoldingBars(int holdingBars) {
        this.holdingBars = holdingBars;
    }
}
