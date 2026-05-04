package com.ruoyi.web.okx.backtest;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BackTestResult {
    private int totalTrades;
    private BigDecimal winRate;
    private BigDecimal profitLossRatio;
    private BigDecimal maxDrawdown;
    private BigDecimal sharpeRatio;
}