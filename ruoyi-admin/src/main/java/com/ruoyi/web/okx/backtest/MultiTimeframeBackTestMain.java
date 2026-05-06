package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategy.*;
import com.ruoyi.web.okx.vo.KLineVO;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 多级别中枢自适应系统 — 多时间框架回测入口
 * 使用 MultiTimeframeBacktestEngine（时间戳对齐，修复版）
 */
public class MultiTimeframeBackTestMain {

    private static final boolean USE_PROXY  = true;
    private static final String  PROXY_HOST = "127.0.0.1";
    private static final int     PROXY_PORT = 7890;

    private static final String  API_KEY    = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String  API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String  PASSPHRASE = "King$168$hello#168#";
    private static final boolean SIMULATED  = true;

    private static final String  INST_ID = "BTC-USDT-SWAP";
    private static final boolean IS_BTC  = true;

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

        System.out.println("拉取 15M K线...");
        KLineVO[] k15m = client.getTwoYearsKLineArray(INST_ID, "15m");
        System.out.println("15M：" + k15m.length + " 根");

        System.out.println("拉取 1H K线...");
        KLineVO[] k1h = client.getTwoYearsKLineArray(INST_ID, "1H");
        System.out.println("1H ：" + k1h.length + " 根");

        System.out.println("拉取 4H K线...");
        KLineVO[] k4h = client.getTwoYearsKLineArray(INST_ID, "4H");
        System.out.println("4H ：" + k4h.length + " 根\n");

        List<TradeStrategy> strategies = buildStrategies();
        System.out.println("共 " + strategies.size() + " 个策略\n");

        MultiTimeframeBacktestEngine engine = new MultiTimeframeBacktestEngine();
        List<MultiTimeframeBacktestEngine.Report> reports = new ArrayList<>();

        for (int i = 0; i < strategies.size(); i++) {
            TradeStrategy s = strategies.get(i);
            System.out.printf("[%d/%d] %s%n", i + 1, strategies.size(), s.name());
            MultiTimeframeBacktestEngine.Report r = engine.test(s, k15m, k1h, k4h);
            reports.add(r);
            System.out.printf("      交易：%d笔  胜率：%.1f%%  收益：%.2f%%  回撤：%.2f%%%n%n",
                    r.getTotalTrades(),
                    r.getWinRate() * 100,
                    r.getProfitRate() * 100,
                    r.getMaxDrawdown() * 100);
        }

        printRanking(reports);
    }

    private static List<TradeStrategy> buildStrategies() {
        List<TradeStrategy> list = new ArrayList<>();
        list.add(new MasterStrategy(IS_BTC));
        list.add(new CoreTrendStrategy(IS_BTC));
        list.add(new TrendStrengthStrategy(IS_BTC));
        list.add(new TrendAccelerationStrategy(IS_BTC));
        list.add(new TrendReversalStrategy(IS_BTC));
        list.add(new RangeOscillationStrategy(IS_BTC));
        list.add(new FakeBreakoutStrategy(IS_BTC));
        list.add(new NakedKStrategy());
        list.add(new TrialPositionStrategy(IS_BTC));
        return list;
    }

    private static void printRanking(List<MultiTimeframeBacktestEngine.Report> reports) {
        System.out.println("\n" + "=".repeat(110));
        System.out.printf("%-35s %8s %8s %10s %10s%n",
                "策略名称", "交易笔数", "胜率(%)", "总收益(%)", "最大回撤(%)");
        System.out.println("-".repeat(110));

        reports.stream()
                .sorted(Comparator.comparingDouble(MultiTimeframeBacktestEngine.Report::getProfitRate).reversed())
                .forEach(r -> System.out.printf("%-35s %8d %8.1f %10.2f %10.2f%n",
                        r.getStrategy(),
                        r.getTotalTrades(),
                        r.getWinRate() * 100,
                        r.getProfitRate() * 100,
                        r.getMaxDrawdown() * 100));

        System.out.println("=".repeat(110));
    }
}
