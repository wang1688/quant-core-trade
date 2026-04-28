package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 工具&小众业务模块 10个
 */
@RestController
@RequestMapping("/okx/tool")
public class OkxToolController extends BaseController{
    @Autowired
    private OkxApiClient okxApiClient;

    // 1.一键划转全部余额
    @PostMapping("/transferAllBalance")
    public AjaxResult transferAllBalance(@RequestParam String from,@RequestParam String to){
        try { return success(okxApiClient.transferAllBalance(from,to)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 2.币种风险提示
    @GetMapping("/coinRiskTip")
    public AjaxResult coinRiskTip(@RequestParam(required = false) String ccy){
        try { return success(okxApiClient.getCoinRiskTip(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 3.节假日交易安排
    @GetMapping("/tradeHoliday")
    public AjaxResult tradeHoliday(){
        try { return success(okxApiClient.getTradeHoliday()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 4.系统公告列表
    @GetMapping("/noticeList")
    public AjaxResult noticeList(){
        try { return success(okxApiClient.getNoticeList()); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 5.行情涨跌榜
    @GetMapping("/marketRank")
    public AjaxResult marketRank(@RequestParam String instType){
        try { return success(okxApiClient.getMarketRank(instType)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 6.大宗交易下单
    @PostMapping("/placeBlockOrder")
    public AjaxResult placeBlockOrder(@RequestParam String instId,
                                      @RequestParam String side,
                                      @RequestParam String amt,
                                      @RequestParam String px){
        try { return success(okxApiClient.placeBlockOrder(instId,side,amt,px)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 7.大宗交易记录
    @GetMapping("/blockFills")
    public AjaxResult blockFills(@RequestParam(required = false) String instId){
        try { return success(okxApiClient.getBlockFills(instId)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 8.跨链划转
    @PostMapping("/crossChainTransfer")
    public AjaxResult crossChainTransfer(@RequestParam String ccy,
                                         @RequestParam String amt,
                                         @RequestParam String fromChain,
                                         @RequestParam String toChain){
        try { return success(okxApiClient.crossChainTransfer(ccy,amt,fromChain,toChain)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 9.燃烧销毁记录
    @GetMapping("/burnHistory")
    public AjaxResult burnHistory(@RequestParam(required = false) String ccy){
        try { return success(okxApiClient.getBurnHistory(ccy)); }
        catch (Exception e) { return error(e.getMessage()); }
    }

    // 10.平台限制开关
    @GetMapping("/platformSwitch")
    public AjaxResult platformSwitch(){
        try { return success(okxApiClient.getPlatformSwitch()); }
        catch (Exception e) { return error(e.getMessage()); }
    }
}