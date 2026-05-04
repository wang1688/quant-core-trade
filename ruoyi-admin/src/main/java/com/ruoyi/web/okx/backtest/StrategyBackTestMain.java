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

    // 回测配置
    private static final String API_KEY = "api-key: 56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String PASSPHRASE = "King$168$hello#168#";
    private static final boolean SIMULATED = true;
    private static final String INST_ID = "BTC-USDT-SWAP";
    private static final String BAR = "15m";

    public static void main(String[] args) throws Exception {
        // 1. 初始化你原始的OkxApiClient
        HttpClient httpClient = HttpClient.newHttpClient();
        OkxApiClient okxClient = new OkxApiClient(API_KEY, API_SECRET, PASSPHRASE, SIMULATED, httpClient);

        // 2. 拉取2年真实K线（完全匹配你的KLineVO）
        System.out.println("正在从OKX接口拉取2年历史K线数据...");
        KLineVO[] kLineArray = okxClient.getTwoYearsKLineArray(INST_ID, BAR);
        System.out.println("K线数据拉取完成，共 " + kLineArray.length + " 根");

        // 3. 初始化你的策略（直接替换成你任意策略）
        TradeStrategy strategy = new TrendStrengthStrategy();
        System.out.println("正在执行策略回测：" + strategy.name());

        // 4. 回测执行
        List<BackTradeRecord> tradeList = new ArrayList<>();
        int holdPosition = 0;
        BigDecimal openPrice = BigDecimal.ZERO;

        for (int i = 60; i < kLineArray.length; i++) {
            KLineVO[] currentKLine = new KLineVO[i + 1];
            System.arraycopy(kLineArray, 0, currentKLine, 0, i + 1);

            int signal = strategy.signal(currentKLine, null, null);
            // 从你的KLineVO取double，转BigDecimal计算
            BigDecimal currentClose = BigDecimal.valueOf(kLineArray[i].getClose());

            if (holdPosition == 0 && signal == 1) {
                holdPosition = 1;
                openPrice = currentClose;
            } else if (holdPosition == 0 && signal == -1) {
                holdPosition = -1;
                openPrice = currentClose;
            } else if (holdPosition != 0 && signal == 0) {
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

                holdPosition = 0;
                openPrice = BigDecimal.ZERO;
            }
        }

        // 5. 计算指标
        BackTestResult result = BackTestCalculator.calculate(tradeList);

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