package com.ruoyi.web.okx.strategynew;

/**
 * 全局常量定义
 */
public class Constants {

    // 品种定义
    public static final String BTC = "BTC";
    public static final String ETH = "ETH";

    // 杠杆倍数
    public static final int LEVERAGE = 100;

    // 起投资金（美金）
    public static final int BASE_CAPITAL = 1000;

    // BTC专属跳数
    public static final int BTC_FEE_BUFFER = 500;
    public static final int BTC_FEE_BUFFER_HIGH_VOL = 1000;
    public static final int BTC_VALID_BOX_HEIGHT = 7000;
    public static final int BTC_BREAKOUT_CONFIRM = 3500;
    public static final int BTC_OVERLAP_BUFFER = 2100;

    // ETH专属跳数
    public static final int ETH_FEE_BUFFER = 200;
    public static final int ETH_FEE_BUFFER_HIGH_VOL = 400;
    public static final int ETH_VALID_BOX_HEIGHT = 2200;
    public static final int ETH_BREAKOUT_CONFIRM = 1000;
    public static final int ETH_OVERLAP_BUFFER = 650;

    // EMA30角度阈值（度）
    public static final double EMA30_ANGLE_LOW_VOL = 14.0;
    public static final double EMA30_ANGLE_MID_VOL = 16.5;
    public static final double EMA30_ANGLE_HIGH_VOL = 18.0;
    public static final double EMA30_ANGLE_BUFFER = 12.0;
    public static final double EMA30_ANGLE_STRONG = 22.0;
    public static final double EMA30_ANGLE_VERY_STRONG = 25.0;

    // DIF乖离率阈值
    public static final double DIF_STRONG = 0.12;
    public static final double DIF_MID = 0.05;
    public static final double DIF_WEAK = 0.05;
    public static final double DIF_REVERSE = -0.12;
    public static final double DIF_5M_STRONG = 0.10;

    // 波动率分级（百分比）
    public static final double BTC_LOW_VOL = 0.8;
    public static final double BTC_MID_VOL_MIN = 0.8;
    public static final double BTC_MID_VOL_MAX = 1.6;
    public static final double BTC_HIGH_VOL = 1.6;
    public static final double BTC_EXTREME_VOL = 3.0;

    public static final double ETH_LOW_VOL = 1.0;
    public static final double ETH_MID_VOL_MIN = 1.0;
    public static final double ETH_MID_VOL_MAX = 2.0;
    public static final double ETH_HIGH_VOL = 2.0;
    public static final double ETH_EXTREME_VOL = 4.0;

    // 箱体重叠度要求
    public static final double BOX_OVERLAP_RATIO = 0.80;

    // 量能倍数
    public static final double VOLUME_NORMAL = 1.5;
    public static final double VOLUME_HIGH = 2.0;
    public static final double VOLUME_STRONG = 2.5;
    public static final double VOLUME_SHRINK = 0.6;

    // 信号评分标准
    public static final double SIGNAL_SCORE_SUPER = 9.0;
    public static final double SIGNAL_SCORE_STRONG = 7.0;
    public static final double SIGNAL_SCORE_MID = 5.0;
    public static final double SIGNAL_SCORE_LOW = 4.5;

    // 仓位上限
    public static final double POSITION_LIMIT_SINGLE = 0.90;
    public static final double POSITION_LIMIT_COUNTER = 0.30;
    public static final double POSITION_LIMIT_NAKED = 0.40;
    public static final double POSITION_LIMIT_RANGE = 0.05;

    // 风险敞口档位
    public static final double[] RISK_EXPOSURE = {
        0.7,  // 负一档
        1.0,  // 1档（默认）
        1.3,  // 2档
        1.6,  // 3档
        1.9,  // 4档
        2.1,  // 5档
        3.0,  // 6档
        4.0,  // 7档
        5.0   // 8档
    };

    // 日亏损阈值
    public static final double[] DAILY_LOSS_THRESHOLD = {
        0.06, 0.06, 0.06, 0.08, 0.10, 0.12, 0.16, 0.18, 0.21
    };

    // 累计亏损阈值
    public static final double[] CUMULATIVE_LOSS_THRESHOLD = {
        0.15, 0.10, 0.10, 0.10, 0.15, 0.20, 0.25, 0.30, 0.30
    };

    // 连续亏损限制
    public static final int CONSECUTIVE_LOSS_LIMIT = 6;
    public static final int TRIAL_LOSS_LIMIT_1H = 2;
    public static final int TRIAL_LOSS_LIMIT_4H = 3;

    // 时间止损（15M K线根数）
    public static final int TIME_STOP_RANGE_GRID = 6;
    public static final int TIME_STOP_RANGE_BREAKOUT = 4;
    public static final int TIME_STOP_REVERSAL = 2;
    public static final int TIME_STOP_ACCELERATION = 2;
    public static final int TIME_STOP_FAKE_BREAKOUT = 2;
    public static final int TIME_STOP_TRIAL = 2;
    public static final int TIME_STOP_NAKED = 4;
}
