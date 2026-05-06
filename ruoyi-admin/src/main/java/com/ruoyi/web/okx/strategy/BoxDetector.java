package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 箱体（中枢）检测器
 * BTC: 1 tick = 0.1 USDT，箱体高度 ≥ 7000 ticks = 700 USDT
 * ETH: 1 tick = 0.01 USDT，箱体高度 ≥ 2200 ticks = 22 USDT
 */
public class BoxDetector {

    public static final double BTC_BOX_MIN_USDT = 700.0;  // 7000 ticks × 0.1
    public static final double ETH_BOX_MIN_USDT = 22.0;   // 2200 ticks × 0.01

    public static class Box {
        public final double high;
        public final double low;
        public final boolean valid;

        public Box(double high, double low, boolean valid) {
            this.high = high;
            this.low = low;
            this.valid = valid;
        }

        public double mid() { return (high + low) / 2; }
        public double height() { return high - low; }

        /** 价格是否在箱体内 */
        public boolean contains(double price) {
            return price >= low && price <= high;
        }

        /** 价格是否在箱体上方（突破） */
        public boolean aboveBox(double price) { return price > high; }

        /** 价格是否在箱体下方（跌破） */
        public boolean belowBox(double price) { return price < low; }
    }

    /**
     * 从K线数组中检测最近一个有效箱体
     * 取近lookback根K线的最高/最低价构成箱体
     * @param klines   K线数组
     * @param lookback 回看根数（建议20~40）
     * @param minHeight 箱体最小高度（USDT）
     */
    public static Box detect(KLineVO[] klines, int lookback, double minHeight) {
        if (klines == null || klines.length < lookback) {
            return new Box(0, 0, false);
        }
        int len = klines.length;
        int start = len - lookback;

        double high = Double.MIN_VALUE;
        double low = Double.MAX_VALUE;
        for (int i = start; i < len; i++) {
            high = Math.max(high, klines[i].getHigh());
            low = Math.min(low, klines[i].getLow());
        }

        boolean valid = (high - low) >= minHeight;
        return new Box(high, low, valid);
    }

    /**
     * 自动判断品种并检测箱体
     * @param klines  K线数组
     * @param isBtc   true=BTC，false=ETH
     */
    public static Box detectAuto(KLineVO[] klines, boolean isBtc) {
        double minHeight = isBtc ? BTC_BOX_MIN_USDT : ETH_BOX_MIN_USDT;
        return detect(klines, 30, minHeight);
    }

    /**
     * 判断当前价格相对箱体的位置
     * 返回：1=箱体上方，-1=箱体下方，0=箱体内
     */
    public static int pricePosition(Box box, double price) {
        if (!box.valid) return 0;
        if (box.aboveBox(price)) return 1;
        if (box.belowBox(price)) return -1;
        return 0;
    }

    /**
     * 检测价格是否刚刚突破箱体（当前K线收盘在外，前一根在内）
     */
    public static int breakout(KLineVO[] klines, Box box) {
        if (!box.valid || klines.length < 2) return 0;
        int len = klines.length;
        double prev = klines[len - 2].getClose();
        double curr = klines[len - 1].getClose();

        if (box.contains(prev) && curr > box.high) return 1;   // 向上突破
        if (box.contains(prev) && curr < box.low) return -1;   // 向下突破
        return 0;
    }
}
