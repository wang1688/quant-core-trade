package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 裸K交易体系（独立辅助策略）
 * 不依赖均线，纯价格行为：吞没形态、锤子线、射击之星、孕线
 */
public class NakedKStrategy implements TradeStrategy {

    @Override
    public String name() { return "裸K交易体系"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 3) return 0;
        int len = k15m.length;

        KLineVO c0 = k15m[len - 1]; // 当前K线
        KLineVO c1 = k15m[len - 2]; // 前一根
        KLineVO c2 = k15m[len - 3]; // 前两根

        double body0 = Math.abs(c0.getClose() - c0.getOpen());
        double body1 = Math.abs(c1.getClose() - c1.getOpen());
        double range0 = c0.getHigh() - c0.getLow();
        double range1 = c1.getHigh() - c1.getLow();

        // 防止除零
        if (range0 == 0 || range1 == 0) return 0;

        // ===== 看多形态 =====

        // 1. 多头吞没：当前阳线实体完全吞没前一根阴线实体
        if (isBullCandle(c0) && isBearCandle(c1)
                && c0.getOpen() <= c1.getClose()
                && c0.getClose() >= c1.getOpen()
                && body0 > body1) {
            return 1;
        }

        // 2. 锤子线：下影线 ≥ 实体2倍，上影线短，出现在下跌后
        double lowerShadow0 = Math.min(c0.getOpen(), c0.getClose()) - c0.getLow();
        double upperShadow0 = c0.getHigh() - Math.max(c0.getOpen(), c0.getClose());
        if (lowerShadow0 >= body0 * 2 && upperShadow0 <= body0 * 0.5
                && c1.getClose() < c2.getClose()) { // 前两根下跌趋势
            return 1;
        }

        // 3. 多头孕线：前一根大阴线，当前K线实体完全在前一根实体内
        if (isBearCandle(c1) && body1 > 0
                && c0.getOpen() > c1.getClose() && c0.getClose() < c1.getOpen()
                && body0 < body1 * 0.5) {
            // 孕线本身是中性偏多信号，需结合趋势
            if (k1h != null && k1h.length >= 10
                    && TrendAnalyzer.trendOf(k1h) == TrendAnalyzer.TrendDir.UP) {
                return 1;
            }
        }

        // ===== 看空形态 =====

        // 4. 空头吞没：当前阴线实体完全吞没前一根阳线实体
        if (isBearCandle(c0) && isBullCandle(c1)
                && c0.getOpen() >= c1.getClose()
                && c0.getClose() <= c1.getOpen()
                && body0 > body1) {
            return -1;
        }

        // 5. 射击之星：上影线 ≥ 实体2倍，下影线短，出现在上涨后
        double upperShadow1 = c0.getHigh() - Math.max(c0.getOpen(), c0.getClose());
        double lowerShadow1 = Math.min(c0.getOpen(), c0.getClose()) - c0.getLow();
        if (upperShadow1 >= body0 * 2 && lowerShadow1 <= body0 * 0.5
                && c1.getClose() > c2.getClose()) { // 前两根上涨趋势
            return -1;
        }

        // 6. 空头孕线
        if (isBullCandle(c1) && body1 > 0
                && c0.getOpen() < c1.getClose() && c0.getClose() > c1.getOpen()
                && body0 < body1 * 0.5) {
            if (k1h != null && k1h.length >= 10
                    && TrendAnalyzer.trendOf(k1h) == TrendAnalyzer.TrendDir.DOWN) {
                return -1;
            }
        }

        return 0;
    }

    private boolean isBullCandle(KLineVO k) { return k.getClose() > k.getOpen(); }
    private boolean isBearCandle(KLineVO k) { return k.getClose() < k.getOpen(); }
}
