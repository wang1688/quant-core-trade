package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 中枢核心策略（主策略）
 * 条件：三级别趋势共振 + 信号评分≥7 + EMA30有效趋势 + 箱体突破确认
 */
public class CoreTrendStrategy implements TradeStrategy {

    private final boolean isBtc;

    public CoreTrendStrategy() { this.isBtc = true; }
    public CoreTrendStrategy(boolean isBtc) { this.isBtc = isBtc; }

    @Override
    public String name() { return "中枢核心策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 60) return 0;

        TrendAnalyzer.MultiTrend mt = TrendAnalyzer.analyze(k15m, k1h, k4h);
        SignalScorer.Score sc = SignalScorer.score(k15m, k1h, k4h, isBtc);

        // 必要条件1：至少4H+1H共振
        if (!mt.resonance4h1h() && !mt.resonanceUp && !mt.resonanceDown) return 0;

        // 必要条件2：评分≥7
        if (!sc.canOpen()) return 0;

        // 必要条件3：EMA30有效趋势（非缓冲期）
        if (!TrendAnalyzer.ema30Trending(k15m)) return 0;

        // 必要条件4：15M趋势强度中等以上
        if (TrendAnalyzer.trendStrength(k15m) < 1) return 0;

        return sc.direction;
    }
}
