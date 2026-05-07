package com.ruoyi.web.okx.strategynew;

import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.enums.Timeframe;
import com.ruoyi.web.okx.strategynew.enums.VolatilityLevel;
import com.ruoyi.web.okx.strategynew.indicator.IndicatorCalculator;
import com.ruoyi.web.okx.strategynew.indicator.VolatilityAnalyzer;
import com.ruoyi.web.okx.strategynew.model.KLine;
import com.ruoyi.web.okx.strategynew.model.Position;
import com.ruoyi.web.okx.strategynew.model.Signal;
import com.ruoyi.web.okx.strategynew.risk.PositionManager;
import com.ruoyi.web.okx.strategynew.risk.RiskController;
import com.ruoyi.web.okx.strategynew.strategy.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主交易引擎
 */
public class TradingEngine {

    private String symbol;
    private double totalCapital;
    private RiskController riskController;
    private PositionManager positionManager;
    private Map<String, Position> positions;

    private CoreTrendStrategy coreTrendStrategy;
    private RangeOscillationStrategy rangeOscillationStrategy;
    private TrendReversalStrategy trendReversalStrategy;
    private TrendAccelerationStrategy trendAccelerationStrategy;
    private FakeBreakoutStrategy fakeBreakoutStrategy;
    private NakedKStrategy nakedKStrategy;

    private boolean circuitBreakerActive;
    private VolatilityLevel currentVolatilityLevel;

    public TradingEngine(String symbol, double totalCapital) {
        this.symbol = symbol;
        this.totalCapital = totalCapital;
        this.riskController = new RiskController(totalCapital, 1);
        this.positionManager = new PositionManager(totalCapital, symbol);
        this.positions = new HashMap<>();

        this.coreTrendStrategy = new CoreTrendStrategy(symbol);
        this.rangeOscillationStrategy = new RangeOscillationStrategy(symbol);
        this.trendReversalStrategy = new TrendReversalStrategy(symbol);
        this.trendAccelerationStrategy = new TrendAccelerationStrategy(symbol);
        this.fakeBreakoutStrategy = new FakeBreakoutStrategy(symbol);
        this.nakedKStrategy = new NakedKStrategy(symbol);

        this.circuitBreakerActive = false;
    }

    /**
     * 主分析入口
     */
    public List<Signal> analyze(List<KLine> klines4H, List<KLine> klines1H,
                                 List<KLine> klines15M, List<KLine> klines5M, List<KLine> klines1M) {
        List<Signal> signals = new ArrayList<>();

        if (!validateData(klines4H, klines1H, klines15M, klines5M)) {
            return signals;
        }

        updateVolatilityLevel(klines15M);

        if (circuitBreakerActive) {
            return signals;
        }

        Signal coreTrendSignal = coreTrendStrategy.analyze(klines4H, klines1H, klines15M, klines5M);
        if (coreTrendSignal != null && riskController.canOpenPosition(false)) {
            signals.add(coreTrendSignal);
        }

        if (!circuitBreakerActive) {
            Signal rangeSignal = rangeOscillationStrategy.analyze(klines4H, klines15M);
            if (rangeSignal != null && riskController.canOpenPosition(false)) {
                signals.add(rangeSignal);
            }

            Signal reversalSignal = trendReversalStrategy.analyze(klines15M, klines1H);
            if (reversalSignal != null && riskController.canOpenPosition(false)) {
                signals.add(reversalSignal);
            }

            Signal accelerationSignal = trendAccelerationStrategy.analyze(klines15M, klines1H, klines1M);
            if (accelerationSignal != null && riskController.canOpenPosition(false)) {
                signals.add(accelerationSignal);
            }

            Signal fakeBreakoutSignal = fakeBreakoutStrategy.analyze(klines15M, klines1H);
            if (fakeBreakoutSignal != null && riskController.canOpenPosition(false)) {
                signals.add(fakeBreakoutSignal);
            }
        }

        Signal nakedKSignal1H = nakedKStrategy.analyze(klines4H, klines1H, klines15M, klines5M, Timeframe.H1);
        if (nakedKSignal1H != null && riskController.canOpenPosition(false)) {
            signals.add(nakedKSignal1H);
        }

        Signal nakedKSignal15M = nakedKStrategy.analyze(klines4H, klines1H, klines15M, klines5M, Timeframe.M15);
        if (nakedKSignal15M != null && riskController.canOpenPosition(false)) {
            signals.add(nakedKSignal15M);
        }

        Signal nakedKSignal5M = nakedKStrategy.analyze(klines4H, klines1H, klines15M, klines5M, Timeframe.M5);
        if (nakedKSignal5M != null && riskController.canOpenPosition(false)) {
            signals.add(nakedKSignal5M);
        }

        return signals;
    }

    /**
     * 验证数据完整性
     */
    private boolean validateData(List<KLine> klines4H, List<KLine> klines1H,
                                  List<KLine> klines15M, List<KLine> klines5M) {
        return klines4H != null && klines4H.size() >= 50 &&
               klines1H != null && klines1H.size() >= 50 &&
               klines15M != null && klines15M.size() >= 50 &&
               klines5M != null && klines5M.size() >= 50;
    }

    /**
     * 更新波动率级别
     */
    private void updateVolatilityLevel(List<KLine> klines15M) {
        List<BigDecimal> atr15 = IndicatorCalculator.calculateATR(klines15M, 15);
        if (atr15.isEmpty()) {
            return;
        }

        BigDecimal currentPrice = klines15M.get(klines15M.size() - 1).getClose();
        BigDecimal atrValue = atr15.get(atr15.size() - 1);
        double volatility = IndicatorCalculator.calculateVolatility(atrValue, currentPrice);
        this.currentVolatilityLevel = VolatilityAnalyzer.getVolatilityLevel(symbol, volatility);
    }

    /**
     * 开仓
     */
    public Position openPosition(Signal signal) {
        if (!riskController.canOpenPosition(false)) {
            return null;
        }

        boolean liquidityWarning = VolatilityAnalyzer.isLiquidityWarning(
            symbol,
            currentVolatilityLevel == VolatilityLevel.HIGH ? 0.024 : 0.028
        );

        BigDecimal quantity = positionManager.calculateInitialPosition(
            signal.getScore(),
            currentVolatilityLevel,
            liquidityWarning,
            signal.getEntryPrice()
        );

        Position position = new Position(
            symbol,
            signal.getDirection(),
            signal.getStrategyName(),
            signal.getEntryPrice(),
            quantity
        );

        position.setStopLoss(signal.getStopLoss());
        position.setTakeProfit(signal.getTakeProfit());

        positions.put(generatePositionKey(signal), position);

        return position;
    }

    /**
     * 平仓
     */
    public void closePosition(String positionKey, BigDecimal exitPrice) {
        Position position = positions.get(positionKey);
        if (position == null) {
            return;
        }

        position.setCurrentPrice(exitPrice);
        double pnl = position.getUnrealizedPnL().doubleValue();

        riskController.recordTrade(pnl, position.isTrialPosition());

        positions.remove(positionKey);

        riskController.checkCircuitBreakerReset();
    }

    /**
     * 更新持仓
     */
    public void updatePositions(BigDecimal currentPrice) {
        for (Position position : positions.values()) {
            position.setCurrentPrice(currentPrice);
            position.incrementKlinesSinceEntry();

            if (shouldClosePosition(position)) {
                closePosition(generatePositionKey(position), currentPrice);
            }
        }
    }

    /**
     * 判断是否应该平仓
     */
    private boolean shouldClosePosition(Position position) {
        BigDecimal currentPrice = position.getCurrentPrice();

        if (position.getDirection() == Direction.LONG) {
            if (currentPrice.compareTo(position.getStopLoss()) <= 0) {
                return true;
            }
            if (currentPrice.compareTo(position.getTakeProfit()) >= 0) {
                return true;
            }
        } else {
            if (currentPrice.compareTo(position.getStopLoss()) >= 0) {
                return true;
            }
            if (currentPrice.compareTo(position.getTakeProfit()) <= 0) {
                return true;
            }
        }

        int timeStopLimit = getTimeStopLimit(position.getStrategyName());
        if (position.getKlinesSinceEntry() >= timeStopLimit) {
            return true;
        }

        return false;
    }

    /**
     * 获取时间止损限制
     */
    private int getTimeStopLimit(String strategyName) {
        switch (strategyName) {
            case "CoreTrend":
                return 20;
            case "RangeGrid":
            case "RangeOscillation":
                return Constants.TIME_STOP_RANGE_GRID;
            case "TrendReversal":
                return Constants.TIME_STOP_REVERSAL;
            case "TrendAcceleration":
                return Constants.TIME_STOP_ACCELERATION;
            case "FakeBreakout":
                return Constants.TIME_STOP_FAKE_BREAKOUT;
            case "NakedK":
                return Constants.TIME_STOP_NAKED;
            default:
                return 10;
        }
    }

    /**
     * 生成持仓键
     */
    private String generatePositionKey(Signal signal) {
        return String.format("%s_%s_%s_%d",
            signal.getSymbol(),
            signal.getStrategyName(),
            signal.getDirection(),
            System.currentTimeMillis()
        );
    }

    private String generatePositionKey(Position position) {
        return String.format("%s_%s_%s_%d",
            position.getSymbol(),
            position.getStrategyName(),
            position.getDirection(),
            position.getEntryTime().toEpochSecond(java.time.ZoneOffset.UTC)
        );
    }

    /**
     * 重置日亏损
     */
    public void resetDailyPnL() {
        riskController.resetDailyPnL();
    }

    /**
     * 激活熔断
     */
    public void activateCircuitBreaker() {
        this.circuitBreakerActive = true;
        riskController.setCircuitBreakerActive(true);
    }

    /**
     * 取消熔断
     */
    public void deactivateCircuitBreaker() {
        this.circuitBreakerActive = false;
        riskController.setCircuitBreakerActive(false);
    }

    // Getters
    public Map<String, Position> getPositions() {
        return positions;
    }

    public RiskController getRiskController() {
        return riskController;
    }

    public PositionManager getPositionManager() {
        return positionManager;
    }

    public boolean isCircuitBreakerActive() {
        return circuitBreakerActive;
    }

    public VolatilityLevel getCurrentVolatilityLevel() {
        return currentVolatilityLevel;
    }
}
