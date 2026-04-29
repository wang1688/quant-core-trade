package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「布林带突破策略」，20 周期均线 + 2 倍标准差，跌破下轨做多，涨破上轨做空
 */
public class BollingerStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;

        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) {
            close[i] = k15m[i].getClose();
        }

        int last = close.length - 1;
        double ma = sma(close, last, 20);
        double std = std(close, last, 20);
        double upper = ma + 2 * std;
        double lower = ma - 2 * std;

        if (close[last] <= lower) return 1;
        if (close[last] >= upper) return -1;
        return 0;
    }

    private double sma(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    private double std(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double ma = sma(arr, idx, p);
        double sum = 0;
        int n = idx - start + 1;
        for (int i = start; i <= idx; i++) {
            sum += Math.pow(arr[i] - ma, 2);
        }
        return Math.sqrt(sum / n);
    }

    @Override
    public String name() {
        return "布林带突破策略";
    }
}