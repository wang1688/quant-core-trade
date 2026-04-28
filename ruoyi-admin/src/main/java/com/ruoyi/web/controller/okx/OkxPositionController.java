package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 合约仓位扩展模块（12个接口）
 */
@RestController
@RequestMapping("/okx/position")
public class OkxPositionController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1. 获取全部持仓
    @GetMapping("/all")
    public AjaxResult positions(@RequestParam String instType){
        try{
            return success(okxApiClient.getPositionsAll(instType));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 2. 调整逐仓保证金
    @PostMapping("/margin")
    public AjaxResult margin(
            @RequestParam String instId,
            @RequestParam String posId,
            @RequestParam String amt,
            @RequestParam String type){
        try{
            return success(okxApiClient.positionMargin(instId,posId,amt,type));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 3. 一键全平
    @PostMapping("/closeAll")
    public AjaxResult closeAll(@RequestParam String instType){
        try{
            return success(okxApiClient.closeAllPosition(instType));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 4. 设置止盈止损
    @PostMapping("/setSlTp")
    public AjaxResult setSlTp(
            @RequestParam String instId,
            @RequestParam String posId,
            @RequestParam String slPx,
            @RequestParam String tpPx){
        try{
            return success(okxApiClient.setPosSlTp(instId,posId,slPx,tpPx));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 5. 资金费率历史
    @GetMapping("/fundingHistory")
    public AjaxResult fundingHistory(
            @RequestParam String instId,
            @RequestParam(required = false) String limit){
        try{
            return success(okxApiClient.fundingRateHistory(instId,limit));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 6. 设置持仓模式
    @PostMapping("/setMode")
    public AjaxResult setMode(@RequestParam String posMode){
        try{
            return success(okxApiClient.setPositionMode(posMode));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 7. 设置杠杆
    @PostMapping("/setLeverage")
    public AjaxResult setLeverage(
            @RequestParam String instId,
            @RequestParam String lever,
            @RequestParam String mgnMode,
            @RequestParam(required = false) String posSide){
        try{
            return success(okxApiClient.setLeverage(instId,lever,mgnMode,posSide));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 8. 设置账户模式
    @PostMapping("/setAccountLevel")
    public AjaxResult setAccountLevel(@RequestParam String acctLv){
        try{
            return success(okxApiClient.setAccountLevel(acctLv));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 9. 获取账户配置
    @GetMapping("/config")
    public AjaxResult config(){
        try{
            return success(okxApiClient.getAccountConfig());
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 10. 历史结算
    @GetMapping("/settlements")
    public AjaxResult settlements(
            @RequestParam String instType,
            @RequestParam(required = false) String instId){
        try{
            return success(okxApiClient.getSettlements(instType,instId));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 11. 自动追加保证金
    @PostMapping("/autoMargin")
    public AjaxResult autoMargin(
            @RequestParam String instId,
            @RequestParam String type){
        try{
            return success(okxApiClient.setAutoMargin(instId,type));
        }catch (Exception e){
            return error(e.getMessage());
        }
    }

    // 12. 风险敞口
    @GetMapping("/risk")
    public AjaxResult risk(){
        try{
            return success(okxApiClient.getAccountRisk());
        }catch (Exception e){
            return error(e.getMessage());
        }
    }
}