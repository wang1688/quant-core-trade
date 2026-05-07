package com.ruoyi.web.okx.strategynew.strategy;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.indicator.*;
import com.ruoyi.web.okx.strategynew.model.*;
import com.ruoyi.web.okx.strategynew.pattern.PatternRecognizer;
import java.math.BigDecimal;

import java.util.List;

/**
 * 辅助策略2：趋势末端反转策略
 */
public class TrendReversalStrategy {

    private String symbol;
    private VolatilityLevel volatilityLevel;

    public TrendReversalStrategy(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 分析趋势末端反转并生成信号
     */
    public Signal analyze(List<KLine> klines15M, List<KLine> klines1H) {
        if (klines15M == null || klines15M.size() < 50 || klines1H == null || klines1H.size() < 50) {
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

        Direction reversalDirection = detectReversalSignal(klines15M, klines1H, ema30_15M, ema6_15M,
                                                           ema13_15M, atr15, ema30Angle);
        if (reversalDirection == null) {
            return null;
        }

        KLine lastKline = klines15M.get(klines15M.size() - 1);
        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines15M, 20);

        boolean volumeConfirm = IndicatorCalculator.isVolumeExpanded(
            lastKline.getVolume(), avgVolume, Constants.VOLUME_HIGH
        );

        if (!volumeConfirm) {
            return null;
        }

        Signal signal = new Signal(symbol, reversalDirection, "TrendReversal");
        signal.setEntryPrice(currentPrice);
        signal.setTimeframe("15M");
        signal.setScore(5.5);

        BigDecimal stopLoss = calculateReversalStopLoss(currentPrice, klines15M, reversalDirection);
        signal.setStopLoss(stopLoss);

        BigDecimal takeProfit = calculateReversalTakeProfit(currentPrice, ema30_15M, reversalDirection);
        signal.setTakeProfit(takeProfit);

        signal.setReason("趋势末端反转");

        return signal;
    }

    /**
     * 检测反转信号（满足任意2条+箱体突破衰竭）
     */
    private Direction detectReversalSignal(List<KLine> klines15M, List<KLine> klines1H,
                                           List<BigDecimal> ema30, List<BigDecimal> ema6,
                                           List<BigDecimal> ema13, BigDecimal atr, double ema30Angle) {
        int conditions = 0;
        Direction potentialDirection = null;

        BigDecimal currentPrice = klines15M.get(klines15M.size() - 1).getClose();
        BigDecimal ema30Val = ema30.get(ema30.size() - 1);

        BigDecimal deviation = currentPrice.subtract(ema30Val).abs();
        BigDecimal deviationThreshold = atr.multiply(BigDecimal.valueOf(
            volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID ? 1.2 : 1.5
        ));

        if (deviation.compareTo(deviationThreshold) > 0) {
            conditions++;
            potentialDirection = currentPrice.compareTo(ema30Val) < 0 ? Direction.LONG : Direction.SHORT;
        }

        if (checkPriceDivergence(klines15M)) {
            conditions++;
        }

        if (checkLongShadow(klines15M)) {
            conditions++;
        }

        BigDecimal avgBody = IndicatorCalculator.calculateAvgBodySize(klines15M, 20);
        if (PatternRecognizer.isTopFractal(klines15M, avgBody)) {
            conditions++;
            potentialDirection = Direction.SHORT;
        } else if (PatternRecognizer.isBottomFractal(klines15M, avgBody)) {
            conditions++;
            potentialDirection = Direction.LONG;
        }

        double absAngle = Math.abs(ema30Angle);
        double minAngle = volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID ?
                         Constants.EMA30_ANGLE_LOW_VOL : Constants.EMA30_ANGLE_HIGH_VOL;
        if (absAngle >= minAngle && absAngle <= Constants.EMA30_ANGLE_STRONG) {
            conditions++;
        }

        if (conditions >= 2) {
            return potentialDirection;
        }

        return null;
    }

    /**
     * 检查价格背离
     */
    private boolean checkPriceDivergence(List<KLine> klines) {
        if (klines.size() < 20) {
            return false;
        }

        int size = klines.size();
        BigDecimal recentHigh = klines.get(size - 1).getHigh();
        BigDecimal recentLow = klines.get(size - 1).getLow();

        for (int i = size - 10; i < size; i++) {
            if (klines.get(i).getHigh().compareTo(recentHigh) > 0) {
                recentHigh = klines.get(i).getHigh();
            }
            if (klines.get(i).getLow().compareTo(recentLow) < 0) {
                recentLow = klines.get(i).getLow();
            }
        }

        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines, 20);
        BigDecimal recentAvgVolume = BigDecimal.ZERO;
        for (int i = size - 10; i < size; i++) {
            recentAvgVolume = recentAvgVolume.add(klines.get(i).getVolume());
        }
        recentAvgVolume = recentAvgVolume.divide(BigDecimal.valueOf(10), 8, BigDecimal.ROUND_HALF_UP);

        boolean volumeShrink = IndicatorCalculator.isVolumeShrink(recentAvgVolume, avgVolume, Constants.VOLUME_SHRINK);

        double dif = IndicatorCalculator.calculateDIF(
            recentHigh, recentLow, klines.get(size - 1).getClose()
        );

        return volumeShrink && Math.abs(dif) <= Constants.DIF_MID;
    }

    /**
     * 检查长影线
     */
    private boolean checkLongShadow(List<KLine> klines) {
        if (klines.isEmpty()) {
            return false;
        }

        KLine lastKline = klines.get(klines.size() - 1);
        BigDecimal avgBody = IndicatorCalculator.calculateAvgBodySize(klines, 20);

        BigDecimal shadowThreshold = avgBody.multiply(BigDecimal.valueOf(
            volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID ? 2.5 : 3.0
        ));

        BigDecimal maxShadow = lastKline.getUpperShadow().max(lastKline.getLowerShadow());

        return maxShadow.compareTo(shadowThreshold) >= 0;
    }

    /**
     * 计算反转止损
     */
    private BigDecimal calculateReversalStopLoss(BigDecimal entryPrice, List<KLine> klines, Direction direction) {
        int size = klines.size();
        BigDecimal extremePrice;

        if (direction == Direction.LONG) {
            extremePrice = klines.get(size - 1).getLow();
            for (int i = size - 10; i < size; i++) {
                if (klines.get(i).getLow().compareTo(extremePrice) < 0) {
                    extremePrice = klines.get(i).getLow();
                }
            }
            int buffer = Constants.BTC.equals(symbol) ? 7000 : 400;
            return extremePrice.subtract(BigDecimal.valueOf(buffer * 0.0001));
        } else {
            extremePrice = klines.get(size - 1).getHigh();
            for (int i = size - 10; i < size; i++) {
                if (klines.get(i).getHigh().compareTo(extremePrice) > 0) {
                    extremePrice = klines.get(i).getHigh();
                }
            }
            int buffer = Constants.BTC.equals(symbol) ? 7000 : 400;
            return extremePrice.add(BigDecimal.valueOf(buffer * 0.0001));
        }
    }

    /**
     * 计算反转止盈
     */
    private BigDecimal calculateReversalTakeProfit(BigDecimal entryPrice, List<BigDecimal> ema30, Direction direction) {
        BigDecimal ema30Val = ema30.get(ema30.size() - 1);

        if (direction == Direction.LONG) {
            return ema30Val;
        } else {
            return ema30Val;
        }
    }
}
