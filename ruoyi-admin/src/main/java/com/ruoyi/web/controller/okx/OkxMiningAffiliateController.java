package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 海外挖矿/云算力/邀请返利/榜单
 */
@RestController
@RequestMapping("/okx/miningAffiliate")
public class OkxMiningAffiliateController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.全网算力挖矿（海外）
    @GetMapping("/miningPower")
    public AjaxResult miningPower(){
        try{return success(okxApiClient.getMiningPower());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 2.挖矿收益记录
    @GetMapping("/miningEarnHistory")
    public AjaxResult miningEarnHistory(){
        try{return success(okxApiClient.getMiningEarnHistory());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 3.矿机资产明细
    @GetMapping("/miningAsset")
    public AjaxResult miningAsset(){
        try{return success(okxApiClient.getMiningAsset());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 4.云算力订单
    @GetMapping("/cloudMiningOrder")
    public AjaxResult cloudMiningOrder(){
        try{return success(okxApiClient.getCloudMiningOrder());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 5.邀请返利数据（海外）
    @GetMapping("/inviteData")
    public AjaxResult inviteData(){
        try{return success(okxApiClient.getInviteData());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 6.返利流水记录
    @GetMapping("/affiliateCommission")
    public AjaxResult affiliateCommission(){
        try{return success(okxApiClient.getAffiliateCommission());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 7.邀请榜单排名
    @GetMapping("/inviteRank")
    public AjaxResult inviteRank(){
        try{return success(okxApiClient.getInviteRank());}
        catch (Exception e){return error(e.getMessage());}
    }
}