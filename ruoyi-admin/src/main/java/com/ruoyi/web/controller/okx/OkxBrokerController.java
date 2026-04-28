package com.ruoyi.web.controller.okx;


import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 经纪商Broker&机构模块 12个
 */
@RestController
@RequestMapping("/okx/broker")
public class OkxBrokerController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.经纪商下级用户列表
    @GetMapping("/subUser")
    public AjaxResult subUser(){
        try { return success(okxApiClient.getBrokerSubUser()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 2.下级用户资产汇总
    @GetMapping("/subAsset")
    public AjaxResult subAsset(){
        try { return success(okxApiClient.getBrokerSubAsset()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 3.下级交易分成记录
    @GetMapping("/commission")
    public AjaxResult commission(){
        try { return success(okxApiClient.getBrokerCommission()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 4.经纪商手续费配置
    @GetMapping("/feeConfig")
    public AjaxResult feeConfig(){
        try { return success(okxApiClient.getBrokerFeeConfig()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 5.调整下级手续费比例
    @PostMapping("/setFeeRate")
    public AjaxResult setFeeRate(@RequestParam String subAcct,@RequestParam String rate){
        try { return success(okxApiClient.setBrokerFeeRate(subAcct,rate)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 6.经纪商数据大盘
    @GetMapping("/overview")
    public AjaxResult overview(){
        try { return success(okxApiClient.getBrokerOverview()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 7.机构VIP费率查询
    @GetMapping("/institutionFee")
    public AjaxResult institutionFee(){
        try { return success(okxApiClient.getInstitutionFee()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 8.机构授信额度
    @GetMapping("/creditQuota")
    public AjaxResult creditQuota(){
        try { return success(okxApiClient.getCreditQuota()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 9.联名活动数据
    @GetMapping("/partnerActivityData")
    public AjaxResult partnerActivityData(){
        try { return success(okxApiClient.getPartnerActivityData()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 10.渠道注册统计
    @GetMapping("/channelStat")
    public AjaxResult channelStat(){
        try { return success(okxApiClient.getChannelRegisterStat()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 11.经纪商白名单配置
    @PostMapping("/whiteList")
    public AjaxResult whiteList(@RequestParam String subAcct){
        try { return success(okxApiClient.setBrokerWhiteList(subAcct)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 12.下级账号批量操作
    @PostMapping("/batchOperate")
    public AjaxResult batchOperate(@RequestParam String subAcctList,@RequestParam String op){
        try { return success(okxApiClient.brokerBatchOperate(subAcctList,op)); }
        catch (Exception e) { return error(e.getMessage()); }
    }
}