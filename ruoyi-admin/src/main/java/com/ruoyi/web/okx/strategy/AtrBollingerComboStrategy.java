package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「ATR+布林带组合策略」
 * 布林带下轨+ATR缩量企稳做多；布林带上轨+ATR放量回落做空
 */
public class AtrBollingerComboStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if (k15m == null || k15m.length < 20) return 0;
        double[] close = new double[k15m.length];
        for(int i=0;i<k15m.length;i++) close[i] = k15m[i].getClose();
        int last = close.length - 1;

        double ma = sma(close,last,20);
        double std = stdDev(close,last,20);
        double up = ma + 2 * std;
        double dn = ma - 2 * std;
        double atr = calcATR(k15m,14);

        if (close[last] <= dn && atr < std) return 1;
        if (close[last] >= up && atr > std) return -1;
        return 0;
    }

    private double sma(double[] arr,int idx,int p) {
        int start = Math.max(0, idx-p+1);
        double sum = 0;
        for(int i=start;i<=idx;i++) sum += arr[i];
        return sum/(idx-start+1);
    }

    private double stdDev(double[] arr,int idx,int p) {
        int start = Math.max(0, idx-p+1);
        double avg = sma(arr,idx,p);
        double sum = 0;
        for(int i=start;i<=idx;i++) sum += Math.pow(arr[i]-avg,2);
        return Math.sqrt(sum/(idx-start+1));
    }

    private double calcATR(KLineVO[] k,int period) {
        double sum = 0;
        int len = k.length;
        for(int i=len-period;i<len;i++) sum += Math.abs(k[i].getClose()-k[i-1].getClose());
        return sum/period;
    }

    @Override
    public String name() {
        return "ATR+布林带组合策略";
    }
}