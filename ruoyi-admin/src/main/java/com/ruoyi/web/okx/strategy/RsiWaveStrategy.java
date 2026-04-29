package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * RSI波浪高低点策略
 * RSI一浪低于一浪见底做多，一浪高于一浪见顶做空
 */
public class RsiWaveStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();

        double rsi1 = calcRsi(close,14, close.length-1);
        double rsi2 = calcRsi(close,14, close.length-5);

        if(rsi1 < 30 && rsi1 < rsi2) return 1;
        if(rsi1 > 70 && rsi1 > rsi2) return -1;
        return 0;
    }

    private double calcRsi(double[] c, int n, int idx){
        double g=0,l=0;
        for(int i=idx-n+1;i<idx;i++){
            double d = c[i]-c[i-1];
            if(d>0) g+=d; else l-=d;
        }
        if(l==0) return 100;
        return 100 - 100/(1+g/l);
    }

    @Override
    public String name() {
        return "RSI波浪高低点策略";
    }
}