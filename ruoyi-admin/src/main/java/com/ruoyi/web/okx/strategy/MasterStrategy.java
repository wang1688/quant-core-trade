package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 综合主策略（Multi-level Hub Adaptive Strategy）
 *
 * 策略优先级：
 *   1. 熔断检查（最高优先级，触发则返回0）
 *   2. 中枢核心策略（主策略，评分≥7 + 三级别共振）
 *   3. 威科夫弹簧/上冲（高可靠性反转信号）
 *   4. 趋势加速策略（量价齐升/跌）
 *   5. 趋势末端反转（DIF极值 + 缩量）
 *   6. 区间震荡策略（低波动箱体内）
 *   7. 假突破策略（EMA30平坦时）
 *   8. 裸K形态（辅助确认）
 *   9. 15M试单（最宽松，评分≥5）
 */
public class MasterStrategy implements TradeStrategy {

    private final boolean isBtc;

    // 子策略实例
    private final CoreTrendStrategy core;
    private final TrendAccelerationStrategy acceleration;
    private final TrendReversalStrategy reversal;
    private final RangeOscillationStrategy range;
    private final FakeBreakoutStrategy fakeBreakout;
    private final NakedKStrategy nakedK;
    private final TrialPositionStrategy trial;

    // 运行时状态（回测时由外部注入，实盘时由服务层维护）
    private double dailyLossPct = 0;
    private int consecutiveLoss = 0;
    private double maxDrawdownPct = 0;

    public MasterStrategy() { this(true); }

    public MasterStrategy(boolean isBtc) {
        this.isBtc = isBtc;
        this.core = new CoreTrendStrategy(isBtc);
        this.acceleration = new TrendAccelerationStrategy(isBtc);
        this.reversal = new TrendReversalStrategy(isBtc);
        this.range = new RangeOscillationStrategy(isBtc);
        this.fakeBreakout = new FakeBreakoutStrategy(isBtc);
        this.nakedK = new NakedKStrategy();
        this.trial = new TrialPositionStrategy(isBtc);
    }

    @Override
    public String name() { return "综合主策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 30) return 0;

        // 1. 熔断检查
        CircuitBreaker.BreakerState breaker = CircuitBreaker.check(
                dailyLossPct, consecutiveLoss, maxDrawdownPct, k15m, isBtc);
        if (breaker.triggered) return 0;

        // 极端波动保护
        if (CircuitBreaker.extremeVolatility(k15m, isBtc)) return 0;

        // 2. 中枢核心策略（最高优先级）
        int sig = core.signal(k15m, k1h, k4h);
        if (sig != 0) return sig;

        // 3. 威科夫形态
        WyckoffDetector.WyckoffResult wyckoff = WyckoffDetector.detect(k15m, isBtc);
        if (wyckoff.signal != 0) return wyckoff.signal;

        // 4. 趋势加速
        sig = acceleration.signal(k15m, k1h, k4h);
        if (sig != 0) return sig;

        // 5. 趋势末端反转
        sig = reversal.signal(k15m, k1h, k4h);
        if (sig != 0) return sig;

        // 6. 区间震荡
        sig = range.signal(k15m, k1h, k4h);
        if (sig != 0) return sig;

        // 7. 假突破
        sig = fakeBreakout.signal(k15m, k1h, k4h);
        if (sig != 0) return sig;

        // 8. 裸K形态（需要1H趋势支持）
        sig = nakedK.signal(k15m, k1h, k4h);
        if (sig != 0) {
            // 裸K信号需要1H趋势方向一致才采纳
            if (k1h != null && k1h.length >= 30) {
                TrendAnalyzer.TrendDir t1h = TrendAnalyzer.trendOf(k1h);
                if ((sig == 1 && t1h == TrendAnalyzer.TrendDir.UP)
                        || (sig == -1 && t1h == TrendAnalyzer.TrendDir.DOWN)) {
                    return sig;
                }
            }
        }

        // 9. 15M试单（最宽松）
        return trial.signal(k15m, k1h, k4h);
    }

    // ==================== 运行时状态更新（实盘使用）====================

    public void updateRiskState(double dailyLossPct, int consecutiveLoss, double maxDrawdownPct) {
        this.dailyLossPct = dailyLossPct;
        this.consecutiveLoss = consecutiveLoss;
        this.maxDrawdownPct = maxDrawdownPct;
    }

    public void resetDailyState() {
        this.dailyLossPct = 0;
        this.consecutiveLoss = 0;
    }

    /**
     * 获取当前信号的风险档位建议
     */
    public int suggestTier(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        SignalScorer.Score sc = SignalScorer.score(k15m, k1h, k4h, isBtc);
        if (!sc.canOpen()) return 0;
        return RiskManager.tierFromScore(sc.total);
    }

    /**
     * 获取完整信号详情（用于日志/监控）
     */
    public SignalScorer.Score scoreDetail(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        return SignalScorer.score(k15m, k1h, k4h, isBtc);
    }
}
