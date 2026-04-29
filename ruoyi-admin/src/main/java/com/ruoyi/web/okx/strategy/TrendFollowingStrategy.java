package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「趋势跟随策略」
 * 价格持续站上EMA20顺势做多，持续跌破EMA20顺势做空
 */
public class TrendFollowingStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;
        double ema20 = ema(close, last, 20);

        if (close[last] > ema20 && close[last-1] > ema20) return 1;
        if (close[last] < ema20 && close[last-1] < ema20) return -1;
        return 0;
    }

    private double ema(double[] close, int idx, int p) {
        double a = 2.0 / (p + 1);
        double res = close[0];
        for (int i = 1; i <= idx; i++) res = a * close[i] + (1 - a) * res;
        return res;
    }

    @Override
    public String name() {
        return "趋势跟随策略";
    }
}