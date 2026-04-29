package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「布林带反转策略」
 * 触碰上轨不追涨反手做空，触碰下轨不杀跌反手做多，震荡行情专用
 */
public class BollingerBreakoutReverseStrategy implements TradeStrategy {
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

        if (close[last] >= up) return -1;
        if (close[last] <= dn) return 1;
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
        int n = idx - start + 1;
        for (int i = start; i <= idx; i++) sum += Math.pow(arr[i] - avg, 2);
        return Math.sqrt(sum / n);
    }

    @Override
    public String name() {
        return "布林带反转策略";
    }
}