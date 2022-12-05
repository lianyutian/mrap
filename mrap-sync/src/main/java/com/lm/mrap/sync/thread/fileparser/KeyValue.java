package com.lm.mrap.sync.thread.fileparser;

import com.lm.mrap.common.config.CommonConfig;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/15 上午10:45
 */
public class KeyValue {

    private final byte[] key;

    private final byte[] value;

    public KeyValue(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getValue() {
        return value;
    }

    public String getKeyStr() {
        return new String(key, CommonConfig.DEFAULT_CHARSET);
    }

    public String getValueStr() {
        return new String(value, CommonConfig.DEFAULT_CHARSET);
    }
}
