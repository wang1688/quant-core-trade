package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「KDJ 随机指标策略」，9 周期 KDJ，低位金叉做多、高位死叉做空，超买 80、超卖 20 阈值标准规则
 */
public class KdjStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 9) return 0;
        int last = k15m.length - 1;
        double k = calcK(k15m, last);
        double d = calcD(k15m, last);

        if (k > d && k < 20) return 1;
        if (k < d && k > 80) return -1;
        return 0;
    }

    private double calcK(KLineVO[] k, int idx) {
        int start = idx - 8;
        if (start < 0) start = 0;
        double low = Double.MAX_VALUE;
        double high = Double.MIN_VALUE;
        for (int i = start; i <= idx; i++) {
            low = Math.min(low, k[i].getClose());
            high = Math.max(high, k[i].getClose());
        }
        return (k[idx].getClose() - low) / (high - low) * 100;
    }

    private double calcD(KLineVO[] k, int idx) {
        double rsv = calcK(k, idx);
        return rsv / 3 + 50 * 2 / 3;
    }

    @Override
    public String name() {
        return "KDJ随机指标策略";
    }
}