package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

public interface TradeStrategy {
    /**
     * 1=做多，-1=做空，0=无信号
     */
    int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h);

    String name();
}