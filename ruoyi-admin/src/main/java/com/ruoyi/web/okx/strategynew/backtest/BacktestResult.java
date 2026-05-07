package com.ruoyi.web.okx.strategynew.backtest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 回测结果
 */
public class BacktestResult {
    private String symbol;
    private String strategyName;
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private BigDecimal totalProfit;
    private BigDecimal totalLoss;
    private BigDecimal netProfit;
    private BigDecimal initialCapital;
    private BigDecimal finalCapital;
    private BigDecimal maxDrawdown;
    private BigDecimal maxDrawdownPercent;
    private List<TradeResult> trades;

    public BacktestResult(String symbol, String strategyName, BigDecimal initialCapital) {
        this.symbol = symbol;
        this.strategyName = strategyName;
        this.initialCapital = initialCapital;
        this.finalCapital = initialCapital;
        this.totalTrades = 0;
        this.winningTrades = 0;
        this.losingTrades = 0;
        this.totalProfit = BigDecimal.ZERO;
        this.totalLoss = BigDecimal.ZERO;
        this.netProfit = BigDecimal.ZERO;
        this.maxDrawdown = BigDecimal.ZERO;
        this.maxDrawdownPercent = BigDecimal.ZERO;
        this.trades = new ArrayList<>();
    }

    /**
     * 添加交易记录
     */
    public void addTrade(TradeResult trade) {
        trades.add(trade);
        totalTrades++;

        BigDecimal pnl = trade.getPnl();
        if (pnl.compareTo(BigDecimal.ZERO) > 0) {
            winningTrades++;
            totalProfit = totalProfit.add(pnl);
        } else if (pnl.compareTo(BigDecimal.ZERO) < 0) {
            losingTrades++;
            totalLoss = totalLoss.add(pnl.abs());
        }

        netProfit = netProfit.add(pnl);
        finalCapital = initialCapital.add(netProfit);

        updateMaxDrawdown();
    }

    /**
     * 更新最大回撤
     */
    private void updateMaxDrawdown() {
        BigDecimal peak = initialCapital;
        BigDecimal currentDrawdown = BigDecimal.ZERO;

        BigDecimal runningCapital = initialCapital;
        for (TradeResult trade : trades) {
            runningCapital = runningCapital.add(trade.getPnl());

            if (runningCapital.compareTo(peak) > 0) {
                peak = runningCapital;
            }

            BigDecimal drawdown = peak.subtract(runningCapital);
            if (drawdown.compareTo(currentDrawdown) > 0) {
                currentDrawdown = drawdown;
            }
        }

        maxDrawdown = currentDrawdown;
        if (peak.compareTo(BigDecimal.ZERO) > 0) {
            maxDrawdownPercent = currentDrawdown.divide(peak, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * 计算胜率
     */
    public double getWinRate() {
        if (totalTrades == 0) {
            return 0.0;
        }
        return (double) winningTrades / totalTrades * 100.0;
    }

    /**
     * 计算总收益率
     */
    public double getTotalReturnPercent() {
        if (initialCapital.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return netProfit.divide(initialCapital, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    /**
     * 计算盈亏比
     */
    public double getProfitLossRatio() {
        if (losingTrades == 0 || totalLoss.compareTo(BigDecimal.ZERO) == 0) {
            return winningTrades > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }

        BigDecimal avgProfit = totalProfit.divide(BigDecimal.valueOf(winningTrades), 8, RoundingMode.HALF_UP);
        BigDecimal avgLoss = totalLoss.divide(BigDecimal.valueOf(losingTrades), 8, RoundingMode.HALF_UP);

        return avgProfit.divide(avgLoss, 4, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 打印回测报告
     */
    public void printReport() {
        System.out.println("========================================");
        System.out.println("回测报告");
        System.out.println("========================================");
        System.out.println("品种: " + symbol);
        System.out.println("策略: " + strategyName);
        System.out.println("----------------------------------------");
        System.out.println("成交笔数: " + totalTrades);
        System.out.println("盈利笔数: " + winningTrades);
        System.out.println("亏损笔数: " + losingTrades);
        System.out.println("----------------------------------------");
        System.out.println(String.format("初始资金: $%.2f", initialCapital.doubleValue()));
        System.out.println(String.format("最终资金: $%.2f", finalCapital.doubleValue()));
        System.out.println(String.format("净利润: $%.2f", netProfit.doubleValue()));
        System.out.println(String.format("总收益率: %.2f%%", getTotalReturnPercent()));
        System.out.println("----------------------------------------");
        System.out.println(String.format("胜率: %.2f%%", getWinRate()));
        System.out.println(String.format("盈亏比: %.2f", getProfitLossRatio()));
        System.out.println("----------------------------------------");
        System.out.println(String.format("最大回撤: $%.2f", maxDrawdown.doubleValue()));
        System.out.println(String.format("最大回撤率: %.2f%%", maxDrawdownPercent.doubleValue()));
        System.out.println("========================================");
    }

    // Getters
    public String getSymbol() {
        return symbol;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public int getWinningTrades() {
        return winningTrades;
    }

    public int getLosingTrades() {
        return losingTrades;
    }

    public BigDecimal getTotalProfit() {
        return totalProfit;
    }

    public BigDecimal getTotalLoss() {
        return totalLoss;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public BigDecimal getInitialCapital() {
        return initialCapital;
    }

    public BigDecimal getFinalCapital() {
        return finalCapital;
    }

    public BigDecimal getMaxDrawdown() {
        return maxDrawdown;
    }

    public BigDecimal getMaxDrawdownPercent() {
        return maxDrawdownPercent;
    }

    public List<TradeResult> getTrades() {
        return trades;
    }
}
