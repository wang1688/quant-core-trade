package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「ATR波动率突破策略」
 * 价格突破前收盘价+ATR做多，跌破前收盘价-ATR做空，14周期ATR
 */
public class AtrBreakoutStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 14) return 0;
        double atr = calcATR(k15m, 14);
        int last = k15m.length - 1;
        double now = k15m[last].getClose();
        double prev = k15m[last - 1].getClose();

        if (now > prev + atr) return 1;
        if (now < prev - atr) return -1;
        return 0;
    }

    private double calcATR(KLineVO[] k, int period) {
        double sum = 0;
        int len = k.length;
        for (int i = len - period; i < len; i++) {
            sum += Math.abs(k[i].getClose() - k[i-1].getClose());
        }
        return sum / period;
    }

    @Override
    public String name() {
        return "ATR波动率突破策略";
    }
}