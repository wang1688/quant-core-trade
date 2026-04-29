package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「RSI超卖反弹策略」
 * RSI低于25严重超卖做多；RSI高于75严重超买做空
 */
public class RsiOversoldReboundStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 14) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) {
            close[i] = k15m[i].getClose();
        }
        double rsi = calcRsi(close, 14);
        if (rsi < 25) return 1;
        if (rsi > 75) return -1;
        return 0;
    }

    private double calcRsi(double[] c, int n) {
        double gain = 0, loss = 0;
        for (int i = 1; i < n; i++) {
            double d = c[i] - c[i-1];
            if (d > 0) gain += d;
            else loss -= d;
        }
        if (loss == 0) return 100;
        return 100 - 100 / (1 + gain / loss);
    }

    @Override
    public String name() {
        return "RSI超卖反弹策略";
    }
}