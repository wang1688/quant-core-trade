package com.ruoyi.web.okx.strategynew.backtest;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 回测性能指标计算器
 */
public class PerformanceMetrics {

    /**
     * 计算夏普比率（Sharpe Ratio）
     * 假设无风险利率为0
     */
    public static double calculateSharpeRatio(BacktestResult result) {
        if (result.getTotalTrades() < 2) {
            return 0.0;
        }

        double[] returns = new double[result.getTrades().size()];
        for (int i = 0; i < result.getTrades().size(); i++) {
            TradeResult trade = result.getTrades().get(i);
            double returnRate = trade.getPnl().divide(result.getInitialCapital(), 8, RoundingMode.HALF_UP).doubleValue();
            returns[i] = returnRate;
        }

        double avgReturn = calculateMean(returns);
        double stdDev = calculateStdDev(returns, avgReturn);

        if (stdDev == 0) {
            return 0.0;
        }

        return avgReturn / stdDev * Math.sqrt(252);
    }

    /**
     * 计算索提诺比率（Sortino Ratio）
     * 只考虑下行波动
     */
    public static double calculateSortinoRatio(BacktestResult result) {
        if (result.getTotalTrades() < 2) {
            return 0.0;
        }

        double[] returns = new double[result.getTrades().size()];
        for (int i = 0; i < result.getTrades().size(); i++) {
            TradeResult trade = result.getTrades().get(i);
            double returnRate = trade.getPnl().divide(result.getInitialCapital(), 8, RoundingMode.HALF_UP).doubleValue();
            returns[i] = returnRate;
        }

        double avgReturn = calculateMean(returns);
        double downDev = calculateDownsideDeviation(returns, 0.0);

        if (downDev == 0) {
            return 0.0;
        }

        return avgReturn / downDev * Math.sqrt(252);
    }

    /**
     * 计算卡玛比率（Calmar Ratio）
     * 年化收益率 / 最大回撤率
     */
    public static double calculateCalmarRatio(BacktestResult result) {
        if (result.getMaxDrawdownPercent().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        double annualizedReturn = result.getTotalReturnPercent() / 2.0;
        double maxDrawdownPercent = result.getMaxDrawdownPercent().doubleValue();

        return annualizedReturn / maxDrawdownPercent;
    }

    /**
     * 计算盈利因子（Profit Factor）
     * 总盈利 / 总亏损
     */
    public static double calculateProfitFactor(BacktestResult result) {
        if (result.getTotalLoss().compareTo(BigDecimal.ZERO) == 0) {
            return result.getTotalProfit().compareTo(BigDecimal.ZERO) > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }

        return result.getTotalProfit().divide(result.getTotalLoss(), 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 计算恢复因子（Recovery Factor）
     * 净利润 / 最大回撤
     */
    public static double calculateRecoveryFactor(BacktestResult result) {
        if (result.getMaxDrawdown().compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        return result.getNetProfit().divide(result.getMaxDrawdown(), 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 计算期望收益（Expectancy）
     * (胜率 × 平均盈利) - (败率 × 平均亏损)
     */
    public static double calculateExpectancy(BacktestResult result) {
        if (result.getTotalTrades() == 0) {
            return 0.0;
        }

        double winRate = result.getWinRate() / 100.0;
        double lossRate = 1.0 - winRate;

        double avgWin = result.getWinningTrades() > 0 ?
            result.getTotalProfit().divide(BigDecimal.valueOf(result.getWinningTrades()), 4, RoundingMode.HALF_UP).doubleValue() : 0.0;

        double avgLoss = result.getLosingTrades() > 0 ?
            result.getTotalLoss().divide(BigDecimal.valueOf(result.getLosingTrades()), 4, RoundingMode.HALF_UP).doubleValue() : 0.0;

        return (winRate * avgWin) - (lossRate * avgLoss);
    }

    /**
     * 计算平均收益
     */
    private static double calculateMean(double[] values) {
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    /**
     * 计算标准差
     */
    private static double calculateStdDev(double[] values, double mean) {
        double sumSquaredDiff = 0.0;
        for (double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / values.length);
    }

    /**
     * 计算下行标准差
     */
    private static double calculateDownsideDeviation(double[] values, double threshold) {
        double sumSquaredDiff = 0.0;
        int count = 0;
        for (double value : values) {
            if (value < threshold) {
                double diff = value - threshold;
                sumSquaredDiff += diff * diff;
                count++;
            }
        }
        return count > 0 ? Math.sqrt(sumSquaredDiff / count) : 0.0;
    }

    /**
     * 打印所有性能指标
     */
    public static void printAllMetrics(BacktestResult result) {
        System.out.println("\n========================================");
        System.out.println("高级性能指标");
        System.out.println("========================================");

        System.out.println(String.format("夏普比率: %.2f", calculateSharpeRatio(result)));
        System.out.println(String.format("索提诺比率: %.2f", calculateSortinoRatio(result)));
        System.out.println(String.format("卡玛比率: %.2f", calculateCalmarRatio(result)));
        System.out.println(String.format("盈利因子: %.2f", calculateProfitFactor(result)));
        System.out.println(String.format("恢复因子: %.2f", calculateRecoveryFactor(result)));
        System.out.println(String.format("期望收益: $%.2f", calculateExpectancy(result)));

        System.out.println("========================================");
    }
}
