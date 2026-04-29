package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「EMA+MACD组合共振策略」
 * EMA6上穿EMA30 且 MACD金叉 共振做多；
 * EMA6下穿EMA30 且 MACD死叉 共振做空
 */
public class EmaMacdComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 50) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) {
            close[i] = k15m[i].getClose();
        }
        int last = close.length - 1;

        // EMA 金叉死叉
        double ema6 = ema(close, last, 6);
        double ema6Prev = ema(close, last - 1, 6);
        double ema30 = ema(close, last, 30);
        double ema30Prev = ema(close, last - 1, 30);
        boolean emaGold = ema6 > ema30 && ema6Prev <= ema30Prev;
        boolean emaDead = ema6 < ema30 && ema6Prev >= ema30Prev;

        // MACD 金叉死叉
        double[] dif = calcMacdDif(close);
        double[] dea = calcEma(dif, 9);
        boolean macdGold = dif[last-1] < dea[last-1] && dif[last] > dea[last];
        boolean macdDead = dif[last-1] > dea[last-1] && dif[last] < dea[last];

        if (emaGold && macdGold) return 1;
        if (emaDead && macdDead) return -1;
        return 0;
    }

    private double ema(double[] close, int index, int period) {
        double alpha = 2.0 / (period + 1);
        double res = close[0];
        for (int i = 1; i <= index; i++) {
            res = alpha * close[i] + (1 - alpha) * res;
        }
        return res;
    }

    private double[] calcEma(double[] src, int p) {
        double[] res = new double[src.length];
        res[0] = src[0];
        double a = 2.0 / (p + 1);
        for (int i = 1; i < src.length; i++) {
            res[i] = a * src[i] + (1 - a) * res[i-1];
        }
        return res;
    }

    private double[] calcMacdDif(double[] c) {
        double[] e12 = calcEma(c, 12);
        double[] e26 = calcEma(c, 26);
        double[] dif = new double[c.length];
        for (int i = 0; i < c.length; i++) {
            dif[i] = e12[i] - e26[i];
        }
        return dif;
    }

    @Override
    public String name() {
        return "EMA+MACD组合共振策略";
    }
}