package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「DMI趋向指标策略」
 * +DI上穿-DI做多，+DI下穿-DI做空，判断趋势强弱转折
 */
public class DmiStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 14) return 0;
        double plusDI = calcPlusDI(k15m, 14);
        double minusDI = calcMinusDI(k15m, 14);

        if (plusDI > minusDI) return 1;
        if (plusDI < minusDI) return -1;
        return 0;
    }

    private double calcPlusDI(KLineVO[] k, int p) {
        int len = k.length;
        double up = 0;
        for (int i = len - p; i < len; i++) {
            up += Math.max(k[i].getClose() - k[i-1].getClose(), 0);
        }
        return up / p * 100;
    }

    private double calcMinusDI(KLineVO[] k, int p) {
        int len = k.length;
        double down = 0;
        for (int i = len - p; i < len; i++) {
            down += Math.max(k[i-1].getClose() - k[i].getClose(), 0);
        }
        return down / p * 100;
    }

    @Override
    public String name() {
        return "DMI趋向指标策略";
    }
}