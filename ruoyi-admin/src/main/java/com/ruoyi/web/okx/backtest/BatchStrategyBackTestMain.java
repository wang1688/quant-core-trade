package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量策略回测入口（三级别升级版）
 * 自动扫描 strategy 包下所有策略，使用 15M/1H/4H 三级别K线回测
 */
public class BatchStrategyBackTestMain {

    private static final boolean USE_PROXY  = true;
    private static final String  PROXY_HOST = "127.0.0.1";
    private static final int     PROXY_PORT = 7890;

    private static final String  API_KEY    = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String  API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String  PASSPHRASE = "King$168$hello#168#";
    private static final boolean SIMULATED  = true;

    private static final String  INST_ID          = "BTC-USDT-SWAP";
    private static final String  STRATEGY_PACKAGE = "com.ruoyi.web.okx.strategy";
    private static final double  RISK_FREE_RATE   = 0.02;
    private static final boolean PRINT_SIGNAL_STATS = true;

    public static void main(String[] args) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        OkxApiClient okxClient;
        if (USE_PROXY) {
            System.out.println("代理连接 OKX：" + PROXY_HOST + ":" + PROXY_PORT);
            okxClient = new OkxApiClient(API_KEY, API_SECRET, PASSPHRASE, SIMULATED,
                    httpClient, PROXY_HOST, PROXY_PORT);
        } else {
            okxClient = new OkxApiClient(API_KEY, API_SECRET, PASSPHRASE, SIMULATED, httpClient);
        }

        System.out.println("拉取 15M K线...");
        KLineVO[] k15m = okxClient.getTwoYearsKLineArray(INST_ID, "15m");
        System.out.println("15M：" + k15m.length + " 根");

        System.out.println("拉取 1H K线...");
        KLineVO[] k1h = okxClient.getTwoYearsKLineArray(INST_ID, "1H");
        System.out.println("1H ：" + k1h.length + " 根");

        System.out.println("拉取 4H K线...");
        KLineVO[] k4h = okxClient.getTwoYearsKLineArray(INST_ID, "4H");
        System.out.println("4H ：" + k4h.length + " 根\n");

        // 预计算对齐索引（所有策略共用，只算一次）
        int[] align1h = KLineAligner.buildAlignTable(k15m, k1h);
        int[] align4h = KLineAligner.buildAlignTable(k15m, k4h);

        System.out.println("扫描策略包：" + STRATEGY_PACKAGE);
        List<TradeStrategy> strategies = StrategyScanner.scanStrategies(STRATEGY_PACKAGE);
        System.out.println("发现 " + strategies.size() + " 个策略\n");

        List<StrategyBackTestResult> allResults = new ArrayList<>();

        for (int i = 0; i < strategies.size(); i++) {
            TradeStrategy strategy = strategies.get(i);
            System.out.printf("[%d/%d] %s%n", i + 1, strategies.size(), strategy.name());

            StrategyBackTestResult r = backtest(strategy, k15m, k1h, k4h, align1h, align4h);
            allResults.add(r);

            String status = r.getResult().getTotalTrades() == 0 ? "⚠️ 无交易" : "✅ 完成";
            System.out.printf("    %s  交易：%d笔  胜率：%s%%  收益：%s%%  回撤：%s%%%n%n",
                    status,
                    r.getResult().getTotalTrades(),
                    r.getResult().getWinRate().setScale(2, BigDecimal.ROUND_HALF_UP),
                    r.getTotalProfitRate().setScale(2, BigDecimal.ROUND_HALF_UP),
                    r.getResult().getMaxDrawdown().setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        printTable(allResults);
    }

    private static StrategyBackTestResult backtest(TradeStrategy strategy,
                                                    KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h,
                                                    int[] align1h, int[] align4h) {
        List<BackTradeRecord> tradeList = new ArrayList<>();
        List<BigDecimal> equityCurve = new ArrayList<>();

        int holdDir = 0;
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal equity = BigDecimal.ZERO;
        int longCount = 0, shortCount = 0, neutralCount = 0;
        equityCurve.add(equity);

        for (int i = 60; i < k15m.length; i++) {
            KLineVO[] s15m = slice(k15m, i, 100);
            KLineVO[] s1h  = KLineAligner.slice(k1h, align1h[i], 60);
            KLineVO[] s4h  = KLineAligner.slice(k4h, align4h[i], 30);

            int sig = strategy.signal(s15m, s1h, s4h);
            if (sig == 1) longCount++;
            else if (sig == -1) shortCount++;
            else neutralCount++;

            BigDecimal close = BigDecimal.valueOf(k15m[i].getClose());

            // 平仓
            if (holdDir != 0 && (sig == 0 || sig != holdDir)) {
                BigDecimal pnl = holdDir == 1
                        ? close.subtract(openPrice).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP)
                        : openPrice.subtract(close).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
                BackTradeRecord rec = new BackTradeRecord();
                rec.setOpenPrice(openPrice);
                rec.setClosePrice(close);
                rec.setProfitRate(pnl);
                rec.setWin(pnl.compareTo(BigDecimal.ZERO) > 0);
                tradeList.add(rec);
                equity = equity.add(pnl);
                equityCurve.add(equity);
                holdDir = 0;
            }

            // 开仓
            if (holdDir == 0 && sig != 0) {
                holdDir = sig;
                openPrice = close;
            } else {
                equityCurve.add(equity);
            }
        }

        // 强制平最后一笔
        if (holdDir != 0) {
            BigDecimal close = BigDecimal.valueOf(k15m[k15m.length - 1].getClose());
            BigDecimal pnl = holdDir == 1
                    ? close.subtract(openPrice).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP)
                    : openPrice.subtract(close).divide(openPrice, 8, BigDecimal.ROUND_HALF_UP);
            BackTradeRecord rec = new BackTradeRecord();
            rec.setOpenPrice(openPrice);
            rec.setClosePrice(close);
            rec.setProfitRate(pnl);
            rec.setWin(pnl.compareTo(BigDecimal.ZERO) > 0);
            tradeList.add(rec);
            equityCurve.add(equity.add(pnl));
        }

        if (PRINT_SIGNAL_STATS) {
            System.out.printf("    [信号] 做多：%d  做空：%d  观望：%d%n",
                    longCount, shortCount, neutralCount);
        }

        BackTestResult result = BackTestCalculator.calculate(tradeList, equityCurve, RISK_FREE_RATE);
        BigDecimal totalProfitRate = equityCurve.get(equityCurve.size() - 1)
                .multiply(BigDecimal.valueOf(100));
        return new StrategyBackTestResult(strategy.name(), result, totalProfitRate);
    }

    private static void printTable(List<StrategyBackTestResult> results) {
        System.out.println("\n" + "=".repeat(150));
        System.out.printf("%-35s %8s %12s %10s %12s %15s %10s%n",
                "策略名称", "交易笔数", "总收益率(%)", "胜率(%)", "盈亏比", "最大回撤(%)", "夏普比率");
        System.out.println("-".repeat(150));

        results.stream()
                .sorted((a, b) -> b.getTotalProfitRate().compareTo(a.getTotalProfitRate()))
                .forEach(r -> {
                    BackTestResult res = r.getResult();
                    String cnt = res.getTotalTrades() == 0 ? "⚠️0" : String.valueOf(res.getTotalTrades());
                    System.out.printf("%-35s %8s %12.2f %10.2f %12.2f %15.2f %10.2f%n",
                            r.getStrategyName(), cnt,
                            r.getTotalProfitRate().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(),
                            res.getWinRate().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(),
                            res.getProfitLossRatio().doubleValue(),
                            res.getMaxDrawdown().setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(),
                            res.getSharpeRatio().doubleValue());
                });

        System.out.println("=".repeat(150));
        long valid = results.stream().filter(r -> r.getResult().getTotalTrades() > 0).count();
        System.out.printf("回测完成：有效策略 %d 个，无交易策略 %d 个%n", valid, results.size() - valid);
    }

    private static KLineVO[] slice(KLineVO[] arr, int index, int need) {
        int start = Math.max(0, index - need + 1);
        int len = index - start + 1;
        KLineVO[] res = new KLineVO[len];
        System.arraycopy(arr, start, res, 0, len);
        return res;
    }

    static class StrategyBackTestResult {
        private final String strategyName;
        private final BackTestResult result;
        private final BigDecimal totalProfitRate;

        StrategyBackTestResult(String name, BackTestResult result, BigDecimal totalProfitRate) {
            this.strategyName = name;
            this.result = result;
            this.totalProfitRate = totalProfitRate;
        }

        String getStrategyName() { return strategyName; }
        BackTestResult getResult() { return result; }
        BigDecimal getTotalProfitRate() { return totalProfitRate; }
    }
}
