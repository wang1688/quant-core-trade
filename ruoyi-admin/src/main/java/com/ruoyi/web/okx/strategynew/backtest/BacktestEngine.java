package com.ruoyi.web.okx.strategynew.backtest;

import com.ruoyi.web.okx.strategynew.TradingEngine;
import com.ruoyi.web.okx.strategynew.enums.Direction;
import com.ruoyi.web.okx.strategynew.model.KLine;
import com.ruoyi.web.okx.strategynew.model.Position;
import com.ruoyi.web.okx.strategynew.model.Signal;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 回测引擎
 */
public class BacktestEngine {

    private String symbol;
    private BigDecimal initialCapital;
    private TradingEngine tradingEngine;
    private BacktestResult result;
    private Map<String, BacktestPosition> openPositions;

    public BacktestEngine(String symbol, double initialCapital) {
        this.symbol = symbol;
        this.initialCapital = BigDecimal.valueOf(initialCapital);
        this.tradingEngine = new TradingEngine(symbol, initialCapital);
        this.result = new BacktestResult(symbol, "MultiStrategy", this.initialCapital);
        this.openPositions = new HashMap<>();
    }

    /**
     * 运行回测
     */
    public BacktestResult runBacktest(List<KLine> klines4H, List<KLine> klines1H,
                                       List<KLine> klines15M, List<KLine> klines5M,
                                       List<KLine> klines1M) {
        if (klines15M == null || klines15M.isEmpty()) {
            return result;
        }

        int totalBars = klines15M.size();
        int warmupPeriod = 100;

        for (int i = warmupPeriod; i < totalBars; i++) {
            List<KLine> current4H = getSubList(klines4H, 0, getCorrespondingIndex(i, 15, 240));
            List<KLine> current1H = getSubList(klines1H, 0, getCorrespondingIndex(i, 15, 60));
            List<KLine> current15M = getSubList(klines15M, 0, i + 1);
            List<KLine> current5M = getSubList(klines5M, 0, getCorrespondingIndex(i, 15, 5));
            List<KLine> current1M = getSubList(klines1M, 0, getCorrespondingIndex(i, 15, 1));

            BigDecimal currentPrice = klines15M.get(i).getClose();
            LocalDateTime currentTime = klines15M.get(i).getTimestamp();

            checkExitConditions(currentPrice, currentTime, i - warmupPeriod);

            if (openPositions.size() < 5) {
                List<Signal> signals = tradingEngine.analyze(current4H, current1H, current15M, current5M, current1M);

                for (Signal signal : signals) {
                    if (signal != null && openPositions.size() < 5) {
                        openPosition(signal, currentTime);
                    }
                }
            }

            if (i % 96 == 0) {
                tradingEngine.resetDailyPnL();
            }
        }

        closeAllPositions(klines15M.get(totalBars - 1).getClose(),
                         klines15M.get(totalBars - 1).getTimestamp(),
                         "回测结束");

        return result;
    }

    /**
     * 开仓
     */
    private void openPosition(Signal signal, LocalDateTime currentTime) {
        Position position = tradingEngine.openPosition(signal);
        if (position == null) {
            return;
        }

        BacktestPosition btPosition = new BacktestPosition(
            signal.getSymbol(),
            signal.getStrategyName(),
            signal.getDirection(),
            currentTime,
            signal.getEntryPrice(),
            position.getQuantity(),
            signal.getStopLoss(),
            signal.getTakeProfit()
        );

        String key = generatePositionKey(signal);
        openPositions.put(key, btPosition);
    }

    /**
     * 检查平仓条件
     */
    private void checkExitConditions(BigDecimal currentPrice, LocalDateTime currentTime, int barsSinceStart) {
        List<String> toClose = new ArrayList<>();

        for (Map.Entry<String, BacktestPosition> entry : openPositions.entrySet()) {
            BacktestPosition position = entry.getValue();
            position.incrementHoldingBars();

            String exitReason = null;

            if (position.getDirection() == Direction.LONG) {
                if (currentPrice.compareTo(position.getStopLoss()) <= 0) {
                    exitReason = "止损";
                } else if (currentPrice.compareTo(position.getTakeProfit()) >= 0) {
                    exitReason = "止盈";
                }
            } else {
                if (currentPrice.compareTo(position.getStopLoss()) >= 0) {
                    exitReason = "止损";
                } else if (currentPrice.compareTo(position.getTakeProfit()) <= 0) {
                    exitReason = "止盈";
                }
            }

            if (position.getHoldingBars() >= getTimeStopLimit(position.getStrategyName())) {
                exitReason = "时间止损";
            }

            if (exitReason != null) {
                closePosition(entry.getKey(), position, currentPrice, currentTime, exitReason);
                toClose.add(entry.getKey());
            }
        }

        for (String key : toClose) {
            openPositions.remove(key);
        }
    }

    /**
     * 平仓
     */
    private void closePosition(String key, BacktestPosition position, BigDecimal exitPrice,
                               LocalDateTime exitTime, String exitReason) {
        TradeResult trade = new TradeResult(
            position.getSymbol(),
            position.getStrategyName(),
            position.getDirection(),
            position.getEntryTime(),
            position.getEntryPrice(),
            position.getQuantity()
        );

        trade.close(exitTime, exitPrice, exitReason, position.getHoldingBars());
        result.addTrade(trade);

        tradingEngine.closePosition(key, exitPrice);
    }

    /**
     * 平掉所有持仓
     */
    private void closeAllPositions(BigDecimal exitPrice, LocalDateTime exitTime, String exitReason) {
        for (Map.Entry<String, BacktestPosition> entry : openPositions.entrySet()) {
            closePosition(entry.getKey(), entry.getValue(), exitPrice, exitTime, exitReason);
        }
        openPositions.clear();
    }

    /**
     * 获取子列表
     */
    private List<KLine> getSubList(List<KLine> list, int start, int end) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        end = Math.min(end, list.size());
        start = Math.max(0, start);
        if (start >= end) {
            return new ArrayList<>();
        }
        return new ArrayList<>(list.subList(start, end));
    }

    /**
     * 获取对应索引
     */
    private int getCorrespondingIndex(int index15M, int sourceMinutes, int targetMinutes) {
        return (index15M * sourceMinutes) / targetMinutes;
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
                return 6;
            case "TrendReversal":
                return 2;
            case "TrendAcceleration":
                return 2;
            case "FakeBreakout":
                return 2;
            case "NakedK":
                return 4;
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

    public BacktestResult getResult() {
        return result;
    }

    /**
     * 回测持仓（简化版）
     */
    private static class BacktestPosition {
        private String symbol;
        private String strategyName;
        private Direction direction;
        private LocalDateTime entryTime;
        private BigDecimal entryPrice;
        private BigDecimal quantity;
        private BigDecimal stopLoss;
        private BigDecimal takeProfit;
        private int holdingBars;

        public BacktestPosition(String symbol, String strategyName, Direction direction,
                                LocalDateTime entryTime, BigDecimal entryPrice, BigDecimal quantity,
                                BigDecimal stopLoss, BigDecimal takeProfit) {
            this.symbol = symbol;
            this.strategyName = strategyName;
            this.direction = direction;
            this.entryTime = entryTime;
            this.entryPrice = entryPrice;
            this.quantity = quantity;
            this.stopLoss = stopLoss;
            this.takeProfit = takeProfit;
            this.holdingBars = 0;
        }

        public void incrementHoldingBars() {
            this.holdingBars++;
        }

        // Getters
        public String getSymbol() {
            return symbol;
        }

        public String getStrategyName() {
            return strategyName;
        }

        public Direction getDirection() {
            return direction;
        }

        public LocalDateTime getEntryTime() {
            return entryTime;
        }

        public BigDecimal getEntryPrice() {
            return entryPrice;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public BigDecimal getStopLoss() {
            return stopLoss;
        }

        public BigDecimal getTakeProfit() {
            return takeProfit;
        }

        public int getHoldingBars() {
            return holdingBars;
        }
    }
}
