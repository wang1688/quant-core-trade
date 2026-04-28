package com.ruoyi.web.okx;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;

/**
 * OKX 配置类
 * 功能：将 OkxApiClient 注册为 Spring 单例 Bean，全局复用
 */
@Configuration
@EnableConfigurationProperties(OkxProperties.class) // 启用配置绑定
public class OkxConfig {

    /**
     * 单例：OKX API 客户端（全局唯一实例）
     * Spring 默认 singleton，容器启动时创建，全局共享
     */
    @Bean
    public OkxApiClient okxApiClient(OkxProperties okxProperties) {
        // 从全局配置读取参数
        String apiKey = okxProperties.getApiKey();
        String secretKey = okxProperties.getSecretKey();
        String passphrase = okxProperties.getPassphrase();
        boolean simulated = okxProperties.isSimulated();

        // 全局 HttpClient（单例，复用连接池）
        HttpClient httpClient = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1", 7890)))
                .build();

        // 返回单例客户端
        return new OkxApiClient(apiKey, secretKey, passphrase, simulated, httpClient);
    }
}