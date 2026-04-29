package com.ruoyi.web.okx.test;
import com.ruoyi.web.okx.strategy.Ma10Ma30Strategy;
import com.ruoyi.web.okx.util.OkxKlineUtil;
import com.ruoyi.web.okx.vo.KLineVO;
import java.util.List;

public class Ma10Ma30StrategyTest {
    public static void main(String[] args) throws Exception {
        List<KLineVO> klist = OkxKlineUtil.parseKline(OkxKlineUtil.getOkxKline());
        KLineVO[] k15m = klist.toArray(new KLineVO[0]);
        Ma10Ma30Strategy s = new Ma10Ma30Strategy();
        int sig = s.signal(k15m, null, null);
        System.out.println("===== " + s.name() + " =====");
        System.out.println("信号：" + (sig == 1 ? "做多" : sig == -1 ? "做空" : "观望"));
    }
}