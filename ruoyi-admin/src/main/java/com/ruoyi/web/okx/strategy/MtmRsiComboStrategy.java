package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「动量MTM+RSI组合策略」
 * MTM拐头向上+RSI超卖做多；MTM拐头向下+RSI超买做空
 */
public class MtmRsiComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 14) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();
        int len = close.length;

        double mtm = close[len-1] - close[len-10];
        double mtmPrev = close[len-2] - close[len-11];
        double rsi = calcRsi(close,14);

        if (mtm > mtmPrev && rsi < 30) return 1;
        if (mtm < mtmPrev && rsi > 70) return -1;
        return 0;
    }

    private double calcRsi(double[] c, int n){
        double g=0,l=0;
        for(int i=1;i<n;i++){
            double d=c[i]-c[i-1];
            if(d>0)g+=d;else l-=d;
        }
        if(l==0) return 100;
        return 100 - 100/(1+g/l);
    }

    @Override
    public String name() {
        return "动量MTM+RSI组合策略";
    }
}