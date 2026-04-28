package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 资产补充/海外多链/提币充值/红包/小额兑换
 */
@RestController
@RequestMapping("/okx/assetSupplement")
public class OkxAssetSupplementController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.获取币种充值地址（海外多链）
    @GetMapping("/depositAddress")
    public AjaxResult depositAddress(@RequestParam String ccy,
                                    @RequestParam(required = false) String chain){
        try{return success(okxApiClient.getDepositAddress(ccy,chain));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 2.充值地址列表
    @GetMapping("/depositAddressList")
    public AjaxResult depositAddressList(@RequestParam String ccy){
        try{return success(okxApiClient.getDepositAddressList(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 3.充值记录全量
    @GetMapping("/depositHistory")
    public AjaxResult depositHistory(@RequestParam(required = false) String ccy,
                                    @RequestParam(required = false) String state){
        try{return success(okxApiClient.getDepositHistory(ccy,state));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 4.提币提交（海外多链）
    @PostMapping("/withdraw")
    public AjaxResult withdraw(@RequestParam String ccy,
                              @RequestParam String chain,
                              @RequestParam String addr,
                              @RequestParam String amt,
                              @RequestParam String fee){
        try{return success(okxApiClient.withdraw(ccy,chain,addr,amt,fee));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 5.提币记录
    @GetMapping("/withdrawHistory")
    public AjaxResult withdrawHistory(@RequestParam(required = false) String ccy,
                                      @RequestParam(required = false) String state){
        try{return success(okxApiClient.getWithdrawHistory(ccy,state));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 6.取消提币
    @PostMapping("/cancelWithdraw")
    public AjaxResult cancelWithdraw(@RequestParam String wdId){
        try{return success(okxApiClient.cancelWithdraw(wdId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 7.内部转账免手续费（海外）
    @PostMapping("/innerTransferFree")
    public AjaxResult innerTransferFree(@RequestParam String uid,
                                        @RequestParam String ccy,
                                        @RequestParam String amt){
        try{return success(okxApiClient.innerTransferFree(uid,ccy,amt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 8.币种基础信息
    @GetMapping("/ccyInfo")
    public AjaxResult ccyInfo(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getCcyInfo(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 9.多链配置信息
    @GetMapping("/chainConfig")
    public AjaxResult chainConfig(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getChainConfig(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 10.小额资产兑换
    @PostMapping("/dustConvert")
    public AjaxResult dustConvert(){
        try{return success(okxApiClient.dustConvert());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 11.钱包账单流水
    @GetMapping("/assetBills")
    public AjaxResult assetBills(@RequestParam(required = false) String ccy,
                                  @RequestParam(required = false) String type){
        try{return success(okxApiClient.getAssetBills(ccy,type));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 12.红包发放（海外地区）
    @PostMapping("/sendRedPacket")
    public AjaxResult sendRedPacket(@RequestParam String ccy,
                                    @RequestParam String amt){
        try{return success(okxApiClient.sendRedPacket(ccy,amt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 13.红包记录查询
    @GetMapping("/redPacketHistory")
    public AjaxResult redPacketHistory(){
        try{return success(okxApiClient.getRedPacketHistory());}
        catch (Exception e){return error(e.getMessage());}
    }
}