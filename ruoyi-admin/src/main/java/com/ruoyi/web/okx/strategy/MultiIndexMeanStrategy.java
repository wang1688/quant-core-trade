package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 多指标均值共振策略
 * RSI/CCI/WR 均值超卖做多，均值超买做空
 */
public class MultiIndexMeanStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();

        double rsi = calcRsi(close,14);
        double cci = calcCCI(k15m,20);
        double wr = calcWR(k15m,14);

        double avg = (rsi + (100+cci)/2 + wr) / 3;
        if(avg < 35) return 1;
        if(avg > 65) return -1;
        return 0;
    }

    private double calcRsi(double[] c,int n){
        double g=0,l=0;
        for(int i=1;i<n;i++){
            double d=c[i]-c[i-1];
            if(d>0)g+=d;else l-=d;
        }
        if(l==0) return 100;
        return 100 - 100/(1+g/l);
    }

    private double calcCCI(KLineVO[] k,int period){
        int len = k.length;
        int start = len-period;
        double sum=0;
        for(int i=start;i<len;i++) sum+=k[i].getClose();
        double ma=sum/period;
        double dev=0;
        for(int i=start;i<len;i++) dev+=Math.abs(k[i].getClose()-ma);
        dev /= period;
        return dev==0?0:(k[len-1].getClose()-ma)/(0.015*dev);
    }

    private double calcWR(KLineVO[] k,int period){
        int len = k.length;
        int start = len-period;
        double high=Double.MIN_VALUE,low=Double.MAX_VALUE;
        for(int i=start;i<len;i++){
            high=Math.max(high,k[i].getClose());
            low=Math.min(low,k[i].getClose());
        }
        return (high - k[len-1].getClose())/(high-low)*100;
    }

    @Override
    public String name() {
        return "多指标均值共振策略";
    }
}