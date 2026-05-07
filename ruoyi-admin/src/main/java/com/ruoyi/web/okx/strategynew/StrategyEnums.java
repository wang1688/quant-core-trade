package com.ruoyi.web.okx.strategynew;

/**
 * 策略枚举定义
 */
public class StrategyEnums {

    /**
     * 品种类型
     */
    public enum Symbol {
        BTC, ETH
    }

    /**
     * 趋势方向
     */
    public enum TrendDirection {
        UP(1, "多头"),
        DOWN(-1, "空头"),
        FLAT(0, "震荡");

        private final int value;
        private final String desc;

        TrendDirection(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public int getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        public static TrendDirection fromValue(int value) {
            for (TrendDirection dir : values()) {
                if (dir.value == value) return dir;
            }
            return FLAT;
        }
    }

    /**
     * 波动率等级
     */
    public enum VolatilityLevel {
        LOW("低波"),
        MID("中波"),
        HIGH("高波"),
        EXTREME("极端高波");

        private final String desc;

        VolatilityLevel(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 1H确认规则类型
     */
    public enum ConfirmationType {
        STRICT("严格确认"),
        LOOSE("宽松确认"),
        FREE("免确认");

        private final String desc;

        ConfirmationType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 动能等级
     */
    public enum MomentumLevel {
        NONE("无动能", 0),
        WEAK("弱动能", 0.8),
        MID("中动能", 1.2),
        STRONG("强动能", 1.6);

        private final String desc;
        private final double coefficient;

        MomentumLevel(String desc, double coefficient) {
            this.desc = desc;
            this.coefficient = coefficient;
        }

        public String getDesc() {
            return desc;
        }

        public double getCoefficient() {
            return coefficient;
        }
    }

    /**
     * 缓冲期阶段类型
     */
    public enum BufferStage {
        START("刚起涨/下跌阶段"),
        CONSOLIDATION("中继阶段"),
        END("末端阶段");

        private final String desc;

        BufferStage(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 策略类型
     */
    public enum StrategyType {
        CORE_TREND("中枢核心策略"),
        RANGE_OSCILLATION("区间震荡策略"),
        TREND_REVERSAL("趋势末端反转策略"),
        TREND_ACCELERATION("趋势加速策略"),
        FALSE_BREAKOUT("假突破策略"),
        NAKED_K("裸K交易策略"),
        TRIAL_15M("15M试单策略");

        private final String desc;

        StrategyType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 入场方式
     */
    public enum EntryMethod {
        PULLBACK("回踩入场"),
        SIDEWAYS_PULLBACK("横盘回踩入场"),
        BREAKOUT("突破入场"),
        REVERSAL("反转入场"),
        ACCELERATION("加速入场"),
        FALSE_BREAK("假突破入场");

        private final String desc;

        EntryMethod(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 挂单优先级
     */
    public enum OrderPriority {
        PRIORITY_1(1, "5M EMA13"),
        PRIORITY_2(2, "15M前高/前低"),
        PRIORITY_3(3, "5M EMA6");

        private final int level;
        private final String desc;

        OrderPriority(int level, String desc) {
            this.level = level;
            this.desc = desc;
        }

        public int getLevel() {
            return level;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 裸K形态强度
     */
    public enum NakedKStrength {
        SUPER_STRONG("超强形态", 1.4),
        STRONG("强形态", 1.4),
        MID("中等形态", 1.2),
        NORMAL("一般形态", 1.0),
        WEAK("次强形态", 0.8);

        private final String desc;
        private final double coefficient;

        NakedKStrength(String desc, double coefficient) {
            this.desc = desc;
            this.coefficient = coefficient;
        }

        public String getDesc() {
            return desc;
        }

        public double getCoefficient() {
            return coefficient;
        }
    }

    /**
     * K线级别
     */
    public enum TimeFrame {
        M1("1M", 1),
        M5("5M", 5),
        M15("15M", 15),
        H1("1H", 60),
        H4("4H", 240);

        private final String desc;
        private final int minutes;

        TimeFrame(String desc, int minutes) {
            this.desc = desc;
            this.minutes = minutes;
        }

        public String getDesc() {
            return desc;
        }

        public int getMinutes() {
            return minutes;
        }
    }
}
