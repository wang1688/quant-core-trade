package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 期权/交割合约/衍生品模块 16个接口
 */
@RestController
@RequestMapping("/okx/derivatives")
public class OkxDerivativesController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.期权持仓
    @GetMapping("/optionPosition")
    public AjaxResult optionPosition(@RequestParam(required = false) String instId){
        try{return success(okxApiClient.getOptionPositions(instId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 2.期权希腊字母风险
    @GetMapping("/greeks")
    public AjaxResult greeks(){
        try{return success(okxApiClient.getOptionGreeks());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 3.交割合约结算历史
    @GetMapping("/futuresSettlement")
    public AjaxResult futuresSettlement(@RequestParam(required = false) String instId){
        try{return success(okxApiClient.getFuturesSettlement(instId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 4.行权履约记录
    @GetMapping("/exerciseHistory")
    public AjaxResult exerciseHistory(@RequestParam(required = false) String instId){
        try{return success(okxApiClient.getExerciseHistory(instId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 5.手动行权
    @PostMapping("/exerciseOption")
    public AjaxResult exerciseOption(@RequestParam String instId,
                                    @RequestParam String holdAmt){
        try{return success(okxApiClient.exerciseOption(instId,holdAmt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 6.自动行权设置
    @PostMapping("/setAutoExercise")
    public AjaxResult setAutoExercise(@RequestParam String enable){
        try{return success(okxApiClient.setAutoExercise(enable));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 7.期权溢价指数
    @GetMapping("/optionVolIndex")
    public AjaxResult optionVolIndex(@RequestParam(required = false) String instId){
        try{return success(okxApiClient.getOptionVolIndex(instId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 8.期权链行情
    @GetMapping("/optionChain")
    public AjaxResult optionChain(@RequestParam String underlying){
        try{return success(okxApiClient.getOptionChain(underlying));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 9.交割合约历史K线
    @GetMapping("/futuresCandle")
    public AjaxResult futuresCandle(@RequestParam String instId,
                                    @RequestParam(required = false) String bar){
        try{return success(okxApiClient.getFuturesCandle(instId,bar));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 10.结算价查询
    @GetMapping("/settlePrice")
    public AjaxResult settlePrice(@RequestParam String instId){
        try{return success(okxApiClient.getSettlePrice(instId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 11.合约历史费率
    @GetMapping("/futuresRateHistory")
    public AjaxResult futuresRateHistory(@RequestParam String instId){
        try{return success(okxApiClient.getFuturesRateHistory(instId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 12.风险对冲配置
    @PostMapping("/setHedgeMode")
    public AjaxResult setHedgeMode(@RequestParam String hedgeMode){
        try{return success(okxApiClient.setHedgeMode(hedgeMode));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 13.批量行权查询
    @GetMapping("/batchExerciseList")
    public AjaxResult batchExerciseList(){
        try{return success(okxApiClient.getBatchExerciseList());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 14.期权损益汇总
    @GetMapping("/optionProfit")
    public AjaxResult optionProfit(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getOptionProfitSummary(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 15.交割计划查询
    @GetMapping("/deliveryPlan")
    public AjaxResult deliveryPlan(@RequestParam String instType){
        try{return success(okxApiClient.getDeliveryPlan(instType));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 16.合约阶梯费率
    @GetMapping("/futuresTierFee")
    public AjaxResult futuresTierFee(@RequestParam String instType){
        try{return success(okxApiClient.getFuturesTierFee(instType));}
        catch (Exception e){return error(e.getMessage());}
    }
}