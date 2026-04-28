package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 交易模块控制器
 * 功能：下单、撤单、改单、订单查询、成交记录
 * 全局环境统一配置，接口无需传环境参数
 */
@RestController
@RequestMapping("/okx/trade")
public class OkxTradeController extends BaseController {

    /**
     * 注入全局单例OKX客户端
     * 全局复用、线程安全
     */
    @Autowired
    private OkxApiClient okxApiClient;

    // ====================================== 1. 下单 ======================================
    /**
     * 下单接口
     * @param instId   【必填】交易对 例：BTC-USDT-SWAP
     * @param tdMode   【必填】账户模式 cross全仓/isolated逐仓/cash现货
     * @param side     【必填】买卖方向 buy买/sell卖
     * @param ordType  【必填】订单类型 limit限价/market市价
     * @param sz       【必填】数量
     * @param px       【可选】价格（市价单不传）
     */
    @PostMapping("/placeOrder")
    public AjaxResult placeOrder(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String ordType,
            @RequestParam String sz,
            @RequestParam(required = false) String px) {
        try {
            return success(okxApiClient.placeOrder(instId, tdMode, side, ordType, sz, px));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 2. 批量下单 ======================================
    /**
     * 批量下单（最多10单）
     * @param instId   【必填】交易对
     * @param tdMode   【必填】模式
     * @param side     【必填】方向
     * @param ordType  【必填】类型
     * @param sz       【必填】数量
     * @param px       【必填】价格
     */
    @PostMapping("/batchOrders")
    public AjaxResult batchOrders(
            @RequestParam String instId,
            @RequestParam String tdMode,
            @RequestParam String side,
            @RequestParam String ordType,
            @RequestParam String sz,
            @RequestParam String px) {
        try {
            return success(okxApiClient.batchOrders(instId, tdMode, side, ordType, sz, px));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 3. 撤单 ======================================
    /**
     * 撤销指定订单
     * @param instId 【必填】交易对
     * @param ordId  【必填】订单ID
     */
    @PostMapping("/cancelOrder")
    public AjaxResult cancelOrder(
            @RequestParam String instId,
            @RequestParam String ordId) {
        try {
            return success(okxApiClient.cancelOrder(instId, ordId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 4. 批量撤单 ======================================
    /**
     * 批量撤单
     * @param instId  【必填】交易对
     * @param ordIds  【必填】订单ID，多个用英文逗号分隔
     */
    @PostMapping("/batchCancelOrders")
    public AjaxResult batchCancelOrders(
            @RequestParam String instId,
            @RequestParam String ordIds) {
        try {
            return success(okxApiClient.batchCancelOrders(instId, ordIds));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 5. 修改订单 ======================================
    /**
     * 修改未成交订单的数量/价格
     * @param instId  【必填】交易对
     * @param ordId   【必填】订单ID
     * @param newSz   【可选】新数量
     * @param newPx   【可选】新价格
     */
    @PostMapping("/amendOrder")
    public AjaxResult amendOrder(
            @RequestParam String instId,
            @RequestParam String ordId,
            @RequestParam(required = false) String newSz,
            @RequestParam(required = false) String newPx) {
        try {
            return success(okxApiClient.amendOrder(instId, ordId, newSz, newPx));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 6. 未成交订单 ======================================
    /**
     * 查询当前未成交、部分成交订单
     * @param instId   【可选】交易对
     * @param ordType  【可选】订单类型
     */
    @GetMapping("/pendingOrders")
    public AjaxResult pendingOrders(
            @RequestParam(required = false) String instId,
            @RequestParam(required = false) String ordType) {
        try {
            return success(okxApiClient.getPendingOrders(instId, ordType));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 7. 订单详情 ======================================
    /**
     * 查询单个订单详情
     * @param instId 【必填】交易对
     * @param ordId  【必填】订单ID
     */
    @GetMapping("/order")
    public AjaxResult order(
            @RequestParam String instId,
            @RequestParam String ordId) {
        try {
            return success(okxApiClient.getOrder(instId, ordId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 8. 历史订单 ======================================
    /**
     * 查询已完结订单（超过3个月查归档）
     * @param instId 【必填】交易对
     * @param state  【必填】状态 filled完全成交/cancelled已撤销
     */
    @GetMapping("/ordersHistory")
    public AjaxResult ordersHistory(
            @RequestParam String instId,
            @RequestParam String state) {
        try {
            return success(okxApiClient.getOrdersHistory(instId, state));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 9. 最近成交记录 ======================================
    /**
     * 获取最近的成交明细
     * @param instId 【可选】交易对
     * @param ordId  【可选】订单ID
     */
    @GetMapping("/trades")
    public AjaxResult trades(
            @RequestParam(required = false) String instId,
            @RequestParam(required = false) String ordId) {
        try {
            return success(okxApiClient.getTrades(instId, ordId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 10. 成交历史 ======================================
    /**
     * 获取账户全部成交历史
     * @param instId 【必填】交易对
     */
    @GetMapping("/fillsHistory")
    public AjaxResult fillsHistory(@RequestParam String instId) {
        try {
            return success(okxApiClient.getFillsHistory(instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
}