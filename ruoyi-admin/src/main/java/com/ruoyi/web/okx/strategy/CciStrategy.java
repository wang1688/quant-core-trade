package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「CCI顺势指标策略」
 * CCI小于-100超卖做多，CCI大于100超买做空，标准20周期参数
 */
public class CciStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        double cci = calcCCI(k15m, 20);
        if (cci < -100) return 1;
        if (cci > 100) return -1;
        return 0;
    }

    private double calcCCI(KLineVO[] k, int period) {
        int len = k.length;
        int start = len - period;
        double sum = 0;
        double[] tp = new double[period];
        for (int i = 0; i < period; i++) {
            tp[i] = k[start + i].getClose();
            sum += tp[i];
        }
        double ma = sum / period;
        double avgDev = 0;
        for (double v : tp) avgDev += Math.abs(v - ma);
        avgDev /= period;
        return avgDev == 0 ? 0 : (tp[period - 1] - ma) / (0.015 * avgDev);
    }

    @Override
    public String name() {
        return "CCI顺势指标策略";
    }
}