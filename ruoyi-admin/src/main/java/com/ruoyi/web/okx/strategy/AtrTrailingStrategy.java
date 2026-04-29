package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * ATR动态止损趋势策略
 * 价格站稳前高+ATR放大顺势做多，破前低+ATR放大顺势做空
 */
public class AtrTrailingStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 14) return 0;
        int len = k15m.length;
        double atr = calcATR(k15m,14);
        double now = k15m[len-1].getClose();
        double preHigh = k15m[len-2].getClose();
        double preLow = k15m[len-2].getClose();

        if(now > preHigh + atr) return 1;
        if(now < preLow - atr) return -1;
        return 0;
    }

    private double calcATR(KLineVO[] k, int period){
        double sum = 0;
        int len = k.length;
        for(int i=len-period;i<len;i++){
            sum += Math.abs(k[i].getClose()-k[i-1].getClose());
        }
        return sum / period;
    }

    @Override
    public String name() {
        return "ATR动态止损趋势策略";
    }
}