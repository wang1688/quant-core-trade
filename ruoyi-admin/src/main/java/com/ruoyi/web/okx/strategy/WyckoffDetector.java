package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 威科夫吸筹/派发检测器
 * 吸筹（Accumulation）：价格横盘低位 + 成交量逐渐萎缩后放大 + 弹簧测试（Spring）
 * 派发（Distribution）：价格横盘高位 + 成交量逐渐萎缩后放大 + 上冲测试（Upthrust）
 */
public class WyckoffDetector {

    public enum WyckoffPhase {
        ACCUMULATION,   // 吸筹阶段（看多）
        DISTRIBUTION,   // 派发阶段（看空）
        MARKUP,         // 上涨阶段
        MARKDOWN,       // 下跌阶段
        NEUTRAL         // 中性/不明确
    }

    public static class WyckoffResult {
        public final WyckoffPhase phase;
        public final boolean springDetected;    // 弹簧（假跌破支撑后快速回升）
        public final boolean upthrustDetected;  // 上冲（假突破阻力后快速回落）
        public final int signal;                // 1=做多，-1=做空，0=观望

        public WyckoffResult(WyckoffPhase phase, boolean spring, boolean upthrust, int signal) {
            this.phase = phase;
            this.springDetected = spring;
            this.upthrustDetected = upthrust;
            this.signal = signal;
        }
    }

    /**
     * 检测威科夫形态
     * @param klines   K线数组（建议60根以上）
     * @param isBtc    true=BTC，false=ETH
     */
    public static WyckoffResult detect(KLineVO[] klines, boolean isBtc) {
        if (klines == null || klines.length < 40) {
            return new WyckoffResult(WyckoffPhase.NEUTRAL, false, false, 0);
        }

        int len = klines.length;
        BoxDetector.Box box = BoxDetector.detectAuto(klines, isBtc);

        double close = klines[len - 1].getClose();
        double atr = Indicators.atrPct(klines, 14);
        double vr = Indicators.volumeRatio(klines, 20);

        // 判断价格区间位置（近40根K线的高低点）
        double high40 = Indicators.highest(klines, 40);
        double low40  = Indicators.lowest(klines, 40);
        double range  = high40 - low40;
        if (range <= 0) return new WyckoffResult(WyckoffPhase.NEUTRAL, false, false, 0);

        double positionPct = (close - low40) / range; // 0=最低，1=最高

        // 弹簧检测：价格短暂跌破近期低点后快速回升（下影线 + 收盘回到低点上方）
        boolean spring = false;
        if (len >= 3) {
            KLineVO c0 = klines[len - 1];
            KLineVO c1 = klines[len - 2];
            double recentLow = Indicators.lowest(klines, 20);
            // 当前K线低点跌破近期低点，但收盘回升
            if (c0.getLow() < recentLow && c0.getClose() > recentLow
                    && c0.getClose() > c1.getClose()) {
                spring = true;
            }
        }

        // 上冲检测：价格短暂突破近期高点后快速回落
        boolean upthrust = false;
        if (len >= 3) {
            KLineVO c0 = klines[len - 1];
            KLineVO c1 = klines[len - 2];
            double recentHigh = Indicators.highest(klines, 20);
            if (c0.getHigh() > recentHigh && c0.getClose() < recentHigh
                    && c0.getClose() < c1.getClose()) {
                upthrust = true;
            }
        }

        // 判断阶段
        WyckoffPhase phase;
        int signal = 0;

        // 低位横盘（价格在低位40%区间内）+ 成交量特征
        if (positionPct <= 0.4) {
            // 吸筹：低位横盘 + 成交量逐渐萎缩
            double volTrend = volumeTrend(klines, 10);
            if (volTrend < 0) {
                phase = WyckoffPhase.ACCUMULATION;
                if (spring) signal = 1; // 弹簧确认，做多
            } else if (spring) {
                phase = WyckoffPhase.ACCUMULATION;
                signal = 1;
            } else {
                phase = WyckoffPhase.NEUTRAL;
            }
        }
        // 高位横盘（价格在高位60%区间以上）
        else if (positionPct >= 0.6) {
            double volTrend = volumeTrend(klines, 10);
            if (volTrend < 0) {
                phase = WyckoffPhase.DISTRIBUTION;
                if (upthrust) signal = -1; // 上冲确认，做空
            } else if (upthrust) {
                phase = WyckoffPhase.DISTRIBUTION;
                signal = -1;
            } else {
                phase = WyckoffPhase.NEUTRAL;
            }
        }
        // 趋势阶段
        else {
            TrendAnalyzer.TrendDir trend = TrendAnalyzer.trendOf(klines);
            if (trend == TrendAnalyzer.TrendDir.UP) phase = WyckoffPhase.MARKUP;
            else if (trend == TrendAnalyzer.TrendDir.DOWN) phase = WyckoffPhase.MARKDOWN;
            else phase = WyckoffPhase.NEUTRAL;
        }

        return new WyckoffResult(phase, spring, upthrust, signal);
    }

    /**
     * 计算近N根K线成交量趋势（正=放量，负=缩量）
     * 用线性回归斜率近似
     */
    private static double volumeTrend(KLineVO[] klines, int n) {
        int len = klines.length;
        int start = Math.max(0, len - n);
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        int cnt = 0;
        for (int i = start; i < len; i++) {
            double x = cnt;
            double y = klines[i].getVolume();
            sumX += x; sumY += y; sumXY += x * y; sumX2 += x * x;
            cnt++;
        }
        if (cnt < 2) return 0;
        double denom = cnt * sumX2 - sumX * sumX;
        if (denom == 0) return 0;
        return (cnt * sumXY - sumX * sumY) / denom;
    }
}
