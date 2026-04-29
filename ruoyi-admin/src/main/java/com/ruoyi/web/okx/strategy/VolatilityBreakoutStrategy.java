package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「波动率突破策略」
 * 基于10周期价格波动幅度，放量突破上沿做空、跌破下沿做多
 */
public class VolatilityBreakoutStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 10) return 0;
        int len = k15m.length;
        int period = 10;
        double sumVol = 0;
        for (int i = len - period; i < len; i++) {
            sumVol += Math.abs(k15m[i].getClose() - k15m[i-1].getClose());
        }
        double avgVol = sumVol / period;
        double now = k15m[len-1].getClose();
        double prev = k15m[len-2].getClose();

        if (now > prev + avgVol) return -1;
        if (now < prev - avgVol) return 1;
        return 0;
    }

    @Override
    public String name() {
        return "波动率突破策略";
    }
}