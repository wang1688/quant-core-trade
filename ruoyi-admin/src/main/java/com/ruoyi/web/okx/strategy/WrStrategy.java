package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「威廉WR超买超卖策略」
 * WR大于80超卖做多，WR小于20超买做空，14周期标准参数
 */
public class WrStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 14) return 0;
        double wr = calcWR(k15m, 14);
        if (wr > 80) return 1;
        if (wr < 20) return -1;
        return 0;
    }

    private double calcWR(KLineVO[] k, int period) {
        int len = k.length;
        int start = len - period;
        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        for (int i = start; i < len; i++) {
            high = Math.max(high, k[i].getClose());
            low = Math.min(low, k[i].getClose());
        }
        double close = k[len - 1].getClose();
        return (high - close) / (high - low) * 100;
    }

    @Override
    public String name() {
        return "威廉WR超买超卖策略";
    }
}