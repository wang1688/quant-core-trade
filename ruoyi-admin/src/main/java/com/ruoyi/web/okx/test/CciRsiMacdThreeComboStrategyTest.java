package com.ruoyi.web.okx.test;
import com.ruoyi.web.okx.strategy.CciRsiMacdThreeComboStrategy;
import com.ruoyi.web.okx.util.OkxKlineUtil;
import com.ruoyi.web.okx.vo.KLineVO;
import java.util.List;

public class CciRsiMacdThreeComboStrategyTest {
    public static void main(String[] args) throws Exception {
        List<KLineVO> klist = OkxKlineUtil.parseKline(OkxKlineUtil.getOkxKline());
        KLineVO[] k15m = klist.toArray(new KLineVO[0]);
        CciRsiMacdThreeComboStrategy s = new CciRsiMacdThreeComboStrategy();
        int sig = s.signal(k15m, null, null);
        System.out.println("===== " + s.name() + " =====");
        System.out.println("信号：" + (sig == 1 ? "做多" : sig == -1 ? "做空" : "观望"));
    }
}