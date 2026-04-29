package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「能量潮OBV策略」
 * OBV向上拐头做多、向下拐头做空，通过量能变化预判价格趋势
 */
public class ObvStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 5) return 0;
        double obvNow = calcObv(k15m, k15m.length - 1);
        double obvPrev = calcObv(k15m, k15m.length - 2);

        if(obvNow > obvPrev) return 1;
        if(obvNow < obvPrev) return -1;
        return 0;
    }

    private double calcObv(KLineVO[] k, int idx){
        if(idx <= 0) return 0;
        double curr = k[idx].getClose();
        double prev = k[idx-1].getClose();
        return curr > prev ? 1 : curr < prev ? -1 : 0;
    }

    @Override
    public String name() {
        return "能量潮OBV策略";
    }
}