package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「EMA+ATR趋势过滤策略」
 * 价格站上EMA20且突破ATR波幅顺势做多；跌破EMA20且下破ATR波幅顺势做空
 */
public class EmaAtrStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ema20 = ema(close, last, 20);
        double atr = calcATR(k15m,14);

        if(close[last] > ema20 && close[last] > close[last-1]+atr) return 1;
        if(close[last] < ema20 && close[last] < close[last-1]-atr) return -1;
        return 0;
    }

    private double ema(double[] arr, int idx, int p){
        double a = 2.0/(p+1);
        double res = arr[0];
        for(int i=1;i<=idx;i++) res = a*arr[i] + (1-a)*res;
        return res;
    }

    private double calcATR(KLineVO[] k, int period){
        double sum = 0;
        int len = k.length;
        for(int i=len-period;i<len;i++){
            sum += Math.abs(k[i].getClose() - k[i-1].getClose());
        }
        return sum / period;
    }

    @Override
    public String name() {
        return "EMA+ATR趋势过滤策略";
    }
}