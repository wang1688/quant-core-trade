package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 公共接口补充/费率/爆仓/理财公共数据
 */
@RestController
@RequestMapping("/okx/publicSupplement")
public class OkxPublicSupplementController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.产品基础列表
    @GetMapping("/instruments")
    public AjaxResult instruments(@RequestParam String instType){
        try{return success(okxApiClient.getInstruments(instType));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 2.交易费率档位
    @GetMapping("/feeRateInfo")
    public AjaxResult feeRateInfo(@RequestParam String instType){
        try{return success(okxApiClient.getFeeRateInfo(instType));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 3.杠杆借贷利率公共
    @GetMapping("/publicInterestRate")
    public AjaxResult publicInterestRate(){
        try{return success(okxApiClient.getPublicInterestRate());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 4.爆仓订单公共
    @GetMapping("/liquidationOrders")
    public AjaxResult liquidationOrders(@RequestParam String instType){
        try{return success(okxApiClient.getLiquidationOrders(instType));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 5.理财产品公共
    @GetMapping("/publicEarnProduct")
    public AjaxResult publicEarnProduct(){
        try{return success(okxApiClient.getPublicEarnProduct());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 6.质押挖矿公共
    @GetMapping("/publicStakeProduct")
    public AjaxResult publicStakeProduct(){
        try{return success(okxApiClient.getPublicStakeProduct());}
        catch (Exception e){return error(e.getMessage());}
    }
}