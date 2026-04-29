package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class BacktestEngine {

    private static final double INIT_MONEY = 10000.0;
    private static final double RISK = 0.01;

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

    @Data
    @AllArgsConstructor
    static class Trade {
        int direction;
        boolean isWin;
        double entryPrice;
    }

    public Report test(TradeStrategy strategy, KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        List<Trade> trades = new ArrayList<>();
        Trade current = null;
        double money = INIT_MONEY;
        double maxMoney = INIT_MONEY;
        double maxDraw = 0;

        int n = k15m.length;
        for (int i = 60; i < n; i++) {
            KLineVO c = k15m[i];
            int sig = strategy.signal(
                    slice(k15m, i, 100),
                    slice(k1h, i, 60),
                    slice(k4h, i, 30)
            );

            // 平仓逻辑
            if (current != null && sig != 0 && sig != current.getDirection()) {
                double p = current.getDirection() == 1
                        ? (c.getClose() - current.getEntryPrice()) / current.getEntryPrice()
                        : (current.getEntryPrice() - c.getClose()) / current.getEntryPrice();

                money *= (1 + p);
                maxMoney = Math.max(maxMoney, money);
                double draw = (maxMoney - money) / maxMoney;
                maxDraw = Math.max(maxDraw, draw);

                trades.add(new Trade(current.getDirection(), p > 0, current.getEntryPrice()));
                current = null;
            }

            // 开仓逻辑
            if (current == null && sig != 0) {
                current = new Trade(sig, false, c.getClose());
            }
        }

        int total = trades.size();
        int win = (int) trades.stream().filter(Trade::isWin).count();
        double profit = (money - INIT_MONEY) / INIT_MONEY;
        double winRate = total == 0 ? 0 : (double) win / total;

        return new Report(
                strategy.name(),
                total, win, profit, maxDraw, winRate
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