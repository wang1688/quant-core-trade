package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 辅助策略1：区间震荡策略
 * 条件：价格在有效箱体内 + ATR低波动 + EMA30平坦 + 触及箱体边界反弹
 */
public class RangeOscillationStrategy implements TradeStrategy {

    private final boolean isBtc;

    public RangeOscillationStrategy() { this.isBtc = true; }
    public RangeOscillationStrategy(boolean isBtc) { this.isBtc = isBtc; }

    @Override
    public String name() { return "区间震荡策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 30) return 0;

        // 条件1：ATR低波动
        double atr = Indicators.atrPct(k15m, 14);
        double lowThreshold = isBtc ? 0.8 : 1.0;
        if (atr >= lowThreshold) return 0;

        // 条件2：EMA30平坦（非趋势）
        if (TrendAnalyzer.ema30Trending(k15m)) return 0;

        // 条件3：有效箱体存在
        BoxDetector.Box box = BoxDetector.detectAuto(k15m, isBtc);
        if (!box.valid) return 0;

        // 条件4：价格在箱体内
        double close = k15m[k15m.length - 1].getClose();
        if (!box.contains(close)) return 0;

        // 条件5：触及箱体边界后反弹
        double range = box.height();
        double lowerZone = box.low + range * 0.2;  // 下20%区域
        double upperZone = box.high - range * 0.2; // 上20%区域

        // 价格在下边界区域 → 做多
        if (close <= lowerZone) {
            // 确认前一根K线也在下边界区域（确认支撑）
            if (k15m.length >= 2) {
                double prevClose = k15m[k15m.length - 2].getClose();
                if (prevClose <= lowerZone + range * 0.05) return 1;
            }
            return 1;
        }

        // 价格在上边界区域 → 做空
        if (close >= upperZone) {
            if (k15m.length >= 2) {
                double prevClose = k15m[k15m.length - 2].getClose();
                if (prevClose >= upperZone - range * 0.05) return -1;
            }
            return -1;
        }

        return 0;
    }
}
