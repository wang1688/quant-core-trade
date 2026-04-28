package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OKX 行情 Market 模块控制器
 * 统一全局模拟/实盘配置
 * 全部GET接口，URL拼接参数传参
 */
@RestController
@RequestMapping("/okx/market")
public class OkxMarketController extends BaseController {

    /**
     * 注入全局单例OKX客户端
     * 全局唯一、线程安全、复用连接
     */
    @Autowired
    private OkxApiClient okxApiClient;

    // ====================================== 1.单个交易对Ticker行情 ======================================
    /**
     * 获取单个交易对最新行情
     * @param instId 【必填】交易对ID 例：BTC-USDT
     */
    @GetMapping("/ticker")
    public AjaxResult ticker(@RequestParam String instId) {
        try {
            return success(okxApiClient.getTicker(instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 2.全部现货Ticker行情 ======================================
    /**
     * 获取全站所有现货交易对行情列表
     * 无请求参数
     */
    @GetMapping("/tickerAll")
    public AjaxResult tickerAll() {
        try {
            return success(okxApiClient.getTickerAll());
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 3.K线数据 ======================================
    /**
     * 获取K线蜡烛图数据
     * @param instId 【必填】交易对
     * @param bar    【可选】K线周期 1m/5m/15m/30m/1H/2H/4H/1D/1W
     * @param limit  【可选】返回数据条数，默认100
     */
    @GetMapping("/candlesticks")
    public AjaxResult candlesticks(
            @RequestParam String instId,
            @RequestParam(required = false) String bar,
            @RequestParam(required = false) String limit) {
        try {
            return success(okxApiClient.getCandlesticks(instId, bar, limit));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 4.盘口深度 ======================================
    /**
     * 获取买卖盘口深度数据
     * @param instId 【必填】交易对
     * @param size   【可选】返回深度档位数量
     */
    @GetMapping("/orderBook")
    public AjaxResult orderBook(
            @RequestParam String instId,
            @RequestParam(required = false) String size) {
        try {
            return success(okxApiClient.getOrderBook(instId, size));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 5.最新成交明细 ======================================
    /**
     * 获取实时最新逐笔成交记录
     * @param instId 【必填】交易对
     * @param limit  【可选】返回条数
     */
    @GetMapping("/tradesDetail")
    public AjaxResult tradesDetail(
            @RequestParam String instId,
            @RequestParam(required = false) String limit) {
        try {
            return success(okxApiClient.getTradesDetail(instId, limit));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 6.当前资金费率 ======================================
    /**
     * 获取永续合约实时资金费率
     * @param instId 【必填】合约交易对 例：BTC-USDT-SWAP
     */
    @GetMapping("/fundingRate")
    public AjaxResult fundingRate(@RequestParam String instId) {
        try {
            return success(okxApiClient.getFundingRate(instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 7.资金费率历史 ======================================
    /**
     * 获取合约历史资金费率记录
     * @param instId 【必填】合约交易对
     * @param limit  【可选】返回条数
     */
    @GetMapping("/fundingRateHistory")
    public AjaxResult fundingRateHistory(
            @RequestParam String instId,
            @RequestParam(required = false) String limit) {
        try {
            return success(okxApiClient.getFundingRateHistory(instId, limit));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 8.指数行情 ======================================
    /**
     * 获取指数价格行情
     * @param instId 【必填】指数ID
     */
    @GetMapping("/indexTicker")
    public AjaxResult indexTicker(@RequestParam String instId) {
        try {
            return success(okxApiClient.getIndexTicker(instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 9.标记价格 ======================================
    /**
     * 获取合约标记价格（用于强平计算）
     * @param instId 【必填】交易对
     */
    @GetMapping("/markPrice")
    public AjaxResult markPrice(@RequestParam String instId) {
        try {
            return success(okxApiClient.getMarkPrice(instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 10.交易对规格配置 ======================================
    /**
     * 获取产品/交易对基础规则
     * @param instType 【必填】产品类型 SPOT现货/SWAP永续/FUTURES交割/OPTION期权
     * @param instId   【可选】指定单个交易对，不传返回全量
     */
    @GetMapping("/instrument")
    public AjaxResult instrument(
            @RequestParam String instType,
            @RequestParam(required = false) String instId) {
        try {
            return success(okxApiClient.getInstrument(instType, instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}