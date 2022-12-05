package com.lm.mrap.db.kv.config;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/11 下午2:21
 */
public class HBaseConfTable {

    private final HBaseConfTableType hBaseConfTableType;

    public HBaseConfTable(HBaseConfTableType hBaseConfTableType) {
        this.hBaseConfTableType = hBaseConfTableType;
    }

    public HBaseConfTableType gethBaseConfTableType() {
        return hBaseConfTableType;
    }
}
