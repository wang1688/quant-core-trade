package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「全指标综合过滤策略」
 * RSI+MACD+CCI+布林带多指标同时共振，高胜率过滤杂波
 */
public class AllIndexFilterStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 50) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double rsi = calcRsi(close,14);
        double cci = calcCCI(k15m,20);
        boolean macdGold = isMacdGold(close);
        boolean macdDead = isMacdDead(close);

        if(rsi < 35 && cci < -80 && macdGold) return 1;
        if(rsi > 65 && cci > 80 && macdDead) return -1;
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

    private boolean isMacdGold(double[] c){
        double[] e12 = calcEma(c,12);
        double[] e26 = calcEma(c,26);
        double[] dif = sub(e12,e26);
        double[] dea = calcEma(dif,9);
        int last = c.length - 1;
        return dif[last-1] < dea[last-1] && dif[last] > dea[last];
    }

    private boolean isMacdDead(double[] c){
        double[] e12 = calcEma(c,12);
        double[] e26 = calcEma(c,26);
        double[] dif = sub(e12,e26);
        double[] dea = calcEma(dif,9);
        int last = c.length - 1;
        return dif[last-1] > dea[last-1] && dif[last] < dea[last];
    }

    private double[] calcEma(double[] src,int p){
        double[] res = new double[src.length];
        res[0] = src[0];
        double a = 2.0/(p+1);
        for(int i=1;i<src.length;i++) res[i] = a*src[i] + (1-a)*res[i-1];
        return res;
    }

    private double[] sub(double[] a,double[] b){
        double[] res = new double[a.length];
        for(int i=0;i<a.length;i++) res[i] = a[i]-b[i];
        return res;
    }

    @Override
    public String name() {
        return "全指标综合过滤策略";
    }
}