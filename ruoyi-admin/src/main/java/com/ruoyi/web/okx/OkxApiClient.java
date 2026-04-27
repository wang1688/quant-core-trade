package com.ruoyi.web.okx;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;

public class OkxApiClient {

    private static final String BASE_URL = "https://www.okx.com";
    private final String apiKey;
    private final String secretKey;
    private final String passphrase;


    private final HttpClient httpClient = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890))) // 改成你自己的代理端口
            .build();

    public OkxApiClient(String apiKey, String secretKey, String passphrase) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.passphrase = passphrase;
    }

    public String getAccountBalance(String ccy) throws Exception {
        String path = "/api/v5/account/balance";
        if (ccy != null && !ccy.isEmpty()) {
            path += "?ccy=" + ccy;
        }
        return get(path);
    }

    private String get(String path) throws Exception {
        String timestamp = Instant.now().toString().replaceAll("\\.\\d+", "");
        // 签名只用纯路径（不含query string以外的部分），OKX要求带query
        String sign = sign(timestamp, "GET", path, "");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("OK-ACCESS-KEY", apiKey)
                .header("OK-ACCESS-SIGN", sign)
                .header("OK-ACCESS-TIMESTAMP", timestamp)
                .header("OK-ACCESS-PASSPHRASE", passphrase)
                .header("Content-Type", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String sign(String timestamp, String method, String path, String body) throws Exception {
        String preHash = timestamp + method + path + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(preHash.getBytes()));
    }
}
