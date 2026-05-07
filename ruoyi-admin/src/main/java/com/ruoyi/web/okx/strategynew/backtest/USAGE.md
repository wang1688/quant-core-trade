# 回测系统使用指南

## 概述

回测系统通过OKX API获取真实的2年历史数据，测试策略表现并计算关键指标。

## 运行方式

### 方式一：独立运行（推荐）

```bash
# 设置环境变量
export OKX_API_KEY="your_api_key"
export OKX_SECRET_KEY="your_secret_key"
export OKX_PASSPHRASE="your_passphrase"

# 如果需要代理
export PROXY_HOST="127.0.0.1"
export PROXY_PORT="7890"

# 运行回测
java -cp target/classes com.ruoyi.web.okx.strategynew.backtest.BacktestMain
```

### 方式二：Spring Boot运行

```bash
# 在application.yml中配置OKX API密钥
mvn spring-boot:run -Dspring-boot.run.main-class=com.ruoyi.web.okx.strategynew.backtest.BacktestApplication
```

### 方式三：IDE运行

1. 在IDE中打开 `BacktestMain.java`
2. 配置环境变量（Run Configuration）
3. 直接运行main方法

## 配置说明

### OKX API密钥配置

#### 环境变量方式（推荐）

```bash
# Linux/Mac
export OKX_API_KEY="your_api_key"
export OKX_SECRET_KEY="your_secret_key"
export OKX_PASSPHRASE="your_passphrase"

# Windows
set OKX_API_KEY=your_api_key
set OKX_SECRET_KEY=your_secret_key
set OKX_PASSPHRASE=your_passphrase
```

#### application.yml方式

```yaml
okx:
  api-key: your_api_key
  secret-key: your_secret_key
  passphrase: your_passphrase
  simulated: false
```

### 代理配置（可选）

如果需要通过代理访问OKX API：

```bash
export PROXY_HOST="127.0.0.1"
export PROXY_PORT="7890"
```

## 回测参数配置

在 `BacktestMain.java` 中修改：

```java
// 交易品种
String symbol = "BTC";  // 或 "ETH"
String instId = "BTC-USDT-SWAP";  // 或 "ETH-USDT-SWAP"

// 初始资金
double initialCapital = 10000.0;  // 美金
```

## 输出报告

### 基础指标

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
```

### 详细分析

#### 1. 策略分布
显示每个策略的交易笔数、盈亏和胜率

```
策略分布:
  CoreTrend: 120笔, 盈亏: $3500.00, 胜率: 65.00%
  RangeOscillation: 45笔, 盈亏: $800.00, 胜率: 55.56%
  TrendReversal: 30笔, 盈亏: $500.00, 胜率: 60.00%
  ...
```

#### 2. 方向分布
显示做多和做空的表现

```
方向分布:
  做多: 125笔, 盈亏: $2800.00, 胜率: 64.00%
  做空: 120笔, 盈亏: $2434.56, 胜率: 60.00%
```

#### 3. 持仓时间分析

```
持仓时间分析:
  平均持仓: 8.5根15M K线 (2.1小时)
  最短持仓: 2根15M K线 (0.5小时)
  最长持仓: 20根15M K线 (5.0小时)
```

#### 4. 最佳/最差交易

```
最佳/最差交易:
  最佳交易: LONG BTC, 盈利: $450.00, 策略: CoreTrend
  最差交易: SHORT BTC, 亏损: $180.00, 策略: TrendReversal
```

#### 5. 连续盈亏

```
连续盈亏:
  最大连续盈利: 8 笔
  最大连续亏损: 4 笔
```

#### 6. 月度收益

```
月度收益:
  2024-01: $450.00
  2024-02: $320.00
  2024-03: $580.00
  ...
```

## 数据加载说明

### 自动加载流程

1. 系统调用 `OkxApiClient.getTwoYearsKLineArray()` 获取2年历史数据
2. 自动分页加载，防止API限流
3. 数据按时间正序排列
4. 支持多个时间级别：1m, 5m, 15m, 1H, 4H

### 数据量说明

- **15M K线**: 约 70,080 根（2年）
- **1H K线**: 约 17,520 根（2年）
- **4H K线**: 约 4,380 根（2年）
- **5M K线**: 约 210,240 根（2年）
- **1M K线**: 约 1,051,200 根（2年）

### 加载时间

根据网络情况，完整加载2年数据大约需要：
- 15M: 1-2分钟
- 1H: 30秒-1分钟
- 4H: 10-20秒
- 5M: 3-5分钟
- 1M: 10-15分钟

## 性能优化

### 1. 使用缓存

首次加载后可以将数据缓存到本地：

```java
// 保存到文件
DataLoader.saveToCSV(klines, "data/BTC_15m.csv");

// 下次从文件加载
List<KLine> klines = DataLoader.loadFromCSV("data/BTC_15m.csv", "15m");
```

### 2. 减少数据量

如果只需要测试部分时间段：

```java
// 只加载最近1年数据
List<KLine> klines = klines15M.subList(klines15M.size() - 35040, klines15M.size());
```

### 3. 并行加载

可以并行加载不同时间级别的数据（需要注意API限流）

## 常见问题

### Q: API密钥配置错误怎么办？
A: 检查环境变量是否正确设置，或在application.yml中配置

### Q: 网络连接失败？
A: 配置代理或检查网络连接

### Q: 数据加载很慢？
A: 正常现象，2年数据量较大。可以先测试较短时间段

### Q: 回测结果不理想？
A: 这是正常的，策略需要根据回测结果进行优化

### Q: 如何对比不同参数？
A: 修改策略参数后重新运行回测，对比结果

## 注意事项

1. **API限流**: OKX API有频率限制，系统已自动处理
2. **数据质量**: 使用真实历史数据，结果更可靠
3. **手续费**: 当前版本未计入手续费，实际收益会略低
4. **滑点**: 未模拟滑点，实盘执行会有差异
5. **资金管理**: 确保初始资金足够支持策略运行

## 下一步

- 添加手续费和滑点模拟
- 支持参数优化
- 生成可视化图表
- 导出详细交易记录
- 支持多品种对比测试
