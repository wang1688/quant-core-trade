package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * OKX 账户模块控制器
 * 全局统一模拟/实盘配置，接口无需传环境参数
 * GET 接口：url 拼接普通表单参数
 * POST 接口：form 表单传参
 */
@RestController
@RequestMapping("/okx/account")
public class OkxAccountController extends BaseController {

    /**
     * 注入全局单例OKX客户端
     * 全局只创建一次，线程安全，复用连接
     */
    @Autowired
    private OkxApiClient okxApiClient;

    // ====================================== 1.账户余额 ======================================
    /**
     * 获取账户全部/指定币种余额
     * @param ccy 【可选】币种，例：USDT、BTC，不传返回全部币种
     */
    @GetMapping("/balance")
    public AjaxResult balance(@RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getAccountBalance(ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 2.持仓信息 ======================================
    /**
     * 获取当前持仓列表
     * @param instType 【可选】产品类型 SPOT现货/SWAP永续/FUTURES交割
     * @param instId   【可选】交易对，例：BTC-USDT-SWAP
     */
    @GetMapping("/positions")
    public AjaxResult positions(
            @RequestParam(required = false) String instType,
            @RequestParam(required = false) String instId) {
        try {
            return success(okxApiClient.getPositions(instType, instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 3.账户配置 ======================================
    /**
     * 获取账户基础配置
     * 包含：持仓模式、杠杆模式、交易权限、手续费模板等
     * 无请求参数
     */
    @GetMapping("/config")
    public AjaxResult config() {
        try {
            return success(okxApiClient.getAccountConfig());
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 4.设置持仓模式 ======================================
    /**
     * 全局设置持仓模式
     * @param posMode 【必填】持仓模式
     *                net 单向持仓
     *                long_short 双向持仓
     */
    @PostMapping("/setPositionMode")
    public AjaxResult setPositionMode(@RequestParam String posMode) {
        try {
            return success(okxApiClient.setPositionMode(posMode));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 5.设置杠杆 ======================================
    /**
     * 指定交易对设置杠杆倍数
     * @param instId   【必填】交易对ID 例：BTC-USDT-SWAP
     * @param lever    【必填】杠杆倍数 字符串格式 例：10、20
     * @param mgnMode  【必填】保证金模式 cross全仓 / isolated逐仓
     */
    @PostMapping("/setLeverage")
    public AjaxResult setLeverage(
            @RequestParam String instId,
            @RequestParam String lever,
            @RequestParam String mgnMode) {
        try {
            return success(okxApiClient.setLeverage(instId, lever, mgnMode));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 6.当前账单流水 ======================================
    /**
     * 获取近期账单流水（近7天）
     * @param instType 【可选】产品类型
     * @param ccy      【可选】筛选币种
     * @param type     【可选】账单类型
     */
    @GetMapping("/bills")
    public AjaxResult bills(
            @RequestParam(required = false) String instType,
            @RequestParam(required = false) String ccy,
            @RequestParam(required = false) String type) {
        try {
            return success(okxApiClient.getBills(instType, ccy, type));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 7.历史账单 ======================================
    /**
     * 获取久远历史账单
     * @param instType 【必填】产品类型
     * @param ccy      【可选】筛选币种
     */
    @GetMapping("/billsHistory")
    public AjaxResult billsHistory(
            @RequestParam String instType,
            @RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getBillsHistory(instType, ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 8.借贷利率 ======================================
    /**
     * 获取杠杆借币实时利率
     * @param ccy 【可选】指定查询币种
     */
    @GetMapping("/interestRate")
    public AjaxResult interestRate(@RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getInterestRate(ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 9.最大可下单数量 ======================================
    /**
     * 查询当前币种最大可开仓/下单数量
     * @param instId  【必填】交易对
     * @param tdMode  【必填】交易模式 cross全仓 / isolated逐仓
     */
    @GetMapping("/maxSize")
    public AjaxResult maxSize(@RequestParam String instId, @RequestParam String tdMode) {
        try {
            return success(okxApiClient.getMaxSize(instId, tdMode));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 10.最大可平仓数量 ======================================
    /**
     * 查询持仓最大可平仓数量
     * @param instId   【必填】交易对
     * @param mgnMode  【必填】保证金模式 cross / isolated
     */
    @GetMapping("/maxAvail")
    public AjaxResult maxAvail(@RequestParam String instId, @RequestParam String mgnMode) {
        try {
            return success(okxApiClient.getMaxAvail(instId, mgnMode));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 11.调整保证金 ======================================
    /**
     * 手动增减逐仓持仓保证金
     * @param posId 【必填】持仓ID
     * @param amt   【必填】调整金额
     * @param type  【必填】1增加保证金 / 2减少保证金
     */
    @PostMapping("/adjustMargin")
    public AjaxResult adjustMargin(
            @RequestParam String posId,
            @RequestParam String amt,
            @RequestParam String type) {
        try {
            return success(okxApiClient.adjustMargin(posId, amt, type));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 12.手续费费率 ======================================
    /**
     * 查询账户交易手续费费率
     * @param instType 【必填】产品类型
     * @param instId   【可选】指定交易对
     */
    @GetMapping("/tradeFeeRate")
    public AjaxResult tradeFeeRate(
            @RequestParam String instType,
            @RequestParam(required = false) String instId) {
        try {
            return success(okxApiClient.getTradeFeeRate(instType, instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 13.账户总资产估值 ======================================
    /**
     * 获取账户整体资产估值、折合法币/稳定币总值
     * 无请求参数
     */
    @GetMapping("/equity")
    public AjaxResult equity() {
        try {
            return success(okxApiClient.getAccountEquity());
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 14.逐仓一键借币 ======================================
    /**
     * 逐仓模式借入币种
     * @param ccy    【必填】借币币种
     * @param amt    【必填】借入数量
     * @param instId 【必填】目标逐仓交易对
     */
    @PostMapping("/borrowIsolated")
    public AjaxResult borrowIsolated(
            @RequestParam String ccy,
            @RequestParam String amt,
            @RequestParam String instId) {
        try {
            return success(okxApiClient.borrowIsolated(ccy, amt, instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 15.逐仓归还借币 ======================================
    /**
     * 归还逐仓借入资产
     * @param ccy    【必填】还款币种
     * @param amt    【必填】还款数量
     * @param instId 【必填】目标逐仓交易对
     */
    @PostMapping("/repayIsolated")
    public AjaxResult repayIsolated(
            @RequestParam String ccy,
            @RequestParam String amt,
            @RequestParam String instId) {
        try {
            return success(okxApiClient.repayIsolated(ccy, amt, instId));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 16.爆仓历史记录 ======================================
    /**
     * 查询强制平仓/爆仓历史
     * @param instType 【必填】产品类型
     * @param mgnMode  【必填】保证金模式
     */
    @GetMapping("/liquidationHistory")
    public AjaxResult liquidationHistory(
            @RequestParam String instType,
            @RequestParam String mgnMode) {
        try {
            return success(okxApiClient.getLiquidationHistory(instType, mgnMode));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ====================================== 17.杠杆借贷历史 ======================================
    /**
     * 全仓/逐仓借还款完整记录
     * @param ccy 【可选】筛选指定币种
     */
    @GetMapping("/borrowHistory")
    public AjaxResult borrowHistory(@RequestParam(required = false) String ccy) {
        try {
            return success(okxApiClient.getBorrowHistory(ccy));
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

}