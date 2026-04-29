package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「RSI14 超买超卖策略」，14 周期 RSI，低于 30 超卖做多，高于 70 超买做空
 */
public class RsiStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 14) return 0;

        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) {
            close[i] = k15m[i].getClose();
        }

        double rsi = rsi(close, 14);
        if (rsi < 30) return 1;
        if (rsi > 70) return -1;
        return 0;
    }

    private double rsi(double[] close, int period) {
        double gain = 0, loss = 0;
        for (int i = 1; i < period; i++) {
            double delta = close[i] - close[i - 1];
            if (delta > 0) gain += delta;
            else loss -= delta;
        }

        if (loss == 0) return 100;
        double rs = gain / loss;
        return 100 - (100 / (1 + rs));
    }

    @Override
    public String name() {
        return "RSI14超买超卖策略";
    }
}