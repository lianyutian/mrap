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

    public static final int MONITOR_INTERVAL = CommonConfig.getIntegerConfigOrElse("sync.monitor_interval", 10);

    public static final int HANDLER_INTERVAL = CommonConfig.getIntegerConfigOrElse("sync.handler_interval", 3);

    public static final int HBASE_WRITE_BATCH_SIZE = CommonConfig.getIntegerConfigOrElse("sync.hbaseBatchSize", 2000);

    public static final int FILE_READER_MAX_FILE = CommonConfig.getIntegerConfigOrElse("sync.maxFileSize", 6);

    public static final int FILE_READER_MAX_BYTES_SIZE = CommonConfig.getIntegerConfigOrElse("sync.maxReadBytes", 3 * 1024 * 1024 * 1024);

    public static final int PARSER_THREAD_NUM = CommonConfig.getIntegerConfigOrElse("sync.parserThreadNum", 6);

    public static final int BATCH_QUEUE_LIMIT = CommonConfig.getIntegerConfigOrElse("sync.batchQueueLimit", 1000000);

    public static final int BATCH_SIZE = CommonConfig.getIntegerConfigOrElse("sync.batchSize", 1024 * 1024);

    public static final int WRITER_TASK_WAIT = CommonConfig.getIntegerConfigOrElse("sync.taskWaitingTime", 3600);

    public static final int HBASE_WRITER_THREAD = CommonConfig.getIntegerConfigOrElse("sync.hbaseWriterThread", 32);




}
