package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 信号评分系统（0~12分，5个维度）
 *
 * 维度1 趋势强度（0~3）：基于DIF散度率 + EMA排列
 * 维度2 成交量共振（0~2）：量比 + 放量确认
 * 维度3 结构契合（0~3）：箱体位置 + 突破有效性
 * 维度4 波动率匹配（0~2）：ATR与策略类型匹配
 * 维度5 EMA对齐（0~2）：三均线排列整齐度
 */
public class SignalScorer {

    public static class Score {
        public final int total;          // 总分 0~12
        public final int trendScore;     // 趋势强度
        public final int volumeScore;    // 成交量共振
        public final int structureScore; // 结构契合
        public final int volatilityScore;// 波动率匹配
        public final int emaScore;       // EMA对齐
        public final int direction;      // 1=多，-1=空，0=无方向

        public Score(int trend, int volume, int structure, int volatility, int ema, int direction) {
            this.trendScore = trend;
            this.volumeScore = volume;
            this.structureScore = structure;
            this.volatilityScore = volatility;
            this.emaScore = ema;
            this.total = trend + volume + structure + volatility + ema;
            this.direction = direction;
        }

        /** 是否达到开仓门槛（≥7分） */
        public boolean canOpen() { return total >= 7 && direction != 0; }

        /** 是否达到高质量信号（≥9分） */
        public boolean highQuality() { return total >= 9 && direction != 0; }
    }

    /**
     * 综合评分
     * @param k15m  15分钟K线
     * @param k1h   1小时K线
     * @param k4h   4小时K线
     * @param isBtc true=BTC，false=ETH
     */
    public static Score score(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h, boolean isBtc) {
        TrendAnalyzer.MultiTrend mt = TrendAnalyzer.analyze(k15m, k1h, k4h);

        // 确定方向
        int direction = 0;
        if (mt.bullScore() >= 2) direction = 1;
        else if (mt.bearScore() >= 2) direction = -1;

        // 维度1：趋势强度（0~3）
        int trendScore = scoreTrend(k15m, k1h, k4h, mt);

        // 维度2：成交量共振（0~2）
        int volumeScore = scoreVolume(k15m);

        // 维度3：结构契合（0~3）
        int structureScore = scoreStructure(k15m, isBtc, direction);

        // 维度4：波动率匹配（0~2）
        int volatilityScore = scoreVolatility(k15m, isBtc);

        // 维度5：EMA对齐（0~2）
        int emaScore = scoreEma(k15m, direction);

        return new Score(trendScore, volumeScore, structureScore, volatilityScore, emaScore, direction);
    }

    // ==================== 各维度评分 ====================

    private static int scoreTrend(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h,
                                   TrendAnalyzer.MultiTrend mt) {
        int s = 0;
        // 三级别共振 +3
        if (mt.resonanceUp || mt.resonanceDown) return 3;
        // 4H+1H共振 +2
        if (mt.resonance4h1h()) s = 2;
        // 1H+15M共振 +1（若4H未共振）
        else if (mt.resonance1h15m()) s = 1;
        // 15M趋势强度加成
        int strength = TrendAnalyzer.trendStrength(k15m);
        if (strength == 2 && s < 3) s = Math.min(3, s + 1);
        return s;
    }

    private static int scoreVolume(KLineVO[] k15m) {
        double vr = Indicators.volumeRatio(k15m, 20);
        if (vr >= 2.0) return 2;   // 明显放量
        if (vr >= 1.3) return 1;   // 温和放量
        return 0;
    }

    private static int scoreStructure(KLineVO[] k15m, boolean isBtc, int direction) {
        BoxDetector.Box box = BoxDetector.detectAuto(k15m, isBtc);
        if (!box.valid) return 1; // 无有效箱体，给基础分

        double close = k15m[k15m.length - 1].getClose();
        int pos = BoxDetector.pricePosition(box, close);
        int breakout = BoxDetector.breakout(k15m, box);

        int s = 0;
        // 突破方向与信号方向一致 +2
        if (breakout == direction && direction != 0) s += 2;
        // 价格在箱体外且方向一致 +1
        else if (pos == direction && direction != 0) s += 1;
        // 价格在箱体内（震荡区间）+1
        else if (pos == 0) s += 1;

        // 箱体高度越大，结构越可靠，额外+1
        double minH = isBtc ? BoxDetector.BTC_BOX_MIN_USDT : BoxDetector.ETH_BOX_MIN_USDT;
        if (box.height() >= minH * 2) s = Math.min(3, s + 1);

        return Math.min(3, s);
    }

    private static int scoreVolatility(KLineVO[] k15m, boolean isBtc) {
        double atr = Indicators.atrPct(k15m, 14);
        // BTC: 低≤0.8%，中0.8~1.6%，高≥1.6%
        // ETH: 低≤1.0%，中1.0~2.0%，高≥2.0%
        double low = isBtc ? 0.8 : 1.0;
        double high = isBtc ? 1.6 : 2.0;

        if (atr >= low && atr < high) return 2; // 中等波动，最适合趋势策略
        if (atr < low) return 1;                // 低波动，信号较弱
        return 1;                               // 高波动，风险较大
    }

    private static int scoreEma(KLineVO[] k15m, int direction) {
        if (k15m == null || k15m.length < 30) return 0;
        double e6  = Indicators.ema(k15m, 6);
        double e13 = Indicators.ema(k15m, 13);
        double e30 = Indicators.ema(k15m, 30);

        if (direction == 1) {
            // 多头：EMA6>EMA13>EMA30 完美排列 +2，部分排列 +1
            if (e6 > e13 && e13 > e30) return 2;
            if (e6 > e13 || e13 > e30) return 1;
        } else if (direction == -1) {
            // 空头：EMA6<EMA13<EMA30
            if (e6 < e13 && e13 < e30) return 2;
            if (e6 < e13 || e13 < e30) return 1;
        }
        return 0;
    }
}
