package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单时间框架回测引擎（修复版）
 * 修复：
 *   1. 使用 KLineAligner 对齐 1H/4H 索引，避免时间错位
 *   2. 补充 signal=0 时平仓逻辑
 *   3. 强制平仓最后一笔未平仓位
 */
public class BacktestEngine {

    @Data
    @AllArgsConstructor
    public static class Report {
        private String strategy;
        private int totalTrades;
        private int winCount;
        private double profitRate;
        private double maxDrawdown;
        private double winRate;
    }

    public Report test(TradeStrategy strategy, KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        // 预计算对齐索引
        int[] align1h = (k1h != null && k1h.length > 0) ? KLineAligner.buildAlignTable(k15m, k1h) : null;
        int[] align4h = (k4h != null && k4h.length > 0) ? KLineAligner.buildAlignTable(k15m, k4h) : null;

        List<Boolean> results = new ArrayList<>();
        int holdDir = 0;
        double entryPrice = 0;
        double money = 1.0;
        double peakMoney = 1.0;
        double maxDrawdown = 0;

        int n = k15m.length;
        for (int i = 60; i < n; i++) {
            KLineVO[] s15m = slice(k15m, i, 100);
            KLineVO[] s1h  = align1h != null ? KLineAligner.slice(k1h, align1h[i], 60) : null;
            KLineVO[] s4h  = align4h != null ? KLineAligner.slice(k4h, align4h[i], 30) : null;

            int sig = strategy.signal(s15m, s1h, s4h);
            double close = k15m[i].getClose();

            // 平仓：信号反转或归零
            if (holdDir != 0 && (sig == 0 || sig != holdDir)) {
                double pnl = holdDir == 1
                        ? (close - entryPrice) / entryPrice
                        : (entryPrice - close) / entryPrice;
                money *= (1 + pnl);
                peakMoney = Math.max(peakMoney, money);
                maxDrawdown = Math.max(maxDrawdown, (peakMoney - money) / peakMoney);
                results.add(pnl > 0);
                holdDir = 0;
            }

            // 开仓
            if (holdDir == 0 && sig != 0) {
                holdDir = sig;
                entryPrice = close;
            }
        }

        // 强制平仓最后一笔
        if (holdDir != 0) {
            double close = k15m[n - 1].getClose();
            double pnl = holdDir == 1
                    ? (close - entryPrice) / entryPrice
                    : (entryPrice - close) / entryPrice;
            money *= (1 + pnl);
            results.add(pnl > 0);
        }

        int total = results.size();
        int wins  = (int) results.stream().filter(b -> b).count();
        return new Report(
                strategy.name(),
                total, wins,
                money - 1.0,
                maxDrawdown,
                total == 0 ? 0 : (double) wins / total
        );
    }

    private KLineVO[] slice(KLineVO[] arr, int index, int need) {
        int start = Math.max(0, index - need + 1);
        int len = index - start + 1;
        KLineVO[] res = new KLineVO[len];
        System.arraycopy(arr, start, res, 0, len);
        return res;
    }
}
