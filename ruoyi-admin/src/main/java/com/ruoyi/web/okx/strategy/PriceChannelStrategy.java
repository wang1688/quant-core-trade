package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「价格通道策略」
 * 20周期高低点形成通道，突破上沿做多，跌破下沿做空
 */
public class PriceChannelStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        int len = k15m.length;
        int period = 20;
        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;

        for (int i = len - period; i < len; i++) {
            high = Math.max(high, k15m[i].getClose());
            low = Math.min(low, k15m[i].getClose());
        }
        double now = k15m[len - 1].getClose();
        if (now > high) return 1;
        if (now < low) return -1;
        return 0;
    }

    @Override
    public String name() {
        return "价格通道策略";
    }
}