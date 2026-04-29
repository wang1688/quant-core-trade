package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「均线交叉反转策略」
 * 高位均线金叉不追涨反手做空，低位均线死叉不杀跌反手做多
 */
public class MaCrossReverseStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 60) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma20 = sma(close, last, 20);
        double ma60 = sma(close, last, 60);
        double ma20Prev = sma(close, last-1, 20);
        double ma60Prev = sma(close, last-1, 60);

        boolean gold = ma20 > ma60 && ma20Prev <= ma60Prev;
        boolean dead = ma20 < ma60 && ma20Prev >= ma60Prev;

        if(gold && close[last] > ma60 * 1.05) return -1;
        if(dead && close[last] < ma60 * 0.95) return 1;
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
        return "均线交叉反转策略";
    }
}