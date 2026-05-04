package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.strategy.TradeStrategy;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 自动扫描策略包下所有实现了TradeStrategy接口的类
 */
public class StrategyScanner {

    /**
     * 扫描指定包下所有策略类
     * @param packageName 包名，如 "com.ruoyi.web.okx.strategy"
     * @return 策略实例列表
     */
    public static List<TradeStrategy> scanStrategies(String packageName) {
        List<TradeStrategy> strategies = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File file = new File(resource.getFile());
                if (file.isDirectory()) {
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            if (f.getName().endsWith(".class")) {
                                String className = packageName + "." + f.getName().replace(".class", "");
                                try {
                                    Class<?> clazz = Class.forName(className);
                                    if (TradeStrategy.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                                        TradeStrategy strategy = (TradeStrategy) clazz.getDeclaredConstructor().newInstance();
                                        strategies.add(strategy);
                                    }
                                } catch (Exception e) {
                                    System.out.println("跳过策略类：" + className + "，原因：" + e.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strategies;
    }
}