package com.ruoyi.web.okx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategy.*;
import com.ruoyi.web.okx.util.KLineParser;
import com.ruoyi.web.okx.vo.KLineVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 实盘交易服务
 * 每15分钟执行一次信号检测，根据信号自动下单
 *
 * 使用前提：
 *   1. OkxApiClient 已注入（Spring Bean）
 *   2. application.yml 中配置 okx.instId / okx.isBtc / okx.equity
 *   3. 开启 @EnableScheduling
 */
@Service
public class LiveTradingService {

    @Autowired
    private OkxApiClient okxClient;

    @Autowired
    private PositionStateStore stateStore;

    private static final String  INST_ID  = "BTC-USDT-SWAP";
    private static final boolean IS_BTC   = true;

    private final MasterStrategy masterStrategy = new MasterStrategy(IS_BTC);
    private final TierManager    tierManager    = new TierManager(3, 8);

    // 当前持仓状态（启动时从文件恢复）
    private int    holdDirection = 0;
    private double entryPrice    = 0;
    private int    holdContracts = 0;
    private String holdPosId     = "";

    private final AtomicBoolean running = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        System.out.println("[LiveTradingService] 初始化完成，策略：" + masterStrategy.name());
        // 恢复持仓状态（防止重启后丢失）
        PositionStateStore.PositionState saved = stateStore.load();
        if (saved.hasPosition()) {
            holdDirection = saved.holdDirection;
            entryPrice    = saved.entryPrice;
            holdContracts = saved.holdContracts;
            holdPosId     = saved.posId != null ? saved.posId : "";
            System.out.printf("[LiveTradingService] 恢复持仓状态：%s  入场价=%.2f  张数=%d%n",
                    holdDirection == 1 ? "多头" : "空头", entryPrice, holdContracts);
        }
    }

    /**
     * 每15分钟执行一次（与K线周期对齐）
     */
    @Scheduled(cron = "0 0/15 * * * ?")
    public void onKlineClose() {
        if (!running.compareAndSet(false, true)) {
            System.out.println("[LiveTradingService] 上一轮未完成，跳过本次");
            return;
        }
        try {
            execute();
        } catch (Exception e) {
            System.err.println("[LiveTradingService] 执行异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            running.set(false);
        }
    }

    private void execute() throws Exception {
        KLineVO[] k15m = fetchRecent(INST_ID, "15m", 200);
        KLineVO[] k1h  = fetchRecent(INST_ID, "1H",  100);
        KLineVO[] k4h  = fetchRecent(INST_ID, "4H",   60);

        if (k15m == null || k15m.length < 60) {
            System.out.println("[LiveTradingService] K线数据不足，跳过");
            return;
        }

        tierManager.checkCircuitBreaker(k15m, IS_BTC);
        if (tierManager.isCircuitBroken()) {
            System.out.println("[LiveTradingService] 熔断触发，禁止开仓");
            if (holdDirection != 0) closePosition(k15m, "熔断强平");
            return;
        }

        masterStrategy.updateRiskState(
                tierManager.getDailyLossPct(),
                tierManager.getConsecutiveLoss(),
                tierManager.getMaxDrawdownPct());

        int signal = masterStrategy.signal(k15m, k1h, k4h);

        System.out.println(SignalMonitor.oneLiner(k15m, k1h, k4h, IS_BTC));

        if (holdDirection != 0) {
            boolean shouldClose = (signal != 0 && signal != holdDirection) || signal == 0;
            if (shouldClose) {
                int prevDir = holdDirection;
                closePosition(k15m, signal != 0 ? "信号反转" : "信号归零");
                if (signal != 0 && signal != prevDir) {
                    openPosition(signal, k15m);
                }
                return;
            }
        }

        if (holdDirection == 0 && signal != 0) {
            openPosition(signal, k15m);
        }
    }

    private void openPosition(int direction, KLineVO[] k15m) throws Exception {
        int tier = tierManager.getCurrentTier();
        if (tier <= 0) return;

        double close = k15m[k15m.length - 1].getClose();
        double equity = fetchUsdtEquity();
        RiskManager.PositionResult pos = RiskManager.calculateFromKline(tier, equity, k15m, IS_BTC);
        if (!pos.canOpen || pos.contracts <= 0) return;

        String side    = direction == 1 ? "buy" : "sell";
        String posSide = direction == 1 ? "long" : "short";

        System.out.printf("[开仓] %s  价格=%.2f  张数=%d  档位=%d档  风险=%.1f%%%n",
                direction == 1 ? "做多" : "做空", close, pos.contracts, tier, pos.riskPct);

        String orderResult = okxClient.placeOrderWithPosSide(
                INST_ID, "cross", side, "market",
                String.valueOf(pos.contracts), null, posSide);
        System.out.println("[开仓结果] " + orderResult);

        holdDirection = direction;
        entryPrice    = close;
        holdContracts = pos.contracts;
        holdPosId     = "";

        // 查询持仓ID，用于设置止盈止损
        String posId = queryPosId(INST_ID, posSide);
        holdPosId = posId;

        // 设置止盈止损（2:1盈亏比）
        if (!posId.isEmpty()) {
            double slPx = RiskManager.stopLossPrice(close, direction, pos.stopLoss);
            double tpPx = RiskManager.takeProfitPrice(close, direction, pos.stopLoss);
            String slTpResult = okxClient.setPosSlTp(
                    INST_ID, posId,
                    String.format("%.2f", slPx),
                    String.format("%.2f", tpPx));
            System.out.printf("[止盈止损] SL=%.2f  TP=%.2f  结果=%s%n", slPx, tpPx, slTpResult);
        }

        // 持久化持仓状态
        stateStore.save(holdDirection, entryPrice, holdContracts, holdPosId);
    }

    private void closePosition(KLineVO[] k15m, String reason) throws Exception {
        if (holdDirection == 0) return;

        double close = k15m[k15m.length - 1].getClose();
        String side    = holdDirection == 1 ? "sell" : "buy";
        String posSide = holdDirection == 1 ? "long" : "short";

        double pnlPct = holdDirection == 1
                ? (close - entryPrice) / entryPrice
                : (entryPrice - close) / entryPrice;

        System.out.printf("[平仓] 原因=%s  价格=%.2f  收益=%.2f%%%n",
                reason, close, pnlPct * 100);

        String result = okxClient.placeOrderWithPosSide(
                INST_ID, "cross", side, "market",
                String.valueOf(holdContracts), null, posSide);
        System.out.println("[平仓结果] " + result);

        tierManager.onTradeClose(pnlPct);

        holdDirection = 0;
        entryPrice    = 0;
        holdContracts = 0;
        holdPosId     = "";

        // 清除持久化状态
        stateStore.clear();
    }

    /**
     * 查询当前持仓ID（用于设置止盈止损）
     * OKX getPositions 返回的 posId 字段
     */
    private String queryPosId(String instId, String posSide) {
        try {
            String json = okxClient.getPositions("SWAP", instId);
            JSONObject resp = JSON.parseObject(json);
            if (!"0".equals(resp.getString("code"))) return "";
            JSONArray data = resp.getJSONArray("data");
            if (data == null || data.isEmpty()) return "";
            for (int i = 0; i < data.size(); i++) {
                JSONObject p = data.getJSONObject(i);
                if (posSide.equals(p.getString("posSide"))) {
                    return p.getString("posId");
                }
            }
        } catch (Exception e) {
            System.err.println("[LiveTradingService] 查询posId失败：" + e.getMessage());
        }
        return "";
    }

    private KLineVO[] fetchRecent(String instId, String bar, int limit) throws Exception {
        String json = okxClient.getCandlesticks(instId, bar, String.valueOf(Math.min(limit, 300)));
        return KLineParser.parse(json);
    }

    /** 查询账户 USDT 权益，失败时回退到默认值 10000 */
    private double fetchUsdtEquity() {
        try {
            String json = okxClient.getAccountBalance("USDT");
            JSONObject resp = JSON.parseObject(json);
            if (!"0".equals(resp.getString("code"))) return 10000.0;
            JSONArray details = resp.getJSONArray("data")
                    .getJSONObject(0).getJSONArray("details");
            if (details == null || details.isEmpty()) return 10000.0;
            for (int i = 0; i < details.size(); i++) {
                JSONObject d = details.getJSONObject(i);
                if ("USDT".equals(d.getString("ccy"))) {
                    double eq = d.getDoubleValue("eq");
                    return eq > 0 ? eq : 10000.0;
                }
            }
        } catch (Exception e) {
            System.err.println("[LiveTradingService] 查询权益失败，使用默认值：" + e.getMessage());
        }
        return 10000.0;
    }

    public void triggerManually() throws Exception {
        execute();
    }

    // ==================== 状态查询 ====================

    public int getHoldDirection()  { return holdDirection; }
    public double getEntryPrice()  { return entryPrice; }
    public int getHoldContracts()  { return holdContracts; }
    public String getHoldPosId()   { return holdPosId; }
    public TierManager getTierManager() { return tierManager; }

    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyReset() {
        tierManager.dailyReset();
        masterStrategy.resetDailyState();
        System.out.println("[LiveTradingService] 每日状态已重置");
    }
}
