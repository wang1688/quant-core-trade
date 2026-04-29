package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「KDJ+MACD共振策略」
 * KDJ低位金叉+MACD金叉共振做多；KDJ高位死叉+MACD死叉共振做空
 */
public class KdjMacdComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 50) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double k = calcK(k15m, last);
        double d = calcD(k15m, last);
        boolean kdjGold = k > d && k < 20;
        boolean kdjDead = k < d && k > 80;

        double[] dif = calcEma(close,12);
        double[] dea = calcEma(calcSub(dif, calcEma(close,26)),9);
        boolean macdGold = dif[last-1] < dea[last-1] && dif[last] > dea[last];
        boolean macdDead = dif[last-1] > dea[last-1] && dif[last] < dea[last];

        if (kdjGold && macdGold) return 1;
        if (kdjDead && macdDead) return -1;
        return 0;
    }

    private double calcK(KLineVO[] k, int idx) {
        int start = idx - 8;
        if (start < 0) start = 0;
        double low = Double.MAX_VALUE, high = Double.MIN_VALUE;
        for (int i = start; i <= idx; i++) {
            low = Math.min(low, k[i].getClose());
            high = Math.max(high, k[i].getClose());
        }
        return (k[idx].getClose() - low) / (high - low) * 100;
    }

    private double calcD(KLineVO[] k, int idx) {
        double rsv = calcK(k, idx);
        return rsv / 3 + 50 * 2 / 3;
    }

    private double[] calcEma(double[] src, int p) {
        double[] res = new double[src.length];
        res[0] = src[0];
        double a = 2.0 / (p + 1);
        for (int i = 1; i < src.length; i++) res[i] = a * src[i] + (1 - a) * res[i-1];
        return res;
    }

    private double[] calcSub(double[] a, double[] b) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) res[i] = a[i] - b[i];
        return res;
    }

    @Override
    public String name() {
        return "KDJ+MACD共振策略";
    }
}