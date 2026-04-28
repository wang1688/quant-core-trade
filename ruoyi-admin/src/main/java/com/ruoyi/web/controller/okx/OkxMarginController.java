package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 杠杆&借贷模块
 * 借入、还款、利率、挂单、风险、流水全套
 */
@RestController
@RequestMapping("/okx/margin")
public class OkxMarginController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // ====================================== 1.杠杆账户资产总览 ======================================
    /**
     * 1. 杠杆账户资产总览
     * 无参数
     */
    @GetMapping("/account")
    public AjaxResult marginAccount(){
        try {
            return success(okxApiClient.getMarginAccount());
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 2.杠杆币种配置 ======================================
    /**
     * 2. 杠杆支持币种配置
     * @param ccy 【可选】币种
     */
    @GetMapping("/currency")
    public AjaxResult marginCurrency(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getMarginCurrencies(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 3.杠杆借入 ======================================
    /**
     * 3. 杠杆借入资产
     * @param ccy 【必填】币种
     * @param amt 【必填】数量
     * @param side【必填】方向 borrow借入
     */
    @PostMapping("/borrow")
    public AjaxResult borrow(
            @RequestParam String ccy,
            @RequestParam String amt,
            @RequestParam String side){
        try {
            return success(okxApiClient.loanBorrow(ccy,amt,side));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 4.杠杆还款 ======================================
    /**
     * 4. 单笔借款还款
     * @param ccy     【必填】币种
     * @param amt     【必填】还款数量
     * @param borrowId【必填】借款ID
     */
    @PostMapping("/repay")
    public AjaxResult repay(
            @RequestParam String ccy,
            @RequestParam String amt,
            @RequestParam String borrowId){
        try {
            return success(okxApiClient.loanRepay(ccy,amt,borrowId));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 5.借还款记录 ======================================
    /**
     * 5. 借入历史记录
     * @param ccy 【可选】币种
     */
    @GetMapping("/borrowHistory")
    public AjaxResult borrowHistory(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanBorrowHistory(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 6.还款记录 ======================================
    /**
     * 6. 还款历史记录
     * @param ccy 【可选】币种
     */
    @GetMapping("/repayHistory")
    public AjaxResult repayHistory(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanRepayHistory(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 7.未还清借款 ======================================
    /**
     * 7. 当前未结清借款列表
     * @param ccy 【可选】币种
     */
    @GetMapping("/outstanding")
    public AjaxResult outstanding(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanOutstanding(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 8.利息记录 ======================================
    /**
     * 8. 杠杆利息扣费记录
     * @param ccy 【可选】币种
     */
    @GetMapping("/interest")
    public AjaxResult interest(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanInterestHistory(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 9.市场借贷利率 ======================================
    /**
     * 9. 全网市场实时借贷利率
     * @param ccy 【可选】币种
     */
    @GetMapping("/marketRate")
    public AjaxResult marketRate(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanMarketRate(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 10.杠杆风险率 ======================================
    /**
     * 10. 账户杠杆风险率
     * 无参数
     */
    @GetMapping("/riskRate")
    public AjaxResult riskRate(){
        try {
            return success(okxApiClient.getMarginRiskRate());
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 11.杠杆强平记录 ======================================
    /**
     * 11. 杠杆强制平仓记录
     * 无参数
     */
    @GetMapping("/liquidation")
    public AjaxResult liquidation(){
        try {
            return success(okxApiClient.getMarginLiquidation());
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 12.一键全部还款 ======================================
    /**
     * 12. 指定币种一键全额还款
     * @param ccy 【必填】币种
     */
    @PostMapping("/repayAll")
    public AjaxResult repayAll(@RequestParam String ccy){
        try {
            return success(okxApiClient.loanRepayAll(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 13.自动还款设置 ======================================
    /**
     * 13. 开关杠杆自动还款
     * @param autoRepay 【必填】true/false
     */
    @PostMapping("/setAutoRepay")
    public AjaxResult setAutoRepay(@RequestParam String autoRepay){
        try {
            return success(okxApiClient.setAutoRepay(autoRepay));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 14.借贷挂单 ======================================
    /**
     * 14. 质押理财借出挂单
     * @param ccy 【必填】币种
     * @param amt 【必填】数量
     * @param rate【必填】年化利率
     */
    @PostMapping("/offer")
    public AjaxResult offer(
            @RequestParam String ccy,
            @RequestParam String amt,
            @RequestParam String rate){
        try {
            return success(okxApiClient.loanOffer(ccy,amt,rate));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 15.撤销借贷挂单 ======================================
    /**
     * 15. 撤销借出挂单
     * @param offerId 【必填】挂单ID
     */
    @PostMapping("/cancelOffer")
    public AjaxResult cancelOffer(@RequestParam String offerId){
        try {
            return success(okxApiClient.loanCancelOffer(offerId));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 16.未成交借贷挂单 ======================================
    /**
     * 16. 当前有效借贷挂单
     * @param ccy 【可选】币种
     */
    @GetMapping("/offerPending")
    public AjaxResult offerPending(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanOfferPending(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 17.借贷挂单历史 ======================================
    /**
     * 17. 借贷挂单历史记录
     * @param ccy 【可选】币种
     */
    @GetMapping("/offerHistory")
    public AjaxResult offerHistory(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanOfferHistory(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 18.借贷市场深度 ======================================
    /**
     * 18. 借贷市场买卖深度
     * @param ccy 【可选】币种
     */
    @GetMapping("/loanBook")
    public AjaxResult loanBook(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanMarketBook(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 19.杠杆额度 ======================================
    /**
     * 19. 币种杠杆可借限额
     * @param ccy 【可选】币种
     */
    @GetMapping("/quota")
    public AjaxResult quota(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getMarginQuota(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 20.跨币种杠杆配置 ======================================
    /**
     * 20. 跨仓杠杆全局配置
     * 无参数
     */
    @GetMapping("/crossConfig")
    public AjaxResult crossConfig(){
        try {
            return success(okxApiClient.getCrossMarginConfig());
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 21.跨仓杠杆流水 ======================================
    /**
     * 21. 跨仓杠杆资金流水
     * 无参数
     */
    @GetMapping("/crossBills")
    public AjaxResult crossBills(){
        try {
            return success(okxApiClient.getCrossMarginBills());
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // ====================================== 22.借贷公共限制 ======================================
    /**
     * 22. 币种借贷公共规则限制
     * @param ccy 【可选】币种
     */
    @GetMapping("/loanPublicLimit")
    public AjaxResult loanPublicLimit(@RequestParam(required = false) String ccy){
        try {
            return success(okxApiClient.getLoanPublicLimit(ccy));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }
}