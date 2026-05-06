package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 辅助策略4：假突破策略
 * 条件：价格短暂突破箱体后快速回落 + 成交量不配合 + EMA30平坦
 * 逻辑：假突破方向的反向入场
 */
public class FakeBreakoutStrategy implements TradeStrategy {

    private final boolean isBtc;

    public FakeBreakoutStrategy() { this.isBtc = true; }
    public FakeBreakoutStrategy(boolean isBtc) { this.isBtc = isBtc; }

    @Override
    public String name() { return "假突破策略(" + (isBtc ? "BTC" : "ETH") + ")"; }

    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 30) return 0;

        // 条件1：EMA30平坦（震荡市，假突破概率高）
        if (TrendAnalyzer.ema30Trending(k15m)) return 0;

        // 条件2：有效箱体
        BoxDetector.Box box = BoxDetector.detectAuto(k15m, isBtc);
        if (!box.valid) return 0;

        int len = k15m.length;
        if (len < 3) return 0;

        double close0 = k15m[len - 1].getClose(); // 当前K线收盘
        double high0  = k15m[len - 1].getHigh();
        double low0   = k15m[len - 1].getLow();
        double close1 = k15m[len - 2].getClose(); // 前一根收盘
        double close2 = k15m[len - 3].getClose(); // 前两根收盘

        // 条件3：成交量不配合突破（量比 < 1.2）
        double vr = Indicators.volumeRatio(k15m, 20);
        if (vr >= 1.5) return 0; // 放量突破，不是假突破

        // 假突破向上：前一根收盘突破箱体上轨，当前K线收盘回落至箱体内
        if (close1 > box.high && box.contains(close0)) {
            return -1; // 假突破向上 → 做空
        }

        // 假突破向下：前一根收盘跌破箱体下轨，当前K线收盘回升至箱体内
        if (close1 < box.low && box.contains(close0)) {
            return 1; // 假突破向下 → 做多
        }

        // 针刺假突破：当前K线高点突破上轨但收盘回落至箱体内（上影线）
        if (high0 > box.high && box.contains(close0) && close0 < close1) {
            return -1;
        }

        // 针刺假突破：当前K线低点跌破下轨但收盘回升至箱体内（下影线）
        if (low0 < box.low && box.contains(close0) && close0 > close1) {
            return 1;
        }

        return 0;
    }
}
