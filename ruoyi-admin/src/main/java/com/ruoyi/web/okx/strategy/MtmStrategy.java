package com.ruoyi.web.okx.strategy;
import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 「动量MTM策略」
 * MTM由负转正做多、由正转负做空，捕捉价格涨跌动量切换
 */
public class MtmStrategy implements TradeStrategy {
    @Override
    public int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h) {
        if(k15m == null || k15m.length < 10) return 0;
        int len = k15m.length;
        double mtm = k15m[len-1].getClose() - k15m[len-10].getClose();
        double mtmPrev = k15m[len-2].getClose() - k15m[len-11].getClose();

        if(mtm > 0 && mtmPrev < 0) return 1;
        if(mtm < 0 && mtmPrev > 0) return -1;
        return 0;
    }

    @Override
    public String name() {
        return "动量MTM指标策略";
    }
}