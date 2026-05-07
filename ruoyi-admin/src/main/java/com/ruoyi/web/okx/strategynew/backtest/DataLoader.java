package com.ruoyi.web.okx.strategynew.backtest;

import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.strategynew.model.KLine;
import com.ruoyi.web.okx.vo.KLineVO;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据加载器
 */
public class DataLoader {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 从OKX API加载2年历史K线数据
     */
    public static List<KLine> loadFromOkx(OkxApiClient okxClient, String instId, String timeframe) {
        List<KLine> klines = new ArrayList<>();

        try {
            System.out.println("正在从OKX加载 " + instId + " " + timeframe + " 数据...");
            KLineVO[] klineArray = okxClient.getTwoYearsKLineArray(instId, timeframe);

            for (KLineVO vo : klineArray) {
                LocalDateTime timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(vo.getTimestamp()),
                    ZoneId.systemDefault()
                );

                KLine kline = new KLine(
                    timestamp,
                    BigDecimal.valueOf(vo.getOpen()),
                    BigDecimal.valueOf(vo.getHigh()),
                    BigDecimal.valueOf(vo.getLow()),
                    BigDecimal.valueOf(vo.getClose()),
                    BigDecimal.valueOf(vo.getVolume()),
                    timeframe
                );

                klines.add(kline);
            }

            System.out.println("成功加载 " + klines.size() + " 根K线");
        } catch (Exception e) {
            System.err.println("从OKX加载数据失败: " + e.getMessage());
            e.printStackTrace();
        }

        return klines;
    }

    /**
     * 从CSV文件加载K线数据
     * CSV格式: timestamp,open,high,low,close,volume
     */
    public static List<KLine> loadFromCSV(String filePath, String timeframe) {
        List<KLine> klines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 6) {
                    continue;
                }

                try {
                    LocalDateTime timestamp = LocalDateTime.parse(values[0], FORMATTER);
                    BigDecimal open = new BigDecimal(values[1]);
                    BigDecimal high = new BigDecimal(values[2]);
                    BigDecimal low = new BigDecimal(values[3]);
                    BigDecimal close = new BigDecimal(values[4]);
                    BigDecimal volume = new BigDecimal(values[5]);

                    KLine kline = new KLine(timestamp, open, high, low, close, volume, timeframe);
                    klines.add(kline);
                } catch (Exception e) {
                    System.err.println("解析行数据失败: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("读取文件失败: " + filePath);
            e.printStackTrace();
        }

        return klines;
    }

    /**
     * 生成模拟数据（用于测试）
     */
    public static List<KLine> generateMockData(int count, String timeframe, BigDecimal startPrice) {
        List<KLine> klines = new ArrayList<>();
        LocalDateTime timestamp = LocalDateTime.now().minusDays(count);

        BigDecimal currentPrice = startPrice;
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < count; i++) {
            double changePercent = (random.nextDouble() - 0.5) * 0.02;
            BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePercent));

            BigDecimal open = currentPrice;
            BigDecimal close = currentPrice.add(change);
            BigDecimal high = open.max(close).multiply(BigDecimal.valueOf(1 + random.nextDouble() * 0.005));
            BigDecimal low = open.min(close).multiply(BigDecimal.valueOf(1 - random.nextDouble() * 0.005));
            BigDecimal volume = BigDecimal.valueOf(1000 + random.nextInt(9000));

            KLine kline = new KLine(timestamp, open, high, low, close, volume, timeframe);
            klines.add(kline);

            currentPrice = close;
            timestamp = timestamp.plusMinutes(getMinutes(timeframe));
        }

        return klines;
    }

    /**
     * 获取时间框架对应的分钟数
     */
    private static int getMinutes(String timeframe) {
        switch (timeframe) {
            case "1m":
                return 1;
            case "5m":
                return 5;
            case "15m":
                return 15;
            case "1H":
                return 60;
            case "4H":
                return 240;
            default:
                return 15;
        }
    }

    /**
     * 从较小级别K线聚合到较大级别
     */
    public static List<KLine> aggregateKlines(List<KLine> sourceKlines, String sourceTimeframe, String targetTimeframe) {
        int sourceMinutes = getMinutes(sourceTimeframe);
        int targetMinutes = getMinutes(targetTimeframe);

        if (targetMinutes <= sourceMinutes) {
            return sourceKlines;
        }

        int ratio = targetMinutes / sourceMinutes;
        List<KLine> aggregated = new ArrayList<>();

        for (int i = 0; i < sourceKlines.size(); i += ratio) {
            if (i + ratio > sourceKlines.size()) {
                break;
            }

            LocalDateTime timestamp = sourceKlines.get(i).getTimestamp();
            BigDecimal open = sourceKlines.get(i).getOpen();
            BigDecimal close = sourceKlines.get(i + ratio - 1).getClose();

            BigDecimal high = sourceKlines.get(i).getHigh();
            BigDecimal low = sourceKlines.get(i).getLow();
            BigDecimal volume = BigDecimal.ZERO;

            for (int j = i; j < i + ratio; j++) {
                KLine kline = sourceKlines.get(j);
                if (kline.getHigh().compareTo(high) > 0) {
                    high = kline.getHigh();
                }
                if (kline.getLow().compareTo(low) < 0) {
                    low = kline.getLow();
                }
                volume = volume.add(kline.getVolume());
            }

            KLine aggregatedKline = new KLine(timestamp, open, high, low, close, volume, targetTimeframe);
            aggregated.add(aggregatedKline);
        }

        return aggregated;
    }
}
