package com.ruoyi.web.okx.strategynew.indicator;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.model.KLine;
import java.math.BigDecimal;
import java.util.List;

/**
 * 波动率分析器
 */
public class VolatilityAnalyzer {

    /**
     * 判定波动率级别
     */
    public static VolatilityLevel getVolatilityLevel(String symbol, double volatility) {
        if (Constants.BTC.equals(symbol)) {
            if (volatility >= Constants.BTC_EXTREME_VOL) {
                return VolatilityLevel.EXTREME;
            } else if (volatility >= Constants.BTC_HIGH_VOL) {
                return VolatilityLevel.HIGH;
            } else if (volatility >= Constants.BTC_MID_VOL_MIN) {
                return VolatilityLevel.MID;
            } else {
                return VolatilityLevel.LOW;
            }
        } else {
            if (volatility >= Constants.ETH_EXTREME_VOL) {
                return VolatilityLevel.EXTREME;
            } else if (volatility >= Constants.ETH_HIGH_VOL) {
                return VolatilityLevel.HIGH;
            } else if (volatility >= Constants.ETH_MID_VOL_MIN) {
                return VolatilityLevel.MID;
            } else {
                return VolatilityLevel.LOW;
            }
        }
    }

    /**
     * 获取有效EMA30角度阈值
     */
    public static double getValidEMA30Angle(VolatilityLevel level) {
        switch (level) {
            case LOW:
                return Constants.EMA30_ANGLE_LOW_VOL;
            case MID:
                return Constants.EMA30_ANGLE_MID_VOL;
            case HIGH:
            case EXTREME:
                return Constants.EMA30_ANGLE_HIGH_VOL;
            default:
                return Constants.EMA30_ANGLE_MID_VOL;
        }
    }

    /**
     * 获取资费缓冲跳数
     */
    public static int getFeeBuffer(String symbol, VolatilityLevel level) {
        boolean isHighVol = level == VolatilityLevel.HIGH || level == VolatilityLevel.EXTREME;

        if (Constants.BTC.equals(symbol)) {
            return isHighVol ? Constants.BTC_FEE_BUFFER_HIGH_VOL : Constants.BTC_FEE_BUFFER;
        } else {
            return isHighVol ? Constants.ETH_FEE_BUFFER_HIGH_VOL : Constants.ETH_FEE_BUFFER;
        }
    }

    /**
     * 获取有效箱体高度
     */
    public static int getValidBoxHeight(String symbol) {
        return Constants.BTC.equals(symbol) ?
            Constants.BTC_VALID_BOX_HEIGHT :
            Constants.ETH_VALID_BOX_HEIGHT;
    }

    /**
     * 获取突破确认跳数
     */
    public static int getBreakoutConfirmTicks(String symbol) {
        return Constants.BTC.equals(symbol) ?
            Constants.BTC_BREAKOUT_CONFIRM :
            Constants.ETH_BREAKOUT_CONFIRM;
    }

    /**
     * 计算仓位调整系数（高波/流动性预警）
     */
    public static double getPositionAdjustmentFactor(VolatilityLevel level, boolean liquidityWarning) {
        if (liquidityWarning) {
            return 0.5;
        }

        switch (level) {
            case HIGH:
                return 0.8;
            case EXTREME:
                return 0.3;
            default:
                return 1.0;
        }
    }

    /**
     * 判断是否流动性预警
     */
    public static boolean isLiquidityWarning(String symbol, double volatility) {
        if (Constants.BTC.equals(symbol)) {
            return volatility >= 0.024;
        } else {
            return volatility >= 0.028;
        }
    }

    /**
     * 获取DIF强发散阈值
     */
    public static double getStrongDIFThreshold(VolatilityLevel level, double ema30Angle) {
        if (level == VolatilityLevel.HIGH || level == VolatilityLevel.EXTREME) {
            return 0.15;
        } else if (Math.abs(ema30Angle) >= Constants.EMA30_ANGLE_VERY_STRONG) {
            return 0.07;
        } else {
            return Constants.DIF_STRONG;
        }
    }

    /**
     * 获取放量标准倍数
     */
    public static double getVolumeMultiplier(VolatilityLevel level) {
        switch (level) {
            case LOW:
            case MID:
                return Constants.VOLUME_NORMAL;
            case HIGH:
            case EXTREME:
                return Constants.VOLUME_HIGH;
            default:
                return Constants.VOLUME_NORMAL;
        }
    }
}
