# 加密货币多级别中枢自适应量化交易系统

## 系统概述

这是一个完整的加密货币（BTC/ETH）量化交易策略系统，基于多级别中枢自适应机制，在不同市场环境下自动切换策略。

### 核心特征
- **交易品种**：BTC、ETH（100倍杠杆合约）
- **级别体系**：4H/1H/15M/5M/1M五级联动
- **起投资金**：1000美金起
- **多空对称**：所有规则完全对称

## 项目结构

```
com.ruoyi.web.okx.strategynew/
├── Constants.java                      # 全局常量定义
├── TradingEngine.java                  # 主交易引擎
├── enums/                              # 枚举类
│   ├── Direction.java                  # 交易方向
│   ├── Timeframe.java                  # 时间级别
│   └── VolatilityLevel.java            # 波动率级别
├── model/                              # 数据模型
│   ├── KLine.java                      # K线数据
│   ├── Signal.java                     # 交易信号
│   ├── Position.java                   # 持仓模型
│   └── Box.java                        # 箱体/中枢模型
├── indicator/                          # 技术指标
│   ├── IndicatorCalculator.java        # 指标计算器
│   ├── VolatilityAnalyzer.java         # 波动率分析器
│   ├── BoxDetector.java                # 箱体识别器
│   └── SignalScorer.java               # 信号评分系统
├── pattern/                            # 形态识别
│   └── PatternRecognizer.java          # 裸K形态识别器
├── risk/                               # 风控管理
│   ├── RiskController.java             # 风控管理器
│   └── PositionManager.java            # 仓位管理器
└── strategy/                           # 策略实现
    ├── CoreTrendStrategy.java          # 中枢核心策略
    ├── RangeOscillationStrategy.java   # 区间震荡策略
    ├── TrendReversalStrategy.java      # 趋势末端反转策略
    ├── TrendAccelerationStrategy.java  # 趋势加速策略
    ├── FakeBreakoutStrategy.java       # 假突破策略
    └── NakedKStrategy.java             # 裸K交易体系
```

## 策略架构

### 主策略
**中枢核心策略（CoreTrendStrategy）**
- 核心盈利来源
- 基于4H/1H趋势共振判定
- 三级挂单入场（5M EMA13/15M前高/5M EMA6）
- 分级别加仓体系

### 四大辅助策略
1. **区间震荡策略（RangeOscillationStrategy）**：低波网格交易 + 中波震荡交易
2. **趋势末端反转策略（TrendReversalStrategy）**：捕捉背离、长影线、分型反转
3. **趋势加速策略（TrendAccelerationStrategy）**：挂单+实时双模式，1M极致快进
4. **假突破策略（FakeBreakoutStrategy）**：识别虚假突破，反向入场

### 独立体系
**裸K交易体系（NakedKStrategy）**
- 三级联动（1H/15M/5M）
- 强形态/次强形态识别
- 精准动态止损
- 独立核算仓位（≤40%）

## 核心技术指标

### EMA30角度判定
- 低波（BTC≤0.8%/ETH≤1.0%）：≥14.0°
- 中波（BTC 0.8%-1.6%/ETH 1.0%-2.0%）：≥16.5°
- 高波（BTC≥1.6%/ETH≥2.0%）：≥18.0°

### DIF乖离率
- 强发散：＞0.12%
- 中发散：0.05%-0.12%
- 弱发散：0-0.05%

### 箱体/中枢定义
- BTC有效箱体：≥7000跳
- ETH有效箱体：≥2200跳
- 突破确认：BTC≥3500跳/ETH≥1000跳

## 风控体系

### 仓位管理
- 风险敞口分档（负一档至8档）
- 单品种单策略≤90%
- 逆趋势单≤30%
- 裸K加仓≤40%
- 高波期所有仓位×0.8

### 止损止盈
- 止损仅向盈利方向移动
- 保本为最低标准
- 新仓独立设损
- 资费缓冲：BTC 500跳/ETH 200跳

### 亏损控制
- 日亏损阈值：6%-21%（按档位）
- 累计亏损封顶：10%-30%
- 连续6笔亏损强制停交易

### 策略熔断
- 中枢主策略连续4笔亏损
- 且市场高波预警
- 暂停所有辅助策略
- 仅保留核心趋势策略

## 使用示例

```java
// 初始化交易引擎
TradingEngine engine = new TradingEngine("BTC", 10000.0);

// 准备K线数据
List<KLine> klines4H = ...;
List<KLine> klines1H = ...;
List<KLine> klines15M = ...;
List<KLine> klines5M = ...;
List<KLine> klines1M = ...;

// 分析市场并生成信号
List<Signal> signals = engine.analyze(klines4H, klines1H, klines15M, klines5M, klines1M);

// 处理信号
for (Signal signal : signals) {
    if (signal != null) {
        Position position = engine.openPosition(signal);
        System.out.println("开仓: " + signal.getStrategyName() + 
                          " " + signal.getDirection() + 
                          " 评分: " + signal.getScore());
    }
}

// 更新持仓
BigDecimal currentPrice = getCurrentPrice();
engine.updatePositions(currentPrice);

// 每日重置
engine.resetDailyPnL();
```

## 关键特性

### 1. 多级别联动
- 4H定调大趋势
- 1H确认趋势
- 15M决策执行
- 5M小级别加仓
- 1M极致快进

### 2. 自适应切换
- 根据波动率自动调整策略
- 低波/中波/高波/极端高波四档
- 动态调整仓位和止损

### 3. 信号评分系统
- 趋势强度（0-3分）
- 量能共振（0-3分）
- 结构适配（0-2分）
- 波动匹配（0-2分）
- 均线排列（0-2分）
- 满分12分

### 4. 风控优先
- 多层风控保护
- 自动熔断机制
- 档位自动升降
- 连续亏损保护

## 注意事项

1. **参数调整**：每季度/半年一次，禁止高频优化
2. **回测验证**：回测+外验衰减≤30%方可采纳
3. **多空对称**：多空绩效差异≤10%
4. **实盘监控**：分开统计BTC/ETH绩效
5. **API安全**：仅开通"下单、撤单、查询"权限

## 开发规范

1. 严格按照文档中的定量参数编写代码
2. 所有跳数、角度、百分比阈值必须精确匹配
3. 多空规则必须完全对称
4. 风控规则优先级最高
5. 裸K体系与中枢主策略代码逻辑严格区分

## 版本信息

- **版本**：1.0.0
- **最后更新**：2026-05-07
- **基于文档**：加密货币多级别中枢自适应量化交易系统（0427版本优化）

## 许可证

本项目仅供学习和研究使用。
