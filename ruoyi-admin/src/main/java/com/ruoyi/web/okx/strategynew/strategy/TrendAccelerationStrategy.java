package com.ruoyi.web.okx.strategynew.strategy;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.indicator.*;
import com.ruoyi.web.okx.strategynew.model.*;
import java.math.BigDecimal;

import java.util.List;

/**
 * 辅助策略3：趋势加速策略
 */
public class TrendAccelerationStrategy {

    private String symbol;
    private VolatilityLevel volatilityLevel;

    public TrendAccelerationStrategy(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 分析趋势加速并生成信号
     */
    public Signal analyze(List<KLine> klines15M, List<KLine> klines1H, List<KLine> klines1M) {
        if (klines15M == null || klines15M.size() < 50) {
            return null;
        }

        List<BigDecimal> ema30_15M = IndicatorCalculator.calculateEMA(klines15M, 30);
        List<BigDecimal> ema6_15M = IndicatorCalculator.calculateEMA(klines15M, 6);
        List<BigDecimal> ema13_15M = IndicatorCalculator.calculateEMA(klines15M, 13);
        List<BigDecimal> atr15_15M = IndicatorCalculator.calculateATR(klines15M, 15);

        if (atr15_15M.isEmpty()) {
            return null;
        }

        BigDecimal currentPrice = klines15M.get(klines15M.size() - 1).getClose();
        BigDecimal atr15 = atr15_15M.get(atr15_15M.size() - 1);
        double volatility = IndicatorCalculator.calculateVolatility(atr15, currentPrice);
        this.volatilityLevel = VolatilityAnalyzer.getVolatilityLevel(symbol, volatility);

        double ema30Angle = IndicatorCalculator.calculateEMA30Angle(ema30_15M, 5);

        Direction accelerationDirection = detectAcceleration(klines15M, ema30_15M, ema6_15M,
                                                             ema13_15M, atr15, ema30Angle);
        if (accelerationDirection == null) {
            return null;
        }

        Signal signal = new Signal(symbol, accelerationDirection, "TrendAcceleration");
        signal.setTimeframe("15M");
        signal.setScore(7.0);

        BigDecimal entryPrice = calculateAccelerationEntry(currentPrice, klines15M, accelerationDirection);
        signal.setEntryPrice(entryPrice);

        BigDecimal stopLoss = calculateAccelerationStopLoss(entryPrice, klines15M, accelerationDirection);
        signal.setStopLoss(stopLoss);

        BigDecimal takeProfit = calculateAccelerationTakeProfit(entryPrice, atr15, accelerationDirection);
        signal.setTakeProfit(takeProfit);

        signal.setReason("趋势加速");

        return signal;
    }

    /**
     * 检测加速信号（满足任意2条）
     */
    private Direction detectAcceleration(List<KLine> klines, List<BigDecimal> ema30,
                                         List<BigDecimal> ema6, List<BigDecimal> ema13,
                                         BigDecimal atr, double ema30Angle) {
        int conditions = 0;
        Direction potentialDirection = null;

        if (checkContinuousLargeKlines(klines)) {
            conditions++;
            potentialDirection = klines.get(klines.size() - 1).isBullish() ? Direction.LONG : Direction.SHORT;
        }

        BigDecimal currentPrice = klines.get(klines.size() - 1).getClose();
        BigDecimal ema30Val = ema30.get(ema30.size() - 1);
        BigDecimal deviation = currentPrice.subtract(ema30Val).abs();
        BigDecimal deviationThreshold = atr.multiply(BigDecimal.valueOf(1.5));

        double dif = IndicatorCalculator.calculateDIF(
            ema6.get(ema6.size() - 1),
            ema13.get(ema13.size() - 1),
            currentPrice
        );

        if (deviation.compareTo(deviationThreshold) > 0 &&
            Math.abs(dif) > 0.15 &&
            Math.abs(ema30Angle) >= Constants.EMA30_ANGLE_HIGH_VOL) {
            conditions++;
        }

        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines, 20);
        KLine lastKline = klines.get(klines.size() - 1);
        double volumeMultiplier = volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID ?
                                 1.8 : Constants.VOLUME_HIGH;

        if (IndicatorCalculator.isVolumeExpanded(lastKline.getVolume(), avgVolume, volumeMultiplier)) {
            conditions++;
        }

        double volumeDivergence = IndicatorCalculator.calculateVolumeDivergence(klines, 10);
        if (volumeDivergence >= -0.1) {
            conditions++;
        }

        if (conditions >= 2) {
            return potentialDirection != null ? potentialDirection :
                   (ema30Angle > 0 ? Direction.LONG : Direction.SHORT);
        }

        return null;
    }

    /**
     * 检查连续大K线
     */
    private boolean checkContinuousLargeKlines(List<KLine> klines) {
        if (klines.size() < 3) {
            return false;
        }

        BigDecimal avgBody = IndicatorCalculator.calculateAvgBodySize(klines, 20);
        BigDecimal threshold = avgBody.multiply(BigDecimal.valueOf(
            volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID ? 2.0 : 3.0
        ));

        int size = klines.size();
        int largeKlineCount = 0;

        for (int i = size - 3; i < size; i++) {
            KLine kline = klines.get(i);
            if (kline.getBodySize().compareTo(threshold) >= 0) {
                BigDecimal oppositeShadow = kline.isBullish() ?
                    kline.getUpperShadow() : kline.getLowerShadow();
                BigDecimal shadowLimit = kline.getBodySize().multiply(BigDecimal.valueOf(0.5));

                if (oppositeShadow.compareTo(shadowLimit) < 0) {
                    largeKlineCount++;
                }
            }
        }

        if (volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID) {
            return largeKlineCount >= 2;
        } else {
            return largeKlineCount >= 3;
        }
    }

    /**
     * 计算加速入场价
     */
    private BigDecimal calculateAccelerationEntry(BigDecimal currentPrice, List<KLine> klines, Direction direction) {
        int size = klines.size();
        KLine accumulationKline = null;

        for (int i = size - 5; i < size - 1; i++) {
            KLine kline = klines.get(i);
            BigDecimal avgBody = IndicatorCalculator.calculateAvgBodySize(klines, 20);

            if (kline.getBodySize().compareTo(avgBody.multiply(BigDecimal.valueOf(0.8))) < 0) {
                accumulationKline = kline;
                break;
            }
        }

        if (accumulationKline == null) {
            return currentPrice;
        }

        int buffer = Constants.BTC.equals(symbol) ? 350 : 150;
        BigDecimal bufferAmount = BigDecimal.valueOf(buffer * 0.0001);

        if (direction == Direction.LONG) {
            return accumulationKline.getHigh().add(bufferAmount);
        } else {
            return accumulationKline.getLow().subtract(bufferAmount);
        }
    }

    /**
     * 计算加速止损
     */
    private BigDecimal calculateAccelerationStopLoss(BigDecimal entryPrice, List<KLine> klines, Direction direction) {
        int size = klines.size();
        KLine accumulationKline = null;

        for (int i = size - 5; i < size - 1; i++) {
            KLine kline = klines.get(i);
            BigDecimal avgBody = IndicatorCalculator.calculateAvgBodySize(klines, 20);

            if (kline.getBodySize().compareTo(avgBody.multiply(BigDecimal.valueOf(0.8))) < 0) {
                accumulationKline = kline;
                break;
            }
        }

        if (accumulationKline == null) {
            accumulationKline = klines.get(size - 2);
        }

        int buffer = Constants.BTC.equals(symbol) ? 350 : 150;
        BigDecimal bufferAmount = BigDecimal.valueOf(buffer * 0.0001);

        if (direction == Direction.LONG) {
            return accumulationKline.getLow().subtract(bufferAmount);
        } else {
            return accumulationKline.getHigh().add(bufferAmount);
        }
    }

    /**
     * 计算加速止盈
     */
    private BigDecimal calculateAccelerationTakeProfit(BigDecimal entryPrice, BigDecimal atr, Direction direction) {
        BigDecimal target = atr.multiply(BigDecimal.valueOf(2));

        if (direction == Direction.LONG) {
            return entryPrice.add(target);
        } else {
            return entryPrice.subtract(target);
        }
    }
}
