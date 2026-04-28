package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 资产 / 资金模块
 * 余额、划转、充值、提现、资产估值
 */
@RestController
@RequestMapping("/okx/asset")
public class OkxAssetController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // ====================================== 1. 获取资金账户余额 ======================================
    /**
     * 1. 获取资金账户余额
     * @param ccy 【可选】币种，不传查询全部
     */
    @GetMapping("/balance")
    public AjaxResult balance(@RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getAssetBalance(ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 2. 获取账户总资产估值 ======================================
    /**
     * 2. 获取账户全部资产估值（折合USDT）
     * 无参数
     */
    @GetMapping("/assetValuation")
    public AjaxResult assetValuation() {
        try {
            return success(okxApiClient.getAssetAllValuation());
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 3. 资金划转（账户互转） ======================================
    /**
     * 3. 资金划转：资金/现货/合约/杠杆 互转
     * @param ccy    【必填】币种
     * @param amt    【必填】划转数量
     * @param from   【必填】转出账户
     * @param to     【必填】转入账户
     * @param instId 【可选】合约/杠杆交易对
     */
    @PostMapping("/transfer")
    public AjaxResult transfer(
            @RequestParam String ccy,
            @RequestParam String amt,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String instId) {
        try {
            return success(okxApiClient.transfer(ccy, amt, from, to, instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 4. 获取充值地址 ======================================
    /**
     * 4. 获取币种充值地址
     * @param ccy   【必填】币种
     * @param chain 【可选】链名称
     */
    @GetMapping("/depositAddress")
    public AjaxResult depositAddress(
            @RequestParam String ccy,
            @RequestParam(required = false) String chain) {
        try {
            return success(okxApiClient.getDepositAddress(ccy, chain));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 5. 获取充值记录 ======================================
    /**
     * 5. 获取充值历史记录
     * @param ccy 【可选】币种
     */
    @GetMapping("/depositHistory")
    public AjaxResult depositHistory(@RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getDepositHistory(ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 6. 提币 ======================================
    /**
     * 6. 提交提币申请
     * @param ccy     【必填】币种
     * @param chain   【必填】链名称
     * @param toAddr  【必填】提币地址
     * @param amt     【必填】提币数量
     * @param fee     【必填】矿工费
     */
    @PostMapping("/withdraw")
    public AjaxResult withdraw(
            @RequestParam String ccy,
            @RequestParam String chain,
            @RequestParam String toAddr,
            @RequestParam String amt,
            @RequestParam String fee) {
        try {
            return success(okxApiClient.withdraw(ccy, chain, toAddr, amt, fee));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 7. 获取提币记录 ======================================
    /**
     * 7. 获取提币历史记录
     * @param ccy 【可选】币种
     */
    @GetMapping("/withdrawHistory")
    public AjaxResult withdrawHistory(@RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getWithdrawHistory(ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}