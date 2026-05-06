package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 辅助策略3：趋势加速策略
 * 条件：已有明确趋势 + 成交量突然放大 + DIF散度率快速扩大 + 价格突破近期高/低点
 */
public class TrendAccelerationStrategy implements TradeStrategy {

    private final boolean isBtc;

    public TrendAccelerationStrategy() { this.isBtc = true; }
    public TrendAccelerationStrategy(boolean isBtc) { this.isBtc = isBtc; }

    @Override
    public String name() { return "趋势加速策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 60) return 0;

        TrendAnalyzer.MultiTrend mt = TrendAnalyzer.analyze(k15m, k1h, k4h);

        // 条件1：至少1H+15M趋势共振
        if (!mt.resonance1h15m()) return 0;

        // 条件2：成交量突然放大（量比≥2.0，加速信号）
        double vr = Indicators.volumeRatio(k15m, 20);
        if (vr < 2.0) return 0;

        // 条件3：DIF散度率快速扩大（当前 > 3根前 × 1.3）
        double difNow = Indicators.difDivergence(k15m);
        KLineVO[] prev = new KLineVO[k15m.length - 3];
        System.arraycopy(k15m, 0, prev, 0, prev.length);
        double difPrev = Indicators.difDivergence(prev);
        if (Math.abs(difNow) < Math.abs(difPrev) * 1.3) return 0;

        // 条件4：价格突破近20根K线高/低点
        double close = k15m[k15m.length - 1].getClose();
        double high20 = Indicators.highest(k15m, 21); // 包含当前根
        double low20  = Indicators.lowest(k15m, 21);

        // 排除当前K线自身的高低点（取前20根）
        KLineVO[] prev20 = new KLineVO[Math.min(20, k15m.length - 1)];
        System.arraycopy(k15m, k15m.length - 1 - prev20.length, prev20, 0, prev20.length);
        double prevHigh = Indicators.highest(prev20, prev20.length);
        double prevLow  = Indicators.lowest(prev20, prev20.length);

        if (difNow > 0 && close > prevHigh) return 1;   // 多头加速突破
        if (difNow < 0 && close < prevLow)  return -1;  // 空头加速突破

        return 0;
    }
}
