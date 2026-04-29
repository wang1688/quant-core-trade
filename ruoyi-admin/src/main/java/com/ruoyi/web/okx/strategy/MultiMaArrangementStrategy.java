package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「多均线排列策略」
 * MA5、MA10、MA20、MA60多头排列顺势做多，空头排列顺势做空
 */
public class MultiMaArrangementStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 60) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma5 = sma(close, last, 5);
        double ma10 = sma(close, last, 10);
        double ma20 = sma(close, last, 20);
        double ma60 = sma(close, last, 60);

        if (ma5 > ma10 && ma10 > ma20 && ma20 > ma60) return 1;
        if (ma5 < ma10 && ma10 < ma20 && ma20 < ma60) return -1;
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
        return "多均线排列策略";
    }
}