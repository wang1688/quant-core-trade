package com.ruoyi.web.okx;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Base64;

/**
 * OKX V5 API 客户端（单例模式）
 * 全局唯一实例，线程安全
 * 包含全部17个账户接口
 */
public class OkxApiClient {

    /**
     * OKX 统一域名
     */
    private static final String BASE_URL = "https://www.okx.com";

    // 全局配置（只读，线程安全）
    private final String apiKey;
    private final String secretKey;
    private final String passphrase;
    private final boolean simulated;

    // 全局 HttpClient（单例，复用连接池）
    private final HttpClient httpClient;

    /**
     * 构造方法（由 Spring @Bean 调用）
     */
    public OkxApiClient(String apiKey, String secretKey, String passphrase,
                        boolean simulated, HttpClient httpClient) {
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.passphrase = passphrase;
        this.simulated = simulated;
        this.httpClient = httpClient;
    }

    // ==================== 1. 获取账户余额 ====================
    public String getAccountBalance(String ccy) throws Exception {
        String path = "/api/v5/account/balance";
        if (ccy != null && !ccy.isBlank()) {
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    // ==================== 2. 获取持仓 ====================
    public String getPositions(String instType, String instId) throws Exception {
        StringBuilder path = new StringBuilder("/api/v5/account/positions");
        boolean hasParam = false;
        if (instType != null) {
            path.append("?instType=").append(instType);
            hasParam = true;
        }
        if (instId != null) {
            path.append(hasParam ? "&" : "?").append("instId=").append(instId);
        }
        return doGet(path.toString());
    }

    // ==================== 3. 获取账户配置 ====================
    public String getAccountConfig() throws Exception {
        return doGet("/api/v5/account/config");
    }

    // ==================== 4. 设置持仓模式 ====================
    public String setPositionMode(String posMode) throws Exception {
        String body = "{\"posMode\":\"" + posMode + "\"}";
        return doPost("/api/v5/account/set-position-mode", body);
    }

    // ==================== 5. 设置杠杆 ====================
    public String setLeverage(String instId, String lever, String mgnMode) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"lever\":\"%s\",\"mgnMode\":\"%s\"}",
                instId, lever, mgnMode
        );
        return doPost("/api/v5/account/set-leverage", body);
    }

    // ==================== 6. 获取账单 ====================
    public String getBills(String instType, String ccy, String type) throws Exception {
        StringBuilder path = new StringBuilder("/api/v5/account/bills");
        boolean hasParam = false;
        if (instType != null) {
            path.append("?instType=").append(instType);
            hasParam = true;
        }
        if (ccy != null) {
            path.append(hasParam ? "&" : "?").append("ccy=").append(ccy);
            hasParam = true;
        }
        if (type != null) {
            path.append(hasParam ? "&" : "?").append("type=").append(type);
        }
        return doGet(path.toString());
    }

    // ==================== 7. 获取历史账单 ====================
    public String getBillsHistory(String instType, String ccy) throws Exception {
        String path = "/api/v5/account/bills-history?instType=" + instType;
        if (ccy != null) {
            path += "&ccy=" + ccy;
        }
        return doGet(path);
    }

    // ==================== 8. 获取借币利率 ====================
    public String getInterestRate(String ccy) throws Exception {
        String path = "/api/v5/account/interest-rate";
        if (ccy != null) {
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    // ==================== 9. 最大可买数量 ====================
    public String getMaxSize(String instId, String tdMode) throws Exception {
        return doGet("/api/v5/account/max-size?instId=" + instId + "&tdMode=" + tdMode);
    }

    // ==================== 10. 最大可平数量 ====================
    public String getMaxAvail(String instId, String mgnMode) throws Exception {
        return doGet("/api/v5/account/max-avail?instId=" + instId + "&mgnMode=" + mgnMode);
    }

    // ==================== 11. 调整保证金 ====================
    public String adjustMargin(String posId, String amt, String type) throws Exception {
        String body = String.format(
                "{\"posId\":\"%s\",\"amt\":\"%s\",\"type\":\"%s\"}",
                posId, amt, type
        );
        return doPost("/api/v5/account/adjust-margin", body);
    }

    // ==================== 12. 获取手续费率 ====================
    public String getTradeFeeRate(String instType, String instId) throws Exception {
        return doGet("/api/v5/account/trade-fee-rate?instType=" + instType + "&instId=" + instId);
    }

    // ==================== 13. 账户资产估值 ====================
    public String getAccountEquity() throws Exception {
        return doGet("/api/v5/account/equity");
    }

    // ==================== 14. 逐仓借币 ====================
    public String borrowIsolated(String ccy, String amt, String instId) throws Exception {
        String body = String.format(
                "{\"ccy\":\"%s\",\"amt\":\"%s\",\"instId\":\"%s\"}",
                ccy, amt, instId
        );
        return doPost("/api/v5/account/borrow-isolated", body);
    }

    // ==================== 15. 逐仓还币 ====================
    public String repayIsolated(String ccy, String amt, String instId) throws Exception {
        String body = String.format(
                "{\"ccy\":\"%s\",\"amt\":\"%s\",\"instId\":\"%s\"}",
                ccy, amt, instId
        );
        return doPost("/api/v5/account/repay-isolated", body);
    }

    // ==================== 16. 爆仓记录 ====================
    public String getLiquidationHistory(String instType, String mgnMode) throws Exception {
        return doGet("/api/v5/account/liquidation-history?instType=" + instType + "&mgnMode=" + mgnMode);
    }

    // ==================== 17. 借贷历史 ====================
    public String getBorrowHistory(String ccy) throws Exception {
        String path = "/api/v5/account/borrow-history";
        if (ccy != null) {
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }


    // ========================== 【交易模块接口】 ==========================

    /**
     * 1. 下单
     */
    public String placeOrder(String instId, String tdMode, String side, String ordType,
                             String sz, String px) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"tdMode\":\"%s\",\"side\":\"%s\",\"ordType\":\"%s\",\"sz\":\"%s\",\"px\":\"%s\"}",
                instId, tdMode, side, ordType, sz, px
        );
        return doPost("/api/v5/trade/order", body);
    }

    /**
     * 2. 批量下单
     */
    public String batchOrders(String instId, String tdMode, String side, String ordType,
                              String sz, String px) throws Exception {
        String body = "[{\"instId\":\"" + instId + "\",\"tdMode\":\"" + tdMode + "\",\"side\":\"" + side + "\",\"ordType\":\"" + ordType + "\",\"sz\":\"" + sz + "\",\"px\":\"" + px + "\"}]";
        return doPost("/api/v5/trade/batch-orders", body);
    }

    /**
     * 3. 撤单
     */
    public String cancelOrder(String instId, String ordId) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"ordId\":\"%s\"}", instId, ordId);
        return doPost("/api/v5/trade/cancel-order", body);
    }

    /**
     * 4. 批量撤单
     */
    public String batchCancelOrders(String instId, String ordIds) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"ordIds\":[%s]}", instId, ordIds);
        return doPost("/api/v5/trade/cancel-batch-orders", body);
    }

    /**
     * 5. 修改订单
     */
    public String amendOrder(String instId, String ordId, String newSz, String newPx) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"ordId\":\"%s\",\"newSz\":\"%s\",\"newPx\":\"%s\"}",
                instId, ordId, newSz, newPx
        );
        return doPost("/api/v5/trade/amend-order", body);
    }

    /**
     * 6. 获取未成交订单
     */
    public String getPendingOrders(String instId, String ordType) throws Exception {
        String path = "/api/v5/trade/orders-pending";
        if (instId != null) path += "?instId=" + instId;
        if (ordType != null) path += "&ordType=" + ordType;
        return doGet(path);
    }

    /**
     * 7. 获取单个订单信息
     */
    public String getOrder(String instId, String ordId) throws Exception {
        return doGet("/api/v5/trade/order?instId=" + instId + "&ordId=" + ordId);
    }

    /**
     * 8. 获取历史订单
     */
    public String getOrdersHistory(String instId, String state) throws Exception {
        return doGet("/api/v5/trade/orders-history?instId=" + instId + "&state=" + state);
    }

    /**
     * 9. 最近成交记录
     */
    public String getTrades(String instId, String ordId) throws Exception {
        String path = "/api/v5/trade/fills";
        if (instId != null) path += "?instId=" + instId;
        if (ordId != null) path += "&ordId=" + ordId;
        return doGet(path);
    }

    /**
     * 10. 账户成交历史
     */
    public String getFillsHistory(String instId) throws Exception {
        return doGet("/api/v5/trade/fills-history?instId=" + instId);
    }


    // ========================== 【行情 Market 模块 完整接口】 ==========================
    /**
     * 1. 获取单个交易对Ticker行情
     * @param instId 交易对ID
     */
    public String getTicker(String instId) throws Exception {
        String path = "/api/v5/market/ticker";
        if (instId != null) {
            path += "?instId=" + instId;
        }
        return doGet(path);
    }

    /**
     * 2. 获取全部交易对Ticker行情
     */
    public String getTickerAll() throws Exception {
        return doGet("/api/v5/market/tickers?instType=SPOT");
    }

    /**
     * 3. 获取K线数据
     * @param instId 【必填】交易对
     * @param bar    【可选】K线周期 1m/5m/15m/1H/4H/1D
     * @param limit  【可选】返回条数
     */
    public String getCandlesticks(String instId, String bar, String limit) throws Exception {
        StringBuilder path = new StringBuilder("/api/v5/market/candlesticks?instId=" + instId);
        if (bar != null) {
            path.append("&bar=").append(bar);
        }
        if (limit != null) {
            path.append("&limit=").append(limit);
        }
        return doGet(path.toString());
    }

    /**
     * 4. 获取盘口深度
     * @param instId 【必填】交易对
     * @param size   【可选】深度档位数量
     */
    public String getOrderBook(String instId, String size) throws Exception {
        StringBuilder path = new StringBuilder("/api/v5/market/books?instId=" + instId);
        if (size != null) {
            path.append("&size=").append(size);
        }
        return doGet(path.toString());
    }

    /**
     * 5. 获取最新成交明细
     * @param instId 【必填】交易对
     * @param limit  【可选】返回条数
     */
    public String getTradesDetail(String instId, String limit) throws Exception {
        StringBuilder path = new StringBuilder("/api/v5/market/trades?instId=" + instId);
        if (limit != null) {
            path.append("&limit=").append(limit);
        }
        return doGet(path.toString());
    }

    /**
     * 6. 获取合约资金费率
     * @param instId 【必填】合约交易对
     */
    public String getFundingRate(String instId) throws Exception {
        return doGet("/api/v5/market/funding-rate?instId=" + instId);
    }

    /**
     * 7. 获取资金费率历史
     * @param instId 【必填】合约交易对
     * @param limit  【可选】数据条数
     */
    public String getFundingRateHistory(String instId, String limit) throws Exception {
        StringBuilder path = new StringBuilder("/api/v5/market/funding-rate-history?instId=" + instId);
        if (limit != null) {
            path.append("&limit=").append(limit);
        }
        return doGet(path.toString());
    }

    /**
     * 8. 获取指数行情
     * @param instId 【必填】指数交易对
     */
    public String getIndexTicker(String instId) throws Exception {
        return doGet("/api/v5/market/index-ticker?instId=" + instId);
    }

    /**
     * 9. 获取标记价格
     * @param instId 【必填】交易对
     */
    public String getMarkPrice(String instId) throws Exception {
        return doGet("/api/v5/market/mark-price?instId=" + instId);
    }

    /**
     * 10. 获取交易对基础配置规格
     * @param instType 【必填】产品类型 SPOT/SWAP/FUTURES/OPTION
     * @param instId   【可选】指定单个交易对
     */
    public String getInstrument(String instType, String instId) throws Exception {
        StringBuilder path = new StringBuilder("/api/v5/public/instruments?instType=" + instType);
        if (instId != null) {
            path.append("&instId=").append(instId);
        }
        return doGet(path.toString());
    }


    // ========================== 【资产模块 完整接口（有序号）】 ==========================

    /**
     * 1. 获取资金账户余额
     */
    public String getAssetBalance(String ccy) throws Exception {
        String path = "/api/v5/asset/balances";
        if (ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 2. 获取账户总资产估值
     */
    public String getAssetAllValuation() throws Exception {
        return doGet("/api/v5/asset/asset-valuation");
    }

    /**
     * 3. 资金划转（账户互转）
     */
    public String transfer(String ccy, String amt, String from, String to, String instId) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"instId\":\"%s\"}",
                ccy, amt, from, to, instId);
        return doPost("/api/v5/asset/transfer", body);
    }

    /**
     * 4. 获取充值地址
     */
    public String getDepositAddress(String ccy, String chain) throws Exception {
        String path = "/api/v5/asset/deposit-address?ccy=" + ccy;
        if (chain != null) path += "&chain=" + chain;
        return doGet(path);
    }

    /**
     * 5. 获取充值记录
     */
    public String getDepositHistory(String ccy) throws Exception {
        String path = "/api/v5/asset/deposit-history";
        if (ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 6. 提币
     */
    public String withdraw(String ccy, String chain, String toAddr, String amt, String fee) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"chain\":\"%s\",\"toAddr\":\"%s\",\"amt\":\"%s\",\"fee\":\"%s\"}",
                ccy, chain, toAddr, amt, fee);
        return doPost("/api/v5/asset/withdrawal", body);
    }

    /**
     * 7. 获取提币记录
     */
    public String getWithdrawHistory(String ccy) throws Exception {
        String path = "/api/v5/asset/withdrawal-history";
        if (ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    // ========================== 【公共模块 接口（有序号）】 ==========================

    /**
     * 1. 获取服务器时间（用于签名时间校准）
     */
    public String getServerTime() throws Exception {
        return doGet("/api/v5/public/time");
    }

    /**
     * 2. 获取币种配置信息
     */
    public String getCurrencies(String ccy) throws Exception {
        String path = "/api/v5/asset/currencies";
        if (ccy != null) {
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 3. 获取法币汇率（如 USD/CNY）
     */
    public String getExchangeRate() throws Exception {
        return doGet("/api/v5/public/exchange-rate");
    }


    // ========================== 【合约仓位扩展模块 12个】 ==========================
    /**
     * 1. 查看所有合约持仓
     */
    public String getPositionsAll(String instType) throws Exception {
        return doGet("/api/v5/account/positions?instType="+instType);
    }

    /**
     * 2. 调整逐仓保证金
     */
    public String positionMargin(String instId, String posId, String amt, String type) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"posId\":\"%s\",\"amt\":\"%s\",\"type\":\"%s\"}",instId,posId,amt,type);
        return doPost("/api/v5/account/position/margin", body);
    }

    /**
     * 3. 一键平仓所有持仓
     */
    public String closeAllPosition(String instType) throws Exception {
        String body = "{\"instType\":\""+instType+"\"}";
        return doPost("/api/v5/trade/close-all-position", body);
    }

    /**
     * 4. 设置持仓止盈止损
     */
    public String setPosSlTp(String instId, String posId, String slPx, String tpPx) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"posId\":\"%s\",\"slPx\":\"%s\",\"tpPx\":\"%s\"}",instId,posId,slPx,tpPx);
        return doPost("/api/v5/trade/position-sl-tp", body);
    }

    /**
     * 5. 获取资金费率历史
     */
    public String fundingRateHistory(String instId, String limit) throws Exception {
        return doGet("/api/v5/market/funding-rate-history?instId="+instId+"&limit="+limit);
    }



    /**
     * 7. 切换杠杆模式
     */
    public String setLeverage(String instId, String lever, String mgnMode, String posSide) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"lever\":\"%s\",\"mgnMode\":\"%s\",\"posSide\":\"%s\"}",instId,lever,mgnMode,posSide);
        return doPost("/api/v5/account/set-leverage", body);
    }

    /**
     * 8. 设置账户模式
     */
    public String setAccountLevel(String acctLv) throws Exception {
        String body = "{\"acctLv\":\""+acctLv+"\"}";
        return doPost("/api/v5/account/set-account-level", body);
    }

    /**
     * 10. 历史结算记录
     */
    public String getSettlements(String instType, String instId) throws Exception {
        return doGet("/api/v5/account/settlements?instType="+instType+"&instId="+instId);
    }

    /**
     * 11. 调整自动追加保证金
     */
    public String setAutoMargin(String instId, String type) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"type\":\"%s\"}",instId,type);
        return doPost("/api/v5/account/set-auto-margin", body);
    }

    /**
     * 12. 风险敞口
     */
    public String getAccountRisk() throws Exception {
        return doGet("/api/v5/account/risk-offset");
    }

    // ==================== 底层 GET 请求 ====================
    private String doGet(String path) throws Exception {
        String timestamp = getTimestamp();
        String sign = generateSign(timestamp, "GET", path, "");

        HttpRequest request = buildBaseHeaders(timestamp, sign)
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();

        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }


    // ========================== 【子账户模块 15个】 ==========================

    /**
     * 1. 获取子账户列表
     */
    public String getSubAccountList() throws Exception {
        return doGet("/api/v5/users/subaccount/list");
    }

    /**
     * 2. 创建子账户
     */
    public String createSubAccount(String subAcct, String label) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\",\"label\":\"%s\"}", subAcct, label);
        return doPost("/api/v5/users/subaccount/create", body);
    }

    /**
     * 3. 子账户资金划转
     */
    public String subAccountTransfer(String ccy, String amt, String from, String to, String fromSubAccount, String toSubAccount) throws Exception {
        String body = String.format(
                "{\"ccy\":\"%s\",\"amt\":\"%s\",\"from\":\"%s\",\"to\":\"%s\",\"fromSubAccount\":\"%s\",\"toSubAccount\":\"%s\"}",
                ccy, amt, from, to, fromSubAccount, toSubAccount
        );
        return doPost("/api/v5/asset/subaccount/transfer", body);
    }

    /**
     * 4. 查询子账户余额
     */
    public String getSubAccountBalance(String subAcct) throws Exception {
        return doGet("/api/v5/asset/subaccount/balances?subAcct=" + subAcct);
    }

    /**
     * 5. 子账户APIKey创建
     */
    public String createSubApiKey(String subAcct, String label, String perm, String ip) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\",\"label\":\"%s\",\"perm\":\"%s\",\"ip\":\"%s\"}", subAcct, label, perm, ip);
        return doPost("/api/v5/users/subaccount/apikey", body);
    }

    /**
     * 6. 查询子账户APIKey
     */
    public String getSubApiKey(String subAcct, String apiKey) throws Exception {
        return doGet("/api/v5/users/subaccount/apikey?subAcct=" + subAcct + "&apiKey=" + apiKey);
    }

    /**
     * 7. 修改子账户APIKey
     */
    public String updateSubApiKey(String subAcct, String apiKey, String label, String perm, String ip) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\",\"apiKey\":\"%s\",\"label\":\"%s\",\"perm\":\"%s\",\"ip\":\"%s\"}", subAcct, apiKey, label, perm, ip);
        return doPost("/api/v5/users/subaccount/modify-apikey", body);
    }

    /**
     * 8. 删除子账户APIKey
     */
    public String deleteSubApiKey(String subAcct, String apiKey) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\",\"apiKey\":\"%s\"}", subAcct, apiKey);
        return doPost("/api/v5/users/subaccount/delete-apikey", body);
    }

    /**
     * 9. 设置子账户权限
     */
    public String setSubAccountPermission(String subAcct, String ip, String perm) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\",\"ip\":\"%s\",\"perm\":\"%s\"}", subAcct, ip, perm);
        return doPost("/api/v5/users/subaccount/set-permission", body);
    }

    /**
     * 10. 冻结子账户
     */
    public String freezeSubAccount(String subAcct, String frozen) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\",\"frozen\":\"%s\"}", subAcct, frozen);
        return doPost("/api/v5/users/subaccount/freeze", body);
    }

    /**
     * 11. 子账户充值地址
     */
    public String getSubDepositAddress(String subAcct, String ccy, String chain) throws Exception {
        return doGet("/api/v5/asset/subaccount/deposit-address?subAcct=" + subAcct + "&ccy=" + ccy + "&chain=" + chain);
    }

    /**
     * 12. 子账户充值记录
     */
    public String getSubDepositHistory(String subAcct, String ccy) throws Exception {
        return doGet("/api/v5/asset/subaccount/deposit-history?subAcct=" + subAcct + "&ccy=" + ccy);
    }

    /**
     * 13. 子账户提币记录
     */
    public String getSubWithdrawHistory(String subAcct, String ccy) throws Exception {
        return doGet("/api/v5/asset/subaccount/withdrawal-history?subAcct=" + subAcct + "&ccy=" + ccy);
    }

    /**
     * 14. 子账户交易流水
     */
    public String getSubBills(String subAcct, String instType, String ccy) throws Exception {
        return doGet("/api/v5/account/subaccount/bills?subAcct=" + subAcct + "&instType=" + instType + "&ccy=" + ccy);
    }

    /**
     * 15. 子账户总资产
     */
    public String getSubAssetValuation(String subAcct) throws Exception {
        return doGet("/api/v5/asset/subaccount/asset-valuation?subAcct=" + subAcct);
    }

    // ========================== 【杠杆&借贷模块 22个】 ==========================
    /**
     * 1. 杠杆账户资产总览
     */
    public String getMarginAccount() throws Exception {
        return doGet("/api/v5/account/margin-account");
    }

    /**
     * 2. 杠杆币种配置
     */
    public String getMarginCurrencies(String ccy) throws Exception {
        String path = "/api/v5/loan/margin-currencies";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 3. 杠杆借入
     */
    public String loanBorrow(String ccy, String amt, String side) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\",\"side\":\"%s\"}",ccy,amt,side);
        return doPost("/api/v5/loan/borrow",body);
    }

    /**
     * 4. 杠杆还款
     */
    public String loanRepay(String ccy, String amt, String borrowId) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\",\"borrowId\":\"%s\"}",ccy,amt,borrowId);
        return doPost("/api/v5/loan/repay",body);
    }

    /**
     * 5. 借还款记录
     */
    public String getLoanBorrowHistory(String ccy) throws Exception {
        String path = "/api/v5/loan/borrow-history";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 6. 还款记录
     */
    public String getLoanRepayHistory(String ccy) throws Exception {
        String path = "/api/v5/loan/repay-history";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 7. 未还清借款列表
     */
    public String getLoanOutstanding(String ccy) throws Exception {
        String path = "/api/v5/loan/outstanding";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 8. 杠杆利息记录
     */
    public String getLoanInterestHistory(String ccy) throws Exception {
        String path = "/api/v5/loan/interest-history";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 9. 全网借贷市场利率
     */
    public String getLoanMarketRate(String ccy) throws Exception {
        String path = "/api/v5/loan/market-rate";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 10. 杠杆风险率
     */
    public String getMarginRiskRate() throws Exception {
        return doGet("/api/v5/account/margin-risk-rate");
    }

    /**
     * 11. 强制平仓杠杆记录
     */
    public String getMarginLiquidation() throws Exception {
        return doGet("/api/v5/account/margin-liquidation");
    }

    /**
     * 12. 一键全部还款
     */
    public String loanRepayAll(String ccy) throws Exception {
        String body = String.format("{\"ccy\":\"%s\"}",ccy);
        return doPost("/api/v5/loan/repay-all",body);
    }

    /**
     * 13. 调整自动还款
     */
    public String setAutoRepay(String autoRepay) throws Exception {
        String body = String.format("{\"autoRepay\":\"%s\"}",autoRepay);
        return doPost("/api/v5/loan/set-auto-repay",body);
    }

    /**
     * 14. 杠杆挂单借贷
     */
    public String loanOffer(String ccy, String amt, String rate) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\",\"rate\":\"%s\"}",ccy,amt,rate);
        return doPost("/api/v5/loan/offer",body);
    }

    /**
     * 15. 撤销借贷挂单
     */
    public String loanCancelOffer(String offerId) throws Exception {
        String body = String.format("{\"offerId\":\"%s\"}",offerId);
        return doPost("/api/v5/loan/cancel-offer",body);
    }

    /**
     * 16. 我的借贷挂单
     */
    public String getLoanOfferPending(String ccy) throws Exception {
        String path = "/api/v5/loan/offer-pending";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 17. 借贷挂单历史
     */
    public String getLoanOfferHistory(String ccy) throws Exception {
        String path = "/api/v5/loan/offer-history";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 18. 借贷市场深度
     */
    public String getLoanMarketBook(String ccy) throws Exception {
        String path = "/api/v5/loan/market-book";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 19. 杠杆限额
     */
    public String getMarginQuota(String ccy) throws Exception {
        String path = "/api/v5/loan/quota";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 20. 跨币种杠杆配置
     */
    public String getCrossMarginConfig() throws Exception {
        return doGet("/api/v5/account/cross-margin-config");
    }

    /**
     * 21. 跨仓杠杆流水
     */
    public String getCrossMarginBills() throws Exception {
        return doGet("/api/v5/account/cross-margin-bills");
    }

    /**
     * 22. 杠杆借贷公共限制
     */
    public String getLoanPublicLimit(String ccy) throws Exception {
        String path = "/api/v5/loan/public-limit";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    // ========================== 【策略交易/高级委托模块 20个】 ==========================
    /**
     * 1. 策略下单（止盈止损/条件单）
     */
    public String placeAlgoOrder(String instId, String tdMode, String side, String ordType,
                                 String sz, String triggerPx, String orderPx) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"tdMode\":\"%s\",\"side\":\"%s\",\"ordType\":\"%s\",\"sz\":\"%s\",\"triggerPx\":\"%s\",\"orderPx\":\"%s\"}",
                instId,tdMode,side,ordType,sz,triggerPx,orderPx);
        return doPost("/api/v5/trade/order-algo",body);
    }

    /**
     * 2. 撤销策略委托单
     */
    public String cancelAlgoOrder(String instId, String algoId) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"algoId\":\"%s\"}",instId,algoId);
        return doPost("/api/v5/trade/cancel-algo",body);
    }

    /**
     * 3. 批量撤销策略单
     */
    public String cancelBatchAlgo(String instId, String algoIds) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"algoIds\":[%s]}",instId,algoIds);
        return doPost("/api/v5/trade/cancel-batch-algo",body);
    }

    /**
     * 4. 获取当前未触发策略单
     */
    public String getAlgoPending(String instId, String ordType) throws Exception {
        String path = "/api/v5/trade/algo-orders-pending";
        if(instId!=null) path += "?instId="+instId;
        if(ordType!=null) path += "&ordType="+ordType;
        return doGet(path);
    }

    /**
     * 5. 获取策略单历史记录
     */
    public String getAlgoHistory(String instId, String state) throws Exception {
        String path = "/api/v5/trade/algo-orders-history";
        if(instId!=null) path += "?instId="+instId;
        if(state!=null) path += "&state="+state;
        return doGet(path);
    }

    /**
     * 6. 追踪委托下单
     */
    public String placeTrackOrder(String instId, String tdMode, String side, String sz, String callbackRatio) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"tdMode\":\"%s\",\"side\":\"%s\",\"sz\":\"%s\",\"callbackRatio\":\"%s\"}",
                instId,tdMode,side,sz,callbackRatio);
        return doPost("/api/v5/trade/order-track",body);
    }

    /**
     * 7. 撤销追踪委托
     */
    public String cancelTrackOrder(String instId, String trackId) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"trackId\":\"%s\"}",instId,trackId);
        return doPost("/api/v5/trade/cancel-track",body);
    }

    /**
     * 8. 获取进行中追踪委托
     */
    public String getTrackPending(String instId) throws Exception {
        String path = "/api/v5/trade/track-orders-pending";
        if(instId!=null) path += "?instId="+instId;
        return doGet(path);
    }

    /**
     * 9. 冰山委托下单
     */
    public String placeIcebergOrder(String instId, String tdMode, String side, String sz, String displaySz) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"tdMode\":\"%s\",\"side\":\"%s\",\"sz\":\"%s\",\"displaySz\":\"%s\"}",
                instId,tdMode,side,sz,displaySz);
        return doPost("/api/v5/trade/order-iceberg",body);
    }

    /**
     * 10. 时间加权TWAP下单
     */
    public String placeTwapOrder(String instId, String tdMode, String side, String sz, String duration) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"tdMode\":\"%s\",\"side\":\"%s\",\"sz\":\"%s\",\"duration\":\"%s\"}",
                instId,tdMode,side,sz,duration);
        return doPost("/api/v5/trade/order-twap",body);
    }

    /**
     * 11. 持仓止盈止损全局设置
     */
    public String setGlobalSlTp(String slTriggerType, String tpTriggerType) throws Exception {
        String body = String.format("{\"slTriggerType\":\"%s\",\"tpTriggerType\":\"%s\"}",slTriggerType,tpTriggerType);
        return doPost("/api/v5/trade/global-sl-tp",body);
    }

    /**
     * 12. 一键止盈止损全部持仓
     */
    public String closeAllSlTp(String instType) throws Exception {
        String body = "{\"instType\":\""+instType+"\"}";
        return doPost("/api/v5/trade/close-all-sl-tp",body);
    }

    /**
     * 13. 高级限价委托
     */
    public String placeAdvancedLimit(String instId, String tdMode, String side, String sz, String px, String timeInForce) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"tdMode\":\"%s\",\"side\":\"%s\",\"sz\":\"%s\",\"px\":\"%s\",\"timeInForce\":\"%s\"}",
                instId,tdMode,side,sz,px,timeInForce);
        return doPost("/api/v5/trade/order-advanced-limit",body);
    }

    /**
     * 14. 溢价保护委托
     */
    public String placePriceProtectOrder(String instId, String tdMode, String side, String sz, String slippage) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"tdMode\":\"%s\",\"side\":\"%s\",\"sz\":\"%s\",\"slippage\":\"%s\"}",
                instId,tdMode,side,sz,slippage);
        return doPost("/api/v5/trade/order-price-protect",body);
    }

    /**
     * 15. 查询策略单详情
     */
    public String getAlgoDetail(String algoId) throws Exception {
        return doGet("/api/v5/trade/algo-detail?algoId="+algoId);
    }

    /**
     * 16. 修改未触发策略单
     */
    public String amendAlgoOrder(String instId, String algoId, String triggerPx, String orderPx) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"algoId\":\"%s\",\"triggerPx\":\"%s\",\"orderPx\":\"%s\"}",
                instId,algoId,triggerPx,orderPx);
        return doPost("/api/v5/trade/amend-algo",body);
    }

    /**
     * 17. 网格交易策略创建
     */
    public String createGridStrategy(String instId, String gridType, String minPx, String maxPx, String gridNum) throws Exception {
        String body = String.format(
                "{\"instId\":\"%s\",\"gridType\":\"%s\",\"minPx\":\"%s\",\"maxPx\":\"%s\",\"gridNum\":\"%s\"}",
                instId,gridType,minPx,maxPx,gridNum);
        return doPost("/api/v5/strategy/grid/create",body);
    }

    /**
     * 18. 网格策略列表查询
     */
    public String getGridList(String instId, String gridType) throws Exception {
        String path = "/api/v5/strategy/grid/list";
        if(instId!=null) path += "?instId="+instId;
        if(gridType!=null) path += "&gridType="+gridType;
        return doGet(path);
    }

    /**
     * 19. 暂停网格策略
     */
    public String pauseGridStrategy(String strategyId) throws Exception {
        String body = String.format("{\"strategyId\":\"%s\"}",strategyId);
        return doPost("/api/v5/strategy/grid/pause",body);
    }

    /**
     * 20. 终止网格策略
     */
    public String stopGridStrategy(String strategyId) throws Exception {
        String body = String.format("{\"strategyId\":\"%s\"}",strategyId);
        return doPost("/api/v5/strategy/grid/stop",body);
    }

    // ========================== 【理财/赚币模块 18个】 ==========================
    /**
     * 1. 理财产品列表
     */
    public String getEarnProducts(String ccy, String type) throws Exception {
        StringBuilder sb = new StringBuilder("/api/v5/finance/earn/products");
        if(ccy != null) sb.append("?ccy=").append(ccy);
        if(type != null) sb.append("&type=").append(type);
        return doGet(sb.toString());
    }

    /**
     * 2. 持有理财资产
     */
    public String getEarnHoldings(String ccy) throws Exception {
        String path = "/api/v5/finance/earn/holdings";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 3. 理财申购
     */
    public String earnSubscribe(String productId, String ccy, String amt) throws Exception {
        String body = String.format("{\"productId\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\"}",productId,ccy,amt);
        return doPost("/api/v5/finance/earn/subscribe",body);
    }

    /**
     * 4. 理财赎回
     */
    public String earnRedeem(String productId, String ccy, String amt) throws Exception {
        String body = String.format("{\"productId\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\"}",productId,ccy,amt);
        return doPost("/api/v5/finance/earn/redeem",body);
    }

    /**
     * 5. 理财订单记录
     */
    public String getEarnOrders(String ccy) throws Exception {
        String path = "/api/v5/finance/earn/orders";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 6. 活期理财自动划转设置
     */
    public String setEarnAutoTransfer(String ccy, String auto) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"auto\":\"%s\"}",ccy,auto);
        return doPost("/api/v5/finance/earn/auto-transfer",body);
    }

    /**
     * 7. 活期收益记录
     */
    public String getEarnInterestHistory(String ccy) throws Exception {
        String path = "/api/v5/finance/earn/interest-history";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 8. 活动奖励记录
     */
    public String getEarnRewardHistory() throws Exception {
        return doGet("/api/v5/finance/earn/reward-history");
    }

    /**
     * 9. 锁仓挖矿列表
     */
    public String getStakeProducts(String ccy) throws Exception {
        String path = "/api/v5/finance/stake/products";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 10. 锁仓质押
     */
    public String stakeSubscribe(String productId, String ccy, String amt) throws Exception {
        String body = String.format("{\"productId\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\"}",productId,ccy,amt);
        return doPost("/api/v5/finance/stake/subscribe",body);
    }

    /**
     * 11. 锁仓解锁
     */
    public String stakeRedeem(String productId, String ccy, String amt) throws Exception {
        String body = String.format("{\"productId\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\"}",productId,ccy,amt);
        return doPost("/api/v5/finance/stake/redeem",body);
    }

    /**
     * 12. 锁仓持有明细
     */
    public String getStakeHoldings(String ccy) throws Exception {
        String path = "/api/v5/finance/stake/holdings";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 13. 质押订单流水
     */
    public String getStakeOrders(String ccy) throws Exception {
        String path = "/api/v5/finance/stake/orders";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 14. 双币理财列表
     */
    public String getDualProducts(String ccy) throws Exception {
        String path = "/api/v5/finance/dual/products";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

    /**
     * 15. 双币理财申购
     */
    public String dualSubscribe(String productId, String ccy, String amt, String direction) throws Exception {
        String body = String.format("{\"productId\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\",\"direction\":\"%s\"}",productId,ccy,amt,direction);
        return doPost("/api/v5/finance/dual/subscribe",body);
    }

    /**
     * 16. 双币持仓查询
     */
    public String getDualHoldings() throws Exception {
        return doGet("/api/v5/finance/dual/holdings");
    }

    /**
     * 17. 定投计划列表
     */
    public String getDcPlanList(String instId) throws Exception {
        String path = "/api/v5/finance/dc/plan-list";
        if(instId != null) path += "?instId=" + instId;
        return doGet(path);
    }

    /**
     * 18. 创建定投计划
     */
    public String createDcPlan(String instId, String ccy, String amt, String cycle) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\",\"cycle\":\"%s\"}",instId,ccy,amt,cycle);
        return doPost("/api/v5/finance/dc/create-plan",body);
    }

// ========================== 【C2C 场外交易模块 12个】 ==========================
    /**
     * 1. C2C 交易币种列表
     */
    public String getC2cCurrencies() throws Exception {
        return doGet("/api/v5/c2c/currencies");
    }

    /**
     * 2. C2C 广告单列表
     */
    public String getC2cAds(String ccy, String side, String payment) throws Exception {
        StringBuilder sb = new StringBuilder("/api/v5/c2c/ads");
        if(ccy!=null) sb.append("?ccy=").append(ccy);
        if(side!=null) sb.append("&side=").append(side);
        if(payment!=null) sb.append("&payment=").append(payment);
        return doGet(sb.toString());
    }

    /**
     * 3. 创建C2C订单
     */
    public String createC2cOrder(String adId, String ccy, String amt) throws Exception {
        String body = String.format("{\"adId\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\"}",adId,ccy,amt);
        return doPost("/api/v5/c2c/create-order",body);
    }

    /**
     * 4. C2C 订单列表
     */
    public String getC2cOrders(String ccy, String status) throws Exception {
        StringBuilder sb = new StringBuilder("/api/v5/c2c/orders");
        if(ccy!=null) sb.append("?ccy=").append(ccy);
        if(status!=null) sb.append("&status=").append(status);
        return doGet(sb.toString());
    }

    /**
     * 5. 确认收款/付款
     */
    public String confirmC2cPay(String orderId, String op) throws Exception {
        String body = String.format("{\"orderId\":\"%s\",\"op\":\"%s\"}",orderId,op);
        return doPost("/api/v5/c2c/confirm-pay",body);
    }

    /**
     * 6. 申诉C2C订单
     */
    public String appealC2cOrder(String orderId, String reason) throws Exception {
        String body = String.format("{\"orderId\":\"%s\",\"reason\":\"%s\"}",orderId,reason);
        return doPost("/api/v5/c2c/appeal",body);
    }

    /**
     * 7. 关闭C2C订单
     */
    public String closeC2cOrder(String orderId) throws Exception {
        String body = String.format("{\"orderId\":\"%s\"}",orderId);
        return doPost("/api/v5/c2c/close-order",body);
    }

    /**
     * 8. 我的发布广告
     */
    public String getC2cMyAds() throws Exception {
        return doGet("/api/v5/c2c/my-ads");
    }

    /**
     * 9. 发布C2C广告
     */
    public String createC2cAd(String ccy, String side, String price, String minAmt, String maxAmt) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"side\":\"%s\",\"price\":\"%s\",\"minAmt\":\"%s\",\"maxAmt\":\"%s\"}",ccy,side,price,minAmt,maxAmt);
        return doPost("/api/v5/c2c/create-ad",body);
    }

    /**
     * 10. 下架广告
     */
    public String offlineC2cAd(String adId) throws Exception {
        String body = String.format("{\"adId\":\"%s\"}",adId);
        return doPost("/api/v5/c2c/offline-ad",body);
    }

    /**
     * 11. C2C 支付方式列表
     */
    public String getC2cPaymentList() throws Exception {
        return doGet("/api/v5/c2c/payment-list");
    }

    /**
     * 12. C2C 手续费查询
     */
    public String getC2cFeeRate(String ccy) throws Exception {
        String path = "/api/v5/c2c/fee-rate";
        if(ccy != null) path += "?ccy=" + ccy;
        return doGet(path);
    }

// ========================== 【期权&交割合约专属 16个】 ==========================
    /**
     * 1. 期权持仓
     */
    public String getOptionPositions(String instId) throws Exception {
        String path = "/api/v5/account/option-positions";
        if(instId != null) path += "?instId=" + instId;
        return doGet(path);
    }

    /**
     * 2. 期权希腊字母风险
     */
    public String getOptionGreeks() throws Exception {
        return doGet("/api/v5/account/option-greeks");
    }

    /**
     * 3. 交割合约结算历史
     */
    public String getFuturesSettlement(String instId) throws Exception {
        String path = "/api/v5/account/futures-settlement";
        if(instId != null) path += "?instId=" + instId;
        return doGet(path);
    }

    /**
     * 4. 行权/履约记录
     */
    public String getExerciseHistory(String instId) throws Exception {
        String path = "/api/v5/trade/exercise-history";
        if(instId != null) path += "?instId=" + instId;
        return doGet(path);
    }

    /**
     * 5. 手动行权
     */
    public String exerciseOption(String instId, String holdAmt) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"holdAmt\":\"%s\"}",instId,holdAmt);
        return doPost("/api/v5/trade/exercise-option",body);
    }

    /**
     * 6. 自动行权设置
     */
    public String setAutoExercise(String enable) throws Exception {
        String body = String.format("{\"enable\":\"%s\"}",enable);
        return doPost("/api/v5/trade/auto-exercise",body);
    }

    /**
     * 7. 期权溢价指数
     */
    public String getOptionVolIndex(String instId) throws Exception {
        String path = "/api/v5/market/option-vol-index";
        if(instId != null) path += "?instId=" + instId;
        return doGet(path);
    }

    /**
     * 8. 期权链行情
     */
    public String getOptionChain(String underlying) throws Exception {
        String path = "/api/v5/market/option-chain?underlying=" + underlying;
        return doGet(path);
    }

    /**
     * 9. 交割合约历史K线
     */
    public String getFuturesCandle(String instId, String bar) throws Exception {
        String path = "/api/v5/market/futures-candlesticks?instId="+instId;
        if(bar!=null) path += "&bar="+bar;
        return doGet(path);
    }

    /**
     * 10. 结算价查询
     */
    public String getSettlePrice(String instId) throws Exception {
        return doGet("/api/v5/market/settle-price?instId="+instId);
    }

    /**
     * 11. 合约历史费率
     */
    public String getFuturesRateHistory(String instId) throws Exception {
        String path = "/api/v5/market/futures-rate-history?instId="+instId;
        return doGet(path);
    }

    /**
     * 12. 风险对冲配置
     */
    public String setHedgeMode(String hedgeMode) throws Exception {
        String body = String.format("{\"hedgeMode\":\"%s\"}",hedgeMode);
        return doPost("/api/v5/account/set-hedge-mode",body);
    }

    /**
     * 13. 批量行权查询
     */
    public String getBatchExerciseList() throws Exception {
        return doGet("/api/v5/trade/batch-exercise-list");
    }

    /**
     * 14. 期权损益汇总
     */
    public String getOptionProfitSummary(String ccy) throws Exception {
        String path = "/api/v5/account/option-profit-summary";
        if(ccy!=null) path += "?ccy="+ccy;
        return doGet(path);
    }

    /**
     * 15. 交割计划查询
     */
    public String getDeliveryPlan(String instType) throws Exception {
        return doGet("/api/v5/public/delivery-plan?instType="+instType);
    }

    /**
     * 16. 合约阶梯费率
     */
    public String getFuturesTierFee(String instType) throws Exception {
        return doGet("/api/v5/public/futures-tier-fee?instType="+instType);
    }

    // ========================== 【风控&权限&用户模块 14个】 ==========================
    /**
     * 1. 获取用户基础信息
     */
    public String getUserInfo() throws Exception {
        return doGet("/api/v5/users/info");
    }

    /**
     * 2. 接口访问权限查询
     */
    public String getApiPermission() throws Exception {
        return doGet("/api/v5/users/api-permission");
    }

    /**
     * 3. 绑定IP白名单
     */
    public String setIpWhitelist(String ip) throws Exception {
        String body = String.format("{\"ip\":\"%s\"}",ip);
        return doPost("/api/v5/users/ip-whitelist",body);
    }

    /**
     * 4. 查询IP白名单
     */
    public String getIpWhitelist() throws Exception {
        return doGet("/api/v5/users/ip-whitelist");
    }

    /**
     * 5. 交易风险等级
     */
    public String getRiskLevel() throws Exception {
        return doGet("/api/v5/users/risk-level");
    }

    /**
     * 6. 提现安全设置查询
     */
    public String getWithdrawSecurity() throws Exception {
        return doGet("/api/v5/users/withdraw-security");
    }

    /**
     * 7. 行为风控记录
     */
    public String getRiskOperationLog() throws Exception {
        return doGet("/api/v5/users/risk-operation-log");
    }

    /**
     * 8. 账号登录日志
     */
    public String getLoginLog() throws Exception {
        return doGet("/api/v5/users/login-log");
    }

    /**
     * 9. 身份认证信息
     */
    public String getKycInfo() throws Exception {
        return doGet("/api/v5/users/kyc-info");
    }

    /**
     * 10. 地区限制查询
     */
    public String getRegionLimit() throws Exception {
        return doGet("/api/v5/users/region-limit");
    }

    /**
     * 11. 手续费档位查询
     */
    public String getFeeTier() throws Exception {
        return doGet("/api/v5/users/fee-tier");
    }

    /**
     * 12. 交易活跃度数据
     */
    public String getUserActivity() throws Exception {
        return doGet("/api/v5/users/activity");
    }

    /**
     * 13. 消息订阅配置
     */
    public String setMsgSubscribe(String type,String status) throws Exception {
        String body = String.format("{\"type\":\"%s\",\"status\":\"%s\"}",type,status);
        return doPost("/api/v5/users/msg-subscribe",body);
    }

    /**
     * 14. 账号资产摘要
     */
    public String getAssetSummary() throws Exception {
        return doGet("/api/v5/users/asset-summary");
    }

// ========================== 【归档&历史大数据模块 13个】 ==========================
    /**
     * 1. 历史账单归档查询
     */
    public String getArchiveBills(String ccy,String instType) throws Exception {
        String path = "/api/v5/account/archive-bills";
        if(ccy!=null) path += "?ccy="+ccy;
        if(instType!=null) path += "&instType="+instType;
        return doGet(path);
    }

    /**
     * 2. 历史成交归档
     */
    public String getArchiveFills(String instId) throws Exception {
        String path = "/api/v5/trade/archive-fills";
        if(instId!=null) path += "?instId="+instId;
        return doGet(path);
    }

    /**
     * 3. 历史订单归档
     */
    public String getArchiveOrders(String instId) throws Exception {
        String path = "/api/v5/trade/archive-orders";
        if(instId!=null) path += "?instId="+instId;
        return doGet(path);
    }

    /**
     * 4. 合约历史持仓归档
     */
    public String getArchivePosition(String instType) throws Exception {
        String path = "/api/v5/account/archive-position";
        if(instType!=null) path += "?instType="+instType;
        return doGet(path);
    }

    /**
     * 5. 历史资金划转归档
     */
    public String getArchiveTransfer() throws Exception {
        return doGet("/api/v5/asset/archive-transfer");
    }

    /**
     * 6. 批量导出记录
     */
    public String getExportRecord() throws Exception {
        return doGet("/api/v5/tool/export-record");
    }

    /**
     * 7. 大盘历史成交量
     */
    public String getMarketVolumeHistory(String instType) throws Exception {
        String path = "/api/v5/market/volume-history?instType="+instType;
        return doGet(path);
    }

    /**
     * 8. 历史深度归档
     */
    public String getArchiveDepth(String instId) throws Exception {
        String path = "/api/v5/market/archive-depth?instId="+instId;
        return doGet(path);
    }

    /**
     * 9. 超长周期K线归档
     */
    public String getArchiveCandle(String instId,String bar) throws Exception {
        String path = "/api/v5/market/archive-candle?instId="+instId;
        if(bar!=null) path += "&bar="+bar;
        return doGet(path);
    }

    /**
     * 10. 历史资金费率归档
     */
    public String getArchiveFundingRate(String instId) throws Exception {
        return doGet("/api/v5/market/archive-funding-rate?instId="+instId);
    }

    /**
     * 11. 历史清算记录
     */
    public String getArchiveLiquidation(String instType) throws Exception {
        String path = "/api/v5/market/archive-liquidation?instType="+instType;
        return doGet(path);
    }

    /**
     * 12. 币种历史行情统计
     */
    public String getCoinStatHistory(String ccy) throws Exception {
        String path = "/api/v5/market/coin-stat-history?ccy="+ccy;
        return doGet(path);
    }

    /**
     * 13. 链上历史记录查询
     */
    public String getOnchainArchive(String ccy) throws Exception {
        String path = "/api/v5/asset/onchain-archive?ccy="+ccy;
        return doGet(path);
    }

// ========================== 【经纪商Broker&机构模块 12个】 ==========================
    /**
     * 1. 经纪商下级用户列表
     */
    public String getBrokerSubUser() throws Exception {
        return doGet("/api/v5/broker/sub-user");
    }

    /**
     * 2. 下级用户资产汇总
     */
    public String getBrokerSubAsset() throws Exception {
        return doGet("/api/v5/broker/sub-asset");
    }

    /**
     * 3. 下级交易分成记录
     */
    public String getBrokerCommission() throws Exception {
        return doGet("/api/v5/broker/commission");
    }

    /**
     * 4. 经纪商手续费配置
     */
    public String getBrokerFeeConfig() throws Exception {
        return doGet("/api/v5/broker/fee-config");
    }

    /**
     * 5. 调整下级手续费比例
     */
    public String setBrokerFeeRate(String subAcct,String rate) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\",\"rate\":\"%s\"}",subAcct,rate);
        return doPost("/api/v5/broker/set-fee-rate",body);
    }

    /**
     * 6. 经纪商数据大盘
     */
    public String getBrokerOverview() throws Exception {
        return doGet("/api/v5/broker/overview");
    }

    /**
     * 7. 机构VIP费率查询
     */
    public String getInstitutionFee() throws Exception {
        return doGet("/api/v5/institution/fee");
    }

    /**
     * 8. 机构授信额度
     */
    public String getCreditQuota() throws Exception {
        return doGet("/api/v5/institution/credit-quota");
    }

    /**
     * 9. 联名活动数据
     */
    public String getPartnerActivityData() throws Exception {
        return doGet("/api/v5/partner/activity-data");
    }

    /**
     * 10. 渠道注册统计
     */
    public String getChannelRegisterStat() throws Exception {
        return doGet("/api/v5/partner/channel-stat");
    }

    /**
     * 11. 经纪商白名单配置
     */
    public String setBrokerWhiteList(String subAcct) throws Exception {
        String body = String.format("{\"subAcct\":\"%s\"}",subAcct);
        return doPost("/api/v5/broker/white-list",body);
    }

    /**
     * 12. 下级账号批量操作
     */
    public String brokerBatchOperate(String subAcctList,String op) throws Exception {
        String body = String.format("{\"subAcctList\":[%s],\"op\":\"%s\"}",subAcctList,op);
        return doPost("/api/v5/broker/batch-operate",body);
    }

// ========================== 【其余小众工具&业务补齐 10个】 ==========================
    /**
     * 1. 一键划转全部余额
     */
    public String transferAllBalance(String from,String to) throws Exception {
        String body = String.format("{\"from\":\"%s\",\"to\":\"%s\"}",from,to);
        return doPost("/api/v5/asset/transfer-all",body);
    }

    /**
     * 2. 币种风险提示
     */
    public String getCoinRiskTip(String ccy) throws Exception {
        String path = "/api/v5/public/coin-risk-tip";
        if(ccy!=null) path += "?ccy="+ccy;
        return doGet(path);
    }

    /**
     * 3. 节假日交易安排
     */
    public String getTradeHoliday() throws Exception {
        return doGet("/api/v5/public/trade-holiday");
    }

    /**
     * 4. 系统公告列表
     */
    public String getNoticeList() throws Exception {
        return doGet("/api/v5/public/notice");
    }

    /**
     * 5. 行情涨跌榜
     */
    public String getMarketRank(String instType) throws Exception {
        String path = "/api/v5/market/rank?instType="+instType;
        return doGet(path);
    }

    /**
     * 6. 大宗交易下单
     */
    public String placeBlockOrder(String instId,String side,String amt,String px) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"side\":\"%s\",\"amt\":\"%s\",\"px\":\"%s\"}",instId,side,amt,px);
        return doPost("/api/v5/trade/block-order",body);
    }

    /**
     * 7. 大宗交易记录
     */
    public String getBlockFills(String instId) throws Exception {
        String path = "/api/v5/trade/block-fills";
        if(instId!=null) path += "?instId="+instId;
        return doGet(path);
    }

    /**
     * 8. 跨链划转
     */
    public String crossChainTransfer(String ccy,String amt,String fromChain,String toChain) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\",\"fromChain\":\"%s\",\"toChain\":\"%s\"}",ccy,amt,fromChain,toChain);
        return doPost("/api/v5/asset/cross-chain-transfer",body);
    }

    /**
     * 9. 燃烧销毁记录
     */
    public String getBurnHistory(String ccy) throws Exception {
        String path = "/api/v5/asset/burn-history";
        if(ccy!=null) path += "?ccy="+ccy;
        return doGet(path);
    }

    /**
     * 10. 平台限制开关
     */
    public String getPlatformSwitch() throws Exception {
        return doGet("/api/v5/public/platform-switch");
    }


    // ========================== 【账户剩余/海外机构专属 补充12个】 ==========================
    /**
     * 1. 统一账户资产估值（海外）
     */
    public String getAssetValuation(String ccy) throws Exception {
        String path = "/api/v5/account/asset-valuation";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 2. 账户固定估值信息
     */
    public String getFixedValuation() throws Exception {
        return doGet("/api/v5/account/fixed-valuation");
    }

    /**
     * 3. 风险状态详情
     */
    public String getRiskState() throws Exception {
        return doGet("/api/v5/account/risk-state");
    }

    /**
     * 4. 设置自动借币（海外机构）
     */
    public String setAutoLoan(String autoLoan) throws Exception {
        String body = String.format("{\"autoLoan\":\"%s\"}",autoLoan);
        return doPost("/api/v5/account/set-auto-loan",body);
    }



    /**
     * 6. 子账户汇总持仓（海外专属）
     */
    public String getSubAccountPosition(String instType) throws Exception {
        String path = "/api/v5/account/subaccount-positions";
        if(instType != null){
            path += "?instType=" + instType;
        }
        return doGet(path);
    }

    /**
     * 7. 账户内部划转（海外）
     */
    public String innerTransfer(String from, String to, String ccy, String amt) throws Exception {
        String body = String.format("{\"from\":\"%s\",\"to\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\"}",from,to,ccy,amt);
        return doPost("/api/v5/account/transfer",body);
    }

    /**
     * 8. 划转订单状态查询
     */
    public String getTransferState(String transferId) throws Exception {
        return doGet("/api/v5/account/transfer-state?transferId="+transferId);
    }

    /**
     * 9. 计息明细记录
     */
    public String getInterestAccrued(String ccy) throws Exception {
        String path = "/api/v5/account/interest-accrued";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }


    /**
     * 11. 手动借币下单
     */
    public String accountBorrow(String ccy, String amt) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\"}",ccy,amt);
        return doPost("/api/v5/account/borrow",body);
    }

    /**
     * 12. 手动归还借币
     */
    public String accountRepay(String ccy, String amt) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\"}",ccy,amt);
        return doPost("/api/v5/account/repay",body);
    }

// ========================== 【交易剩余/海外兑换/批量高级 补充11个】 ==========================
    /**
     * 1. 批量改单
     */
    public String amendBatchOrders(String bodyJson) throws Exception {
        return doPost("/api/v5/trade/amend-batch-orders",bodyJson);
    }

    /**
     * 2. 一键平仓持仓
     */
    public String closePositions(String instId, String posSide) throws Exception {
        String body = String.format("{\"instId\":\"%s\",\"posSide\":\"%s\"}",instId,posSide);
        return doPost("/api/v5/trade/close-positions",body);
    }

    /**
     * 3. 批量撤销策略单
     */
    public String cancelAlgoList(String bodyJson) throws Exception {
        return doPost("/api/v5/trade/cancel-algos",bodyJson);
    }

    /**
     * 4. 一键快捷兑换（海外专属）
     */
    public String easyConvert(String fromCcy, String toCcy, String amt) throws Exception {
        String body = String.format("{\"fromCcy\":\"%s\",\"toCcy\":\"%s\",\"amt\":\"%s\"}",fromCcy,toCcy,amt);
        return doPost("/api/v5/trade/easy-convert",body);
    }

    /**
     * 5. 快捷兑换历史（海外）
     */
    public String getEasyConvertHistory(String ccy) throws Exception {
        String path = "/api/v5/trade/easy-convert-history";
        if(ccy != null){
            path += "?ccy=" + ccy;
        }
        return doGet(path);
    }

    /**
     * 6. 撤销快捷兑换订单（海外）
     */
    public String cancelConvert(String convertId) throws Exception {
        String body = String.format("{\"convertId\":\"%s\"}",convertId);
        return doPost("/api/v5/trade/convert-cancel",body);
    }

    /**
     * 7. 获取兑换报价（海外）
     */
    public String getConvertQuote(String fromCcy, String toCcy, String amt) throws Exception {
        return doGet(String.format("/api/v5/trade/convert-quote?fromCcy=%s&toCcy=%s&amt=%s",fromCcy,toCcy,amt));
    }

    /**
     * 8. 订单详情查询
     */
    public String getOrderDetail(String ordId) throws Exception {
        return doGet("/api/v5/trade/order?ordId="+ordId);
    }

    /**
     * 9. 7天内成交明细
     */
    public String getFillsDetail(String instId) throws Exception {
        String path = "/api/v5/trade/fills";
        if(instId != null){
            path += "?instId=" + instId;
        }
        return doGet(path);
    }

    /**
     * 10. 历史成交归档全量
     */
    public String getFillsArchive(String instId) throws Exception {
        String path = "/api/v5/trade/fills-history";
        if(instId != null){
            path += "?instId=" + instId;
        }
        return doGet(path);
    }

    /**
     * 11. 历史持仓查询
     */
    public String getPositionHistory(String instType) throws Exception {
        String path = "/api/v5/account/positions-history";
        if(instType != null){
            path += "?instType=" + instType;
        }
        return doGet(path);
    }

// ========================== 【行情全量剩余/海外高频/期权指数 补充13个】 ==========================
    /**
     * 1. 全量深度盘口（海外高频）
     */
    public String getBooksFull(String instId) throws Exception {
        return doGet("/api/v5/market/books-full?instId="+instId);
    }

    /**
     * 2. 历史K线全量
     */
    public String getHistoryCandles(String instId, String bar) throws Exception {
        String path = "/api/v5/market/history-candles?instId="+instId;
        if(bar != null){
            path += "&bar="+bar;
        }
        return doGet(path);
    }

    /**
     * 3. 历史成交记录
     */
    public String getHistoryTrades(String instId) throws Exception {
        return doGet("/api/v5/market/history-trades?instId="+instId);
    }

    /**
     * 4. 指数K线行情
     */
    public String getIndexCandles(String index, String bar) throws Exception {
        String path = "/api/v5/market/index-candles?index="+index;
        if(bar != null){
            path += "&bar="+bar;
        }
        return doGet(path);
    }

    /**
     * 5. 指数行情列表
     */
    public String getIndexTickers(String quoteCcy) throws Exception {
        String path = "/api/v5/market/index-tickers";
        if(quoteCcy != null){
            path += "?quoteCcy=" + quoteCcy;
        }
        return doGet(path);
    }

    /**
     * 6. 平台24小时成交额
     */
    public String getPlatform24Volume() throws Exception {
        return doGet("/api/v5/market/platform-24-volume");
    }

    /**
     * 7. 简化盘口深度（海外）
     */
    public String getOrderBookLite(String instId) throws Exception {
        return doGet("/api/v5/market/order-book?instId="+instId);
    }

    /**
     * 8. 期权产品列表（海外）
     */
    public String getOptionInstruments(String underlying) throws Exception {
        return doGet("/api/v5/market/option/instruments?underlying="+underlying);
    }

    /**
     * 9. 期权市场汇总（海外）
     */
    public String getOptionSummary(String underlying) throws Exception {
        return doGet("/api/v5/market/option/summary?underlying="+underlying);
    }




    /**
     * 12. 限价范围
     */
    public String getPriceLimit(String instId) throws Exception {
        return doGet("/api/v5/market/price-limit?instId="+instId);
    }

    /**
     * 13. 合约公共利率
     */
    public String getInterestRate() throws Exception {
        return doGet("/api/v5/market/interest-rate");
    }

// ========================== 【公共/资产/链上/挖矿 海外全量剩余 26个】 ==========================


    /**
     * 2. 充值地址列表
     */
    public String getDepositAddressList(String ccy) throws Exception {
        return doGet("/api/v5/asset/deposit-address-list?ccy="+ccy);
    }

    /**
     * 3. 充值记录全量
     */
    public String getDepositHistory(String ccy, String state) throws Exception {
        String path = "/api/v5/asset/deposit-history";
        if(ccy != null) path += "?ccy="+ccy;
        if(state != null) path += "&state="+state;
        return doGet(path);
    }



    /**
     * 5. 提币记录
     */
    public String getWithdrawHistory(String ccy, String state) throws Exception {
        String path = "/api/v5/asset/withdraw-history";
        if(ccy != null) path += "?ccy="+ccy;
        if(state != null) path += "&state="+state;
        return doGet(path);
    }

    /**
     * 6. 取消提币
     */
    public String cancelWithdraw(String wdId) throws Exception {
        String body = String.format("{\"wdId\":\"%s\"}",wdId);
        return doPost("/api/v5/asset/cancel-withdraw",body);
    }

    /**
     * 7. 内部转账免手续费（海外）
     */
    public String innerTransferFree(String uid, String ccy, String amt) throws Exception {
        String body = String.format("{\"uid\":\"%s\",\"ccy\":\"%s\",\"amt\":\"%s\"}",uid,ccy,amt);
        return doPost("/api/v5/asset/inner-transfer",body);
    }

    /**
     * 8. 币种基础信息
     */
    public String getCcyInfo(String ccy) throws Exception {
        String path = "/api/v5/asset/ccy-info";
        if(ccy != null) path += "?ccy="+ccy;
        return doGet(path);
    }

    /**
     * 9. 多链配置信息
     */
    public String getChainConfig(String ccy) throws Exception {
        String path = "/api/v5/asset/chain-config";
        if(ccy != null) path += "?ccy="+ccy;
        return doGet(path);
    }

    /**
     * 10. 小额资产兑换
     */
    public String dustConvert() throws Exception {
        return doPost("/api/v5/asset/dust-convert","{}");
    }

    /**
     * 11. 钱包账单流水
     */
    public String getAssetBills(String ccy, String type) throws Exception {
        String path = "/api/v5/asset/bills";
        if(ccy != null) path += "?ccy="+ccy;
        if(type != null) path += "&type="+type;
        return doGet(path);
    }

    /**
     * 12. 红包发放（海外地区）
     */
    public String sendRedPacket(String ccy, String amt) throws Exception {
        String body = String.format("{\"ccy\":\"%s\",\"amt\":\"%s\"}",ccy,amt);
        return doPost("/api/v5/asset/red-packet/send",body);
    }

    /**
     * 13. 红包记录查询
     */
    public String getRedPacketHistory() throws Exception {
        return doGet("/api/v5/asset/red-packet/history");
    }

    /**
     * 14. 产品基础列表
     */
    public String getInstruments(String instType) throws Exception {
        return doGet("/api/v5/public/instruments?instType="+instType);
    }

    /**
     * 15. 交易费率档位
     */
    public String getFeeRateInfo(String instType) throws Exception {
        return doGet("/api/v5/public/fee-rate?instType="+instType);
    }

    /**
     * 16. 杠杆借贷利率公共
     */
    public String getPublicInterestRate() throws Exception {
        return doGet("/api/v5/public/interest-rate");
    }

    /**
     * 17. 爆仓订单公共
     */
    public String getLiquidationOrders(String instType) throws Exception {
        return doGet("/api/v5/public/liquidation-orders?instType="+instType);
    }

    /**
     * 18. 理财产品公共
     */
    public String getPublicEarnProduct() throws Exception {
        return doGet("/api/v5/public/earn-product");
    }

    /**
     * 19. 质押挖矿公共
     */
    public String getPublicStakeProduct() throws Exception {
        return doGet("/api/v5/public/stake-product");
    }

    /**
     * 20. 全网算力挖矿（海外）
     */
    public String getMiningPower() throws Exception {
        return doGet("/api/v5/mining/power");
    }

    /**
     * 21. 挖矿收益记录
     */
    public String getMiningEarnHistory() throws Exception {
        return doGet("/api/v5/mining/earn-history");
    }

    /**
     * 22. 矿机资产明细
     */
    public String getMiningAsset() throws Exception {
        return doGet("/api/v5/mining/asset");
    }

    /**
     * 23. 云算力订单
     */
    public String getCloudMiningOrder() throws Exception {
        return doGet("/api/v5/mining/cloud-order");
    }

    /**
     * 24. 邀请返利数据（海外）
     */
    public String getInviteData() throws Exception {
        return doGet("/api/v5/affiliate/invite-data");
    }

    /**
     * 25. 返利流水记录
     */
    public String getAffiliateCommission() throws Exception {
        return doGet("/api/v5/affiliate/commission");
    }

    /**
     * 26. 邀请榜单排名
     */
    public String getInviteRank() throws Exception {
        return doGet("/api/v5/affiliate/rank");
    }

    // ==================== 底层 POST 请求 ====================
    private String doPost(String path, String body) throws Exception {
        String timestamp = getTimestamp();
        String sign = generateSign(timestamp, "POST", path, body);

        HttpRequest request = buildBaseHeaders(timestamp, sign)
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return resp.body();
    }

    /**
     * 统一请求头（全局模拟配置自动生效）
     */
    private HttpRequest.Builder buildBaseHeaders(String timestamp, String sign) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .header("OK-ACCESS-KEY", apiKey)
                .header("OK-ACCESS-SIGN", sign)
                .header("OK-ACCESS-TIMESTAMP", timestamp)
                .header("OK-ACCESS-PASSPHRASE", passphrase);

        // 全局模拟开关
        if (simulated) {
            builder.header("x-simulated-trading", "1");
        }
        return builder;
    }

    /**
     * 生成 OKX 标准时间戳
     */
    private String getTimestamp() {
        return Instant.now().toString().replaceAll("\\.\\d+", "");
    }

    /**
     * HmacSHA256 签名（OKX 官方标准）
     * 无状态、线程安全
     */
    private String generateSign(String timestamp, String method, String path, String body) throws Exception {
        String preHash = timestamp + method.toUpperCase() + path + body;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey.getBytes(), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(preHash.getBytes()));
    }
}