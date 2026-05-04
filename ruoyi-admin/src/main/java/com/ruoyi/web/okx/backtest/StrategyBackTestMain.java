package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.backtest.BackTestCalculator;
import com.ruoyi.web.okx.backtest.BackTestResult;
import com.ruoyi.web.okx.backtest.BackTradeRecord;
import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.strategy.TrendStrengthStrategy;
import com.ruoyi.web.okx.vo.KLineVO;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class StrategyBackTestMain {
    // 代理配置
    private static final boolean USE_PROXY = true;
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7890;

    // 回测配置
    private static final String API_KEY = "你的OKX_API_KEY";
    private static final String API_SECRET = "你的OKX_API_SECRET";
    private static final String PASSPHRASE = "你的OKX_PASSPHRASE";
    private static final boolean SIMULATED = true;
    private static final String INST_ID = "BTC-USDT-SWAP";
    private static final String BAR = "15m";

    public static void main(String[] args) throws Exception {
        // 1. 初始化带代理的 OkxApiClient
        HttpClient httpClient = HttpClient.newHttpClient();
        OkxApiClient okxClient;
        
        if (USE_PROXY) {
            System.out.println("正在使用代理连接 OKX：" + PROXY_HOST + ":" + PROXY_PORT);
            okxClient = new OkxApiClient(
                    API_KEY, API_SECRET, PASSPHRASE, SIMULATED, httpClient,
                    PROXY_HOST, PROXY_PORT
            );
        } else {
            System.out.println("正在直连 OKX（无代理）");
            okxClient = new OkxApiClient(API_KEY, API_SECRET, PASSPHRASE, SIMULATED, httpClient);
        }

        // 2. 拉取2年真实K线
        System.out.println("正在从OKX接口拉取2年历史K线数据...");
        KLineVO[] kLineArray = okxClient.getTwoYearsKLineArray(INST_ID, BAR);
        System.out.println("K线数据拉取完成，共 " + kLineArray.length + " 根");

        // 3. 初始化策略
        TradeStrategy strategy = new TrendStrengthStrategy();
        System.out.println("正在执行策略回测：" + strategy.name());

        // 4. 【修复】回测执行：优化开平仓逻辑
        List<BackTradeRecord> tradeList = new ArrayList<>();
        List<BigDecimal> equityCurve = new ArrayList<>(); // 【新增】连续资金曲线，用于计算最大回撤
        int holdPosition = 0;
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal currentEquity = BigDecimal.ZERO; // 当前权益
        equityCurve.add(currentEquity); // 初始资金

        for (int i = 60; i < kLineArray.length; i++) {
            KLineVO[] currentKLine = new KLineVO[i + 1];
            System.arraycopy(kLineArray, 0, currentKLine, 0, i + 1);

            int signal = strategy.signal(currentKLine, null, null);
            BigDecimal currentClose = BigDecimal.valueOf(kLineArray[i].getClose());

            // 【修复】开平仓逻辑：信号反转时平仓并反向开仓，不再要求signal==0
            if (holdPosition == 0) {
                // 空仓，开仓
                if (signal == 1) {
                    holdPosition = 1;
                    openPrice = currentClose;
                } else if (signal == -1) {
                    holdPosition = -1;
                    openPrice = currentClose;
                }
            } else {
                // 有持仓，检查是否需要平仓或反向
                if ((holdPosition == 1 && signal == -1) || (holdPosition == -1 && signal == 1)) {
                    // 信号反转：先平仓
                    BackTradeRecord record = new BackTradeRecord();
                    record.setOpenPrice(openPrice);
                    record.setClosePrice(currentClose);

                    BigDecimal profitRate;
                    if (holdPosition == 1) {
                        profitRate = currentClose.subtract(openPrice)
                                .divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
                    } else {
                        profitRate = openPrice.subtract(currentClose)
                                .divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
                    }
                    record.setProfitRate(profitRate);
                    record.setWin(profitRate.compareTo(BigDecimal.ZERO) > 0);
                    tradeList.add(record);

                    // 更新权益
                    currentEquity = currentEquity.add(profitRate);
                    equityCurve.add(currentEquity);

                    // 反向开仓
                    holdPosition = signal;
                    openPrice = currentClose;
                } else if (signal == 0) {
                    // 信号为0，平仓
                    BackTradeRecord record = new BackTradeRecord();
                    record.setOpenPrice(openPrice);
                    record.setClosePrice(currentClose);

                    BigDecimal profitRate;
                    if (holdPosition == 1) {
                        profitRate = currentClose.subtract(openPrice)
                                .divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
                    } else {
                        profitRate = openPrice.subtract(currentClose)
                                .divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
                    }
                    record.setProfitRate(profitRate);
                    record.setWin(profitRate.compareTo(BigDecimal.ZERO) > 0);
                    tradeList.add(record);

                    // 更新权益
                    currentEquity = currentEquity.add(profitRate);
                    equityCurve.add(currentEquity);

                    // 清空持仓
                    holdPosition = 0;
                    openPrice = BigDecimal.ZERO;
                } else {
                    // 信号不变，继续持有，但更新资金曲线（用于计算回撤）
                    equityCurve.add(currentEquity);
                }
            }
        }

        // 5. 【修复】计算指标：传入资金曲线计算真实最大回撤
        BackTestResult result = BackTestCalculator.calculate(tradeList, equityCurve);

        // 6. 打印结果
        System.out.println("\n==================== 回测结果 ====================");
        System.out.println("策略名称：" + strategy.name());
        System.out.println("交易对：" + INST_ID);
        System.out.println("K线周期：" + BAR);
        System.out.println("回测周期：2年");
        System.out.println("总交易笔数：" + result.getTotalTrades());
        System.out.println("胜率：" + result.getWinRate().setScale(2, BigDecimal.ROUND_HALF_UP) + " %");
        System.out.println("盈亏比：" + result.getProfitLossRatio());
        System.out.println("最大回撤：" + result.getMaxDrawdown().setScale(2, BigDecimal.ROUND_HALF_UP) + " %");
        System.out.println("夏普比率：" + result.getSharpeRatio());
        System.out.println("===================================================");
    }
}