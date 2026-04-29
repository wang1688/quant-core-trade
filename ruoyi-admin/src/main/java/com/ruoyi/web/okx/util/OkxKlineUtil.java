package com.ruoyi.web.okx.util;

import com.ruoyi.web.okx.vo.KLineVO;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class OkxKlineUtil {

    private static final String API_KEY = "56fa6a8e-5bb0-4ae4-80f8-2227a038587a";
    private static final String API_SECRET = "A80CCCC49227189F9442139DE57AEA57";
    private static final String PASSPHRASE = "King$168$hello#168#";
    private static final String INST_ID = "BTC-USDT-SWAP";
    private static final String BAR = "15m";
    private static final int LIMIT = 200;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890)))
            .build();

    public static String getOkxKline() throws Exception {
        String timestamp = getUtcTime();
        String method = "GET";
        String path = "/api/v5/market/candles?instId=" + INST_ID + "&bar=" + BAR + "&limit=" + LIMIT;
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

    public static List<KLineVO> parseKline(String json) {
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
            Collections.reverse(list);
        } catch (Exception ignored) {}
        return list;
    }

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