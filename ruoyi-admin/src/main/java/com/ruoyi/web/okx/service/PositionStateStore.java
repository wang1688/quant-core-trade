package com.ruoyi.web.okx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 持仓状态持久化（JSON文件）
 * 防止应用重启后丢失持仓状态，导致重复开仓或漏平
 */
@Component
public class PositionStateStore {

    private static final Path STATE_FILE = Paths.get(
            System.getProperty("user.home"), ".okx-trade", "position-state.json");

    public static class PositionState {
        public int holdDirection;   // 1=多，-1=空，0=无仓
        public double entryPrice;
        public int holdContracts;
        public String posId;        // OKX持仓ID，用于设置止盈止损

        public boolean hasPosition() { return holdDirection != 0; }
    }

    public void save(int direction, double entryPrice, int contracts, String posId) {
        try {
            ensureDir();
            JSONObject obj = new JSONObject();
            obj.put("holdDirection", direction);
            obj.put("entryPrice", entryPrice);
            obj.put("holdContracts", contracts);
            obj.put("posId", posId != null ? posId : "");
            Files.writeString(STATE_FILE, obj.toJSONString());
        } catch (IOException e) {
            System.err.println("[PositionStateStore] 保存失败：" + e.getMessage());
        }
    }

    public PositionState load() {
        try {
            if (!Files.exists(STATE_FILE)) return new PositionState();
            String json = Files.readString(STATE_FILE);
            JSONObject obj = JSON.parseObject(json);
            PositionState state = new PositionState();
            state.holdDirection  = obj.getIntValue("holdDirection");
            state.entryPrice     = obj.getDoubleValue("entryPrice");
            state.holdContracts  = obj.getIntValue("holdContracts");
            state.posId          = obj.getString("posId");
            return state;
        } catch (Exception e) {
            System.err.println("[PositionStateStore] 读取失败：" + e.getMessage());
            return new PositionState();
        }
    }

    public void clear() {
        try {
            Files.deleteIfExists(STATE_FILE);
        } catch (IOException e) {
            System.err.println("[PositionStateStore] 清除失败：" + e.getMessage());
        }
    }

    private void ensureDir() throws IOException {
        File dir = STATE_FILE.getParent().toFile();
        if (!dir.exists()) dir.mkdirs();
    }
}
