package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchStrategyBackTestMain {
    // ==================== 配置区域 ====================
    // 代理配置
    private static final boolean USE_PROXY = true;
    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7890;

    // 回测配置
    private static final String API_KEY = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String PASSPHRASE = "King$168$hello#168#";
    private static final boolean SIMULATED = true;

    // 回测配置
    private static final String INST_ID = "BTC-USDT-SWAP";
    private static final String BAR = "15m";
    private static final String STRATEGY_PACKAGE = "com.ruoyi.web.okx.strategy";
    
    // 【新增】回测参数优化
    private static final double RISK_FREE_RATE = 0.02; // 无风险利率，可调整为0
    private static final int FORCE_CLOSE_AFTER_KLINES = 48; // 持仓N根K线后强制平仓（0表示不强制）
    private static final boolean PRINT_DEBUG_LOG = false; // 是否打印详细开平仓日志
    private static final boolean PRINT_SIGNAL_STATS = true; // 是否打印信号统计

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
            
            StrategyBackTestResult result = backTestSingleStrategy(strategy, kLineArray);
            allResults.add(result);
            
            // 打印单个策略结果
            String status = result.getResult().getTotalTrades() == 0 ? "⚠️ 无交易" : "✅ 完成";
            System.out.printf("    %s！交易笔数：%d，胜率：%s，总收益率：%s%%\n\n",
                    status,
                    result.getResult().getTotalTrades(),
                    result.getResult().getWinRate().setScale(2, BigDecimal.ROUND_HALF_UP),
                    result.getTotalProfitRate().setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        // 5. 输出所有策略的对比结果
        printComparisonTable(allResults);
    }

    /**
     * 回测单个策略（优化版）
     */
    private static StrategyBackTestResult backTestSingleStrategy(TradeStrategy strategy, KLineVO[] kLineArray) {
        List<BackTradeRecord> tradeList = new ArrayList<>();
        List<BigDecimal> equityCurve = new ArrayList<>();
        Map<Integer, Integer> signalStats = new HashMap<>(); // 信号统计
        
        int holdPosition = 0;
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal currentEquity = BigDecimal.ZERO;
        int holdKlineCount = 0; // 持仓K线计数
        
        equityCurve.add(currentEquity);
        signalStats.put(0, 0);
        signalStats.put(1, 0);
        signalStats.put(-1, 0);

        for (int i = 60; i < kLineArray.length; i++) {
            KLineVO[] currentKLine = new KLineVO[i + 1];
            System.arraycopy(kLineArray, 0, currentKLine, 0, i + 1);

            int signal = strategy.signal(currentKLine, null, null);
            signalStats.put(signal, signalStats.getOrDefault(signal, 0) + 1); // 统计信号
            
            BigDecimal currentClose = BigDecimal.valueOf(kLineArray[i].getClose());

            if (holdPosition == 0) {
                // 空仓，开仓
                if (signal == 1 || signal == -1) {
                    holdPosition = signal;
                    openPrice = currentClose;
                    holdKlineCount = 0;
                    if (PRINT_DEBUG_LOG) {
                        System.out.printf("    [开仓] %s 价格：%.2f 信号：%d%n",
                                holdPosition == 1 ? "做多" : "做空",
                                currentClose, signal);
                    }
                }
                equityCurve.add(currentEquity);
            } else {
                holdKlineCount++;
                boolean needClose = false;
                
                // 条件1：信号反转
                if ((holdPosition == 1 && signal == -1) || (holdPosition == -1 && signal == 1)) {
                    needClose = true;
                    if (PRINT_DEBUG_LOG) System.out.println("    [平仓原因] 信号反转");
                }
                // 条件2：信号为0
                else if (signal == 0) {
                    needClose = true;
                    if (PRINT_DEBUG_LOG) System.out.println("    [平仓原因] 信号为0");
                }
                // 条件3：强制平仓（持仓时间过长）
                else if (FORCE_CLOSE_AFTER_KLINES > 0 && holdKlineCount >= FORCE_CLOSE_AFTER_KLINES) {
                    needClose = true;
                    if (PRINT_DEBUG_LOG) System.out.println("    [平仓原因] 持仓时间过长，强制平仓");
                }

                if (needClose) {
                    // 平仓
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

                    if (PRINT_DEBUG_LOG) {
                        System.out.printf("    [平仓] 价格：%.2f 收益率：%.2f%% %s%n",
                                currentClose,
                                profitRate.multiply(BigDecimal.valueOf(100)),
                                profitRate.compareTo(BigDecimal.ZERO) > 0 ? "✅盈利" : "❌亏损");
                    }

                    // 如果是信号反转，继续反向开仓
                    if ((holdPosition == 1 && signal == -1) || (holdPosition == -1 && signal == 1)) {
                        holdPosition = signal;
                        openPrice = currentClose;
                        holdKlineCount = 0;
                        if (PRINT_DEBUG_LOG) {
                            System.out.printf("    [反向开仓] %s 价格：%.2f%n",
                                    holdPosition == 1 ? "做多" : "做空",
                                    currentClose);
                        }
                    } else {
                        holdPosition = 0;
                        openPrice = BigDecimal.ZERO;
                        holdKlineCount = 0;
                    }
                } else {
                    // 信号不变，继续持有
                    equityCurve.add(currentEquity);
                }
            }
        }

        // 计算回测结果
        BackTestResult result = BackTestCalculator.calculate(tradeList, equityCurve, RISK_FREE_RATE);
        
        // 计算总收益率
        BigDecimal totalProfitRate = equityCurve.get(equityCurve.size() - 1).multiply(BigDecimal.valueOf(100));
        
        // 打印信号统计
        if (PRINT_SIGNAL_STATS) {
            System.out.printf("    [信号统计] 做多：%d次，做空：%d次，观望：%d次%n",
                    signalStats.getOrDefault(1, 0),
                    signalStats.getOrDefault(-1, 0),
                    signalStats.getOrDefault(0, 0));
        }

        return new StrategyBackTestResult(strategy.name(), result, totalProfitRate);
    }

    /**
     * 打印所有策略的对比表格
     */
    private static void printComparisonTable(List<StrategyBackTestResult> results) {
        System.out.println("\n" + "=".repeat(150));
        System.out.printf("%-30s %-10s %-12s %-10s %-12s %-15s %-10s%n", 
                "策略名称", "交易笔数", "总收益率(%)", "胜率(%)", "盈亏比", "最大回撤(%)", "夏普比率");
        System.out.println("-".repeat(150));

        for (StrategyBackTestResult r : results) {
            BackTestResult res = r.getResult();
            String tradeCount = res.getTotalTrades() == 0 ? "⚠️0" : String.valueOf(res.getTotalTrades());
            
            System.out.printf("%-30s %-10s %-12.2f %-10.2f %-12.2f %-15.2f %-10.2f%n",
                    r.getStrategyName(),
                    tradeCount,
                    r.getTotalProfitRate().setScale(2, BigDecimal.ROUND_HALF_UP),
                    res.getWinRate().setScale(2, BigDecimal.ROUND_HALF_UP),
                    res.getProfitLossRatio(),
                    res.getMaxDrawdown().setScale(2, BigDecimal.ROUND_HALF_UP),
                    res.getSharpeRatio());
        }

        System.out.println("=".repeat(150));
        
        // 统计总结
        long validCount = results.stream().filter(r -> r.getResult().getTotalTrades() > 0).count();
        long zeroTradeCount = results.size() - validCount;
        System.out.printf("回测完成：有效策略 %d 个，无交易策略 %d 个%n", validCount, zeroTradeCount);
    }

    /**
     * 策略回测结果封装类
     */
    static class StrategyBackTestResult {
        private final String strategyName;
        private final BackTestResult result;
        private final BigDecimal totalProfitRate;

        public StrategyBackTestResult(String strategyName, BackTestResult result, BigDecimal totalProfitRate) {
            this.strategyName = strategyName;
            this.result = result;
            this.totalProfitRate = totalProfitRate;
        }

        public String getStrategyName() { return strategyName; }
        public BackTestResult getResult() { return result; }
        public BigDecimal getTotalProfitRate() { return totalProfitRate; }
    }
}