package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「量价配合策略」
 * 涨价放量做多、跌价放量做空，缩量震荡观望
 */
public class VolumePriceStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 5) return 0;
        int len = k15m.length;
        double curr = k15m[len-1].getClose();
        double prev = k15m[len-2].getClose();

        if(curr > prev) return 1;
        if(curr < prev) return -1;
        return 0;
    }

    @Override
    public String name() {
        return "量价配合策略";
    }
}