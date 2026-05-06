package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 技术指标计算工具类
 * EMA / ATR / DIF散度率 / 成交量比率
 */
public class Indicators {

    // ==================== EMA ====================

    /**
     * 计算最后一根K线的EMA值
     * @param klines K线数组（时间升序，最后一根为最新）
     * @param period EMA周期
     */
    public static double ema(KLineVO[] klines, int period) {
        if (klines == null || klines.length == 0) return 0;
        double k = 2.0 / (period + 1);
        double ema = klines[0].getClose();
        for (int i = 1; i < klines.length; i++) {
            ema = klines[i].getClose() * k + ema * (1 - k);
        }
        return ema;
    }

    /**
     * 计算全序列EMA数组
     */
    public static double[] emaArray(KLineVO[] klines, int period) {
        double[] result = new double[klines.length];
        if (klines.length == 0) return result;
        double k = 2.0 / (period + 1);
        result[0] = klines[0].getClose();
        for (int i = 1; i < klines.length; i++) {
            result[i] = klines[i].getClose() * k + result[i - 1] * (1 - k);
        }
        return result;
    }

    // ==================== ATR ====================

    /**
     * 计算最后一根K线的ATR(14)值（百分比形式）
     */
    public static double atrPct(KLineVO[] klines, int period) {
        if (klines == null || klines.length < 2) return 0;
        int len = klines.length;
        // 计算TR序列
        double[] tr = new double[len];
        tr[0] = klines[0].getHigh() - klines[0].getLow();
        for (int i = 1; i < len; i++) {
            double hl = klines[i].getHigh() - klines[i].getLow();
            double hc = Math.abs(klines[i].getHigh() - klines[i - 1].getClose());
            double lc = Math.abs(klines[i].getLow() - klines[i - 1].getClose());
            tr[i] = Math.max(hl, Math.max(hc, lc));
        }
        // Wilder平滑ATR
        int start = Math.max(0, len - period * 3);
        double atr = 0;
        int count = 0;
        for (int i = start; i < len; i++) {
            if (count == 0) {
                atr = tr[i];
            } else {
                atr = (atr * (period - 1) + tr[i]) / period;
            }
            count++;
        }
        double price = klines[len - 1].getClose();
        return price > 0 ? atr / price * 100 : 0;
    }

    // ==================== DIF散度率 ====================

    /**
     * DIF散度率 = (EMA6 - EMA13) / close × 100%
     * 正值=多头趋势，负值=空头趋势
     * 绝对值越大趋势越强
     */
    public static double difDivergence(KLineVO[] klines) {
        if (klines == null || klines.length < 13) return 0;
        double e6 = ema(klines, 6);
        double e13 = ema(klines, 13);
        double close = klines[klines.length - 1].getClose();
        return close > 0 ? (e6 - e13) / close * 100 : 0;
    }

    // ==================== 成交量 ====================

    /**
     * 当前K线成交量 / 近N根均量
     */
    public static double volumeRatio(KLineVO[] klines, int period) {
        if (klines == null || klines.length < 2) return 1;
        int len = klines.length;
        double current = klines[len - 1].getVolume();
        int start = Math.max(0, len - 1 - period);
        double sum = 0;
        int cnt = 0;
        for (int i = start; i < len - 1; i++) {
            sum += klines[i].getVolume();
            cnt++;
        }
        if (cnt == 0 || sum == 0) return 1;
        return current / (sum / cnt);
    }

    /**
     * 近N根K线成交量均值
     */
    public static double avgVolume(KLineVO[] klines, int period) {
        if (klines == null || klines.length == 0) return 0;
        int len = klines.length;
        int start = Math.max(0, len - period);
        double sum = 0;
        for (int i = start; i < len; i++) sum += klines[i].getVolume();
        return sum / (len - start);
    }

    // ==================== EMA斜率/方向 ====================

    /**
     * EMA斜率（当前EMA - N根前EMA）/ N根前EMA × 100
     * 正=上升，负=下降
     */
    public static double emaSlope(KLineVO[] klines, int emaPeriod, int lookback) {
        if (klines == null || klines.length < lookback + 1) return 0;
        double[] arr = emaArray(klines, emaPeriod);
        int len = arr.length;
        double prev = arr[len - 1 - lookback];
        double curr = arr[len - 1];
        return prev > 0 ? (curr - prev) / prev * 100 : 0;
    }

    /**
     * 判断EMA30角度是否超过阈值（用于趋势判断）
     * 用近lookback根K线的EMA30斜率近似角度
     */
    public static boolean ema30AboveAngle(KLineVO[] klines, double angleThreshold) {
        // 用斜率百分比近似角度，阈值对应约12°~25°
        // 15m K线：0.05%/根 ≈ 12°，0.12%/根 ≈ 25°
        double slope = emaSlope(klines, 30, 3);
        return Math.abs(slope) >= angleThreshold;
    }

    // ==================== 高低点 ====================

    public static double highest(KLineVO[] klines, int period) {
        int len = klines.length;
        int start = Math.max(0, len - period);
        double h = Double.MIN_VALUE;
        for (int i = start; i < len; i++) h = Math.max(h, klines[i].getHigh());
        return h;
    }

    public static double lowest(KLineVO[] klines, int period) {
        int len = klines.length;
        int start = Math.max(0, len - period);
        double l = Double.MAX_VALUE;
        for (int i = start; i < len; i++) l = Math.min(l, klines[i].getLow());
        return l;
    }
}
