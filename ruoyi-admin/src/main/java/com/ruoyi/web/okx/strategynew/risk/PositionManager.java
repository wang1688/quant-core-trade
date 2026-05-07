package com.ruoyi.web.okx.strategynew.risk;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import java.math.BigDecimal;


/**
 * 仓位管理器
 */
public class PositionManager {

    private int riskLevel;
    private double totalCapital;
    private String symbol;
    
    public PositionManager(double totalCapital, String symbol) {
        this.totalCapital = totalCapital;
        this.symbol = symbol;
        this.riskLevel = 1;
    }

    /**
     * 计算首仓开仓数量
     */
    public BigDecimal calculateInitialPosition(double signalScore, VolatilityLevel volatilityLevel,
                                                boolean liquidityWarning, BigDecimal currentPrice) {
        double riskExposure = Constants.RISK_EXPOSURE[riskLevel];
        double riskAmount = totalCapital * riskExposure / 100.0;

        double positionMultiplier = getPositionMultiplier(signalScore);

        double adjustmentFactor = getAdjustmentFactor(volatilityLevel, liquidityWarning);

        double margin = riskAmount * positionMultiplier * adjustmentFactor;

        double nominalAmount = margin * Constants.LEVERAGE;

        BigDecimal quantity = BigDecimal.valueOf(nominalAmount)
            .divide(currentPrice, 8, BigDecimal.ROUND_HALF_UP);

        return quantity;
    }

    /**
     * 根据信号评分获取仓位系数
     */
    private double getPositionMultiplier(double score) {
        if (score >= Constants.SIGNAL_SCORE_SUPER) {
            return 1.2;
        } else if (score >= Constants.SIGNAL_SCORE_STRONG) {
            return 1.0;
        } else if (score >= Constants.SIGNAL_SCORE_MID) {
            return 0.8;
        } else {
            return 0.8;
        }
    }

    /**
     * 获取调整系数（高波/流动性预警）
     */
    private double getAdjustmentFactor(VolatilityLevel level, boolean liquidityWarning) {
        if (liquidityWarning) {
            return 0.5;
        }

        switch (level) {
            case HIGH:
                return 0.8;
            case EXTREME:
                return 0.3;
            default:
                return 1.0;
        }
    }

    /**
     * 计算试单仓位（40%基准仓位）
     */
    public BigDecimal calculateTrialPosition(double signalScore, VolatilityLevel volatilityLevel,
                                              BigDecimal currentPrice) {
        BigDecimal normalPosition = calculateInitialPosition(signalScore, volatilityLevel, false, currentPrice);
        return normalPosition.multiply(BigDecimal.valueOf(0.4));
    }

    /**
     * 计算加仓数量
     */
    public BigDecimal calculateAddPosition(BigDecimal initialQuantity, int addCount, String level) {
        double ratio;

        if ("1H".equals(level)) {
            if (addCount <= 2) {
                ratio = 1.0;
            } else {
                ratio = 0.7;
            }
        } else if ("15M".equals(level)) {
            if (addCount <= 3) {
                ratio = 1.0;
            } else {
                ratio = 0.5;
            }
        } else if ("5M".equals(level)) {
            if (addCount <= 2) {
                ratio = 1.0;
            } else if (addCount == 3) {
                ratio = 0.8;
            } else if (addCount == 4) {
                ratio = 0.5;
            } else {
                ratio = 0.3;
            }
        } else {
            ratio = 1.0;
        }

        return initialQuantity.multiply(BigDecimal.valueOf(ratio));
    }

    /**
     * 检查仓位是否超限
     */
    public boolean isPositionLimitExceeded(double currentPositionRatio, String strategyType) {
        switch (strategyType) {
            case "CORE_TREND":
                return currentPositionRatio > Constants.POSITION_LIMIT_SINGLE;
            case "COUNTER_TREND":
                return currentPositionRatio > Constants.POSITION_LIMIT_COUNTER;
            case "NAKED_K":
                return currentPositionRatio > Constants.POSITION_LIMIT_NAKED;
            case "RANGE":
                return currentPositionRatio > Constants.POSITION_LIMIT_RANGE;
            default:
                return currentPositionRatio > Constants.POSITION_LIMIT_SINGLE;
        }
    }

    /**
     * 升档
     */
    public void upgradeRiskLevel() {
        if (riskLevel < Constants.RISK_EXPOSURE.length - 1) {
            riskLevel++;
        }
    }

    /**
     * 降档
     */
    public void downgradeRiskLevel() {
        if (riskLevel > 0) {
            riskLevel--;
        }
    }

    /**
     * 获取当前档位日亏损阈值
     */
    public double getDailyLossThreshold() {
        return Constants.DAILY_LOSS_THRESHOLD[riskLevel];
    }

    /**
     * 获取当前档位累计亏损阈值
     */
    public double getCumulativeLossThreshold() {
        return Constants.CUMULATIVE_LOSS_THRESHOLD[riskLevel];
    }

    // Getters and Setters
    public int getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(int riskLevel) {
        if (riskLevel >= 0 && riskLevel < Constants.RISK_EXPOSURE.length) {
            this.riskLevel = riskLevel;
        }
    }

    public double getTotalCapital() {
        return totalCapital;
    }

    public void setTotalCapital(double totalCapital) {
        this.totalCapital = totalCapital;
    }

    public String getSymbol() {
        return symbol;
    }
}
