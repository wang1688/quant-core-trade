package com.ruoyi.web.okx.strategynew.model;

import com.ruoyi.web.okx.strategynew.enums.Direction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 持仓模型
 */
public class Position {
    private String symbol;
    private Direction direction;
    private String strategyName;
    private BigDecimal entryPrice;
    private BigDecimal currentPrice;
    private BigDecimal quantity;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private LocalDateTime entryTime;
    private int klinesSinceEntry;
    private List<AddPosition> addPositions;
    private boolean isTrialPosition;

    public Position() {
        this.addPositions = new ArrayList<>();
    }

    public Position(String symbol, Direction direction, String strategyName,
                    BigDecimal entryPrice, BigDecimal quantity) {
        this.symbol = symbol;
        this.direction = direction;
        this.strategyName = strategyName;
        this.entryPrice = entryPrice;
        this.currentPrice = entryPrice;
        this.quantity = quantity;
        this.entryTime = LocalDateTime.now();
        this.klinesSinceEntry = 0;
        this.addPositions = new ArrayList<>();
        this.isTrialPosition = false;
    }

    // 计算浮动盈亏
    public BigDecimal getUnrealizedPnL() {
        BigDecimal priceDiff = direction == Direction.LONG ?
            currentPrice.subtract(entryPrice) :
            entryPrice.subtract(currentPrice);
        return priceDiff.multiply(quantity);
    }

    // 计算平均成本
    public BigDecimal getAverageCost() {
        BigDecimal totalCost = entryPrice.multiply(quantity);
        BigDecimal totalQty = quantity;

        for (AddPosition add : addPositions) {
            totalCost = totalCost.add(add.getPrice().multiply(add.getQuantity()));
            totalQty = totalQty.add(add.getQuantity());
        }

        return totalCost.divide(totalQty, 8, BigDecimal.ROUND_HALF_UP);
    }

    // 添加加仓记录
    public void addPosition(BigDecimal price, BigDecimal qty) {
        AddPosition add = new AddPosition(price, qty, LocalDateTime.now());
        addPositions.add(add);
        quantity = quantity.add(qty);
    }

    // Getters and Setters
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public BigDecimal getEntryPrice() {
        return entryPrice;
    }

    public void setEntryPrice(BigDecimal entryPrice) {
        this.entryPrice = entryPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }

    public BigDecimal getTakeProfit() {
        return takeProfit;
    }

    public void setTakeProfit(BigDecimal takeProfit) {
        this.takeProfit = takeProfit;
    }

    public LocalDateTime getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }

    public int getKlinesSinceEntry() {
        return klinesSinceEntry;
    }

    public void setKlinesSinceEntry(int klinesSinceEntry) {
        this.klinesSinceEntry = klinesSinceEntry;
    }

    public void incrementKlinesSinceEntry() {
        this.klinesSinceEntry++;
    }

    public List<AddPosition> getAddPositions() {
        return addPositions;
    }

    public boolean isTrialPosition() {
        return isTrialPosition;
    }

    public void setTrialPosition(boolean trialPosition) {
        isTrialPosition = trialPosition;
    }

    // 内部类：加仓记录
    public static class AddPosition {
        private BigDecimal price;
        private BigDecimal quantity;
        private LocalDateTime time;

        public AddPosition(BigDecimal price, BigDecimal quantity, LocalDateTime time) {
            this.price = price;
            this.quantity = quantity;
            this.time = time;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public LocalDateTime getTime() {
            return time;
        }
    }
}
