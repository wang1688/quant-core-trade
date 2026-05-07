package com.ruoyi.web.okx.strategynew;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 核心技术指标计算类
 * 包含EMA、DIF、ATR、角度、量能等所有基础指标计算
 */
public class TechnicalIndicators {

    /**
     * 计算EMA指数移动平均线
     * @param klines K线数组
     * @param period 周期
     * @return EMA值
     */
    public static double calculateEMA(KLineVO[] klines, int period) {
        if (klines == null || klines.length < period) {
            return 0.0;
        }

        double multiplier = 2.0 / (period + 1);

        // 初始SMA
        double sum = 0.0;
        for (int i = klines.length - period; i < klines.length; i++) {
            sum += klines[i].getClose();
        }
        double ema = sum / period;

        // 计算EMA
        for (int i = klines.length - period + 1; i < klines.length; i++) {
            ema = (klines[i].getClose() - ema) * multiplier + ema;
        }

        return ema;
    }

    /**
     * 计算DIF乖离率
     * DIF = (EMA6 - EMA13) / 当前价格 * 100%
     * @param klines K线数组
     * @return DIF乖离率（小数形式，如0.0012表示0.12%）
     */
    public static double calculateDIF(KLineVO[] klines) {
        if (klines == null || klines.length < 13) {
            return 0.0;
        }

        double ema6 = calculateEMA(klines, 6);
        double ema13 = calculateEMA(klines, 13);
        double currentPrice = klines[klines.length - 1].getClose();

        if (currentPrice == 0) {
            return 0.0;
        }

        return (ema6 - ema13) / currentPrice;
    }

    /**
     * 计算ATR（Average True Range）
     * @param klines K线数组
     * @param period 周期（默认14）
     * @return ATR值
     */
    public static double calculateATR(KLineVO[] klines, int period) {
        if (klines == null || klines.length < period + 1) {
            return 0.0;
        }

        double[] trueRanges = new double[klines.length - 1];

        for (int i = 1; i < klines.length; i++) {
            double high = klines[i].getHigh();
            double low = klines[i].getLow();
            double prevClose = klines[i - 1].getClose();

            double tr1 = high - low;
            double tr2 = Math.abs(high - prevClose);
            double tr3 = Math.abs(low - prevClose);

            trueRanges[i - 1] = Math.max(tr1, Math.max(tr2, tr3));
        }

        // 计算ATR（使用EMA平滑）
        double atr = 0.0;
        for (int i = 0; i < period && i < trueRanges.length; i++) {
            atr += trueRanges[trueRanges.length - period + i];
        }
        atr /= period;

        return atr;
    }

    /**
     * 计算ATR百分比（波动率）
     * ATR% = ATR / 当前价格 * 100%
     * @param klines K线数组
     * @param period ATR周期
     * @return ATR百分比（小数形式）
     */
    public static double calculateATRPercent(KLineVO[] klines, int period) {
        if (klines == null || klines.length < period + 1) {
            return 0.0;
        }

        double atr = calculateATR(klines, period);
        double currentPrice = klines[klines.length - 1].getClose();

        if (currentPrice == 0) {
            return 0.0;
        }

        return atr / currentPrice;
    }

    /**
     * 计算EMA30角度
     * 角度 = atan((当前值 - N周期前值) / N周期前值) * (180 / PI)
     * N = 对应级别20根K线
     * @param klines K线数组
     * @return 角度（度）
     */
    public static double calculateEMA30Angle(KLineVO[] klines) {
        if (klines == null || klines.length < 50) {  // 至少需要30+20根
            return 0.0;
        }

        int lookback = 20;
        double currentEMA = calculateEMA(klines, 30);

        // 计算N周期前的EMA30
        KLineVO[] previousKlines = new KLineVO[klines.length - lookback];
        System.arraycopy(klines, 0, previousKlines, 0, previousKlines.length);
        double previousEMA = calculateEMA(previousKlines, 30);

        if (previousEMA == 0) {
            return 0.0;
        }

        double slope = (currentEMA - previousEMA) / previousEMA;
        double angleRadians = Math.atan(slope);
        double angleDegrees = angleRadians * (180.0 / Math.PI);

        return angleDegrees;
    }

    /**
     * 计算均量（成交量均值）
     * @param klines K线数组
     * @param period 周期（默认20）
     * @return 均量
     */
    public static double calculateAverageVolume(KLineVO[] klines, int period) {
        if (klines == null || klines.length < period) {
            return 0.0;
        }

        double sum = 0.0;
        for (int i = klines.length - period; i < klines.length; i++) {
            sum += klines[i].getVolume();
        }

        return sum / period;
    }

    /**
     * 计算量比
     * 量比 = 当前成交量 / 均量
     * @param klines K线数组
     * @param period 均量周期
     * @return 量比
     */
    public static double calculateVolumeRatio(KLineVO[] klines, int period) {
        if (klines == null || klines.length < period) {
            return 0.0;
        }

        double avgVolume = calculateAverageVolume(klines, period);
        if (avgVolume == 0) {
            return 0.0;
        }

        double currentVolume = klines[klines.length - 1].getVolume();
        return currentVolume / avgVolume;
    }

    /**
     * 计算量能背离系数
     * 量能背离系数 = 当前成交量/均量 - 价格涨跌幅/ATR15波动幅度
     * @param klines K线数组
     * @return 量能背离系数
     */
    public static double calculateVolumeDivergence(KLineVO[] klines) {
        if (klines == null || klines.length < 20) {
            return 0.0;
        }

        double volumeRatio = calculateVolumeRatio(klines, 20);
        double atr = calculateATR(klines, 15);

        if (atr == 0) {
            return 0.0;
        }

        double currentPrice = klines[klines.length - 1].getClose();
        double previousPrice = klines[klines.length - 2].getClose();
        double priceChange = Math.abs(currentPrice - previousPrice);
        double priceChangeRatio = priceChange / atr;

        return volumeRatio - priceChangeRatio;
    }

    /**
     * 计算平均K线实体大小
     * @param klines K线数组
     * @param period 周期
     * @return 平均实体大小
     */
    public static double calculateAverageBody(KLineVO[] klines, int period) {
        if (klines == null || klines.length < period) {
            return 0.0;
        }

        double sum = 0.0;
        for (int i = klines.length - period; i < klines.length; i++) {
            sum += Math.abs(klines[i].getClose() - klines[i].getOpen());
        }

        return sum / period;
    }

    /**
     * 判断是否为大实体K线
     * @param kline K线
     * @param avgBody 平均实体
     * @param multiplier 倍数（2倍或3倍）
     * @return 是否为大实体
     */
    public static boolean isBigBody(KLineVO kline, double avgBody, double multiplier) {
        double body = Math.abs(kline.getClose() - kline.getOpen());
        return body >= avgBody * multiplier;
    }

    /**
     * 判断是否为长影线K线
     * @param kline K线
     * @param multiplier 影线/实体倍数（1.5倍或2.5倍）
     * @return 是否为长影线
     */
    public static boolean isLongShadow(KLineVO kline, double multiplier) {
        double body = Math.abs(kline.getClose() - kline.getOpen());
        if (body == 0) {
            return false;
        }

        double upperShadow = kline.getHigh() - Math.max(kline.getOpen(), kline.getClose());
        double lowerShadow = Math.min(kline.getOpen(), kline.getClose()) - kline.getLow();
        double maxShadow = Math.max(upperShadow, lowerShadow);

        return maxShadow >= body * multiplier;
    }

    /**
     * 判断是否为阳K线
     * @param kline K线
     * @return 是否为阳K线
     */
    public static boolean isBullish(KLineVO kline) {
        return kline.getClose() > kline.getOpen();
    }

    /**
     * 判断是否为阴K线
     * @param kline K线
     * @return 是否为阴K线
     */
    public static boolean isBearish(KLineVO kline) {
        return kline.getClose() < kline.getOpen();
    }

    /**
     * 判断是否为十字星K线
     * @param kline K线
     * @param avgBody 平均实体
     * @return 是否为十字星
     */
    public static boolean isDoji(KLineVO kline, double avgBody) {
        double body = Math.abs(kline.getClose() - kline.getOpen());
        return body <= avgBody * 0.3;
    }

    /**
     * 判断EMA排列方向
     * @param klines K线数组
     * @return 1=多头排列，-1=空头排列，0=缠绕
     */
    public static int getEMAAlignment(KLineVO[] klines) {
        if (klines == null || klines.length < 30) {
            return 0;
        }

        double ema6 = calculateEMA(klines, 6);
        double ema13 = calculateEMA(klines, 13);
        double ema30 = calculateEMA(klines, 30);
        double dif = calculateDIF(klines);

        // 多头排列：EMA6 > EMA13 > EMA30 且 DIF > 0
        if (ema6 > ema13 && ema13 > ema30 && dif > 0) {
            return 1;
        }

        // 空头排列：EMA6 < EMA13 < EMA30 且 DIF < 0
        if (ema6 < ema13 && ema13 < ema30 && dif < 0) {
            return -1;
        }

        return 0;
    }

    /**
     * 判断价格是否在EMA30上方/下方
     * @param klines K线数组
     * @return 1=上方，-1=下方，0=在EMA30上
     */
    public static int getPricePositionToEMA30(KLineVO[] klines) {
        if (klines == null || klines.length < 30) {
            return 0;
        }

        double ema30 = calculateEMA(klines, 30);
        double currentPrice = klines[klines.length - 1].getClose();

        if (currentPrice > ema30) {
            return 1;
        } else if (currentPrice < ema30) {
            return -1;
        }

        return 0;
    }

    /**
     * 计算前高/前低
     * @param klines K线数组
     * @param lookback 回溯周期
     * @param isHigh true=前高，false=前低
     * @return 前高/前低价格
     */
    public static double getPreviousHighLow(KLineVO[] klines, int lookback, boolean isHigh) {
        if (klines == null || klines.length < lookback) {
            return 0.0;
        }

        double extreme = isHigh ? Double.MIN_VALUE : Double.MAX_VALUE;

        for (int i = klines.length - lookback; i < klines.length; i++) {
            if (isHigh) {
                extreme = Math.max(extreme, klines[i].getHigh());
            } else {
                extreme = Math.min(extreme, klines[i].getLow());
            }
        }

        return extreme;
    }

    /**
     * 判断是否出现顶分型
     * 3根连续K线，中间K线实体最高点为最高，且实体≥0.5×avgBody；上影线≤实体1.5倍
     * @param klines K线数组（至少3根）
     * @param avgBody 平均实体
     * @return 是否为顶分型
     */
    public static boolean isTopFractal(KLineVO[] klines, double avgBody) {
        if (klines == null || klines.length < 3) {
            return false;
        }

        int len = klines.length;
        KLineVO k1 = klines[len - 3];
        KLineVO k2 = klines[len - 2];  // 中间K线
        KLineVO k3 = klines[len - 1];

        double body2 = Math.abs(k2.getClose() - k2.getOpen());
        double high2 = Math.max(k2.getOpen(), k2.getClose());
        double high1 = Math.max(k1.getOpen(), k1.getClose());
        double high3 = Math.max(k3.getOpen(), k3.getClose());
        double upperShadow2 = k2.getHigh() - high2;

        // 中间K线实体最高点为最高
        boolean isHighest = high2 > high1 && high2 > high3;

        // 实体≥0.5×avgBody
        boolean bodyValid = body2 >= avgBody * 0.5;

        // 上影线≤实体1.5倍
        boolean shadowValid = upperShadow2 <= body2 * 1.5;

        return isHighest && bodyValid && shadowValid;
    }

    /**
     * 判断是否出现底分型
     * 3根连续K线，中间K线实体最低点为最低，且实体≥0.5×avgBody；下影线≤实体1.5倍
     * @param klines K线数组（至少3根）
     * @param avgBody 平均实体
     * @return 是否为底分型
     */
    public static boolean isBottomFractal(KLineVO[] klines, double avgBody) {
        if (klines == null || klines.length < 3) {
            return false;
        }

        int len = klines.length;
        KLineVO k1 = klines[len - 3];
        KLineVO k2 = klines[len - 2];  // 中间K线
        KLineVO k3 = klines[len - 1];

        double body2 = Math.abs(k2.getClose() - k2.getOpen());
        double low2 = Math.min(k2.getOpen(), k2.getClose());
        double low1 = Math.min(k1.getOpen(), k1.getClose());
        double low3 = Math.min(k3.getOpen(), k3.getClose());
        double lowerShadow2 = low2 - k2.getLow();

        // 中间K线实体最低点为最低
        boolean isLowest = low2 < low1 && low2 < low3;

        // 实体≥0.5×avgBody
        boolean bodyValid = body2 >= avgBody * 0.5;

        // 下影线≤实体1.5倍
        boolean shadowValid = lowerShadow2 <= body2 * 1.5;

        return isLowest && bodyValid && shadowValid;
    }
}
