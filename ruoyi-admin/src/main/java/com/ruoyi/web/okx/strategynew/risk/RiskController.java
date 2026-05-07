package com.ruoyi.web.okx.strategynew.risk;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.model.Position;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 风控管理器
 */
public class RiskController {

    private double totalCapital;
    private double dailyPnL;
    private double cumulativePnL;
    private int consecutiveLosses;
    private int consecutiveTrialLosses;
    private List<TradeRecord> recentTrades;
    private boolean circuitBreakerActive;
    private int riskLevel;

    public RiskController(double totalCapital, int riskLevel) {
        this.totalCapital = totalCapital;
        this.riskLevel = riskLevel;
        this.dailyPnL = 0.0;
        this.cumulativePnL = 0.0;
        this.consecutiveLosses = 0;
        this.consecutiveTrialLosses = 0;
        this.recentTrades = new ArrayList<>();
        this.circuitBreakerActive = false;
    }

    /**
     * 检查是否允许开仓
     */
    public boolean canOpenPosition(boolean isTrialPosition) {
        if (circuitBreakerActive) {
            return false;
        }

        if (isDailyLossLimitReached()) {
            return false;
        }

        if (isCumulativeLossLimitReached()) {
            return false;
        }

        if (consecutiveLosses >= Constants.CONSECUTIVE_LOSS_LIMIT) {
            return false;
        }

        if (isTrialPosition && consecutiveTrialLosses >= Constants.TRIAL_LOSS_LIMIT_1H) {
            return false;
        }

        return true;
    }

    /**
     * 检查日亏损是否达到阈值
     */
    public boolean isDailyLossLimitReached() {
        double dailyLossRatio = Math.abs(dailyPnL) / totalCapital;
        return dailyLossRatio >= Constants.DAILY_LOSS_THRESHOLD[riskLevel];
    }

    /**
     * 检查累计亏损是否达到阈值
     */
    public boolean isCumulativeLossLimitReached() {
        double cumulativeLossRatio = Math.abs(cumulativePnL) / totalCapital;
        return cumulativeLossRatio >= Constants.CUMULATIVE_LOSS_THRESHOLD[riskLevel];
    }

    /**
     * 记录交易结果
     */
    public void recordTrade(double pnl, boolean isTrialPosition) {
        TradeRecord record = new TradeRecord(pnl, isTrialPosition);
        recentTrades.add(record);

        dailyPnL += pnl;
        cumulativePnL += pnl;

        if (pnl < 0) {
            consecutiveLosses++;
            if (isTrialPosition) {
                consecutiveTrialLosses++;
            }
        } else {
            consecutiveLosses = 0;
            consecutiveTrialLosses = 0;
        }

        checkCircuitBreaker();
    }

    /**
     * 检查是否触发熔断
     */
    private void checkCircuitBreaker() {
        if (recentTrades.size() < 4) {
            return;
        }

        int coreTrendLosses = 0;
        for (int i = recentTrades.size() - 4; i < recentTrades.size(); i++) {
            TradeRecord record = recentTrades.get(i);
            if (!record.isTrialPosition && record.pnl < 0) {
                coreTrendLosses++;
            }
        }

        if (coreTrendLosses >= 4) {
            circuitBreakerActive = true;
        }
    }

    /**
     * 重置熔断（连续2笔盈利）
     */
    public void checkCircuitBreakerReset() {
        if (!circuitBreakerActive) {
            return;
        }

        if (recentTrades.size() < 2) {
            return;
        }

        int size = recentTrades.size();
        boolean lastTwoProfit = recentTrades.get(size - 1).pnl > 0 &&
                                recentTrades.get(size - 2).pnl > 0;

        if (lastTwoProfit) {
            circuitBreakerActive = false;
        }
    }

    /**
     * 计算仓位调整系数（连续亏损后减半）
     */
    public double getPositionAdjustmentForLosses() {
        if (consecutiveLosses >= 3) {
            int profitCount = 0;
            for (int i = recentTrades.size() - 1; i >= 0 && i >= recentTrades.size() - 3; i--) {
                if (recentTrades.get(i).pnl > 0) {
                    profitCount++;
                }
            }

            if (profitCount == 0) {
                return 0.5;
            } else if (profitCount == 1) {
                return 0.6;
            } else if (profitCount == 2) {
                return 0.8;
            } else {
                return 1.0;
            }
        }

        return 1.0;
    }

    /**
     * 重置日亏损（每日开始时调用）
     */
    public void resetDailyPnL() {
        this.dailyPnL = 0.0;
    }

    /**
     * 检查是否需要升档
     */
    public boolean shouldUpgradeRiskLevel(double ema30Angle, double strongSignalRatio) {
        if (recentTrades.size() < 3) {
            return false;
        }

        int size = recentTrades.size();
        boolean lastThreeProfit = recentTrades.get(size - 1).pnl > 0 &&
                                  recentTrades.get(size - 2).pnl > 0 &&
                                  recentTrades.get(size - 3).pnl > 0;

        boolean strongTrend = Math.abs(ema30Angle) >= 20.0;
        boolean highStrongSignalRatio = strongSignalRatio >= 0.7;

        return lastThreeProfit && strongTrend && highStrongSignalRatio;
    }

    /**
     * 检查是否需要降档
     */
    public boolean shouldDowngradeRiskLevel(boolean isHighVolatility) {
        double dailyLossRatio = Math.abs(dailyPnL) / totalCapital;
        double threshold = Constants.DAILY_LOSS_THRESHOLD[riskLevel] * 0.5;

        return dailyLossRatio >= threshold && isHighVolatility;
    }

    // Getters
    public double getDailyPnL() {
        return dailyPnL;
    }

    public double getCumulativePnL() {
        return cumulativePnL;
    }

    public int getConsecutiveLosses() {
        return consecutiveLosses;
    }

    public boolean isCircuitBreakerActive() {
        return circuitBreakerActive;
    }

    public void setCircuitBreakerActive(boolean active) {
        this.circuitBreakerActive = active;
    }

    // 内部类：交易记录
    private static class TradeRecord {
        double pnl;
        boolean isTrialPosition;

        TradeRecord(double pnl, boolean isTrialPosition) {
            this.pnl = pnl;
            this.isTrialPosition = isTrialPosition;
        }
    }
}
