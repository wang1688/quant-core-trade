package com.ruoyi.web.okx.test;

import com.ruoyi.web.okx.vo.KLineVO;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OkxRealKlineBacktest {

    // ========== 全部硬编码，你什么都不用改 ==========
    private static final String API_KEY       = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String API_SECRET    = "A80CCCC49227189F9442139DE57AEA57";
    private static final String PASSPHRASE    = "King$168$hello#168#";
    private static final String INST_ID       = "BTC-USDT-SWAP";
    private static final String BAR           = "15m";
    private static final int    LIMIT         = 200;

    // 你的代理
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890)))
            .build();

    public static void main(String[] args) {
        try {
            // 1. 拉取真实K线
            String json = getOkxKline();
            System.out.println("OKX返回: " + json);

            // 2. 解析K线
            List<KLineVO> klineList = parseKline(json);
            Collections.reverse(klineList);
            KLineVO[] kline = klineList.toArray(new KLineVO[0]);

            if (kline.length == 0) {
                System.out.println("未获取到K线");
                return;
            }

            // 3. EMA策略
            double[] ema6  = ema(kline, 6);
            double[] ema30 = ema(kline, 30);

            int    tradeCount = 0;
            double profit     = 0;
            boolean hold      = false;

            for (int i = 1; i < kline.length; i++) {
                boolean buy  = ema6[i-1] < ema30[i-1] && ema6[i] > ema30[i];
                boolean sell = ema6[i-1] > ema30[i-1] && ema6[i] < ema30[i];

                if (buy && !hold) {
                    tradeCount++;
                    hold = true;
                }
                if (sell && hold) {
                    profit += (kline[i].getClose() - kline[i-1].getClose()) / kline[i-1].getClose();
                    hold = false;
                }
            }

            // 输出结果
            System.out.println("======= 真实回测结果 =======");
            System.out.println("K线数量: " + kline.length);
            System.out.println("交易次数: " + tradeCount);
            System.out.println("收益率: " + String.format("%.2f%%", profit * 100));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ========== OKX官方V5接口：获取K线 ==========
    private static String getOkxKline() throws Exception {
        String timestamp = getUtcTime();
        String method = "GET";
        String path = "/api/v5/market/candles?instId=BTC-USDT-SWAP&bar=15m&limit=200";
        String sign = sign(timestamp, method, path, "", API_SECRET);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.okx.com" + path))
                .header("OK-ACCESS-KEY", API_KEY)
                .header("OK-ACCESS-SIGN", sign)
                .header("OK-ACCESS-TIMESTAMP", timestamp)
                .header("OK-ACCESS-PASSPHRASE", PASSPHRASE)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // ========== EMA计算 ==========
    private static double[] ema(KLineVO[] kline, int n) {
        double[] ema = new double[kline.length];
        ema[0] = kline[0].getClose();
        double mul = 2.0 / (n + 1);
        for (int i = 1; i < kline.length; i++) {
            ema[i] = kline[i].getClose() * mul + ema[i-1] * (1 - mul);
        }
        return ema;
    }

    // ========== 解析K线 ==========
    private static List<KLineVO> parseKline(String json) {
        List<KLineVO> list = new ArrayList<>();
        try {
            int s = json.indexOf("[[");
            int e = json.lastIndexOf("]]") + 2;
            String arr = json.substring(s, e);
            String[] rows = arr.split("\\],\\[");
            for (String row : rows) {
                String[] f = row.replace("[", "").replace("]", "").replace("\"", "").split(",");
                KLineVO vo = new KLineVO();
                vo.setClose(Double.parseDouble(f[4]));
                list.add(vo);
            }
        } catch (Exception ignored) {}
        return list;
    }

    // ========== OKX签名 ==========
    private static String sign(String timestamp, String method, String path, String body, String secret) throws Exception {
        String msg = timestamp + method + path + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(msg.getBytes(StandardCharsets.UTF_8)));
    }

    private static String getUtcTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return String.format("%tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS.%1$tLZ", cal);
    }
}