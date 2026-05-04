package com.ruoyi.web.okx.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class KLineVO {
    private long timestamp;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
}