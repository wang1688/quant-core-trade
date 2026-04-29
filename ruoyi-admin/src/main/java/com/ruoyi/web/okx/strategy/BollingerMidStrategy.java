package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 布林中轨突破策略
 * 价格放量站上布林中轨做多，跌破中轨做空
 */
public class BollingerMidStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double mid = sma(close, last, 20);
        if(close[last] > mid && close[last-1] < mid) return 1;
        if(close[last] < mid && close[last-1] > mid) return -1;
        return 0;
    }

    private double sma(double[] arr, int idx, int p){
        int start = Math.max(0, idx - p + 1);
        double sum = 0;
        for(int i=start;i<=idx;i++) sum += arr[i];
        return sum / (idx - start + 1);
    }

    @Override
    public String name() {
        return "布林中轨突破策略";
    }
}