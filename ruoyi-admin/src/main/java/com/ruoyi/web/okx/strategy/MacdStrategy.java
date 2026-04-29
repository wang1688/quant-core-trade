package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「MACD金叉死叉策略」
 * 默认12、26、9标准参数，MACD线与信号线金叉做多、死叉做空
 */
public class MacdStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 50) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) {
            close[i] = k15m[i].getClose();
        }

        double[] ema12 = ema(close, 12);
        double[] ema26 = ema(close, 26);
        double[] dif = sub(ema12, ema26);
        double[] dea = ema(dif, 9);

        int last = close.length - 1;
        boolean gold = dif[last-1] < dea[last-1] && dif[last] > dea[last];
        boolean dead = dif[last-1] > dea[last-1] && dif[last] < dea[last];

        return gold ? 1 : dead ? -1 : 0;
    }

    private double[] ema(double[] src, int period) {
        double[] res = new double[src.length];
        res[0] = src[0];
        double alpha = 2.0 / (period + 1);
        for (int i = 1; i < src.length; i++) {
            res[i] = alpha * src[i] + (1 - alpha) * res[i-1];
        }
        return res;
    }

    private double[] sub(double[] a, double[] b) {
        double[] res = new double[a.length];
        for (int i = 0; i < a.length; i++) res[i] = a[i] - b[i];
        return res;
    }

    @Override
    public String name() {
        return "MACD金叉死叉策略";
    }
}