package com.ruoyi.web.controller.okx;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.okx.OkxApiClient;
import com.ruoyi.web.okx.OkxProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * OKX 账户控制器
 * 提供账户余额查询接口
 */
@RestController
@RequestMapping("/okx/account")
public class OkxAccountController extends BaseController {

    // 注入配置文件中的 OKX 密钥信息
    @Autowired
    private OkxProperties okxProperties;

    /**
     * 查询账户余额接口
     * @param ccy  币种（可选，不传则查全部）
     * @return     统一返回结果
     */
    @GetMapping("/balance")
    public AjaxResult getBalance(@RequestParam(required = false) String ccy) {
        try {
            // 1. 创建 OKX API 客户端（传入密钥）
            OkxApiClient client = new OkxApiClient(
                    okxProperties.getApiKey(),
                    okxProperties.getSecretKey(),
                    okxProperties.getPassphrase()
            );

            // 2. 调用接口获取余额
            String result = client.getAccountBalance(ccy);

            // 3. 返回成功结果
            return AjaxResult.success(result);
        } catch (Exception e) {
            // 异常捕获，返回错误信息
            return AjaxResult.error(e.getMessage());
        }
    }
}