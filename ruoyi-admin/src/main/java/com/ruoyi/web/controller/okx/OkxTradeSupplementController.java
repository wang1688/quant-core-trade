package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 交易补充+海外快捷兑换+批量操作
 */
@RestController
@RequestMapping("/okx/tradeSupplement")
public class OkxTradeSupplementController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // 1.批量改单
    @PostMapping("/amendBatchOrders")
    public AjaxResult amendBatchOrders(@RequestBody String bodyJson) {
        try { return success(okxApiClient.amendBatchOrders(bodyJson)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 2.一键平仓持仓
    @PostMapping("/closePositions")
    public AjaxResult closePositions(@RequestParam String instId,
                                     @RequestParam String posSide) {
        try { return success(okxApiClient.closePositions(instId, posSide)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 3.批量撤销策略单
    @PostMapping("/cancelAlgoList")
    public AjaxResult cancelAlgoList(@RequestBody String bodyJson) {
        try { return success(okxApiClient.cancelAlgoList(bodyJson)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 4.一键快捷兑换（海外专属）
    @PostMapping("/easyConvert")
    public AjaxResult easyConvert(@RequestParam String fromCcy,
                                  @RequestParam String toCcy,
                                  @RequestParam String amt) {
        try { return success(okxApiClient.easyConvert(fromCcy, toCcy, amt)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 5.快捷兑换历史（海外）
    @GetMapping("/easyConvertHistory")
    public AjaxResult easyConvertHistory(@RequestParam(required = false) String ccy) {
        try { return success(okxApiClient.getEasyConvertHistory(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 6.撤销快捷兑换订单（海外）
    @PostMapping("/cancelConvert")
    public AjaxResult cancelConvert(@RequestParam String convertId) {
        try { return success(okxApiClient.cancelConvert(convertId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 7.获取兑换报价（海外）
    @GetMapping("/convertQuote")
    public AjaxResult convertQuote(@RequestParam String fromCcy,
                                   @RequestParam String toCcy,
                                   @RequestParam String amt) {
        try { return success(okxApiClient.getConvertQuote(fromCcy, toCcy, amt)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 8.订单详情查询
    @GetMapping("/orderDetail")
    public AjaxResult orderDetail(@RequestParam String ordId) {
        try { return success(okxApiClient.getOrderDetail(ordId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 9.7天内成交明细
    @GetMapping("/fillsDetail")
    public AjaxResult fillsDetail(@RequestParam(required = false) String instId) {
        try { return success(okxApiClient.getFillsDetail(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 10.历史成交归档全量
    @GetMapping("/fillsArchive")
    public AjaxResult fillsArchive(@RequestParam(required = false) String instId) {
        try { return success(okxApiClient.getFillsArchive(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 11.历史持仓查询
    @GetMapping("/positionHistory")
    public AjaxResult positionHistory(@RequestParam(required = false) String instType) {
        try { return success(okxApiClient.getPositionHistory(instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }
}