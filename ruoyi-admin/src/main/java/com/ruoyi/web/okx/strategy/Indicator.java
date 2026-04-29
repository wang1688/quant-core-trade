package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

public class Indicator {

    // EMA
    public static double ema(double[] arr, int idx, int n) {
        double a = 2.0 / (n + 1);
        double res = arr[idx];
        for (int i = idx - 1; i >= 0 && i >= idx - 200; i--) {
            res = a * arr[i] + (1 - a) * res;
        }
        return res;
    }

    // ATR15
    public static double atr(KLineVO[] k, int n) {
        double sum = 0;
        for (int i = 1; i < k.length && i <= n; i++) {
            KLineVO c = k[i];
            KLineVO p = k[i - 1];
            double tr = Math.max(c.getHigh() - c.getLow(),
                    Math.max(Math.abs(c.getHigh() - p.getClose()),
                            Math.abs(c.getLow() - p.getClose())));
            sum += tr;
        }
        return sum / Math.min(n, k.length - 1);
    }

    // 顶分型
    public static boolean top(KLineVO[] k, int i) {
        if (i < 2 || i >= k.length - 2) return false;
        double m = k[i].getHigh();
        return m > k[i - 1].getHigh() && m > k[i - 2].getHigh()
                && m > k[i + 1].getHigh() && m > k[i + 2].getHigh();
    }

    // 底分型
    public static boolean bottom(KLineVO[] k, int i) {
        if (i < 2 || i >= k.length - 2) return false;
        double m = k[i].getLow();
        return m < k[i - 1].getLow() && m < k[i - 2].getLow()
                && m < k[i + 1].getLow() && m < k[i + 2].getLow();
    }
}