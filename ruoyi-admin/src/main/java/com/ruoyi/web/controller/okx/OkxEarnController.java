package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 理财/赚币/质押/定投模块 18个接口
 */
@RestController
@RequestMapping("/okx/earn")
public class OkxEarnController extends BaseController {
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.理财产品列表
    @GetMapping("/productList")
    public AjaxResult productList(@RequestParam(required = false) String ccy,
                                  @RequestParam(required = false) String type){
        try{return success(okxApiClient.getEarnProducts(ccy,type));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 2.持有理财资产
    @GetMapping("/holdings")
    public AjaxResult holdings(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getEarnHoldings(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 3.理财申购
    @PostMapping("/subscribe")
    public AjaxResult subscribe(@RequestParam String productId,
                                @RequestParam String ccy,
                                @RequestParam String amt){
        try{return success(okxApiClient.earnSubscribe(productId,ccy,amt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 4.理财赎回
    @PostMapping("/redeem")
    public AjaxResult redeem(@RequestParam String productId,
                             @RequestParam String ccy,
                             @RequestParam String amt){
        try{return success(okxApiClient.earnRedeem(productId,ccy,amt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 5.理财订单记录
    @GetMapping("/earnOrders")
    public AjaxResult earnOrders(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getEarnOrders(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 6.活期理财自动划转设置
    @PostMapping("/setAutoTransfer")
    public AjaxResult setAutoTransfer(@RequestParam String ccy,
                                      @RequestParam String auto){
        try{return success(okxApiClient.setEarnAutoTransfer(ccy,auto));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 7.活期收益记录
    @GetMapping("/interestHistory")
    public AjaxResult interestHistory(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getEarnInterestHistory(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 8.活动奖励记录
    @GetMapping("/rewardHistory")
    public AjaxResult rewardHistory(){
        try{return success(okxApiClient.getEarnRewardHistory());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 9.锁仓挖矿列表
    @GetMapping("/stakeProduct")
    public AjaxResult stakeProduct(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getStakeProducts(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 10.锁仓质押
    @PostMapping("/stakeSubscribe")
    public AjaxResult stakeSubscribe(@RequestParam String productId,
                                    @RequestParam String ccy,
                                    @RequestParam String amt){
        try{return success(okxApiClient.stakeSubscribe(productId,ccy,amt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 11.锁仓解锁
    @PostMapping("/stakeRedeem")
    public AjaxResult stakeRedeem(@RequestParam String productId,
                                  @RequestParam String ccy,
                                  @RequestParam String amt){
        try{return success(okxApiClient.stakeRedeem(productId,ccy,amt));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 12.锁仓持有明细
    @GetMapping("/stakeHoldings")
    public AjaxResult stakeHoldings(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getStakeHoldings(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 13.质押订单流水
    @GetMapping("/stakeOrders")
    public AjaxResult stakeOrders(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getStakeOrders(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 14.双币理财列表
    @GetMapping("/dualProduct")
    public AjaxResult dualProduct(@RequestParam(required = false) String ccy){
        try{return success(okxApiClient.getDualProducts(ccy));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 15.双币理财申购
    @PostMapping("/dualSubscribe")
    public AjaxResult dualSubscribe(@RequestParam String productId,
                                   @RequestParam String ccy,
                                   @RequestParam String amt,
                                   @RequestParam String direction){
        try{return success(okxApiClient.dualSubscribe(productId,ccy,amt,direction));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 16.双币持仓查询
    @GetMapping("/dualHoldings")
    public AjaxResult dualHoldings(){
        try{return success(okxApiClient.getDualHoldings());}
        catch (Exception e){return error(e.getMessage());}
    }

    // 17.定投计划列表
    @GetMapping("/dcPlanList")
    public AjaxResult dcPlanList(@RequestParam(required = false) String instId){
        try{return success(okxApiClient.getDcPlanList(instId));}
        catch (Exception e){return error(e.getMessage());}
    }

    // 18.创建定投计划
    @PostMapping("/createDcPlan")
    public AjaxResult createDcPlan(@RequestParam String instId,
                                   @RequestParam String ccy,
                                   @RequestParam String amt,
                                   @RequestParam String cycle){
        try{return success(okxApiClient.createDcPlan(instId,ccy,amt,cycle));}
        catch (Exception e){return error(e.getMessage());}
    }
}