package com.ruoyi.web.okx.strategynew.indicator;

import com.ruoyi.web.okx.strategynew.Constants;
import com.ruoyi.web.okx.strategynew.model.Box;
import com.ruoyi.web.okx.strategynew.model.KLine;
import java.math.BigDecimal;
import java.util.List;

/**
 * 箱体/中枢识别器
 */
public class BoxDetector {

    /**
     * 识别箱体（基于N根K线）
     */
    public static Box detectBox(List<KLine> klines, int period, String symbol, String timeframe) {
        if (klines.size() < period) {
            return null;
        }

        int start = klines.size() - period;
        BigDecimal upper = klines.get(start).getHigh();
        BigDecimal lower = klines.get(start).getLow();

        for (int i = start + 1; i < klines.size(); i++) {
            BigDecimal high = klines.get(i).getHigh();
            BigDecimal low = klines.get(i).getLow();

            if (high.compareTo(upper) > 0) {
                upper = high;
            }
            if (low.compareTo(lower) < 0) {
                lower = low;
            }
        }

        Box box = new Box(upper, lower, period, timeframe);

        int validHeight = Constants.BTC.equals(symbol) ?
            Constants.BTC_VALID_BOX_HEIGHT :
            Constants.ETH_VALID_BOX_HEIGHT;

        BigDecimal heightInTicks = box.getHeight().multiply(BigDecimal.valueOf(10000));
        if (heightInTicks.compareTo(BigDecimal.valueOf(validHeight)) < 0) {
            return null;
        }

        return box;
    }

    /**
     * 判断箱体重叠度
     */
    public static boolean isOverlapping(Box box1, Box box2, double overlapRatio) {
        if (box1 == null || box2 == null) {
            return false;
        }

        BigDecimal overlapUpper = box1.getUpper().min(box2.getUpper());
        BigDecimal overlapLower = box1.getLower().max(box2.getLower());

        if (overlapUpper.compareTo(overlapLower) <= 0) {
            return false;
        }

        BigDecimal overlapHeight = overlapUpper.subtract(overlapLower);
        BigDecimal minHeight = box1.getHeight().min(box2.getHeight());

        double actualRatio = overlapHeight.divide(minHeight, 8, BigDecimal.ROUND_HALF_UP).doubleValue();
        return actualRatio >= overlapRatio;
    }

    /**
     * 判断是否有效突破
     */
    public static boolean isValidBreakout(BigDecimal currentPrice, Box box, String symbol,
                                          boolean isUpBreakout, BigDecimal volume, BigDecimal avgVolume,
                                          double volumeMultiplier) {
        int confirmTicks = Constants.BTC.equals(symbol) ?
            Constants.BTC_BREAKOUT_CONFIRM :
            Constants.ETH_BREAKOUT_CONFIRM;

        BigDecimal tickSize = BigDecimal.valueOf(0.0001);
        BigDecimal threshold = tickSize.multiply(BigDecimal.valueOf(confirmTicks));

        boolean priceBreakout;
        if (isUpBreakout) {
            priceBreakout = currentPrice.subtract(box.getUpper()).compareTo(threshold) >= 0;
        } else {
            priceBreakout = box.getLower().subtract(currentPrice).compareTo(threshold) >= 0;
        }

        boolean volumeConfirm = IndicatorCalculator.isVolumeExpanded(volume, avgVolume, volumeMultiplier);

        return priceBreakout && volumeConfirm;
    }

    /**
     * 判断回踩深度是否合理
     */
    public static boolean isValidPullback(BigDecimal pullbackDepth, BigDecimal boxHeight, double maxRatio) {
        BigDecimal maxDepth = boxHeight.multiply(BigDecimal.valueOf(maxRatio));
        return pullbackDepth.compareTo(maxDepth) <= 0;
    }

    /**
     * 扩展箱体（高波期）
     */
    public static Box expandBox(Box box, double expansionRatio) {
        if (box == null) {
            return null;
        }

        BigDecimal expansion = box.getHeight().multiply(BigDecimal.valueOf(expansionRatio / 2));
        BigDecimal newUpper = box.getUpper().add(expansion);
        BigDecimal newLower = box.getLower().subtract(expansion);

        return new Box(newUpper, newLower, box.getKlineCount(), box.getTimeframe());
    }

    /**
     * 升级箱体（9根→18根→36根）
     */
    public static Box upgradeBox(List<KLine> klines, Box currentBox, String symbol) {
        int newPeriod;
        if (currentBox.getKlineCount() == 9) {
            newPeriod = 18;
        } else if (currentBox.getKlineCount() == 18) {
            newPeriod = 36;
        } else {
            return currentBox;
        }

        return detectBox(klines, newPeriod, symbol, currentBox.getTimeframe());
    }

    /**
     * 判断价格是否在箱体中间位置（禁止开单区域）
     */
    public static boolean isInMiddleZone(BigDecimal price, Box box, double middleZoneRatio) {
        BigDecimal upperBound = box.getMiddle().add(
            box.getHeight().multiply(BigDecimal.valueOf(middleZoneRatio / 2))
        );
        BigDecimal lowerBound = box.getMiddle().subtract(
            box.getHeight().multiply(BigDecimal.valueOf(middleZoneRatio / 2))
        );

        return price.compareTo(lowerBound) >= 0 && price.compareTo(upperBound) <= 0;
    }
}
