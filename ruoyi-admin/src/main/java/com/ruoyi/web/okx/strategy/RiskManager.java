package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 风险管理与仓位计算
 *
 * 风险档位（-1 ~ 8档）：
 *   -1档：熔断，禁止开仓
 *    0档：观望，r=0
 *    1档：r=0.7%
 *    2档：r=1.0%
 *    3档：r=1.5%
 *    4档：r=2.0%
 *    5档：r=2.5%
 *    6档：r=3.0%
 *    7档：r=4.0%
 *    8档：r=5.0%
 *
 * 仓位计算：
 *   止损距离 = ATR × 1.5（默认）
 *   合约张数 = 账户权益 × r% / 止损距离 / 合约面值
 */
public class RiskManager {

    // 各档位对应风险敞口百分比
    private static final double[] RISK_TABLE = {
            0.0,  // 0档
            0.7,  // 1档
            1.0,  // 2档
            1.5,  // 3档
            2.0,  // 4档
            2.5,  // 5档
            3.0,  // 6档
            4.0,  // 7档
            5.0   // 8档
    };

    // BTC合约面值（1张 = 0.01 BTC，约600~700 USDT）
    public static final double BTC_CONTRACT_VALUE = 0.01;
    // ETH合约面值（1张 = 0.1 ETH，约200~300 USDT）
    public static final double ETH_CONTRACT_VALUE = 0.1;

    public static class PositionResult {
        public final int tier;           // 风险档位
        public final double riskPct;     // 风险敞口%
        public final double stopLoss;    // 止损价格距离（USDT）
        public final int contracts;      // 建议合约张数
        public final boolean canOpen;    // 是否可以开仓

        public PositionResult(int tier, double riskPct, double stopLoss, int contracts, boolean canOpen) {
            this.tier = tier;
            this.riskPct = riskPct;
            this.stopLoss = stopLoss;
            this.contracts = contracts;
            this.canOpen = canOpen;
        }
    }

    /**
     * 根据信号评分自动确定风险档位
     * 评分5~6 → 1~2档，7~8 → 3~4档，9~10 → 5~6档，11~12 → 7~8档
     */
    public static int tierFromScore(int score) {
        if (score < 5)  return 0;
        if (score <= 6) return score - 4;  // 5→1, 6→2
        if (score <= 8) return score - 4;  // 7→3, 8→4
        if (score <= 10) return score - 4; // 9→5, 10→6
        return score - 4;                  // 11→7, 12→8
    }

    /**
     * 计算仓位
     * @param tier        风险档位（0~8，-1=熔断）
     * @param equity      账户权益（USDT）
     * @param entryPrice  入场价格
     * @param atrPct      ATR百分比（如1.2表示1.2%）
     * @param isBtc       true=BTC，false=ETH
     * @param leverage    杠杆倍数（默认100）
     */
    public static PositionResult calculate(int tier, double equity, double entryPrice,
                                           double atrPct, boolean isBtc, int leverage) {
        if (tier < 0) {
            return new PositionResult(-1, 0, 0, 0, false);
        }
        if (tier == 0 || equity <= 0 || entryPrice <= 0) {
            return new PositionResult(0, 0, 0, 0, false);
        }

        int safeTier = Math.min(tier, 8);
        double riskPct = RISK_TABLE[safeTier];

        // 止损距离 = ATR × 1.5（USDT）
        double atrUsdt = entryPrice * atrPct / 100;
        double stopLoss = atrUsdt * 1.5;
        if (stopLoss <= 0) stopLoss = entryPrice * 0.01; // 兜底1%

        // 风险金额
        double riskAmount = equity * riskPct / 100;

        // 合约面值（USDT）
        double contractFaceValue = isBtc
                ? entryPrice * BTC_CONTRACT_VALUE
                : entryPrice * ETH_CONTRACT_VALUE;

        // 合约张数 = 风险金额 / 止损距离 × 合约面值（考虑杠杆）
        // 实际：每张合约亏损 = stopLoss / entryPrice × contractFaceValue × leverage
        // 简化：contracts = riskAmount / (stopLoss / entryPrice × contractFaceValue)
        double lossPerContract = (stopLoss / entryPrice) * contractFaceValue;
        if (lossPerContract <= 0) {
            return new PositionResult(safeTier, riskPct, stopLoss, 0, false);
        }

        int contracts = (int) Math.floor(riskAmount / lossPerContract);
        contracts = Math.max(1, contracts); // 最少1张

        return new PositionResult(safeTier, riskPct, stopLoss, contracts, true);
    }

    /**
     * 计算止损价格
     * @param entryPrice 入场价
     * @param direction  1=多，-1=空
     * @param stopLoss   止损距离（USDT）
     */
    public static double stopLossPrice(double entryPrice, int direction, double stopLoss) {
        return direction == 1 ? entryPrice - stopLoss : entryPrice + stopLoss;
    }

    /**
     * 计算止盈价格（默认盈亏比2:1）
     */
    public static double takeProfitPrice(double entryPrice, int direction, double stopLoss) {
        return direction == 1 ? entryPrice + stopLoss * 2 : entryPrice - stopLoss * 2;
    }

    /**
     * 从K线数据计算仓位（便捷方法）
     */
    public static PositionResult calculateFromKline(int tier, double equity,
                                                     KLineVO[] k15m, boolean isBtc) {
        if (k15m == null || k15m.length == 0) {
            return new PositionResult(0, 0, 0, 0, false);
        }
        double entryPrice = k15m[k15m.length - 1].getClose();
        double atrPct = Indicators.atrPct(k15m, 14);
        return calculate(tier, equity, entryPrice, atrPct, isBtc, 100);
    }
}
