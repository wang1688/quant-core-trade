package com.ruoyi.web.okx.strategynew.strategy;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.indicator.*;
import com.ruoyi.web.okx.strategynew.model.*;
import java.math.BigDecimal;

import java.util.List;

/**
 * 辅助策略1：区间震荡策略
 */
public class RangeOscillationStrategy {

    private String symbol;
    private VolatilityLevel volatilityLevel;

    public RangeOscillationStrategy(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 分析震荡行情并生成信号
     */
    public Signal analyze(List<KLine> klines4H, List<KLine> klines15M) {
        if (klines4H == null || klines4H.size() < 50 || klines15M == null || klines15M.size() < 50) {
            return null;
        }

        List<BigDecimal> ema30_4H = IndicatorCalculator.calculateEMA(klines4H, 30);
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

        double ema30Angle_4H = IndicatorCalculator.calculateEMA30Angle(ema30_4H, 5);

        if (!isRangeMarket(ema30Angle_4H, ema6_15M, ema13_15M, klines15M, currentPrice, volatility)) {
            return null;
        }

        Box box15M = BoxDetector.detectBox(klines15M, 9, symbol, "15M");
        if (box15M == null) {
            return null;
        }

        Direction trendDirection = determineTrendDirection(ema30Angle_4H);
        if (trendDirection == null) {
            return null;
        }

        Signal signal = null;

        if (volatilityLevel == VolatilityLevel.LOW) {
            signal = analyzeGridTrading(box15M, currentPrice, trendDirection, atr15, klines15M);
        } else if (volatilityLevel == VolatilityLevel.MID) {
            signal = analyzeMidVolOscillation(box15M, currentPrice, trendDirection, atr15, klines15M);
        }

        return signal;
    }

    /**
     * 判断是否震荡行情（满足任意3条）
     */
    private boolean isRangeMarket(double ema30Angle_4H, List<BigDecimal> ema6, List<BigDecimal> ema13,
                                  List<KLine> klines, BigDecimal currentPrice, double volatility) {
        int conditions = 0;

        if (Math.abs(ema30Angle_4H) <= Constants.EMA30_ANGLE_BUFFER) {
            conditions++;
        }

        double dif = IndicatorCalculator.calculateDIF(
            ema6.get(ema6.size() - 1),
            ema13.get(ema13.size() - 1),
            currentPrice
        );
        if (Math.abs(dif) < Constants.DIF_MID) {
            conditions++;
        }

        Box box = BoxDetector.detectBox(klines, 9, symbol, "15M");
        if (box != null) {
            conditions++;
        }

        if (volatility <= (Constants.BTC.equals(symbol) ? Constants.BTC_MID_VOL_MAX : Constants.ETH_MID_VOL_MAX)) {
            conditions++;
        }

        double priceChangeRatio = calculatePriceChangeRatio(klines, 20);
        if (priceChangeRatio > 0.018) {
            conditions++;
        }

        return conditions >= 3;
    }

    /**
     * 计算价格变化比率
     */
    private double calculatePriceChangeRatio(List<KLine> klines, int period) {
        if (klines.size() < period) {
            return 0.0;
        }

        int start = klines.size() - period;
        BigDecimal highest = klines.get(start).getHigh();
        BigDecimal lowest = klines.get(start).getLow();

        for (int i = start + 1; i < klines.size(); i++) {
            if (klines.get(i).getHigh().compareTo(highest) > 0) {
                highest = klines.get(i).getHigh();
            }
            if (klines.get(i).getLow().compareTo(lowest) < 0) {
                lowest = klines.get(i).getLow();
            }
        }

        BigDecimal range = highest.subtract(lowest);
        return range.divide(lowest, 8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 判定趋势方向
     */
    private Direction determineTrendDirection(double ema30Angle) {
        if (ema30Angle > 0) {
            return Direction.LONG;
        } else if (ema30Angle < 0) {
            return Direction.SHORT;
        }
        return null;
    }

    /**
     * 分析低波网格交易
     */
    private Signal analyzeGridTrading(Box box, BigDecimal currentPrice, Direction direction,
                                      BigDecimal atr, List<KLine> klines) {
        BigDecimal gridSpacing = atr.multiply(BigDecimal.valueOf(0.6));
        double volatilityRatio = IndicatorCalculator.calculateVolatility(atr, currentPrice);

        if (volatilityRatio >= 0.004 && volatilityRatio < 0.006) {
            gridSpacing = atr.multiply(BigDecimal.valueOf(0.8));
        }

        BigDecimal middle = box.getMiddle();
        BigDecimal distanceFromMiddle = currentPrice.subtract(middle).abs();

        boolean shouldEnter = false;
        BigDecimal entryPrice = currentPrice;

        if (direction == Direction.LONG) {
            BigDecimal lowerBound = box.getLower().add(box.getHeight().multiply(BigDecimal.valueOf(0.1)));
            if (currentPrice.compareTo(lowerBound) <= 0) {
                shouldEnter = true;
                entryPrice = currentPrice;
            }
        } else {
            BigDecimal upperBound = box.getUpper().subtract(box.getHeight().multiply(BigDecimal.valueOf(0.1)));
            if (currentPrice.compareTo(upperBound) >= 0) {
                shouldEnter = true;
                entryPrice = currentPrice;
            }
        }

        if (!shouldEnter) {
            return null;
        }

        KLine lastKline = klines.get(klines.size() - 1);
        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines, 20);
        boolean volumeConfirm = IndicatorCalculator.isVolumeExpanded(lastKline.getVolume(), avgVolume, 1.2);

        if (!volumeConfirm) {
            return null;
        }

        Signal signal = new Signal(symbol, direction, "RangeGrid");
        signal.setEntryPrice(entryPrice);
        signal.setTimeframe("15M");
        signal.setScore(4.0);

        BigDecimal stopLoss = calculateGridStopLoss(entryPrice, box, direction, atr);
        signal.setStopLoss(stopLoss);

        BigDecimal takeProfit = calculateGridTakeProfit(entryPrice, box, direction);
        signal.setTakeProfit(takeProfit);

        signal.setReason("低波网格交易");

        return signal;
    }

    /**
     * 分析中波震荡交易
     */
    private Signal analyzeMidVolOscillation(Box box, BigDecimal currentPrice, Direction direction,
                                            BigDecimal atr, List<KLine> klines) {
        boolean shouldEnter = false;
        BigDecimal entryPrice = currentPrice;

        if (direction == Direction.LONG) {
            BigDecimal lowerBound = box.getLower().subtract(box.getHeight().multiply(BigDecimal.valueOf(0.1)));
            BigDecimal upperBound = box.getLower().add(box.getHeight().multiply(BigDecimal.valueOf(0.1)));
            if (currentPrice.compareTo(lowerBound) >= 0 && currentPrice.compareTo(upperBound) <= 0) {
                shouldEnter = true;
            }
        } else {
            BigDecimal lowerBound = box.getUpper().subtract(box.getHeight().multiply(BigDecimal.valueOf(0.1)));
            BigDecimal upperBound = box.getUpper().add(box.getHeight().multiply(BigDecimal.valueOf(0.1)));
            if (currentPrice.compareTo(lowerBound) >= 0 && currentPrice.compareTo(upperBound) <= 0) {
                shouldEnter = true;
            }
        }

        if (!shouldEnter) {
            return null;
        }

        KLine lastKline = klines.get(klines.size() - 1);
        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines, 20);
        boolean volumeConfirm = IndicatorCalculator.isVolumeExpanded(lastKline.getVolume(), avgVolume, 1.2);

        boolean directionConfirm = (direction == Direction.LONG && lastKline.isBullish()) ||
                                   (direction == Direction.SHORT && lastKline.isBearish());

        if (!volumeConfirm || !directionConfirm) {
            return null;
        }

        Signal signal = new Signal(symbol, direction, "RangeOscillation");
        signal.setEntryPrice(entryPrice);
        signal.setTimeframe("15M");
        signal.setScore(4.5);

        BigDecimal stopLoss = calculateOscillationStopLoss(entryPrice, box, direction, atr);
        signal.setStopLoss(stopLoss);

        BigDecimal takeProfit = calculateOscillationTakeProfit(entryPrice, box, direction);
        signal.setTakeProfit(takeProfit);

        signal.setReason("中波震荡交易");

        return signal;
    }

    /**
     * 计算网格止损
     */
    private BigDecimal calculateGridStopLoss(BigDecimal entryPrice, Box box, Direction direction, BigDecimal atr) {
        BigDecimal stopDistance = atr.multiply(BigDecimal.valueOf(0.3));

        if (direction == Direction.LONG) {
            return box.getLower().subtract(stopDistance);
        } else {
            return box.getUpper().add(stopDistance);
        }
    }

    /**
     * 计算网格止盈
     */
    private BigDecimal calculateGridTakeProfit(BigDecimal entryPrice, Box box, Direction direction) {
        int buffer = Constants.BTC.equals(symbol) ? 350 : 150;
        BigDecimal bufferAmount = BigDecimal.valueOf(buffer * 0.0001);

        if (direction == Direction.LONG) {
            return box.getUpper().subtract(bufferAmount);
        } else {
            return box.getLower().add(bufferAmount);
        }
    }

    /**
     * 计算震荡止损
     */
    private BigDecimal calculateOscillationStopLoss(BigDecimal entryPrice, Box box, Direction direction, BigDecimal atr) {
        BigDecimal stopDistance = atr.multiply(BigDecimal.valueOf(0.5));

        if (direction == Direction.LONG) {
            return box.getLower().subtract(stopDistance);
        } else {
            return box.getUpper().add(stopDistance);
        }
    }

    /**
     * 计算震荡止盈
     */
    private BigDecimal calculateOscillationTakeProfit(BigDecimal entryPrice, Box box, Direction direction) {
        int buffer = Constants.BTC.equals(symbol) ? 350 : 150;
        BigDecimal bufferAmount = BigDecimal.valueOf(buffer * 0.0001);

        if (direction == Direction.LONG) {
            return box.getUpper().subtract(bufferAmount);
        } else {
            return box.getLower().add(bufferAmount);
        }
    }
}
