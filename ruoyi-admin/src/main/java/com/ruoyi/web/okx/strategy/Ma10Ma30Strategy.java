package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「MA10/MA30短期双均线策略」
 * 10周期与30周期均线金叉做多、死叉做空，适合短线震荡行情
 */
public class Ma10Ma30Strategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 30) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma10 = sma(close, last, 10);
        double ma10Prev = sma(close, last-1, 10);
        double ma30 = sma(close, last, 30);
        double ma30Prev = sma(close, last-1, 30);

        if (ma10 > ma30 && ma10Prev <= ma30Prev) return 1;
        if (ma10 < ma30 && ma10Prev >= ma30Prev) return -1;
        return 0;
    }

    private double sma(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    @Override
    public String name() {
        return "MA10/MA30短期双均线策略";
    }
}