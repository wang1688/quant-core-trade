package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 15M轻仓试单策略
 * 条件比主策略宽松，用于探测趋势方向，仓位为标准仓位的30%
 * 信号评分≥5即可开仓（主策略需≥7）
 */
public class TrialPositionStrategy implements TradeStrategy {

    private final boolean isBtc;

    public TrialPositionStrategy() { this.isBtc = true; }
    public TrialPositionStrategy(boolean isBtc) { this.isBtc = isBtc; }

    @Override
    public String name() { return "15M试单策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 30) return 0;

        SignalScorer.Score sc = SignalScorer.score(k15m, k1h, k4h, isBtc);

        // 试单门槛：评分≥5，方向明确
        if (sc.total < 5 || sc.direction == 0) return 0;

        // 额外过滤：EMA6/13方向与信号一致
        double e6  = Indicators.ema(k15m, 6);
        double e13 = Indicators.ema(k15m, 13);
        if (sc.direction == 1  && e6 < e13) return 0;
        if (sc.direction == -1 && e6 > e13) return 0;

        // 缓冲期内允许试单（主策略不允许）
        return sc.direction;
    }
}
