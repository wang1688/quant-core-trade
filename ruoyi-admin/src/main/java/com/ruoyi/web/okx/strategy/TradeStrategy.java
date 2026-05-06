package com.ruoyi.web.okx.strategy;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 交易策略接口
 * signal返回值：1=做多，-1=做空，0=观望
 */
public interface TradeStrategy {
    String name();
    int signal(KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h);
}
