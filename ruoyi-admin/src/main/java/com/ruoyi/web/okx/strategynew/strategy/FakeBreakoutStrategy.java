package com.ruoyi.web.okx.strategynew.strategy;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.indicator.*;
import com.ruoyi.web.okx.strategynew.model.*;
import java.math.BigDecimal;

import java.util.List;

/**
 * 辅助策略4：假突破策略
 */
public class FakeBreakoutStrategy {

    private String symbol;
    private VolatilityLevel volatilityLevel;

    public FakeBreakoutStrategy(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 分析假突破并生成信号
     */
    public Signal analyze(List<KLine> klines15M, List<KLine> klines1H) {
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

        if (Math.abs(ema30Angle) > Constants.EMA30_ANGLE_BUFFER) {
            return null;
        }

        Box box15M = BoxDetector.detectBox(klines15M, 9, symbol, "15M");
        if (box15M == null) {
            return null;
        }

        Direction fakeBreakoutDirection = detectFakeBreakout(klines15M, box15M, ema6_15M, ema13_15M, currentPrice);
        if (fakeBreakoutDirection == null) {
            return null;
        }

        Signal signal = new Signal(symbol, fakeBreakoutDirection, "FakeBreakout");
        signal.setEntryPrice(currentPrice);
        signal.setTimeframe("15M");
        signal.setScore(4.5);

        BigDecimal stopLoss = calculateFakeBreakoutStopLoss(currentPrice, klines15M, fakeBreakoutDirection);
        signal.setStopLoss(stopLoss);

        BigDecimal takeProfit = calculateFakeBreakoutTakeProfit(currentPrice, box15M, fakeBreakoutDirection);
        signal.setTakeProfit(takeProfit);

        signal.setReason("假突破反向入场");

        return signal;
    }

    /**
     * 检测假突破（5条满足4条）
     */
    private Direction detectFakeBreakout(List<KLine> klines, Box box, List<BigDecimal> ema6,
                                         List<BigDecimal> ema13, BigDecimal currentPrice) {
        int conditions = 0;
        Direction potentialDirection = null;

        int penetrationTicks = Constants.BTC.equals(symbol) ? 2100 : 650;
        BigDecimal penetrationAmount = BigDecimal.valueOf(penetrationTicks * 0.0001);

        boolean isPenetrationUp = currentPrice.compareTo(box.getUpper()) > 0 &&
                                  currentPrice.subtract(box.getUpper()).compareTo(penetrationAmount) <= 0;
        boolean isPenetrationDown = currentPrice.compareTo(box.getLower()) < 0 &&
                                    box.getLower().subtract(currentPrice).compareTo(penetrationAmount) <= 0;

        if (isPenetrationUp || isPenetrationDown) {
            conditions++;
            potentialDirection = isPenetrationUp ? Direction.SHORT : Direction.LONG;
        }

        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines, 20);
        KLine lastKline = klines.get(klines.size() - 1);

        if (IndicatorCalculator.isVolumeShrink(lastKline.getVolume(), avgVolume, 1.0)) {
            conditions++;
        }

        BigDecimal avgBody = IndicatorCalculator.calculateAvgBodySize(klines, 20);
        BigDecimal shadowThreshold = lastKline.getBodySize().multiply(BigDecimal.valueOf(
            volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID ? 1.5 : 2.0
        ));

        BigDecimal relevantShadow = isPenetrationUp ? lastKline.getUpperShadow() : lastKline.getLowerShadow();
        if (relevantShadow.compareTo(shadowThreshold) >= 0) {
            conditions++;
        }

        if (checkPriceReturnToBox(klines, box, 5)) {
            conditions++;
            double volumeMultiplier = volatilityLevel == VolatilityLevel.LOW || volatilityLevel == VolatilityLevel.MID ?
                                     1.5 : Constants.VOLUME_HIGH;
            if (IndicatorCalculator.isVolumeExpanded(lastKline.getVolume(), avgVolume, volumeMultiplier)) {
                conditions++;
            }
        }

        double dif = IndicatorCalculator.calculateDIF(
            ema6.get(ema6.size() - 1),
            ema13.get(ema13.size() - 1),
            currentPrice
        );

        if (Math.abs(dif) < Constants.DIF_MID) {
            conditions++;
        }

        if (conditions >= 4) {
            return potentialDirection;
        }

        return null;
    }

    /**
     * 检查价格是否回到箱体内
     */
    private boolean checkPriceReturnToBox(List<KLine> klines, Box box, int lookback) {
        if (klines.size() < lookback) {
            return false;
        }

        int size = klines.size();
        for (int i = size - lookback; i < size; i++) {
            if (box.isInside(klines.get(i).getClose())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 计算假突破止损
     */
    private BigDecimal calculateFakeBreakoutStopLoss(BigDecimal entryPrice, List<KLine> klines, Direction direction) {
        int size = klines.size();
        BigDecimal extremePrice;

        if (direction == Direction.LONG) {
            extremePrice = klines.get(size - 1).getLow();
            for (int i = size - 5; i < size; i++) {
                if (klines.get(i).getLow().compareTo(extremePrice) < 0) {
                    extremePrice = klines.get(i).getLow();
                }
            }
            int buffer = Constants.BTC.equals(symbol) ? 500 : 200;
            return extremePrice.subtract(BigDecimal.valueOf(buffer * 0.0001));
        } else {
            extremePrice = klines.get(size - 1).getHigh();
            for (int i = size - 5; i < size; i++) {
                if (klines.get(i).getHigh().compareTo(extremePrice) > 0) {
                    extremePrice = klines.get(i).getHigh();
                }
            }
            int buffer = Constants.BTC.equals(symbol) ? 500 : 200;
            return extremePrice.add(BigDecimal.valueOf(buffer * 0.0001));
        }
    }

    /**
     * 计算假突破止盈
     */
    private BigDecimal calculateFakeBreakoutTakeProfit(BigDecimal entryPrice, Box box, Direction direction) {
        if (direction == Direction.LONG) {
            return box.getUpper();
        } else {
            return box.getLower();
        }
    }
}
