package com.ruoyi.web.okx.strategynew.backtest;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 回测结果导出器
 */
public class BacktestExporter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出交易记录到CSV
     */
    public static void exportTradesToCSV(BacktestResult result, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("交易序号,品种,策略,方向,入场时间,出场时间,入场价格,出场价格,数量,盈亏,持仓K线数,出场原因");

            int index = 1;
            for (TradeResult trade : result.getTrades()) {
                writer.println(String.format("%d,%s,%s,%s,%s,%s,%.8f,%.8f,%.8f,%.2f,%d,%s",
                    index++,
                    trade.getSymbol(),
                    trade.getStrategyName(),
                    trade.getDirection(),
                    trade.getEntryTime().format(FORMATTER),
                    trade.getExitTime().format(FORMATTER),
                    trade.getEntryPrice().doubleValue(),
                    trade.getExitPrice().doubleValue(),
                    trade.getQuantity().doubleValue(),
                    trade.getPnl().doubleValue(),
                    trade.getHoldingBars(),
                    trade.getExitReason()
                ));
            }

            System.out.println("交易记录已导出到: " + filePath);
        } catch (IOException e) {
            System.err.println("导出交易记录失败: " + e.getMessage());
        }
    }

    /**
     * 导出资金曲线到CSV
     */
    public static void exportEquityCurve(BacktestResult result, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("时间,资金,累计盈亏,回撤");

            BigDecimal runningCapital = result.getInitialCapital();
            BigDecimal peak = result.getInitialCapital();
            BigDecimal runningPnL = BigDecimal.ZERO;

            for (TradeResult trade : result.getTrades()) {
                runningPnL = runningPnL.add(trade.getPnl());
                runningCapital = result.getInitialCapital().add(runningPnL);

                if (runningCapital.compareTo(peak) > 0) {
                    peak = runningCapital;
                }

                BigDecimal drawdown = peak.subtract(runningCapital);
                BigDecimal drawdownPercent = drawdown.divide(peak, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

                writer.println(String.format("%s,%.2f,%.2f,%.2f",
                    trade.getExitTime().format(FORMATTER),
                    runningCapital.doubleValue(),
                    runningPnL.doubleValue(),
                    drawdownPercent.doubleValue()
                ));
            }

            System.out.println("资金曲线已导出到: " + filePath);
        } catch (IOException e) {
            System.err.println("导出资金曲线失败: " + e.getMessage());
        }
    }

    /**
     * 导出完整报告到文本文件
     */
    public static void exportFullReport(BacktestResult result, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("========================================");
            writer.println("加密货币量化交易策略回测报告");
            writer.println("========================================");
            writer.println("生成时间: " + LocalDateTime.now().format(FORMATTER));
            writer.println();

            writer.println("基础信息");
            writer.println("----------------------------------------");
            writer.println("品种: " + result.getSymbol());
            writer.println("策略: " + result.getStrategyName());
            writer.println("回测周期: " + getBacktestPeriod(result));
            writer.println();

            writer.println("交易统计");
            writer.println("----------------------------------------");
            writer.println("成交笔数: " + result.getTotalTrades());
            writer.println("盈利笔数: " + result.getWinningTrades());
            writer.println("亏损笔数: " + result.getLosingTrades());
            writer.println();

            writer.println("收益统计");
            writer.println("----------------------------------------");
            writer.println(String.format("初始资金: $%.2f", result.getInitialCapital().doubleValue()));
            writer.println(String.format("最终资金: $%.2f", result.getFinalCapital().doubleValue()));
            writer.println(String.format("净利润: $%.2f", result.getNetProfit().doubleValue()));
            writer.println(String.format("总收益率: %.2f%%", result.getTotalReturnPercent()));
            writer.println();

            writer.println("风险指标");
            writer.println("----------------------------------------");
            writer.println(String.format("胜率: %.2f%%", result.getWinRate()));
            writer.println(String.format("盈亏比: %.2f", result.getProfitLossRatio()));
            writer.println(String.format("最大回撤: $%.2f", result.getMaxDrawdown().doubleValue()));
            writer.println(String.format("最大回撤率: %.2f%%", result.getMaxDrawdownPercent().doubleValue()));
            writer.println();

            writer.println("========================================");

            System.out.println("完整报告已导出到: " + filePath);
        } catch (IOException e) {
            System.err.println("导出完整报告失败: " + e.getMessage());
        }
    }

    /**
     * 获取回测周期
     */
    private static String getBacktestPeriod(BacktestResult result) {
        List<TradeResult> trades = result.getTrades();
        if (trades.isEmpty()) {
            return "无交易";
        }

        LocalDateTime start = trades.get(0).getEntryTime();
        LocalDateTime end = trades.get(trades.size() - 1).getExitTime();

        return start.format(FORMATTER) + " 至 " + end.format(FORMATTER);
    }

    /**
     * 导出策略统计到CSV
     */
    public static void exportStrategyStats(BacktestResult result, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("策略名称,交易笔数,盈利笔数,亏损笔数,胜率(%),总盈亏,平均盈利,平均亏损,盈亏比");

            java.util.Map<String, java.util.List<TradeResult>> strategyTrades = new java.util.HashMap<>();
            for (TradeResult trade : result.getTrades()) {
                strategyTrades.computeIfAbsent(trade.getStrategyName(), k -> new java.util.ArrayList<>()).add(trade);
            }

            for (java.util.Map.Entry<String, java.util.List<TradeResult>> entry : strategyTrades.entrySet()) {
                String strategy = entry.getKey();
                List<TradeResult> trades = entry.getValue();

                int totalTrades = trades.size();
                int wins = 0;
                int losses = 0;
                BigDecimal totalPnL = BigDecimal.ZERO;
                BigDecimal totalProfit = BigDecimal.ZERO;
                BigDecimal totalLoss = BigDecimal.ZERO;

                for (TradeResult trade : trades) {
                    totalPnL = totalPnL.add(trade.getPnl());
                    if (trade.getPnl().compareTo(BigDecimal.ZERO) > 0) {
                        wins++;
                        totalProfit = totalProfit.add(trade.getPnl());
                    } else if (trade.getPnl().compareTo(BigDecimal.ZERO) < 0) {
                        losses++;
                        totalLoss = totalLoss.add(trade.getPnl().abs());
                    }
                }

                double winRate = (double) wins / totalTrades * 100.0;
                double avgProfit = wins > 0 ? totalProfit.divide(BigDecimal.valueOf(wins), 2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0;
                double avgLoss = losses > 0 ? totalLoss.divide(BigDecimal.valueOf(losses), 2, BigDecimal.ROUND_HALF_UP).doubleValue() : 0.0;
                double profitLossRatio = avgLoss > 0 ? avgProfit / avgLoss : 0.0;

                writer.println(String.format("%s,%d,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f",
                    strategy, totalTrades, wins, losses, winRate,
                    totalPnL.doubleValue(), avgProfit, avgLoss, profitLossRatio));
            }

            System.out.println("策略统计已导出到: " + filePath);
        } catch (IOException e) {
            System.err.println("导出策略统计失败: " + e.getMessage());
        }
    }
}
