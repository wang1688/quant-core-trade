package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「双重高低顶底反转策略」
 * 双重底企稳做多、双重顶承压做空
 */
public class DoubleTroughPeakStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 10) return 0;
        int len = k15m.length;
        double low1 = k15m[len-3].getClose();
        double low2 = k15m[len-1].getClose();
        double high1 = k15m[len-3].getClose();
        double high2 = k15m[len-1].getClose();

        // 双重底
        if(Math.abs(low1 - low2) / low1 < 0.015) return 1;
        // 双重顶
        if(Math.abs(high1 - high2) / high1 < 0.015) return -1;
        return 0;
    }

    @Override
    public String name() {
        return "双重高低顶底反转策略";
    }
}