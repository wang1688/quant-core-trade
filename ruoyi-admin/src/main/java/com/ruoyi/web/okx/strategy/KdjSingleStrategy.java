package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 纯KDJ单指标策略
 * KDJ20以下金叉做多，80以上死叉做空
 */
public class KdjSingleStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 9) return 0;
        double k = calcK(k15m, k15m.length-1);
        double d = calcD(k15m, k15m.length-1);

        if(k < 20 && k > d) return 1;
        if(k > 80 && k < d) return -1;
        return 0;
    }

    private double calcK(KLineVO[] k, int idx){
        int start = idx - 8;
        if(start < 0) start = 0;
        double low = Double.MAX_VALUE, high = Double.MIN_VALUE;
        for(int i=start;i<=idx;i++){
            low = Math.min(low, k[i].getClose());
            high = Math.max(high, k[i].getClose());
        }
        return (k[idx].getClose()-low)/(high-low)*100;
    }

    private double calcD(KLineVO[] k, int idx){
        return calcK(k,idx)*0.3 + 50*0.7;
    }

    @Override
    public String name() {
        return "纯KDJ单指标策略";
    }
}