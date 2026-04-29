package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「布林带+RSI组合策略」
 * 价格触碰下轨+RSI超卖共振做多；价格触碰上轨+RSI超买共振做空
 */
public class BollingerRsiComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma = sma(close, last, 20);
        double std = stdDev(close, last, 20);
        double up = ma + 2 * std;
        double dn = ma - 2 * std;
        double rsi = calcRsi(close, 14);

        if (close[last] <= dn && rsi < 30) return 1;
        if (close[last] >= up && rsi > 70) return -1;
        return 0;
    }

    private double sma(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    private double stdDev(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double avg = sma(arr, idx, p);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += Math.pow(arr[i] - avg, 2);
        return Math.sqrt(sum / (idx - start + 1));
    }

    private double calcRsi(double[] c, int n) {
        double gain = 0, loss = 0;
        for (int i = 1; i < n; i++) {
            double d = c[i] - c[i-1];
            if (d > 0) gain += d;
            else loss -= d;
        }
        if (loss == 0) return 100;
        return 100 - 100 / (1 + gain / loss);
    }

    @Override
    public String name() {
        return "布林带+RSI组合策略";
    }
}