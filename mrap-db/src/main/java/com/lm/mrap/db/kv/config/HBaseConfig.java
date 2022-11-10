package com.lm.mrap.db.kv.config;

import com.lm.mrap.common.config.CommonConfig;

/**
 * @author liming
 * @version 1.0
 * @description: Hbase 配置类
 * @date 2022/11/7 下午4:53
 */
public class HBaseConfig {

    public static final String[] EXCHANGE_TABLE_QUAILIFIERS = {"old_table", "active_table", "update_time"};

    public static final String[] HIS_INFO_TABLE_QUAILIFIERS = {"table_name", "write_time", "model_name"};

    public static final String COLUMN_FAMILY = CommonConfig.getStringConfigOrElse("hbase.column_family", "data");

    public static final int MAX_VERSIONS = CommonConfig.getIntegerConfigOrElse("hbase.max_version", 1);

    public static final boolean BLOCK_CACHE_ENABLED = CommonConfig.getBooleanConfigOrElse("hbase.max_version", true);

    public static final String BLOOM_FILTER_TYPE = CommonConfig.getStringConfigOrElse("hbase.bloom_filter_type", "ROW");

    public static final String THREAD_SLEEP = CommonConfig.getStringConfigOrElse("hbase.thread_sleep", "100-500");
}
