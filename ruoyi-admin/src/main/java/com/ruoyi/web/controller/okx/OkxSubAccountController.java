package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 子账户模块（15个接口）
 * 子账户管理、资金划转、APIKey、充值提现、权限控制
 */
@RestController
@RequestMapping("/okx/subAccount")
public class OkxSubAccountController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // ====================================== 1. 获取子账户列表 ======================================
    /**
     * 1. 获取所有子账户列表
     */
    @GetMapping("/list")
    public AjaxResult list() {
        try {
            return success(okxApiClient.getSubAccountList());
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 2. 创建子账户 ======================================
    /**
     * 2. 创建子账户
     * @param subAcct 【必填】子账户名
     * @param label   【可选】备注
     */
    @PostMapping("/create")
    public AjaxResult create(
            @RequestParam String subAcct,
            @RequestParam(required = false) String label) {
        try {
            return success(okxApiClient.createSubAccount(subAcct, label));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 3. 子账户资金划转 ======================================
    /**
     * 3. 主账户 ↔ 子账户 / 子账户 ↔ 子账户 划转
     */
    @PostMapping("/transfer")
    public AjaxResult transfer(
            @RequestParam String ccy,
            @RequestParam String amt,
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String fromSubAccount,
            @RequestParam String toSubAccount) {
        try {
            return success(okxApiClient.subAccountTransfer(ccy, amt, from, to, fromSubAccount, toSubAccount));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 4. 查询子账户余额 ======================================
    /**
     * 4. 查询子账户资金余额
     * @param subAcct 【必填】子账户
     */
    @GetMapping("/balance")
    public AjaxResult balance(@RequestParam String subAcct) {
        try {
            return success(okxApiClient.getSubAccountBalance(subAcct));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 5. 创建子账户APIKey ======================================
    @PostMapping("/createApiKey")
    public AjaxResult createApiKey(
            @RequestParam String subAcct,
            @RequestParam String label,
            @RequestParam String perm,
            @RequestParam String ip) {
        try {
            return success(okxApiClient.createSubApiKey(subAcct, label, perm, ip));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 6. 查询子账户APIKey ======================================
    @GetMapping("/apiKey")
    public AjaxResult apiKey(
            @RequestParam String subAcct,
            @RequestParam String apiKey) {
        try {
            return success(okxApiClient.getSubApiKey(subAcct, apiKey));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 7. 修改子账户APIKey ======================================
    @PostMapping("/updateApiKey")
    public AjaxResult updateApiKey(
            @RequestParam String subAcct,
            @RequestParam String apiKey,
            @RequestParam String label,
            @RequestParam String perm,
            @RequestParam String ip) {
        try {
            return success(okxApiClient.updateSubApiKey(subAcct, apiKey, label, perm, ip));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 8. 删除子账户APIKey ======================================
    @PostMapping("/deleteApiKey")
    public AjaxResult deleteApiKey(
            @RequestParam String subAcct,
            @RequestParam String apiKey) {
        try {
            return success(okxApiClient.deleteSubApiKey(subAcct, apiKey));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 9. 设置子账户权限 ======================================
    @PostMapping("/setPermission")
    public AjaxResult setPermission(
            @RequestParam String subAcct,
            @RequestParam String ip,
            @RequestParam String perm) {
        try {
            return success(okxApiClient.setSubAccountPermission(subAcct, ip, perm));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 10. 冻结/解冻子账户 ======================================
    @PostMapping("/freeze")
    public AjaxResult freeze(
            @RequestParam String subAcct,
            @RequestParam String frozen) {
        try {
            return success(okxApiClient.freezeSubAccount(subAcct, frozen));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 11. 子账户充值地址 ======================================
    @GetMapping("/depositAddress")
    public AjaxResult depositAddress(
            @RequestParam String subAcct,
            @RequestParam String ccy,
            @RequestParam(required = false) String chain) {
        try {
            return success(okxApiClient.getSubDepositAddress(subAcct, ccy, chain));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 12. 子账户充值记录 ======================================
    @GetMapping("/depositHistory")
    public AjaxResult depositHistory(
            @RequestParam String subAcct,
            @RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getSubDepositHistory(subAcct, ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 13. 子账户提币记录 ======================================
    @GetMapping("/withdrawHistory")
    public AjaxResult withdrawHistory(
            @RequestParam String subAcct,
            @RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getSubWithdrawHistory(subAcct, ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 14. 子账户交易流水 ======================================
    @GetMapping("/bills")
    public AjaxResult bills(
            @RequestParam String subAcct,
            @RequestParam String instType,
            @RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getSubBills(subAcct, instType, ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 15. 子账户总资产 ======================================
    @GetMapping("/assetValuation")
    public AjaxResult assetValuation(@RequestParam String subAcct) {
        try {
            return success(okxApiClient.getSubAssetValuation(subAcct));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}