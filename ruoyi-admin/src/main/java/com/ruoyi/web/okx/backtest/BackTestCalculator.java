package com.ruoyi.web.okx.backtest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class BackTestCalculator {
    private static final int SCALE = 8;
    private static final RoundingMode ROUND_MODE = RoundingMode.HALF_UP;

    /**
     * 【修复】重载方法：传入资金曲线计算真实最大回撤
     */
    public static BackTestResult calculate(List<BackTradeRecord> tradeList, List<BigDecimal> equityCurve) {
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

        // 4. 【修复】基于连续资金曲线计算最大回撤
        BigDecimal maxDrawdown = calculateMaxDrawdown(equityCurve);
        result.setMaxDrawdown(maxDrawdown);

        // 5. 夏普比率
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
            BigDecimal riskFreeRate = new BigDecimal("0.02");
            sharpeRatio = annualReturn.subtract(riskFreeRate).divide(annualStd, 2, ROUND_MODE);
        }
        result.setSharpeRatio(sharpeRatio);

        return result;
    }

    /**
     * 【修复】基于连续资金曲线计算最大回撤的核心方法
     */
    private static BigDecimal calculateMaxDrawdown(List<BigDecimal> equityCurve) {
        if (equityCurve == null || equityCurve.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal peak = equityCurve.get(0);
        BigDecimal maxDrawdown = BigDecimal.ZERO;

        for (BigDecimal equity : equityCurve) {
            // 更新峰值
            if (equity.compareTo(peak) > 0) {
                peak = equity;
            }

            // 计算当前回撤
            if (peak.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal drawdown = peak.subtract(equity)
                        .divide(peak, SCALE, ROUND_MODE)
                        .multiply(BigDecimal.valueOf(100));

                // 更新最大回撤
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                }
            }
        }

        return maxDrawdown;
    }

    /**
     * 【保留】原有方法，向后兼容
     */
    public static BackTestResult calculate(List<BackTradeRecord> tradeList) {
        // 如果没有资金曲线，用交易记录模拟一个
        List<BigDecimal> equityCurve = new java.util.ArrayList<>();
        BigDecimal equity = BigDecimal.ZERO;
        equityCurve.add(equity);
        for (BackTradeRecord record : tradeList) {
            equity = equity.add(record.getProfitRate());
            equityCurve.add(equity);
        }
        return calculate(tradeList, equityCurve);
    }

    private static BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(Math.sqrt(value.doubleValue())).setScale(SCALE, ROUND_MODE);
    }
}