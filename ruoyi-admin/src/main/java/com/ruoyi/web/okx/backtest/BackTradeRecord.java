package com.ruoyi.web.okx.backtest;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BackTradeRecord {
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal profitRate;
    private boolean isWin;
}