package com.ruoyi.web.okx.strategynew.backtest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 回测分析器
 */
public class BacktestAnalyzer {

    /**
     * 按策略分析
     */
    public static void analyzeByStrategy(BacktestResult result) {
        Map<String, Integer> strategyCount = new HashMap<>();
        Map<String, BigDecimal> strategyPnL = new HashMap<>();
        Map<String, Integer> strategyWins = new HashMap<>();

        for (TradeResult trade : result.getTrades()) {
            String strategy = trade.getStrategyName();
            strategyCount.put(strategy, strategyCount.getOrDefault(strategy, 0) + 1);
            strategyPnL.put(strategy, strategyPnL.getOrDefault(strategy, BigDecimal.ZERO).add(trade.getPnl()));

            if (trade.getPnl().compareTo(BigDecimal.ZERO) > 0) {
                strategyWins.put(strategy, strategyWins.getOrDefault(strategy, 0) + 1);
            }
        }

        for (String strategy : strategyCount.keySet()) {
            int count = strategyCount.get(strategy);
            int wins = strategyWins.getOrDefault(strategy, 0);
            double winRate = (double) wins / count * 100.0;

            System.out.println(String.format("  %s: %d笔, 盈亏: $%.2f, 胜率: %.2f%%",
                strategy, count, strategyPnL.get(strategy).doubleValue(), winRate));
        }
    }

    /**
     * 按方向分析
     */
    public static void analyzeByDirection(BacktestResult result) {
        int longCount = 0, shortCount = 0;
        int longWins = 0, shortWins = 0;
        BigDecimal longPnL = BigDecimal.ZERO, shortPnL = BigDecimal.ZERO;

        for (TradeResult trade : result.getTrades()) {
            if (trade.getDirection().name().equals("LONG")) {
                longCount++;
                longPnL = longPnL.add(trade.getPnl());
                if (trade.getPnl().compareTo(BigDecimal.ZERO) > 0) {
                    longWins++;
                }
            } else {
                shortCount++;
                shortPnL = shortPnL.add(trade.getPnl());
                if (trade.getPnl().compareTo(BigDecimal.ZERO) > 0) {
                    shortWins++;
                }
            }
        }

        double longWinRate = longCount > 0 ? (double) longWins / longCount * 100.0 : 0.0;
        double shortWinRate = shortCount > 0 ? (double) shortWins / shortCount * 100.0 : 0.0;

        System.out.println(String.format("  做多: %d笔, 盈亏: $%.2f, 胜率: %.2f%%",
            longCount, longPnL.doubleValue(), longWinRate));
        System.out.println(String.format("  做空: %d笔, 盈亏: $%.2f, 胜率: %.2f%%",
            shortCount, shortPnL.doubleValue(), shortWinRate));
    }

    /**
     * 持仓时间分析
     */
    public static void analyzeHoldingTime(BacktestResult result) {
        int totalBars = 0;
        int minBars = Integer.MAX_VALUE;
        int maxBars = 0;

        for (TradeResult trade : result.getTrades()) {
            int bars = trade.getHoldingBars();
            totalBars += bars;
            minBars = Math.min(minBars, bars);
            maxBars = Math.max(maxBars, bars);
        }

        double avgBars = (double) totalBars / result.getTotalTrades();
        System.out.println(String.format("  平均持仓: %.1f根15M K线 (%.1f小时)", avgBars, avgBars * 0.25));
        System.out.println(String.format("  最短持仓: %d根15M K线 (%.1f小时)", minBars, minBars * 0.25));
        System.out.println(String.format("  最长持仓: %d根15M K线 (%.1f小时)", maxBars, maxBars * 0.25));
    }

    /**
     * 最佳/最差交易分析
     */
    public static void analyzeBestWorstTrades(BacktestResult result) {
        TradeResult bestTrade = null;
        TradeResult worstTrade = null;

        for (TradeResult trade : result.getTrades()) {
            if (bestTrade == null || trade.getPnl().compareTo(bestTrade.getPnl()) > 0) {
                bestTrade = trade;
            }
            if (worstTrade == null || trade.getPnl().compareTo(worstTrade.getPnl()) < 0) {
                worstTrade = trade;
            }
        }

        if (bestTrade != null) {
            System.out.println(String.format("  最佳交易: %s %s, 盈利: $%.2f, 策略: %s",
                bestTrade.getDirection(), bestTrade.getSymbol(),
                bestTrade.getPnl().doubleValue(), bestTrade.getStrategyName()));
        }

        if (worstTrade != null) {
            System.out.println(String.format("  最差交易: %s %s, 亏损: $%.2f, 策略: %s",
                worstTrade.getDirection(), worstTrade.getSymbol(),
                worstTrade.getPnl().doubleValue(), worstTrade.getStrategyName()));
        }
    }

    /**
     * 月度收益分析
     */
    public static void analyzeMonthlyReturns(BacktestResult result) {
        Map<String, BigDecimal> monthlyPnL = new HashMap<>();

        for (TradeResult trade : result.getTrades()) {
            String month = trade.getExitTime().toString().substring(0, 7);
            monthlyPnL.put(month, monthlyPnL.getOrDefault(month, BigDecimal.ZERO).add(trade.getPnl()));
        }

        System.out.println("\n月度收益:");
        monthlyPnL.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> System.out.println(String.format("  %s: $%.2f",
                entry.getKey(), entry.getValue().doubleValue())));
    }

    /**
     * 连续盈亏分析
     */
    public static void analyzeConsecutiveWinsLosses(BacktestResult result) {
        int maxConsecutiveWins = 0;
        int maxConsecutiveLosses = 0;
        int currentWins = 0;
        int currentLosses = 0;

        for (TradeResult trade : result.getTrades()) {
            if (trade.getPnl().compareTo(BigDecimal.ZERO) > 0) {
                currentWins++;
                currentLosses = 0;
                maxConsecutiveWins = Math.max(maxConsecutiveWins, currentWins);
            } else if (trade.getPnl().compareTo(BigDecimal.ZERO) < 0) {
                currentLosses++;
                currentWins = 0;
                maxConsecutiveLosses = Math.max(maxConsecutiveLosses, currentLosses);
            }
        }

        System.out.println("\n连续盈亏:");
        System.out.println("  最大连续盈利: " + maxConsecutiveWins + " 笔");
        System.out.println("  最大连续亏损: " + maxConsecutiveLosses + " 笔");
    }
}
