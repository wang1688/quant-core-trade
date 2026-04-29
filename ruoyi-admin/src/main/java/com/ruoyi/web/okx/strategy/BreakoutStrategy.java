package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「高低点突破策略」
 * 突破20周期最高点做空反转，跌破20周期最低点做多反弹
 */
public class BreakoutStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        int len = k15m.length;
        int look = 20;
        int start = len - look;

        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        for(int i=start;i<len-1;i++){
            high = Math.max(high, k15m[i].getClose());
            low = Math.min(low, k15m[i].getClose());
        }
        double now = k15m[len-1].getClose();
        if(now > high) return -1;
        if(now < low) return 1;
        return 0;
    }

    @Override
    public String name() {
        return "20周期高低点突破策略";
    }
}