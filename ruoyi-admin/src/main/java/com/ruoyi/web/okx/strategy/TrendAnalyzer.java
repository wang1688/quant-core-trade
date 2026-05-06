package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 多级别趋势分析器
 * 分析4H/1H/15M三级别趋势方向与共振
 */
public class TrendAnalyzer {

    public enum TrendDir { UP, DOWN, FLAT }

    public static class MultiTrend {
        public final TrendDir t4h;
        public final TrendDir t1h;
        public final TrendDir t15m;
        public final boolean resonanceUp;    // 三级别共振做多
        public final boolean resonanceDown;  // 三级别共振做空
        public final boolean diverge;        // 高低级别背离

        public MultiTrend(TrendDir t4h, TrendDir t1h, TrendDir t15m) {
            this.t4h = t4h;
            this.t1h = t1h;
            this.t15m = t15m;
            this.resonanceUp = (t4h == TrendDir.UP && t1h == TrendDir.UP && t15m == TrendDir.UP);
            this.resonanceDown = (t4h == TrendDir.DOWN && t1h == TrendDir.DOWN && t15m == TrendDir.DOWN);
            // 背离：4H与15M方向相反
            this.diverge = (t4h == TrendDir.UP && t15m == TrendDir.DOWN)
                    || (t4h == TrendDir.DOWN && t15m == TrendDir.UP);
        }

        /** 4H与1H共振 */
        public boolean resonance4h1h() {
            return t4h == t1h && t4h != TrendDir.FLAT;
        }

        /** 1H与15M共振 */
        public boolean resonance1h15m() {
            return t1h == t15m && t1h != TrendDir.FLAT;
        }

        /** 整体多头倾向得分（0~3） */
        public int bullScore() {
            int s = 0;
            if (t4h == TrendDir.UP) s++;
            if (t1h == TrendDir.UP) s++;
            if (t15m == TrendDir.UP) s++;
            return s;
        }

        /** 整体空头倾向得分（0~3） */
        public int bearScore() {
            int s = 0;
            if (t4h == TrendDir.DOWN) s++;
            if (t1h == TrendDir.DOWN) s++;
            if (t15m == TrendDir.DOWN) s++;
            return s;
        }
    }

    /**
     * 判断单级别趋势方向
     * 基于EMA6/13/30排列 + DIF散度率
     */
    public static TrendDir trendOf(KLineVO[] klines) {
        if (klines == null || klines.length < 30) return TrendDir.FLAT;

        double e6  = Indicators.ema(klines, 6);
        double e13 = Indicators.ema(klines, 13);
        double e30 = Indicators.ema(klines, 30);
        double dif = Indicators.difDivergence(klines);

        // 多头排列：EMA6 > EMA13 > EMA30 且 DIF > 0
        if (e6 > e13 && e13 > e30 && dif > 0) return TrendDir.UP;
        // 空头排列：EMA6 < EMA13 < EMA30 且 DIF < 0
        if (e6 < e13 && e13 < e30 && dif < 0) return TrendDir.DOWN;
        return TrendDir.FLAT;
    }

    /**
     * 分析三级别趋势
     * k1h/k4h 数组长度不足时降级处理
     */
    public static MultiTrend analyze(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        TrendDir t15m = trendOf(k15m);
        TrendDir t1h  = (k1h  != null && k1h.length  >= 30) ? trendOf(k1h)  : TrendDir.FLAT;
        TrendDir t4h  = (k4h  != null && k4h.length  >= 30) ? trendOf(k4h)  : TrendDir.FLAT;
        return new MultiTrend(t4h, t1h, t15m);
    }

    /**
     * 趋势强度分类（基于DIF散度率绝对值）
     * 弱：|DIF| < 0.3%，中：0.3~0.8%，强：> 0.8%
     */
    public static int trendStrength(KLineVO[] klines) {
        double dif = Math.abs(Indicators.difDivergence(klines));
        if (dif < 0.3) return 0;  // 弱/震荡
        if (dif < 0.8) return 1;  // 中等趋势
        return 2;                  // 强趋势
    }

    /**
     * 判断EMA30是否处于缓冲期（角度在12°~阈值之间）
     * 用斜率百分比近似：0.03%~0.08% 对应约12°~25°
     */
    public static boolean inBufferZone(KLineVO[] klines) {
        double slope = Math.abs(Indicators.emaSlope(klines, 30, 3));
        return slope >= 0.03 && slope < 0.08;
    }

    /**
     * EMA30是否有效趋势（角度 > 阈值）
     */
    public static boolean ema30Trending(KLineVO[] klines) {
        double slope = Math.abs(Indicators.emaSlope(klines, 30, 3));
        return slope >= 0.08;
    }
}
