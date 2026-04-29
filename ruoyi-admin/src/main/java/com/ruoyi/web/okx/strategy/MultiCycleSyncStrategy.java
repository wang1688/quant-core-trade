package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 多周期同步共振策略
 * 15分、1小时、4小时均线趋势同向共振开单
 */
public class MultiCycleSyncStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k1h == null || k4h == null) return 0;
        if (k15m.length < 20 || k1h.length < 20 || k4h.length < 20) return 0;

        boolean m15Up = isTrendUp(k15m);
        boolean h1Up = isTrendUp(k1h);
        boolean h4Up = isTrendUp(k4h);

        boolean m15Dn = isTrendDown(k15m);
        boolean h1Dn = isTrendDown(k1h);
        boolean h4Dn = isTrendDown(k4h);

        if (m15Up && h1Up && h4Up) return 1;
        if (m15Dn && h1Dn && h4Dn) return -1;
        return 0;
    }

    private boolean isTrendUp(KLineVO[] k) {
        double[] close = new double[k.length];
        for (int i = 0; i < k.length; i++) close[i] = k[i].getClose();
        int last = close.length - 1;
        double ma20 = sma(close, last, 20);
        return close[last] > ma20;
    }

    private boolean isTrendDown(KLineVO[] k) {
        double[] close = new double[k.length];
        for (int i = 0; i < k.length; i++) close[i] = k[i].getClose();
        int last = close.length - 1;
        double ma20 = sma(close, last, 20);
        return close[last] < ma20;
    }

    private double sma(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    @Override
    public String name() {
        return "多周期同步共振策略";
    }
}