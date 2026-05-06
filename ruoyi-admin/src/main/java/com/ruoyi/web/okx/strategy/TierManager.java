package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 自动档位管理器
 * 根据近期表现自动升降风险档位
 *
 * 升档条件（连续盈利）：
 *   - 连续3笔盈利 → 档位+1（最高8档）
 *   - 单笔盈利 > 2% → 档位+1
 *
 * 降档条件（亏损保护）：
 *   - 单笔亏损 > 1% → 档位-1
 *   - 连续2笔亏损 → 档位-2
 *   - 触发熔断 → 档位=-1
 *
 * 重置条件：
 *   - 每日开盘重置为基础档位（默认3档）
 */
public class TierManager {

    private int currentTier;
    private final int baseTier;
    private final int maxTier;

    private int consecutiveWins  = 0;
    private int consecutiveLoss  = 0;
    private double dailyLossPct  = 0;
    private double maxDrawdownPct = 0;

    public TierManager() { this(3, 8); }

    public TierManager(int baseTier, int maxTier) {
        this.baseTier    = baseTier;
        this.currentTier = baseTier;
        this.maxTier     = maxTier;
    }

    /**
     * 每笔交易结束后更新档位
     * @param profitPct 本笔收益率（正=盈利，负=亏损，如0.015表示1.5%）
     */
    public void onTradeClose(double profitPct) {
        if (profitPct > 0) {
            consecutiveWins++;
            consecutiveLoss = 0;

            // 连续3笔盈利升档
            if (consecutiveWins >= 3) {
                upgrade(1);
                consecutiveWins = 0;
            }
            // 单笔盈利超2%升档
            if (profitPct > 0.02) {
                upgrade(1);
            }
        } else {
            consecutiveLoss++;
            consecutiveWins = 0;
            double lossPct = Math.abs(profitPct) * 100;
            dailyLossPct += lossPct;

            // 单笔亏损超1%降档
            if (lossPct > 1.0) {
                downgrade(1);
            }
            // 连续2笔亏损降2档
            if (consecutiveLoss >= 2) {
                downgrade(2);
                consecutiveLoss = 0;
            }
        }

        // 更新最大回撤（简化：用日内累计亏损近似）
        maxDrawdownPct = Math.max(maxDrawdownPct, dailyLossPct);
    }

    /**
     * 检查熔断并更新档位
     * @param k15m 当前15M K线（用于极端波动检测）
     * @param isBtc true=BTC
     */
    public void checkCircuitBreaker(KLineVO[] k15m, boolean isBtc) {
        CircuitBreaker.BreakerState state = CircuitBreaker.check(
                dailyLossPct, consecutiveLoss, maxDrawdownPct, k15m, isBtc);
        if (state.triggered) {
            currentTier = -1;
        }
    }

    /** 每日重置 */
    public void dailyReset() {
        currentTier    = baseTier;
        consecutiveWins = 0;
        consecutiveLoss = 0;
        dailyLossPct   = 0;
        maxDrawdownPct  = 0;
    }

    public int getCurrentTier() { return currentTier; }
    public double getDailyLossPct() { return dailyLossPct; }
    public int getConsecutiveLoss() { return consecutiveLoss; }
    public double getMaxDrawdownPct() { return maxDrawdownPct; }

    /** 是否熔断 */
    public boolean isCircuitBroken() { return currentTier < 0; }

    private void upgrade(int steps) {
        currentTier = Math.min(maxTier, currentTier + steps);
    }

    private void downgrade(int steps) {
        currentTier = Math.max(0, currentTier - steps);
    }
}
