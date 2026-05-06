package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 熔断机制
 * 触发条件（任一满足）：
 *   1. 单日亏损 ≥ 账户权益 6%
 *   2. 连续亏损 ≥ 3笔
 *   3. 最大回撤 ≥ 15%
 *   4. ATR异常放大（≥ 正常值3倍，极端行情）
 *
 * 熔断后：
 *   - 禁止开新仓（风险档位设为-1）
 *   - 冷却期：日内熔断冷却4小时，连亏熔断冷却2小时
 */
public class CircuitBreaker {

    public static class BreakerState {
        public final boolean triggered;
        public final String reason;
        public final int cooldownMinutes; // 冷却时间（分钟）

        public BreakerState(boolean triggered, String reason, int cooldownMinutes) {
            this.triggered = triggered;
            this.reason = reason;
            this.cooldownMinutes = cooldownMinutes;
        }

        public static BreakerState ok() {
            return new BreakerState(false, "", 0);
        }
    }

    /**
     * 检查是否触发熔断
     * @param dailyLossPct    当日亏损百分比（正数表示亏损，如6.0表示亏损6%）
     * @param consecutiveLoss 连续亏损笔数
     * @param maxDrawdownPct  当前最大回撤百分比
     * @param k15m            15M K线（用于检测极端波动）
     * @param isBtc           true=BTC，false=ETH
     */
    public static BreakerState check(double dailyLossPct, int consecutiveLoss,
                                      double maxDrawdownPct, KLineVO[] k15m, boolean isBtc) {
        // 条件1：单日亏损≥6%
        if (dailyLossPct >= 6.0) {
            return new BreakerState(true, "单日亏损达" + String.format("%.1f", dailyLossPct) + "%", 240);
        }

        // 条件2：连续亏损≥3笔
        if (consecutiveLoss >= 3) {
            return new BreakerState(true, "连续亏损" + consecutiveLoss + "笔", 120);
        }

        // 条件3：最大回撤≥15%
        if (maxDrawdownPct >= 15.0) {
            return new BreakerState(true, "最大回撤达" + String.format("%.1f", maxDrawdownPct) + "%", 480);
        }

        // 条件4：ATR异常放大（极端行情）
        if (k15m != null && k15m.length >= 30) {
            double atrNow = Indicators.atrPct(k15m, 3);   // 近3根ATR
            double atrNorm = Indicators.atrPct(k15m, 14); // 正常ATR
            if (atrNorm > 0 && atrNow >= atrNorm * 3.0) {
                return new BreakerState(true, "ATR异常放大（极端行情）", 60);
            }
        }

        return BreakerState.ok();
    }

    /**
     * 检查是否触发单笔止损熔断（单笔亏损超过账户2%）
     */
    public static boolean singleTradeLossBreaker(double tradeLossPct) {
        return tradeLossPct >= 2.0;
    }

    /**
     * 根据ATR判断当前市场是否处于极端波动（不适合交易）
     */
    public static boolean extremeVolatility(KLineVO[] k15m, boolean isBtc) {
        if (k15m == null || k15m.length < 14) return false;
        double atr = Indicators.atrPct(k15m, 14);
        // BTC ATR > 3%，ETH ATR > 4% 视为极端波动
        double threshold = isBtc ? 3.0 : 4.0;
        return atr >= threshold;
    }
}
