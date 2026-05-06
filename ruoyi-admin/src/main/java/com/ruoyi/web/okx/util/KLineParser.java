package com.ruoyi.web.okx.util;

import com.ruoyi.web.okx.vo.KLineVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * K线解析工具（消除 LiveTradingService 和 QuantTradingController 中的重复代码）
 */
public class KLineParser {

    /**
     * 解析 OKX getCandlesticks 返回的原始JSON为 KLineVO 数组（时间升序）
     */
    public static KLineVO[] parse(String json) {
        List<KLineVO> list = new ArrayList<>();
        try {
            int s = json.indexOf("[[");
            int e = json.lastIndexOf("]]") + 2;
            if (s < 0 || e < 2) return new KLineVO[0];
            String arr = json.substring(s, e);
            String[] rows = arr.split("\\],\\[");
            for (String row : rows) {
                String[] f = row.replace("[", "").replace("]", "")
                        .replace("\"", "").split(",");
                if (f.length < 6) continue;
                KLineVO vo = new KLineVO();
                vo.setTimestamp(Long.parseLong(f[0].trim()));
                vo.setOpen(Double.parseDouble(f[1].trim()));
                vo.setHigh(Double.parseDouble(f[2].trim()));
                vo.setLow(Double.parseDouble(f[3].trim()));
                vo.setClose(Double.parseDouble(f[4].trim()));
                vo.setVolume(Double.parseDouble(f[5].trim()));
                list.add(vo);
            }
            // OKX 返回时间降序，反转为升序
            Collections.reverse(list);
        } catch (Exception ignored) {}
        return list.toArray(new KLineVO[0]);
    }
}
