package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「EMA波浪趋势策略」
 * EMA12、EMA26波浪起伏，顺势跟随波浪方向交易
 */
public class EmaWaveStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 30) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ema12 = ema(close, last, 12);
        double ema26 = ema(close, last, 26);

        if(ema12 > ema26) return 1;
        if(ema12 < ema26) return -1;
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
        return "EMA波浪趋势策略";
    }
}