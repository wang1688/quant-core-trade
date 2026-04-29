package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「RSI+CCI双指标共振策略」
 * RSI超卖+CCI负超卖共振做多；RSI超买+CCI正超买共振做空
 */
public class RsiCciComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();

        double rsi = calcRsi(close,14);
        double cci = calcCCI(k15m,20);

        if(rsi < 30 && cci < -100) return 1;
        if(rsi > 70 && cci > 100) return -1;
        return 0;
    }

    private double calcRsi(double[] c, int n){
        double g=0,l=0;
        for(int i=1;i<n;i++){
            double d = c[i]-c[i-1];
            if(d>0) g+=d; else l-=d;
        }
        if(l==0) return 100;
        return 100 - 100/(1+g/l);
    }

    private double calcCCI(KLineVO[] k, int period){
        int len = k.length;
        int start = len - period;
        double sum = 0;
        for(int i=start;i<len;i++) sum += k[i].getClose();
        double ma = sum / period;
        double avgDev = 0;
        for(int i=start;i<len;i++) avgDev += Math.abs(k[i].getClose() - ma);
        avgDev /= period;
        return avgDev == 0 ? 0 : (k[len-1].getClose() - ma)/(0.015*avgDev);
    }

    @Override
    public String name() {
        return "RSI+CCI双指标共振策略";
    }
}