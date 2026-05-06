package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 多时间框架回测引擎（修复版）
 *
 * 与 BacktestEngine 的区别：
 *   - 使用 KLineAligner 按时间戳对齐 1H/4H 索引，避免数组越界和时间错位
 *   - 以 15M K线为主循环基准
 *   - 传给策略的切片：15M 取近100根，1H 取近60根，4H 取近30根
 */
public class MultiTimeframeBacktestEngine {

    @Data
    @AllArgsConstructor
    public static class Report {
        private String strategy;
        private int totalTrades;
        private int winCount;
        private double profitRate;   // 累计收益率（非百分比，如0.35表示35%）
        private double maxDrawdown;  // 最大回撤（非百分比）
        private double winRate;
    }

    public Report test(TradeStrategy strategy, KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        // 预计算对齐索引
        int[] align1h = (k1h != null && k1h.length > 0) ? KLineAligner.buildAlignTable(k15m, k1h) : null;
        int[] align4h = (k4h != null && k4h.length > 0) ? KLineAligner.buildAlignTable(k15m, k4h) : null;

        List<Boolean> tradeResults = new ArrayList<>();
        int holdDirection = 0;
        double entryPrice = 0;
        double money = 1.0;
        double peakMoney = 1.0;
        double maxDrawdown = 0;

        int n = k15m.length;
        for (int i = 60; i < n; i++) {
            // 构建各时间框架切片
            KLineVO[] slice15m = slice(k15m, i, 100);
            KLineVO[] slice1h  = align1h != null ? KLineAligner.slice(k1h, align1h[i], 60) : null;
            KLineVO[] slice4h  = align4h != null ? KLineAligner.slice(k4h, align4h[i], 30) : null;

            int sig = strategy.signal(slice15m, slice1h, slice4h);
            double close = k15m[i].getClose();

            // 平仓：信号反转
            if (holdDirection != 0 && sig != 0 && sig != holdDirection) {
                double pnl = holdDirection == 1
                        ? (close - entryPrice) / entryPrice
                        : (entryPrice - close) / entryPrice;
                money *= (1 + pnl);
                peakMoney = Math.max(peakMoney, money);
                double dd = (peakMoney - money) / peakMoney;
                maxDrawdown = Math.max(maxDrawdown, dd);
                tradeResults.add(pnl > 0);
                holdDirection = 0;
            }

            // 平仓：信号归零
            if (holdDirection != 0 && sig == 0) {
                double pnl = holdDirection == 1
                        ? (close - entryPrice) / entryPrice
                        : (entryPrice - close) / entryPrice;
                money *= (1 + pnl);
                peakMoney = Math.max(peakMoney, money);
                double dd = (peakMoney - money) / peakMoney;
                maxDrawdown = Math.max(maxDrawdown, dd);
                tradeResults.add(pnl > 0);
                holdDirection = 0;
            }

            // 开仓
            if (holdDirection == 0 && sig != 0) {
                holdDirection = sig;
                entryPrice = close;
            }
        }

        // 强制平仓最后一笔未平仓位
        if (holdDirection != 0) {
            double close = k15m[n - 1].getClose();
            double pnl = holdDirection == 1
                    ? (close - entryPrice) / entryPrice
                    : (entryPrice - close) / entryPrice;
            money *= (1 + pnl);
            tradeResults.add(pnl > 0);
        }

        int total = tradeResults.size();
        int wins  = (int) tradeResults.stream().filter(b -> b).count();
        double profitRate = money - 1.0;
        double winRate = total == 0 ? 0 : (double) wins / total;

        return new Report(strategy.name(), total, wins, profitRate, maxDrawdown, winRate);
    }

    private KLineVO[] slice(KLineVO[] arr, int index, int need) {
        int start = Math.max(0, index - need + 1);
        int len = index - start + 1;
        KLineVO[] res = new KLineVO[len];
        System.arraycopy(arr, start, res, 0, len);
        return res;
    }
}
