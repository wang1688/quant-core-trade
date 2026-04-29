package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 趋势强弱判定策略
 * 连续3根K线站上均线强趋势做多，连续跌破强趋势做空
 */
public class TrendStrengthStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;
        double ma20 = sma(close,last,20);

        boolean up1 = close[last] > ma20;
        boolean up2 = close[last-1] > ma20;
        boolean up3 = close[last-2] > ma20;

        boolean dn1 = close[last] < ma20;
        boolean dn2 = close[last-1] < ma20;
        boolean dn3 = close[last-2] < ma20;

        if(up1 && up2 && up3) return 1;
        if(dn1 && dn2 && dn3) return -1;
        return 0;
    }

    private double sma(double[] arr,int idx,int p){
        int start = Math.max(0, idx-p+1);
        double sum=0;
        for(int i=start;i<=idx;i++) sum+=arr[i];
        return sum/(idx-start+1);
    }

    @Override
    public String name() {
        return "趋势强弱判定策略";
    }
}