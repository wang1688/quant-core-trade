package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「RSI+MACD组合共振策略」
 * MACD金叉 + RSI低位 共振做多；MACD死叉 + RSI高位 共振做空
 */
public class RsiMacdComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 50) return 0;
        double[] close = new double[k15m.length];
        for (int i = 0; i < k15m.length; i++) close[i] = k15m[i].getClose();

        double rsi = calcRsi(close,14);
        double[] dif = calcMacdDif(close);
        double[] dea = calcEma(dif,9);

        int last = close.length - 1;
        boolean macdGold = dif[last-1] < dea[last-1] && dif[last] > dea[last];
        boolean macdDead = dif[last-1] > dea[last-1] && dif[last] < dea[last];

        if(macdGold && rsi < 40) return 1;
        if(macdDead && rsi > 60) return -1;
        return 0;
    }

    private double calcRsi(double[] c, int n){
        double g=0,l=0;
        for(int i=1;i<n;i++){
            double d=c[i]-c[i-1];
            if(d>0)g+=d;else l-=d;
        }
        if(l==0)return 100;
        return 100 - 100/(1+g/l);
    }
    private double[] calcEma(double[] src,int p){
        double[] res=new double[src.length];
        res[0]=src[0];
        double a=2.0/(p+1);
        for(int i=1;i<src.length;i++)res[i]=a*src[i]+(1-a)*res[i-1];
        return res;
    }
    private double[] calcMacdDif(double[] c){
        double[] e12=calcEma(c,12);
        double[] e26=calcEma(c,26);
        double[] dif=new double[c.length];
        for(int i=0;i<c.length;i++)dif[i]=e12[i]-e26[i];
        return dif;
    }

    @Override
    public String name() {
        return "RSI+MACD组合共振策略";
    }
}