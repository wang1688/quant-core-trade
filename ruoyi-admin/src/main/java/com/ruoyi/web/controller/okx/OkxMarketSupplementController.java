package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 行情补充+海外高频+期权指数行情
 */
@RestController
@RequestMapping("/okx/marketSupplement")
public class OkxMarketSupplementController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // 1.全量深度盘口（海外高频）
    @GetMapping("/booksFull")
    public AjaxResult booksFull(@RequestParam String instId) {
        try { return success(okxApiClient.getBooksFull(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 2.历史K线全量
    @GetMapping("/historyCandles")
    public AjaxResult historyCandles(@RequestParam String instId,
                                     @RequestParam(required = false) String bar) {
        try { return success(okxApiClient.getHistoryCandles(instId, bar)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 3.历史成交记录
    @GetMapping("/historyTrades")
    public AjaxResult historyTrades(@RequestParam String instId) {
        try { return success(okxApiClient.getHistoryTrades(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 4.指数K线行情
    @GetMapping("/indexCandles")
    public AjaxResult indexCandles(@RequestParam String index,
                                    @RequestParam(required = false) String bar) {
        try { return success(okxApiClient.getIndexCandles(index, bar)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 5.指数行情列表
    @GetMapping("/indexTickers")
    public AjaxResult indexTickers(@RequestParam(required = false) String quoteCcy) {
        try { return success(okxApiClient.getIndexTickers(quoteCcy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 6.平台24小时成交额
    @GetMapping("/platform24Volume")
    public AjaxResult platform24Volume() {
        try { return success(okxApiClient.getPlatform24Volume()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 7.简化盘口深度（海外）
    @GetMapping("/orderBookLite")
    public AjaxResult orderBookLite(@RequestParam String instId) {
        try { return success(okxApiClient.getOrderBookLite(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 8.期权产品列表（海外）
    @GetMapping("/optionInstruments")
    public AjaxResult optionInstruments(@RequestParam String underlying) {
        try { return success(okxApiClient.getOptionInstruments(underlying)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 9.期权市场汇总（海外）
    @GetMapping("/optionSummary")
    public AjaxResult optionSummary(@RequestParam String underlying) {
        try { return success(okxApiClient.getOptionSummary(underlying)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 10.永续合约资金费率
    @GetMapping("/fundingRate")
    public AjaxResult fundingRate(@RequestParam String instId) {
        try { return success(okxApiClient.getFundingRate(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 11.标记价格
    @GetMapping("/markPrice")
    public AjaxResult markPrice(@RequestParam String instId) {
        try { return success(okxApiClient.getMarkPrice(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 12.限价范围
    @GetMapping("/priceLimit")
    public AjaxResult priceLimit(@RequestParam String instId) {
        try { return success(okxApiClient.getPriceLimit(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 13.合约公共利率
    @GetMapping("/interestRate")
    public AjaxResult interestRate() {
        try { return success(okxApiClient.getInterestRate()); }
        catch (Exception e) { return error(e.getMessage()); }
    }
}