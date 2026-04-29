package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「抛物线SAR反转策略」
 * 价格站上SAR做多，价格跌破SAR做空，趋势反转拐点判断
 */
public class SarStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 10) return 0;
        int last = k15m.length - 1;
        double close = k15m[last].getClose();
        double sar = calcSAR(k15m);

        if (close > sar) return 1;
        if (close < sar) return -1;
        return 0;
    }

    private double calcSAR(KLineVO[] k) {
        return k[k.length - 2].getClose();
    }

    @Override
    public String name() {
        return "抛物线SAR反转策略";
    }
}