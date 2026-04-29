package com.ruoyi.web.okx.test;
import com.ruoyi.web.okx.strategy.Ma20Ma60Strategy;
import com.ruoyi.web.okx.util.OkxKlineUtil;
import com.ruoyi.web.okx.vo.KLineVO;
import java.util.List;

public class Ma20Ma60StrategyTest {
    public static void main(String[] args) throws Exception {
        List<KLineVO> klist = OkxKlineUtil.parseKline(OkxKlineUtil.getOkxKline());
        KLineVO[] k15m = klist.toArray(new KLineVO[0]);
        Ma20Ma60Strategy strategy = new Ma20Ma60Strategy();
        int signal = strategy.signal(k15m, null, null);
        System.out.println("===== " + strategy.name() + " =====");
        System.out.println("信号：" + (signal == 1 ? "做多" : signal == -1 ? "做空" : "观望"));
    }
}