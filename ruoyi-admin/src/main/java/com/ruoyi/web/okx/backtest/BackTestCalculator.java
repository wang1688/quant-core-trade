package com.ruoyi.web.okx.backtest;






//import org.apache.commons.math3.dfp.DfpField;



import org.apache.commons.math3.dfp.DfpField;

import java.math.BigDecimal;

import java.util.List;

public class BackTestCalculator {
    private static final int SCALE = 8;


    // 直接对应你原来的 HALF_UP 四舍五入
    private static final int ROUND_MODE = BigDecimal.ROUND_HALF_UP;

    /**
     * 【新增】支持自定义无风险利率的计算方法
     */
    public static BackTestResult calculate(List<BackTradeRecord> tradeList, List<BigDecimal> equityCurve, double riskFreeRate) {
        BackTestResult result = new BackTestResult();
        int total = tradeList.size();
        result.setTotalTrades(total);

        if (total == 0) {
            result.setWinRate(BigDecimal.ZERO);
            result.setProfitLossRatio(BigDecimal.ZERO);
            result.setMaxDrawdown(BigDecimal.ZERO);
            result.setSharpeRatio(BigDecimal.ZERO);
            return result;
        }

        // 1. 胜率
        long winCount = tradeList.stream().filter(BackTradeRecord::isWin).count();
        BigDecimal winRate = BigDecimal.valueOf(winCount)
                .divide(BigDecimal.valueOf(total), SCALE, ROUND_MODE)
                .multiply(BigDecimal.valueOf(100));
        result.setWinRate(winRate);

        // 2. 盈亏统计
        BigDecimal sumWin = BigDecimal.ZERO;
        BigDecimal sumLoss = BigDecimal.ZERO;
        int lossCount = 0;

        for (BackTradeRecord record : tradeList) {
            BigDecimal profit = record.getProfitRate();
            if (record.isWin()) {
                sumWin = sumWin.add(profit);
            } else {
                sumLoss = sumLoss.add(profit.abs());
                lossCount++;
            }
        }

        // 3. 盈亏比
        BigDecimal avgWin = winCount > 0
                ? sumWin.divide(BigDecimal.valueOf(winCount), SCALE, ROUND_MODE)
                : BigDecimal.ZERO;
        BigDecimal avgLoss = lossCount > 0
                ? sumLoss.divide(BigDecimal.valueOf(lossCount), SCALE, ROUND_MODE)
                : BigDecimal.ZERO;
        BigDecimal profitLossRatio = avgLoss.compareTo(BigDecimal.ZERO) > 0
                ? avgWin.divide(avgLoss, 2, ROUND_MODE)
                : BigDecimal.ZERO;
        result.setProfitLossRatio(profitLossRatio);

        // 4. 基于连续资金曲线计算最大回撤
        BigDecimal maxDrawdown = calculateMaxDrawdown(equityCurve);
        result.setMaxDrawdown(maxDrawdown);

        // 5. 【修改】支持自定义无风险利率的夏普比率计算
        BigDecimal totalProfit = equityCurve.get(equityCurve.size() - 1);
        BigDecimal mean = totalProfit.divide(BigDecimal.valueOf(equityCurve.size()), SCALE, ROUND_MODE);
        BigDecimal variance = BigDecimal.ZERO;
        for (BigDecimal equity : equityCurve) {
            BigDecimal diff = equity.subtract(mean);
            variance = variance.add(diff.multiply(diff));
        }
        BigDecimal std = sqrt(variance.divide(BigDecimal.valueOf(equityCurve.size()), SCALE, ROUND_MODE));
        BigDecimal sharpeRatio = BigDecimal.ZERO;

        if (std.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal annualFactor = sqrt(BigDecimal.valueOf(252));
            BigDecimal annualReturn = mean.multiply(annualFactor);
            BigDecimal annualStd = std.multiply(annualFactor);
            BigDecimal rf = BigDecimal.valueOf(riskFreeRate);
            sharpeRatio = annualReturn.subtract(rf).divide(annualStd, 2, ROUND_MODE);
        }
        result.setSharpeRatio(sharpeRatio);

        return result;
    }

    /**
     * 基于连续资金曲线计算最大回撤的核心方法
     */
    private static BigDecimal calculateMaxDrawdown(List<BigDecimal> equityCurve) {
        if (equityCurve == null || equityCurve.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal peak = equityCurve.get(0);
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (BigDecimal equity : equityCurve) {
            if (equity.compareTo(peak) > 0) {
                peak = equity;
            }

            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = peak.subtract(equity)
                        .divide(peak, SCALE, ROUND_MODE)
                        .multiply(BigDecimal.valueOf(100));

                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                }
            }
        }

        return maxDrawdown;
    }

    // ==================== 保留原有方法，向后兼容 ====================
    public static BackTestResult calculate(List<BackTradeRecord> tradeList, List<BigDecimal> equityCurve) {
        return calculate(tradeList, equityCurve, 0.02);
    }

    public static BackTestResult calculate(List<BackTradeRecord> tradeList) {
        List<BigDecimal> equityCurve = new java.util.ArrayList<>();
        BigDecimal equity = BigDecimal.ZERO;
        equityCurve.add(equity);
        for (BackTradeRecord record : tradeList) {
            equity = equity.add(record.getProfitRate());
            equityCurve.add(equity);
        }
        return calculate(tradeList, equityCurve, 0.02);
    }

    private static BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(Math.sqrt(value.doubleValue())).setScale(SCALE, ROUND_MODE);
    }
}