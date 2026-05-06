package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 趋势强度策略（供 StrategyBackTestMain 直接引用）
 * 综合 DIF散度率 + EMA排列 + 多级别共振
 * 这是系统的标准参考策略，用于单策略回测对比
 */
public class TrendStrengthStrategy implements TradeStrategy {

    private final boolean isBtc;

    public TrendStrengthStrategy() { this.isBtc = true; }
    public TrendStrengthStrategy(boolean isBtc) { this.isBtc = isBtc; }

    @Override
    public String name() { return "趋势强度策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 30) return 0;

        double dif = Indicators.difDivergence(k15m);
        double absDif = Math.abs(dif);

        // 趋势强度不足，观望
        if (absDif < 0.3) return 0;

        double e6  = Indicators.ema(k15m, 6);
        double e13 = Indicators.ema(k15m, 13);
        double e30 = Indicators.ema(k15m, 30);

        // 多头：EMA多头排列 + DIF正值
        if (dif > 0 && e6 > e13 && e13 > e30) {
            // 多级别共振加分
            if (k1h != null && k1h.length >= 30) {
                TrendAnalyzer.TrendDir t1h = TrendAnalyzer.trendOf(k1h);
                if (t1h == TrendAnalyzer.TrendDir.UP) return 1;
            }
            // 仅15M趋势，强度需更高
            if (absDif >= 0.6) return 1;
        }

        // 空头：EMA空头排列 + DIF负值
        if (dif < 0 && e6 < e13 && e13 < e30) {
            if (k1h != null && k1h.length >= 30) {
                TrendAnalyzer.TrendDir t1h = TrendAnalyzer.trendOf(k1h);
                if (t1h == TrendAnalyzer.TrendDir.DOWN) return -1;
            }
            if (absDif >= 0.6) return -1;
        }

        return 0;
    }
}
