package com.ruoyi.web.okx.strategynew.pattern;

import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.model.KLine;
import java.math.BigDecimal;
import java.util.List;

/**
 * 裸K形态识别器
 */
public class PatternRecognizer {

    /**
     * 识别强反转形态（优先级1）
     */
    public static boolean isStrongReversalPattern(List<KLine> klines, Direction direction, BigDecimal avgBody) {
        if (klines.size() < 5) {
            return false;
        }

        int size = klines.size();
        KLine lastKline = klines.get(size - 1);

        boolean hasContinuousBearish = checkContinuousBearish(klines, 4);
        boolean hasLowerShadowPin = checkLowerShadowPin(klines, avgBody);
        boolean isStrongBullishReversal = checkStrongBullishReversal(lastKline, avgBody);

        if (direction == Direction.LONG) {
            return hasContinuousBearish && hasLowerShadowPin && isStrongBullishReversal;
        } else {
            boolean hasContinuousBullish = checkContinuousBullish(klines, 4);
            boolean hasUpperShadowPin = checkUpperShadowPin(klines, avgBody);
            boolean isStrongBearishReversal = checkStrongBearishReversal(lastKline, avgBody);
            return hasContinuousBullish && hasUpperShadowPin && isStrongBearishReversal;
        }
    }

    /**
     * 识别次强反转形态（优先级2）
     */
    public static boolean isSecondaryReversalPattern(List<KLine> klines, Direction direction, BigDecimal avgBody) {
        if (klines.size() < 6) {
            return false;
        }

        int size = klines.size();
        KLine lastKline = klines.get(size - 1);
        KLine secondLastKline = klines.get(size - 2);

        if (direction == Direction.LONG) {
            boolean hasContinuousBearish = checkContinuousBearish(klines, 4);
            boolean hasLowerShadowPin = checkLowerShadowPin(klines, avgBody);
            boolean isWeakReversal = checkWeakBullishReversal(secondLastKline, avgBody);
            boolean isConfirmation = checkBullishConfirmation(lastKline, secondLastKline, avgBody);

            return hasContinuousBearish && hasLowerShadowPin && isWeakReversal && isConfirmation;
        } else {
            boolean hasContinuousBullish = checkContinuousBullish(klines, 4);
            boolean hasUpperShadowPin = checkUpperShadowPin(klines, avgBody);
            boolean isWeakReversal = checkWeakBearishReversal(secondLastKline, avgBody);
            boolean isConfirmation = checkBearishConfirmation(lastKline, secondLastKline, avgBody);

            return hasContinuousBullish && hasUpperShadowPin && isWeakReversal && isConfirmation;
        }
    }

    /**
     * 检查连续阴K
     */
    private static boolean checkContinuousBearish(List<KLine> klines, int count) {
        int size = klines.size();
        int bearishCount = 0;
        int noiseAllowed = 1;

        for (int i = size - count - 1; i < size - 1; i++) {
            if (klines.get(i).isBearish()) {
                bearishCount++;
            } else if (isNoiseKline(klines.get(i), calculateAvgBody(klines, 20))) {
                noiseAllowed--;
            }
        }

        return bearishCount >= count && noiseAllowed >= 0;
    }

    /**
     * 检查连续阳K
     */
    private static boolean checkContinuousBullish(List<KLine> klines, int count) {
        int size = klines.size();
        int bullishCount = 0;
        int noiseAllowed = 1;

        for (int i = size - count - 1; i < size - 1; i++) {
            if (klines.get(i).isBullish()) {
                bullishCount++;
            } else if (isNoiseKline(klines.get(i), calculateAvgBody(klines, 20))) {
                noiseAllowed--;
            }
        }

        return bullishCount >= count && noiseAllowed >= 0;
    }

    /**
     * 检查下影线插针
     */
    private static boolean checkLowerShadowPin(List<KLine> klines, BigDecimal avgBody) {
        int size = klines.size();
        for (int i = size - 3; i < size - 1; i++) {
            KLine kline = klines.get(i);
            if (kline.getLowerShadow().compareTo(kline.getBodySize()) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查上影线插针
     */
    private static boolean checkUpperShadowPin(List<KLine> klines, BigDecimal avgBody) {
        int size = klines.size();
        for (int i = size - 3; i < size - 1; i++) {
            KLine kline = klines.get(i);
            if (kline.getUpperShadow().compareTo(kline.getBodySize()) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查强阳线反转（阳包阴/长下影）
     */
    private static boolean checkStrongBullishReversal(KLine kline, BigDecimal avgBody) {
        if (!kline.isBullish()) {
            return false;
        }

        BigDecimal twoTimesAvg = avgBody.multiply(BigDecimal.valueOf(2));
        boolean isLargeBody = kline.getBodySize().compareTo(twoTimesAvg) >= 0;
        boolean isLongLowerShadow = kline.getLowerShadow().compareTo(
            kline.getBodySize().multiply(BigDecimal.valueOf(2))
        ) >= 0;

        return isLargeBody || isLongLowerShadow;
    }

    /**
     * 检查强阴线反转（阴包阳/长上影）
     */
    private static boolean checkStrongBearishReversal(KLine kline, BigDecimal avgBody) {
        if (!kline.isBearish()) {
            return false;
        }

        BigDecimal twoTimesAvg = avgBody.multiply(BigDecimal.valueOf(2));
        boolean isLargeBody = kline.getBodySize().compareTo(twoTimesAvg) >= 0;
        boolean isLongUpperShadow = kline.getUpperShadow().compareTo(
            kline.getBodySize().multiply(BigDecimal.valueOf(2))
        ) >= 0;

        return isLargeBody || isLongUpperShadow;
    }

    /**
     * 检查弱阳线反转
     */
    private static boolean checkWeakBullishReversal(KLine kline, BigDecimal avgBody) {
        if (!kline.isBullish()) {
            return false;
        }

        BigDecimal twoTimesAvg = avgBody.multiply(BigDecimal.valueOf(2));
        boolean isSmallBody = kline.getBodySize().compareTo(twoTimesAvg) < 0;
        BigDecimal shadowRatio = kline.getLowerShadow().divide(kline.getBodySize(), 8, BigDecimal.ROUND_HALF_UP);
        boolean isMediumShadow = shadowRatio.compareTo(BigDecimal.valueOf(1.5)) >= 0 &&
                                 shadowRatio.compareTo(BigDecimal.valueOf(2.0)) < 0;

        return isSmallBody || isMediumShadow;
    }

    /**
     * 检查弱阴线反转
     */
    private static boolean checkWeakBearishReversal(KLine kline, BigDecimal avgBody) {
        if (!kline.isBearish()) {
            return false;
        }

        BigDecimal twoTimesAvg = avgBody.multiply(BigDecimal.valueOf(2));
        boolean isSmallBody = kline.getBodySize().compareTo(twoTimesAvg) < 0;
        BigDecimal shadowRatio = kline.getUpperShadow().divide(kline.getBodySize(), 8, BigDecimal.ROUND_HALF_UP);
        boolean isMediumShadow = shadowRatio.compareTo(BigDecimal.valueOf(1.5)) >= 0 &&
                                 shadowRatio.compareTo(BigDecimal.valueOf(2.0)) < 0;

        return isSmallBody || isMediumShadow;
    }

    /**
     * 检查阳线确认
     */
    private static boolean checkBullishConfirmation(KLine current, KLine previous, BigDecimal avgBody) {
        if (!current.isBullish()) {
            return false;
        }

        BigDecimal bodyRatio = current.getBodySize().divide(previous.getBodySize(), 8, BigDecimal.ROUND_HALF_UP);
        boolean isSufficientBody = bodyRatio.compareTo(BigDecimal.valueOf(0.8)) >= 0;
        boolean isAbovePreviousOpen = current.getClose().compareTo(previous.getOpen()) > 0;

        return isSufficientBody && isAbovePreviousOpen;
    }

    /**
     * 检查阴线确认
     */
    private static boolean checkBearishConfirmation(KLine current, KLine previous, BigDecimal avgBody) {
        if (!current.isBearish()) {
            return false;
        }

        BigDecimal bodyRatio = current.getBodySize().divide(previous.getBodySize(), 8, BigDecimal.ROUND_HALF_UP);
        boolean isSufficientBody = bodyRatio.compareTo(BigDecimal.valueOf(0.8)) >= 0;
        boolean isBelowPreviousOpen = current.getClose().compareTo(previous.getOpen()) < 0;

        return isSufficientBody && isBelowPreviousOpen;
    }

    /**
     * 判断是否杂波K线
     */
    private static boolean isNoiseKline(KLine kline, BigDecimal avgBody) {
        BigDecimal threshold = avgBody.multiply(BigDecimal.valueOf(0.3));
        return kline.getBodySize().compareTo(threshold) <= 0;
    }

    /**
     * 计算平均实体大小
     */
    private static BigDecimal calculateAvgBody(List<KLine> klines, int period) {
        if (klines.size() < period) {
            period = klines.size();
        }

        BigDecimal sum = BigDecimal.ZERO;
        int start = klines.size() - period;
        for (int i = start; i < klines.size(); i++) {
            sum = sum.add(klines.get(i).getBodySize());
        }

        return sum.divide(BigDecimal.valueOf(period), 8, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 识别顶分型
     */
    public static boolean isTopFractal(List<KLine> klines, BigDecimal avgBody) {
        if (klines.size() < 3) {
            return false;
        }

        int size = klines.size();
        KLine left = klines.get(size - 3);
        KLine middle = klines.get(size - 2);
        KLine right = klines.get(size - 1);

        BigDecimal middleHigh = middle.getHigh();
        boolean isHighest = middleHigh.compareTo(left.getHigh()) > 0 &&
                           middleHigh.compareTo(right.getHigh()) > 0;

        BigDecimal threshold = avgBody.multiply(BigDecimal.valueOf(0.5));
        boolean isSufficientBody = middle.getBodySize().compareTo(threshold) >= 0;

        BigDecimal upperShadowLimit = middle.getBodySize().multiply(BigDecimal.valueOf(1.5));
        boolean isValidShadow = middle.getUpperShadow().compareTo(upperShadowLimit) <= 0;

        return isHighest && isSufficientBody && isValidShadow;
    }

    /**
     * 识别底分型
     */
    public static boolean isBottomFractal(List<KLine> klines, BigDecimal avgBody) {
        if (klines.size() < 3) {
            return false;
        }

        int size = klines.size();
        KLine left = klines.get(size - 3);
        KLine middle = klines.get(size - 2);
        KLine right = klines.get(size - 1);

        BigDecimal middleLow = middle.getLow();
        boolean isLowest = middleLow.compareTo(left.getLow()) < 0 &&
                          middleLow.compareTo(right.getLow()) < 0;

        BigDecimal threshold = avgBody.multiply(BigDecimal.valueOf(0.5));
        boolean isSufficientBody = middle.getBodySize().compareTo(threshold) >= 0;

        BigDecimal lowerShadowLimit = middle.getBodySize().multiply(BigDecimal.valueOf(1.5));
        boolean isValidShadow = middle.getLowerShadow().compareTo(lowerShadowLimit) <= 0;

        return isLowest && isSufficientBody && isValidShadow;
    }
}
