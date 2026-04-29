package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「高低点反转策略」
 * 连续触碰高点不破反手做空，连续触碰低点不破反手做多
 */
public class HighLowReversalStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 15) return 0;
        int len = k15m.length;
        double preHigh = k15m[len-2].getClose();
        double preLow = k15m[len-2].getClose();
        double now = k15m[len-1].getClose();

        if (now >= preHigh) return -1;
        if (now <= preLow) return 1;
        return 0;
    }

    @Override
    public String name() {
        return "高低点反转策略";
    }
}