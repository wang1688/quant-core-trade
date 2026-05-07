package com.ruoyi.web.okx.strategynew.indicator;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.model.KLine;
import java.math.BigDecimal;
import java.util.List;

/**
 * 信号评分系统
 */
public class SignalScorer {

    /**
     * 计算综合信号评分（满分12分）
     */
    public static double calculateScore(
        double ema30Angle,
        double dif,
        BigDecimal volume,
        BigDecimal avgVolume,
        boolean structureValid,
        VolatilityLevel volatilityLevel,
        boolean emaAlignmentComplete,
        Direction direction
    ) {
        double score = 0.0;

        score += scoreTrendStrength(ema30Angle);
        score += scoreVolumeMomentum(volume, avgVolume, volatilityLevel);
        score += scoreStructure(structureValid);
        score += scoreVolatilityMatch(volatilityLevel);
        score += scoreEMAAlignment(emaAlignmentComplete);

        return score;
    }

    /**
     * 趋势强度评分（0-3分）
     */
    private static double scoreTrendStrength(double ema30Angle) {
        double absAngle = Math.abs(ema30Angle);

        if (absAngle >= Constants.EMA30_ANGLE_VERY_STRONG) {
            return 3.0;
        } else if (absAngle >= Constants.EMA30_ANGLE_STRONG) {
            return 2.5;
        } else if (absAngle >= Constants.EMA30_ANGLE_HIGH_VOL) {
            return 2.0;
        } else if (absAngle >= Constants.EMA30_ANGLE_MID_VOL) {
            return 1.5;
        } else if (absAngle >= Constants.EMA30_ANGLE_LOW_VOL) {
            return 1.0;
        } else if (absAngle >= Constants.EMA30_ANGLE_BUFFER) {
            return 0.5;
        } else {
            return 0.0;
        }
    }

    /**
     * 量能共振评分（0-3分）
     */
    private static double scoreVolumeMomentum(BigDecimal volume, BigDecimal avgVolume, VolatilityLevel level) {
        if (avgVolume.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        BigDecimal ratio = volume.divide(avgVolume, 8, BigDecimal.ROUND_HALF_UP);
        double ratioValue = ratio.doubleValue();

        if (ratioValue >= Constants.VOLUME_STRONG) {
            return 3.0;
        } else if (ratioValue >= Constants.VOLUME_HIGH) {
            return 2.5;
        } else if (ratioValue >= Constants.VOLUME_NORMAL) {
            return 2.0;
        } else if (ratioValue >= 1.2) {
            return 1.5;
        } else if (ratioValue >= 1.0) {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    /**
     * 结构适配评分（0-2分）
     */
    private static double scoreStructure(boolean structureValid) {
        return structureValid ? 2.0 : 0.0;
    }

    /**
     * 波动匹配评分（0-2分）
     */
    private static double scoreVolatilityMatch(VolatilityLevel level) {
        switch (level) {
            case LOW:
                return 2.0;
            case MID:
                return 1.5;
            case HIGH:
                return 1.0;
            case EXTREME:
                return 0.5;
            default:
                return 0.0;
        }
    }

    /**
     * 均线排列完整性评分（0-2分）
     */
    private static double scoreEMAAlignment(boolean complete) {
        return complete ? 2.0 : 1.0;
    }

    /**
     * 判断信号是否达到开仓标准
     */
    public static boolean isValidSignal(double score, VolatilityLevel level) {
        switch (level) {
            case LOW:
                return score >= Constants.SIGNAL_SCORE_LOW;
            case MID:
            case HIGH:
                return score >= Constants.SIGNAL_SCORE_MID;
            case EXTREME:
                return score >= Constants.SIGNAL_SCORE_STRONG;
            default:
                return false;
        }
    }

    /**
     * 根据评分计算仓位系数
     */
    public static double getPositionMultiplier(double score) {
        if (score >= Constants.SIGNAL_SCORE_SUPER) {
            return 1.2;
        } else if (score >= Constants.SIGNAL_SCORE_STRONG) {
            return 1.0;
        } else if (score >= Constants.SIGNAL_SCORE_MID) {
            return 0.8;
        } else {
            return 0.8;
        }
    }

    /**
     * 判断是否强信号（≥7分）
     */
    public static boolean isStrongSignal(double score) {
        return score >= Constants.SIGNAL_SCORE_STRONG;
    }

    /**
     * 判断是否超强信号（≥9分）
     */
    public static boolean isSuperSignal(double score) {
        return score >= Constants.SIGNAL_SCORE_SUPER;
    }
}
