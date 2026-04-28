package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKC C2C场外交易模块 12个接口
 */
@RestController
@RequestMapping("/okx/c2c")
public class OkxC2cController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.C2C交易币种列表
    @GetMapping("/currency")
    public AjaxResult currency(){
        try{return success(okxApiClient.getC2cCurrencies());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 2.C2C广告单列表
    @GetMapping("/ads")
    public AjaxResult ads(@RequestParam(required = false) String ccy,
                          @RequestParam(required = false) String side,
                          @RequestParam(required = false) String payment){
        try{return success(okxApiClient.getC2cAds(ccy,side,payment));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 3.创建C2C订单
    @PostMapping("/createOrder")
    public AjaxResult createOrder(@RequestParam String adId,
                                  @RequestParam String ccy,
                                  @RequestParam String amt){
        try{return success(okxApiClient.createC2cOrder(adId,ccy,amt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 4.C2C订单列表
    @GetMapping("/orders")
    public AjaxResult orders(@RequestParam(required = false) String ccy,
                             @RequestParam(required = false) String status){
        try{return success(okxApiClient.getC2cOrders(ccy,status));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 5.确认收款/付款
    @PostMapping("/confirmPay")
    public AjaxResult confirmPay(@RequestParam String orderId,
                                 @RequestParam String op){
        try{return success(okxApiClient.confirmC2cPay(orderId,op));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 6.申诉C2C订单
    @PostMapping("/appeal")
    public AjaxResult appeal(@RequestParam String orderId,
                             @RequestParam String reason){
        try{return success(okxApiClient.appealC2cOrder(orderId,reason));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 7.关闭C2C订单
    @PostMapping("/closeOrder")
    public AjaxResult closeOrder(@RequestParam String orderId){
        try{return success(okxApiClient.closeC2cOrder(orderId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 8.我的发布广告
    @GetMapping("/myAds")
    public AjaxResult myAds(){
        try{return success(okxApiClient.getC2cMyAds());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 9.发布C2C广告
    @PostMapping("/createAd")
    public AjaxResult createAd(@RequestParam String ccy,
                              @RequestParam String side,
                              @RequestParam String price,
                              @RequestParam String minAmt,
                              @RequestParam String maxAmt){
        try{return success(okxApiClient.createC2cAd(ccy,side,price,minAmt,maxAmt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 10.下架广告
    @PostMapping("/offlineAd")
    public AjaxResult offlineAd(@RequestParam String adId){
        try{return success(okxApiClient.offlineC2cAd(adId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 11.C2C支付方式列表
    @GetMapping("/paymentList")
    public AjaxResult paymentList(){
        try{return success(okxApiClient.getC2cPaymentList());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 12.C2C手续费查询
    @GetMapping("/feeRate")
    public AjaxResult feeRate(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getC2cFeeRate(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }
}