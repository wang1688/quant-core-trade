package com.ruoyi.web.okx.strategynew.strategy;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.Timeframe;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.indicator.*;
import com.ruoyi.web.okx.strategynew.model.*;
import com.ruoyi.web.okx.strategynew.pattern.PatternRecognizer;
import java.math.BigDecimal;

import java.util.List;

/**
 * 裸K交易体系（独立核算）
 */
public class NakedKStrategy {

    private String symbol;
    private VolatilityLevel volatilityLevel;

    public NakedKStrategy(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 分析裸K形态并生成信号
     */
    public Signal analyze(List<KLine> klines4H, List<KLine> klines1H,
                          List<KLine> klines15M, List<KLine> klines5M, Timeframe timeframe) {
        List<KLine> targetKlines = selectKlines(klines1H, klines15M, klines5M, timeframe);
        if (targetKlines == null || targetKlines.size() < 50) {
            return null;
        }

        List<BigDecimal> atr15 = IndicatorCalculator.calculateATR(targetKlines, 15);
        if (atr15.isEmpty()) {
            return null;
        }

        BigDecimal currentPrice = targetKlines.get(targetKlines.size() - 1).getClose();
        BigDecimal atrValue = atr15.get(atr15.size() - 1);
        double volatility = IndicatorCalculator.calculateVolatility(atrValue, currentPrice);
        this.volatilityLevel = VolatilityAnalyzer.getVolatilityLevel(symbol, volatility);

        List<BigDecimal> ema30_4H = IndicatorCalculator.calculateEMA(klines4H, 30);
        double ema30Angle_4H = IndicatorCalculator.calculateEMA30Angle(ema30_4H, 5);

        boolean canTrade = checkTradingPermission(ema30Angle_4H, currentPrice, ema30_4H, timeframe);
        if (!canTrade) {
            return null;
        }

        BigDecimal avgBody = IndicatorCalculator.calculateAvgBodySize(targetKlines, 20);

        Direction direction = null;
        boolean isStrongPattern = false;

        if (PatternRecognizer.isStrongReversalPattern(targetKlines, Direction.LONG, avgBody)) {
            direction = Direction.LONG;
            isStrongPattern = true;
        } else if (PatternRecognizer.isStrongReversalPattern(targetKlines, Direction.SHORT, avgBody)) {
            direction = Direction.SHORT;
            isStrongPattern = true;
        } else if (timeframe != Timeframe.M5) {
            if (PatternRecognizer.isSecondaryReversalPattern(targetKlines, Direction.LONG, avgBody)) {
                direction = Direction.LONG;
                isStrongPattern = false;
            } else if (PatternRecognizer.isSecondaryReversalPattern(targetKlines, Direction.SHORT, avgBody)) {
                direction = Direction.SHORT;
                isStrongPattern = false;
            }
        }

        if (direction == null) {
            return null;
        }

        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(targetKlines, 20);
        KLine lastKline = targetKlines.get(targetKlines.size() - 1);

        boolean volumeConfirm = IndicatorCalculator.isVolumeExpanded(
            lastKline.getVolume(), avgVolume, 1.2
        );

        if (!volumeConfirm) {
            return null;
        }

        Signal signal = new Signal(symbol, direction, "NakedK");
        signal.setTimeframe(timeframe.getCode());
        signal.setScore(isStrongPattern ? 6.0 : 5.0);

        BigDecimal entryPrice = calculateNakedKEntry(targetKlines, direction, isStrongPattern);
        signal.setEntryPrice(entryPrice);

        BigDecimal stopLoss = calculateNakedKStopLoss(targetKlines, direction, timeframe);
        signal.setStopLoss(stopLoss);

        BigDecimal takeProfit = calculateNakedKTakeProfit(entryPrice, atrValue, direction);
        signal.setTakeProfit(takeProfit);

        signal.setReason(String.format("裸K%s形态-%s", isStrongPattern ? "强" : "次强", timeframe.getCode()));

        return signal;
    }

    /**
     * 选择对应级别K线
     */
    private List<KLine> selectKlines(List<KLine> klines1H, List<KLine> klines15M,
                                     List<KLine> klines5M, Timeframe timeframe) {
        switch (timeframe) {
            case H1:
                return klines1H;
            case M15:
                return klines15M;
            case M5:
                return klines5M;
            default:
                return null;
        }
    }

    /**
     * 检查交易权限（唯一入场级别限制）
     */
    private boolean checkTradingPermission(double ema30Angle_4H, BigDecimal currentPrice,
                                           List<BigDecimal> ema30_4H, Timeframe timeframe) {
        BigDecimal ema30Val = ema30_4H.get(ema30_4H.size() - 1);
        boolean isCounterTrend = (ema30Angle_4H > 0 && currentPrice.compareTo(ema30Val) < 0) ||
                                 (ema30Angle_4H < 0 && currentPrice.compareTo(ema30Val) > 0);

        boolean isWeakTrend = Math.abs(ema30Angle_4H) < Constants.EMA30_ANGLE_BUFFER;

        if (isCounterTrend || isWeakTrend) {
            return timeframe == Timeframe.H1;
        }

        return true;
    }

    /**
     * 计算裸K入场价
     */
    private BigDecimal calculateNakedKEntry(List<KLine> klines, Direction direction, boolean isStrongPattern) {
        int size = klines.size();
        KLine reversalKline = null;

        for (int i = size - 3; i < size; i++) {
            KLine kline = klines.get(i);
            if (direction == Direction.LONG && kline.getLowerShadow().compareTo(kline.getBodySize()) > 0) {
                reversalKline = kline;
                break;
            } else if (direction == Direction.SHORT && kline.getUpperShadow().compareTo(kline.getBodySize()) > 0) {
                reversalKline = kline;
                break;
            }
        }

        if (reversalKline == null) {
            return klines.get(size - 1).getClose();
        }

        int aggressiveBuffer = 0;
        int conservativeBuffer = 0;

        if (isStrongPattern) {
            if (Constants.BTC.equals(symbol)) {
                aggressiveBuffer = 300;
                conservativeBuffer = 150;
            } else {
                aggressiveBuffer = 450;
                conservativeBuffer = 225;
            }
        }

        if (direction == Direction.LONG) {
            return reversalKline.getLow();
        } else {
            return reversalKline.getHigh();
        }
    }

    /**
     * 计算裸K止损
     */
    private BigDecimal calculateNakedKStopLoss(List<KLine> klines, Direction direction, Timeframe timeframe) {
        int size = klines.size();
        BigDecimal extremePrice;

        if (direction == Direction.LONG) {
            extremePrice = klines.get(size - 1).getLow();
            for (int i = size - 5; i < size; i++) {
                if (klines.get(i).getLow().compareTo(extremePrice) < 0) {
                    extremePrice = klines.get(i).getLow();
                }
            }

            int buffer;
            if (timeframe == Timeframe.H1) {
                buffer = Constants.BTC.equals(symbol) ? 3500 : 2200;
            } else if (timeframe == Timeframe.M15) {
                buffer = Constants.BTC.equals(symbol) ? 4200 : 2200;
            } else {
                buffer = Constants.BTC.equals(symbol) ? 2800 : 1500;
            }

            return extremePrice.subtract(BigDecimal.valueOf(buffer * 0.0001));
        } else {
            extremePrice = klines.get(size - 1).getHigh();
            for (int i = size - 5; i < size; i++) {
                if (klines.get(i).getHigh().compareTo(extremePrice) > 0) {
                    extremePrice = klines.get(i).getHigh();
                }
            }

            int buffer;
            if (timeframe == Timeframe.H1) {
                buffer = Constants.BTC.equals(symbol) ? 3500 : 2200;
            } else if (timeframe == Timeframe.M15) {
                buffer = Constants.BTC.equals(symbol) ? 4200 : 2200;
            } else {
                buffer = Constants.BTC.equals(symbol) ? 2800 : 1500;
            }

            return extremePrice.add(BigDecimal.valueOf(buffer * 0.0001));
        }
    }

    /**
     * 计算裸K止盈
     */
    private BigDecimal calculateNakedKTakeProfit(BigDecimal entryPrice, BigDecimal atr, Direction direction) {
        BigDecimal target = atr.multiply(BigDecimal.valueOf(2));

        if (direction == Direction.LONG) {
            return entryPrice.add(target);
        } else {
            return entryPrice.subtract(target);
        }
    }

    /**
     * 计算裸K仓位系数
     */
    public double calculateNakedKPositionMultiplier(boolean isStrongPattern, Timeframe timeframe) {
        double patternMultiplier;
        if (isStrongPattern) {
            patternMultiplier = 1.4;
        } else {
            patternMultiplier = 0.8;
        }

        double levelMultiplier;
        switch (timeframe) {
            case H1:
                levelMultiplier = 1.0;
                break;
            case M15:
                levelMultiplier = 0.8;
                break;
            case M5:
                levelMultiplier = 0.6;
                break;
            default:
                levelMultiplier = 1.0;
        }

        return patternMultiplier * levelMultiplier;
    }
}
