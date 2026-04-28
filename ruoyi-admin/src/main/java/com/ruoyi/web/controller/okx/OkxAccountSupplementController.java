package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 账户补充+海外机构专属接口
 */
@RestController
@RequestMapping("/okx/accountSupplement")
public class OkxAccountSupplementController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // 1.统一账户资产估值（海外）
    @GetMapping("/assetValuation")
    public AjaxResult assetValuation(@RequestParam(required = false) String ccy) {
        try { return success(okxApiClient.getAssetValuation(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 2.账户固定估值信息
    @GetMapping("/fixedValuation")
    public AjaxResult fixedValuation() {
        try { return success(okxApiClient.getFixedValuation()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 3.风险状态详情
    @GetMapping("/riskState")
    public AjaxResult riskState() {
        try { return success(okxApiClient.getRiskState()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 4.设置自动借币（海外机构）
    @PostMapping("/setAutoLoan")
    public AjaxResult setAutoLoan(@RequestParam String autoLoan) {
        try { return success(okxApiClient.setAutoLoan(autoLoan)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 5.子账户汇总资产（海外专属）
    @GetMapping("/subAccountBalance")
    public AjaxResult subAccountBalance(@RequestParam(required = false) String instType) {
        try { return success(okxApiClient.getSubAccountBalance(instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 6.子账户汇总持仓（海外专属）
    @GetMapping("/subAccountPosition")
    public AjaxResult subAccountPosition(@RequestParam(required = false) String instType) {
        try { return success(okxApiClient.getSubAccountPosition(instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 7.账户内部划转（海外）
    @PostMapping("/innerTransfer")
    public AjaxResult innerTransfer(@RequestParam String from,
                                    @RequestParam String to,
                                    @RequestParam String ccy,
                                    @RequestParam String amt) {
        try { return success(okxApiClient.innerTransfer(from, to, ccy, amt)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 8.划转订单状态查询
    @GetMapping("/transferState")
    public AjaxResult transferState(@RequestParam String transferId) {
        try { return success(okxApiClient.getTransferState(transferId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 9.计息明细记录
    @GetMapping("/interestAccrued")
    public AjaxResult interestAccrued(@RequestParam(required = false) String ccy) {
        try { return success(okxApiClient.getInterestAccrued(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 10.借币历史明细
    @GetMapping("/borrowHistory")
    public AjaxResult borrowHistory(@RequestParam(required = false) String ccy) {
        try { return success(okxApiClient.getBorrowHistory(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 11.手动借币下单
    @PostMapping("/borrow")
    public AjaxResult borrow(@RequestParam String ccy, @RequestParam String amt) {
        try { return success(okxApiClient.accountBorrow(ccy, amt)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 12.手动归还借币
    @PostMapping("/repay")
    public AjaxResult repay(@RequestParam String ccy, @RequestParam String amt) {
        try { return success(okxApiClient.accountRepay(ccy, amt)); }
        catch (Exception e) { return error(e.getMessage()); }
    }
}