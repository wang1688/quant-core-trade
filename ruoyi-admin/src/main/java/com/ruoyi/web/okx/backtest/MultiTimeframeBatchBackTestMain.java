package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;

import java.math.BigDecimal;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 多时间框架批量回测入口
 * 自动扫描 strategy 包下所有策略，使用 MultiTimeframeBacktestEngine 回测
 */
public class MultiTimeframeBatchBackTestMain {

    private static final boolean USE_PROXY  = true;
    private static final String  PROXY_HOST = "127.0.0.1";
    private static final int     PROXY_PORT = 7890;

    private static final String  API_KEY    = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String  API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String  PASSPHRASE = "King$168$hello#168#";
    private static final boolean SIMULATED  = true;

    private static final String  INST_ID    = "BTC-USDT-SWAP";
    private static final String  STRATEGY_PACKAGE = "com.ruoyi.web.okx.strategy";

    // 回测参数
    private static final double  RISK_FREE_RATE        = 0.02;
    private static final boolean PRINT_SIGNAL_STATS    = true;

    public static void main(String[] args) throws Exception {
        HttpClient httpClient = HttpClient.newHttpClient();
        OkxApiClient client;
        if (USE_PROXY) {
            System.out.println("代理连接 OKX：" + PROXY_HOST + ":" + PROXY_PORT);
            client = new OkxApiClient(API_KEY, API_SECRET, PASSPHRASE, SIMULATED,
                    httpClient, PROXY_HOST, PROXY_PORT);
        } else {
            client = new OkxApiClient(API_KEY, API_SECRET, PASSPHRASE, SIMULATED, httpClient);
        }

        // 拉取三个时间框架
        System.out.println("拉取 15M K线...");
        KLineVO[] k15m = client.getTwoYearsKLineArray(INST_ID, "15m");
        System.out.println("15M：" + k15m.length + " 根");

        System.out.println("拉取 1H K线...");
        KLineVO[] k1h = client.getTwoYearsKLineArray(INST_ID, "1H");
        System.out.println("1H ：" + k1h.length + " 根");

        System.out.println("拉取 4H K线...");
        KLineVO[] k4h = client.getTwoYearsKLineArray(INST_ID, "4H");
        System.out.println("4H ：" + k4h.length + " 根\n");

        // 扫描策略
        System.out.println("扫描策略包：" + STRATEGY_PACKAGE);
        List<TradeStrategy> strategies = StrategyScanner.scanStrategies(STRATEGY_PACKAGE);
        System.out.println("发现 " + strategies.size() + " 个策略\n");

        // 批量回测
        MultiTimeframeBacktestEngine engine = new MultiTimeframeBacktestEngine();
        List<MultiTimeframeBacktestEngine.Report> reports = new ArrayList<>();

        for (int i = 0; i < strategies.size(); i++) {
            TradeStrategy s = strategies.get(i);
            System.out.printf("[%d/%d] %s%n", i + 1, strategies.size(), s.name());
            MultiTimeframeBacktestEngine.Report r = engine.test(s, k15m, k1h, k4h);
            reports.add(r);

            String status = r.getTotalTrades() == 0 ? "⚠️ 无交易" : "✅ 完成";
            System.out.printf("    %s  交易：%d笔  胜率：%.1f%%  收益：%.2f%%  回撤：%.2f%%%n%n",
                    status,
                    r.getTotalTrades(),
                    r.getWinRate() * 100,
                    r.getProfitRate() * 100,
                    r.getMaxDrawdown() * 100);
        }

        printTable(reports);
    }

    private static void printTable(List<MultiTimeframeBacktestEngine.Report> reports) {
        System.out.println("\n" + "=".repeat(120));
        System.out.printf("%-35s %8s %10s %8s %12s %10s%n",
                "策略名称", "交易笔数", "总收益(%)", "胜率(%)", "最大回撤(%)", "盈亏比");
        System.out.println("-".repeat(120));

        reports.stream()
                .sorted(Comparator.comparingDouble(MultiTimeframeBacktestEngine.Report::getProfitRate).reversed())
                .forEach(r -> {
                    String trades = r.getTotalTrades() == 0 ? "⚠️0" : String.valueOf(r.getTotalTrades());
                    System.out.printf("%-35s %8s %10.2f %8.1f %12.2f%n",
                            r.getStrategy(),
                            trades,
                            r.getProfitRate() * 100,
                            r.getWinRate() * 100,
                            r.getMaxDrawdown() * 100);
                });

        System.out.println("=".repeat(120));

        long valid = reports.stream().filter(r -> r.getTotalTrades() > 0).count();
        System.out.printf("回测完成：有效策略 %d 个，无交易策略 %d 个%n", valid, reports.size() - valid);
    }
}
