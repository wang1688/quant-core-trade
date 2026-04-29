package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

public class EmaTrendStrategy implements TradeStrategy {

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 50) return 0;

        // 真实收盘价
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) {
            close[i] = k15m[i].getClose();
        }

        // 真实EMA计算
        int last = k15m.length - 1;
        double ema6 = ema(close, last, 6);
        double ema6_prev = ema(close, last - 1, 6);
        double ema30 = ema(close, last, 30);
        double ema30_prev = ema(close, last - 1, 30);

        // 金叉 → 多
        if (ema6 > ema30 && ema6_prev <= ema30_prev) {
            return 1;
        }
        // 死叉 → 空
        if (ema6 < ema30 && ema6_prev >= ema30_prev) {
            return -1;
        }
        return 0;
    }

    // EMA算法（真实）
    private double ema(double[] close, int index, int period) {
        if (index < 0) return close[0];
        double alpha = 2.0 / (period + 1);
        double res = close[index];
        for (int i = index - 1; i >= 0; i--) {
            res = alpha * close[i] + (1 - alpha) * res;
        }
        return res;
    }

    @Override
    public String name() {
        return "EMA6/EMA30真实趋势策略";
    }
}