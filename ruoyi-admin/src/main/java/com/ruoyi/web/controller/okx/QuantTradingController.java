package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategy.*;
import com.ruoyi.web.okx.util.KLineParser;
import com.ruoyi.web.okx.vo.KLineVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 多级别中枢自适应量化交易系统 — REST接口
 * 提供信号查询、仓位计算、手动触发等功能
 */
@RestController
@RequestMapping("/okx/quant")
public class QuantTradingController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // 注入实盘服务（可选，不影响信号查询接口）
    @Autowired(required = false)
    private com.ruoyi.web.okx.service.LiveTradingService liveTradingService;

    private static final String INST_ID_BTC = "BTC-USDT-SWAP";
    private static final String INST_ID_ETH = "ETH-USDT-SWAP";

    // ==================== 信号查询 ====================

    /**
     * 查询当前信号（含完整评分详情）
     * GET /okx/quant/signal?instId=BTC-USDT-SWAP
     */
    @GetMapping("/signal")
    public AjaxResult getSignal(@RequestParam(defaultValue = "BTC-USDT-SWAP") String instId) {
        try {
            boolean isBtc = instId.toUpperCase().contains("BTC");
            KLineVO[] k15m = fetchRecent(instId, "15m", 200);
            KLineVO[] k1h  = fetchRecent(instId, "1H",  100);
            KLineVO[] k4h  = fetchRecent(instId, "4H",   60);

            MasterStrategy master = new MasterStrategy(isBtc);
            int signal = master.signal(k15m, k1h, k4h);
            SignalScorer.Score score = master.scoreDetail(k15m, k1h, k4h);
            TrendAnalyzer.MultiTrend trend = TrendAnalyzer.analyze(k15m, k1h, k4h);
            WyckoffDetector.WyckoffResult wyckoff = WyckoffDetector.detect(k15m, isBtc);
            BoxDetector.Box box = BoxDetector.detectAuto(k15m, isBtc);

            double close = k15m[k15m.length - 1].getClose();
            double e6  = Indicators.ema(k15m, 6);
            double e13 = Indicators.ema(k15m, 13);
            double e30 = Indicators.ema(k15m, 30);
            double dif = Indicators.difDivergence(k15m);
            double atr = Indicators.atrPct(k15m, 14);
            double vr  = Indicators.volumeRatio(k15m, 20);

            Map<String, Object> data = new HashMap<>();
            data.put("instId", instId);
            data.put("price", close);
            data.put("signal", signal);
            data.put("signalText", signal == 1 ? "做多" : signal == -1 ? "做空" : "观望");

            // 指标
            Map<String, Object> indicators = new HashMap<>();
            indicators.put("ema6", round2(e6));
            indicators.put("ema13", round2(e13));
            indicators.put("ema30", round2(e30));
            indicators.put("difDivergence", round4(dif));
            indicators.put("atrPct", round3(atr));
            indicators.put("volumeRatio", round2(vr));
            data.put("indicators", indicators);

            // 趋势
            Map<String, Object> trendMap = new HashMap<>();
            trendMap.put("t4h", trend.t4h.name());
            trendMap.put("t1h", trend.t1h.name());
            trendMap.put("t15m", trend.t15m.name());
            trendMap.put("resonanceUp", trend.resonanceUp);
            trendMap.put("resonanceDown", trend.resonanceDown);
            trendMap.put("diverge", trend.diverge);
            data.put("trend", trendMap);

            // 评分
            Map<String, Object> scoreMap = new HashMap<>();
            scoreMap.put("total", score.total);
            scoreMap.put("trendScore", score.trendScore);
            scoreMap.put("volumeScore", score.volumeScore);
            scoreMap.put("structureScore", score.structureScore);
            scoreMap.put("volatilityScore", score.volatilityScore);
            scoreMap.put("emaScore", score.emaScore);
            scoreMap.put("canOpen", score.canOpen());
            scoreMap.put("highQuality", score.highQuality());
            data.put("score", scoreMap);

            // 箱体
            Map<String, Object> boxMap = new HashMap<>();
            boxMap.put("valid", box.valid);
            if (box.valid) {
                boxMap.put("high", round2(box.high));
                boxMap.put("low", round2(box.low));
                boxMap.put("height", round2(box.height()));
                boxMap.put("position", BoxDetector.pricePosition(box, close));
            }
            data.put("box", boxMap);

            // 威科夫
            Map<String, Object> wyckoffMap = new HashMap<>();
            wyckoffMap.put("phase", wyckoff.phase.name());
            wyckoffMap.put("spring", wyckoff.springDetected);
            wyckoffMap.put("upthrust", wyckoff.upthrustDetected);
            wyckoffMap.put("signal", wyckoff.signal);
            data.put("wyckoff", wyckoffMap);

            // 建议档位
            int tier = master.suggestTier(k15m, k1h, k4h);
            data.put("suggestedTier", tier);

            return success(data);
        } catch (Exception e) {
            return error("信号查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询仓位建议
     * GET /okx/quant/position?instId=BTC-USDT-SWAP&equity=10000&tier=3
     */
    @GetMapping("/position")
    public AjaxResult getPosition(
            @RequestParam(defaultValue = "BTC-USDT-SWAP") String instId,
            @RequestParam(defaultValue = "10000") double equity,
            @RequestParam(defaultValue = "0") int tier) {
        try {
            boolean isBtc = instId.toUpperCase().contains("BTC");
            KLineVO[] k15m = fetchRecent(instId, "15m", 100);

            // tier=0 时自动从信号评分推断
            if (tier == 0) {
                KLineVO[] k1h = fetchRecent(instId, "1H", 60);
                KLineVO[] k4h = fetchRecent(instId, "4H", 30);
                SignalScorer.Score sc = SignalScorer.score(k15m, k1h, k4h, isBtc);
                tier = RiskManager.tierFromScore(sc.total);
            }

            RiskManager.PositionResult pos = RiskManager.calculateFromKline(tier, equity, k15m, isBtc);
            double close = k15m[k15m.length - 1].getClose();

            Map<String, Object> data = new HashMap<>();
            data.put("instId", instId);
            data.put("price", round2(close));
            data.put("equity", equity);
            data.put("tier", pos.tier);
            data.put("riskPct", pos.riskPct);
            data.put("stopLossDistance", round2(pos.stopLoss));
            data.put("contracts", pos.contracts);
            data.put("canOpen", pos.canOpen);
            data.put("stopLossPrice", round2(RiskManager.stopLossPrice(close, 1, pos.stopLoss)));
            data.put("takeProfitPrice", round2(RiskManager.takeProfitPrice(close, 1, pos.stopLoss)));

            return success(data);
        } catch (Exception e) {
            return error("仓位计算失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发实盘交易（需要 LiveTradingService 已启动）
     * POST /okx/quant/trigger
     */
    @PostMapping("/trigger")
    public AjaxResult manualTrigger() {
        if (liveTradingService == null) {
            return error("LiveTradingService 未启动");
        }
        try {
            liveTradingService.triggerManually();
            return success("手动触发成功");
        } catch (Exception e) {
            return error("触发失败：" + e.getMessage());
        }
    }

    /**
     * 实盘当前状态（持仓、档位、熔断、浮盈）
     * GET /okx/quant/status
     */
    @GetMapping("/status")
    public AjaxResult getLiveStatus() {
        if (liveTradingService == null) {
            return error("LiveTradingService 未启动");
        }
        try {
            int dir       = liveTradingService.getHoldDirection();
            double entry  = liveTradingService.getEntryPrice();
            int contracts = liveTradingService.getHoldContracts();
            String posId  = liveTradingService.getHoldPosId();
            com.ruoyi.web.okx.strategy.TierManager tm = liveTradingService.getTierManager();

            Map<String, Object> data = new HashMap<>();
            data.put("holdDirection", dir);
            data.put("holdDirectionText", dir == 1 ? "多头" : dir == -1 ? "空头" : "空仓");
            data.put("entryPrice", round2(entry));
            data.put("holdContracts", contracts);
            data.put("posId", posId);

            // 浮盈（需要当前价格）
            if (dir != 0 && entry > 0) {
                KLineVO[] k15m = fetchRecent(INST_ID_BTC, "15m", 2);
                double close = k15m[k15m.length - 1].getClose();
                double pnlPct = dir == 1
                        ? (close - entry) / entry * 100
                        : (entry - close) / entry * 100;
                data.put("currentPrice", round2(close));
                data.put("unrealizedPnlPct", round2(pnlPct));
            }

            // 档位与风控
            Map<String, Object> tier = new HashMap<>();
            tier.put("currentTier", tm.getCurrentTier());
            tier.put("circuitBroken", tm.isCircuitBroken());
            tier.put("dailyLossPct", round2(tm.getDailyLossPct()));
            tier.put("consecutiveLoss", tm.getConsecutiveLoss());
            tier.put("maxDrawdownPct", round2(tm.getMaxDrawdownPct()));
            data.put("tier", tier);

            return success(data);
        } catch (Exception e) {
            return error("状态查询失败：" + e.getMessage());
        }
    }

    /**
     * 熔断状态检查
     * GET /okx/quant/breaker?instId=BTC-USDT-SWAP
     */
    @GetMapping("/breaker")
    public AjaxResult checkBreaker(
            @RequestParam(defaultValue = "BTC-USDT-SWAP") String instId,
            @RequestParam(defaultValue = "0") double dailyLossPct,
            @RequestParam(defaultValue = "0") int consecutiveLoss,
            @RequestParam(defaultValue = "0") double maxDrawdownPct) {
        try {
            boolean isBtc = instId.toUpperCase().contains("BTC");
            KLineVO[] k15m = fetchRecent(instId, "15m", 50);
            CircuitBreaker.BreakerState state = CircuitBreaker.check(
                    dailyLossPct, consecutiveLoss, maxDrawdownPct, k15m, isBtc);

            Map<String, Object> data = new HashMap<>();
            data.put("triggered", state.triggered);
            data.put("reason", state.reason);
            data.put("cooldownMinutes", state.cooldownMinutes);
            data.put("extremeVolatility", CircuitBreaker.extremeVolatility(k15m, isBtc));
            data.put("atrPct", round3(Indicators.atrPct(k15m, 14)));

            return success(data);
        } catch (Exception e) {
            return error("熔断检查失败：" + e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    private KLineVO[] fetchRecent(String instId, String bar, int limit) throws Exception {
        String json = okxApiClient.getCandlesticks(instId, bar, String.valueOf(Math.min(limit, 300)));
        return KLineParser.parse(json);
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
    private double round3(double v) { return Math.round(v * 1000.0) / 1000.0; }
    private double round4(double v) { return Math.round(v * 10000.0) / 10000.0; }
}
