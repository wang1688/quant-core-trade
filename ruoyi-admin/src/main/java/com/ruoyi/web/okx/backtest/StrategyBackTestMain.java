package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategy.TrendStrengthStrategy;
import com.ruoyi.web.okx.strategy.TradeStrategy;
import com.ruoyi.web.okx.vo.KLineVO;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

/**
 * 单策略回测入口（升级版）
 * 使用三级别K线 + MultiTimeframeBacktestEngine
 */
public class StrategyBackTestMain {

    private static final boolean USE_PROXY  = true;
    private static final String  PROXY_HOST = "127.0.0.1";
    private static final int     PROXY_PORT = 7890;

    private static final String  API_KEY    = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String  API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String  PASSPHRASE = "King$168$hello#168#";
    private static final boolean SIMULATED  = true;
    private static final String  INST_ID    = "BTC-USDT-SWAP";

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

        TradeStrategy strategy = new TrendStrengthStrategy(true);
        System.out.println("回测策略：" + strategy.name());

        MultiTimeframeBacktestEngine engine = new MultiTimeframeBacktestEngine();
        MultiTimeframeBacktestEngine.Report r = engine.test(strategy, k15m, k1h, k4h);

        // 同时用旧版 BackTestCalculator 输出详细指标
        List<BackTradeRecord> tradeList = new ArrayList<>();
        List<BigDecimal> equityCurve = new ArrayList<>();
        runDetailedBacktest(strategy, k15m, k1h, k4h, tradeList, equityCurve);
        BackTestResult result = BackTestCalculator.calculate(tradeList, equityCurve);

        System.out.println("\n==================== 回测结果 ====================");
        System.out.println("策略名称：" + strategy.name());
        System.out.println("交易对：" + INST_ID + "  周期：15M/1H/4H  回测：2年");
        System.out.println("总交易笔数：" + r.getTotalTrades());
        System.out.printf("胜率：%.2f%%%n", r.getWinRate() * 100);
        System.out.printf("总收益：%.2f%%%n", r.getProfitRate() * 100);
        System.out.printf("最大回撤：%.2f%%%n", r.getMaxDrawdown() * 100);
        System.out.println("盈亏比：" + result.getProfitLossRatio());
        System.out.println("夏普比率：" + result.getSharpeRatio());
        System.out.println("===================================================");
    }

    /**
     * 详细回测（用于 BackTestCalculator 计算夏普/盈亏比等高级指标）
     */
    private static void runDetailedBacktest(TradeStrategy strategy,
                                             KLineVO[] k15m, KLineVO[] k1h, KLineVO[] k4h,
                                             List<BackTradeRecord> tradeList,
                                             List<BigDecimal> equityCurve) {
        int[] align1h = KLineAligner.buildAlignTable(k15m, k1h);
        int[] align4h = KLineAligner.buildAlignTable(k15m, k4h);

        int holdDir = 0;
        BigDecimal openPrice = BigDecimal.ZERO;
        BigDecimal equity = BigDecimal.ZERO;
        equityCurve.add(equity);

        for (int i = 60; i < k15m.length; i++) {
            KLineVO[] s15m = slice(k15m, i, 100);
            KLineVO[] s1h  = KLineAligner.slice(k1h, align1h[i], 60);
            KLineVO[] s4h  = KLineAligner.slice(k4h, align4h[i], 30);

            int sig = strategy.signal(s15m, s1h, s4h);
            BigDecimal close = BigDecimal.valueOf(k15m[i].getClose());

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
    }

    private static KLineVO[] slice(KLineVO[] arr, int index, int need) {
        int start = Math.max(0, index - need + 1);
        int len = index - start + 1;
        KLineVO[] res = new KLineVO[len];
        System.arraycopy(arr, start, res, 0, len);
        return res;
    }
}
