package com.ruoyi.web.okx.strategynew.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategynew.model.KLine;
import java.math.BigDecimal;
import java.util.List;

/**
 * 回测主程序
 */
public class BacktestMain {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("加密货币量化交易策略回测系统");
        System.out.println("========================================");

        String symbol = "BTC";
        String instId = "BTC-USDT-SWAP";
        double initialCapital = 10000.0;

        OkxApiClient okxClient = createOkxClient();

        System.out.println("\n正在加载历史数据...");

        List<KLine> klines15M = DataLoader.loadFromOkx(okxClient, instId, "15m");
        List<KLine> klines1H = DataLoader.loadFromOkx(okxClient, instId, "1H");
        List<KLine> klines4H = DataLoader.loadFromOkx(okxClient, instId, "4H");
        List<KLine> klines5M = DataLoader.loadFromOkx(okxClient, instId, "5m");
        List<KLine> klines1M = DataLoader.loadFromOkx(okxClient, instId, "1m");

        System.out.println("数据加载完成:");
        System.out.println("  4H K线: " + klines4H.size() + " 根");
        System.out.println("  1H K线: " + klines1H.size() + " 根");
        System.out.println("  15M K线: " + klines15M.size() + " 根");
        System.out.println("  5M K线: " + klines5M.size() + " 根");
        System.out.println("  1M K线: " + klines1M.size() + " 根");

        System.out.println("\n开始回测...");
        long startTime = System.currentTimeMillis();

        BacktestEngine engine = new BacktestEngine(symbol, initialCapital);
        BacktestResult result = engine.runBacktest(klines4H, klines1H, klines15M, klines5M, klines1M);

        long endTime = System.currentTimeMillis();
        System.out.println("回测完成，耗时: " + (endTime - startTime) / 1000.0 + " 秒\n");

        result.printReport();

        printDetailedAnalysis(result);

        PerformanceMetrics.printAllMetrics(result);

        exportResults(result, symbol);
    }

    /**
     * 导出结果
     */
    private static void exportResults(BacktestResult result, String symbol) {
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        );

        String baseDir = "backtest_results/" + symbol + "_" + timestamp + "/";
        new java.io.File(baseDir).mkdirs();

        BacktestExporter.exportTradesToCSV(result, baseDir + "trades.csv");
        BacktestExporter.exportEquityCurve(result, baseDir + "equity_curve.csv");
        BacktestExporter.exportFullReport(result, baseDir + "report.txt");
        BacktestExporter.exportStrategyStats(result, baseDir + "strategy_stats.csv");

        System.out.println("\n所有结果已导出到: " + baseDir);
    }

    /**
     * 创建OKX API客户端
     */
    private static OkxApiClient createOkxClient() {
        String apiKey = System.getenv("OKX_API_KEY");
        String secretKey = System.getenv("OKX_SECRET_KEY");
        String passphrase = System.getenv("OKX_PASSPHRASE");

        if (apiKey == null || secretKey == null || passphrase == null) {
            System.out.println("警告: 未配置OKX API密钥，请设置环境变量:");
            System.out.println("  OKX_API_KEY");
            System.out.println("  OKX_SECRET_KEY");
            System.out.println("  OKX_PASSPHRASE");
            System.out.println("使用默认配置（可能无法访问）");
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
                System.out.println("代理端口配置错误，不使用代理");
            }
        }

        return new OkxApiClient(apiKey, secretKey, passphrase, false, null, proxyHost, proxyPort);
    }

    /**
     * 打印详细分析
     */
    private static void printDetailedAnalysis(BacktestResult result) {
        System.out.println("\n========================================");
        System.out.println("详细分析");
        System.out.println("========================================");

        if (result.getTotalTrades() == 0) {
            System.out.println("无交易记录");
            return;
        }

        System.out.println("\n策略分布:");
        BacktestAnalyzer.analyzeByStrategy(result);

        System.out.println("\n方向分布:");
        BacktestAnalyzer.analyzeByDirection(result);

        System.out.println("\n持仓时间分析:");
        BacktestAnalyzer.analyzeHoldingTime(result);

        System.out.println("\n最佳/最差交易:");
        BacktestAnalyzer.analyzeBestWorstTrades(result);

        BacktestAnalyzer.analyzeConsecutiveWinsLosses(result);

        BacktestAnalyzer.analyzeMonthlyReturns(result);
    }
}
