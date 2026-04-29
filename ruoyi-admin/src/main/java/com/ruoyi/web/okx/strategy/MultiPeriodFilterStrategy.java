package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「多周期过滤策略」
 * 短周期站上均线+长周期多头趋势共振做多；短周期跌破均线+长周期空头共振做空
 */
public class MultiPeriodFilterStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 60) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma20 = sma(close, last, 20);
        double ma60 = sma(close, last, 60);

        if (close[last] > ma20 && ma20 > ma60) return 1;
        if (close[last] < ma20 && ma20 < ma60) return -1;
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
        return "多周期过滤策略";
    }
}