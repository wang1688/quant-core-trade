# 加密货币量化交易策略系统 - 完整实现

## 项目概述

这是一个完整的加密货币（BTC/ETH）量化交易策略系统，包含策略实现和回测系统。

## 目录结构

```
com.ruoyi.web.okx.strategynew/
├── Constants.java                      # 全局常量
├── TradingEngine.java                  # 主交易引擎
├── README.md                           # 本文件
│
├── enums/                              # 枚举定义
│   ├── Direction.java                  # 交易方向
│   ├── Timeframe.java                  # 时间级别
│   └── VolatilityLevel.java            # 波动率级别
│
├── model/                              # 数据模型
│   ├── KLine.java                      # K线数据
│   ├── Signal.java                     # 交易信号
│   ├── Position.java                   # 持仓模型
│   └── Box.java                        # 箱体模型
│
├── indicator/                          # 技术指标
│   ├── IndicatorCalculator.java        # 指标计算
│   ├── VolatilityAnalyzer.java         # 波动率分析
│   ├── BoxDetector.java                # 箱体识别
│   └── SignalScorer.java               # 信号评分
│
├── pattern/                            # 形态识别
│   └── PatternRecognizer.java          # 裸K形态
│
├── risk/                               # 风控管理
│   ├── RiskController.java             # 风控管理器
│   └── PositionManager.java            # 仓位管理器
│
├── strategy/                           # 策略实现
│   ├── CoreTrendStrategy.java          # 中枢核心策略
│   ├── RangeOscillationStrategy.java   # 区间震荡策略
│   ├── TrendReversalStrategy.java      # 趋势反转策略
│   ├── TrendAccelerationStrategy.java  # 趋势加速策略
│   ├── FakeBreakoutStrategy.java       # 假突破策略
│   └── NakedKStrategy.java             # 裸K交易体系
│
└── backtest/                           # 回测系统
    ├── BacktestEngine.java             # 回测引擎
    ├── BacktestResult.java             # 回测结果
    ├── BacktestAnalyzer.java           # 结果分析
    ├── BacktestExporter.java           # 结果导出
    ├── PerformanceMetrics.java         # 性能指标
    ├── TradeResult.java                # 交易记录
    ├── DataLoader.java                 # 数据加载
    ├── BacktestMain.java               # 主程序
    ├── BacktestApplication.java        # Spring Boot启动
    ├── MultiSymbolBacktest.java        # 多品种对比
    ├── README.md                       # 回测说明
    ├── USAGE.md                        # 使用指南
    └── FEATURES.md                     # 功能清单
```

## 核心功能

### 1. 策略系统

#### 主策略
- **中枢核心策略**: 趋势单边行情，核心盈利来源
  - 4H/1H趋势共振判定
  - 三级挂单入场
  - 分级别加仓体系

#### 四大辅助策略
1. **区间震荡策略**: 低波网格 + 中波震荡
2. **趋势末端反转策略**: 捕捉背离和反转
3. **趋势加速策略**: 快速捕捉加速行情
4. **假突破策略**: 识别虚假突破反向入场

#### 独立体系
- **裸K交易体系**: 三级联动（1H/15M/5M）
  - 强形态/次强形态识别
  - 精准动态止损
  - 独立核算仓位

### 2. 技术指标系统

- **EMA系统**: EMA6/13/30，角度计算
- **ATR系统**: 波动率计算和分级
- **DIF乖离率**: EMA6-EMA13发散度
- **箱体识别**: 自动识别和升级
- **信号评分**: 12分制综合评分

### 3. 风控系统

- **仓位管理**: 9档风险敞口（负一档至8档）
- **止损止盈**: 动态调整，仅向盈利方向移动
- **亏损控制**: 日亏损/累计亏损/连续亏损
- **策略熔断**: 自动熔断和恢复机制

### 4. 回测系统

#### 基础功能
- 通过OKX API获取2年真实历史数据
- 支持多时间级别（1m/5m/15m/1H/4H）
- 完整交易模拟
- 多策略并行测试

#### 性能指标
- 基础指标: 成交笔数、总收益率、胜率、最大回撤、盈亏比
- 高级指标: 夏普比率、索提诺比率、卡玛比率、盈利因子等

#### 分析功能
- 按策略分析
- 按方向分析
- 持仓时间统计
- 连续盈亏分析
- 月度收益分析

#### 导出功能
- 交易记录CSV
- 资金曲线CSV
- 完整报告TXT
- 策略统计CSV

## 快速开始

### 1. 运行回测

```bash
# 设置环境变量
export OKX_API_KEY="your_api_key"
export OKX_SECRET_KEY="your_secret_key"
export OKX_PASSPHRASE="your_passphrase"

# 可选：设置代理
export PROXY_HOST="127.0.0.1"
export PROXY_PORT="7890"

# 运行单品种回测
java com.ruoyi.web.okx.strategynew.backtest.BacktestMain

# 运行多品种对比
java com.ruoyi.web.okx.strategynew.backtest.MultiSymbolBacktest
```

### 2. Spring Boot方式

```bash
mvn spring-boot:run -Dspring-boot.run.main-class=com.ruoyi.web.okx.strategynew.backtest.BacktestApplication
```

### 3. 实盘交易（开发中）

```java
TradingEngine engine = new TradingEngine("BTC", 10000.0);

// 获取实时K线数据
List<KLine> klines4H = ...;
List<KLine> klines1H = ...;
List<KLine> klines15M = ...;
List<KLine> klines5M = ...;
List<KLine> klines1M = ...;

// 分析并生成信号
List<Signal> signals = engine.analyze(klines4H, klines1H, klines15M, klines5M, klines1M);

// 执行交易
for (Signal signal : signals) {
    Position position = engine.openPosition(signal);
    // 发送订单到交易所...
}
```

## 核心特性

### 1. 多空对称
所有策略规则完全对称，仅方向反向，无单方向偏向。

### 2. 多级别联动
- 4H: 大趋势定调
- 1H: 趋势确认
- 15M: 决策执行
- 5M: 小级别加仓
- 1M: 极致快进

### 3. 自适应切换
根据波动率自动调整：
- 低波: 宽松信号，允许4.5分开仓
- 中波: 常规标准，≥5分开仓
- 高波: 收紧标准，≥6分开仓
- 极端高波: 极严标准，≥9分开仓

### 4. 风控优先
- 多层风控保护
- 自动熔断机制
- 档位自动升降
- 连续亏损保护

## 关键参数

### BTC专属
- 有效箱体: ≥7000跳
- 突破确认: ≥3500跳
- 资费缓冲: 500跳（高波1000跳）

### ETH专属
- 有效箱体: ≥2200跳
- 突破确认: ≥1000跳
- 资费缓冲: 200跳（高波400跳）

### 波动率阈值
- BTC低波: ≤0.8%, 中波: 0.8%-1.6%, 高波: ≥1.6%
- ETH低波: ≤1.0%, 中波: 1.0%-2.0%, 高波: ≥2.0%

### EMA30角度
- 低波: ≥14.0°
- 中波: ≥16.5°
- 高波: ≥18.0°

## 回测结果示例

```
========================================
回测报告
========================================
品种: BTC
策略: MultiStrategy
----------------------------------------
成交笔数: 245
盈利笔数: 152
亏损笔数: 93
----------------------------------------
初始资金: $10000.00
最终资金: $15234.56
净利润: $5234.56
总收益率: 52.35%
----------------------------------------
胜率: 62.04%
盈亏比: 1.85
----------------------------------------
最大回撤: $1234.56
最大回撤率: 12.35%
========================================

高级性能指标
========================================
夏普比率: 1.85
索提诺比率: 2.34
卡玛比率: 4.24
盈利因子: 2.15
恢复因子: 4.24
期望收益: $21.36
========================================
```

## 注意事项

1. **数据质量**: 使用OKX真实历史数据，结果可靠
2. **API限流**: 系统已自动处理OKX API限流
3. **手续费**: 当前版本未计入手续费
4. **滑点**: 未模拟滑点，实盘会有差异
5. **参数优化**: 避免过度拟合历史数据

## 开发规范

1. 严格按照定量参数编写代码
2. 多空规则必须完全对称
3. 风控规则优先级最高
4. 裸K体系与主策略严格区分
5. 所有跳数、角度精确匹配文档

## 版本信息

- **版本**: 1.0.0
- **最后更新**: 2026-05-08
- **开发语言**: Java 8+
- **依赖框架**: Spring Boot (可选)

## 文档索引

- [策略系统README](./README.md) - 本文件
- [回测系统README](./backtest/README.md) - 回测系统说明
- [使用指南](./backtest/USAGE.md) - 详细使用说明
- [功能清单](./backtest/FEATURES.md) - 完整功能列表

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，请提交Issue。
