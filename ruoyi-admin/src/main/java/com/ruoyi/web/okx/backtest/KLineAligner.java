package com.ruoyi.web.okx.backtest;

import com.ruoyi.web.okx.vo.KLineVO;

/**
 * 多时间框架K线对齐工具
 *
 * 问题：BacktestEngine 用同一个索引 i 访问 15M/1H/4H 数组，
 *       但 1H 根数约为 15M 的 1/4，4H 约为 1/16，直接用同一索引会越界或错位。
 *
 * 解决：按时间戳对齐 —— 对每根 15M K线，找到时间戳 ≤ 它的最新 1H/4H K线索引。
 */
public class KLineAligner {

    /**
     * 预计算对齐索引表
     * alignTable[i] = 在 target 数组中，时间戳 ≤ source[i].timestamp 的最大索引
     *
     * @param source 基准数组（如 15M，根数多）
     * @param target 目标数组（如 1H/4H，根数少）
     * @return 长度与 source 相同的索引数组，值为 target 中对应的索引（-1 表示无对应）
     */
    public static int[] buildAlignTable(KLineVO[] source, KLineVO[] target) {
        int[] table = new int[source.length];
        int j = 0;
        for (int i = 0; i < source.length; i++) {
            long ts = source[i].getTimestamp();
            // 向前推进 j，直到 target[j+1].timestamp > ts
            while (j + 1 < target.length && target[j + 1].getTimestamp() <= ts) {
                j++;
            }
            // 确认 target[j].timestamp <= ts
            table[i] = (target[j].getTimestamp() <= ts) ? j : -1;
        }
        return table;
    }

    /**
     * 根据对齐索引截取 target 数组的前缀切片（含 alignIdx）
     * 等价于 BacktestEngine.slice() 但使用对齐后的索引
     *
     * @param target   目标K线数组
     * @param alignIdx 对齐索引（buildAlignTable 的结果）
     * @param need     需要的根数
     */
    public static KLineVO[] slice(KLineVO[] target, int alignIdx, int need) {
        if (alignIdx < 0) return new KLineVO[0];
        int start = Math.max(0, alignIdx - need + 1);
        int len = alignIdx - start + 1;
        KLineVO[] res = new KLineVO[len];
        System.arraycopy(target, start, res, 0, len);
        return res;
    }
}
