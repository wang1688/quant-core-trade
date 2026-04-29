package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「MA+EMA双趋势策略」
 * 收盘价同时站上MA20与EMA20做多，同时跌破双指标做空
 */
public class MaEmaDualStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma20 = sma(close, last, 20);
        double ema20 = ema(close, last, 20);
        double now = close[last];

        if (now > ma20 && now > ema20) return 1;
        if (now < ma20 && now < ema20) return -1;
        return 0;
    }

    private double sma(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    private double ema(double[] close, int idx, int period) {
        double alpha = 2.0 / (period + 1);
        double res = close[0];
        for (int i = 1; i <= idx; i++) res = alpha * close[i] + (1 - alpha) * res;
        return res;
    }

    @Override
    public String name() {
        return "MA+EMA双趋势策略";
    }
}