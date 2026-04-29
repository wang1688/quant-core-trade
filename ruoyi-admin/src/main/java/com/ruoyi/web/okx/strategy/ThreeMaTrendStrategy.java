package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 三线均线趋势策略
 * MA5/MA15/MA30 三线多头排列做多，空头排列做空
 */
public class ThreeMaTrendStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 30) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma5 = sma(close, last, 5);
        double ma15 = sma(close, last, 15);
        double ma30 = sma(close, last, 30);

        if(ma5 > ma15 && ma15 > ma30) return 1;
        if(ma5 < ma15 && ma15 < ma30) return -1;
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
        return "三线均线趋势策略";
    }
}