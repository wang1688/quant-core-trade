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
 * OKX 归档&历史大数据模块 13个
 */
@RestController
@RequestMapping("/okx/archive")
public class OkxArchiveController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.历史账单归档查询
    @GetMapping("/archiveBills")
    public AjaxResult archiveBills(@RequestParam(required = false) String ccy,
                                   @RequestParam(required = false) String instType){
        try { return success(okxApiClient.getArchiveBills(ccy,instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 2.历史成交归档
    @GetMapping("/archiveFills")
    public AjaxResult archiveFills(@RequestParam(required = false) String instId){
        try { return success(okxApiClient.getArchiveFills(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 3.历史订单归档
    @GetMapping("/archiveOrders")
    public AjaxResult archiveOrders(@RequestParam(required = false) String instId){
        try { return success(okxApiClient.getArchiveOrders(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 4.合约历史持仓归档
    @GetMapping("/archivePosition")
    public AjaxResult archivePosition(@RequestParam(required = false) String instType){
        try { return success(okxApiClient.getArchivePosition(instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 5.历史资金划转归档
    @GetMapping("/archiveTransfer")
    public AjaxResult archiveTransfer(){
        try { return success(okxApiClient.getArchiveTransfer()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 6.批量导出记录
    @GetMapping("/exportRecord")
    public AjaxResult exportRecord(){
        try { return success(okxApiClient.getExportRecord()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 7.大盘历史成交量
    @GetMapping("/volumeHistory")
    public AjaxResult volumeHistory(@RequestParam String instType){
        try { return success(okxApiClient.getMarketVolumeHistory(instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 8.历史深度归档
    @GetMapping("/archiveDepth")
    public AjaxResult archiveDepth(@RequestParam String instId){
        try { return success(okxApiClient.getArchiveDepth(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 9.超长周期K线归档
    @GetMapping("/archiveCandle")
    public AjaxResult archiveCandle(@RequestParam String instId,
                                    @RequestParam(required = false) String bar){
        try { return success(okxApiClient.getArchiveCandle(instId,bar)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 10.历史资金费率归档
    @GetMapping("/archiveFundingRate")
    public AjaxResult archiveFundingRate(@RequestParam String instId){
        try { return success(okxApiClient.getArchiveFundingRate(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 11.历史清算记录
    @GetMapping("/archiveLiquidation")
    public AjaxResult archiveLiquidation(@RequestParam String instType){
        try { return success(okxApiClient.getArchiveLiquidation(instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 12.币种历史行情统计
    @GetMapping("/coinStatHistory")
    public AjaxResult coinStatHistory(@RequestParam String ccy){
        try { return success(okxApiClient.getCoinStatHistory(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 13.链上历史记录查询
    @GetMapping("/onchainArchive")
    public AjaxResult onchainArchive(@RequestParam String ccy){
        try { return success(okxApiClient.getOnchainArchive(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }
}