package com.ruoyi.web.okx.strategynew.strategy;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.indicator.BoxDetector;
import com.ruoyi.web.okx.strategynew.indicator.IndicatorCalculator;
import com.ruoyi.web.okx.strategynew.indicator.SignalScorer;
import com.ruoyi.web.okx.strategynew.indicator.VolatilityAnalyzer;
import com.ruoyi.web.okx.strategynew.model.Box;
import com.ruoyi.web.okx.strategynew.model.KLine;
import com.ruoyi.web.okx.strategynew.model.Signal;

import java.math.BigDecimal;
import java.util.List;

/**
 * 中枢核心策略（趋势单边行情）
 */
public class CoreTrendStrategy {

    private String symbol;
    private VolatilityLevel volatilityLevel;

    public CoreTrendStrategy(String symbol) {
        this.symbol = symbol;
    }

    /**
     * 分析趋势并生成信号
     */
    public Signal analyze(List<KLine> klines4H, List<KLine> klines1H, List<KLine> klines15M, List<KLine> klines5M) {
        if (!validateData(klines4H, klines1H, klines15M, klines5M)) {
            return null;
        }

        List<BigDecimal> ema30_4H = IndicatorCalculator.calculateEMA(klines4H, 30);
        List<BigDecimal> ema30_1H = IndicatorCalculator.calculateEMA(klines1H, 30);
        List<BigDecimal> ema30_15M = IndicatorCalculator.calculateEMA(klines15M, 30);
        List<BigDecimal> ema6_15M = IndicatorCalculator.calculateEMA(klines15M, 6);
        List<BigDecimal> ema13_15M = IndicatorCalculator.calculateEMA(klines15M, 13);
        List<BigDecimal> ema6_5M = IndicatorCalculator.calculateEMA(klines5M, 6);
        List<BigDecimal> ema13_5M = IndicatorCalculator.calculateEMA(klines5M, 13);

        List<BigDecimal> atr15_15M = IndicatorCalculator.calculateATR(klines15M, 15);
        if (atr15_15M.isEmpty()) {
            return null;
        }

        BigDecimal currentPrice = klines15M.get(klines15M.size() - 1).getClose();
        BigDecimal atr15 = atr15_15M.get(atr15_15M.size() - 1);
        double volatility = IndicatorCalculator.calculateVolatility(atr15, currentPrice);
        this.volatilityLevel = VolatilityAnalyzer.getVolatilityLevel(symbol, volatility);

        double ema30Angle_4H = IndicatorCalculator.calculateEMA30Angle(ema30_4H, 5);
        double ema30Angle_1H = IndicatorCalculator.calculateEMA30Angle(ema30_1H, 5);

        Direction direction = determineTrendDirection(ema30Angle_4H, currentPrice, ema30_4H.get(ema30_4H.size() - 1));
        if (direction == null) {
            return null;
        }

        if (!checkTrendConditions(ema30Angle_4H, ema30Angle_1H, ema6_15M, ema13_15M, currentPrice, klines15M)) {
            return null;
        }

        Box box15M = BoxDetector.detectBox(klines15M, 9, symbol, "15M");
        if (box15M == null) {
            return null;
        }

        if (BoxDetector.isInMiddleZone(currentPrice, box15M, 0.4)) {
            return null;
        }

        boolean isBreakout = checkBreakout(currentPrice, box15M, direction, klines15M);
        if (!isBreakout) {
            return null;
        }

        boolean needsH1Confirmation = checkH1ConfirmationRequired(ema30Angle_4H, volatilityLevel);
        if (needsH1Confirmation && !checkH1Confirmation(klines1H, direction)) {
            return null;
        }

        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines15M, 20);
        BigDecimal currentVolume = klines15M.get(klines15M.size() - 1).getVolume();

        double dif = IndicatorCalculator.calculateDIF(
            ema6_15M.get(ema6_15M.size() - 1),
            ema13_15M.get(ema13_15M.size() - 1),
            currentPrice
        );

        if (Math.abs(dif) < Constants.DIF_MID) {
            return null;
        }

        boolean emaAlignmentComplete = checkEMAAlignment(ema6_15M, ema13_15M, ema30_15M, direction);

        double score = SignalScorer.calculateScore(
            ema30Angle_4H,
            dif,
            currentVolume,
            avgVolume,
            true,
            volatilityLevel,
            emaAlignmentComplete,
            direction
        );

        if (!SignalScorer.isValidSignal(score, volatilityLevel)) {
            return null;
        }

        Signal signal = new Signal(symbol, direction, "CoreTrend");
        signal.setScore(score);
        signal.setTimeframe("15M");

        BigDecimal entryPrice = calculateEntryPrice(currentPrice, box15M, direction, ema13_5M, ema6_5M, score);
        signal.setEntryPrice(entryPrice);

        BigDecimal stopLoss = calculateStopLoss(entryPrice, box15M, direction, klines15M);
        signal.setStopLoss(stopLoss);

        BigDecimal takeProfit = calculateTakeProfit(entryPrice, atr15, direction);
        signal.setTakeProfit(takeProfit);

        signal.setReason(String.format("4H角度:%.2f, DIF:%.4f, 评分:%.2f", ema30Angle_4H, dif, score));

        return signal;
    }

    /**
     * 验证数据完整性
     */
    private boolean validateData(List<KLine> klines4H, List<KLine> klines1H,
                                  List<KLine> klines15M, List<KLine> klines5M) {
        return klines4H != null && klines4H.size() >= 50 &&
               klines1H != null && klines1H.size() >= 50 &&
               klines15M != null && klines15M.size() >= 50 &&
               klines5M != null && klines5M.size() >= 50;
    }

    /**
     * 判定趋势方向
     */
    private Direction determineTrendDirection(double ema30Angle, BigDecimal currentPrice, BigDecimal ema30) {
        double validAngle = VolatilityAnalyzer.getValidEMA30Angle(volatilityLevel);

        if (Math.abs(ema30Angle) < validAngle) {
            return null;
        }

        if (ema30Angle > 0 && currentPrice.compareTo(ema30) > 0) {
            return Direction.LONG;
        } else if (ema30Angle < 0 && currentPrice.compareTo(ema30) < 0) {
            return Direction.SHORT;
        }

        return null;
    }

    /**
     * 检查趋势条件
     */
    private boolean checkTrendConditions(double ema30Angle_4H, double ema30Angle_1H,
                                         List<BigDecimal> ema6, List<BigDecimal> ema13,
                                         BigDecimal currentPrice, List<KLine> klines) {
        double dif = IndicatorCalculator.calculateDIF(
            ema6.get(ema6.size() - 1),
            ema13.get(ema13.size() - 1),
            currentPrice
        );

        if (Math.abs(dif) < Constants.DIF_MID) {
            return false;
        }

        double volumeDivergence = IndicatorCalculator.calculateVolumeDivergence(klines, 10);
        if (volumeDivergence < -0.3) {
            return false;
        }

        return true;
    }

    /**
     * 检查突破
     */
    private boolean checkBreakout(BigDecimal currentPrice, Box box, Direction direction, List<KLine> klines) {
        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines, 20);
        BigDecimal currentVolume = klines.get(klines.size() - 1).getVolume();
        double volumeMultiplier = VolatilityAnalyzer.getVolumeMultiplier(volatilityLevel);

        boolean isUpBreakout = direction == Direction.LONG;
        return BoxDetector.isValidBreakout(currentPrice, box, symbol, isUpBreakout,
                                           currentVolume, avgVolume, volumeMultiplier);
    }

    /**
     * 检查是否需要1H确认
     */
    private boolean checkH1ConfirmationRequired(double ema30Angle_4H, VolatilityLevel level) {
        if (Math.abs(ema30Angle_4H) >= Constants.EMA30_ANGLE_STRONG &&
            (level == VolatilityLevel.HIGH || level == VolatilityLevel.EXTREME)) {
            return false;
        }

        return true;
    }

    /**
     * 检查1H确认
     */
    private boolean checkH1Confirmation(List<KLine> klines1H, Direction direction) {
        if (klines1H.size() < 2) {
            return false;
        }

        KLine lastKline = klines1H.get(klines1H.size() - 1);
        BigDecimal avgVolume = IndicatorCalculator.calculateAvgVolume(klines1H, 20);

        boolean volumeConfirm = IndicatorCalculator.isVolumeExpanded(
            lastKline.getVolume(), avgVolume, Constants.VOLUME_NORMAL
        );

        boolean directionConfirm = (direction == Direction.LONG && lastKline.isBullish()) ||
                                   (direction == Direction.SHORT && lastKline.isBearish());

        return volumeConfirm && directionConfirm;
    }

    /**
     * 检查均线排列
     */
    private boolean checkEMAAlignment(List<BigDecimal> ema6, List<BigDecimal> ema13,
                                      List<BigDecimal> ema30, Direction direction) {
        BigDecimal ema6Val = ema6.get(ema6.size() - 1);
        BigDecimal ema13Val = ema13.get(ema13.size() - 1);
        BigDecimal ema30Val = ema30.get(ema30.size() - 1);

        if (direction == Direction.LONG) {
            return ema6Val.compareTo(ema13Val) > 0 && ema13Val.compareTo(ema30Val) > 0;
        } else {
            return ema6Val.compareTo(ema13Val) < 0 && ema13Val.compareTo(ema30Val) < 0;
        }
    }

    /**
     * 计算入场价格（三级挂单）
     */
    private BigDecimal calculateEntryPrice(BigDecimal currentPrice, Box box, Direction direction,
                                           List<BigDecimal> ema13_5M, List<BigDecimal> ema6_5M, double score) {
        BigDecimal ema13 = ema13_5M.get(ema13_5M.size() - 1);
        BigDecimal ema6 = ema6_5M.get(ema6_5M.size() - 1);

        if (score >= Constants.SIGNAL_SCORE_SUPER) {
            return ema6;
        } else if (score >= Constants.SIGNAL_SCORE_STRONG) {
            int buffer = Constants.BTC.equals(symbol) ? 500 : 200;
            BigDecimal bufferAmount = BigDecimal.valueOf(buffer * 0.0001);
            if (direction == Direction.LONG) {
                return box.getUpper().subtract(bufferAmount);
            } else {
                return box.getLower().add(bufferAmount);
            }
        } else {
            return ema13;
        }
    }

    /**
     * 计算止损
     */
    private BigDecimal calculateStopLoss(BigDecimal entryPrice, Box box, Direction direction, List<KLine> klines) {
        int feeBuffer = VolatilityAnalyzer.getFeeBuffer(symbol, volatilityLevel);
        BigDecimal bufferAmount = BigDecimal.valueOf(feeBuffer * 0.0001);

        KLine lastKline = klines.get(klines.size() - 1);

        if (direction == Direction.LONG) {
            BigDecimal stopBase = lastKline.getLow().min(box.getLower());
            return stopBase.subtract(bufferAmount);
        } else {
            BigDecimal stopBase = lastKline.getHigh().max(box.getUpper());
            return stopBase.add(bufferAmount);
        }
    }

    /**
     * 计算止盈
     */
    private BigDecimal calculateTakeProfit(BigDecimal entryPrice, BigDecimal atr, Direction direction) {
        BigDecimal target = atr.multiply(BigDecimal.valueOf(2));

        if (direction == Direction.LONG) {
            return entryPrice.add(target);
        } else {
            return entryPrice.subtract(target);
        }
    }
}
