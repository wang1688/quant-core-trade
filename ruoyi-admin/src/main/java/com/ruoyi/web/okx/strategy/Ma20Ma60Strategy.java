package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「MA20/MA60双均线趋势策略」
 * 短期20周期均线、长期60周期均线，金叉做多、死叉做空
 */
public class Ma20Ma60Strategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 60) return 0;
        int len = k15m.length;
        double[] close = new double[len];
        for (int i = 0; i < len; i++) {
            close[i] = k15m[i].getClose();
        }

        double ma20 = sma(close, len - 1, 20);
        double ma20Prev = sma(close, len - 2, 20);
        double ma60 = sma(close, len - 1, 60);
        double ma60Prev = sma(close, len - 2, 60);

        if (ma20 > ma60 && ma20Prev <= ma60Prev) return 1;
        if (ma20 < ma60 && ma20Prev >= ma60Prev) return -1;
        return 0;
    }

    private double sma(double[] arr, int idx, int period) {
        int start = Math.max(0, idx - period + 1);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    @Override
    public String name() {
        return "MA20/MA60双均线趋势策略";
    }
}