package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * MACD零轴突破策略
 * DIF上穿零轴做多，下穿零轴做空
 */
public class MacdZeroLineStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 30) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();

        double[] dif = calcMacdDif(close);
        int last = dif.length - 1;
        if(dif[last-1] < 0 && dif[last] > 0) return 1;
        if(dif[last-1] > 0 && dif[last] < 0) return -1;
        return 0;
    }

    private double[] calcEma(double[] src,int p){
        double[] res = new double[src.length];
        res[0] = src[0];
        double a = 2.0/(p+1);
        for(int i=1;i<src.length;i++) res[i] = a*src[i] + (1-a)*res[i-1];
        return res;
    }

    private double[] calcMacdDif(double[] c){
        double[] e12 = calcEma(c,12);
        double[] e26 = calcEma(c,26);
        double[] dif = new double[c.length];
        for(int i=0;i<c.length;i++) dif[i] = e12[i] - e26[i];
        return dif;
    }

    @Override
    public String name() {
        return "MACD零轴突破策略";
    }
}