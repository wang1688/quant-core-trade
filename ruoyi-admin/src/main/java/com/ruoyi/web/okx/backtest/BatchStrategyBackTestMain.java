package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.backtest.BackTestCalculator;
import com.ruoyi.web.okx.backtest.BackTestResult;
import com.ruoyi.web.okx.backtest.BackTradeRecord;
import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

public class BatchStrategyBackTestMain {
    // 代理配置
    private static final boolean USE_PROXY = true;
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7890;

    // 回测配置
    private static final String API_KEY = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String PASSPHRASE = "King$168$hello#168#";
    private static final boolean SIMULATED = true;
    private static final String INST_ID = "BTC-USDT-SWAP";
    private static final String BAR = "15m";
    private static final String STRATEGY_PACKAGE = "com.ruoyi.web.okx.strategy";

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

        // 2. 拉取2年真实K线（所有策略共用同一套K线，保证对比公平）
        System.out.println("正在从OKX接口拉取2年历史K线数据...");
        KLineVO[] kLineArray = okxClient.getTwoYearsKLineArray(INST_ID, BAR);
        System.out.println("K线数据拉取完成，共 " + kLineArray.length + " 根\n");

        // 3. 自动扫描所有策略
        System.out.println("正在扫描策略包：" + STRATEGY_PACKAGE);
        List<TradeStrategy> strategies = StrategyScanner.scanStrategies(STRATEGY_PACKAGE);
        System.out.println("共发现 " + strategies.size() + " 个策略\n");

        // 4. 逐个回测所有策略
        List<StrategyBackTestResult> allResults = new ArrayList<>();
        for (int i = 0; i < strategies.size(); i++) {
            TradeStrategy strategy = strategies.get(i);
            System.out.println("[" + (i + 1) + "/" + strategies.size() + "] 正在回测策略：" + strategy.name());
            
            BackTestResult result = backTestSingleStrategy(strategy, kLineArray);
            allResults.add(new StrategyBackTestResult(strategy.name(), result));
            
            System.out.println("    完成！交易笔数：" + result.getTotalTrades() + 
                             "，胜率：" + result.getWinRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%\n");
        }

        // 5. 输出所有策略的对比结果
        printComparisonTable(allResults);
    }

    /**
     * 回测单个策略
     */
    private static BackTestResult backTestSingleStrategy(TradeStrategy strategy, KLineVO[] kLineArray) {
        List<BackTradeRecord> tradeList = new ArrayList<>();
        List<BigDecimal> equityCurve = new ArrayList<>();
        int holdPosition = 0;
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal currentEquity = BigDecimal.ZERO;
        equityCurve.add(currentEquity);

        for (int i = 60; i < kLineArray.length; i++) {
            KLineVO[] currentKLine = new KLineVO[i + 1];
            System.arraycopy(kLineArray, 0, currentKLine, 0, i + 1);

            int signal = strategy.signal(currentKLine, null, null);
            BigDecimal currentClose = BigDecimal.valueOf(kLineArray[i].getClose());

            if (holdPosition == 0) {
                if (signal == 1) {
                    holdPosition = 1;
                    openPrice = currentClose;
                } else if (signal == -1) {
                    holdPosition = -1;
                    openPrice = currentClose;
                }
                equityCurve.add(currentEquity);
            } else {
                if ((holdPosition == 1 && signal == -1) || (holdPosition == -1 && signal == 1)) {
                    // 信号反转：平仓并反向开仓
                    BackTradeRecord record = new BackTradeRecord();
                    record.setOpenPrice(openPrice);
                    record.setClosePrice(currentClose);

                    BigDecimal profitRate = holdPosition == 1
                            ? currentClose.subtract(openPrice).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP)
                            : openPrice.subtract(currentClose).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
                    record.setProfitRate(profitRate);
                    record.setWin(profitRate.compareTo(BigDecimal.ZERO) > 0);
                    tradeList.add(record);

                    currentEquity = currentEquity.add(profitRate);
                    equityCurve.add(currentEquity);

                    holdPosition = signal;
                    openPrice = currentClose;
                } else if (signal == 0) {
                    // 信号为0：平仓
                    BackTradeRecord record = new BackTradeRecord();
                    record.setOpenPrice(openPrice);
                    record.setClosePrice(currentClose);

                    BigDecimal profitRate = holdPosition == 1
                            ? currentClose.subtract(openPrice).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP)
                            : openPrice.subtract(currentClose).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
                    record.setProfitRate(profitRate);
                    record.setWin(profitRate.compareTo(BigDecimal.ZERO) > 0);
                    tradeList.add(record);

                    currentEquity = currentEquity.add(profitRate);
                    equityCurve.add(currentEquity);

                    holdPosition = 0;
                    openPrice = BigDecimal.ZERO;
                } else {
                    // 信号不变：继续持有
                    equityCurve.add(currentEquity);
                }
            }
        }

        return BackTestCalculator.calculate(tradeList, equityCurve);
    }

    /**
     * 打印所有策略的对比表格
     */
    private static void printComparisonTable(List<StrategyBackTestResult> results) {
        System.out.println("\n" + "=".repeat(120));
        System.out.printf("%-30s %-10s %-10s %-10s %-15s %-10s%n", 
                "策略名称", "交易笔数", "胜率(%)", "盈亏比", "最大回撤(%)", "夏普比率");
        System.out.println("-".repeat(120));

        for (StrategyBackTestResult r : results) {
            BackTestResult res = r.getResult();
            System.out.printf("%-30s %-10d %-10.2f %-10.2f %-15.2f %-10.2f%n",
                    r.getStrategyName(),
                    res.getTotalTrades(),
                    res.getWinRate().setScale(2, BigDecimal.ROUND_HALF_UP),
                    res.getProfitLossRatio(),
                    res.getMaxDrawdown().setScale(2, BigDecimal.ROUND_HALF_UP),
                    res.getSharpeRatio());
        }

        System.out.println("=".repeat(120));
    }

    /**
     * 策略回测结果封装类
     */
    static class StrategyBackTestResult {
        private final String strategyName;
        private final BackTestResult result;

        public StrategyBackTestResult(String strategyName, BackTestResult result) {
            this.strategyName = strategyName;
            this.result = result;
        }

        public String getStrategyName() { return strategyName; }
        public BackTestResult getResult() { return result; }
    }
}