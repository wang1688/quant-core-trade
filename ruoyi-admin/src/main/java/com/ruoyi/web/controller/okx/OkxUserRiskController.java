package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 用户&权限&风控模块 14个
 */
@RestController
@RequestMapping("/okx/userRisk")
public class OkxUserRiskController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.获取用户基础信息
    @GetMapping("/userInfo")
    public AjaxResult userInfo(){
        try { return success(okxApiClient.getUserInfo()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 2.接口访问权限查询
    @GetMapping("/apiPermission")
    public AjaxResult apiPermission(){
        try { return success(okxApiClient.getApiPermission()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 3.绑定IP白名单
    @PostMapping("/setIpWhitelist")
    public AjaxResult setIpWhitelist(@RequestParam String ip){
        try { return success(okxApiClient.setIpWhitelist(ip)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 4.查询IP白名单
    @GetMapping("/ipWhitelist")
    public AjaxResult ipWhitelist(){
        try { return success(okxApiClient.getIpWhitelist()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 5.交易风险等级
    @GetMapping("/riskLevel")
    public AjaxResult riskLevel(){
        try { return success(okxApiClient.getRiskLevel()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 6.提现安全设置查询
    @GetMapping("/withdrawSecurity")
    public AjaxResult withdrawSecurity(){
        try { return success(okxApiClient.getWithdrawSecurity()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 7.行为风控记录
    @GetMapping("/riskOperationLog")
    public AjaxResult riskOperationLog(){
        try { return success(okxApiClient.getRiskOperationLog()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 8.账号登录日志
    @GetMapping("/loginLog")
    public AjaxResult loginLog(){
        try { return success(okxApiClient.getLoginLog()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 9.身份认证信息
    @GetMapping("/kycInfo")
    public AjaxResult kycInfo(){
        try { return success(okxApiClient.getKycInfo()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 10.地区限制查询
    @GetMapping("/regionLimit")
    public AjaxResult regionLimit(){
        try { return success(okxApiClient.getRegionLimit()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 11.手续费档位查询
    @GetMapping("/feeTier")
    public AjaxResult feeTier(){
        try { return success(okxApiClient.getFeeTier()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 12.交易活跃度数据
    @GetMapping("/userActivity")
    public AjaxResult userActivity(){
        try { return success(okxApiClient.getUserActivity()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 13.消息订阅配置
    @PostMapping("/setMsgSubscribe")
    public AjaxResult setMsgSubscribe(@RequestParam String type,@RequestParam String status){
        try { return success(okxApiClient.setMsgSubscribe(type,status)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 14.账号资产摘要
    @GetMapping("/assetSummary")
    public AjaxResult assetSummary(){
        try { return success(okxApiClient.getAssetSummary()); }
        catch (Exception e) { return error(e.getMessage()); }
    }
}