package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「三重EMA趋势策略」
 * EMA5、EMA15、EMA40 三线排列，多头排列做多、空头排列做空
 */
public class TripleEmaStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 40) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) {
            close[i] = k15m[i].getClose();
        }
        int last = close.length - 1;

        double ema5 = ema(close, last, 5);
        double ema15 = ema(close, last, 15);
        double ema40 = ema(close, last, 40);

        // 多头排列
        if (ema5 > ema15 && ema15 > ema40) return 1;
        // 空头排列
        if (ema5 < ema15 && ema15 < ema40) return -1;
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

    @Override
    public String name() {
        return "三重EMA趋势策略";
    }
}