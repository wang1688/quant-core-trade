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
 * OKX 公共模块
 * 服务器时间、币种信息、汇率
 */
@RestController
@RequestMapping("/okx/public")
public class OkxPublicController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // ====================================== 1. 获取服务器时间 ======================================
    /**
     * 1. 获取OKX服务器时间
     * 用于签名时间校准，无参数
     */
    @GetMapping("/serverTime")
    public AjaxResult serverTime() {
        try {
            return success(okxApiClient.getServerTime());
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 2. 获取币种配置 ======================================
    /**
     * 2. 获取所有币种/指定币种的链信息、精度、充值提币配置
     * @param ccy 【可选】币种，不传返回全部
     */
    @GetMapping("/currencies")
    public AjaxResult currencies(@RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getCurrencies(ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 3. 获取法币汇率 ======================================
    /**
     * 3. 获取法币与法币、法币与稳定币之间的汇率
     * 无参数
     */
    @GetMapping("/exchangeRate")
    public AjaxResult exchangeRate() {
        try {
            return success(okxApiClient.getExchangeRate());
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}