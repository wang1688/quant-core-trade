package com.ruoyi.web.okx.strategynew.enums;

/**
 * 时间级别
 */
public enum Timeframe {
    M1("1m", 1),
    M5("5m", 5),
    M15("15m", 15),
    H1("1H", 60),
    H4("4H", 240);

    private final String code;
    private final int minutes;

    Timeframe(String code, int minutes) {
        this.code = code;
        this.minutes = minutes;
    }

    public String getCode() {
        return code;
    }

    public int getMinutes() {
        return minutes;
    }
}
