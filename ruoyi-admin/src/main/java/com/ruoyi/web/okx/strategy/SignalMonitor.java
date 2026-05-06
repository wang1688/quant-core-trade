package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 信号监控日志工具
 * 打印每根K线的完整信号分析，用于调试和实盘监控
 */
public class SignalMonitor {

    /**
     * 打印完整信号分析报告
     */
    public static void printReport(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h,
                                    boolean isBtc, TierManager tierManager) {
        if (k15m == null || k15m.length == 0) return;

        KLineVO latest = k15m[k15m.length - 1];
        double close = latest.getClose();

        System.out.println("\n========== 信号分析报告 ==========");
        System.out.printf("当前价格：%.2f  时间戳：%d%n", close, latest.getTimestamp());

        // 指标
        double e6  = Indicators.ema(k15m, 6);
        double e13 = Indicators.ema(k15m, 13);
        double e30 = Indicators.ema(k15m, 30);
        double dif = Indicators.difDivergence(k15m);
        double atr = Indicators.atrPct(k15m, 14);
        double vr  = Indicators.volumeRatio(k15m, 20);

        System.out.printf("EMA6=%.2f  EMA13=%.2f  EMA30=%.2f%n", e6, e13, e30);
        System.out.printf("DIF散度率=%.4f%%  ATR=%.3f%%  量比=%.2f%n", dif, atr, vr);

        // 趋势分析
        TrendAnalyzer.MultiTrend mt = TrendAnalyzer.analyze(k15m, k1h, k4h);
        System.out.printf("趋势：4H=%s  1H=%s  15M=%s  共振=%s%n",
                mt.t4h, mt.t1h, mt.t15m,
                mt.resonanceUp ? "多头共振" : mt.resonanceDown ? "空头共振" : "无共振");

        // 箱体
        BoxDetector.Box box = BoxDetector.detectAuto(k15m, isBtc);
        if (box.valid) {
            System.out.printf("箱体：高=%.2f  低=%.2f  高度=%.2f  位置=%s%n",
                    box.high, box.low, box.height(),
                    BoxDetector.pricePosition(box, close) == 1 ? "箱体上方"
                            : BoxDetector.pricePosition(box, close) == -1 ? "箱体下方" : "箱体内");
        } else {
            System.out.println("箱体：无有效箱体");
        }

        // 评分
        SignalScorer.Score sc = SignalScorer.score(k15m, k1h, k4h, isBtc);
        System.out.printf("评分：总分=%d/12  趋势=%d  量能=%d  结构=%d  波动=%d  EMA=%d  方向=%s%n",
                sc.total, sc.trendScore, sc.volumeScore, sc.structureScore,
                sc.volatilityScore, sc.emaScore,
                sc.direction == 1 ? "做多" : sc.direction == -1 ? "做空" : "无");

        // 威科夫
        WyckoffDetector.WyckoffResult wyckoff = WyckoffDetector.detect(k15m, isBtc);
        System.out.printf("威科夫：阶段=%s  弹簧=%s  上冲=%s%n",
                wyckoff.phase, wyckoff.springDetected, wyckoff.upthrustDetected);

        // 档位与仓位
        if (tierManager != null) {
            int tier = tierManager.getCurrentTier();
            System.out.printf("风险档位：%d档  熔断=%s  日内亏损=%.2f%%%n",
                    tier, tierManager.isCircuitBroken(), tierManager.getDailyLossPct());

            if (!tierManager.isCircuitBroken() && tier > 0) {
                RiskManager.PositionResult pos = RiskManager.calculateFromKline(tier, 10000, k15m, isBtc);
                System.out.printf("仓位建议：风险=%.1f%%  止损距离=%.2f  建议张数=%d%n",
                        pos.riskPct, pos.stopLoss, pos.contracts);
            }
        }

        // 最终信号
        MasterStrategy master = new MasterStrategy(isBtc);
        int sig = master.signal(k15m, k1h, k4h);
        System.out.printf("最终信号：%s%n",
                sig == 1 ? "▲ 做多" : sig == -1 ? "▼ 做空" : "— 观望");
        System.out.println("==================================\n");
    }

    /**
     * 简洁版单行信号输出（适合循环打印）
     */
    public static String oneLiner(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h, boolean isBtc) {
        if (k15m == null || k15m.length == 0) return "数据不足";
        SignalScorer.Score sc = SignalScorer.score(k15m, k1h, k4h, isBtc);
        MasterStrategy master = new MasterStrategy(isBtc);
        int sig = master.signal(k15m, k1h, k4h);
        double close = k15m[k15m.length - 1].getClose();
        return String.format("价格=%.2f  评分=%d/12  信号=%s",
                close, sc.total,
                sig == 1 ? "做多" : sig == -1 ? "做空" : "观望");
    }
}
