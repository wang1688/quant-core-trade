package com.ruoyi.web.okx.strategynew.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.OkxConfig;
import com.ruoyi.web.okx.strategynew.model.KLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * Spring Boot回测启动类
 * 可以直接运行，自动注入OkxApiClient
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.ruoyi.web.okx"})
public class BacktestApplication implements CommandLineRunner {

    @Autowired(required = false)
    private OkxApiClient okxApiClient;

    public static void main(String[] args) {
        SpringApplication.run(BacktestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("加密货币量化交易策略回测系统");
        System.out.println("========================================");

        String symbol = "BTC";
        String instId = "BTC-USDT-SWAP";
        double initialCapital = 10000.0;

        if (okxApiClient == null) {
            System.err.println("错误: OkxApiClient未注入，请检查配置");
            System.err.println("请在application.yml中配置OKX API密钥");
            return;
        }

        System.out.println("\n正在加载历史数据...");

        List<KLine> klines15M = DataLoader.loadFromOkx(okxApiClient, instId, "15m");
        List<KLine> klines1H = DataLoader.loadFromOkx(okxApiClient, instId, "1H");
        List<KLine> klines4H = DataLoader.loadFromOkx(okxApiClient, instId, "4H");
        List<KLine> klines5M = DataLoader.loadFromOkx(okxApiClient, instId, "5m");
        List<KLine> klines1M = DataLoader.loadFromOkx(okxApiClient, instId, "1m");

        if (klines15M.isEmpty()) {
            System.err.println("错误: 未能加载到数据");
            return;
        }

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

        System.exit(0);
    }

    /**
     * 打印详细分析
     */
    private void printDetailedAnalysis(BacktestResult result) {
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
    }
}
