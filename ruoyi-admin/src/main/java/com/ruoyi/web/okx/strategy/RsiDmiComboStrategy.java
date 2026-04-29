package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「RSI+DMI趋势共振策略」
 * DMI多头占优+RSI低位做多；DMI空头占优+RSI高位做空
 */
public class RsiDmiComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 14) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();

        double rsi = calcRsi(close,14);
        double plusDI = calcPlusDI(k15m,14);
        double minusDI = calcMinusDI(k15m,14);

        if (plusDI > minusDI && rsi < 40) return 1;
        if (plusDI < minusDI && rsi > 60) return -1;
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

    private double calcPlusDI(KLineVO[] k, int p){
        int len = k.length;
        double up = 0;
        for(int i=len-p;i<len;i++){
            up += Math.max(k[i].getClose()-k[i-1].getClose(),0);
        }
        return up/p*100;
    }

    private double calcMinusDI(KLineVO[] k, int p){
        int len = k.length;
        double down = 0;
        for(int i=len-p;i<len;i++){
            down += Math.max(k[i-1].getClose()-k[i].getClose(),0);
        }
        return down/p*100;
    }

    @Override
    public String name() {
        return "RSI+DMI趋势共振策略";
    }
}