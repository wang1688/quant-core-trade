package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「OBV量能+均线策略」
 * OBV持续放量+价格站上均线做多；OBV持续缩量+价格跌破均线做空
 */
public class ObvMaComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma20 = sma(close, last, 20);
        double obvNow = close[last] > close[last-1] ? 1 : -1;

        if (close[last] > ma20 && obvNow > 0) return 1;
        if (close[last] < ma20 && obvNow < 0) return -1;
        return 0;
    }

    private double sma(double[] arr, int idx, int p) {
        int start = Math.max(0, idx - p + 1);
        double sum = 0;
        for (int i = start; i <= idx; i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    @Override
    public String name() {
        return "OBV量能+均线策略";
    }
}