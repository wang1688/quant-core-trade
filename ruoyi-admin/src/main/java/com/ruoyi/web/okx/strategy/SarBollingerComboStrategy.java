package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「SAR+布林带反转策略」
 * 价格跌破布林下轨且站上SAR反转做多；突破布林上轨且跌破SAR反转做空
 */
public class SarBollingerComboStrategy implements TradeStrategy {
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
        double sar = close[last - 1];

        if (close[last] <= dn && close[last] > sar) return 1;
        if (close[last] >= up && close[last] < sar) return -1;
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

    @Override
    public String name() {
        return "SAR+布林带反转策略";
    }
}