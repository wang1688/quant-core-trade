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

@RestController
@RequestMapping("/okx/account")
public class OkxAccountController extends BaseController {

    @Autowired
    private OkxProperties okxProperties;

    @GetMapping("/balance")
    public AjaxResult getBalance(@RequestParam(required = false) String ccy) {
        try {
            OkxApiClient client = new OkxApiClient(
                    okxProperties.getApiKey(),
                    okxProperties.getSecretKey(),
                    okxProperties.getPassphrase()
            );
            String result = client.getAccountBalance(ccy);
            return AjaxResult.success(result);
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
