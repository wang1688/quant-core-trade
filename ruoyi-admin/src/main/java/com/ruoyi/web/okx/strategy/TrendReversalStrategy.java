package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 辅助策略2：趋势末端反转策略
 * 条件：强趋势末端 + DIF散度率极值 + 成交量萎缩 + EMA6/13背离
 */
public class TrendReversalStrategy implements TradeStrategy {

    private final boolean isBtc;

    public TrendReversalStrategy() { this.isBtc = true; }
    public TrendReversalStrategy(boolean isBtc) { this.isBtc = isBtc; }

    @Override
    public String name() { return "趋势末端反转策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 60) return 0;

        double dif = Indicators.difDivergence(k15m);
        double absDif = Math.abs(dif);

        // 条件1：DIF散度率处于极值区（强趋势末端）
        if (absDif < 1.2) return 0;

        // 条件2：成交量萎缩（量比 < 0.7，趋势衰竭信号）
        double vr = Indicators.volumeRatio(k15m, 20);
        if (vr >= 0.7) return 0;

        // 条件3：EMA6开始向EMA13靠拢（趋势减速）
        double[] ema6arr  = Indicators.emaArray(k15m, 6);
        double[] ema13arr = Indicators.emaArray(k15m, 13);
        int len = ema6arr.length;
        if (len < 5) return 0;

        double gap0 = Math.abs(ema6arr[len - 1] - ema13arr[len - 1]);
        double gap3 = Math.abs(ema6arr[len - 4] - ema13arr[len - 4]);
        // EMA6/13间距收窄（趋势减速）
        if (gap0 >= gap3) return 0;

        // 条件4：4H级别趋势与15M方向一致（确认是末端而非中途）
        if (k4h != null && k4h.length >= 30) {
            TrendAnalyzer.TrendDir t4h = TrendAnalyzer.trendOf(k4h);
            if (dif > 0 && t4h != TrendAnalyzer.TrendDir.UP) return 0;
            if (dif < 0 && t4h != TrendAnalyzer.TrendDir.DOWN) return 0;
        }

        // 反转信号：多头末端做空，空头末端做多
        return dif > 0 ? -1 : 1;
    }
}
