package com.ruoyi.web.okx.strategynew;

/**
 * 策略常量定义
 * 所有跳数、角度、百分比阈值的统一定义
 */
public class StrategyConstants {

    // ==================== 品种基础参数 ====================

    /** BTC 1跳 = 0.1 USDT */
    public static final double BTC_TICK = 0.1;

    /** ETH 1跳 = 0.01 USDT */
    public static final double ETH_TICK = 0.01;

    /** 起投资金（美金） */
    public static final double INITIAL_CAPITAL = 1000.0;

    /** 杠杆倍数 */
    public static final int LEVERAGE = 100;

    // ==================== 波动率分级阈值 ====================

    /** BTC低波阈值 */
    public static final double BTC_LOW_VOL = 0.008;  // 0.8%

    /** BTC高波阈值 */
    public static final double BTC_HIGH_VOL = 0.016; // 1.6%

    /** BTC极端高波阈值 */
    public static final double BTC_EXTREME_VOL = 0.030; // 3.0%

    /** BTC流动性预警阈值 */
    public static final double BTC_LIQUIDITY_WARNING = 0.024; // 2.4%

    /** ETH低波阈值 */
    public static final double ETH_LOW_VOL = 0.010;  // 1.0%

    /** ETH高波阈值 */
    public static final double ETH_HIGH_VOL = 0.020; // 2.0%

    /** ETH极端高波阈值 */
    public static final double ETH_EXTREME_VOL = 0.040; // 4.0%

    /** ETH流动性预警阈值 */
    public static final double ETH_LIQUIDITY_WARNING = 0.028; // 2.8%

    // ==================== EMA30角度阈值（度） ====================

    /** 震荡区角度上限 */
    public static final double ANGLE_FLAT_MAX = 12.0;

    /** 低波有效趋势角度 */
    public static final double ANGLE_LOW_VOL_TREND = 14.0;

    /** 中波有效趋势角度 */
    public static final double ANGLE_MID_VOL_TREND = 16.5;

    /** 高波有效趋势角度 */
    public static final double ANGLE_HIGH_VOL_TREND = 18.0;

    /** 高波加仓角度 */
    public static final double ANGLE_HIGH_VOL_ADD = 20.0;

    /** 强趋势角度 */
    public static final double ANGLE_STRONG_TREND = 22.0;

    /** 极端高波有效趋势角度 */
    public static final double ANGLE_EXTREME_VOL_TREND = 22.0;

    /** 极端高波加仓角度 */
    public static final double ANGLE_EXTREME_VOL_ADD = 25.0;

    /** EMA30角度≥25°时发散度阈值下调 */
    public static final double ANGLE_DIF_ADJUST = 25.0;

    // ==================== DIF乖离率阈值 ====================

    /** DIF有效排列阈值 */
    public static final double DIF_VALID = 0.0005;  // 0.05%

    /** 1H/15M强发散阈值（基础） */
    public static final double DIF_STRONG_BASE = 0.0012;  // 0.12%

    /** 1H/15M强发散阈值（EMA30≥25°时） */
    public static final double DIF_STRONG_ANGLE_ADJUST = 0.0007;  // 0.07%

    /** 1H/15M强发散阈值（高波期） */
    public static final double DIF_STRONG_HIGH_VOL = 0.0015;  // 0.15%

    /** 极端高波/加速强发散阈值 */
    public static final double DIF_STRONG_EXTREME = 0.0020;  // 0.20%

    /** 5M强发散阈值 */
    public static final double DIF_STRONG_5M = 0.0010;  // 0.10%

    /** 中发散下限 */
    public static final double DIF_MID_MIN = 0.0005;  // 0.05%

    /** 5M中发散下限 */
    public static final double DIF_MID_MIN_5M = 0.0004;  // 0.04%

    // ==================== 放量标准 ====================

    /** 裸K基础放量 */
    public static final double VOLUME_NAKED_K = 1.2;

    /** 显著放量 */
    public static final double VOLUME_SIGNIFICANT = 1.5;

    /** 威科夫放量 */
    public static final double VOLUME_WYCKOFF = 1.8;

    /** 强放量 */
    public static final double VOLUME_STRONG = 2.0;

    /** 加速/推动放量 */
    public static final double VOLUME_ACCELERATION = 2.5;

    /** 缩量阈值 */
    public static final double VOLUME_SHRINK = 0.6;

    // ==================== 箱体参数 ====================

    /** BTC有效箱体高度（跳） */
    public static final int BTC_BOX_MIN_TICKS = 7000;

    /** ETH有效箱体高度（跳） */
    public static final int ETH_BOX_MIN_TICKS = 2200;

    /** BTC突破确认跳数 */
    public static final int BTC_BREAKOUT_TICKS = 3500;

    /** ETH突破确认跳数 */
    public static final int ETH_BREAKOUT_TICKS = 1000;

    /** BTC回踩确认跳数 */
    public static final int BTC_PULLBACK_TICKS = 2100;

    /** ETH回踩确认跳数 */
    public static final int ETH_PULLBACK_TICKS = 650;

    /** 箱体重叠度要求 */
    public static final double BOX_OVERLAP_RATIO = 0.80;

    /** 低波回踩深度上限 */
    public static final double PULLBACK_LOW_VOL = 0.30;

    /** 中波回踩深度上限 */
    public static final double PULLBACK_MID_VOL = 0.25;

    /** 高波回踩深度上限 */
    public static final double PULLBACK_HIGH_VOL = 0.20;

    // ==================== 资费缓冲（跳） ====================

    /** BTC常规资费缓冲 */
    public static final int BTC_FEE_BUFFER = 500;

    /** ETH常规资费缓冲 */
    public static final int ETH_FEE_BUFFER = 200;

    /** BTC高波资费缓冲 */
    public static final int BTC_FEE_BUFFER_HIGH = 1000;

    /** ETH高波资费缓冲 */
    public static final int ETH_FEE_BUFFER_HIGH = 400;

    // ==================== 信号评分阈值 ====================

    /** 低波最低开仓评分 */
    public static final int SCORE_MIN_LOW_VOL = 4;  // 4.5分向下取整

    /** 中高波最低开仓评分 */
    public static final int SCORE_MIN_NORMAL = 5;

    /** 高波最低开仓评分 */
    public static final int SCORE_MIN_HIGH_VOL = 6;

    /** 强信号评分 */
    public static final int SCORE_STRONG = 7;

    /** 极端高波最低开仓评分 */
    public static final int SCORE_MIN_EXTREME = 9;

    /** 超强信号评分 */
    public static final int SCORE_SUPER_STRONG = 9;

    // ==================== 风险敞口比率 ====================

    /** 风险敞口档位 */
    public static final double[] RISK_RATIOS = {
        0.007,  // 负一档 0.7%
        0.010,  // 1档 1.0%
        0.013,  // 2档 1.3%
        0.016,  // 3档 1.6%
        0.019,  // 4档 1.9%
        0.021,  // 5档 2.1%
        0.030,  // 6档 3.0%
        0.040,  // 7档 4.0%
        0.050   // 8档 5.0%
    };

    /** 日亏损阈值 */
    public static final double[] DAILY_LOSS_LIMITS = {
        0.06, 0.06, 0.06, 0.08, 0.10, 0.12, 0.16, 0.18, 0.21
    };

    /** 累计亏损阈值 */
    public static final double[] TOTAL_LOSS_LIMITS = {
        0.15, 0.10, 0.10, 0.10, 0.15, 0.20, 0.25, 0.30, 0.30
    };

    // ==================== 仓位上限 ====================

    /** 单品种单策略仓位上限 */
    public static final double POSITION_LIMIT_SINGLE = 0.90;

    /** 逆趋势单仓位上限 */
    public static final double POSITION_LIMIT_COUNTER = 0.30;

    /** 裸K加仓仓位上限 */
    public static final double POSITION_LIMIT_NAKED_K = 0.40;

    /** 区间震荡策略仓位上限 */
    public static final double POSITION_LIMIT_RANGE = 0.05;

    /** 试单仓位上限 */
    public static final double POSITION_LIMIT_TRIAL = 0.10;

    /** 小级别加仓合计上限 */
    public static final double POSITION_LIMIT_SMALL_ADD = 0.15;

    // ==================== 时间止损（15M K线根数） ====================

    /** 区间震荡（网格交易）时间止损 */
    public static final int TIME_STOP_RANGE_GRID = 6;

    /** 区间震荡（网格交易）低波延长 */
    public static final int TIME_STOP_RANGE_GRID_LOW = 8;

    /** 区间震荡（突破回踩）时间止损 */
    public static final int TIME_STOP_RANGE_BREAKOUT = 4;

    /** 趋势末端反转时间止损 */
    public static final int TIME_STOP_REVERSAL = 2;

    /** 趋势末端反转低中波延长 */
    public static final int TIME_STOP_REVERSAL_LOW = 3;

    /** 趋势加速结构型时间止损 */
    public static final int TIME_STOP_ACCEL_STRUCT = 2;

    /** 趋势加速突发型时间止损 */
    public static final int TIME_STOP_ACCEL_SUDDEN = 1;

    /** 趋势加速低中波结构型延长 */
    public static final int TIME_STOP_ACCEL_STRUCT_LOW = 4;

    /** 假突破时间止损 */
    public static final int TIME_STOP_FALSE_BREAK = 2;

    /** 15M试单时间止损 */
    public static final int TIME_STOP_TRIAL = 2;

    /** 15M试单低中波延长 */
    public static final int TIME_STOP_TRIAL_LOW = 3;

    /** 15M试单缓冲期末端缩短 */
    public static final int TIME_STOP_TRIAL_BUFFER_END = 1;

    /** 裸K纯形态入场时间止损 */
    public static final int TIME_STOP_NAKED_K = 4;

    // ==================== 量能背离系数 ====================

    /** 量能背离系数阈值 */
    public static final double VOLUME_DIVERGENCE_THRESHOLD = -0.3;

    /** 显著量能背离系数 */
    public static final double VOLUME_DIVERGENCE_SIGNIFICANT = -0.5;

    // ==================== 其他常量 ====================

    /** 连续亏损停交易阈值 */
    public static final int CONSECUTIVE_LOSS_LIMIT = 6;

    /** 试单连续亏损暂停阈值1 */
    public static final int TRIAL_LOSS_PAUSE_1 = 2;

    /** 试单连续亏损暂停阈值2 */
    public static final int TRIAL_LOSS_PAUSE_2 = 3;

    /** 试单暂停时长1（小时） */
    public static final int TRIAL_PAUSE_HOURS_1 = 1;

    /** 试单暂停时长2（小时） */
    public static final int TRIAL_PAUSE_HOURS_2 = 4;

    /** 策略熔断连续亏损阈值 */
    public static final int CIRCUIT_BREAKER_LOSS = 4;

    /** 策略熔断恢复连续盈利阈值 */
    public static final int CIRCUIT_BREAKER_PROFIT = 2;

    /** 横盘K线数上限 */
    public static final int SIDEWAYS_MAX_BARS = 5;

    /** 横盘波动上限 */
    public static final double SIDEWAYS_MAX_VOLATILITY = 0.30;
}
