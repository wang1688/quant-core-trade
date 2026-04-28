package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 策略交易/高级委托模块 20个
 * 条件单、止盈止损、追踪、冰山、TWAP、网格策略
 */
@RestController
@RequestMapping("/okx/strategy")
public class OkxStrategyController extends BaseController {

    @Autowired
    private OkxApiClient okxApiClient;

    // 1.策略条件单下单
    @PostMapping("/placeAlgo")
    public AjaxResult placeAlgo(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String ordType,
            @RequestParam String sz,
            @RequestParam String triggerPx,
            @RequestParam String orderPx) {
        try {
            return success(okxApiClient.placeAlgoOrder(instId,tdMode,side,ordType,sz,triggerPx,orderPx));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 2.撤销单个策略单
    @PostMapping("/cancelAlgo")
    public AjaxResult cancelAlgo(
            @RequestParam String instId,
            @RequestParam String algoId) {
        try {
            return success(okxApiClient.cancelAlgoOrder(instId,algoId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 3.批量撤销策略单
    @PostMapping("/cancelBatchAlgo")
    public AjaxResult cancelBatchAlgo(
            @RequestParam String instId,
            @RequestParam String algoIds) {
        try {
            return success(okxApiClient.cancelBatchAlgo(instId,algoIds));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 4.未触发策略单列表
    @GetMapping("/algoPending")
    public AjaxResult algoPending(
            @RequestParam(required = false) String instId,
            @RequestParam(required = false) String ordType) {
        try {
            return success(okxApiClient.getAlgoPending(instId,ordType));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 5.策略单历史
    @GetMapping("/algoHistory")
    public AjaxResult algoHistory(
            @RequestParam(required = false) String instId,
            @RequestParam(required = false) String state) {
        try {
            return success(okxApiClient.getAlgoHistory(instId,state));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 6.追踪委托下单
    @PostMapping("/placeTrack")
    public AjaxResult placeTrack(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String sz,
            @RequestParam String callbackRatio) {
        try {
            return success(okxApiClient.placeTrackOrder(instId,tdMode,side,sz,callbackRatio));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 7.撤销追踪委托
    @PostMapping("/cancelTrack")
    public AjaxResult cancelTrack(
            @RequestParam String instId,
            @RequestParam String trackId) {
        try {
            return success(okxApiClient.cancelTrackOrder(instId,trackId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 8.进行中追踪委托
    @GetMapping("/trackPending")
    public AjaxResult trackPending(@RequestParam(required = false) String instId) {
        try {
            return success(okxApiClient.getTrackPending(instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 9.冰山委托下单
    @PostMapping("/placeIceberg")
    public AjaxResult placeIceberg(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String sz,
            @RequestParam String displaySz) {
        try {
            return success(okxApiClient.placeIcebergOrder(instId,tdMode,side,sz,displaySz));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 10.TWAP时间加权下单
    @PostMapping("/placeTwap")
    public AjaxResult placeTwap(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String sz,
            @RequestParam String duration) {
        try {
            return success(okxApiClient.placeTwapOrder(instId,tdMode,side,sz,duration));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 11.全局止盈止损配置
    @PostMapping("/setGlobalSlTp")
    public AjaxResult setGlobalSlTp(
            @RequestParam String slTriggerType,
            @RequestParam String tpTriggerType) {
        try {
            return success(okxApiClient.setGlobalSlTp(slTriggerType,tpTriggerType));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 12.一键全部持仓止盈止损平仓
    @PostMapping("/closeAllSlTp")
    public AjaxResult closeAllSlTp(@RequestParam String instType) {
        try {
            return success(okxApiClient.closeAllSlTp(instType));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 13.高级限价委托
    @PostMapping("/placeAdvancedLimit")
    public AjaxResult placeAdvancedLimit(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String sz,
            @RequestParam String px,
            @RequestParam String timeInForce) {
        try {
            return success(okxApiClient.placeAdvancedLimit(instId,tdMode,side,sz,px,timeInForce));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 14.溢价保护委托
    @PostMapping("/placePriceProtect")
    public AjaxResult placePriceProtect(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String sz,
            @RequestParam String slippage) {
        try {
            return success(okxApiClient.placePriceProtectOrder(instId,tdMode,side,sz,slippage));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 15.策略单详情
    @GetMapping("/algoDetail")
    public AjaxResult algoDetail(@RequestParam String algoId) {
        try {
            return success(okxApiClient.getAlgoDetail(algoId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 16.修改策略单
    @PostMapping("/amendAlgo")
    public AjaxResult amendAlgo(
            @RequestParam String instId,
            @RequestParam String algoId,
            @RequestParam(required = false) String triggerPx,
            @RequestParam(required = false) String orderPx) {
        try {
            return success(okxApiClient.amendAlgoOrder(instId,algoId,triggerPx,orderPx));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 17.创建网格策略
    @PostMapping("/createGrid")
    public AjaxResult createGrid(
            @RequestParam String instId,
            @RequestParam String gridType,
            @RequestParam String minPx,
            @RequestParam String maxPx,
            @RequestParam String gridNum) {
        try {
            return success(okxApiClient.createGridStrategy(instId,gridType,minPx,maxPx,gridNum));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 18.网格策略列表
    @GetMapping("/gridList")
    public AjaxResult gridList(
            @RequestParam(required = false) String instId,
            @RequestParam(required = false) String gridType) {
        try {
            return success(okxApiClient.getGridList(instId,gridType));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 19.暂停网格策略
    @PostMapping("/pauseGrid")
    public AjaxResult pauseGrid(@RequestParam String strategyId) {
        try {
            return success(okxApiClient.pauseGridStrategy(strategyId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // 20.终止网格策略
    @PostMapping("/stopGrid")
    public AjaxResult stopGrid(@RequestParam String strategyId) {
        try {
            return success(okxApiClient.stopGridStrategy(strategyId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}