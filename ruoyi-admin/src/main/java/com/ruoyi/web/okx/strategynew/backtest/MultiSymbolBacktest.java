package com.ruoyi.web.okx.strategynew.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategynew.model.KLine;
import java.util.ArrayList;
import java.util.List;

/**
 * 多品种对比测试
 */
public class MultiSymbolBacktest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("多品种对比回测");
        System.out.println("========================================");

        String[] symbols = {"BTC", "ETH"};
        String[] instIds = {"BTC-USDT-SWAP", "ETH-USDT-SWAP"};
        double initialCapital = 10000.0;

        OkxApiClient okxClient = createOkxClient();

        List<BacktestResult> results = new ArrayList<>();

        for (int i = 0; i < symbols.length; i++) {
            String symbol = symbols[i];
            String instId = instIds[i];

            System.out.println("\n========================================");
            System.out.println("正在回测: " + symbol);
            System.out.println("========================================");

            try {
                List<KLine> klines15M = DataLoader.loadFromOkx(okxClient, instId, "15m");
                List<KLine> klines1H = DataLoader.loadFromOkx(okxClient, instId, "1H");
                List<KLine> klines4H = DataLoader.loadFromOkx(okxClient, instId, "4H");
                List<KLine> klines5M = DataLoader.loadFromOkx(okxClient, instId, "5m");
                List<KLine> klines1M = DataLoader.loadFromOkx(okxClient, instId, "1m");

                if (klines15M.isEmpty()) {
                    System.err.println("无法加载 " + symbol + " 数据，跳过");
                    continue;
                }

                BacktestEngine engine = new BacktestEngine(symbol, initialCapital);
                BacktestResult result = engine.runBacktest(klines4H, klines1H, klines15M, klines5M, klines1M);

                results.add(result);

                result.printReport();
            } catch (Exception e) {
                System.err.println("回测 " + symbol + " 失败: " + e.getMessage());
                e.printStackTrace();
            }
        }

        printComparison(results);
    }

    /**
     * 打印对比结果
     */
    private static void printComparison(List<BacktestResult> results) {
        if (results.isEmpty()) {
            return;
        }

        System.out.println("\n========================================");
        System.out.println("品种对比");
        System.out.println("========================================");

        System.out.println(String.format("%-10s %-10s %-15s %-10s %-10s %-15s %-15s",
            "品种", "交易笔数", "总收益率(%)", "胜率(%)", "盈亏比", "最大回撤(%)", "夏普比率"));
        System.out.println("----------------------------------------");

        for (BacktestResult result : results) {
            System.out.println(String.format("%-10s %-10d %-15.2f %-10.2f %-10.2f %-15.2f %-15.2f",
                result.getSymbol(),
                result.getTotalTrades(),
                result.getTotalReturnPercent(),
                result.getWinRate(),
                result.getProfitLossRatio(),
                result.getMaxDrawdownPercent().doubleValue(),
                PerformanceMetrics.calculateSharpeRatio(result)
            ));
        }

        System.out.println("========================================");

        BacktestResult best = findBestStrategy(results);
        if (best != null) {
            System.out.println("\n最佳品种: " + best.getSymbol());
            System.out.println("综合评分最高，收益风险比最优");
        }
    }

    /**
     * 找出最佳策略
     */
    private static BacktestResult findBestStrategy(List<BacktestResult> results) {
        BacktestResult best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (BacktestResult result : results) {
            double score = calculateScore(result);
            if (score > bestScore) {
                bestScore = score;
                best = result;
            }
        }

        return best;
    }

    /**
     * 计算综合评分
     */
    private static double calculateScore(BacktestResult result) {
        double returnScore = result.getTotalReturnPercent() * 0.3;
        double winRateScore = result.getWinRate() * 0.2;
        double sharpeScore = PerformanceMetrics.calculateSharpeRatio(result) * 10 * 0.3;
        double drawdownScore = (100 - result.getMaxDrawdownPercent().doubleValue()) * 0.2;

        return returnScore + winRateScore + sharpeScore + drawdownScore;
    }

    /**
     * 创建OKX API客户端
     */
    private static OkxApiClient createOkxClient() {
        String apiKey = System.getenv("OKX_API_KEY");
        String secretKey = System.getenv("OKX_SECRET_KEY");
        String passphrase = System.getenv("OKX_PASSPHRASE");

        if (apiKey == null || secretKey == null || passphrase == null) {
            System.out.println("警告: 未配置OKX API密钥");
            apiKey = "";
            secretKey = "";
            passphrase = "";
        }

        String proxyHost = System.getenv("PROXY_HOST");
        String proxyPortStr = System.getenv("PROXY_PORT");
        int proxyPort = 0;

        if (proxyPortStr != null && !proxyPortStr.isEmpty()) {
            try {
                proxyPort = Integer.parseInt(proxyPortStr);
            } catch (NumberFormatException e) {
                System.out.println("代理端口配置错误");
            }
        }

        return new OkxApiClient(apiKey, secretKey, passphrase, false, null, proxyHost, proxyPort);
    }
}
