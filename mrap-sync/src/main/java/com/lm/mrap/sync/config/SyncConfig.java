package com.lm.mrap.sync.config;

import com.lm.mrap.common.config.CommonConfig;

/**
 * @author liming
 * @version 1.0
 * @description: 导数服务默认配置
 * @date 2022/11/2 下午2:17
 */
public class SyncConfig {
    public static final int CORE_POOL_SIZE = CommonConfig.getIntegerConfigOrElse("sync.core_pool_size", 3);

    public static final String MODEL_NAME = CommonConfig.getStringConfigOrElse("sync.model_name", "Synchronization");

    public static final String TALBE_CONF_PATH = CommonConfig.getStringConfigOrElse("sync.table_conf_path", "conf/tables");

    public static final int MONITOR_INTERVAL = CommonConfig.getIntegerConfigOrElse("sync.monitor_interval", 1000 * 1000);


}
