package com.ruoyi.web.okx.strategynew.indicator;

import com.ruoyi.web.okx.strategynew.model.KLine;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

/**
 * 技术指标计算器
 */
public class IndicatorCalculator {

    /**
     * 计算EMA（指数移动平均）
     */
    public static List<BigDecimal> calculateEMA(List<KLine> klines, int period) {
        List<BigDecimal> emaValues = new ArrayList<>();
        if (klines.isEmpty()) {
            return emaValues;
        }

        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1));
        BigDecimal ema = klines.get(0).getClose();
        emaValues.add(ema);

        for (int i = 1; i < klines.size(); i++) {
            BigDecimal close = klines.get(i).getClose();
            ema = close.subtract(ema).multiply(multiplier).add(ema);
            emaValues.add(ema);
        }

        return emaValues;
    }

    /**
     * 计算ATR（平均真实波幅）
     */
    public static List<BigDecimal> calculateATR(List<KLine> klines, int period) {
        List<BigDecimal> atrValues = new ArrayList<>();
        if (klines.size() < 2) {
            return atrValues;
        }

        List<BigDecimal> trValues = new ArrayList<>();
        for (int i = 1; i < klines.size(); i++) {
            BigDecimal high = klines.get(i).getHigh();
            BigDecimal low = klines.get(i).getLow();
            BigDecimal prevClose = klines.get(i - 1).getClose();

            BigDecimal tr1 = high.subtract(low);
            BigDecimal tr2 = high.subtract(prevClose).abs();
            BigDecimal tr3 = low.subtract(prevClose).abs();

            BigDecimal tr = tr1.max(tr2).max(tr3);
            trValues.add(tr);
        }

        if (trValues.size() < period) {
            return atrValues;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            sum = sum.add(trValues.get(i));
        }
        BigDecimal atr = sum.divide(BigDecimal.valueOf(period), 8, BigDecimal.ROUND_HALF_UP);
        atrValues.add(atr);

        BigDecimal multiplier = BigDecimal.valueOf((period - 1.0) / period);
        BigDecimal invMultiplier = BigDecimal.ONE.divide(BigDecimal.valueOf(period), 8, BigDecimal.ROUND_HALF_UP);

        for (int i = period; i < trValues.size(); i++) {
            atr = atr.multiply(multiplier).add(trValues.get(i).multiply(invMultiplier));
            atrValues.add(atr);
        }

        return atrValues;
    }

    /**
     * 计算EMA30角度（度）
     */
    public static double calculateEMA30Angle(List<BigDecimal> ema30Values, int lookback) {
        if (ema30Values.size() < lookback + 1) {
            return 0.0;
        }

        int size = ema30Values.size();
        BigDecimal current = ema30Values.get(size - 1);
        BigDecimal previous = ema30Values.get(size - 1 - lookback);

        BigDecimal diff = current.subtract(previous);
        double slope = diff.divide(previous, 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        double angle = Math.toDegrees(Math.atan(slope * 100));

        return angle;
    }

    /**
     * 计算DIF乖离率（EMA6 - EMA13）
     */
    public static double calculateDIF(BigDecimal ema6, BigDecimal ema13, BigDecimal currentPrice) {
        BigDecimal diff = ema6.subtract(ema13);
        return diff.divide(currentPrice, 8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 计算平均K线实体大小
     */
    public static BigDecimal calculateAvgBodySize(List<KLine> klines, int period) {
        if (klines.size() < period) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        int start = klines.size() - period;
        for (int i = start; i < klines.size(); i++) {
            sum = sum.add(klines.get(i).getBodySize());
        }

        return sum.divide(BigDecimal.valueOf(period), 8, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算平均成交量
     */
    public static BigDecimal calculateAvgVolume(List<KLine> klines, int period) {
        if (klines.size() < period) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        int start = klines.size() - period;
        for (int i = start; i < klines.size(); i++) {
            sum = sum.add(klines.get(i).getVolume());
        }

        return sum.divide(BigDecimal.valueOf(period), 8, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 计算波动率（ATR百分比）
     */
    public static double calculateVolatility(BigDecimal atr, BigDecimal currentPrice) {
        return atr.divide(currentPrice, 8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 判断是否放量
     */
    public static boolean isVolumeExpanded(BigDecimal currentVolume, BigDecimal avgVolume, double multiplier) {
        BigDecimal threshold = avgVolume.multiply(BigDecimal.valueOf(multiplier));
        return currentVolume.compareTo(threshold) >= 0;
    }

    /**
     * 判断是否缩量
     */
    public static boolean isVolumeShrink(BigDecimal currentVolume, BigDecimal avgVolume, double multiplier) {
        BigDecimal threshold = avgVolume.multiply(BigDecimal.valueOf(multiplier));
        return currentVolume.compareTo(threshold) <= 0;
    }

    /**
     * 计算量能背离系数
     */
    public static double calculateVolumeDivergence(List<KLine> klines, int period) {
        if (klines.size() < period * 2) {
            return 0.0;
        }

        int size = klines.size();
        BigDecimal recentAvgVolume = BigDecimal.ZERO;
        BigDecimal previousAvgVolume = BigDecimal.ZERO;

        for (int i = size - period; i < size; i++) {
            recentAvgVolume = recentAvgVolume.add(klines.get(i).getVolume());
        }
        recentAvgVolume = recentAvgVolume.divide(BigDecimal.valueOf(period), 8, BigDecimal.ROUND_HALF_UP);

        for (int i = size - period * 2; i < size - period; i++) {
            previousAvgVolume = previousAvgVolume.add(klines.get(i).getVolume());
        }
        previousAvgVolume = previousAvgVolume.divide(BigDecimal.valueOf(period), 8, BigDecimal.ROUND_HALF_UP);

        if (previousAvgVolume.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        BigDecimal diff = recentAvgVolume.subtract(previousAvgVolume);
        return diff.divide(previousAvgVolume, 8, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
